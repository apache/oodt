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

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.resource.system.XmlRpcResourceManagerClient;
import org.apache.oodt.cas.workflow.structs.TaskJobInput;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowStatus;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.EngineException;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;
import org.apache.oodt.cas.workflow.engine.SequentialProcessor;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.metadata.CoreMetKeys;
import org.apache.oodt.commons.util.DateConvert;

//JDK imports
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

//java.util.concurrent imports
import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import EDU.oswego.cs.dl.util.concurrent.Channel;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * 
 * The ThreadPooling portion of the WorkflowEngine. This class is meant to be an
 * extension point for WorkflowEngines that want to implement ThreadPooling.
 * This WorkflowEngine provides everything needed to manage a ThreadPool using
 * Doug Lea's wonderful java.util.concurrent package that made it into JDK5.
 * 
 * @author mattmann
 * 
 */
public class ThreadPoolWorkflowEngine implements WorkflowEngine, WorkflowStatus {

  /* our thread pool */
  private PooledExecutor pool = null;

  /* our worker thread hash mapping worker threads to workflow instance ids */
  private HashMap workerMap = null;

  /* our log stream */
  private static final Logger LOG = Logger
      .getLogger(ThreadPoolWorkflowEngine.class.getName());

  /* our instance repository */
  private WorkflowInstanceRepository instRep = null;

  /* the URL pointer to the parent Workflow Manager */
  private URL wmgrUrl = null;

  /* how long to wait before checking whether a condition is satisfied. */
  private long conditionWait;

  private ConditionProcessor condProcessor;

  private EngineRunner runner;

