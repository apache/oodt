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

package org.apache.oodt.cas.filemgr.util;

//JDK imports

import org.apache.oodt.cas.filemgr.structs.ExtractorSpec;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.type.TypeHandler;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.xml.XMLUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//OODT imports

/**
 * @author mattmann
 * @author bfoster
 * @author riverma
 * @version $Revision$
 * 
 * <p>
 * A Generic class for constructing File Manager objects out of XML {@link Node}s.
 * </p>
 * 
 */
@Deprecated
public final class XmlStructFactory {

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(XmlStructFactory.class
            .getName());

    private XmlStructFactory() throws InstantiationException {
        throw new InstantiationException("Don't construct XmlStructFactories!");
    }

    public static ProductType getProductType(Node productTypeNode) {
        org.w3c.dom.Element productTypeElem = (org.w3c.dom.Element) productTypeNode;

        String id = productTypeElem.getAttribute("id");
        String name = productTypeElem.getAttribute("name");

        org.w3c.dom.Element repositoryElem = XMLUtils.getFirstElement(
                "repository", productTypeElem);

        String repositoryPath = repositoryElem.getAttribute("path");

        org.w3c.dom.Element versionerElem = XMLUtils.getFirstElement(
                "versioner", productTypeElem);

        String versionerClass = versionerElem.getAttribute("class");

        org.w3c.dom.Element descElem = XMLUtils.getFirstElement("description",
                productTypeElem);

        String description;
        if (descElem.getAttribute("trim") != null
                && !descElem.getAttribute("trim").equals("")
                && !Boolean.valueOf(descElem.getAttribute("trim"))) {
            description = XMLUtils.getElementText("description",
                    productTypeElem);
        } else {
            description = XMLUtils.getElementText("description",
                    productTypeElem, true);
        }

        repositoryPath = PathUtils.replaceEnvVariables(repositoryPath);

        // grab metadata
        Metadata met = new Metadata();
        Element metadataRoot = XMLUtils.getFirstElement("metadata",
                productTypeElem);
        if (metadataRoot != null) {
            ConcurrentHashMap<String, Object> metHash = new ConcurrentHashMap<String, Object>();
            NodeList keyValElems = metadataRoot.getElementsByTagName("keyval");

            for (int i = 0; i < keyValElems.getLength(); i++) {
                Element keyValElem = (Element) keyValElems.item(i);

                String elemName = XMLUtils.read(keyValElem, "key");
                @SuppressWarnings("unchecked")
                List<String> elemValues = XMLUtils.readMany(keyValElem, "val");
                metHash.put(elemName, elemValues);
            }

            met.replaceMetadata(metHash);
        } else {
        	LOG.warning("metadata node missing for product type : "+id);
        }

        // grab extractors
        List<ExtractorSpec> extractors = new Vector<ExtractorSpec>();
        Element extractorRoot = XMLUtils.getFirstElement("metExtractors",
                productTypeElem);

        if (extractorRoot != null) {
            NodeList extractorNodes = extractorRoot
                    .getElementsByTagName("extractor");
            if (extractorNodes != null && extractorNodes.getLength() > 0) {
                for (int i = 0; i < extractorNodes.getLength(); i++) {
                    Element extractorElem = (Element) extractorNodes.item(i);
                    ExtractorSpec spec = new ExtractorSpec();
                    String className = extractorElem.getAttribute("class");
                    spec.setClassName(className);

                    // see if there are any configuration properties
                    Element configuration = XMLUtils.getFirstElement(
                            "configuration", extractorElem);

                    if (configuration != null) {
                        Properties config = new Properties();
                        NodeList propertyNodes = configuration
                                .getElementsByTagName("property");

                        if (propertyNodes != null
                                && propertyNodes.getLength() > 0) {
                            for (int j = 0; j < propertyNodes.getLength(); j++) {
                                Element propertyElem = (Element) propertyNodes
                                        .item(j);
                                String propertyName = propertyElem
                                        .getAttribute("name");
                                String propertyValue = propertyElem
                                        .getAttribute("value");
                                if (Boolean
                                    .valueOf(
                                        propertyElem
                                            .getAttribute("envReplace"))) {
                                    propertyValue = PathUtils
                                            .replaceEnvVariables(propertyValue);
                                }

                                config.setProperty(propertyName, propertyValue);
                            }

                            spec.setConfiguration(config);
                        }
                    }

                    extractors.add(spec);
                }

            }
        } else {
        	LOG.warning("metExtractors node missing from product type : "+id);
        }
        
        List<TypeHandler> handlers = null;
        Element handlerRoot = XMLUtils.getFirstElement("typeHandlers",
                productTypeElem);
        if (handlerRoot != null) {
            NodeList handlerNodes = handlerRoot.getElementsByTagName("typeHandler");
            if (handlerNodes != null && handlerNodes.getLength() > 0) {
                handlers = new Vector<TypeHandler>();
                for (int i = 0; i < handlerNodes.getLength(); i++) {
                    Node handlerNode = handlerNodes.item(i);
                    String handlerClass = ((Element) handlerNode).getAttribute("class");
                    String elementName = ((Element) handlerNode).getAttribute("elementName");
                    try {
                        TypeHandler typeHandler = (TypeHandler) Class.forName(handlerClass).newInstance();
                        typeHandler.setElementName(elementName);
                        handlers.add(typeHandler);
                    }catch (Exception e) {
                        LOG.log(Level.SEVERE, e.getMessage());
                        LOG.log(Level.WARNING, "Failed to load handler for ProductType [name = " + name 
                                + "] and element [name = " + elementName + "] : " + e.getMessage());
                    }
                }
            }
        }
        
        ProductType productType = new ProductType();
        productType.setName(name);
        productType.setProductTypeId(id);
        productType.setProductRepositoryPath(repositoryPath);
        productType.setVersioner(versionerClass);
        productType.setDescription(description);
        productType.setTypeMetadata(met);
        productType.setExtractors(extractors);
        productType.setHandlers(handlers);

        return productType;
    }

