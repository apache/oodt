//Copyright (c) 2010, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.product.rss;

//JDK imports
import java.util.List;
import java.util.Vector;

/**
 * 
 * An output RSS tag to include in an RSS feed as defined by the
 * {@link RSSProductServlet}'s {@link RSSConfig}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class RSSTag {

  private String name;

  private String source;

  private List<RSSTagAttribute> attrs;

  /**
   * Default constructor.
   */
  public RSSTag() {
    this.name = null;
    this.source = null;
    this.attrs = new Vector<RSSTagAttribute>();
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the source
   */
  public String getSource() {
    return source;
  }

  /**
   * @param source
   *          the source to set
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * @return the attrs
   */
  public List<RSSTagAttribute> getAttrs() {
    return attrs;
  }

  /**
   * @param attrs
   *          the attrs to set
   */
  public void setAttrs(List<RSSTagAttribute> attrs) {
    this.attrs = attrs;
  }

}
