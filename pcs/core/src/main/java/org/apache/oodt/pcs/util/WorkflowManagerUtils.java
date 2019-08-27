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

package org.apache.oodt.pcs.util;

import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;
import org.apache.oodt.cas.workflow.system.rpc.RpcCommunicationFactory;
import org.apache.xmlrpc.XmlRpcClient;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * A set of utility methods that can be used by PCS that need to communicate
 * with the Workflow Manager.
 * 
 * @author mattmann
 * @version $Revision$
 */
public class WorkflowManagerUtils implements Serializable {

  /* our workflow manager client */
  private WorkflowManagerClient client;

  /* our log stream */
  private static final Logger LOG = Logger.getLogger(WorkflowManagerUtils.class.getName());

  private URL wmUrl;

  public WorkflowManagerUtils(String urlStr) {
    this(safeGetUrlFromString(urlStr));
  }

  public WorkflowManagerUtils(URL url) {
    this.client = RpcCommunicationFactory.createClient(url);
    this.wmUrl = url;
  }

  public WorkflowManagerUtils(WorkflowManagerClient client) {
    this.client = client;
  }

  public void updateWorkflowInstanceStatus(String wInstId, String status) {
    try {
      this.client.updateWorkflowInstanceStatus(wInstId, status);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
    }

  }

  public List<WorkflowInstance> safeGetWorkflowInstances() {
    if (!isConnected()) {
      return Collections.EMPTY_LIST;
    }

    try {
      return this.client.getWorkflowInstances();
    } catch (Exception ignore) {
      return Collections.EMPTY_LIST;
    }
  }

  public boolean isConnected() {
    try {
      XmlRpcClient c = new XmlRpcClient(this.client.getWorkflowManagerUrl());
      c.execute("workflowmgr.getWorkflowInstances", new Vector());
      return true;
    } catch (Exception ignore) {
      return false;
    }
  }

  public List safeGetWorkflowInstancesByStatus(String status) {
    try {
      return this.client.getWorkflowInstancesByStatus(status);
    } catch (Exception e) {
      LOG.log(Level.WARNING,
          "exception obtaining workflow instances by status: [" + status
              + "]: message: " + e.getMessage());
      return null;
    }
  }

  public int safeGetNumWorkflowInstancesByStatus(String status) {
    try {
      return this.client.getNumWorkflowInstancesByStatus(status);
    } catch (Exception e) {
      LOG.log(Level.WARNING,
          "exception obtaining num workflow instances by status: [" + status
              + "]: message: " + e.getMessage());
      return -1;
    }
  }

  /**
   * @return the client
   */
  public WorkflowManagerClient getClient() {
    return client;
  }

  /**
   * @param client
   *          the client to set
   */
  public void setClient(WorkflowManagerClient client) {
    this.client = client;
    if (this.client != null) {
      this.wmUrl = this.client.getWorkflowManagerUrl();
    }
  }

  private static URL safeGetUrlFromString(String urlStr) {
    URL url = null;

    try {
      url = new URL(urlStr);
    } catch (MalformedURLException e) {
      LOG.log(Level.SEVERE, "PCS: Unable to generate url from url string: ["
          + urlStr + "]: Message: " + e.getMessage());
    }

    return url;
  }

  /**
   * 
   * @return The {@link URL} pointer to the Workflow Manager that this
   *         WorkflowManagerUtils communicates with.
   */
  public URL getWmUrl() {
    return this.wmUrl;
  }

}
