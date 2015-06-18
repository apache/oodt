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

import org.apache.oodt.cas.curation.policymgr.CurationPolicyManager;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class MetViewerServlet extends SSOConfiguredCuratorWebService {

  private static final long serialVersionUID = -991974495711678834L;

  private String basePath;

  public MetViewerServlet() {
  }

  @Override
  public void init(ServletConfig conf) throws ServletException {
    super.init(conf);
    // this.basePath =
    // PathUtils.replaceEnvVariables(getServletContext().getInitParameter(getInitParameter(PATH_VARIABLE_NAME)));
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    HttpSession session = req.getSession();
    session.setAttribute("errorMsg", "You must use GET to access this page");
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(
        "/error.jsp");
    dispatcher.forward(req, res);
  }

  // Handle HTTP GET requests by forwarding to a common processor
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    this.configureSingleSignOn(req, res);

    // Redirect if no valid user logged in
    if (!this.sso.isLoggedIn()) {
      res.sendRedirect("./login.jsp?from=" + req.getRequestURL());
      return;
    }

    String root = req.getParameter("root");
    if (root == null) {
      root = "/";
    }
    PrintWriter out = res.getWriter();

    CurationPolicyManager pm = new CurationPolicyManager();
    out.println(pm.getMetFileAsJSON(basePath, req.getParameter("file")));
    return;
  }
}
