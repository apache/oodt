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
package org.apache.oodt.cas.workflow.engine;

//JDK imports
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.queue.QueueManager;
import org.apache.oodt.cas.workflow.engine.runner.EngineRunner;
import org.apache.oodt.cas.workflow.event.WorkflowEngineEvent;
import org.apache.oodt.cas.workflow.event.repo.WorkflowEngineEventRepository;
import org.apache.oodt.cas.workflow.exceptions.EngineException;
import org.apache.oodt.cas.workflow.instance.TaskInstance;
import org.apache.oodt.cas.workflow.instance.repo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.model.WorkflowGraph;
import org.apache.oodt.cas.workflow.model.WorkflowModel;
import org.apache.oodt.cas.workflow.model.repo.WorkflowModelRepository;
import org.apache.oodt.cas.workflow.page.PageFilter;
import org.apache.oodt.cas.workflow.page.QueryPage;
import org.apache.oodt.cas.workflow.page.QueuePage;
import org.apache.oodt.cas.workflow.page.RunnablesPage;
import org.apache.oodt.cas.workflow.priority.Priority;
import org.apache.oodt.cas.workflow.priority.PriorityManager;
import org.apache.oodt.cas.workflow.processor.ProcessorInfo;
import org.apache.oodt.cas.workflow.processor.ProcessorSkeleton;
import org.apache.oodt.cas.workflow.processor.ProcessorStub;
import org.apache.oodt.cas.workflow.processor.WorkflowProcessor;
import org.apache.oodt.cas.workflow.processor.map.WorkflowProcessorMap;
import org.apache.oodt.cas.workflow.processor.repo.WorkflowProcessorRepository;
import org.apache.oodt.cas.workflow.server.channel.CommunicationChannelClient;
import org.apache.oodt.cas.workflow.state.WorkflowState;
import org.apache.oodt.cas.workflow.state.done.FailureState;
import org.apache.oodt.cas.workflow.state.done.OFFState;
import org.apache.oodt.cas.workflow.state.done.StoppedState;
import org.apache.oodt.cas.workflow.state.done.SuccessState;
import org.apache.oodt.cas.workflow.state.holding.PausedState;
import org.apache.oodt.cas.workflow.state.holding.UnknownState;
import org.apache.oodt.cas.workflow.state.initial.LoadedState;
import org.apache.oodt.cas.workflow.state.initial.NullState;
import org.apache.oodt.cas.workflow.state.running.ExecutingState;
import org.apache.oodt.cas.workflow.state.running.PostConditionEvalState;
import org.apache.oodt.cas.workflow.state.running.PreConditionEvalState;
import org.apache.oodt.cas.workflow.state.transition.ExecutionCompleteState;
import org.apache.oodt.cas.workflow.state.transition.PreConditionSuccessState;
import org.apache.oodt.cas.workflow.state.waiting.BlockedState;
import org.apache.oodt.cas.workflow.state.waiting.QueuedState;
import org.apache.oodt.cas.workflow.state.waiting.WaitingOnResourcesState;
import org.apache.oodt.cas.workflow.util.WorkflowUtils;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * The engine that executes and monitors {@link TaskInstance}s, which are
 * the physical executing representation of the abtract {@link WorkflowModel}s
 * provided.
 * </p>
 * 
 */
public class WorkflowEngineLocal implements WorkflowEngine {
	
	private static final Logger LOG = Logger.getLogger(WorkflowEngineLocal.class.getName());
	
	protected WorkflowModelRepository modelRepo;
	protected WorkflowProcessorMap processorMap;
	protected WorkflowInstanceRepository instanceRepo;
	protected EngineRunner runner;
	protected WorkflowEngineClient weClient;
	protected QueueManager queueManager;
	protected WorkflowEngineEventRepository eventRepo;
	protected Date launchDate;
	
	protected Map<String, WorkflowGraph> workflowGraphs;
	protected Map<String, Class<? extends WorkflowProcessor>> modelToProcessorMap;
	
	protected Thread runnerThread;
	protected boolean allowRunnerToWork;
	protected boolean pauseRunner;
	
