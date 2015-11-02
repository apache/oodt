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

package org.apache.oodt.cas.workflow.engine.processor;

//JDK imports
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.structs.Priority;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

//OODT imports

/**
 * 
 * WorkflowProcessor which handles running task workflows.
 * 
 * @author bfoster
 * @author mattmann
 * 
 * @version $Revision$
 */
public class TaskProcessor extends WorkflowProcessor {

  public static final double DOUBLE = 0.1;
  public static final int INT = 60;
  private Class<? extends WorkflowTaskInstance> instanceClass;
  private String jobId;
  
  public TaskProcessor(WorkflowLifecycleManager lifecycleManager, WorkflowInstance instance) {
    super(lifecycleManager, instance);
  }

  public Class<? extends WorkflowTaskInstance> getInstanceClass() {
    return this.instanceClass;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getJobId() {
    return this.jobId;
  }

  public void setInstanceClass(
      Class<? extends WorkflowTaskInstance> instanceClass) {
    this.instanceClass = instanceClass;
  }

  @Override
  public void setWorkflowInstance(WorkflowInstance instance) {
    instance.setPriority(Priority
        .getPriority(instance.getPriority().getValue() + DOUBLE));
    super.setWorkflowInstance(instance);
  }

  @Override
  public List<TaskProcessor> getRunnableWorkflowProcessors() {
    List<TaskProcessor> tps = super.getRunnableWorkflowProcessors();
    if (tps.size() == 0) {
      if (this.getWorkflowInstance().getState().getName().equals("Blocked")) {
        String requiredBlockTimeElapseString = this.getWorkflowInstance()
            .getCurrentTask().getTaskConfig().getProperty("BlockTimeElapse");
        int requiredBlockTimeElapse = 2;
        if (requiredBlockTimeElapseString != null) {
          try {
            requiredBlockTimeElapse = Integer
                .parseInt(requiredBlockTimeElapseString);
          } catch (Exception ignored) {
          }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.getWorkflowInstance().getState().getStartTime());
        long elapsedTime = ((System.currentTimeMillis() - calendar
            .getTimeInMillis()) / 1000) / INT;
        if (elapsedTime >= requiredBlockTimeElapse) {
          tps.add(this);
        }
      } else if (this.isAnyState("Loaded", "Queued", "PreConditionSuccess") && 
          !this.isAnyState("Executing") && this.passedPreConditions()){
        tps.add(this);
      }
    }
    return tps;
  }

  protected boolean hasSubProcessors() {
    return true;
  }

  @Override
  public List<WorkflowProcessor> getRunnableSubProcessors() {
    return new Vector<WorkflowProcessor>();
  }

  @Override
  public void setSubProcessors(List<WorkflowProcessor> subProcessors) {
    // not allowed
  }

  @Override
  public void handleSubProcessorMetadata(WorkflowProcessor workflowProcessor) {
    // do nothing
  }

}