    public static void writeProductTypeMapXmLDocument(ConcurrentHashMap<String, List<org.apache.oodt.cas.filemgr.structs.Element>> productTypeMap,
            ConcurrentHashMap<String, String> subToSuperMap, String xmlFilePath) {
        XMLUtils.writeXmlFile(getProductTypeMapXmlDocument(productTypeMap,
                subToSuperMap), xmlFilePath);
    }

    public static void writeElementXmlDocument(List<org.apache.oodt.cas.filemgr.structs.Element> elements, String xmlFilePath) {
        XMLUtils.writeXmlFile(getElementXmlDocument(elements), xmlFilePath);
    }

    public static void writeProductTypeXmlDocument(List<ProductType> productTypes,
            String xmlFilePath) {
        XMLUtils.writeXmlFile(getProductTypeXmlDocument(productTypes),
                xmlFilePath);
    }

    public static Document getProductTypeMapXmlDocument(ConcurrentHashMap<String, List<org.apache.oodt.cas.filemgr.structs.Element>> productTypeMap,
            ConcurrentHashMap<String, String> subToSuperMap) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();

            Element root = (Element) document
                    .createElement("cas:producttypemap");
            root.setAttribute("xmlns:cas", "http://oodt.jpl.nasa.gov/1.0/cas");
            document.appendChild(root);

            // Also print types without elements but just with parents
            ArrayList<String> allTypes = new ArrayList<String>(productTypeMap.keySet());
            for(String type: subToSuperMap.keySet()) {
                if(!allTypes.contains(type)) {
                    allTypes.add(type);
                }
            }

