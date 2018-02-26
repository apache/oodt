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

//JDK imports

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.processor.WorkflowProcessorQueue;
import org.apache.oodt.cas.workflow.engine.runner.EngineRunner;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycle;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowState;
import org.apache.oodt.cas.workflow.repository.WorkflowRepository;
import org.apache.oodt.cas.workflow.structs.HighestFIFOPrioritySorter;
import org.apache.oodt.cas.workflow.structs.ParentChildWorkflow;
import org.apache.oodt.cas.workflow.structs.Priority;
import org.apache.oodt.cas.workflow.structs.PrioritySorter;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.EngineException;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;

import java.net.URL;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * 
 * Describe your class here.
 * 
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 */
public class PrioritizedQueueBasedWorkflowEngine implements WorkflowEngine {

  private static final Logger LOG = Logger
      .getLogger(PrioritizedQueueBasedWorkflowEngine.class.getName());
  private final Thread queuerThread;
  private final Thread runnerThread;
  private final WorkflowInstanceRepository repo;
  private final WorkflowRepository modelRepo;
  private final WorkflowLifecycleManager lifecycle;
  private final PrioritySorter prioritizer;
  private WorkflowProcessorQueue processorQueue;
  private URL wmgrUrl;
  private EngineRunner runner;

  public PrioritizedQueueBasedWorkflowEngine(WorkflowInstanceRepository repo,
      PrioritySorter prioritizer, WorkflowLifecycleManager lifecycle,
      EngineRunner runner, WorkflowRepository modelRepo, long querierWaitSeconds) {
    this.repo = repo;
    this.prioritizer = prioritizer == null ? new HighestFIFOPrioritySorter(1,
        50, 1) : prioritizer;
    this.lifecycle = lifecycle;
    this.modelRepo = modelRepo;
    this.processorQueue = new WorkflowProcessorQueue(repo, lifecycle, modelRepo);
    this.runner = runner;
    this.runner.setInstanceRepository(repo);

    // Task QUEUER thread
    TaskQuerier querier = new TaskQuerier(processorQueue, this.prioritizer,
        this.repo, querierWaitSeconds);
    queuerThread = new Thread(querier);
    queuerThread.start();

    // Task Runner thread
    runnerThread = new Thread(new TaskRunner(querier, runner));
    runnerThread.start();

  }

  public void setEngineRunner(EngineRunner runner) {
    this.runner = runner;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#startWorkflow(org.apache
   * .oodt.cas.workflow.structs.Workflow, org.apache.oodt.cas.metadata.Metadata)
   */
  @Override
  public WorkflowInstance startWorkflow(Workflow workflow, Metadata metadata)
      throws EngineException {
    // TODO Auto-generated method stub

    // looks like the work to do here is
    // create a new WorkflowInstance
    // create a new WorkflowProcessor around it
    // set it in Queued status
    // commit it to workflow instance repo and it will get picked up

    WorkflowInstance inst = new WorkflowInstance();
    inst.setParentChildWorkflow(workflow instanceof ParentChildWorkflow ? (ParentChildWorkflow) workflow
        : new ParentChildWorkflow(workflow));
    inst.setStartDate(Calendar.getInstance().getTime());
    inst.setCurrentTaskId(workflow.getTasks().get(0).getTaskId());
    inst.setSharedContext(metadata);
    inst.setPriority(Priority.getDefault()); // FIXME: this should be sensed or
                                             // passed in
    WorkflowLifecycle cycle = getLifecycleForWorkflow(workflow);
    WorkflowState state = cycle.createState("Null", "initial",
        "Workflow created by Engine.");
    inst.setState(state);
    persist(inst);
    return inst;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#stopWorkflow(java.lang
   * .String)
   */
  @Override
  public void stopWorkflow(String workflowInstId) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#pauseWorkflowInstance
   * (java.lang.String)
   */
  @Override
  public void pauseWorkflowInstance(String workflowInstId) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#resumeWorkflowInstance
   * (java.lang.String)
   */
  @Override
  public void resumeWorkflowInstance(String workflowInstId) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#getInstanceRepository()
   */
  @Override
  public WorkflowInstanceRepository getInstanceRepository() {
    return this.repo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#updateMetadata(java.
   * lang.String, org.apache.oodt.cas.metadata.Metadata)
   */
  @Override
  public boolean updateMetadata(String workflowInstId, Metadata met) {
    // TODO Auto-generated method stub
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#setWorkflowManagerUrl
   * (java.net.URL)
   */
  @Override
  public void setWorkflowManagerUrl(URL url) {
    this.wmgrUrl = url;

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#getWallClockMinutes(
   * java.lang.String)
   */
  @Override
  public double getWallClockMinutes(String workflowInstId) {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.engine.WorkflowEngine#
   * getCurrentTaskWallClockMinutes(java.lang.String)
   */
  @Override
  public double getCurrentTaskWallClockMinutes(String workflowInstId) {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowEngine#getWorkflowInstanceMetadata
   * (java.lang.String)
   */
  @Override
  public Metadata getWorkflowInstanceMetadata(String workflowInstId) {
    // TODO Auto-generated method stub
    return null;
  }

  private synchronized void persist(WorkflowInstance inst) throws EngineException {
    try {
      if (inst.getId() == null || (inst.getId().equals(""))) {
        // we have to persist it by adding it
        // rather than updating it
        repo.addWorkflowInstance(inst);
      } else {
        // persist by update
        repo.updateWorkflowInstance(inst);
      }
    } catch (InstanceRepositoryException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new EngineException(e.getMessage());
    }
  }

  private WorkflowLifecycle getLifecycleForWorkflow(Workflow workflow) {
    return lifecycle.getLifecycleForWorkflow(workflow) != null ? lifecycle
        .getLifecycleForWorkflow(workflow) : lifecycle.getDefaultLifecycle();
  }

}
