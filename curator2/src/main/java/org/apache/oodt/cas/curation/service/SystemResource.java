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

//JAX-RS imports
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("system")
public class SystemResource extends CurationService {

  @Context
  UriInfo uriInfo;
  
  private static final long serialVersionUID = -2318607955517605998L;

  /**
   * This is what will go in the upper left box on landing page. To gather
   * information such as is the file manager up If this needs to be something
   * that gets updated periodically then it would change into a JSON feed
   */
  @GET
  @Path("stats")
  @Produces("text/html")
  public String getStatistics() {
    return "<div>Server Stats</div>";
  }

  /**
   * This returns the configuration information that is set in the context.xml
   * file.
   */
  @GET
  @Path("config")
  @Produces("text/html")
  public String getConfig() {
    return "";
  }

  /**
   * This will return the information that appears in the upper right box on
   * landing page.
   */
  @GET
  @Path("feed")
  @Produces("text/html")
  public String getFeed() {
    return "<div>Latest Products?</div>";
  }
}
