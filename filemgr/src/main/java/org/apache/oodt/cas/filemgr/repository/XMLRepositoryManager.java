/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.cas.filemgr.repository;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.util.XmlStructFactory;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A {@link RepositoryManager} that manages {@link Product} policy based on an
 * xml file called <code>product-types.xml</code>.
 * </p>
 * </p>
 * 
 */
public class XMLRepositoryManager implements RepositoryManager {

    /* URIs pointing to directories containing product-types.xml files */
    private List<String> productTypeHomeUris = null;

    /* our map of product types that the system knows about */
    private ConcurrentHashMap<String, ProductType> productTypeMap = new ConcurrentHashMap<String, ProductType>();

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(XMLRepositoryManager.class
            .getName());

    /**
     * 
     */
    public XMLRepositoryManager(List<String> uris) throws InstantiationException {
        if (uris == null) {
            throw new InstantiationException(
                    "Attempt to construct XMLRepositoryManager with a NULL list of  product type home URIs!");
        }

        this.productTypeHomeUris = uris;
        loadProductTypes(productTypeHomeUris);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.repository.RepositoryManager#addProductType(org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    public void addProductType(ProductType productType)
            throws RepositoryManagerException {
        productTypeMap.put(productType.getProductTypeId(), productType);
        saveProductTypes();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.repository.RepositoryManager#modifyProductType(org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    public void modifyProductType(ProductType productType)
            throws RepositoryManagerException {
        productTypeMap.put(productType.getProductTypeId(), productType);
        saveProductTypes();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.repository.RepositoryManager#removeProductType(org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    public void removeProductType(ProductType productType)
            throws RepositoryManagerException {
        productTypeMap.remove(productType.getProductTypeId());
        saveProductTypes();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.repository.RepositoryManager#getProductTypeById(java.lang.String)
     */
    public ProductType getProductTypeById(String productTypeId)
            throws RepositoryManagerException {
        return (ProductType) productTypeMap.get(productTypeId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.repository.RepositoryManager#getProductTypeByName(java.lang.String)
     */
    public ProductType getProductTypeByName(String productTypeName)
            throws RepositoryManagerException {
      for (Map.Entry<String, ProductType> typeId : productTypeMap.entrySet()) {
        ProductType type = typeId.getValue();
        if (type.getName().equals(productTypeName)) {
          return type;
        }
      }

        LOG.log(Level.WARNING,
                "XMLRepositoryManager: Unable to find product type: ["
                        + productTypeName + "], returning null");

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.repository.RepositoryManager#getProductTypes()
     */
    public List<ProductType> getProductTypes() throws RepositoryManagerException {
        return Arrays.asList(productTypeMap.values().toArray(new ProductType[productTypeMap.values().size()]));
    }

    private void saveProductTypes() {
      for (String dirUri : productTypeHomeUris) {
        File productTypeDir;

        try {
          productTypeDir = new File(new URI(dirUri));

          if (!productTypeDir.isDirectory()) {
            LOG
                .log(
                    Level.WARNING,
                    "Product type directory: "
                    + dirUri
                    + " is not "
                    + "a directory: skipping product type saving to it.");
            continue;
          }

          String productTypeDirStr = productTypeDir.getAbsolutePath();
          if (!productTypeDirStr.endsWith("/")) {
            productTypeDirStr += "/";
          }

          String productTypeXmlFile = productTypeDirStr
                                      + "product-types.xml";
          XmlStructFactory.writeProductTypeXmlDocument(Arrays
                  .asList(productTypeMap.values().toArray(new ProductType[productTypeMap.values().size()])),
              productTypeXmlFile);
        } catch (URISyntaxException e) {
          LOG.log(Level.WARNING,
              "URISyntaxException when saving product "
              + "type directory URI: " + dirUri
              + ": Skipping Product Type saving"
              + "for it: Message: " + e.getMessage());
        }

      }

    }

    private void loadProductTypes(List<String> dirUris) {
      for (String dirUri1 : dirUris) {
        File productTypeDir = null;
        String dirUri = dirUri1;

        try {
          productTypeDir = new File(new URI(dirUri));

          if (!productTypeDir.isDirectory()) {
            LOG
                .log(
                    Level.WARNING,
                    "Product type directory: "
                    + dirUri
                    + " is not "
                    + "a directory: skipping product type loading from it.");
            continue;
          }

          String productTypeDirStr = productTypeDir.getAbsolutePath();
          if (!productTypeDirStr.endsWith("/")) {
            productTypeDirStr += "/";
          }

          String productTypeXmlFile = productTypeDirStr
                                      + "product-types.xml";
          Document productTypeDoc = getDocumentRoot(productTypeXmlFile);

          // now load the product types from it
          if (productTypeDoc != null) {
            Element productTypeRoot = productTypeDoc
                .getDocumentElement();

            NodeList productTypeNodeList = productTypeRoot
                .getElementsByTagName("type");

            if (productTypeNodeList != null
                && productTypeNodeList.getLength() > 0) {
              for (int j = 0; j < productTypeNodeList.getLength(); j++) {
                Node productTypeNode = productTypeNodeList.item(j);
                ProductType type = XmlStructFactory
                    .getProductType(productTypeNode);
                LOG.log(Level.FINE,
                    "XMLRepositoryManager: found product type: ["
                    + type.getName() + "]");
                productTypeMap.put(type.getProductTypeId(), type);
              }
            }
          }
        } catch (URISyntaxException e) {
          LOG.log(Level.WARNING,
              "URISyntaxException when loading product "
              + "type directory URI: " + dirUri
              + ": Skipping Product Type loading"
              + "for it: Message: " + e.getMessage());
        }
      }
    }

    private Document getDocumentRoot(String xmlFile) {
        // open up the XML file
        DocumentBuilderFactory factory;
        DocumentBuilder parser;
        Document document;
        InputSource inputSource;

        InputStream xmlInputStream;

        try {
            xmlInputStream = new File(xmlFile).toURI().toURL().openStream();
        } catch (IOException e) {
            LOG.log(Level.WARNING,
                    "IOException when getting input stream from [" + xmlFile
                            + "]: returning null document root");
            return null;
        }

        inputSource = new InputSource(xmlInputStream);

        try {
            factory = DocumentBuilderFactory.newInstance();
            parser = factory.newDocumentBuilder();
            document = parser.parse(inputSource);
        } catch (Exception e) {
            LOG.warning("Unable to parse xml file [" + xmlFile + "]."
                    + "Reason is [" + e + "]");
            return null;
        }

        return document;
    }

}
