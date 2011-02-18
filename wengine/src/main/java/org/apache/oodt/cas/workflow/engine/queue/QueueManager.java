/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.oodt.cas.workflow.engine.queue;

//OODT imports
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.catalog.page.ProcessedPageInfo;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.instance.TaskInstance;
import org.apache.oodt.cas.workflow.page.PageFilter;
import org.apache.oodt.cas.workflow.page.QueuePage;
import org.apache.oodt.cas.workflow.page.RunnablesPage;
import org.apache.oodt.cas.workflow.priority.HighestPriorityFirstManager;
import org.apache.oodt.cas.workflow.priority.Priority;
import org.apache.oodt.cas.workflow.priority.PriorityManager;
import org.apache.oodt.cas.workflow.processor.ProcessorStub;
import org.apache.oodt.cas.workflow.processor.TaskProcessor;
import org.apache.oodt.cas.workflow.processor.WorkflowProcessor;
import org.apache.oodt.cas.workflow.processor.repo.WorkflowProcessorRepository;
import org.apache.oodt.cas.workflow.state.RevertableWorkflowState;
import org.apache.oodt.cas.workflow.state.WorkflowState;
import org.apache.oodt.cas.workflow.state.running.ExecutingState;
import org.apache.oodt.cas.workflow.state.waiting.QueuedState;
import org.apache.oodt.cas.workflow.state.waiting.WaitingOnResourcesState;
import org.apache.oodt.cas.workflow.util.WorkflowUtils;

//JDK imports
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * Manages caching and uncaching, scheduling, and queuing of WorkflowProcessors
 *
 */
public class QueueManager {
	
	private static final Logger LOG = Logger.getLogger(QueueManager.class.getName());
	
	private Map<String, CachedWorkflowProcessor> processorQueue;
	private List<ProcessorStub> runnableTasks;
	private Map<String, ProcessorStub> executingTasks;
	private WorkflowProcessorRepository processorRepo;
	private WorkflowProcessorLock processorLock;
	private PriorityManager priorityManager;
	private List<String> metadataKeysToCache;
	private boolean debugMode;
	private boolean allowQueuerToWork;
	
	private Thread queuerThread;
	
	public QueueManager(WorkflowProcessorRepository processorRepo) {
		this(processorRepo, null, null, false);
	}
	
	public QueueManager(WorkflowProcessorRepository processorRepo, PriorityManager priorityManager) {
		this(processorRepo, priorityManager, null, false);
	}
	
	public QueueManager(WorkflowProcessorRepository processorRepo, boolean debugMode) {
		this(processorRepo, null, null, debugMode);
	}

	public QueueManager(WorkflowProcessorRepository processorRepo, List<String> metadataKeysToCache) {
		this(processorRepo, null, metadataKeysToCache, false);
	}
	
