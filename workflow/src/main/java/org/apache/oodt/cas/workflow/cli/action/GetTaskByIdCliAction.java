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
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;

/**
 * A {@link CmdLineAction} which retrieves WorkflowTask information for
 * WorkflowTask with given task ID.
 *
 * @author bfoster (Brian Foster)
 */
public class GetTaskByIdCliAction extends WorkflowCliAction {

   private String taskId;

   @Override
   public void execute(ActionMessagePrinter printer) throws CmdLineActionException {
       try (WorkflowManagerClient client = getClient()) {
           WorkflowTask task = client.getTaskById(taskId);

           StringBuilder requiredMetFields = new StringBuilder();
           for (Object o : task.getRequiredMetFields()) {
               if (requiredMetFields.length() > 0) {
                   requiredMetFields.append(", ");
               }
               requiredMetFields.append((String) o);
           }

           printer.println("Task: [id=" + task.getTaskId()
                   + ", name=" + task.getTaskName()
                   + ", order=" + task.getOrder()
                   + ", class=" + task.getClass().getName()
                   + ", numConditions=" + task.getConditions().size()
                   + ", requiredMetadataFields=[" + requiredMetFields.toString() + "]"
                   + ", configuration=" + task.getTaskConfig().getProperties() + "]");

       } catch (Exception e) {
           throw new CmdLineActionException(
                   "Failed to get task by id for taskId '" + taskId + "' : "
                           + e.getMessage(), e);
       }
   }

   public void setTaskId(String taskId) {
      this.taskId = taskId;
   }
}