            for (String typeId : allTypes) {
                Element typeElem = document.createElement("type");
                typeElem.setAttribute("id", typeId);

                boolean hasParent = false;
                if (subToSuperMap.containsKey(typeId)) {
                    typeElem.setAttribute("parent", subToSuperMap
                        .get(typeId));
                    hasParent = true;
                }

                List<org.apache.oodt.cas.filemgr.structs.Element> elementIds = productTypeMap.get(typeId);
                if (!hasParent && (elementIds == null || elementIds.size() == 0)) {
                    // If no parent, and no elements, don't add this type to the xml
                    continue;
                }
                if (elementIds != null) {
                    for (org.apache.oodt.cas.filemgr.structs.Element elementId1 : elementIds) {
                        String elementId = elementId1.getElementId();

                        Element elementElem = document.createElement("element");
                        elementElem.setAttribute("id", elementId);
                        typeElem.appendChild(elementElem);
                    }
                }

                root.appendChild(typeElem);
            }

            return document;

        } catch (ParserConfigurationException pce) {
            LOG.log(Level.WARNING,
                    "Error generating product-type-element-map xml file!: "
                            + pce.getMessage());
        }

        return null;
    }

    public static Document getElementXmlDocument(List<org.apache.oodt.cas.filemgr.structs.Element> elements) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();

            Element root = (Element) document.createElement("cas:elements");
            root.setAttribute("xmlns:cas", "http://oodt.jpl.nasa.gov/1.0/cas");
            document.appendChild(root);

            for (org.apache.oodt.cas.filemgr.structs.Element element : elements) {
                Element elementElem = document.createElement("element");
                elementElem.setAttribute("id", friendlyXml(element.getElementId()));
                elementElem.setAttribute("name", friendlyXml(element.getElementName()));

                Element descriptionElem = document.createElement("description");
                descriptionElem.appendChild(document.createTextNode(friendlyXml(element
                    .getDescription())));
                elementElem.appendChild(descriptionElem);

                Element dcElementElem = document.createElement("dcElement");
                dcElementElem.appendChild(document.createTextNode(friendlyXml(element
                    .getDCElement())));
                elementElem.appendChild(dcElementElem);

                root.appendChild(elementElem);
            }

            return document;

        } catch (ParserConfigurationException pce) {
            LOG.log(Level.WARNING, "Error generating elements xml file!: "
                    + pce.getMessage());
        }

        return null;
    }

    public static Document getProductTypeXmlDocument(List<ProductType> productTypes) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();

            Element root = (Element) document.createElement("cas:producttypes");
            root.setAttribute("xmlns:cas", "http://oodt.jpl.nasa.gov/1.0/cas");
            document.appendChild(root);

            // now add the set of metadata elements in the properties object
            for (ProductType type : productTypes) {
                Element typeElem = document.createElement("type");
                typeElem.setAttribute("id", type.getProductTypeId());
                typeElem.setAttribute("name", type.getName());

                Element descriptionElem = document.createElement("description");
                descriptionElem.appendChild(document.createTextNode(type
                    .getDescription()));
                typeElem.appendChild(descriptionElem);

                Element repositoryPathElem = document
                    .createElement("repository");
                repositoryPathElem.setAttribute("path", type
                    .getProductRepositoryPath());
                typeElem.appendChild(repositoryPathElem);

                Element versionerClassPathElem = document
                    .createElement("versioner");
                versionerClassPathElem.setAttribute("class", type
                    .getVersioner());
                typeElem.appendChild(versionerClassPathElem);

                // add extractor info
                Element metExtractorsElem = document.createElement("metExtractors");
                for (Object specObject : type.getExtractors()) {
                    ExtractorSpec spec = (ExtractorSpec) specObject;
                    Element extractorElem = document.createElement("extractor");
                    extractorElem.setAttribute("class", spec.getClassName());

                    if (spec.getConfiguration() != null) {
                        Element extractorConfigElem = document.createElement("configuration");
                        Enumeration e = spec.getConfiguration().propertyNames();

                        while (e.hasMoreElements()) {
                            String key = (String) e.nextElement();

                            Element propertyElem = document.createElement("property");
                            propertyElem.setAttribute("name", key);
                            propertyElem.setAttribute("value", spec.getConfiguration().getProperty(key));

                            extractorConfigElem.appendChild(propertyElem);
                        }

                        extractorElem.appendChild(extractorConfigElem);
                    }

                    metExtractorsElem.appendChild(extractorElem);
                }
                typeElem.appendChild(metExtractorsElem);

                // add type metadata
                Element metElem = document.createElement("metadata");
                Metadata typeMetadata = type.getTypeMetadata();
                
                // loop over all type metadata keys
                for (String key : typeMetadata.getAllKeys()) {
                    Element keyValElem = document.createElement("keyval");
                    Element keyElem = document.createElement("key");
                    keyElem.appendChild(document.createTextNode(key));
                    keyValElem.appendChild(keyElem);
                    
                    // loop over all metadata values for that key
                    for (String value : typeMetadata.getAllMetadata(key)) {
	                    Element valElem = document.createElement("val");         
	                    valElem.appendChild(document.createTextNode(value));
	                    keyValElem.appendChild(valElem);
                    }

                    metElem.appendChild(keyValElem);
                }
                
                typeElem.appendChild(metElem);
                root.appendChild(typeElem);
            }

            return document;
        } catch (ParserConfigurationException pce) {
            LOG.log(Level.WARNING, "Error generating producttypes xml file!: "
                    + pce.getMessage());
        }

        return null;

    }

    public static org.apache.oodt.cas.filemgr.structs.Element getElement(
            Node elementNode) {
        org.w3c.dom.Element elementElem = (org.w3c.dom.Element) elementNode;

        String id = elementElem.getAttribute("id");
        String name = elementElem.getAttribute("name");

        String dcElement = XMLUtils.getElementText("dcElement", elementElem);

        org.w3c.dom.Element descElem = XMLUtils.getFirstElement("description",
                elementElem);
        String description;
        if (descElem.getAttribute("trim") != null
                && !descElem.getAttribute("trim").equals("")
                && !Boolean.valueOf(descElem.getAttribute("trim"))) {
            description = XMLUtils.getElementText("description", elementElem);
        } else {
            description = XMLUtils.getElementText("description", elementElem,
                    true);
        }

        org.apache.oodt.cas.filemgr.structs.Element element = new org.apache.oodt.cas.filemgr.structs.Element();
        element.setDCElement(dcElement);
        element.setDescription(description);
        element.setElementId(id);
        element.setElementName(name);
        return element;
    }

    public static ConcurrentHashMap<String, List<org.apache.oodt.cas.filemgr.structs.Element>>
            getProductTypeElementList(Node typeMapNode, ConcurrentHashMap<String, org.apache.oodt.cas.filemgr.structs.Element> elements) {
        org.w3c.dom.Element typeMapElement = (org.w3c.dom.Element) typeMapNode;

        String typeId = typeMapElement.getAttribute("id");

        ConcurrentHashMap<String, List<org.apache.oodt.cas.filemgr.structs.Element>> productTypeElementMap = new ConcurrentHashMap<String, List<org.apache.oodt.cas.filemgr.structs.Element>>();

        org.w3c.dom.Element elementListRoot = XMLUtils.getFirstElement(
                "elements", typeMapElement);
        NodeList elementNodeList = elementListRoot
                .getElementsByTagName("element");

        List<org.apache.oodt.cas.filemgr.structs.Element> elementList = new Vector<org.apache.oodt.cas.filemgr.structs.Element>(elementNodeList.getLength());

        for (int i = 0; i < elementNodeList.getLength(); i++) {
            org.w3c.dom.Element elementElem = (org.w3c.dom.Element) elementNodeList
                    .item(i);
            String elementId = elementElem.getAttribute("id");
            org.apache.oodt.cas.filemgr.structs.Element element = elements
                    .get(elementId);
            elementList.add(element);
        }

        productTypeElementMap.put(typeId, elementList);
        return productTypeElementMap;
    }
    
    private static String friendlyXml(String value){
      return value != null ? value:"";
    }

}
