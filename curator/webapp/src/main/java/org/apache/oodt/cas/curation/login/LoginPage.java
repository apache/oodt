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

package org.apache.oodt.cas.curation.login;

import org.apache.oodt.cas.curation.CurationApp;
import org.apache.oodt.cas.curation.CurationSession;
import org.apache.oodt.cas.curation.HomePage;
import org.apache.oodt.cas.curation.workbench.WorkbenchPage;
import org.apache.oodt.security.sso.AbstractWebBasedSingleSignOn;
import org.apache.oodt.security.sso.SingleSignOnFactory;
import org.apache.wicket.Application;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;

public class LoginPage extends HomePage {

  public LoginPage(PageParameters parameters) {
    super();
    final CurationApp app = (CurationApp)Application.get();
    final CurationSession session = (CurationSession)getSession();
    String ssoClass = app.getSSOImplClass();
    final AbstractWebBasedSingleSignOn sso = SingleSignOnFactory
        .getWebBasedSingleSignOn(ssoClass);
    sso.setReq(((WebRequest) RequestCycle.get().getRequest())
        .getHttpServletRequest());
    sso.setRes(((WebResponse) RequestCycle.get().getResponse())
        .getHttpServletResponse());
    
    String action = parameters.getString("action");
    String appNameString = app.getProjectName()+" CAS Curation Interface";
    add(new Label("login_project_name", appNameString));
    replace(new Label("crumb_name", "Login"));
    final WebMarkupContainer creds = new WebMarkupContainer("invalid_creds");
    final WebMarkupContainer connect = new WebMarkupContainer("connect_error");
    creds.setVisible(false);
    connect.setVisible(false);
    final TextField<String> loginUser = new TextField<String>("login_username", new Model<String>(""));
    final PasswordTextField pass = new PasswordTextField("password", new Model<String>(""));
    
    
    Form<?> form = new Form<Void>("login_form"){

      private static final long serialVersionUID = 1L;
      
      @Override
      protected void onSubmit() {
        String username = loginUser.getModelObject();
        String password = pass.getModelObject();
        
        if(sso.login(username, password)){
          session.setLoggedIn(true);
          session.setLoginUsername(username);
          setResponsePage(WorkbenchPage.class);
        }
        else{
          session.setLoggedIn(false);
          if (session.getLoginUsername() == null){
            connect.setVisible(true);
          }
          else{
            creds.setVisible(true);
          }
        }
        
      }
      
    };
    
    form.add(loginUser);
    form.add(pass);
    form.add(creds);
    form.add(connect);
    add(form);
    
    if(action.equals("logout")){
      sso.logout();
      session.setLoginUsername(null);
      session.setLoggedIn(false);
      PageParameters params = new PageParameters();
      params.add("action", "login");
      setResponsePage(LoginPage.class, params);
    }

  }

  
  

}
