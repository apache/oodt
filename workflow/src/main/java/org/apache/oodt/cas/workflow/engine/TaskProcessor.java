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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;

/**
 *
 * Describe your class here.
 *
 * @author mattmann
 * @version $Revision$
 *
 */
public class TaskProcessor extends WorkflowProcessor {

  private WorkflowTask task;
  
  private static final Logger LOG = Logger.getLogger(TaskProcessor.class.getName());
  
  /**
   * @param workflowInstance
   * @param instRep
   * @param wParentUrl
   * @param conditionWait
   */
  public TaskProcessor(WorkflowInstance workflowInstance,
      WorkflowInstanceRepository instRep, URL wParentUrl, long conditionWait) {
    super(workflowInstance, instRep, wParentUrl, conditionWait);
    // TODO Auto-generated constructor stub
  }

  public WorkflowTask getTask(){
    return this.task;
  }
  
  public boolean checkTaskRequiredMetadata(
      Metadata dynMetadata) {
    if (task.getRequiredMetFields() == null
        || (task.getRequiredMetFields() != null && task.getRequiredMetFields()
            .size() == 0)) {
      LOG.log(Level.INFO, "Task: [" + task.getTaskName()
          + "] has no required metadata fields");
      return true; /* no required metadata, so we're fine */
    }

    for (String reqField : (List<String>) (List<?>) task.getRequiredMetFields()) {
      if (!dynMetadata.containsKey(reqField)) {
        LOG.log(Level.SEVERE, "Checking metadata key: [" + reqField
            + "] for task: [" + task.getTaskName()
            + "]: failed: aborting workflow");
        return false;
      }
    }

    LOG.log(Level.INFO, "All required metadata fields present for task: ["
        + task.getTaskName() + "]");

    return true;
  }  
  
  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.engine.WorkflowProcessor#getRunnableSubProcessors()
   */
  @Override
  protected List<WorkflowProcessor> getRunnableSubProcessors() {
    // TODO Auto-generated method stub
    return null;
  }
  
  

}
