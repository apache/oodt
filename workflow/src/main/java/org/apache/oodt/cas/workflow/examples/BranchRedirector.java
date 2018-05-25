/**
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

package org.apache.oodt.cas.workflow.examples;

//JDK imports
import java.net.URL;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.metadata.CoreMetKeys;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;
import org.apache.oodt.cas.workflow.system.rpc.RpcCommunicationFactory;

/**
 * 
 * Redirects from an existing {@link org.apache.oodt.cas.workflow.structs.WorkflowInstance} by sending a specified
 * event specified by the task configuration parameter named
 * <code>eventName</code>.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class BranchRedirector implements WorkflowTaskInstance {

  public BranchRedirector() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance#run(org.apache
   * .oodt.cas.metadata.Metadata,
   * org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration)
   */
  @Override
  public void run(Metadata metadata, WorkflowTaskConfiguration config)
      throws WorkflowTaskInstanceException {

    try (WorkflowManagerClient wm = RpcCommunicationFactory
            .createClient(new URL(metadata.getMetadata(CoreMetKeys.WORKFLOW_MANAGER_URL)))) {
      wm.sendEvent(config.getProperty("eventName"), metadata);
    } catch (Exception e) {
      throw new WorkflowTaskInstanceException(e.getMessage());
    }
  }
}
