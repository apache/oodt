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

package org.apache.oodt.cas.webcomponents.workflow;

import java.util.List;
import java.util.Vector;

import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * 
 * Describe your class here.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public abstract class WMMonitorAppBase extends WebApplication {

  public String getWorkflowUrl() {
    return getContextParamEnvReplace("workflow.url");
  }

  public String getLifecycleFilePath() {
    return getContextParamEnvReplace("org.apache.oodt.cas.workflow.webapp.lifecycleFilePath");
  }

  public List<String> getStatuses() {
    String[] statuses = getContextParamEnvReplace("org.apache.oodt.cas.workflow.inst.statuses").split(",");
    List<String> statusList = new Vector<String>();
    for(String status: statuses){
      statusList.add(status.trim());
    }
    statusList.add("ALL");
    return statusList;
  }

  public String getInstMetFieldsFilePath() {
    return getContextParamEnvReplace("org.apache.oodt.cas.workflow.webapp.inst.metFields.filePath");
  }

  private String getContextParamEnvReplace(String paramName) {
    return PathUtils.replaceEnvVariables(this.getServletContext()
        .getInitParameter(paramName));
  }

}
