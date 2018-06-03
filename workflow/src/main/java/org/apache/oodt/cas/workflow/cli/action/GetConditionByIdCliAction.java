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
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;

/**
 * A {@link CmdLineAction} which retrieves WorkflowCondition information for
 * WorkflowCondition with given condition ID.
 * 
 * @author bfoster (Brian Foster)
 */
public class GetConditionByIdCliAction extends WorkflowCliAction {

   private String conditionId;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {

      try (WorkflowManagerClient client = getClient()) {
         WorkflowCondition condition = client.getConditionById(conditionId);
         printer.println("Condition: [id=" + condition.getConditionId()
               + ", name=" + condition.getConditionName() + ", order="
               + condition.getOrder() + ", class="
               + condition.getClass().getName() + "]");
      } catch (Exception e) {
         throw new CmdLineActionException(
               "Failed to get condition by id for conditionId '" + conditionId
                     + "' : " + e.getMessage(), e);
      }
   }

   public void setConditionId(String conditionId) {
      this.conditionId = conditionId;
   }
}
