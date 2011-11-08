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

package org.apache.oodt.pcs.opsui;

//OODT imports
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.webcomponents.filemgr.FMBrowserSession;
import org.apache.oodt.cas.webcomponents.workflow.instance.WorkflowInstancesViewer;
import org.apache.oodt.pcs.opsui.status.StatusPage;

//Wicket imports
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.target.coding.MixedParamUrlCodingStrategy;

/**
 * 
 * The OPSUI application object.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class OpsuiApp extends WebApplication implements Serializable {

  private static final long serialVersionUID = 1403288657369282259L;

  public OpsuiApp() {
    MixedParamUrlCodingStrategy types = new MixedParamUrlCodingStrategy(
        "types", TypesPage.class, new String[] {});

    MixedParamUrlCodingStrategy typeBrowser = new MixedParamUrlCodingStrategy(
        "type", TypeBrowserPage.class, new String[] { "name", "pageNum" });

    MixedParamUrlCodingStrategy prodBrowser = new MixedParamUrlCodingStrategy(
        "product", ProductBrowserPage.class, new String[] { "id" });

    MixedParamUrlCodingStrategy pcsStatus = new MixedParamUrlCodingStrategy(
        "status", StatusPage.class, new String[] {});

    MixedParamUrlCodingStrategy taskPageMount = new MixedParamUrlCodingStrategy(
        "task", WorkflowTaskViewerPage.class, new String[] { "id" });
    MixedParamUrlCodingStrategy condPageMount = new MixedParamUrlCodingStrategy(
        "condition", WorkflowConditionViewerPage.class, new String[] { "id" });

    MixedParamUrlCodingStrategy workflowPageMount = new MixedParamUrlCodingStrategy(
        "workflow", WorkflowViewerPage.class, new String[] { "id" });

    MixedParamUrlCodingStrategy workflowsPageMount = new MixedParamUrlCodingStrategy(
        "workflows", WorkflowsViewerPage.class, new String[] {});

    MixedParamUrlCodingStrategy workflowInstsPageMount = new MixedParamUrlCodingStrategy(
        "instances", WorkflowInstanceViewerPage.class, new String[] { "status",
            "pageNum" });

    mount(pcsStatus);
    mount(types);
    mount(typeBrowser);
    mount(prodBrowser);
    mount(taskPageMount);
    mount(condPageMount);
    mount(workflowPageMount);
    mount(workflowsPageMount);
    mount(workflowInstsPageMount);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.wicket.Application#getHomePage()
   */
  @Override
  public Class<? extends Page> getHomePage() {
    return HomePage.class;
  }

  public String getFmUrlStr() {
    return PathUtils.replaceEnvVariables(getServletContext().getInitParameter(
        "filemgr.url"));
  }

  public String getWmUrlStr() {
    return PathUtils.replaceEnvVariables(getServletContext().getInitParameter(
        "workflow.url"));
  }

  public String getRmUrlStr() {
    return PathUtils.replaceEnvVariables(getServletContext().getInitParameter(
        "resmgr.url"));
  }

  public String getEmailContactLink() {
    return getServletContext().getInitParameter("contact.email");
  }

  public String getCrawlerConfFilePath() {
    return PathUtils.replaceEnvVariables(getServletContext().getInitParameter(
        "org.apache.oodt.pcs.health.crawler.conf.filePath"));
  }

  public String getStatesFilePath() {
    return PathUtils.replaceEnvVariables(getServletContext().getInitParameter(
        "org.apache.oodt.pcs.health.workflow.statuses.filePath"));
  }

  public String getWorkflowLifecycleFilePath() {
    return PathUtils.replaceEnvVariables(getServletContext().getInitParameter(
        "org.apache.oodt.pcs.opsui.workflow.lifecycleFilePath"));
  }

  public List<String> getWorkflowInstStatues() {
    String[] statuses = getServletContext().getInitParameter(
        "org.apache.oodt.pcs.opsui.winst.statuses").split(",");
    List<String> statusList = new Vector<String>();
    for (String status : statuses) {
      statusList.add(status.trim());
    }
    statusList.add("ALL");
    return statusList;
  }

  public String getWorkflowInstMetFieldsFilePath() {
    return PathUtils.replaceEnvVariables(getServletContext().getInitParameter(
        "org.apache.oodt.pcs.opsui.winst.metFields.filePath"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.wicket.protocol.http.WebApplication#newSession(org.apache.wicket
   * .Request, org.apache.wicket.Response)
   */
  @Override
  public Session newSession(Request request, Response response) {
    return new FMBrowserSession(request);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.wicket.protocol.http.WebApplication#init()
   */
  @Override
  protected void init() {
    super.init();

    mountSharedResource("/images/percentImage_back1.png",
        new ResourceReference(WorkflowInstancesViewer.class,
            "percentImage_back1.png").getSharedResourceKey());
    mountSharedResource("/images/percentImage_back2.png",
        new ResourceReference(WorkflowInstancesViewer.class,
            "percentImage_back2.png").getSharedResourceKey());
    mountSharedResource("/images/percentImage_back3.png",
        new ResourceReference(WorkflowInstancesViewer.class,
            "percentImage_back3.png").getSharedResourceKey());
    mountSharedResource("/images/percentImage_back4.png",
        new ResourceReference(WorkflowInstancesViewer.class,
            "percentImage_back4.png").getSharedResourceKey());

    mountSharedResource("/images/icon_arrow_up.gif", new ResourceReference(
        StatusPage.class, "icon_arrow_up.gif").getSharedResourceKey());
    

    mountSharedResource("/images/rightcorner.gif", new ResourceReference(
        BasePage.class, "rightcorner.gif").getSharedResourceKey());
    mountSharedResource("/images/leftcorner.gif", new ResourceReference(
        BasePage.class, "leftcorner.gif").getSharedResourceKey());
    mountSharedResource("/images/selrightcorner.gif", new ResourceReference(
        BasePage.class, "selrightcorner.gif").getSharedResourceKey());
    mountSharedResource("/images/selleftcorner.gif", new ResourceReference(
        BasePage.class, "selleftcorner.gif").getSharedResourceKey());    

  }

}
