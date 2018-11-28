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

//Wicket imports
import org.apache.oodt.pcs.opsui.config.ConfigPage;
import org.apache.oodt.pcs.opsui.status.StatusPage;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;

/**
 *
 * Provides the controller for the OPSUI template.
 *
 * @author mattmann
 * @version $Revision$
 *
 */
public class BasePage extends OPSUIWebPage {
  
  protected OpsuiApp app;
  
  public BasePage(PageParameters parameters){    
    this.app = (OpsuiApp)getApplication();
    add(new Link("home_link"){
      /* (non-Javadoc)
       * @see org.apache.wicket.markup.html.link.Link#onClick()
       */
      @Override
      public void onClick() {
        setResponsePage(app.getHomePage());
      }
    });
    
    add(new Link("fmbrowser_link"){
      /* (non-Javadoc)
       * @see org.apache.wicket.markup.html.link.Link#onClick()
       */
      @Override
      public void onClick() {
       setResponsePage(TypesPage.class); 
      }
    });
    
    add(new Link("pcsstatus_link"){
      
    /* (non-Javadoc)
       * @see org.apache.wicket.markup.html.link.Link#onClick()
       */
      @Override
      public void onClick() {
        setResponsePage(StatusPage.class);
        
      }
    });
    
    add(new Link("wmonitor_link"){
       /* (non-Javadoc)
       * @see org.apache.wicket.markup.html.link.Link#onClick()
       */
      @Override
      public void onClick() {
        PageParameters params = new PageParameters();
        params.add("status", "ALL");
        params.add("pageNum", "1");
        setResponsePage(WorkflowInstanceViewerPage.class, params);
        
      }
    });
    
    add(new Link("config_link"){
        /* (non-Javadoc)
         * @see org.apache.wicket.markup.html.link.Link#onClick()
         */
        @Override
        public void onClick() {
          PageParameters params = new PageParameters();
          params.add("tab", "File Manager");
          setResponsePage(ConfigPage.class, params);  
        }
    });
    
    add(new Link("curate_link"){
      @Override
      public void onClick() {
        setResponsePage(WorkbenchPage.class);
      }
    });
    
    add(new ExternalLink("ganglia_link", app.getGangliaUrl()));
    
    add(new ExternalLink("contact_link", "mailto:"+((OpsuiApp)getApplication()).getEmailContactLink()));
  }

}
