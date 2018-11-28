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

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.resource.structs.ResourceNode;

/**
 * A {@link CmdLineAction} which get list of ResourceNodes.
 * 
 * @author bfoster (Brian Foster)
 */
public class GetNodesCliAction extends ResourceCliAction {

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
         @SuppressWarnings("unchecked")
         List<ResourceNode> nodes = getClient().getNodes();

         if (nodes == null) {
            throw new Exception("ResourceManager returned null nodes");
         }
         for (ResourceNode node : nodes) {
            printer.println("node: [id=" + node.getNodeId() + ",capacity="
                  + node.getCapacity() + ",url=" + node.getIpAddr() + "]");
         }
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to get nodes : "
               + e.getMessage(), e);
      }
   }
}
