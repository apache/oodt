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

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * A {@link CmdLineAction} for submitting dynamically created {@link Workflow}s
 * of pre-defined {@link WorkflowTask}s.
 * 
 * @author bfoster (Brian Foster)
 */
public class DynWorkflowCliAction extends WorkflowCliAction {

   private List<String> taskIds;
   private Metadata metadata;

   public DynWorkflowCliAction() {
      metadata = new Metadata();
   }

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      Validate.notNull(taskIds, "Must specify taskIds");

      try {
         String instId = getClient().executeDynamicWorkflow(taskIds, metadata);
         printer.println("Started dynamic workflow with id '" + instId + "'");
      } catch (Exception e) {
         throw new CmdLineActionException(
               "Failed to submit dynamic workflow for taskIds " + taskIds
                     + " with metadata " + metadata.getHashtable() + " : "
                     + e.getMessage(), e);
      }
   }

   public void setTaskIds(List<String> taskIds) {
      this.taskIds = taskIds;
   }

   public void addMetadata(List<String> metadata) {
      Validate.isTrue(metadata.size() > 1);

      this.metadata.addMetadata(metadata.get(0),
            metadata.subList(1, metadata.size()));
   }
}
