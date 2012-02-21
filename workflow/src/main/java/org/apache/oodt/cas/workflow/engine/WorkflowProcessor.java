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
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;

/**
 * An abstract base class representing the methodology for processing a
 * {@link WorkflowInstance}. The job of this class is to actually take the
 * WorkflowInstance and execute its jobs. The class should maintain the state of
 * the instance, such as the currentTaskId, and so forth.
 * 
 */
public abstract class WorkflowProcessor {

  private static final Logger LOG = Logger.getLogger(WorkflowProcessor.class
      .getName());

  protected WorkflowInstance workflowInstance;

  protected long waitForConditionSatisfy = -1;

  protected WorkflowInstanceRepository instanceRepository = null;

  protected long pollingWaitTime = 10L;

  protected boolean running = false;

  protected int timesPaused;

  protected URL wmgrParentUrl = null;

  protected String currentJobId = null;

  public WorkflowProcessor(WorkflowInstance workflowInstance,
      WorkflowInstanceRepository instRep, URL wParentUrl, long conditionWait) {
    this.workflowInstance = workflowInstance;
    this.instanceRepository = instRep;
    this.running = true;
    this.waitForConditionSatisfy = conditionWait;
    this.pollingWaitTime = conditionWait;
    this.wmgrParentUrl = wParentUrl;
  }

  /**
   * @return the workflowInstance
   */
  public WorkflowInstance getWorkflowInstance() {
    return workflowInstance;
  }

  /**
   * @param workflowInstance
   *          the workflowInstance to set
   */
  public void setWorkflowInstance(WorkflowInstance workflowInstance) {
    this.workflowInstance = workflowInstance;
  }

  /**
   * Returns the identifier of the current {@link WorkflowTask} being processed
   * by this WorkflowProcessor.
   * 
   * @return the identifier of the current {@link WorkflowTask} being processed
   *         by this WorkflowProcessor.
   */
  public String getCurrentTaskId() {
    return this.workflowInstance.getCurrentTaskId();
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
  
  @Override
  public String toString(){
    StringBuilder builder = new StringBuilder();
    builder.append("processor:[type=");
    builder.append(getClass().toString());
    builder.append(",startdate=");
    builder.append(getWorkflowInstance().getStartDate());
    builder.append(",priority=");
    builder.append(getWorkflowInstance().getPriority());
    builder.append("]");
    return builder.toString();
  }

  /**
   * Gets the runnable consituent elements of this {@link WorkflowProcessor}
   * which may include {@link WorkflowProcessor}s themselves.
   * 
   * @return The current runnable set of {@link WorkflowProcessor}s that this 
   * {@link WorkflowProcessor} is modeling.
   */
  protected abstract List<WorkflowProcessor> getRunnableSubProcessors();
  
  protected void persistWorkflowInstance() {
    try {
      instanceRepository.updateWorkflowInstance(this.workflowInstance);
    } catch (InstanceRepositoryException e) {
      LOG.log(Level.WARNING, "Exception persisting workflow instance: ["
          + this.workflowInstance.getId() + "]: Message: " + e.getMessage());
    }
  }

  protected void executeTaskLocally(WorkflowTaskInstance instance,
      Metadata met, WorkflowTaskConfiguration cfg, String taskName) {
    try {
      LOG.log(Level.INFO, "Executing task: [" + taskName + "] locally");
      instance.run(met, cfg);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.log(Level.WARNING, "Exception executing task: [" + taskName
          + "] locally: Message: " + e.getMessage());
    }
  }

  protected String getTaskNameById(String taskId) {
    for (WorkflowTask task : (List<WorkflowTask>) (List<?>) this.workflowInstance
        .getWorkflow().getTasks()) {
      if (task.getTaskId().equals(taskId)) {
        return task.getTaskName();
      }
    }

    return null;
  }
  
}
