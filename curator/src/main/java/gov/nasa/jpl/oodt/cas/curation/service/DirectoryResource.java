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


package gov.nasa.jpl.oodt.cas.curation.service;

//JAX-RS imports
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.spi.resource.Singleton;

@Path("directory")
@Singleton
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
      String response = this.getDirectoryAreaAsHTML(CurationService.config
          .getStagingAreaPath(), path, showFiles);
      return response;
    }
    return this.getDirectoryAreaAsJSON(CurationService.config
        .getStagingAreaPath(), path, showFiles);
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
    StringBuffer html = new StringBuffer();
    String relativePath = null;
    try {
      relativePath = this.cleansePath(path);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.log(Level.WARNING, "Error decoding path: [" + path + "]: Message: "
          + e.getMessage());
      return html.toString();
    }

    String startingPath = (base + "/" + relativePath);
    String f[] = this.getFilesInDirectory(startingPath, showFiles);

    html.append("<ul class=\"fileTree\">\r\n");
    // Loop through and list directories first. Nicer for UI to get these first
    for (int i = 0; i < f.length; i++) {
      if (new File(startingPath + "/" + f[i]).isDirectory()) {
        html.append(" <li class=\"directory collapsed\">");
        html.append("<a href=\"#\" rel=\"").append(relativePath).append("/")
            .append(f[i]).append("\">").append(f[i]).append("</a>");
        html.append("</li>\r\n");
      }
    }
    // If we are showing files now loop through and show files
    if (showFiles) {
      for (int i = 0; i < f.length; i++) {
        if (new File(startingPath + "/" + f[i]).isFile()) {
          String filename = new File(startingPath + "/" + f[i]).getName();
          String ext = filename.substring(filename.lastIndexOf('.') + 1);
          html.append(" <li class=\"file draggy ext_").append(ext)
              .append("\">");
          html.append("<a href=\"#\" rel=\"").append(relativePath).append("/")
              .append(f[i]).append("\">").append(f[i]).append("</a>");
          html.append("</li>\r\n");
        }
      }
    }
    html.append("</ul>");

    return html.toString();
  }
  
}
