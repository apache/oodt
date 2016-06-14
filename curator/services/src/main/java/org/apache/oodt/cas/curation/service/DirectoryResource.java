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

//JDK imports
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

//JAX-RS imports
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("directory")
/**
 * 
 * A web service endpoint to a service providing views of the staging area, the
 * archive area, and the met output area.
 * 
 * @author pramirez
 * @version $Id$
 */
public class DirectoryResource extends CurationService {
  private static final Logger LOG = Logger.getLogger(DirectoryResource.class
      .getName());

  @Context
  UriInfo uriInfo;

  private static final long serialVersionUID = 715126227357637464L;

  @GET
  @Path("staging")
  @Produces("text/plain")
  public String showStagingArea(
      @DefaultValue("/") @QueryParam("path") String path,
      @DefaultValue("true") @QueryParam("showFiles") boolean showFiles,
      @DefaultValue(FORMAT_HTML) @QueryParam("format") String format) {
    if (FORMAT_HTML.equals(format)) {
      return this.getDirectoryAreaAsHTML(
          CurationService.config.getStagingAreaPath(), path, showFiles);

    }
    return this.getDirectoryAreaAsJSON(
        CurationService.config.getStagingAreaPath(), path, showFiles);
  }

  @GET
  @Path("metadata")
  @Produces("text/plain")
  public String showMetArea(@DefaultValue("/") @QueryParam("path") String path,
      @DefaultValue("true") @QueryParam("showFiles") boolean showFiles) {
    return this.getDirectoryAreaAsJSON(CurationService.config.getMetAreaPath(),
        path, showFiles);
  }

  @GET
  @Path("catalog")
  @Produces("text/plain")
  public String showArchiveArea(@QueryParam("policy") String policy,
      @QueryParam("productType") String productType,
      @DefaultValue("20") @QueryParam("num") int numberOfResults,
      @DefaultValue("0") @QueryParam("start") int start) {

    // Figure out where the product type root path is and display contents
    return productType;
  }

  public String getDirectoryAreaAsHTML(String base, String path,
      boolean showFiles) {
    StringBuilder html = new StringBuilder();
    String relativePath;
    try {
      relativePath = this.cleansePath(path);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Error decoding path: [" + path + "]: Message: "
          + e.getMessage());
      return html.toString();
    }

    String startingPath = (base + "/" + relativePath);
    String f[] = this.getFilesInDirectory(startingPath, showFiles);

    html.append("<ul class=\"fileTree\">\r\n");
    // Loop through and list directories first. Nicer for UI to get these first

    if (f != null) {
      for (String aF1 : f) {
        if (new File(startingPath + "/" + aF1).isDirectory()) {
          html.append(" <li class=\"directory collapsed\">");
          html.append("<a href=\"#\" rel=\"").append(relativePath).append("/")
              .append(aF1).append("\">").append(aF1).append("</a>");
          html.append("</li>\r\n");
        }
      }
      // If we are showing files now loop through and show files
      if (showFiles) {
        for (String aF : f) {
          if (new File(startingPath + "/" + aF).isFile()) {
            String filename = new File(startingPath + "/" + aF).getName();
            String ext = filename.substring(filename.lastIndexOf('.') + 1);
            html.append(" <li class=\"file draggy ext_").append(ext)
                .append("\">");
            html.append("<a href=\"#\" rel=\"").append(relativePath)
                .append("/").append(aF).append("\">").append(aF)
                .append("</a>");
            html.append("</li>\r\n");
          }
        }
      }

    }
    html.append("</ul>");

    return html.toString();
  }

}
