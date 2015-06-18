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
import org.apache.oodt.cas.metadata.util.PathUtils;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//APACHE imports
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

// Handles the form (POST) submission made from: 
// IngestData > Create new dataset > provideDatasetDefinitionFiles
// Redirects to:
// IngestData > Create new dataset > 
public class UploadMetExtractorConfigServlet extends
    SSOConfiguredCuratorWebService {

  public UploadMetExtractorConfigServlet() {
  }

  public void init(ServletConfig conf) throws ServletException {
    super.init(conf);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    this.configureSingleSignOn(req, res);

    // Redirect if no valid user logged in
    if (!this.sso.isLoggedIn()) {
      res.sendRedirect("/login.jsp?from=" + req.getRequestURL());
      return;
    }

    // Call the upload method
    processRequest(req, res);

    // Transfer control to the next step in the process
    res.sendRedirect(req.getContextPath()
        + "/addData.jsp?step=specifyIngestOptions");
  }

  // Handle HTTP GET requests by forwarding to a common processor
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    HttpSession session = req.getSession();
    session.setAttribute("errorMsg", "You must use POST to access this page");
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(
        "/error.jsp");
    dispatcher.forward(req, res);
  }

  private static final int BYTE_THRESHOLD = 10000;
  private static final String TEMP_REPOSITORY = "/tmp/";

  private void processRequest(HttpServletRequest request,
      HttpServletResponse response) {
    try {
      DiskFileItemFactory factory = new DiskFileItemFactory();

      factory.setSizeThreshold(BYTE_THRESHOLD);
      factory.setRepository(new File(TEMP_REPOSITORY));

      ServletFileUpload upload = new ServletFileUpload(factory);

      List<FileItem> items = upload.parseRequest(request);
      String targetPath = PathUtils.replaceEnvVariables(getServletContext()
          .getInitParameter(MET_EXTRACTOR_CONF_UPLOAD_PATH));

      HttpSession session = request.getSession();
      session.removeAttribute("metextConfigFilePath");

      // make sure that the target (policy) directory exists first

      try {
        String policyDirectory = targetPath;
        File d = new File(policyDirectory);

        if (!d.isDirectory()) {
          boolean success = (new File(policyDirectory)).mkdirs();
          if (!success) {
            return; // no need to do anything if policy dir cannot be created
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      for (FileItem item : items) {
        if (!item.isFormField()) {
          String fullFilePath = targetPath + "/" + item.getName();
          item.write(new File(fullFilePath));

          // only set this if it hasn't been set yet
          // assumption: the first conf file is the gold source
          if (session.getAttribute("metextConfigFilePath") == null) {
            session.setAttribute("metextConfigFilePath", fullFilePath);
          }

        } else {
          // it's a simple field, check if it's name is metext, and if
          // so
          // get and set everything

          if (item.getFieldName().equals("metext")) {
            // Store the type of metadata extractor in the session
            // Get the full name for use by the cas
            String metextFullName = item.getString();
            session.setAttribute("metext", metextFullName);
            // Get the pretty name for display on the browser
            session.setAttribute("metextPrettyName", metextFullName
                .substring(metextFullName.lastIndexOf(".") + 1));
          }

        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
