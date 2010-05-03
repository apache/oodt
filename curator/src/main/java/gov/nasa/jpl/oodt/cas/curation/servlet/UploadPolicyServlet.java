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


package gov.nasa.jpl.oodt.cas.curation.servlet;

//JDK imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
public class UploadPolicyServlet extends SSOConfiguredCuratorWebService {

  private static final long serialVersionUID = 3992641831567821866L;

  private static final int BYTE_READ_SIZE = 200000;
  private static final String STORAGE_LOCATION = "/";
  private static final String TEMP_REPOSITORY = "/tmp/";

  public UploadPolicyServlet() {
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

    // Transfer control to the next step in the process
    res
        .sendRedirect(req.getContextPath()
            + "/ingestData.jsp?step=choosePolicy");
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

  private void processRequest(HttpServletRequest request,
      HttpServletResponse response) throws ServletException {
    DiskFileItemFactory factory = new DiskFileItemFactory();

    factory.setSizeThreshold(10000);
    factory.setRepository(new File(TEMP_REPOSITORY));

    ServletFileUpload upload = new ServletFileUpload(factory);

    String target = "";

    List<FileItem> items = null;
    try {
      items = upload.parseRequest(request);
    } catch (Exception e) {
      throw new ServletException(e.getMessage());
    }

    for (FileItem item : items) {
      if (item.isFormField()) {
        String name = item.getFieldName();
        String value = item.getString();
        if (name.equals("target"))
          target = value;
      }
    }

    for (FileItem item : items) {
      InputStream is = null;
      OutputStream os = null;

      try {
        if (!item.isFormField()) {
          is = item.getInputStream();
          String filename = STORAGE_LOCATION + "/" + target + "/"
              + item.getName();
          os = new FileOutputStream(new File(filename));
          byte[] b = new byte[BYTE_READ_SIZE];
          int bytesRead = 0;
          while ((bytesRead = is.read(b)) != -1) {
            os.write(b, 0, bytesRead);
          }
        }
      } catch (Exception e) {
        throw new ServletException(e.getMessage());
      } finally {
        if (os != null) {
          try {
            os.close();
          } catch (Exception ignore) {
          }

          os = null;
        }

        if (is != null) {
          try {
            is.close();
          } catch (Exception ignore) {
          }

          is = null;
        }
      }
    }
  }

}
