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

package org.apache.oodt.cas.wmservices.resources;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.apache.oodt.cas.wmservices.servlets.WmServicesServlet;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;

public abstract class AbstractWorkflowServiceResource {

  private static final Logger LOGGER = Logger.getLogger(AbstractWorkflowServiceResource.class.getName());

  // Servlet context
  @Context
  private ServletContext context;

  /**
   * Gets the packaged repository directory from servlet context.
   * @return the packaged repository directory
   * @throws Exception
   *           if an object cannot be retrieved from the context attribute
   */
  public File getContextPkgReposDir() throws Exception {
    Object repositoryDirObject = context.getAttribute(WmServicesServlet.ATTR_NAME_PKG_REPO_DIR);
    if (repositoryDirObject instanceof File) {
      return (File) repositoryDirObject;
    }
    String message = "Unable to retrieve packaged repository directory from the servlet context.";
    LOGGER.log(Level.WARNING, message);
    throw new Exception(message);
  }
  
  /**
   * Gets the servlet's workflow manager client instance from the servlet
   * context.
   * @return the workflow manager client instance from the servlet context
   *         attribute
   * @throws Exception
   *           if an object cannot be retrieved from the context attribute
   */
  public WorkflowManagerClient getContextClient() throws Exception {
    // Get the workflow manager client from the servlet context.
    Object clientObject = context.getAttribute(WmServicesServlet.ATTR_NAME_CLIENT);
    if (clientObject instanceof WorkflowManagerClient) {
      return (WorkflowManagerClient) clientObject;
    }

    String message = "Unable to retrieve workflow manager client from the "
        + "servlet context.";
    LOGGER.log(Level.WARNING, message);
    throw new Exception(message);
  }

  
  /**
   * Sets the servlet context.
   * @param context
   *          the servlet context to set.
   */
  public void setServletContext(ServletContext context) {
    this.context = context;
  }
}
