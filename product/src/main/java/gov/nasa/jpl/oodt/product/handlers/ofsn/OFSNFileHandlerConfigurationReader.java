//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.product.handlers.ofsn;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//OODT imports
import gov.nasa.jpl.oodt.cas.commons.xml.XMLUtils;
import gov.nasa.jpl.oodt.product.handlers.ofsn.metadata.OFSNXMLConfigMetKeys;

/**
 * 
 * Reads an XML file representation of the {@link OFSNFileHandlerConfiguration}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public final class OFSNFileHandlerConfigurationReader implements
    OFSNXMLConfigMetKeys {

  public static OFSNFileHandlerConfiguration getConfig(String filePath)
      throws FileNotFoundException {
    OFSNFileHandlerConfiguration config = new OFSNFileHandlerConfiguration();

    Document configDoc = XMLUtils.getDocumentRoot(new FileInputStream(new File(
        filePath)));
    Element configElem = configDoc.getDocumentElement();
    config.setId(configElem.getAttribute(OFSN_CFG_ID_ATTR));
    config.setName(configElem.getAttribute(OFSN_CFG_NAME_ATTR));
    config.setProductRoot(configElem.getAttribute(OFSN_PRODUCT_ROOT_ATTR));
    addHandlers(configElem, config);
    return config;
  }

  private static void addHandlers(Element configRootElem,
      OFSNFileHandlerConfiguration config) {
    NodeList handlerNodes = configRootElem.getElementsByTagName(HANDLER_TAG);
    for (int i = 0; i < handlerNodes.getLength(); i++) {
      OFSNHandlerConfig cfg = getHandlerConfig((Element) handlerNodes.item(i));
      config.handlerTable.put(cfg.getName(), cfg);
    }
  }

  private static OFSNHandlerConfig getHandlerConfig(Element handlerNodeElem) {
    OFSNHandlerConfig cfg = new OFSNHandlerConfig();
    cfg.setClassName(handlerNodeElem.getAttribute(HANDLER_CLASSNAME_ATTR));
    cfg.setName(handlerNodeElem.getAttribute(HANDLER_NAME_ATTR));
    cfg.setType(handlerNodeElem.getAttribute(HANDLER_TYPE_ATTR));
    cfg.setHandlerConf(readConfig(handlerNodeElem));
    return cfg;
  }

  private static Properties readConfig(Element handlerNodeElem) {
    Properties config = new Properties();
    NodeList propertyNodes = handlerNodeElem.getElementsByTagName(PROPERTY_TAG);

    if (propertyNodes != null && propertyNodes.getLength() > 0) {
      for (int j = 0; j < propertyNodes.getLength(); j++) {
        Element propertyElem = (Element) propertyNodes.item(j);
        String propertyName = propertyElem.getAttribute(PROPERTY_NAME_ATTR);
        String propertyValue = propertyElem.getAttribute(PROPERTY_VALUE_ATTR);

        config.setProperty(propertyName, propertyValue);
      }
    }

    return config;
  }
}
