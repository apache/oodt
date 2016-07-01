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

package org.apache.oodt.grid;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

/**
 * Bean containing login status for and configuration info.
 */
public class ConfigBean implements Serializable {
  /**
   * Return true is administrator is authenticated.
   * 
   * @return True if authentic, false otherwise.
   */
  public boolean isAuthentic() {
    return authentic;
  }

  /**
   * Set whether the administrator's been authenticated.
   * 
   * @param authentic
   *          True if authentic, false otherwise.
   */
  void setAuthentic(boolean authentic) {
    this.authentic = authentic;
  }

  /**
   * Get any message to display. This should never be null.
   * 
   * @return A message to display
   */
  public String getMessage() {
    return message;
  }

  /**
   * Set the message to display.
   * 
   * @param message
   *          Message to display.
   */
  public void setMessage(String message) {
    if (message == null) {
      throw new IllegalArgumentException("message cannot be null");
    }
    this.message = message;
  }

  /**
   * Get the configuration of web-grid.
   * 
   * @return a <code>Configuration</code> value.
   * @throws AuthenticationRequiredException
   *           if the administrator's not authenticated.
   */
  public Configuration getConfiguration()
      throws AuthenticationRequiredException {
    checkAuthenticity();
    return configuration;
  }

  /**
   * Tell if HTTPS is required to access the web-grid configuration.
   * 
   * @return True if HTTPS is required to access the web-grid configuration.
   * @throws AuthenticationRequiredException
   *           if the administrator's not authenticated.
   */
  public boolean isHttpsRequired() throws AuthenticationRequiredException {
    checkAuthenticity();
    return configuration.isHTTPSrequired();
  }

  /**
   * Tell if admin access can come only from the localhost.
   * 
   * @return True if admin access can come only from the localhost.
   * @throws AuthenticationRequiredException
   *           if the administrator's not authenticated.
   */
  public boolean isLocalhostRequired() throws AuthenticationRequiredException {
    checkAuthenticity();
    return configuration.isLocalhostRequired();
  }

  /**
   * Get the list of {@link ProductServer}s that have been installed in this
   * container.
   * 
   * @return a <code>List</code> of {@link ProductServer}s.
   * @throws AuthenticationRequiredException
   *           if the administrator's not authenticated.
   */
  public List getProductServers() throws AuthenticationRequiredException {
    checkAuthenticity();
    return configuration.getProductServers();
  }

  /**
   * Get the list of {@link ProfileServer}s that have been installed in this
   * container.
   * 
   * @return a <code>List</code> of {@link ProfileServer}s.
   * @throws AuthenticationRequiredException
   *           if the administrator's not authenticated.
   */
  public List getProfileServers() throws AuthenticationRequiredException {
    checkAuthenticity();
    return configuration.getProfileServers();
  }

  /**
   * Set the configuration this bean will use.
   * 
   * @param configuration
   *          a <code>Configuration</code> value.
   */
  void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  /**
   * Get the properties defined for this container.
   * 
   * @return a <code>Properties</code> value.
   * @throws AuthenticationRequiredException
   *           if the administrator's not authenticated.
   */
  public Properties getProperties() throws AuthenticationRequiredException {
    checkAuthenticity();
    return configuration.getProperties();
  }

  /**
   * Check if the administrator is authentic. This method does nothing if the
   * administrator is authentic, but throws an exception if not.
   * 
   * @throws AuthenticationRequiredException
   *           if the administrator's not authenticated.
   */
  private void checkAuthenticity() throws AuthenticationRequiredException {
    if (isAuthentic() && configuration != null) {
      return;
    }
    message = "";
    throw new AuthenticationRequiredException();
  }

  /** True if administrator is authentic. */
  private boolean authentic;

  /** Any message to display. */
  private String message = "";

  /** The configuration for this container. */
  private transient Configuration configuration;
}
