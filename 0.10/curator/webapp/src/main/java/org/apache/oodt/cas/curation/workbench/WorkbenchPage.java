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

package org.apache.oodt.cas.curation.workbench;

import org.apache.oodt.cas.curation.CurationSession;
import org.apache.oodt.cas.curation.HomePage;
import org.apache.oodt.cas.curation.login.LoginPage;
import org.apache.oodt.cas.webcomponents.curation.workbench.Workbench;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;

public class WorkbenchPage extends HomePage {

  public WorkbenchPage() {
    super();
    
    CurationSession session = (CurationSession)getSession();

    if (!session.isLoggedIn()) {
      PageParameters params = new PageParameters();
      params.add("action", "login");
      setResponsePage(LoginPage.class, params);
    }
    
    add(new Workbench(
        "curator_workbench"));
    replace(new Label("crumb_name", "Workbench"));

  }

}
