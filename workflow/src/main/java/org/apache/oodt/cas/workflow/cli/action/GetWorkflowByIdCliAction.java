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
package org.apache.oodt.cas.workflow.cli.action;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;

/**
 * A {@link org.apache.oodt.cas.cli.action.CmdLineAction} which retrieves Workflow information for
 * Workflow with given workflow ID.
 *
 * @author bfoster (Brian Foster)
 */
public class GetWorkflowByIdCliAction extends WorkflowCliAction {

   private String workflowId;

   @Override
   public void execute(ActionMessagePrinter printer) throws CmdLineActionException {
      try (WorkflowManagerClient client = getClient()) {
         Workflow workflow = client.getWorkflowById(workflowId);
         
         StringBuilder taskIds = new StringBuilder();
         for (WorkflowTask wt : workflow.getTasks()) {
        	 if (taskIds.length()>0) {
               taskIds.append(", ");
             }
        	 taskIds.append(wt.getTaskId());
         }
         
         printer.println("Workflow: [id=" + workflow.getId() + ", name="
               + workflow.getName() + ", numTasks="
               + workflow.getTasks().size() + ", taskIds="+taskIds.toString()+"]");
         
      } catch (Exception e) {
         throw new CmdLineActionException(
               "Failed to get workflow information for" + " workflowId '"
                     + workflowId + "' : " + e.getMessage(), e);
      }   
   }

   public void setWorkflowId(String workflowId) {
      this.workflowId = workflowId;
   }
}
