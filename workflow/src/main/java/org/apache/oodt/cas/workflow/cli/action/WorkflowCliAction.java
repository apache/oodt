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
import java.net.MalformedURLException;
import java.net.URL;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;
import org.apache.oodt.cas.workflow.system.rpc.RpcCommunicationFactory;

/**
 * Base {@link CmdLineAction} for Workflow Manager.
 *
 * @author bfoster (Brian Foster)
 */
public abstract class WorkflowCliAction extends CmdLineAction {

   private WorkflowManagerClient client;

   public String getUrl() {
      return System.getProperty("org.apache.oodt.cas.workflow.url");
   }

   protected WorkflowManagerClient getClient()
         throws MalformedURLException {
      Validate.notNull(getUrl());

      if (client != null) {
         return client;
      } else {
         return RpcCommunicationFactory.createClient(new URL(getUrl()));
      }
   }

   public void setClient(WorkflowManagerClient client) {
      this.client = client;
   }
}
