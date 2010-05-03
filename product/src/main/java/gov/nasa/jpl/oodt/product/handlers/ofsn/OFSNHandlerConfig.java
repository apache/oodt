//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.product.handlers.ofsn;

//JDK imports
import java.util.Properties;

/**
 * 
 * A configuration element in the XML config file for the OFSN handler.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class OFSNHandlerConfig {

  private String className;

  private String type;

  private String name;
  
  private Properties handlerConf;

  /**
   * @param className
   * @param type
   * @param name
   */
  public OFSNHandlerConfig(String className, String type, String name) {
    super();
    this.className = className;
    this.type = type;
    this.name = name;
    this.handlerConf = new Properties();
  }

  /**
   * 
   */
  public OFSNHandlerConfig() {
    super();
  }

  /**
   * @return the className
   */
  public String getClassName() {
    return className;
  }

  /**
   * @param className
   *          the className to set
   */
  public void setClassName(String className) {
    this.className = className;
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(String type) {
    this.type = type;
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
   * @return the handlerConf
   */
  public Properties getHandlerConf() {
    return handlerConf;
  }

  /**
   * @param handlerConf the handlerConf to set
   */
  public void setHandlerConf(Properties handlerConf) {
    this.handlerConf = handlerConf;
  }

}