	public WorkflowEngineLocal(WorkflowModelRepository modelRepo, WorkflowProcessorRepository processorRepo, WorkflowInstanceRepository instanceRepo, WorkflowEngineEventRepository eventRepo, WorkflowProcessorMap processorMap, PriorityManager priorityManager, EngineRunner runner, CommunicationChannelClient client, List<String> metadataKeysToCache, boolean debug) throws Exception {
		this.modelRepo = modelRepo;
		this.instanceRepo = instanceRepo;
		this.processorMap = processorMap;
		this.eventRepo = eventRepo;
		this.runner = runner;
		this.refreshModelToProcessorMapping();
		this.refreshModels();
		this.weClient = new WorkflowEngineClient();
		this.weClient.setCommunicationChannelClient(client);
		this.queueManager = new QueueManager(processorRepo, priorityManager, metadataKeysToCache, debug);
		this.allowRunnerToWork = true;
		this.launchDate = new Date();
		this.pauseRunner = false;
		
		// Task RUNNER thread
		runnerThread = new Thread(new Runnable() {
			public void run() {
				TaskInstance nextTask = null;
				while(allowRunnerToWork) {
					try {
						if (nextTask == null)
							nextTask = WorkflowEngineLocal.this.queueManager.getNext();
						while (!pauseRunner && allowRunnerToWork && nextTask != null && WorkflowEngineLocal.this.runner.hasOpenSlots(nextTask)) {
							nextTask.setNotifyEngine(WorkflowEngineLocal.this.weClient);
							String jobId = nextTask.getJobId();
							WorkflowEngineLocal.this.runner.execute(nextTask);
							if (!jobId.equals(nextTask.getJobId()))
								WorkflowEngineLocal.this.queueManager.setJobId(nextTask.getInstanceId(), nextTask.getModelId(), nextTask.getJobId());
							nextTask = WorkflowEngineLocal.this.queueManager.getNext();

							//take a breather
							try {
								synchronized(this) {
									this.wait(1);
								}
							}catch (Exception e){
							}
						}
					}catch (Exception e) {
						LOG.log(Level.SEVERE, "Engine failed while submitting jobs to its runner : " + e.getMessage(), e);
						if (nextTask != null) {
							WorkflowEngineLocal.this.queueManager.setState(nextTask.getInstanceId(), nextTask.getModelId(), new FailureState("Failed while submitting job to Runner : " + e.getMessage()));
							nextTask = null;
						}
					}
					
					try {
						synchronized(this) {
							do {
								this.wait(2000);
							}while (WorkflowEngineLocal.this.pauseRunner);
						}
					}catch (Exception e){}
				}
			}
		});
		if (!debug)
			runnerThread.start();
	}
	
	public void shutdown() throws EngineException {
		this.allowRunnerToWork = this.pauseRunner = false;
		try {
			runnerThread.join(5000);
		}catch(Exception e) {}
		this.queueManager.shutdown();
		try {
			this.runner.shutdown();
		}catch (Exception e) {}
	}
	
	public void pauseRunner() throws EngineException {
		this.pauseRunner = true;
	}
	
	public void resumeRunner() throws EngineException {
		this.pauseRunner = false;
	}
	
	public Date getLaunchDate() throws EngineException {
		return this.launchDate;
	}
	
	public void refreshModels() {
		try {
			this.workflowGraphs = this.modelRepo.loadGraphs(this.getSupportedProcessorIds());
		}catch (Exception e) {
    		LOG.log(Level.SEVERE, "Failed to refresh models : " + e.getMessage(), e);
		}
	}
	
	public void refreshModelToProcessorMapping() {
		try {
			this.modelToProcessorMap = this.processorMap.loadMapping();
		}catch (Exception e) {
    		LOG.log(Level.SEVERE, "Failed to refresh model to processor mapping : " + e.getMessage(), e);
		}
	}
	
	public Set<String> getSupportedProcessorIds() {
		return this.modelToProcessorMap.keySet();
	}
	
    public String startWorkflow(String modelId, Metadata inputMetadata) throws EngineException {
    	return this.startWorkflow(modelId, inputMetadata, null);
    }
    
	
    public String startWorkflow(String modelId, Metadata inputMetadata, Priority priority) throws EngineException {
    	try {    			
	    	WorkflowGraph graph = this.workflowGraphs.get(modelId);
	    	if (graph == null)
	    		throw new Exception("Server does not understand ModelId = '" + modelId + "'");
	    	return this.startWorkflow(graph, inputMetadata, priority);
    	}catch (Exception e) {
    		LOG.log(Level.SEVERE, "Failed to start workflow [modelId=" + modelId + "] : " + e.getMessage(), e);
    		return null;
    	}
    }

