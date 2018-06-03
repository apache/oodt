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
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;

/**
 * A {@link CmdLineAction} which get list of know workflow models from
 * WorkflowManager at given URL.
 * 
 * @author bfoster (Brian Foster)
 */
public class GetWorkflowsCliAction extends WorkflowCliAction {

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try (WorkflowManagerClient client = getClient()) {
         @SuppressWarnings("unchecked")
         List<Workflow> workflows = client.getWorkflows();

         if (workflows == null) {
            throw new Exception(
                  "WorkflowManager returned null list of Workflows");
         }

         for (Workflow workflow : workflows) {
            printer.println("Workflow: [id=" + workflow.getId() + ", name="
                  + workflow.getName() + ", numTasks="
                  + workflow.getTasks().size() + "]");
         }
      } catch (Exception e) {
         throw new CmdLineActionException(
               "Failed to get list of workflow models from url '" + getUrl()
                     + "' : " + e.getMessage(), e);
      }
   }

}
