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
package org.apache.oodt.cas.resource.cli.action;

//JDK imports
import java.util.List;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;

/**
 * A {@link CmdLineAction} which gets the nodes in a queue.
 * 
 * @author bfoster (Brian Foster)
 */
public class GetNodesInQueueCliAction extends ResourceCliAction {

   private String queueName;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
         Validate.notNull(queueName, "Must specify queueName");

         List<String> nodeIds = getClient().getNodesInQueue(queueName);
         printer.println("Nodes in Queue '" + queueName + "':");
         for (String nodeId : nodeIds) {
            printer.println(" - " + nodeId);
         }
         printer.println();
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to get nodes in queue '"
               + queueName + "' : " + e.getMessage(), e);
      }
   }

   public void setQueueName(String queueName) {
      this.queueName = queueName;
   }
}
