package org.apache.oodt.cas.product.jaxrs.configurations;

import java.util.List;
import java.util.Vector;

/**
 * Represents an RSS XML tag to include in an RSS feed.
 * @author mattmann
 * @author rlaidlaw
 * @version $Revision$
 */
public class RssTag
{
  private String name = null;
  private String source = null;
  private List<RssTagAttribute> attributes = new Vector<RssTagAttribute>();

  /**
   * Creates an RssTag object and sets the name, source and attributes.
   * @param name the name for the tag
   * @param source the source for the tag
   * @param attributes the attributes for the tag
   */
  public RssTag(String name, String source, List<RssTagAttribute> attributes)
  {
    this.name = name;
    this.source = source;
    this.attributes = attributes;
  }



  /**
   * Gets the name for the tag.
   * @return the name for the tag
   */
  public String getName()
  {
    return name;
  }



  /**
   * Gets the source for the tag.
   * @return the source for the tag
   */
  public String getSource()
  {
    return source;
  }



  /**
   * Gets the attributes for the tag.
   * @return the attributes for the tag
   */
  public List<RssTagAttribute> getAttributes()
  {
    return attributes;
  }
}
