//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.product.handlers.ofsn.metadata;

// OODT imports
import gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNFileHandlerConfigurationReader; //javadoc

/**
 * 
 * Met Keys for the {@link OFSNFileHandlerConfigurationReader}
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface OFSNXMLConfigMetKeys {

  public static final String OFSN_CFG_ID_ATTR = "id";

  public static final String OFSN_CFG_NAME_ATTR = "name";

  public static final String OFSN_PRODUCT_ROOT_ATTR = "productRoot";

  public static final String HANDLER_TAG = "handler";

  public static final String HANDLER_CLASSNAME_ATTR = "class";

  public static final String HANDLER_NAME_ATTR = "name";

  public static final String HANDLER_TYPE_ATTR = "type";

  public static final String PROPERTY_TAG = "property";

  public static final String PROPERTY_NAME_ATTR = "name";

  public static final String PROPERTY_VALUE_ATTR = "value";

}