	public String startWorkflow(WorkflowGraph workflow, Metadata inputMetadata)
			throws EngineException {
		return this.startWorkflow(workflow, inputMetadata, null);
	}
	
    public String startWorkflow(WorkflowGraph workflow, Metadata inputMetadata, Priority priority) throws EngineException {
    	try {
			WorkflowProcessor processor = WorkflowUtils.buildProcessor(UUID
					.randomUUID().toString(), workflow, modelToProcessorMap);
			processor.setDynamicMetadataRecur(inputMetadata);
			if (priority != null)
				processor.setPriorityRecur(priority);
			this.queueManager.addToQueue(processor);
			LOG.log(Level.INFO, "Added instanceId = "
					+ processor.getInstanceId() + " for modelId = "
					+ workflow.getModel().getId() + " to queue");
			return processor.getInstanceId();
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to start workflow [modelId="
					+ workflow.getModel().getId() + "] : " + e.getMessage(), e);
			return null;
		}
    }
    
	public void setWorkflowState(String instanceId, String modelId, WorkflowState state) throws EngineException {
		this.queueManager.setState(instanceId, modelId, state);
	}
	
    public void setWorkflowPriority(String instanceId, String modelId, Priority priority) throws EngineException {
		this.queueManager.setPriority(instanceId, modelId, priority);
    }

	public void deleteWorkflow(String instanceId) throws EngineException {
		try {
			if (!this.queueManager.containsWorkflow(instanceId)) 
				throw new EngineException("Workflow '" + instanceId + "' is not managed by this engine");
			
			LOG.log(Level.INFO, "Deleting workflow '" + instanceId + "'");
			this.instanceRepo.removeInstanceMetadatas(instanceId);
			this.queueManager.deleteWorkflowProcessor(instanceId);
		}catch (Exception e) {
			throw new EngineException("Failed to delete workflow '" + instanceId + "' : " + e.getMessage(), e);
		}
	}

	public Metadata getInstanceMetadata(String jobId)
			throws EngineException {
		try {
			return this.instanceRepo.getInstanceMetadata(jobId);
		} catch (Exception e) {
			throw new EngineException("Failed to get instance metadata for " + jobId + " : " + e.getMessage(), e);
		}
	}

	public WorkflowInstanceRepository getInstanceRepository()
			throws EngineException {
		return this.instanceRepo;
	}

	public WorkflowModel getModel(String modelId) throws EngineException {
		WorkflowGraph graph = this.workflowGraphs.get(modelId);
		if (graph != null)
			return graph.getModel();
		 throw new EngineException("Model " + modelId + " is not understood by this engine");
	}
	
	public WorkflowGraph getWorkflowGraph(String modelId) throws EngineException {
		return this.workflowGraphs.get(modelId);
	}

	public List<WorkflowModel> getModels() throws EngineException {
		List<WorkflowModel> models = new Vector<WorkflowModel>();
		for (WorkflowGraph graph : this.workflowGraphs.values())
			models.add(graph.getModel());
		return models;
	}
	
    public List<WorkflowGraph> getWorkflowGraphs() throws EngineException {
		return new Vector<WorkflowGraph>(this.workflowGraphs.values());
    }
    
    public ProcessorInfo getProcessorInfo(String instanceId) throws EngineException {
    	return this.queueManager.getWorkflowProcessor(instanceId).getProcessorInfo();
    }

    public ProcessorInfo getProcessorInfo(String instanceId, String modelId) throws EngineException {
    	WorkflowProcessor wp = WorkflowUtils.findProcessor(this.queueManager.getWorkflowProcessor(instanceId), modelId);
    	return wp.getProcessorInfo();
    }

	public Metadata getWorkflowMetadata(String instanceId) throws EngineException {
		return this.queueManager.getWorkflowProcessor(instanceId).getDynamicMetadata();
	}
	
