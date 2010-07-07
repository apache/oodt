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
import org.apache.oodt.cas.crawl.MetExtractorProductCrawler;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

//JDK imports
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//SPRING imports
import org.springframework.context.support.FileSystemXmlApplicationContext;

// Handles the form (POST) submission made from: 
// IngestData > Create new dataset > provideDatasetDefinitionFiles
// Redirects to:
// IngestData > Create new dataset > 
public class BeginIngestionTaskServlet extends SSOConfiguredCuratorWebService {

  /**
	 * 
	 */
  private static final long serialVersionUID = -4607282147807134757L;

  public BeginIngestionTaskServlet() {
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

    // TODO: Take the parameters from the POST variables and set the crawlers in
    // motion
    // 
    // POST Variables to expect:
    //
    // dsCollection - data set collection (Grizzle, Aliu, etc)
    // ds - data set (EGFRTranslocation, PreInvasiveNeoplasia, etc)
    // metext - CopyOnRewrite, etc
    // ingestActionUnique - crawler pre-ingest action
    // ingestActionDeleteDataFile - crawler post-ingest action
    // ingestionRootPath - prepend /data/ingest to this for full path
    //
    //

    MetExtractorProductCrawler casCrawler = this
        .configureCrawler(getServletContext().getInitParameter(
            FM_URL));
    casCrawler.setActionIds(Arrays.asList(req
        .getParameterValues("ingestAction")));
    String productRootPath = "/data/ingest/"
        + req.getParameter("ingestionRootPath");
    casCrawler.setProductPath(productRootPath);
    try {
      casCrawler.setMetExtractor(req.getParameter("metext"));
    } catch (Exception e) {
      throw new ServletException(e.getMessage());
    }

    try {
		casCrawler
			.setMetExtractorConfig(req.getParameter("metextConfigFilePath"));
	} catch (MetExtractionException e) {
		throw new ServletException(e.getMessage());
	}

    // turn em' loose
    casCrawler.crawl();
    
    // now we should explicitly destroy various sessions for cleanup
    HttpSession session = req.getSession();
    session.removeAttribute("metextPrettyName");
    session.removeAttribute("metext");
    session.removeAttribute("metextConfigFilePath");

    // Transfer control to the next step in the process
    res.sendRedirect(req.getContextPath() + "/home.jsp");
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

  private MetExtractorProductCrawler configureCrawler(String ecasUrlStr)
      throws MalformedURLException {
    MetExtractorProductCrawler crawler = new MetExtractorProductCrawler();
    crawler
        .setClientTransferer(DEFAULT_TRANSFER_FACTORY);
    crawler.setApplicationContext(new FileSystemXmlApplicationContext(CRAWLER_CONF_FILE));
    crawler.setCrawlForDirs(false);
    crawler.setFilemgrUrl(ecasUrlStr);
    crawler.setNoRecur(false);
    return crawler;
  }
}
