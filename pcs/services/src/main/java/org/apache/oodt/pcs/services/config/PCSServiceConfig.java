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

package org.apache.oodt.pcs.services.config;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;

//OODT imports
import org.apache.oodt.cas.metadata.util.PathUtils;

/**
 * 
 * The configuration class for the PCS JAX RS services.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class PCSServiceConfig implements PCSServiceConfMetKeys {

  private Map<String, String> parameters;

  private static final Logger LOG = Logger.getLogger(PCSServiceConfig.class
      .getName());

  public PCSServiceConfig(ServletConfig config) {
    this.parameters = new ConcurrentHashMap<String, String>();
    this.readContextParams(config);
  }

  /**
   * @return the fmUrl
   * @throws MalformedURLException
   */
  public URL getFmUrl() throws MalformedURLException {
    return new URL(PathUtils.replaceEnvVariables(this.parameters.get(FM_URL)));
  }

  /**
   * @return the wmUrl
   * @throws MalformedURLException
   */
  public URL getWmUrl() throws MalformedURLException {
    return new URL(PathUtils.replaceEnvVariables(this.parameters.get(WM_URL)));
  }

  /**
   * @return the rmUrl
   * @throws MalformedURLException
   */
  public URL getRmUrl() throws MalformedURLException {
    return new URL(PathUtils.replaceEnvVariables(this.parameters.get(RM_URL)));
  }

  /**
   * @return the listingConfFilePath
   */
  public String getListingConfFilePath() {
    return PathUtils.replaceEnvVariables(this.parameters
        .get(PCS_LL_CONF_FILE_PATH));
  }

  /**
   * @return the crawlerConfigFilePath
   */
  public String getCrawlerConfigFilePath() {
    return PathUtils.replaceEnvVariables(this.parameters
        .get(PCS_HEALTH_CRAWLER_CONF_PATH));
  }

  /**
   * @return the workflowStatusesFilePath
   */
  public String getWorkflowStatusesFilePath() {
    return PathUtils.replaceEnvVariables(this.parameters
        .get(PCS_HEALTH_WORKFLOW_STATUS_PATH));
  }

  /**
   * @return the traceNotCatalogedFiles
   */
  public boolean isTraceNotCatalogedFiles() {
    return Boolean.valueOf(this.parameters.get(PCS_TRACE_ENABLE_NON_CAT));
  }
  
  /**
   * @return The comma-separated list of product types to exclude from
   * pedigree tracking.
   */
  public String getTraceProductTypeExcludeList(){
    return PathUtils.replaceEnvVariables(this.parameters.get(PCS_TRACE_PTYPE_EXCLUDE_LIST));
  }

  @SuppressWarnings("unchecked")
  private void readContextParams(ServletConfig config) {
    for (Enumeration<String> paramNames = config.getServletContext()
        .getInitParameterNames(); paramNames.hasMoreElements();) {
      String paramName = paramNames.nextElement();
      this.parameters.put(paramName, config.getServletContext()
          .getInitParameter(paramName));
    }

    LOG.log(Level.INFO, "Init Parameters: " + this.parameters);
  }

}