	public QueueManager(WorkflowProcessorRepository processorRepo, PriorityManager priorityManager, List<String> metadataKeysToCache, boolean debugMode) {
		this.processorQueue = Collections.synchronizedMap(new HashMap<String, CachedWorkflowProcessor>());
		this.runnableTasks = new Vector<ProcessorStub>();
		this.executingTasks = Collections.synchronizedMap(new HashMap<String, ProcessorStub>());
		this.processorLock = new WorkflowProcessorLock();
		this.processorRepo = processorRepo;
		this.priorityManager = priorityManager == null ? new HighestPriorityFirstManager() : priorityManager;
		if (metadataKeysToCache != null)
			this.metadataKeysToCache = new Vector<String>(metadataKeysToCache);
		this.debugMode = debugMode;
		this.allowQueuerToWork = true;
		
		try {
			this.loadProcessorRepo();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		// Task QUEUER thread
		queuerThread = new Thread(new Runnable() {
			public void run() {
				while(allowQueuerToWork) {
					try {
						Vector<CachedWorkflowProcessor> processors = null; 
						synchronized(QueueManager.this.processorQueue) {
							processors = new Vector<CachedWorkflowProcessor>(QueueManager.this.processorQueue.values());
						}
						List<ProcessorStub> runnableProcessors = new Vector<ProcessorStub>();
						for (CachedWorkflowProcessor cachedWP : processors) {
							if (!allowQueuerToWork)
								break;
							if (!(cachedWP.getStub().getState().getCategory().equals(WorkflowState.Category.DONE) || cachedWP.getStub().getState().getCategory().equals(WorkflowState.Category.HOLDING))) {
								cachedWP.uncache();
								if (!QueueManager.this.debugMode) {
									processorLock.lock(cachedWP.getInstanceId());
									WorkflowProcessor wp = cachedWP.getWorkflowProcessor();
									for (TaskProcessor tp : wp.getRunnableWorkflowProcessors()) {
										tp.setState(new WaitingOnResourcesState("Added to Runnable queue", new ExecutingState("")));
										runnableProcessors.add(tp.getStub());
									}
									processorLock.unlock(cachedWP.getInstanceId());
								}
								cachedWP.cache();
							}else {
								continue;
							}

							if (runnableProcessors.size() > 0) {
								synchronized (QueueManager.this.runnableTasks) {
									QueueManager.this.runnableTasks.addAll(runnableProcessors);
									QueueManager.this.priorityManager.sort(QueueManager.this.runnableTasks);
								}
							}
							runnableProcessors.clear();
							
							//take a breather
							try {
								synchronized(this) {
									this.wait(1);
								}
							}catch (Exception e){}
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				try {
					synchronized(this) {
						this.wait(2000);
					}
				}catch (Exception e){}
			}
		});
		queuerThread.start();
	}
		
	public void loadProcessorRepo() throws Exception {
		if (this.processorRepo != null) {
			for (String instanceId : this.processorRepo.getStoredInstanceIds())
				this.processorQueue.put(instanceId, new CachedWorkflowProcessor(instanceId));
		}
	}
	
	public void shutdown() {
		this.allowQueuerToWork = false;
		try {
			this.queuerThread.join(5000);
		}catch(Exception e) {}
	}
	
	public void addToQueue(WorkflowProcessor workflowProcessor) throws Exception {
		workflowProcessor.setStateRecur(new QueuedState(""));
		this.processorQueue.put(workflowProcessor.getInstanceId(), new CachedWorkflowProcessor(workflowProcessor));
	}
	
	public TaskInstance getNext() throws Exception {
		ProcessorStub stub = null;
		synchronized (this.runnableTasks) {
			if (!this.runnableTasks.isEmpty()) 
				stub = this.runnableTasks.remove(0);
		}
		if (stub != null) {
			CachedWorkflowProcessor cachedWP = this.processorQueue.get(stub.getInstanceId());
			cachedWP.uncache();
			processorLock.lock(cachedWP.getInstanceId());
			TaskProcessor taskProcessor = (TaskProcessor) WorkflowUtils.findProcessor(cachedWP.getWorkflowProcessor(), stub.getModelId());
			TaskInstance taskInstance = this.makeInstance(taskProcessor);
			this.executingTasks.put(taskProcessor.getInstanceId() + ":" + taskProcessor.getModelId(), taskProcessor.getStub());
			processorLock.unlock(cachedWP.getInstanceId());
			cachedWP.cache();
			return taskInstance;
		}else { 
			return null;
		}
	}
	
	private TaskInstance makeInstance(TaskProcessor taskProcessor) throws InstantiationException, IllegalAccessException {
		TaskInstance ti = taskProcessor.getInstanceClass().newInstance();
		ti.setInstanceId(taskProcessor.getInstanceId());
		ti.setDynamicMetadata(taskProcessor.getDynamicMetadata());
		ti.setStaticMetadata(taskProcessor.getStaticMetadata());
		ti.setModelId(taskProcessor.getModelId());
		ti.setExecutionType(taskProcessor.getExecutionType());
		if (taskProcessor.getJobId() == null) {
			ti.setJobId(UUID.randomUUID().toString());
			taskProcessor.setJobId(ti.getJobId());
		}else {
			ti.setJobId(taskProcessor.getJobId());
		}
		return ti;
	}
	
	public void revertState(String instanceId, String modelId) {
		try {
			CachedWorkflowProcessor cachedWP = this.processorQueue.get(instanceId);
			if (cachedWP != null) {
				cachedWP.uncache();
				processorLock.lock(cachedWP.getInstanceId());
				if (modelId != null)
					WorkflowUtils.findProcessor(cachedWP.getWorkflowProcessor(), modelId).revertState();
				else
					cachedWP.getWorkflowProcessor().revertState();
				WorkflowUtils.validateWorkflowProcessor(cachedWP.getWorkflowProcessor());
				processorLock.unlock(cachedWP.getInstanceId());
				cachedWP.cache();
			}
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to revert state for workflow [InstanceId = '" + instanceId + "', ModelId = '" + modelId + "'] : " + e.getMessage(), e);
		}
	}
	
	public void setState(String instanceId, String modelId, WorkflowState state) {
		try {
			CachedWorkflowProcessor cachedWP = this.processorQueue.get(instanceId);
			if (cachedWP != null) {
				cachedWP.uncache();
				processorLock.lock(cachedWP.getInstanceId());
				WorkflowProcessor wp = (modelId == null) ? cachedWP.getWorkflowProcessor() : WorkflowUtils.findProcessor(cachedWP.getWorkflowProcessor(), modelId);
				if (state instanceof RevertableWorkflowState)
					((RevertableWorkflowState) state).setPrevState(wp.getState());
				wp.setState(state);
				if (wp instanceof TaskProcessor) {
					if (this.executingTasks.containsKey(instanceId + ":" + modelId)) {
						if (!(state instanceof ExecutingState))
							this.executingTasks.remove(instanceId + ":" + modelId);
						else
							this.executingTasks.put(instanceId + ":" + modelId, wp.getStub());
					}else {
						this.updateRunnableStub(wp);
					}
				}
				processorLock.unlock(cachedWP.getInstanceId());
				cachedWP.cache();
			}
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to set state for workflow [InstanceId = '" + instanceId + "', ModelId = '" + modelId + "'] : " + e.getMessage(), e);
		}
	}
	
	public void setPriority(String instanceId, String modelId, Priority priority) {
		try {
			if (this.executingTasks.containsKey(instanceId + ":" + modelId)) {
				LOG.log(Level.WARNING, "Can't change the priority of an executing task [InstanceId = '" + instanceId + "', ModelId = '" + modelId + "'] : ");
				return;
			}
			CachedWorkflowProcessor cachedWP = this.processorQueue.get(instanceId);
			if (cachedWP != null) {
				cachedWP.uncache();
				processorLock.lock(cachedWP.getInstanceId());
				WorkflowProcessor wp = (modelId == null) ? cachedWP.getWorkflowProcessor() : WorkflowUtils.findProcessor(cachedWP.getWorkflowProcessor(), modelId);
				wp.setPriorityRecur(priority);
				if (wp instanceof TaskProcessor) 
					this.updateRunnableStub(wp);
				processorLock.unlock(cachedWP.getInstanceId());
				cachedWP.cache();
			}
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to set priority for workflow [InstanceId = '" + instanceId + "', ModelId = '" + modelId + "'] : " + e.getMessage(), e);
		}
	}
	
	public void setMetadata(String instanceId, String modelId, Metadata metadata) {
		try {
			CachedWorkflowProcessor cachedWP = this.processorQueue.get(instanceId);
			if (cachedWP != null) {
				cachedWP.uncache();
				processorLock.lock(cachedWP.getInstanceId());
				WorkflowProcessor wp = modelId == null ? cachedWP.getWorkflowProcessor() : WorkflowUtils.findProcessor(cachedWP.getWorkflowProcessor(), modelId);
				wp.setDynamicMetadata(metadata);
				processorLock.unlock(cachedWP.getInstanceId());
				cachedWP.cache();
			}
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to set metadata for workflow [InstanceId = '" + instanceId + "', ModelId = '" + modelId + "'] : " + e.getMessage(), e);
		}
	}
	
	public WorkflowProcessor getWorkflowProcessor(String instanceId) {
		CachedWorkflowProcessor cachedWP = this.processorQueue.get(instanceId);
		WorkflowProcessor returnProcessor = null;
		if (cachedWP != null) {
			cachedWP.uncache();
			processorLock.lock(instanceId);
			returnProcessor = cachedWP.getWorkflowProcessor();
			processorLock.unlock(instanceId);
			cachedWP.cache();
		}		
		return returnProcessor;
	}
	
	public boolean containsWorkflow(String instanceId) {
		return this.processorQueue.containsKey(instanceId);
	}
	
	public void deleteWorkflowProcessor(String instanceId) {
		CachedWorkflowProcessor cachedWP = this.processorQueue.remove(instanceId);
		if (cachedWP != null) {
			cachedWP.delete();
			this.processorLock.delete(instanceId);
			synchronized (this.runnableTasks) {
				for (int i = 0; i < this.runnableTasks.size(); i++) {
					ProcessorStub stub = this.runnableTasks.get(i);
					if (stub.getInstanceId().equals(instanceId)) 
						this.runnableTasks.remove(i--);
				}
			}
		}
	}
	
    public RunnablesPage getExecutingPage(PageInfo pageInfo) {
		List<ProcessorStub> executing = new Vector<ProcessorStub>(this.executingTasks.values());
    	Vector<ProcessorStub> pageWPs = new Vector<ProcessorStub>();
		int startIndex = (pageInfo.getPageNum() - 1) * pageInfo.getPageSize();
		for (int i = startIndex; i < startIndex + pageInfo.getPageSize() && i < executing.size(); i++) 
			pageWPs.add(executing.get(i));
		return new RunnablesPage(this.getProcessedPageInfo(pageInfo, executing.size()), pageWPs);
	}
	
    public RunnablesPage getRunnablesPage(PageInfo pageInfo) {
		List<ProcessorStub> runnables = new Vector<ProcessorStub>(this.runnableTasks);
    	Vector<ProcessorStub> pageWPs = new Vector<ProcessorStub>();
		int startIndex = (pageInfo.getPageNum() - 1) * pageInfo.getPageSize();
		for (int i = startIndex; i < startIndex + pageInfo.getPageSize() && i < runnables.size(); i++) 
			pageWPs.add(runnables.get(i));
		return new RunnablesPage(this.getProcessedPageInfo(pageInfo, runnables.size()), pageWPs);
	}
    	
    public QueuePage getPage(PageInfo pageInfo) {
    	return this.getPage(pageInfo, (Comparator<ProcessorStub>) null);
    }
    
    public QueuePage getPage(PageInfo pageInfo, PageFilter filter) {
    	Vector<CachedWorkflowProcessor> acceptedWPs = new Vector<CachedWorkflowProcessor>();
    	Vector<CachedWorkflowProcessor> cachedWPs = null;
    	synchronized(processorQueue) {
    		cachedWPs = new Vector<CachedWorkflowProcessor>(this.processorQueue.values());
    	}
		if (filter != null) 
			for (CachedWorkflowProcessor cachedWP : cachedWPs) 
				if (filter.accept(cachedWP.getStub(), cachedWP.getCachedMetadata()))
					acceptedWPs.add(cachedWP);
		return new QueuePage(this.getProcessedPageInfo(pageInfo, acceptedWPs.size()), this.getPage(pageInfo, acceptedWPs), filter);
    }
    
    public QueuePage getPage(PageInfo pageInfo, Comparator<ProcessorStub> comparator) {
		Vector<CachedWorkflowProcessor> sortedCachedWPs = null;
		synchronized(this.processorQueue) {
			sortedCachedWPs = new Vector<CachedWorkflowProcessor>(this.processorQueue.values());
		}
		if (comparator != null) {
			final Comparator<ProcessorStub> comparatorFinal = comparator;
			Collections.sort(sortedCachedWPs, new Comparator<CachedWorkflowProcessor>() {
				public int compare(CachedWorkflowProcessor o1,
						CachedWorkflowProcessor o2) {
					return comparatorFinal.compare(o1.getStub(), o2.getStub());
				}
			});
		}
		return new QueuePage(this.getProcessedPageInfo(pageInfo, sortedCachedWPs.size()), this.getPage(pageInfo, sortedCachedWPs), comparator);
    }
    
    public QueuePage getPage(PageInfo pageInfo, PageFilter filter, Comparator<ProcessorStub> comparator) {
    	Vector<CachedWorkflowProcessor> acceptedWPs = new Vector<CachedWorkflowProcessor>();
    	Vector<CachedWorkflowProcessor> cachedWPs = null;
    	synchronized(processorQueue) {
    		cachedWPs = new Vector<CachedWorkflowProcessor>(this.processorQueue.values());
    	}
		if (filter != null) 
			for (CachedWorkflowProcessor cachedWP : cachedWPs) 
				if (filter.accept(cachedWP.getStub(), cachedWP.getCachedMetadata()))
					acceptedWPs.add(cachedWP);
		if (comparator != null) {
			final Comparator<ProcessorStub> comparatorFinal = comparator;
			Collections.sort(cachedWPs, new Comparator<CachedWorkflowProcessor>() {
				public int compare(CachedWorkflowProcessor o1,
						CachedWorkflowProcessor o2) {
					return comparatorFinal.compare(o1.getStub(), o2.getStub());
				}
			});
		}
		return new QueuePage(this.getProcessedPageInfo(pageInfo, acceptedWPs.size()), this.getPage(pageInfo, acceptedWPs), Arrays.asList(filter, comparator));
    }
	
	public QueuePage getPage(PageInfo pageInfo, WorkflowState state) {
		List<CachedWorkflowProcessor> processorsOfGivenState = new Vector<CachedWorkflowProcessor>();
		Vector<CachedWorkflowProcessor> processorQueueValues = null;
		synchronized(this.processorQueue) {
			processorQueueValues = new Vector<CachedWorkflowProcessor>(this.processorQueue.values());
		}
		for (CachedWorkflowProcessor cachedWP : processorQueueValues) 
			if (cachedWP.getStub().getState().equals(state))
				processorsOfGivenState.add(cachedWP);
		return new QueuePage(this.getProcessedPageInfo(pageInfo, processorsOfGivenState.size()), this.getPage(pageInfo, processorsOfGivenState), state);
	}
	
    public QueuePage getPage(PageInfo pageInfo, WorkflowState.Category category) {
		List<CachedWorkflowProcessor> processorsOfGivenCategory = new Vector<CachedWorkflowProcessor>();
		Vector<CachedWorkflowProcessor> processorQueueValues = null;
		synchronized(this.processorQueue) {
			processorQueueValues = new Vector<CachedWorkflowProcessor>(this.processorQueue.values());
		}
		for (CachedWorkflowProcessor cachedWP : processorQueueValues) 
			if (cachedWP.getStub().getState().getCategory().equals(category))
				processorsOfGivenCategory.add(cachedWP);
		return new QueuePage(this.getProcessedPageInfo(pageInfo, processorsOfGivenCategory.size()), this.getPage(pageInfo, processorsOfGivenCategory), category);
    }
    
    public QueuePage getPage(PageInfo pageInfo, String modelId) {
		List<CachedWorkflowProcessor> processorsOfGivenModelId = new Vector<CachedWorkflowProcessor>();
		Vector<CachedWorkflowProcessor> processorQueueValues = null;
		synchronized(this.processorQueue) {
			processorQueueValues = new Vector<CachedWorkflowProcessor>(this.processorQueue.values());
		}
		for (CachedWorkflowProcessor cachedWP : processorQueueValues) 
			if (cachedWP.getStub().getModelId().equals(modelId))
				processorsOfGivenModelId.add(cachedWP);
		return new QueuePage(this.getProcessedPageInfo(pageInfo, processorsOfGivenModelId.size()), this.getPage(pageInfo, processorsOfGivenModelId), modelId);
    }
    
    public QueuePage getPage(PageInfo pageInfo, Map<String, List<String>> keyValPairs) {
		List<CachedWorkflowProcessor> queryResults = new Vector<CachedWorkflowProcessor>();
		Vector<CachedWorkflowProcessor> processorQueueValues = null;
		synchronized(this.processorQueue) {
			processorQueueValues = new Vector<CachedWorkflowProcessor>(this.processorQueue.values());
		}
		for (CachedWorkflowProcessor cachedWP : processorQueueValues) {
			Metadata cachedMetadata = cachedWP.getCachedMetadata();
			if (cachedMetadata.getAllKeys().size() > 0) {
				for (Entry<String, List<String>> entry : keyValPairs.entrySet()) {
					String value = cachedMetadata.getMetadata(entry.getKey());
					if (value != null && entry.getValue().contains(value))
						queryResults.add(cachedWP);
				}
			}
		}
		return new QueuePage(this.getProcessedPageInfo(pageInfo, queryResults.size()), this.getPage(pageInfo, queryResults), keyValPairs);
    }
    
    protected ProcessedPageInfo getProcessedPageInfo(PageInfo pageInfo, int numOfHits) {
    	return new ProcessedPageInfo(pageInfo.getPageSize(), pageInfo.getPageNum(),	numOfHits);
    }
    
    protected List<ProcessorStub> getPage(PageInfo pageInfo, List<CachedWorkflowProcessor> cachedWPs) {
    	Vector<ProcessorStub> pageWPs = new Vector<ProcessorStub>();
		int startIndex = (pageInfo.getPageNum() - 1) * pageInfo.getPageSize();
		for (int i = startIndex; i < startIndex + pageInfo.getPageSize() && i < cachedWPs.size(); i++) {
			CachedWorkflowProcessor cachedWP = cachedWPs.get(i);
			processorLock.lock(cachedWP.getInstanceId());
			pageWPs.add(cachedWP.getStub());
			processorLock.unlock(cachedWP.getInstanceId());
		}
    	return pageWPs;
    }
    
    protected void updateRunnableStub(WorkflowProcessor wp) {
		if (this.runnableTasks.remove(wp.getStub())) {
			if (wp.getState() instanceof WaitingOnResourcesState) {
				this.runnableTasks.add(wp.getStub());
				synchronized (this.runnableTasks) {
					this.priorityManager.sort(this.runnableTasks);
				}
			}
		}
	}
    
    public int getNumOfLoadedProcessors() {
    	int loaded = 0;
		Vector<CachedWorkflowProcessor> processorQueueValues = null;
		synchronized(this.processorQueue) {
			processorQueueValues = new Vector<CachedWorkflowProcessor>(this.processorQueue.values());
		}
		for (CachedWorkflowProcessor cachedWP : processorQueueValues) 
			if (cachedWP.processorStub != null)
				loaded++;
		return loaded;
    }
    
    public int getNumOfProcessors() {
    	return this.processorQueue.size();
    }
	
	private class CachedWorkflowProcessor {
		
		private String instanceId;
		private ProcessorStub processorStub;
		private Metadata cachedMetadata;
		private WorkflowProcessor wp;
		private int uncachedCalls;
		private boolean firstUncache;
		
		public CachedWorkflowProcessor(String instanceId) {
			this.instanceId = instanceId;
			this.processorStub = null;
			this.cachedMetadata = null;
			this.uncachedCalls = 0;
			this.firstUncache = true;
		}
		
		public CachedWorkflowProcessor(WorkflowProcessor workflowProcessor) {
			this(workflowProcessor, true);
		}
		
		public CachedWorkflowProcessor(WorkflowProcessor workflowProcessor, boolean cache) {
			this(workflowProcessor.getInstanceId());
			this.firstUncache = false;
			this.wp = workflowProcessor;
			if (cache)
				this.cache();
			else
				this.save();
		}
		
		public synchronized boolean isCached() {
			return this.wp == null;
		}
		
		public synchronized void delete() {
			try {
				if (QueueManager.this.processorRepo != null)
					QueueManager.this.processorRepo.delete(this.instanceId);
			}catch (Exception e) {
				LOG.log(Level.WARNING, "Failed to delete " + this.instanceId + " : " + e.getMessage(), e);
			}
		}
		
		public synchronized void save() {
			try {
				this.processorStub = this.wp.getStub();
				this.loadCachedMetadata();
				if (QueueManager.this.processorRepo != null)
					QueueManager.this.processorRepo.store(this.wp);
			}catch (Exception e) {
				LOG.log(Level.WARNING, "Failed to cache " + this.instanceId + " : " + e.getMessage(), e);
			}
		}
		
		public synchronized void cache() {
			this.save();
			if (this.uncachedCalls > 0) 
				this.uncachedCalls--;
			if (this.uncachedCalls == 0) {
				if (QueueManager.this.processorRepo != null)
					this.wp = null;	
			}
		}
		
		public synchronized void uncache() {
			try {
				if (this.isCached() && QueueManager.this.processorRepo != null)
					this.wp = QueueManager.this.processorRepo.load(this.instanceId);
				this.uncachedCalls++;
				if (this.firstUncache && !(this.getStub().getState().getCategory().equals(WorkflowState.Category.DONE) || this.getStub().getState().getCategory().equals(WorkflowState.Category.HOLDING))) {
					WorkflowUtils.validateWorkflowProcessor(this.wp);
					this.firstUncache = false;
				}
			}catch (Exception e) {
				LOG.log(Level.WARNING, "Failed to uncache " + this.instanceId + " : " + e.getMessage(), e);
			}
		}
		
		public synchronized String getInstanceId() {
			return this.instanceId;
		}
		
		public synchronized ProcessorStub getStub() {
			if (this.isCached()) {
				if (this.processorStub == null) {
					this.uncache();
					this.processorStub = this.wp.getStub();
					this.cache();
				}
			}else {
				this.processorStub = this.wp.getStub();
			}
			return this.processorStub;
		}
		
		public synchronized Metadata getCachedMetadata() {
			if (this.isCached()) {
				if (this.cachedMetadata == null) {
					this.uncache();
					this.loadCachedMetadata();
					this.cache();
				}
			}else {
				this.loadCachedMetadata();
			}
			return this.cachedMetadata != null ? this.cachedMetadata : new Metadata();
		}
		
		private void loadCachedMetadata() {
			if (QueueManager.this.metadataKeysToCache != null)
				this.cachedMetadata = this._getCachedMetadata();
		}
		
		private Metadata _getCachedMetadata() {
			Metadata m = new Metadata();
			for (String key : QueueManager.this.metadataKeysToCache) {
				List<String> values = this.wp.getDynamicMetadata().getAllMetadata(key);
				if (values == null)
					values = this.wp.getStaticMetadata().getAllMetadata(key);
				if (values != null)
					m.addMetadata(key, values);
			}
			return m;
		}
		
		public synchronized WorkflowProcessor getWorkflowProcessor() {
			try {
				return this.wp;
			}catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
	}
	
	private class WorkflowProcessorLock {
		
		private Map<String, ReadWriteLock> lockedProcessors;
		
		public WorkflowProcessorLock() {
			this.lockedProcessors = new HashMap<String, ReadWriteLock>();
		}
		
		public void lock(String instanceId) {
			ReadWriteLock lock = null;
			synchronized(this.lockedProcessors) {
				lock = this.lockedProcessors.get(instanceId);
				if (lock == null)
					this.lockedProcessors.put(instanceId, lock = new ReentrantReadWriteLock());
			}
			lock.writeLock().lock();
		}
		
		public void unlock(String instanceId) {
			ReadWriteLock lock = null;
			synchronized(this.lockedProcessors) {
				lock = this.lockedProcessors.get(instanceId);
			}
			lock.writeLock().unlock();
		}
		
		public void delete(String instanceId) {
			synchronized(this.lockedProcessors) {
				this.lockedProcessors.remove(instanceId);
			}
		}
		
	}
	
}
