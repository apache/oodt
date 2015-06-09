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
    return baseUri += baseUri.endsWith("/") ? "" : "/";
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
