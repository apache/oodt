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

import org.apache.oodt.cas.workflow.webapp.monitor.events.WorkflowEventViewerPage;
import org.apache.oodt.cas.workflow.webapp.monitor.instance.WorkflowInstanceViewerPage;
import org.apache.oodt.cas.workflow.webapp.monitor.workflow.WorkflowsViewerPage;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;

/**
 * 
 * Describe your class here.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class Home extends WebPage {

  public Home() {
    add(new Link("workflow_event_viewer_link") {
      /*
       * (non-Javadoc)
       * 
       * @see org.apache.wicket.markup.html.link.Link#onClick()
       */
      @Override
      public void onClick() {
        setResponsePage(WorkflowEventViewerPage.class);
      }
    });

    add(new Link("workflows_viewer_link") {
      /*
       * (non-Javadoc)
       * 
       * @see org.apache.wicket.markup.html.link.Link#onClick()
       */
      @Override
      public void onClick() {
        setResponsePage(WorkflowsViewerPage.class);

      }
    });

    add(new Link("workflow_insts_viewer_link") {
      /*
       * (non-Javadoc)
       * 
       * @see org.apache.wicket.markup.html.link.Link#onClick()
       */
      @Override
      public void onClick() {
        PageParameters params = new PageParameters();
        params.add("pageNum", "1");
        params.add("status", "ALL");
        setResponsePage(WorkflowInstanceViewerPage.class, params);

      }
    });

  }

}
