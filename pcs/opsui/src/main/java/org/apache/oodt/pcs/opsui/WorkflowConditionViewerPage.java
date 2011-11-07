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
import org.apache.oodt.cas.webcomponents.workflow.conditions.WorkflowConditionViewer;

//Wicket imports
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;

/**
 * 
 * Page controller for the WorkflowConditionViewerPage.html file.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowConditionViewerPage extends WorkflowCrumbedPage {

  /**
   * @param parameters
   */
  public WorkflowConditionViewerPage(PageParameters parameters) {
    super(parameters);
    add(new WorkflowConditionViewer("cond_viewer", app.getWmUrlStr(),
        parameters.getString("id")));
    add(new Label("cond_id", parameters.getString("id")));
  }

}
