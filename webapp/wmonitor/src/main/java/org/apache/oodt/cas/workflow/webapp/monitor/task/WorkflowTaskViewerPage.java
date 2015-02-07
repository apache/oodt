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

package org.apache.oodt.cas.workflow.webapp.monitor.task;

import org.apache.oodt.cas.webcomponents.workflow.WMMonitorAppBase;
import org.apache.oodt.cas.webcomponents.workflow.tasks.WorkflowTaskViewer;
import org.apache.oodt.cas.workflow.webapp.monitor.condition.WorkflowConditionViewerPage;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

/**
 * 
 * Describe your class here.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowTaskViewerPage extends WebPage {

  public WorkflowTaskViewerPage(PageParameters params) {
    add(new Link("home_link"){
      /* (non-Javadoc)
      * @see org.apache.wicket.markup.html.link.Link#onClick()
      */
     @Override
     public void onClick() {
       setResponsePage(getApplication().getHomePage());
     }
  });    
    add(new Label("task_id", params.getString("id")));
    WorkflowTaskViewer viewerComponent = new WorkflowTaskViewer("task_viewer",
        ((WMMonitorAppBase) getApplication()).getWorkflowUrl(), params
            .getString("id"), WorkflowConditionViewerPage.class);
    add(viewerComponent);
  }

}
