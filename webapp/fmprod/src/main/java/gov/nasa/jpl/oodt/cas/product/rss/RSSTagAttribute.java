//Copyright (c) 2010, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.product.rss;

/**
 * 
 * An attribute on an RSS output tag, generated from the
 * CAS.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class RSSTagAttribute {

  private String name;

  private String value;

  /**
   * Default constructor.
   */
  public RSSTagAttribute() {
    this.name = null;
    this.value = null;
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
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

}
