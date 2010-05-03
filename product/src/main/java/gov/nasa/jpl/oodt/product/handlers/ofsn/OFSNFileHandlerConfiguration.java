//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.product.handlers.ofsn;

//JDK imports
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * The OFSN product handler's configuration object.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class OFSNFileHandlerConfiguration {

  protected Map<String, OFSNHandlerConfig> handlerTable;

  private String productRoot;

  private String id;

  private String name;

  public OFSNFileHandlerConfiguration(String productRoot, String id, String name) {
    this.handlerTable = new HashMap<String, OFSNHandlerConfig>();
    this.productRoot = productRoot;
    this.id = id;
    this.name = name;
    cleanse(this.productRoot);
  }

  public OFSNFileHandlerConfiguration() {
    this(null, null, null);
  }

  public String getHandlerType(String handlerName) {
    if (this.handlerTable.containsKey(handlerName)) {
      return this.handlerTable.get(handlerName).getType();
    } else
      return null;
  }

  public String getHandlerClass(String handlerName) {
    if (this.handlerTable.containsKey(handlerName)) {
      return this.handlerTable.get(handlerName).getClassName();
    } else
      return null;
  }

  public List<OFSNHandlerConfig> getHandlerConfigs() {
    return Arrays.asList(this.handlerTable.values().toArray(
        new OFSNHandlerConfig[this.handlerTable.size()]));
  }

  public OFSNHandlerConfig getHandlerConfig(String handlerName) {
    return this.handlerTable.get(handlerName);
  }

  /**
   * @return the productRoot
   */
  public String getProductRoot() {
    return productRoot;
  }

  /**
   * @param productRoot
   *          the productRoot to set
   */
  public void setProductRoot(String productRoot) {
    this.productRoot = productRoot;
    cleanse(this.productRoot);
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    this.id = id;
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

  private void cleanse(String path) {
    if (path != null && !path.endsWith("/")) {
      path += "/";
    }
  }
}
