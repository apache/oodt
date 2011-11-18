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

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;

/**
 * A {@link CmdLineAction} which changes a node's capacity.
 *
 * @author bfoster (Brian Foster)
 */
public class SetNodeCapacityCliAction extends ResourceCliAction {

   private String nodeId;
   private int capacity;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
         Validate.notNull(nodeId, "Must specify nodeId");
         Validate.notNull(capacity, "Must specify capacity");

         getClient().setNodeCapacity(nodeId, capacity);
         printer.println("Successfully set node capacity!");
      } catch (Exception e) {
         throw new CmdLineActionException("", e);
      }
   }

   public void setNodeId(String nodeId) {
      this.nodeId = nodeId;
   }

   public void setCapacity(int capacity) {
      this.capacity = capacity;
   }
}
