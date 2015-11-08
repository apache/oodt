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

package org.apache.oodt.cas.curation;

import org.apache.oodt.cas.curation.login.LoginPage;
import org.apache.oodt.cas.curation.workbench.WorkbenchPage;
import org.apache.wicket.Application;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

public class HomePage extends WebPage {
  
  public HomePage() {
    super();
    CurationApp app = ((CurationApp) Application.get());
    String loggedInLabel;
    String logoutLabel = "Log Out";
    CurationSession session = (CurationSession)getSession();

    if (session.isLoggedIn()) {
      loggedInLabel = "Logged in as " + session.getLoginUsername()+ ".";
      add(new Label("loggedin_label", loggedInLabel));
      add(new Label("logout_label", logoutLabel).setVisible(false));
      add(new Link<String>("login_link") {
        @Override
        public void onClick() {
          PageParameters params = new PageParameters();
          params.add("action", "login");
          setResponsePage(LoginPage.class, params);
        }
      }.setVisible(false));
      add(new Link<String>("logout_link") {
        @Override
        public void onClick() {
          PageParameters params = new PageParameters();
          params.add("action", "logout");
          setResponsePage(LoginPage.class, params);
        }
      });
    } else {
      loggedInLabel = "Not logged in.";
      add(new Label("loggedin_label", loggedInLabel));
      add(new Label("logout_label", logoutLabel).setVisible(false));
      add(new Link<String>("login_link") {
        @Override
        public void onClick() {
          PageParameters params = new PageParameters();
          params.add("action", "login");
          setResponsePage(LoginPage.class, params);
        }
      });
      add(new Link<String>("logout_link") {
        @Override
        public void onClick() {
          PageParameters params = new PageParameters();
          params.add("action", "logout");
          setResponsePage(LoginPage.class, params);
        }
      }.setVisible(false));
    }

    String projectName = app.getProjectName() + " CAS Curation";
    add(new Label("project_name", projectName));
    add(new Label("crumb_name", "Main"));
    add(new Link<String>("home_link"){
      @Override
      public void onClick() {
        setResponsePage(WorkbenchPage.class);
      }
    });
  }

}
