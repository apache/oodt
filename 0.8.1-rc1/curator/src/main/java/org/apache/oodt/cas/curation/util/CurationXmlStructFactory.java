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


package org.apache.oodt.cas.curation.util;

//JDK imports
import java.net.URLEncoder;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//OODT imports
import org.apache.oodt.commons.xml.XMLUtils;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.util.XmlStructFactory;

/**
 * 
 * Until we solve the serialization problem in CAS 1.8.0 for adding metadata and
 * extractor information to {@link XmlStructFactory}, this class will suffice to
 * get us the actual policy management that we need.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class CurationXmlStructFactory {

  public static void writeProductTypeXmlDocument(
      List<ProductType> productTypes, String xmlFilePath) throws Exception {
    XMLUtils.writeXmlFile(getProductTypeXmlDocument(productTypes), xmlFilePath);
  }

  public static Document getProductTypeXmlDocument(
      List<ProductType> productTypes) throws Exception {
    Document doc = XmlStructFactory.getProductTypeXmlDocument(productTypes);

    // for every product type, i want to add in the versioner info and the
    // met extractor info

    Element rootElem = doc.getDocumentElement();
    NodeList typeNodeList = rootElem.getElementsByTagName("type");
    if (typeNodeList != null && typeNodeList.getLength() > 0) {
      for (int i = 0; i < typeNodeList.getLength(); i++) {
        Element typeElem = (Element) typeNodeList.item(i);
        augmentElement(productTypes, typeElem, doc);
      }
    }

    return doc;
  }

  private static void augmentElement(List<ProductType> productTypes,
      Element typeElem, Document doc) throws Exception {
    String productTypeName = typeElem.getAttribute("name");
    ProductType type = getType(productTypes, productTypeName);

    Element metadataRootElem = doc.createElement("metadata");
    
    for (Object metKey : type.getTypeMetadata().getHashtable().keySet()) {
      String key = (String) metKey;
      List<String> vals = type.getTypeMetadata().getAllMetadata(key);

      Element metadataElem = doc.createElement("keyval");
      Element keyElem = doc.createElement("key");
      keyElem.appendChild(doc.createTextNode(URLEncoder.encode(key, "UTF-8")));

      metadataElem.appendChild(keyElem);
      for (String val : vals) {
        Element valElem = doc.createElement("val");
        if (val == null) {
          throw new Exception("Attempt to write null value "
              + "for property: [" + key + "]: val: [" + val + "]");
        }

        valElem
            .appendChild(doc.createTextNode(URLEncoder.encode(val, "UTF-8")));
        metadataElem.appendChild(valElem);
      }

      metadataRootElem.appendChild(metadataElem);
      typeElem.appendChild(metadataRootElem);

    }

  }

  private static ProductType getType(List<ProductType> types, String name) {
    for (ProductType type : types) {
      if (type.getName().equals(name)) {
        return type;
      }
    }

    return null;
  }

}
