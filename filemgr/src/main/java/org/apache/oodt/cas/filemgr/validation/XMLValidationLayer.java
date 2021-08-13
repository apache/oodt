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

package org.apache.oodt.cas.filemgr.validation;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.util.XmlStructFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//JDK imports

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * An XML ValidationLayer based on two xml files, <code>elements.xml</code>,
 * and <code>product-type-element-map.xml</code>.
 * </p>
 * 
 */
public class XMLValidationLayer implements ValidationLayer {

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(XMLValidationLayer.class
            .getName());

    /* product type ID to element map */
    private ConcurrentHashMap<String, List<Element>> productTypeElementMap = new ConcurrentHashMap<String, List<Element>>();

    /* sub-type to super-type map */
    private ConcurrentHashMap<String, String> subToSuperMap = new ConcurrentHashMap<String, String>();

    /* element map */
    private ConcurrentHashMap<String, Element> elementMap = new ConcurrentHashMap<String, Element>();

    /*
     * URIs pointing to directories with product-type-element-map.xml and
     * elements.xml files
     */
    private List<String> xmlFileDirUris = null;

    /**
     * 
     */
    public XMLValidationLayer(List<String> uris) {
        this.xmlFileDirUris = uris;
        loadElements(xmlFileDirUris);
        loadProductTypeMap(xmlFileDirUris);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#addElement(org.apache.oodt.cas.filemgr.structs.Element)
     */
    public void addElement(Element element) throws ValidationLayerException {
        elementMap.put(element.getElementId(), element);
        saveElementsAndMappings();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#modifyElement(org.apache.oodt.cas.filemgr.structs.Element)
     */
    public void modifyElement(Element element) throws ValidationLayerException {
        for(Element elem: elementMap.values()){
           if(elem.getElementId().equals(element.getElementId())){
             elem.setElementName(element.getElementName());
             elem.setDescription(elem.getDescription());
             elem.setDCElement(element.getDCElement());
           }
        }
        saveElementsAndMappings();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#removeElement(org.apache.oodt.cas.filemgr.structs.Element)
     */
    public void removeElement(Element element) throws ValidationLayerException {
        elementMap.remove(element.getElementId());
        saveElementsAndMappings();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#addElementToProductType(org.apache.oodt.cas.filemgr.structs.ProductType,
     *      org.apache.oodt.cas.filemgr.structs.Element)
     */
    public void addElementToProductType(ProductType type, Element element)
            throws ValidationLayerException {
        List<Element> elements = productTypeElementMap.get(type
                .getProductTypeId());

        if (elements == null) {
            elements = new Vector<Element>();
            productTypeElementMap.put(type.getProductTypeId(), elements);
        }
        elements.add(element);
        saveElementsAndMappings();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#removeElementFromProductType(org.apache.oodt.cas.filemgr.structs.ProductType,
     *      org.apache.oodt.cas.filemgr.structs.Element)
     */
    public void removeElementFromProductType(ProductType type, Element element)
            throws ValidationLayerException {
        List<Element> elements = productTypeElementMap.get(type
                .getProductTypeId());

      for (Element elementObj : elements) {
        if (elementObj.getElementId().equals(element.getElementId())) {
          elements.remove(elementObj);
          saveElementsAndMappings();
          break;
        }
      }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#getElements(org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    public List<Element> getElements(ProductType type)
            throws ValidationLayerException {
    	return this.getElements(type, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#getElements()
     */
    public List<Element> getElements() throws ValidationLayerException {
        return Arrays.asList(elementMap.values().toArray(
                new Element[elementMap.size()]));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#getElementById(java.lang.String)
     */
    public Element getElementById(String elementId)
            throws ValidationLayerException {
        return elementMap.get(elementId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#getElementByName(java.lang.String)
     */
    public Element getElementByName(String elementName)
            throws ValidationLayerException {
      for (Map.Entry<String, Element> elementId : elementMap.entrySet()) {
        Element element = elementId.getValue();
        if (element.getElementName().equals(elementName)) {
          return element;
        }
      }

        return null;

    }
    
    /**
     * Returns the declared elements for a {@link ProductType}
     * @param type The {@link ProductType} to get the elements for
     * @param direct If false, return elements of the parent product types as well
     * @return A list of all {@link Element} of the Product type
     * @throws ValidationLayerException
     * 				If any error occurs
     */
    public List<Element> getElements(ProductType type, boolean direct) {
        List<Element> elems = new Vector<Element>();
        String currType = type.getProductTypeId();
        if (productTypeElementMap.containsKey(currType)) {
            elems.addAll(productTypeElementMap.get(currType));
        }

        if(!direct) {
	        while (subToSuperMap.containsKey(currType)) {
	            currType = subToSuperMap.get(currType);
	            if (productTypeElementMap.containsKey(currType)) {
	                elems.addAll(productTypeElementMap.get(currType));
	            }
	        }
        }

        return elems;
    }
    
    /**
     * Gets the parent-child relationship between product types
     * 
     * @return ConcurrentHashMap of {@link ProductType} ids mapped to their parent id
     */
    public ConcurrentHashMap<String, String> getSubToSuperMap() {
    	return subToSuperMap;
    }
    
    /**
     * Sets a parentId for an existing {@link ProductType}
     * @param type The {@link ProductType} to add a parent for
     * @param parentId The id of the parent {@link ProductType}
     * @throws ValidationLayerException
     * 				If any error occurs
     */
    public void addParentForProductType(ProductType type, String parentId) {
        subToSuperMap.put(type.getProductTypeId(), parentId);
        saveElementsAndMappings();
    }

    /**
     * Removes the parent for a {@link ProductType}
     * @param type The {@link ProductType} to remove the parent from
     * @throws ValidationLayerException
     * 				If any error occurs
     */
    public void removeParentForProductType(ProductType type) {
        subToSuperMap.remove(type.getProductTypeId());
        saveElementsAndMappings();
    }
    

    private void saveElementsAndMappings() {
      for (String dirUri : xmlFileDirUris) {
        File elementDir;

        try {
          elementDir = new File(new URI(dirUri));

          if (!elementDir.isDirectory()) {
            LOG
                .log(
                    Level.WARNING,
                    "Element directory: "
                    + dirUri
                    + " is not "
                    + "a directory: skipping element and product type map saving to it.");
            continue;
          }

          String elementDirStr = elementDir.getAbsolutePath();
          if (!elementDirStr.endsWith("/")) {
            elementDirStr += "/";
          }

          String elementXmlFile = elementDirStr + "elements.xml";

          String productTypeMapXmlFile = elementDirStr
                                         + "product-type-element-map.xml";

          XmlStructFactory.writeElementXmlDocument(Arrays
                  .asList(elementMap.values().toArray(
                      new Element[elementMap.size()])),
              elementXmlFile);

          XmlStructFactory.writeProductTypeMapXmLDocument(
              productTypeElementMap, subToSuperMap,
              productTypeMapXmlFile);

        } catch (URISyntaxException e) {
          LOG
              .log(
                  Level.WARNING,
                  "URISyntaxException when saving element "
                  + "directory URI: "
                  + dirUri
                  + ": Skipping Element and Product Type map saving"
                  + "for it: Message: " + e.getMessage());
        }

      }

    }

    private void loadElements(List<String> dirUris) {
      for (String dirUri1 : dirUris) {
        File elementDir = null;
        String dirUri = dirUri1;

        try {
          elementDir = new File(new URI(dirUri));

          if (!elementDir.isDirectory()) {
            LOG.log(Level.WARNING, "Element directory: " + dirUri
                                   + " is not "
                                   + "a directory: skipping element loading from it.");
            continue;
          }

          String elementDirStr = elementDir.getAbsolutePath();
          if (!elementDirStr.endsWith("/")) {
            elementDirStr += "/";
          }

          String elementXmlFile = elementDirStr + "elements.xml";
          Document elementDoc = getDocumentRoot(elementXmlFile);

          org.w3c.dom.Element elementRootElem = elementDoc
              .getDocumentElement();

          NodeList elementNodeList = elementRootElem
              .getElementsByTagName("element");

          if (elementNodeList != null && elementNodeList.getLength() > 0) {
            for (int j = 0; j < elementNodeList.getLength(); j++) {
              Node elementNode = elementNodeList.item(j);
              Element element = XmlStructFactory
                  .getElement(elementNode);
              elementMap.put(element.getElementId(), element);
            }
          }

        } catch (URISyntaxException e) {
          LOG.log(Level.WARNING,
              "URISyntaxException when loading element "
              + "directory URI: " + dirUri
              + ": Skipping element loading"
              + "for it: Message: " + e.getMessage());
        }
      }
    }

    private void loadProductTypeMap(List<String> dirUris) {
      for (String dirUri1 : dirUris) {
        File elementDir = null;
        String dirUri = dirUri1;

        try {
          elementDir = new File(new URI(dirUri));

          if (!elementDir.isDirectory()) {
            LOG
                .log(
                    Level.WARNING,
                    "Element directory: "
                    + dirUri
                    + " is not "
                    + "a directory: skipping product type element map loading from it.");
            continue;
          }

          String elementDirStr = elementDir.getAbsolutePath();
          if (!elementDirStr.endsWith("/")) {
            elementDirStr += "/";
          }

          String productTypeMapXmlFile = elementDirStr
                                         + "product-type-element-map.xml";
          Document productTypeMapDoc = getDocumentRoot(productTypeMapXmlFile);

          org.w3c.dom.Element mapRootElem = productTypeMapDoc
              .getDocumentElement();

          NodeList typeNodeList = mapRootElem
              .getElementsByTagName("type");

          if (typeNodeList != null && typeNodeList.getLength() > 0) {
            for (int j = 0; j < typeNodeList.getLength(); j++) {
              org.w3c.dom.Element typeElement = (org.w3c.dom.Element) typeNodeList
                  .item(j);
              String typeId = typeElement.getAttribute("id");

              // get inheritance info
              String typeParent = typeElement.getAttribute("parent");
              if (typeParent != null) {
                subToSuperMap.put(typeId, typeParent);
              }

              // get its element list
              NodeList elementIdNodeList = typeElement
                  .getElementsByTagName("element");

              // allow for 0 sized element list
              List<Element> productTypeElementList = new Vector<Element>();

              if (elementIdNodeList != null
                  && elementIdNodeList.getLength() > 0) {
                productTypeElementList = new Vector<Element>(
                    elementIdNodeList.getLength());
                for (int k = 0; k < elementIdNodeList.getLength(); k++) {
                  org.w3c.dom.Element elementIdElement = (org.w3c.dom.Element) elementIdNodeList
                      .item(k);
                  String elementId = elementIdElement
                      .getAttribute("id");

                  if (elementMap.get(elementId) != null) {
                    productTypeElementList.add(elementMap
                        .get(elementId));
                  }
                }
              }

              productTypeElementMap.put(typeId,
                  productTypeElementList);
            }
          }

        } catch (URISyntaxException e) {
          LOG.log(Level.WARNING,
              "URISyntaxException when loading element "
              + "directory URI: " + dirUri
              + ": Skipping product type map loading"
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
