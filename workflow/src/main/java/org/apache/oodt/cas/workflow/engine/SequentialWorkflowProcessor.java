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
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.metadata.CoreMetKeys;
import org.apache.oodt.cas.workflow.structs.TaskJobInput;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowStatus;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;
import org.apache.oodt.commons.util.DateConvert;

//JDK imports
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * An instance of the {@link WorkflowProcessor} that processes through an
 * iterative {@link this.workflowInstance}. This class keeps an
 * <code>Iterator</code> that allows it to move from one end of a sequential
 * {@link Workflow} processing pipeline to another. This class should only be
 * used to process science pipeline style {@link Workflow}s, i.e., those which
 * resemble an iterative processing pipelines, with no forks, or concurrent task
 * executions.
 * 
 * @author mattmann
 * 
 */

public class SequentialWorkflowProcessor extends WorkflowProcessor implements
    WorkflowStatus, CoreMetKeys, Runnable {

  private Iterator<WorkflowTask> taskIterator;

  /* our log stream */
  private static Logger LOG = Logger
      .getLogger(SequentialWorkflowProcessor.class.getName());

  public SequentialWorkflowProcessor(WorkflowInstance wInst,
      WorkflowInstanceRepository instRep, URL wParentUrl, long conditionWait) {
    super(wInst, instRep, wParentUrl, conditionWait);
    taskIterator = this.workflowInstance.getWorkflow().getTasks().iterator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  public void run() {

    String startDateTimeIsoStr = DateConvert.isoFormat(new Date());
    this.workflowInstance.setStartDateTimeIsoStr(startDateTimeIsoStr);
    this.persistWorkflowInstance();

    while (running && taskIterator.hasNext()) {
      if (isPaused()) {
        LOG.log(Level.FINE,
            "SequentialProcessor: Skipping execution: Paused: CurrentTask: "
                + getTaskNameById(this.workflowInstance.getCurrentTaskId()));
        continue;
      }

      WorkflowTask task = (WorkflowTask) taskIterator.next();
      this.workflowInstance.setCurrentTaskId(task.getTaskId());

      this.persistWorkflowInstance();
      if (!checkTaskRequiredMetadata(task,
          this.workflowInstance.getSharedContext())) {
        this.workflowInstance.setStatus(METADATA_MISSING);
        this.persistWorkflowInstance();
        return;
      }

      if (task.getConditions() != null) {
        while (!satisfied(task.getConditions(), task.getTaskId())
            && isRunning()) {

          if (!isPaused()) {
            pause();
          }

          LOG.log(Level.FINEST,
              "Pre-conditions for task: " + task.getTaskName()
                  + " unsatisfied: waiting: " + waitForConditionSatisfy
                  + " seconds before checking again.");
          try {
            Thread.currentThread().sleep(waitForConditionSatisfy * 1000);
          } catch (InterruptedException ignore) {
          }

          if (!isPaused()) {
            break;
          }
        }

        if (!isRunning()) {
          break;
        }

        if (isPaused()) {
          resume();
        }
      }
      LOG.log(
          Level.FINEST,
          "IterativeWorkflowProcessorThread: Executing task: "
              + task.getTaskName());

      WorkflowTaskInstance taskInstance = GenericWorkflowObjectFactory
          .getTaskObjectFromClassName(task.getTaskInstanceClassName());
      this.workflowInstance.getSharedContext().replaceMetadata(TASK_ID,
          task.getTaskId());
      this.workflowInstance.getSharedContext().replaceMetadata(
          WORKFLOW_INST_ID, this.workflowInstance.getId());
      this.workflowInstance.getSharedContext().replaceMetadata(JOB_ID,
          this.workflowInstance.getId());
      this.workflowInstance.getSharedContext().replaceMetadata(PROCESSING_NODE,
          getHostname());
      this.workflowInstance.getSharedContext().replaceMetadata(
          WORKFLOW_MANAGER_URL, this.wmgrParentUrl.toString());

      if (rClient != null) {
        Job taskJob = new Job();
        taskJob.setName(task.getTaskId());
        taskJob
            .setJobInstanceClassName("org.apache.oodt.cas.workflow.structs.TaskJob");
        taskJob
            .setJobInputClassName("org.apache.oodt.cas.workflow.structs.TaskJobInput");
        taskJob.setLoadValue(new Integer(2));
        taskJob
            .setQueueName(task.getTaskConfig().getProperty(QUEUE_NAME) != null ? task
                .getTaskConfig().getProperty(QUEUE_NAME) : DEFAULT_QUEUE_NAME);

        TaskJobInput in = new TaskJobInput();
        in.setDynMetadata(this.workflowInstance.getSharedContext());
        in.setTaskConfig(task.getTaskConfig());
        in.setWorkflowTaskInstanceClassName(task.getTaskInstanceClassName());

        this.workflowInstance.setStatus(RESMGR_SUBMIT);
        this.persistWorkflowInstance();

        try {
          this.currentJobId = rClient.submitJob(taskJob, in);
          while (!safeCheckJobComplete(this.currentJobId) && isRunning()) {
            try {
              Thread.currentThread().sleep(pollingWaitTime * 1000);
            } catch (InterruptedException ignore) {
            }
          }

          if (!isRunning()) {
            break;
          }

          WorkflowInstance updatedInst = null;
          try {
            updatedInst = instanceRepository
                .getWorkflowInstanceById(this.workflowInstance.getId());
            this.workflowInstance = updatedInst;
          } catch (InstanceRepositoryException e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Unable to get " + "updated workflow "
                + "instance record " + "when executing remote job: Message: "
                + e.getMessage());
          }

        } catch (JobExecutionException e) {
          LOG.log(Level.WARNING,
              "Job execution exception using resource manager to execute job: Message: "
                  + e.getMessage());
        }
      } else {
        this.workflowInstance.setStatus(STARTED);
        String currentTaskIsoStartDateTimeStr = DateConvert
            .isoFormat(new Date());
        this.workflowInstance
            .setCurrentTaskStartDateTimeIsoStr(currentTaskIsoStartDateTimeStr);
        this.workflowInstance.setCurrentTaskEndDateTimeIsoStr(null); 
        this.persistWorkflowInstance();
        this.executeTaskLocally(taskInstance,
            this.workflowInstance.getSharedContext(), task.getTaskConfig(),
            task.getTaskName());
        String currentTaskIsoEndDateTimeStr = DateConvert.isoFormat(new Date());
        this.workflowInstance
            .setCurrentTaskEndDateTimeIsoStr(currentTaskIsoEndDateTimeStr);
        this.persistWorkflowInstance();
      }

      LOG.log(Level.FINEST, "SequentialWorkflowProcessor: Completed task: "
          + task.getTaskName());

    }

    LOG.log(Level.FINEST, "SequentialWorkflowProcessor: Completed workflow: "
        + this.workflowInstance.getWorkflow().getName());
    if (isRunning()) {
      stop();
    }

  }

  public synchronized void stop() {
    running = false;
    if (this.rClient != null && this.currentJobId != null) {
      if (!this.rClient.killJob(this.currentJobId)) {
        LOG.log(Level.WARNING, "Attempt to kill " + "current resmgr job: ["
            + this.currentJobId + "]: failed");
      }
    }

    this.workflowInstance.setStatus(FINISHED);
    String isoEndDateTimeStr = DateConvert.isoFormat(new Date());
    this.workflowInstance.setEndDateTimeIsoStr(isoEndDateTimeStr);
    this.persistWorkflowInstance();
  }

  public synchronized void resume() {
    this.paused = false;
    this.workflowInstance.setStatus(STARTED);
    this.persistWorkflowInstance();
  }

  public synchronized void pause() {
    this.paused = true;
    this.workflowInstance.setStatus(PAUSED);
    this.persistWorkflowInstance();
  }

}
