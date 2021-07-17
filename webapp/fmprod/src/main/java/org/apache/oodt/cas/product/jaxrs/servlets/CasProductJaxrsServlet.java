/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.cas.product.jaxrs.servlets;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.product.jaxrs.configurations.RdfConfiguration;
import org.apache.oodt.cas.product.jaxrs.configurations.RssConfiguration;
import org.apache.oodt.cas.product.jaxrs.filters.CORSFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a single place to initialize items such as the file manager client, working directory
 * and configurations when the web application is started up.
 *
 * @author rlaidlaw
 * @version $Revision$
 */
public class CasProductJaxrsServlet extends CXFNonSpringJaxrsServlet {
  // Auto-generated ID for serialization.
  private static final long serialVersionUID = -1835790185000773396L;

  private static Logger logger = LoggerFactory.getLogger(CORSFilter.class);

  private static final int CONFIG_PARAM_LENGTH = 3;
  private static final int CONFIG_PARAM_FORMAT = 1;
  private static final int CONFIG_PARAM_NAME = 2;

  @Override
  public void init(ServletConfig configuration) throws ServletException {
    super.init(configuration);
    ServletContext context = configuration.getServletContext();
    initializeClient(context);
    initializeWorkingDir(context);
    initializeConfigurations(context);
  }

  /**
   * Initializes the file manager client and stores it as a "client" context attribute by retrieving
   * a value for the file manager URL from the servlet context.
   *
   * @param context the servlet context
   * @throws ServletException if the servlet context parameter for the file manager working
   *     directory path cannot be found
   */
  private void initializeClient(ServletContext context) throws ServletException {
    try {
      URL url;
      String urlParameter = context.getInitParameter("filemgr.url");
      if (urlParameter != null) {
        // Get the file manager URL from the context parameter.
        url = new URL(PathUtils.replaceEnvVariables(urlParameter));
      } else {
        // Try the default URL for the file manager.
        logger.debug(
            "WARNING Exception Thrown: {}",
            "Unable to find a servlet context parameter \"\n"
                + "          + \"for the file manager URL.");
        url = new URL("http://localhost:9000");
      }

      // Attempt to connect the client to the file manager and if successful
      // store the client as a context attribute for other objects to access.
      FileManagerClient client = RpcCommunicationFactory.createClient(url);
      context.setAttribute("client", client);
    } catch (MalformedURLException e) {
      String message = "Encountered a malformed URL for the file manager.";
      logger.error("Exception Thrown: {}", message, e);
      throw new ServletException(message);
    } catch (ConnectionException e) {
      String message = "Client could not establish a connection to the file manager.";
      logger.error("Exception Thrown: {}", message, e);
      throw new ServletException(message);
    }
  }

  /**
   * Initializes the file manager working directory path and stores it as a "workingDir" context
   * attribute by retrieving a value from the servlet context.
   *
   * @param context the servlet context
   * @throws ServletException if the servlet context parameter for the file manager working
   *     directory path cannot be found
   */
  private void initializeWorkingDir(ServletContext context) throws ServletException {
    String workingDirPath = context.getInitParameter("filemgr.working.dir");
    if (workingDirPath != null) {
      // Validate the path.
      File workingDir = new File(PathUtils.replaceEnvVariables(workingDirPath));
      if (workingDir.exists() && workingDir.isDirectory()) {
        context.setAttribute("workingDir", workingDir);
        logger.debug(
            "The file manager's working directory has been set up as {}",
            workingDir.getAbsolutePath());
      } else {
        logger.debug(
            "Unable to locate the working directory for the file manager: {}",
            workingDir.getAbsolutePath());
      }
    } else {
      String message =
          "Unable to find a servlet context parameter for the file manager working directory path.";
      logger.debug(message);
      throw new ServletException(message);
    }
  }

  /**
   * Initializes the output configurations for various different formats and stores them in
   * "configurations" attributes by retrieving values from the servlet context.
   *
   * @param context the servlet context
   * @throws IOException if the specified file cannot be found or read
   */
  public void initializeConfigurations(ServletContext context) {
    Map<String, RdfConfiguration> rdfConfigurations =
        new ConcurrentHashMap<String, RdfConfiguration>();
    Map<String, RssConfiguration> rssConfigurations =
        new ConcurrentHashMap<String, RssConfiguration>();

    Enumeration<String> enumeration = context.getInitParameterNames();
    while (enumeration.hasMoreElements()) {
      String parameterName = enumeration.nextElement();
      if (parameterName.startsWith("configuration")) {
        String[] values = parameterName.split("\\.");
        if (values.length == CONFIG_PARAM_LENGTH) {
          String format = values[CONFIG_PARAM_FORMAT];
          String name = values[CONFIG_PARAM_NAME];
          String value = PathUtils.recursivelyReplaceEnvVariables(context.getInitParameter(parameterName));

          try {
            if ("rdf".equals(format)) {
              RdfConfiguration configuration = new RdfConfiguration();
              configuration.initialize(new File(value));
              rdfConfigurations.put(name, configuration);
            } else if ("rss".equals(format)) {
              RssConfiguration configuration = new RssConfiguration();
              configuration.initialize(new File(value));
              rssConfigurations.put(name, configuration);
            }
          } catch (IOException e) {
            logger.error(
                "Exception Thrown: The configuration '{}' could not be initialized (value: {}).",
                parameterName,
                value,
                e);
          }
        } else {
          logger.debug("Exception Thrown: Configuration context parameter could not be parsed.");
        }
      }
    }

    context.setAttribute("rdfConfigurations", rdfConfigurations);
    context.setAttribute("rssConfigurations", rssConfigurations);
  }
}
