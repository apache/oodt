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

//JDK imports
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.resource.system.ResourceManagerClient;
import org.apache.oodt.cas.resource.system.rpc.ResourceManagerFactory;

/**
 * A set of utility methods that can be used by PCS that need to
 * communicate with the Resource Manager.
 *
 * @author mattmann
 * @version $Revision$
 *
 */
public class ResourceManagerUtils implements Serializable {

  /* our resource manager client */
  private ResourceManagerClient client;

  /* our log stream */
  private static final Logger LOG = Logger.getLogger(ResourceManagerUtils.class
      .getName());

  private URL rmUrl;

  public ResourceManagerUtils(String urlStr) {
    this(safeGetUrlFromString(urlStr));
  }

  public ResourceManagerUtils(URL url) {
    this.client = ResourceManagerFactory.getResourceManagerClient(url);
    this.rmUrl = url;

  }

  public ResourceManagerUtils(ResourceManagerClient client) {
    this.client = client;
  }

  /**
   * @return the client
   */
  public ResourceManagerClient getClient() {
    return client;
  }

  /**
   * @param client
   *          the client to set
   */
  public void setClient(ResourceManagerClient client) {
    this.client = client;
    if (this.client != null) {
      this.rmUrl = this.client.getResMgrUrl();
    }
  }

  public List safeGetResourceNodes() {
    try {
      return this.client.getNodes();
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to obtain resource nodes "
          + "while communicating with resource manager at: ["
          + this.client.getResMgrUrl() + "]");
      return null;
    }
  }

  /**
   *
   * @return The {@link URL} for the Resource Manager that this
   *         ResourceManagerUtils communicates with.
   */
  public URL getResmgrUrl() {
    return this.rmUrl;
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
}
