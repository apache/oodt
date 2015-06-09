package org.apache.oodt.cas.product.jaxrs.configurations;

/**
 * Represents an attribute for an RSS XML tag.
 * @author mattmann
 * @author rlaidlaw
 * @version $Revision$
*/
public class RssTagAttribute
{
  private String name;
  private String value;

  /**
   * Creates an RssTagAttribute and sets the name and value.
   * @param name the name of the attribute
   * @param value the value for the attribute
   */
  public RssTagAttribute(String name, String value)
  {
    this.name = name;
    this.value = value;
  }

  /**
   * Gets the name of the attribute.
   * @return the name of the attribute
   */
  public String getName()
  {
    return name;
  }



  /**
   * Gets the value of the attribute.
   * @return the value of the attribute
   */
  public String getValue()
  {
    return value;
  }
}
