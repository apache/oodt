/**
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.oodt.cas.product.jaxrs.writers;

import java.text.SimpleDateFormat;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.oodt.cas.product.jaxrs.configurations.RssConfiguration;

/**
 * Class with shared properties and behavior for RSS writers.
 * @author rlaidlaw
 * @version $Revision$
 */
public abstract class RssWriter
{
  // Constants used by RSS writers.
  protected static final String COPYRIGHT =
    "Copyright 2013: Apache Software Foundation";
  protected static final String LANGUAGE = "en-us";
  protected static final String GENERATOR = "CAS File Manager";
  protected static final String DATE_FORMAT = "E, dd MMM yyyy HH:mm:ss Z";
  protected static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
    DATE_FORMAT);

  @Context
  private ServletContext context;

  @Context
  private UriInfo uriInfo;



  /**
   * Gets the HTTP servlet request URL up to the final '/' as a {@link String}.
   * @return the HTTP servlet request URL up to the final '/' as a String
   */
  public String getBaseUri()
  {
    String baseUri = uriInfo.getBaseUri().toString();
    return baseUri.endsWith("/") ? "" : "/";
  }



  /**
   * Gets a configuration object from the servlet context, or null if not found.
   * @return a configuration object or null if nothing was found
   */
  public RssConfiguration getConfiguration()
  {
    String name = uriInfo.getQueryParameters().getFirst("configuration");

    if (name != null && !name.trim().equals(""))
    {
      Map<String, RssConfiguration> map =
        (Map<String, RssConfiguration>) context
          .getAttribute("rssConfigurations");

      if (map != null)
      {
        return map.get(name);
      }
    }

    return null;
  }
}
