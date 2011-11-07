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
import org.apache.oodt.cas.webcomponents.workflow.model.WorkflowViewer;

//Wicket imports
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

/**
 * 
 * Controller for the WorkflowViewerPage.html.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowViewerPage extends WorkflowCrumbedPage {

  /**
   * @param parameters
   */
  public WorkflowViewerPage(PageParameters parameters) {
    super(parameters);
    add(new WorkflowViewer("workflow_viewer", app.getWmUrlStr(),
        parameters.getString("id"), WorkflowTaskViewerPage.class));
    add(new Link("workflows_viewer_link"){
        /* (non-Javadoc)
         * @see org.apache.wicket.markup.html.link.Link#onClick()
         */
        @Override
        public void onClick() {
          setResponsePage(WorkflowsViewerPage.class);          
        }
    });
    
    add(new Label("workflow_id", parameters.getString("id")));
  }

}
