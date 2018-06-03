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
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;

/**
 * Gets the first page of workflow instances.
 * 
 * @author bfoster (Brian Foster)
 */
public class GetFirstPageCliAction extends WorkflowCliAction {

   private String status;

   @SuppressWarnings("unchecked")
   @Override
   public void execute(ActionMessagePrinter printer) throws CmdLineActionException {
      try (WorkflowManagerClient client = getClient()) {
         WorkflowInstancePage page;
         if (status != null && !status.equals("")) {
            page = client.paginateWorkflowInstances(1, status);
         } else {
            page = client.getFirstPage();
         }

         printer.println("Page: [num=" + page.getPageNum() + ","
               + "pageSize=" + page.getPageSize() + ",totalPages="
               + page.getTotalPages() + "]");
         if (page.getPageWorkflows() == null) {
            throw new Exception(
                  "WorkflowManager returned null page of workflows");
         }
         for (WorkflowInstance inst : (List<WorkflowInstance>) page
               .getPageWorkflows()) {
            printer.println("Instance: [id=" + inst.getId() + ", status="
                  + inst.getStatus() + ", currentTask="
                  + inst.getCurrentTaskId() + ", workflow="
                  + inst.getWorkflow().getName() + ", wallClockTime="
                  + client.getWorkflowWallClockMinutes(inst.getId())
                  + ", currentTaskWallClockTime="
                  + client.getWorkflowCurrentTaskWallClockMinutes(inst.getId())
                  + "]");
         }
      } catch (Exception e) {
         throw new CmdLineActionException(
               "Failed to get first page of workflows : " + e.getMessage(), e);
      }
   }

   public void setStatus(String status) {
      this.status = status;
   }
}
