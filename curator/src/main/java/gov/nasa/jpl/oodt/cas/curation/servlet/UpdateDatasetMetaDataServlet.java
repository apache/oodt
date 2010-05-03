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
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// OODT imports
import gov.nasa.jpl.oodt.cas.curation.policymgr.CurationPolicyManager;
import gov.nasa.jpl.oodt.cas.curation.util.CurationXmlStructFactory;
import gov.nasa.jpl.oodt.cas.curation.util.HTMLEncode;
import gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType;
import gov.nasa.jpl.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;

/**
 * This servlet processes form data submitted via POST from the dataset metadata
 * management tool under development for the CAS Curator.
 * 
 * Product type metadata values can be edited or replaced from the web
 * interface. Form values are saved into their corresponding fields in a
 * CasProductType instance for the current product type.
 * 
 * @author aclark
 * @author mattmann
 * 
 */
public class UpdateDatasetMetaDataServlet extends
    SSOConfiguredCuratorWebService {

  private static final Logger LOG = Logger
      .getLogger(UpdateDatasetMetaDataServlet.class.getName());

  private String stagingAreaPath;

  private String policyDirPath;
  
  private XmlRpcFileManagerClient fm;

  public UpdateDatasetMetaDataServlet() {
  }

  public void init(ServletConfig conf) throws ServletException {
    super.init(conf);
    this.stagingAreaPath = PathUtils.replaceEnvVariables(getServletContext()
        .getInitParameter(STAGING_AREA_PATH));
    this.policyDirPath = PathUtils.replaceEnvVariables(getServletContext()
        .getInitParameter(POLICY_UPLOAD_PATH));
    this.setFm();
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    this.configureSingleSignOn(req, res);

    // Redirect if no valid user logged in
    if (!this.sso.isLoggedIn()) {
      res.sendRedirect("/login.jsp?from=" + req.getRequestURL());
      return;
    }

    HttpSession session = req.getSession();

    // read API parameters from session
    String action = req.getParameter("action");
    String step = req.getParameter("step");
    String policyName = req.getParameter("dsCollection");
    String productTypeName = req.getParameter("ds");

    // instantiate product type object
    CurationPolicyManager cpm = new CurationPolicyManager(this.policyDirPath,
        this.stagingAreaPath);
    Map<String, ProductType> types = cpm.getProductTypes(policyName);

    // get metadata hash table
    ProductType cpt = types.get(productTypeName);

    // get all submitted form values
    Enumeration formKeys = req.getParameterNames();

    // update metadata value from POST form
    String keyName;
    while (formKeys.hasMoreElements()) {
      String keyField = (String) formKeys.nextElement();

      // extract the metadata id from the form field
      String[] tokens = keyField.split("_");
      if (tokens.length == 2 && cpt.getTypeMetadata().containsKey(tokens[1])) {
        keyName = tokens[1];
        // get the submitted values for that key
        String[] formValues = req.getParameterValues(keyField);

        // save new value
        // PubMedID requires HTML entity encoding because
        // of hyperlinks in the metadata field.
        // TODO: create an external XML file that contains key names
        // TODO: like PubMedID that indicate whether or not the value of the key
        // TODO: should be HTML encoded

        if (keyName.equals("PubMedID")) {
          cpt.getTypeMetadata().replaceMetadata(keyName,
              HTMLEncode.encode(formValues[0]));
        } else
          cpt.getTypeMetadata().replaceMetadata(keyName, formValues[0]);
      }
    }

    // build policy file path
    String policyDirectory = PathUtils.replaceEnvVariables(this
        .getServletContext().getInitParameter(POLICY_UPLOAD_PATH));
    String policyPath = policyDirectory;
    String policyFile = policyPath + "/" + policyName + "/product-types.xml";
    LOG.log(Level.INFO, "updating CAS policy at: [" + policyFile + "]");

    // serialize all CasProductType instances from metaDataItems hashtable
    try {
      CurationXmlStructFactory.writeProductTypeXmlDocument(cpm
          .typesToList(types), policyFile);
    } catch (Exception e) {
      throw new ServletException(
          "error writing product type xml document! Message: " + e.getMessage());

    }

    // refresh the config on the fm end
    this.fm.refreshConfigAndPolicy();
    
    // Transfer control to the next step in the process
    res.sendRedirect(req.getContextPath() + "/manageDataset.jsp?step=" + step);
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

  private void setFm() {
    String fmUrlStr = PathUtils.replaceEnvVariables(getServletContext()
        .getInitParameter(FM_URL));

    try {
      this.fm = new XmlRpcFileManagerClient(safeGetUrl(fmUrlStr));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static URL safeGetUrl(String urlStr) {
    URL url = null;
    try {
      url = new URL(urlStr);
    } catch (Exception ignore) {
    }

    return url;
  }
  
}