	public Metadata getWorkflowMetadata(String instanceId,
			String modelId) throws EngineException {
		return WorkflowUtils.findProcessor(this.queueManager.getWorkflowProcessor(instanceId), modelId).getDynamicMetadata();
	}

	public void pauseWorkflow(String instanceId) throws EngineException {
		this.queueManager.setState(instanceId, null, new PausedState(""));
	}

	public void resumeWorkflow(String instanceId) throws EngineException {
		this.queueManager.revertState(instanceId, null);
	}

	public void stopWorkflow(String instanceId)	throws EngineException {
		this.queueManager.setState(instanceId, null, new StoppedState(""));		
	}

	public void updateInstanceMetadata(String jobId, Metadata metadata)
			throws EngineException {
		try {
			this.instanceRepo.storeInstanceMetadata(jobId, metadata);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Failed to update instance metadata for " + jobId + " : " + e.getMessage(), e);
			throw new EngineException("Failed to update instance metadata for " + jobId + " : " + e.getMessage(), e);
		}
	}

	public void updateWorkflowMetadata(String instanceId,
			String modelId, Metadata metadata) throws EngineException {
		this.queueManager.setMetadata(instanceId, modelId, metadata);
	}

    public void updateWorkflowAndInstance(String instanceId, String modelId, WorkflowState state, Metadata metadata, String jobId, Metadata instanceMetadata) throws EngineException {
    	this.updateWorkflowMetadata(instanceId, modelId, metadata);
    	this.setWorkflowState(instanceId, modelId, state);
    	this.updateInstanceMetadata(jobId, instanceMetadata);
    }
	
    public void registerEvent(WorkflowEngineEvent event) throws EngineException {
    	try {
    		this.eventRepo.storeEvent(event);
    	}catch (Exception e) {
			throw new EngineException("Failed to register event " + event + " : " + e.getMessage(), e);
    	}
    }
    
    public void triggerEvent(String eventId, Metadata inputMetadata) throws EngineException {
    	try {
	    	WorkflowEngineEvent event = this.eventRepo.getEventById(eventId);
	    	if (event == null) {
	    		throw new Exception("Event " + eventId + " not registered with this server");
	    	}else {
	    		if (event.passesPreConditions(this)) {
					LOG.log(Level.INFO, "Tiggering event " + eventId + " with inputMetadata = " + inputMetadata.getHashtable());
					event.performAction(WorkflowEngineLocal.this, inputMetadata);
	    		}else {
	    			throw new EngineException("Event " + eventId + " failed to pass preconditions with inputMetadata = " + inputMetadata.getHashtable());
	    		}
	    	}
    	}catch (Exception e) {
			throw new EngineException("Failed to trigger event " + eventId + " : " + e.getMessage(), e);
    	}
    }
    
    public List<WorkflowEngineEvent> getRegisteredEvents() throws EngineException {
    	try {
    		List<WorkflowEngineEvent> events = new Vector<WorkflowEngineEvent>();
	    	for (String eventId : this.eventRepo.getEventIds())
	    		events.add(this.eventRepo.getEventById(eventId));
	    	return events;
		}catch (Exception e) {
			throw new EngineException("Failed to get supported events : " + e.getMessage(), e);
		}
    }

    public List<WorkflowState> getSupportedStates() throws EngineException {
    	return Arrays.asList(new NullState(), new LoadedState(""), new FailureState(""), new OFFState(""), new StoppedState(""), new SuccessState(""), new PausedState(""), new UnknownState(""), new ExecutingState(""), new PostConditionEvalState(""), new PreConditionEvalState(""), new ExecutionCompleteState(""), new PreConditionSuccessState(""), new BlockedState(""), new QueuedState(""), new WaitingOnResourcesState(""));
    }
    
    public int getNumOfLoadedProcessors() throws EngineException {
    	return this.queueManager.getNumOfLoadedProcessors();
    }
    
    public int getNumOfWorkflows() throws EngineException {
    	return this.queueManager.getNumOfProcessors();
    }
    
    public RunnablesPage getExecutingPage(PageInfo pageInfo) throws EngineException {
    	return this.queueManager.getExecutingPage(pageInfo);
    }
    
    public RunnablesPage getRunnablesPage(PageInfo pageInfo) throws EngineException {
    	return this.queueManager.getRunnablesPage(pageInfo);
    }
    
