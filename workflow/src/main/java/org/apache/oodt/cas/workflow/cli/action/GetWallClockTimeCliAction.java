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
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;

/**
 * A {@link CmdLineAction} which gets Wall Clock Time for a given workflow
 * instance.
 * 
 * @author bfoster (Brian Foster)
 */
public class GetWallClockTimeCliAction extends WorkflowCliAction {

   private String instanceId;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try (WorkflowManagerClient client = getClient()) {
         double wallClockTime = client.getWorkflowWallClockMinutes(instanceId);
         printer.println(wallClockTime + " minutes");
      } catch (Exception e) {
         throw new CmdLineActionException(
               "Failed to get wall clock time for instance '" + instanceId
                     + "' : " + e.getMessage(), e);
      }
   }

   public void setInstanceId(String instanceId) {
      this.instanceId = instanceId;
   }
}
