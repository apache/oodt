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
package org.apache.oodt.cas.resource.cli;

//JDK imports
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.action.store.spring.SpringCmdLineActionStore;
import org.apache.oodt.cas.cli.exception.CmdLineActionStoreException;
import org.apache.oodt.cas.resource.cli.action.ResourceCliAction;
import org.apache.oodt.cas.resource.system.MockXmlRpcResourceManagerClient;

/**
 * A {@link SpringCmdLineActionStore} which sets {@link WorkflowCliAction}s
 * to use a {@link MockXmlRpcWorkflowManagerClient}.
 *
 * @author bfoster (Brian Foster)
 */
public class UseMockClientCmdLineActionStore extends SpringCmdLineActionStore {

   private MockXmlRpcResourceManagerClient client;

   public UseMockClientCmdLineActionStore() {
      super(System.getProperty("org.apache.oodt.cas.cli.action.spring.config"));
      try {
         client = new MockXmlRpcResourceManagerClient();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public Set<CmdLineAction> loadSupportedActions() {
      Set<CmdLineAction> actions = super.loadSupportedActions();
      for (CmdLineAction action : actions) {
         if (action instanceof ResourceCliAction) {
            ((ResourceCliAction) action).setClient(client);
         }
      }
      return actions;
   }

   public MockXmlRpcResourceManagerClient getClient() {
      return client;
   }
}
