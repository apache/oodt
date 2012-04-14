/**
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

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.structs.HighestFIFOPrioritySorter;
import org.apache.oodt.cas.workflow.structs.PrioritySorter;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowStatus;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.exceptions.EngineException;
import org.apache.oodt.commons.util.DateConvert;

/**
 * 
 * Describe your class here.
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 */
public class PrioritizedQueueBasedWorkflowEngine implements WorkflowEngine {

  
  private static final Logger LOG = Logger.getLogger(PrioritizedQueueBasedWorkflowEngine.class.getName());
  
  private Map<String, WorkflowPro> processorQueue;
  private List<WorkflowProcessor> runnableTasks;
  private Map<String, WorkflowProcessor> executingTasks;
  private WorkflowProcessorLock processorLock;
  private List<String> metadataKeysToCache;
  private boolean debugMode;
  private boolean allowQueuerToWork;  
  private Thread queuerThread;
  private WorkflowInstanceRepository repo;  
  private PrioritySorter prioritizer;
  private URL wmgrUrl;  
  private long conditionWait;
  
  public PrioritizedQueueBasedWorkflowEngine(WorkflowInstanceRepository repo, PrioritySorter prioritizer, long conditionWait){
    this.repo = repo;
    this.prioritizer = prioritizer != null ? new HighestFIFOPrioritySorter(secondsBetweenBoosts, boostAmount, boostCap):
      prioritizer;
    this.wmgrUrl = null;
    this.conditionWait = conditionWait;
    this.processorQueue = Collections.synchronizedMap(new HashMap<String, CachedWorkflowProcessor>());
    this.runnableTasks = new Vector<WorkflowProcessor>();
    this.executingTasks = Collections.synchronizedMap(new HashMap<String, WorkflowProcessor>());
    this.processorLock = new WorkflowProcessorLock();
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
    queuerThread = new Thread(new TaskQuerier());
    queuerThread.start();
  }
  

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#startWorkflow(org.apache.oodt.cas.workflow.structs.Workflow, org.apache.oodt.cas.metadata.Metadata)
   */
  @Override
  public WorkflowInstance startWorkflow(Workflow workflow, Metadata metadata)
      throws EngineException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#stopWorkflow(java.lang.String)
   */
  @Override
  public void stopWorkflow(String workflowInstId) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#pauseWorkflowInstance(java.lang.String)
   */
  @Override
  public void pauseWorkflowInstance(String workflowInstId) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#resumeWorkflowInstance(java.lang.String)
   */
  @Override
  public void resumeWorkflowInstance(String workflowInstId) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#getInstanceRepository()
   */
  @Override
  public WorkflowInstanceRepository getInstanceRepository() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#updateMetadata(java.lang.String, org.apache.oodt.cas.metadata.Metadata)
   */
  @Override
  public boolean updateMetadata(String workflowInstId, Metadata met) {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#setWorkflowManagerUrl(java.net.URL)
   */
  @Override
  public void setWorkflowManagerUrl(URL url) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#getWallClockMinutes(java.lang.String)
   */
  @Override
  public double getWallClockMinutes(String workflowInstId) {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#getCurrentTaskWallClockMinutes(java.lang.String)
   */
  @Override
  public double getCurrentTaskWallClockMinutes(String workflowInstId) {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#getWorkflowInstanceMetadata(java.lang.String)
   */
  @Override
  public Metadata getWorkflowInstanceMetadata(String workflowInstId) {
    // TODO Auto-generated method stub
    return null;
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
    WorkflowProcessor stub = null;
    synchronized (this.runnableTasks) {
      if (!this.runnableTasks.isEmpty()) 
        stub = this.runnableTasks.remove(0);
    }
    if (stub != null) {
      CachedWorkflowProcessor cachedWP = this.processorQueue.get(stub.getInstanceId());
      try {
        cachedWP.uncache();
        processorLock.lock(cachedWP.getInstanceId());
        TaskProcessor taskProcessor = (TaskProcessor) WorkflowUtils.findProcessor(cachedWP.getWorkflowProcessor(), stub.getModelId());
        TaskInstance taskInstance = this.makeInstance(taskProcessor);
        this.executingTasks.put(taskProcessor.getInstanceId() + ":" + taskProcessor.getModelId(), taskProcessor.getStub());
        return taskInstance;
      }catch (Exception e) {
        throw e;
      }finally {
        processorLock.unlock(cachedWP.getInstanceId());
        cachedWP.cache();
      }
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
    ti.setJobId(UUID.randomUUID().toString());
    taskProcessor.setJobId(ti.getJobId());
    return ti;
  }
  
  public void revertState(String instanceId, String modelId) {
    try {
      CachedWorkflowProcessor cachedWP = this.processorQueue.get(instanceId);
      if (cachedWP != null) {
        try {
          cachedWP.uncache();
          processorLock.lock(cachedWP.getInstanceId());
          if (modelId != null)
            WorkflowUtils.findProcessor(cachedWP.getWorkflowProcessor(), modelId).revertState();
          else
            cachedWP.getWorkflowProcessor().revertState();
          WorkflowUtils.validateWorkflowProcessor(cachedWP.getWorkflowProcessor());
        }catch (Exception e) {
          throw e;
        }finally {
          processorLock.unlock(cachedWP.getInstanceId());
          cachedWP.cache();
        }
      }
    }catch (Exception e) {
      LOG.log(Level.SEVERE, "Failed to revert state for workflow [InstanceId = '" + instanceId + "', ModelId = '" + modelId + "'] : " + e.getMessage(), e);
    }
  }
  
  public void setJobId(String instanceId, String modelId, String jobId) {
    try {
      CachedWorkflowProcessor cachedWP = this.processorQueue.get(instanceId);
      if (cachedWP != null) {
        try {
          cachedWP.uncache();
          processorLock.lock(cachedWP.getInstanceId());
          WorkflowProcessor wp = (modelId == null) ? cachedWP.getWorkflowProcessor() : WorkflowUtils.findProcessor(cachedWP.getWorkflowProcessor(), modelId);
          if (wp instanceof TaskProcessor)
            ((TaskProcessor) wp).setJobId(jobId);
        }catch (Exception e) {
          throw e;
        }finally {
          processorLock.unlock(cachedWP.getInstanceId());
          cachedWP.cache();
        }
      }
    }catch (Exception e) {
      LOG.log(Level.SEVERE, "Failed to set state for workflow [InstanceId = '" + instanceId + "', ModelId = '" + modelId + "'] : " + e.getMessage(), e);
    }
  }
  
  public void setState(String instanceId, String modelId, WorkflowState state) {
    try {
      CachedWorkflowProcessor cachedWP = this.processorQueue.get(instanceId);
      if (cachedWP != null) {
        try {
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
        }catch (Exception e) {
          throw e;
        }finally {
          processorLock.unlock(cachedWP.getInstanceId());
          cachedWP.cache();
        }
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
        try {
          cachedWP.uncache();
          processorLock.lock(cachedWP.getInstanceId());
          WorkflowProcessor wp = (modelId == null) ? cachedWP.getWorkflowProcessor() : WorkflowUtils.findProcessor(cachedWP.getWorkflowProcessor(), modelId);
          wp.setPriorityRecur(priority);
          if (wp instanceof TaskProcessor) 
            this.updateRunnableStub(wp);
        }catch (Exception e) {
          throw e;
        }finally {
          processorLock.unlock(cachedWP.getInstanceId());
          cachedWP.cache();
        }
      }
    }catch (Exception e) {
      LOG.log(Level.SEVERE, "Failed to set priority for workflow [InstanceId = '" + instanceId + "', ModelId = '" + modelId + "'] : " + e.getMessage(), e);
    }
  }
  
  public void setMetadata(String instanceId, String modelId, Metadata metadata) {
    try {
      CachedWorkflowProcessor cachedWP = this.processorQueue.get(instanceId);
      if (cachedWP != null) {
        try {
          cachedWP.uncache();
          processorLock.lock(cachedWP.getInstanceId());
          WorkflowProcessor wp = modelId == null ? cachedWP.getWorkflowProcessor() : WorkflowUtils.findProcessor(cachedWP.getWorkflowProcessor(), modelId);
          wp.setDynamicMetadata(metadata);
        }catch (Exception e) {
          throw e;
        }finally {
          processorLock.unlock(cachedWP.getInstanceId());
          cachedWP.cache();
        }
      }
    }catch (Exception e) {
      LOG.log(Level.SEVERE, "Failed to set metadata for workflow [InstanceId = '" + instanceId + "', ModelId = '" + modelId + "'] : " + e.getMessage(), e);
    }
  }
  
  public WorkflowProcessor getWorkflowProcessor(String instanceId) {
    CachedWorkflowProcessor cachedWP = this.processorQueue.get(instanceId);
    WorkflowProcessor returnProcessor = null;
    if (cachedWP != null) {
      try {
        cachedWP.uncache();
        processorLock.lock(instanceId);
        returnProcessor = cachedWP.getWorkflowProcessor();
      }catch (RuntimeException e) {
        throw e;
      }finally {
        processorLock.unlock(instanceId);
        cachedWP.cache();
      }
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
          WorkflowProcessor stub = this.runnableTasks.get(i);
          if (stub.getInstanceId().equals(instanceId)) 
            this.runnableTasks.remove(i--);
        }
      }
    }
  }
  
    public RunnablesPage getExecutingPage(PageInfo pageInfo) {
    List<WorkflowProcessor> executing = new Vector<WorkflowProcessor>(this.executingTasks.values());
      Vector<WorkflowProcessor> pageWPs = new Vector<WorkflowProcessor>();
    int startIndex = (pageInfo.getPageNum() - 1) * pageInfo.getPageSize();
    for (int i = startIndex; i < startIndex + pageInfo.getPageSize() && i < executing.size(); i++) 
      pageWPs.add(executing.get(i));
    return new RunnablesPage(this.getProcessedPageInfo(pageInfo, executing.size()), pageWPs);
  }
  
    public RunnablesPage getRunnablesPage(PageInfo pageInfo) {
    List<WorkflowProcessor> runnables = new Vector<WorkflowProcessor>(this.runnableTasks);
      Vector<WorkflowProcessor> pageWPs = new Vector<WorkflowProcessor>();
    int startIndex = (pageInfo.getPageNum() - 1) * pageInfo.getPageSize();
    for (int i = startIndex; i < startIndex + pageInfo.getPageSize() && i < runnables.size(); i++) 
      pageWPs.add(runnables.get(i));
    return new RunnablesPage(this.getProcessedPageInfo(pageInfo, runnables.size()), pageWPs);
  }
      
    public QueuePage getPage(PageInfo pageInfo) {
      return this.getPage(pageInfo, (Comparator<WorkflowProcessor>) null);
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
    
    public QueuePage getPage(PageInfo pageInfo, Comparator<WorkflowProcessor> comparator) {
    Vector<CachedWorkflowProcessor> sortedCachedWPs = null;
    synchronized(this.processorQueue) {
      sortedCachedWPs = new Vector<CachedWorkflowProcessor>(this.processorQueue.values());
    }
    if (comparator != null) {
      final Comparator<WorkflowProcessor> comparatorFinal = comparator;
      Collections.sort(sortedCachedWPs, new Comparator<CachedWorkflowProcessor>() {
        public int compare(CachedWorkflowProcessor o1,
            CachedWorkflowProcessor o2) {
          return comparatorFinal.compare(o1.getStub(), o2.getStub());
        }
      });
    }
    return new QueuePage(this.getProcessedPageInfo(pageInfo, sortedCachedWPs.size()), this.getPage(pageInfo, sortedCachedWPs), comparator);
    }
    
    public QueuePage getPage(PageInfo pageInfo, PageFilter filter, final Comparator<WorkflowProcessor> comparator) {
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
      Collections.sort(acceptedWPs, new Comparator<CachedWorkflowProcessor>() {
        public int compare(CachedWorkflowProcessor o1,
            CachedWorkflowProcessor o2) {
          return comparator.compare(o1.getStub(), o2.getStub());
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
      return new ProcessedPageInfo(pageInfo.getPageSize(), pageInfo.getPageNum(), numOfHits);
    }
    
    protected List<WorkflowProcessor> getPage(PageInfo pageInfo, List<CachedWorkflowProcessor> cachedWPs) {
      Vector<WorkflowProcessor> pageWPs = new Vector<WorkflowProcessor>();
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
      if (cachedWP.WorkflowProcessor != null)
        loaded++;
    return loaded;
    }
    
    public int getNumOfProcessors() {
      return this.processorQueue.size();
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
  
  
    private boolean isRunnableInstance(WorkflowInstance instance){
       return !instance.getStatus().equals(WorkflowStatus.ERROR) && 
       !instance.getStatus().equals(WorkflowStatus.FINISHED) && 
       !instance.getStatus().equals(WorkflowStatus.METADATA_MISSING) && 
       !instance.getStatus().equals(WorkflowStatus.PAUSED);
    }

  }

