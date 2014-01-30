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
    return baseUri += baseUri.endsWith("/") ? "" : "/";
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
