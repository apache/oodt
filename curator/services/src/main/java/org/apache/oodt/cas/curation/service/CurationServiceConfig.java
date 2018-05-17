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


package org.apache.oodt.cas.curation.service;

//OODT imports
import org.apache.oodt.cas.curation.metadata.CuratorConfMetKeys;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.util.PathUtils;


//JDK imports
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * 
 * 
 * Configures the {@link CurationService} by reading the parameters out of the
 * <code>context.xml</code> file, doing a
 * {@link PathUtils#replaceEnvVariables(String)} on each property in the
 * context.xml. This allows you to specify environment variables using the
 * traditional CAS syntax of:
 * 
 * <pre>
 *   [VAR_NAME], e.g., [CAS_CURATOR_HOME]/some/other/path
 * </pre>
 * 
 * The configuration parameters are read once upon loading the instance of this
 * object.
 * 
 * @author pramirez
 * @author mattmann
 * @version $Revision$
 * 
 */
public class CurationServiceConfig implements CuratorConfMetKeys {

  private static final Logger LOG = Logger
      .getLogger(CurationServiceConfig.class.getName());

  private static CurationServiceConfig instance;
  private final Map<String, String> parameters = new ConcurrentHashMap<String, String>();

  /**
   * Gets a singleton static instance of the global
   * {@link CurationServiceConfig} for the CAS Curator Webapp.
   * 
   * @param conf
   *          The {@link ServletConfig} read on startup of the webapp. This is
   *          typically specified in a <code>context.xml</code> file, but can
   *          also be specified in <code>web.xml</code>.
   * @return A singleton instance of the global {@link CurationServiceConfig}.
   * @throws InstantiationException
   *           If there is any error constructing the config.
   */
  public static CurationServiceConfig getInstance(ServletConfig conf) {
    if (instance == null) {
      instance = new CurationServiceConfig(conf);
    }
    return instance;
  }

  /**
   * 
   * @return The metadata output file path.
   */
  public String getMetAreaPath() {
    return this.evaluateParameter(MET_AREA_PATH);
  }
  
  /**
   * 
   * @return The extension of metadata files that will be generated and consumed
   *         by CAS curator.
   */
  public String getMetExtension() {
    return this.evaluateParameter(MET_EXTENSION);
  }

  /**
   * 
   * @return The path to the staging area where the CAS curator is ingesting
   *         files from.
   */
  public String getStagingAreaPath() {
    return this.evaluateParameter(STAGING_AREA_PATH);
  }

  /**
   * 
   * @return A {@link String} representation of the CAS File Manager {@link URL}
   *         .
   */
  public String getFileMgrURL() {
    return this.evaluateParameter(FM_URL);
  }

  /**
   * 
   * @return The full {@link FileManagerClient} built from the CAS curator
   *         property <code>filemgr.url</code>.
   */
  public FileManagerClient getFileManagerClient() {
    try {
      return RpcCommunicationFactory.createClient(new URL(this.getFileMgrURL()));
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return null;
    }
  }

  /**
   * 
   * @return The upload path for metadata extractor config files.
   */
  public String getMetExtrConfUploadPath() {
    return this.evaluateParameter(MET_EXTRACTOR_CONF_UPLOAD_PATH);
  }

  /**
   * 
   * @return The path to the crawler config file.
   */
  public String getCrawlerConfFile() {
    return this.evaluateParameter(CRAWLER_CONF_FILE);
  }

  /**
   * 
   * @return The path to the location where policy directories should be
   *         uploaded to.
   */
  public String getPolicyUploadPath() {
    return this.evaluateParameter(POLICY_UPLOAD_PATH);
  }

  /**
   * 
   * @return The default CAS File Manager {@link org.apache.oodt.cas.filemgr.datatransfer.DataTransferFactory} classname.
   */
  public String getDefaultTransferFactory() {
    return this.evaluateParameter(DEFAULT_TRANSFER_FACTORY);
  }

  /**
   * Gets a property from the CAS Curator config without calling
   * {@link PathUtils#replaceEnvVariables(String)}.
   * 
   * @param name
   *          The name of the parameter to return.
   * @return The un-evaluated property value from the config.
   */
  public String getParameter(String name) {
    return this.parameters.get(name);
  }
  
  public String getFileMgrProps() {
    return this.evaluateParameter(FM_PROPS);
  }

  private String evaluateParameter(String name) {
    return PathUtils.replaceEnvVariables(this.parameters.get(name));
  }

  // Note that the constructor is private
  private CurationServiceConfig(ServletConfig conf) {
    readContextParams(conf.getServletContext());
  }

  @SuppressWarnings("unchecked")
  private void readContextParams(ServletContext context) {
    for (Enumeration<String> names = context.getInitParameterNames(); names
        .hasMoreElements();) {
      String name = names.nextElement();
      parameters.put(name, context.getInitParameter(name));
    }
    LOG.log(Level.INFO, "Init Parameters: " + parameters);
  }
}
