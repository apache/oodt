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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.ExtractorSpec;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.type.TypeHandler;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.xml.XMLUtils;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A Generic class for constructing File Manager objects out of XML {@link Node}s.
 * </p>
 * 
 */
public final class XmlStructFactory {

    /* our log stream */
    private static Logger LOG = Logger.getLogger(XmlStructFactory.class
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

        String description = null;
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
        Metadata met = null;
        Element metadataRoot = XMLUtils.getFirstElement("metadata",
                productTypeElem);
        if (metadataRoot != null) {
            Hashtable<String, Object> metHash = new Hashtable<String, Object>();
            met = new Metadata();
            NodeList keyValElems = metadataRoot.getElementsByTagName("keyval");

            for (int i = 0; i < keyValElems.getLength(); i++) {
                Element keyValElem = (Element) keyValElems.item(i);

                String elemName = XMLUtils.read(keyValElem, "key");
                List<String> elemValues = XMLUtils.readMany(keyValElem, "val");
                metHash.put(elemName, elemValues);
            }

            met.replaceMetadata(metHash);
        }

        // grab extractors
        List<ExtractorSpec> extractors = null;
        Element extractorRoot = XMLUtils.getFirstElement("metExtractors",
                productTypeElem);

        if (extractorRoot != null) {
            NodeList extractorNodes = extractorRoot
                    .getElementsByTagName("extractor");

            if (extractorNodes != null && extractorNodes.getLength() > 0) {
                extractors = new Vector<ExtractorSpec>();
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
                                                        .getAttribute("envReplace"))
                                        .booleanValue()) {
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
                        e.printStackTrace();
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

    public static void writeProductTypeMapXmLDocument(HashMap<String, List<org.apache.oodt.cas.filemgr.structs.Element>> productTypeMap,
            HashMap<String, String> subToSuperMap, String xmlFilePath) {
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

    public static Document getProductTypeMapXmlDocument(HashMap<String, List<org.apache.oodt.cas.filemgr.structs.Element>> productTypeMap,
            HashMap<String, String> subToSuperMap) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document = null;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();

            Element root = (Element) document
                    .createElement("cas:producttypemap");
            root.setAttribute("xmlns:cas", "http://oodt.jpl.nasa.gov/1.0/cas");
            document.appendChild(root);

            for (Iterator<String> i = productTypeMap.keySet().iterator(); i.hasNext();) {
                String typeId = i.next();

                Element typeElem = document.createElement("type");
                typeElem.setAttribute("id", typeId);

                if (subToSuperMap.containsKey(typeId)) {
                    typeElem.setAttribute("parent", subToSuperMap
                            .get(typeId));
                }

                List<org.apache.oodt.cas.filemgr.structs.Element> elementIds = productTypeMap.get(typeId);

                for (Iterator<org.apache.oodt.cas.filemgr.structs.Element> j = elementIds.iterator(); j.hasNext();) {
                    String elementId = j.next().getElementId();

                    Element elementElem = document.createElement("element");
                    elementElem.setAttribute("id", elementId);
                    typeElem.appendChild(elementElem);
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
        Document document = null;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();

            Element root = (Element) document.createElement("cas:elements");
            root.setAttribute("xmlns:cas", "http://oodt.jpl.nasa.gov/1.0/cas");
            document.appendChild(root);

            for (Iterator<org.apache.oodt.cas.filemgr.structs.Element> i = elements.iterator(); i.hasNext();) {
                org.apache.oodt.cas.filemgr.structs.Element element = i.next();
                Element elementElem = document.createElement("element");
                elementElem.setAttribute("id", element.getElementId());
                elementElem.setAttribute("name", element.getElementName());

                Element descriptionElem = document.createElement("description");
                descriptionElem.appendChild(document.createTextNode(element
                        .getDescription()));
                elementElem.appendChild(descriptionElem);

                Element dcElementElem = document.createElement("dcElement");
                dcElementElem.appendChild(document.createTextNode(element
                        .getDCElement() != null ? element.getDCElement() : ""));
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
        Document document = null;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();

            Element root = (Element) document.createElement("cas:producttypes");
            root.setAttribute("xmlns:cas", "http://oodt.jpl.nasa.gov/1.0/cas");
            document.appendChild(root);

            // now add the set of metadata elements in the properties object
            for (Iterator<ProductType> i = productTypes.iterator(); i.hasNext();) {
                ProductType type = i.next();

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
        String description = null;
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

    public static HashMap<String, List<org.apache.oodt.cas.filemgr.structs.Element>> 
            getProductTypeElementList(Node typeMapNode, HashMap<String, org.apache.oodt.cas.filemgr.structs.Element> elements) {
        org.w3c.dom.Element typeMapElement = (org.w3c.dom.Element) typeMapNode;

        String typeId = typeMapElement.getAttribute("id");

        HashMap<String, List<org.apache.oodt.cas.filemgr.structs.Element>> productTypeElementMap = new HashMap<String, List<org.apache.oodt.cas.filemgr.structs.Element>>();

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

}
