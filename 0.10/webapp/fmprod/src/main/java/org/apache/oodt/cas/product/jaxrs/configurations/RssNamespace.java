
package org.apache.oodt.cas.product.jaxrs.configurations;

/**
 * Represents an XML namespace for RSS XML content.
 * @author rlaidlaw
 * @version $Revision$
 */
public class RssNamespace
{
  private String prefix;
  private String uriString;

  /**
   * Creates an RssNamespace object and sets the prefix and uri string.
   * @param prefix the namespace prefix
   * @param uriString the namespace URI as a String
   */
  public RssNamespace(String prefix, String uriString)
  {
    this.prefix = prefix;
    this.uriString = uriString;
  }



  /**
   * Gets the prefix for the namespace.
   * @return the prefix for the namespace
   */
  public String getPrefix()
  {
    return prefix;
  }



  /**
   * Gets the URI for the namespace as a String
   * @return the URI for the namespace as a String
   */
  public String getUriString()
  {
    return uriString;
  }
}