  /**
   * Default Constructor.
   * 
   * @param instRep
   *          The WorkflowInstanceRepository to be used by this engine.
   * @param queueSize
   *          The size of the queue that the workflow engine should use
   *          (irrelevant if unlimitedQueue is set to true)
   * @param maxPoolSize
   *          The minimum thread pool size.
   * @param minPoolSize
   *          The maximum thread pool size.
   * @param threadKeepAliveTime
   *          The amount of minutes that each thread in the pool should be kept
   *          alive.
   * @param unlimitedQueue
   *          Whether or not to use a queue whose bounds are dictated by the
   *          physical memory of the underlying hardware.
   * @param resUrl
   *          A URL pointer to a resource manager. If this is set Tasks will be
   *          wrapped as Resource Manager {@link Job}s and sent through the
   *          Resource Manager. If this parameter is not set, local execution
   *          (the default) will be used
   */
  public ThreadPoolWorkflowEngine(WorkflowInstanceRepository instRep,
      int queueSize, int maxPoolSize, int minPoolSize,
      long threadKeepAliveTime, boolean unlimitedQueue, URL resUrl) {

    this.instRep = instRep;
    Channel c = null;
    if (unlimitedQueue) {
      c = new LinkedQueue();
    } else {
      c = new BoundedBuffer(queueSize);
    }

    pool = new PooledExecutor(c, maxPoolSize);
    pool.setMinimumPoolSize(minPoolSize);
    pool.setKeepAliveTime(1000 * 60 * threadKeepAliveTime);

    workerMap = new HashMap();

    if (resUrl != null) {
      this.runner = new ResourceRunner(resUrl);
    } else {
      this.runner = new AsynchronousLocalEngineRunner();
    }

    this.conditionWait = Long.getLong(
        "org.apache.oodt.cas.workflow.engine.preConditionWaitTime", 10)
        .longValue();

    this.condProcessor = new ConditionProcessor();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#pauseWorkflowInstance
   * (java.lang.String)
   */
  public synchronized void pauseWorkflowInstance(String workflowInstId) {
    // okay, try and look up that worker thread in our hash map
    SequentialProcessor worker = ((ThreadedExecutor) workerMap
        .get(workflowInstId)).getProcessor();
    if (worker == null) {
      LOG.log(Level.WARNING,
          "WorkflowEngine: Attempt to pause workflow instance id: "
              + workflowInstId
              + ", however, this engine is not tracking its execution");
      return;
    }

    // otherwise, all good
    worker.pause();

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#resumeWorkflowInstance
   * (java.lang.String)
   */
  public synchronized void resumeWorkflowInstance(String workflowInstId) {
    // okay, try and look up that worker thread in our hash map
    SequentialProcessor worker = ((ThreadedExecutor) workerMap
        .get(workflowInstId)).getProcessor();
    if (worker == null) {
      LOG.log(Level.WARNING,
          "WorkflowEngine: Attempt to resume workflow instance id: "
              + workflowInstId + ", however, this engine is "
              + "not tracking its execution");
      return;
    }

    // also check to make sure that the worker is currently paused
    // only can resume WorkflowInstances that are paused, right?
    if (true/*!worker.isPaused()*/) {
      LOG.log(Level.WARNING,
          "WorkflowEngine: Attempt to resume a workflow that "
              + "isn't paused currently: instance id: " + workflowInstId);
      return;
    }

    // okay, all good
    worker.resume();

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#startWorkflow(org.apache
   * .oodt.cas.workflow.structs.Workflow, org.apache.oodt.cas.metadata.Metadata)
   */
  public synchronized WorkflowInstance startWorkflow(Workflow workflow,
      Metadata metadata) throws EngineException {
    // to start the workflow, we create a default workflow instance
    // populate it
    // persist it
    // add it to the worker map
    // start it

    WorkflowInstance wInst = new WorkflowInstance();
    wInst.setWorkflow(workflow);
    wInst.setCurrentTaskId(((WorkflowTask) workflow.getTasks().get(0))
        .getTaskId());
    wInst.setSharedContext(metadata);
    wInst.setStatus(CREATED);
    persistWorkflowInstance(wInst);

    SequentialProcessor worker = new SequentialProcessor(wInst, instRep,
        this.wmgrUrl, this.conditionWait);
    workerMap.put(wInst.getId(), worker);

    wInst.setStatus(QUEUED);
    persistWorkflowInstance(wInst);

    try {
      pool.execute(new ThreadedExecutor(worker, this.condProcessor));
    } catch (InterruptedException e) {
      throw new EngineException(e);
    }

    return wInst;

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#getInstanceRepository()
   */
  public WorkflowInstanceRepository getInstanceRepository() {
    return this.instRep;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#updateMetadata(java.
   * lang.String, org.apache.oodt.cas.metadata.Metadata)
   */
  public synchronized boolean updateMetadata(String workflowInstId, Metadata met) {
    // okay, try and look up that worker thread in our hash map
    SequentialProcessor worker = ((ThreadedExecutor) workerMap
        .get(workflowInstId)).getProcessor();
    if (worker == null) {
      LOG.log(Level.WARNING,
          "WorkflowEngine: Attempt to update metadata context "
              + "for workflow instance id: " + workflowInstId
              + ", however, this engine is " + "not tracking its execution");
      return false;
    }

    worker.getWorkflowInstance().setSharedContext(met);
    try {
      persistWorkflowInstance(worker.getWorkflowInstance());
    } catch (Exception e) {
      LOG.log(
          Level.WARNING,
          "Exception persisting workflow instance: ["
              + worker.getWorkflowInstance().getId() + "]: Message: "
              + e.getMessage());
      return false;
    }

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#setWorkflowManagerUrl
   * (java.net.URL)
   */
  public void setWorkflowManagerUrl(URL url) {
    this.wmgrUrl = url;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#stopWorkflow(java.lang
   * .String)
   */
  public synchronized void stopWorkflow(String workflowInstId) {
    // okay, try and look up that worker thread in our hash map
    SequentialProcessor worker = ((ThreadedExecutor) workerMap
        .get(workflowInstId)).getProcessor();
    if (worker == null) {
      LOG.log(Level.WARNING,
          "WorkflowEngine: Attempt to stop workflow instance id: "
              + workflowInstId + ", however, this engine is "
              + "not tracking its execution");
      return;
    }

    worker.stop();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#
   * getCurrentTaskWallClockMinutes(java.lang.String)
   */
  public double getCurrentTaskWallClockMinutes(String workflowInstId) {
    // get the workflow instance that we're talking about
    WorkflowInstance inst = safeGetWorkflowInstanceById(workflowInstId);
    return getCurrentTaskWallClockMinutes(inst);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#getWorkflowInstanceMetadata
   * (java.lang.String)
   */
  public Metadata getWorkflowInstanceMetadata(String workflowInstId) {
    // okay, try and look up that worker thread in our hash map
    SequentialProcessor worker = ((ThreadedExecutor) workerMap
        .get(workflowInstId)).getProcessor();
    if (worker == null) {
      // try and get the metadata
      // from the workflow instance repository (as it was persisted)
      try {
        WorkflowInstance inst = instRep.getWorkflowInstanceById(workflowInstId);
        return inst.getSharedContext();
      } catch (InstanceRepositoryException e) {
        LOG.log(Level.FINEST, "WorkflowEngine: Attempt to get metadata "
            + "for workflow instance id: " + workflowInstId
            + ", however, this engine is "
            + "not tracking its execution and the id: [" + workflowInstId
            + "] " + "was never persisted to " + "the instance repository");
        e.printStackTrace();
        return new Metadata();
      }
    }

    return worker.getWorkflowInstance().getSharedContext();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#getWallClockMinutes(
   * java.lang.String)
   */
  public double getWallClockMinutes(String workflowInstId) {
    // get the workflow instance that we're talking about
    WorkflowInstance inst = safeGetWorkflowInstanceById(workflowInstId);
    return getWallClockMinutes(inst);
  }

  protected static double getWallClockMinutes(WorkflowInstance inst) {
    if (inst == null) {
      return 0.0;
    }

    Date currentDateOrStopTime = (inst.getEndDateTimeIsoStr() != null
        && !inst.getEndDateTimeIsoStr().equals("") && !inst
        .getEndDateTimeIsoStr().equals("null")) ? safeDateConvert(inst
        .getEndDateTimeIsoStr()) : new Date();

    Date workflowStartDateTime = null;

    if (inst.getStartDateTimeIsoStr() == null
        || (inst.getStartDateTimeIsoStr() != null && (inst
            .getStartDateTimeIsoStr().equals("") || inst
            .getStartDateTimeIsoStr().equals("null")))) {
      return 0.0;
    }

    try {
      workflowStartDateTime = DateConvert.isoParse(inst
          .getStartDateTimeIsoStr());
    } catch (ParseException e) {
      return 0.0;
    }

    long diffMs = currentDateOrStopTime.getTime()
        - workflowStartDateTime.getTime();
    double diffSecs = (diffMs * 1.0 / 1000.0);
    double diffMins = diffSecs / 60.0;
    return diffMins;

  }

  protected static double getCurrentTaskWallClockMinutes(WorkflowInstance inst) {
    if (inst == null) {
      return 0.0;
    }

    Date currentDateOrStopTime = (inst.getCurrentTaskEndDateTimeIsoStr() != null
        && !inst.getCurrentTaskEndDateTimeIsoStr().equals("") && !inst
        .getCurrentTaskEndDateTimeIsoStr().equals("null")) ? safeDateConvert(inst
        .getCurrentTaskEndDateTimeIsoStr()) : new Date();

    Date workflowTaskStartDateTime = null;

    if (inst.getCurrentTaskStartDateTimeIsoStr() == null
        || (inst.getCurrentTaskStartDateTimeIsoStr() != null && (inst
            .getCurrentTaskStartDateTimeIsoStr().equals("") || inst
            .getCurrentTaskStartDateTimeIsoStr().equals("null")))) {
      return 0.0;
    }

    try {
      workflowTaskStartDateTime = DateConvert.isoParse(inst
          .getCurrentTaskStartDateTimeIsoStr());
    } catch (ParseException e) {
      return 0.0;
    }

    // should never be in this state, so return 0
    if (workflowTaskStartDateTime.after(currentDateOrStopTime)) {
      LOG.log(
          Level.WARNING,
          "Start date time: ["
              + DateConvert.isoFormat(workflowTaskStartDateTime)
              + " of workflow inst [" + inst.getId() + "] is AFTER "
              + "End date time: ["
              + DateConvert.isoFormat(currentDateOrStopTime)
              + "] of workflow inst.");
      return 0.0;
    }

    long diffMs = currentDateOrStopTime.getTime()
        - workflowTaskStartDateTime.getTime();
    double diffSecs = (diffMs * 1.0 / 1000.0);
    double diffMins = diffSecs / 60.0;
    return diffMins;
  }

  private synchronized void persistWorkflowInstance(WorkflowInstance wInst)
      throws EngineException {

    try {
      if (wInst.getId() == null
          || (wInst.getId() != null && wInst.getId().equals(""))) {
        // we have to persist it by adding it
        // rather than updating it
        instRep.addWorkflowInstance(wInst);

      } else {
        // persist by update
        instRep.updateWorkflowInstance(wInst);
      }
    } catch (InstanceRepositoryException e) {
      e.printStackTrace();
      throw new EngineException(e.getMessage());
    }

  }

  protected WorkflowInstance safeGetWorkflowInstanceById(String workflowInstId) {
    try {
      return this.instRep.getWorkflowInstanceById(workflowInstId);
    } catch (Exception e) {
      return null;
    }
  }

  private static Date safeDateConvert(String isoTimeStr) {
    try {
      return DateConvert.isoParse(isoTimeStr);
    } catch (Exception ignore) {
      ignore.printStackTrace();
      return null;
    }
  }

  class ThreadedExecutor implements Runnable, CoreMetKeys {

    private SequentialProcessor processor;

    private boolean running;

    private ConditionProcessor conditionEvaluator;

    public ThreadedExecutor(SequentialProcessor processor,
        ConditionProcessor conditionEvaluator) {
      this.processor = processor;
      this.running = false;
      this.conditionEvaluator = conditionEvaluator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      String startDateTimeIsoStr = DateConvert.isoFormat(new Date());
      this.getProcessor().getWorkflowInstance()
          .setStartDateTimeIsoStr(startDateTimeIsoStr);
      this.getProcessor().persistWorkflowInstance();

      while (running && this.getProcessor().getRunnableSubProcessors() != null
          && this.getProcessor().getRunnableSubProcessors().size() > 0) {
        if (isPaused()) {
          LOG.log(
              Level.FINE,
              "SequentialProcessor: Skipping execution: Paused: CurrentTask: "
                  + this.getProcessor().getTaskNameById(
                      this.getProcessor().getCurrentTaskId()));
          continue;
        }

        TaskProcessor taskProcessor = (TaskProcessor) this.processor
            .getRunnableSubProcessors().get(0);
        WorkflowTask task = taskProcessor.getTask();
        this.getProcessor().getWorkflowInstance()
            .setCurrentTaskId(task.getTaskId());

        this.getProcessor().persistWorkflowInstance();
        if (!taskProcessor.checkTaskRequiredMetadata(this.getProcessor()
            .getWorkflowInstance().getSharedContext())) {
          this.getProcessor().getWorkflowInstance().setStatus(METADATA_MISSING);
          this.getProcessor().persistWorkflowInstance();
          return;
        }

        if (task.getConditions() != null) {
          if (!this.conditionEvaluator.satisfied(task.getConditions(),
              task.getTaskId(), this.getProcessor().getWorkflowInstance()
                  .getSharedContext())
              && isRunning()) {

            LOG.log(Level.FINEST,
                "Pre-conditions for task: " + task.getTaskName()
                    + " unsatisfied");

            if (!isPaused()) {
              this.getProcessor().getWorkflowInstance()
                  .setStatus(WorkflowStatus.PAUSED);
            }
            continue;
          }
        }
        LOG.log(Level.FINEST, "Executing task: " + task.getTaskName());

        this.addStdWorkflowMetadata(getProcessor().getWorkflowInstance(), task,
            getProcessor().getWorkflowInstance().getSharedContext(), wmgrUrl);

        if (runner instanceof ResourceRunner) {
          getProcessor().getWorkflowInstance().setStatus(RESMGR_SUBMIT);
          //persistWorkflowInstance();
          /*runner.execute(task, getProcessor().getWorkflowInstance()
              .getSharedContext());*/
        } else {
          //this.workflowInstance.setStatus(STARTED);
          //this.persistWorkflowInstance();
          String currentTaskIsoStartDateTimeStr = DateConvert
              .isoFormat(new Date());
          //this.workflowInstance
              //.setCurrentTaskStartDateTimeIsoStr(currentTaskIsoStartDateTimeStr);
          //this.workflowInstance.setCurrentTaskEndDateTimeIsoStr(null);
          /*runner.execute(task, getProcessor().getWorkflowInstance()
              .getSharedContext());*/

          String currentTaskIsoEndDateTimeStr = DateConvert
              .isoFormat(new Date());
          //this.workflowInstance
            //  .setCurrentTaskEndDateTimeIsoStr(currentTaskIsoEndDateTimeStr);
         // this.persistWorkflowInstance();
        }

        LOG.log(Level.FINEST, "Completed task: " + task.getTaskName());

      }

      /*LOG.log(Level.FINEST, "Completed workflow: "
          + this.workflowInstance.getWorkflow().getName());
      if (isRunning()) {
        stop();
      }*/
    }

    /**
     * @return the processor
     */
    public SequentialProcessor getProcessor() {
      return processor;
    }

    /**
     * @param processor
     *          the processor to set
     */
    public void setProcessor(SequentialProcessor processor) {
      this.processor = processor;
    }

    /**
     * @return the running
     */
    public boolean isRunning() {
      return running;
    }

    /**
     * @param running
     *          the running to set
     */
    public void setRunning(boolean running) {
      this.running = running;
    }

    /**
     * @return the conditionEvaluator
     */
    public ConditionProcessor getConditionEvaluator() {
      return conditionEvaluator;
    }

    /**
     * @param conditionEvaluator
     *          the conditionEvaluator to set
     */
    public void setConditionEvaluator(ConditionProcessor conditionEvaluator) {
      this.conditionEvaluator = conditionEvaluator;
    }

    public boolean isPaused() {
      return this.getProcessor().getWorkflowInstance().getStatus()
          .equals(WorkflowStatus.PAUSED);
    }

    protected void addStdWorkflowMetadata(WorkflowInstance wInst,
        WorkflowTask task, Metadata ctx, URL wUrl) {
      ctx.replaceMetadata(TASK_ID, task.getTaskId());
      ctx.replaceMetadata(WORKFLOW_INST_ID, wInst.getId());
      ctx.replaceMetadata(JOB_ID, wInst.getId());
      ctx.replaceMetadata(PROCESSING_NODE, getHostname());
      ctx.replaceMetadata(WORKFLOW_MANAGER_URL, wUrl.toString());
    }

    protected String getHostname() {
      try {
        // Get hostname by textual representation of IP address
        InetAddress addr = InetAddress.getLocalHost();
        // Get the host name
        String hostname = addr.getHostName();
        return hostname;
      } catch (UnknownHostException e) {
      }
      return null;
    }

  }

}
