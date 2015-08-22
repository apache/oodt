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


package org.apache.oodt.cas.curation.service;

//OODT imports
import org.apache.oodt.cas.curation.metadata.CuratorConfMetKeys;
import org.apache.oodt.cas.curation.util.SSOUtils;
import org.apache.oodt.security.sso.SingleSignOn;


//JDK imports
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriInfo;


//JAX-RS imports
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


//APACHE imports
import org.apache.commons.lang.StringUtils;

/**
 * 
 * 
 * The Main Curation Web Service responsible for providing basic tools and
 * functionality for performing ingestion into the CAS filemgr, and management
 * of the staging area for ingestion.
 * 
 * @author pramirez
 * @author mattmann
 * @version $Revision$
 * 
 */
public class CurationService extends HttpServlet implements CuratorConfMetKeys {

  private static final long serialVersionUID = -5370697580594691669L;
  
  private static final Logger LOG = Logger.getLogger(CurationService.class
      .getName());
  
  protected static CurationServiceConfig config = null;
  
  protected final static String FORMAT_JSON = "json";
  
  protected final static String FORMAT_HTML = "html";
  
  protected final static String UNKNOWN_OUT_FORMAT = "Unsupported Output Format!";
  
  protected SingleSignOn sso;

  /**
   * Default Constructor.
   */
  public CurationService() {
  }

  @Override
  public void init(ServletConfig conf) throws ServletException {
    super.init(conf);
    this.config = CurationServiceConfig.getInstance(conf);
  }

  /**
   * Gets the staging area as a JSON formatted response object.
   * 
   * @param base
   *          The base directory to read files from, should be the staging area
   *          root.
   * @param path
   *          The particular child path within the staging area.
   * @param showFiles
   *          Whether or not to show {@link File#isFile()} files or not.
   * @return A String representation formatting using
   *         {@link JSONObject#toString()}.
   */
  public String getDirectoryAreaAsJSON(String base, String path,
      boolean showFiles) {
    String startingPath = (base + "/" + path);
    startingPath = StringUtils.replace(startingPath, "source", "/");
    String f[] = getFilesInDirectory(startingPath, showFiles);

    List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
    for (int i = 0; i < f.length; i++) {
      Map<String, Object> entry = new HashMap<String, Object>();
      String children[] = getFilesInDirectory(startingPath + "/" + f[i],
          showFiles);
      entry.put("text", f[i]);
      entry.put("id", path + "/" + f[i]);
      entry.put("expanded", false);
      entry.put("hasChildren", children != null && (children.length > 0));
      entry.put("isFile", new File(startingPath + "/" + f[i]).isFile());
      items.add(entry);
    }

    return JSONArray.fromObject(items).toString();
  }

  protected String[] getFilesInDirectory(String directory,
      final boolean showFiles) {
    File dir = new File(directory);
    FilenameFilter filter = new FilenameFilter() {

      public boolean accept(File dir, String name) {

        return !name.startsWith(".")
            && (showFiles || !new File(dir, name).isFile());
      }
    };
    return dir.list(filter);
  }

  protected void sendRedirect(String page, UriInfo uriInfo,
      HttpServletResponse response) throws IOException {
    response.sendRedirect(uriInfo.getBaseUriBuilder().path("../" + page)
        .build().toASCIIString());
  }

  protected String cleansePath(String path) throws UnsupportedEncodingException {
    String newPath = path;
    if (newPath.startsWith("/")) {
      newPath = path.substring(1);
    }
    newPath = URLDecoder.decode(newPath, "UTF-8");
    return newPath;

  }
  
  /**
   * Configures the web context persistence layer for the CAS SSO so that all
   * services ({@link HttpServlet}s) that extend this implementation get the
   * ability to configure an SSO object for free, essentially.
   * 
   * @param req
   *          The HTTP request object.
   * @param res
   *          The HTTP response object.
   */
  protected void configureSingleSignOn(HttpServletRequest req,
      HttpServletResponse res) {
    this.sso = SSOUtils.getWebSingleSignOn(CurationService.config, req, res);
  }
}
