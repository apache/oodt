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

package org.apache.oodt.cas.workflow.webapp.monitor;

import org.apache.oodt.cas.webcomponents.workflow.WMMonitorAppBase;
import org.apache.oodt.cas.webcomponents.workflow.instance.WorkflowInstancesViewer;
import org.apache.oodt.cas.workflow.webapp.monitor.condition.WorkflowConditionViewerPage;
import org.apache.oodt.cas.workflow.webapp.monitor.events.WorkflowEventViewerPage;
import org.apache.oodt.cas.workflow.webapp.monitor.instance.WorkflowInstanceViewerPage;
import org.apache.oodt.cas.workflow.webapp.monitor.task.WorkflowTaskViewerPage;
import org.apache.oodt.cas.workflow.webapp.monitor.workflow.WorkflowViewerPage;
import org.apache.oodt.cas.workflow.webapp.monitor.workflow.WorkflowsViewerPage;
import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.request.target.coding.MixedParamUrlCodingStrategy;

/**
 * 
 * Describe your class here.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WMMonitorApp extends WMMonitorAppBase {

  public WMMonitorApp() {
    MixedParamUrlCodingStrategy taskPageMount = new MixedParamUrlCodingStrategy(
        "task", WorkflowTaskViewerPage.class, new String[] { "id" });
    MixedParamUrlCodingStrategy condPageMount = new MixedParamUrlCodingStrategy(
        "condition", WorkflowConditionViewerPage.class, new String[] { "id" });

    MixedParamUrlCodingStrategy workflowPageMount = new MixedParamUrlCodingStrategy(
        "workflow", WorkflowViewerPage.class, new String[] { "id" });

    MixedParamUrlCodingStrategy eventsPageMount = new MixedParamUrlCodingStrategy(
        "events", WorkflowEventViewerPage.class, new String[] {});

    MixedParamUrlCodingStrategy workflowsPageMount = new MixedParamUrlCodingStrategy(
        "workflows", WorkflowsViewerPage.class, new String[] {});

    MixedParamUrlCodingStrategy workflowInstsPageMount = new MixedParamUrlCodingStrategy(
        "instances", WorkflowInstanceViewerPage.class, new String[] { "status",
            "pageNum" });

    mount(taskPageMount);
    mount(condPageMount);
    mount(workflowPageMount);
    mount(eventsPageMount);
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
    return Home.class;
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
  }

}
