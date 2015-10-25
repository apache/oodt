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

import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.oodt.cas.product.jaxrs.configurations.RdfConfiguration;

/**
 * Class with shared properties and behavior for RDF writers.
 * @author rlaidlaw
 * @version $Revision$
 */
public class RdfWriter
{
  // The RDF namespace.
  protected static final String RDF_NAMESPACE_NAME = "xmlns:rdf";
  protected static final String RDF_NAMESPACE_VALUE =
    "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  // The CAS namespace - used as a default if no configuration is specified.
  protected static final String CAS_NAMESPACE_PREFIX = "cas";
  protected static final String CAS_NAMESPACE_NAME = "xmlns:"
    + CAS_NAMESPACE_PREFIX;
  protected static final String CAS_NAMESPACE_VALUE =
    "http://oodt.apache.org/ns/cas";

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
  public RdfConfiguration getConfiguration()
  {
    String name = uriInfo.getQueryParameters().getFirst("configuration");

    if (name != null && !name.trim().equals(""))
    {
      Map<String, RdfConfiguration> map =
        (Map<String, RdfConfiguration>) context
          .getAttribute("rdfConfigurations");

      if (map != null)
      {
        return map.get(name);
      }
    }

    return null;
  }
}
