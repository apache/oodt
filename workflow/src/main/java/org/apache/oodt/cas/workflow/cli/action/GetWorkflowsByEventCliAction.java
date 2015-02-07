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

//JDK imports
import java.util.List;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.workflow.structs.Workflow;

/**
 * A {@link CmdLineAction} which get the current list of workflows by
 * event name
 *
 * @author bfoster (Brian Foster)
 */
public class GetWorkflowsByEventCliAction extends WorkflowCliAction {

   private String eventName;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
         @SuppressWarnings("unchecked")
         List<Workflow> workflows = getClient().getWorkflowsByEvent(eventName);

         if (workflows == null) {
            throw new Exception("WorkflowManager returned null workflow list");
         }
         for (Workflow workflow : workflows) {
            printer.println("Workflow: [id=" + workflow.getId() + ", name="
                  + workflow.getName() + ", numTasks="
                  + workflow.getTasks().size() + "]");
         }
      } catch (Exception e) {
         throw new CmdLineActionException(
               "Failed to get workflows by event name '" + eventName + "' : "
                     + e.getMessage(), e);
      }
   }

   public void setEventName(String eventName) {
      this.eventName = eventName;
   }
}