    public QueuePage getPage(PageInfo pageInfo) throws EngineException {
    	return this.queueManager.getPage(pageInfo);
    }

    public QueuePage getPage(PageInfo pageInfo, PageFilter filter) throws EngineException {
    	return this.queueManager.getPage(pageInfo, filter);
    }
    
    public QueuePage getPage(PageInfo pageInfo, Comparator<ProcessorStub> comparator) throws EngineException {
    	return this.queueManager.getPage(pageInfo, comparator);
    }
    
    public QueuePage getPage(PageInfo pageInfo, PageFilter filter, Comparator<ProcessorStub> comparator) throws EngineException {
    	return this.queueManager.getPage(pageInfo, filter, comparator);
    }
    
    public QueuePage getPage(PageInfo pageInfo, WorkflowState state) throws EngineException {
    	return this.queueManager.getPage(pageInfo, state);
    }

    public QueuePage getPage(PageInfo pageInfo, WorkflowState.Category category) throws EngineException {
    	return this.queueManager.getPage(pageInfo, category);
    }

    public QueuePage getPage(PageInfo pageInfo, String modelId) throws EngineException {
    	return this.queueManager.getPage(pageInfo, modelId);
    }
    
    public QueuePage getPage(PageInfo pageInfo, Map<String, List<String>> keyValPairs) throws EngineException {
    	return this.queueManager.getPage(pageInfo, keyValPairs);
    }

	public QueuePage getNextPage(QueuePage page) throws EngineException {
		if (page.getFilter() instanceof PageFilter)
			return this.getPage(new PageInfo(page.getPageInfo().getPageSize(), page.getPageInfo().getPageNum() + 1), (PageFilter) page.getFilter());
		else if (page.getFilter() instanceof Comparator)
			return this.getPage(new PageInfo(page.getPageInfo().getPageSize(), page.getPageInfo().getPageNum() + 1), (Comparator<ProcessorStub>) page.getFilter());
		else if (page.getFilter() instanceof WorkflowState)
			return this.getPage(new PageInfo(page.getPageInfo().getPageSize(), page.getPageInfo().getPageNum() + 1), (WorkflowState) page.getFilter());
		else if (page.getFilter() instanceof WorkflowState.Category)
			return this.getPage(new PageInfo(page.getPageInfo().getPageSize(), page.getPageInfo().getPageNum() + 1), (WorkflowState.Category) page.getFilter());
		else if (page.getFilter() instanceof String)
			return this.getPage(new PageInfo(page.getPageInfo().getPageSize(), page.getPageInfo().getPageNum() + 1), (String) page.getFilter());
		else if (page.getFilter() instanceof List)
			return this.getPage(new PageInfo(page.getPageInfo().getPageSize(), page.getPageInfo().getPageNum() + 1), (PageFilter) ((List) page.getFilter()).get(0), (Comparator<ProcessorStub>) ((List) page.getFilter()).get(1));
		else
			return this.getPage(new PageInfo(page.getPageInfo().getPageSize(), page.getPageInfo().getPageNum() + 1));
	}
	
    public ProcessorSkeleton getWorkflow(String instanceId) throws EngineException {
    	return this.queueManager.getWorkflowProcessor(instanceId).getSkeleton();
    }

    public WorkflowState getWorkflowState(String instanceId) throws EngineException {
    	return this.queueManager.getWorkflowProcessor(instanceId).getState();
    }
    
	public QueryPage getNextPage(QueryPage page) throws EngineException {
		try {
			return this.instanceRepo.getNextPage(page);
		}catch (Exception e) {
			throw new EngineException("Failed to get next page : " + e.getMessage(), e);
		}
	}
	
	public QueryPage getPage(PageInfo pageInfo, QueryExpression queryExpression) throws EngineException {
		try {
			return this.instanceRepo.getPage(pageInfo, queryExpression);
		}catch (Exception e) {
			throw new EngineException("Failed to get page : " + e.getMessage(), e);
		}
	}
	
	public List<Metadata> getMetadata(QueryPage page) throws EngineException {
		try {
			return this.instanceRepo.getMetadata(page);
		}catch (Exception e) {
			throw new EngineException("Failed to get metadata for page : " + e.getMessage(), e);
		}
	}
    
}
