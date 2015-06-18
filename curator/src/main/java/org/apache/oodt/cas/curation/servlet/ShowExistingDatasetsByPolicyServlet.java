/*
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


package org.apache.oodt.cas.curation.servlet;

//OODT imports
import org.apache.oodt.cas.curation.policymgr.CurationPolicyManager;
import org.apache.oodt.cas.metadata.util.PathUtils;

//JDK imports
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// Displays the contents of the staging area as an HTML unordered list (<ul></ul>): 
// This servlet is meant to be invoked via an AJAX call, the generated HTML output
// will be dynamically inserted into a DOM element on the requesting page.
//

public class ShowExistingDatasetsByPolicyServlet extends
    SSOConfiguredCuratorWebService {

  private String stagingAreaPath;
  
  private String policyDirPath;
  
  private static final long serialVersionUID = 4844652723865688280L;

  public ShowExistingDatasetsByPolicyServlet() {
  }

  public void init(ServletConfig conf) throws ServletException {
    super.init(conf);
    this.stagingAreaPath = PathUtils.replaceEnvVariables(getServletContext().getInitParameter(STAGING_AREA_PATH));
    this.policyDirPath = PathUtils.replaceEnvVariables(getServletContext().getInitParameter(POLICY_UPLOAD_PATH));    
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    HttpSession session = req.getSession();
    session.setAttribute("errorMsg", "You must use GET to access this page");
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(
        "/error.jsp");
    dispatcher.forward(req, res);

  }

  // Handle HTTP GET requests by forwarding to a common processor
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    this.configureSingleSignOn(req, res);

    // Redirect if no valid user logged in
    if (!this.sso.isLoggedIn()) {
      res.sendRedirect("/login.jsp?from=" + req.getRequestURL());
      return;
    }

    String policy = req.getParameter("policy");
    PrintWriter out = res.getWriter();

    // we return with blank page with error
    if (policy == null) {
      out.println("");
      return;
    }

    CurationPolicyManager pm = new CurationPolicyManager(this.policyDirPath, this.stagingAreaPath);
    out.println(pm.getDatasetsByPolicyAsJSON(policy));

    return;
  }
}
