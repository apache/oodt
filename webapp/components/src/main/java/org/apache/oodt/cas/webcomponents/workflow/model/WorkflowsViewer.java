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

package org.apache.oodt.cas.webcomponents.workflow.model;

import java.util.List;

import org.apache.oodt.cas.webcomponents.workflow.WorkflowMgrConn;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/**
 * 
 * Describe your class here.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowsViewer extends Panel {

  private static final long serialVersionUID = 808225347615989283L;

  private WorkflowMgrConn wm;

  /**
   * @param id
   */
  public WorkflowsViewer(String id, String workflowUrlStr,
      final Class<? extends WebPage> wViewerPage) {
    super(id);
    this.wm = new WorkflowMgrConn(workflowUrlStr);

    List<Workflow> workflows = this.wm.safeGetWorkflows();
    add(new ListView<Workflow>("workflow_list", workflows) {

      @Override
      protected void populateItem(ListItem<Workflow> item) {
        Workflow w = item.getModelObject();
        item.add(new Label("workflow_id", w.getId()));
        item.add(new Label("workflow_name", w.getName()));
        item.add(new Link<String>("workflow_link", new Model(w.getId())) {
          /*
           * (non-Javadoc)
           * 
           * @see org.apache.wicket.markup.html.link.Link#onClick()
           */
          @Override
          public void onClick() {
            PageParameters params = new PageParameters();
            params.add("id", getModelObject());
            setResponsePage(wViewerPage, params);
          }
        });
      }
    });

  }

}
