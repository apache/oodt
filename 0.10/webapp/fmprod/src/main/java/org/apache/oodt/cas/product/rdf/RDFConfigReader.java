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


package org.apache.oodt.cas.product.rdf;

//OODT imports
import org.apache.oodt.commons.xml.XMLUtils;
import static org.apache.oodt.cas.product.rdf.RDFConfigReaderMetKeys.*;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * Reader class for {@link RDFConfig}s from {@link File}s.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class RDFConfigReader {

  /**
   * Reads an {@link RDFConfig} from a {@link File}.
   * 
   * @param file
   *          The {@link File} representation of the {@link RDFConfig}.
   * @return An {@link RDFConfig} initialized with the information read from the
   *         given {@link File}.
   * @throws FileNotFoundException
   *           If the {@link File} is not found.
   */
  public static RDFConfig readConfig(File file) throws FileNotFoundException {
    RDFConfig conf = new RDFConfig();

    Document doc = XMLUtils.getDocumentRoot(new FileInputStream(file));
    Element rootElem = doc.getDocumentElement();

    readNamespaces(rootElem, conf);
    readRewriteRules(rootElem, conf);
    readResourceLinks(rootElem, conf);
    readKeyNsMap(rootElem, conf);
    readTypesNsMap(rootElem, conf);

    return conf;
  }

  protected static void readNamespaces(Element root, RDFConfig conf) {
    Element namespacesRoot = XMLUtils.getFirstElement(NS_OUTER_TAG, root);
    NodeList nsList = namespacesRoot.getElementsByTagName(NS_TAG);
    if (nsList != null && nsList.getLength() > 0) {
      for (int i = 0; i < nsList.getLength(); i++) {
        Element nsElem = (Element) nsList.item(i);
        conf.getNsMap().put(nsElem.getAttribute(NS_NAME_ATTR),
            nsElem.getAttribute(NS_VALUE_ATTR));
      }
    }
  }

  protected static void readRewriteRules(Element root, RDFConfig conf) {
    Element rewriteRoot = XMLUtils.getFirstElement(REWRITE_OUTER_TAG, root);
    NodeList rewriteList = rewriteRoot.getElementsByTagName(REWRITE_KEY_TAG);
    if (rewriteList != null && rewriteList.getLength() > 0) {
      for (int i = 0; i < rewriteList.getLength(); i++) {
        Element rewriteKeyElem = (Element) rewriteList.item(i);
        conf.getRewriteMap().put(
            rewriteKeyElem.getAttribute(REWRITE_FROM_ATTR),
            rewriteKeyElem.getAttribute(REWRITE_TO_ATTR));
      }
    }
  }

  protected static void readResourceLinks(Element root, RDFConfig conf) {
    Element resLinkRoot = XMLUtils.getFirstElement(RESOURCE_LINK_TAG, root);
    NodeList linkList = resLinkRoot.getElementsByTagName(RESLINK_KEY_TAG);
    if (linkList != null && linkList.getLength() > 0) {
      for (int i = 0; i < linkList.getLength(); i++) {
        Element reslinkKeyElem = (Element) linkList.item(i);
        conf.getResLinkMap().put(
            reslinkKeyElem.getAttribute(RESLINK_KEY_TAG_NAME_ATTR),
            reslinkKeyElem.getAttribute(RESLINK_KEY_TAG_LINK_ATTR));
      }
    }
  }

  protected static void readKeyNsMap(Element root, RDFConfig conf) {
    Element keyNsRoot = XMLUtils.getFirstElement(KEY_NSMAP_TAG, root);
    conf.setDefaultKeyNs(keyNsRoot.getAttribute(KEY_NSMAP_DEFAULT_ATTR));
    NodeList keyNsList = keyNsRoot.getElementsByTagName(KEY_NSMAP_KEY_TAG);
    if (keyNsList != null && keyNsList.getLength() > 0) {
      for (int i = 0; i < keyNsList.getLength(); i++) {
        Element keyNsElem = (Element) keyNsList.item(i);
        conf.getKeyNsMap().put(
            keyNsElem.getAttribute(KEY_NSMAP_KEY_TAG_NAME_ATTR),
            keyNsElem.getAttribute(KEY_NSMAP_KEY_TAG_NS_ATTR));
      }
    }
  }

  protected static void readTypesNsMap(Element root, RDFConfig conf) {
    Element typeNsRoot = XMLUtils.getFirstElement(TYPE_NSMAP_TAG, root);
    conf.setDefaultTypeNs(typeNsRoot.getAttribute(TYPE_NSMAP_DEFAULT_ATTR));
    NodeList typeNsList = typeNsRoot.getElementsByTagName(TYPE_NSMAP_TYPE_TAG);
    if (typeNsList != null && typeNsList.getLength() > 0) {
      for (int i = 0; i < typeNsList.getLength(); i++) {
        Element typeNsElem = (Element) typeNsList.item(i);
        conf.getTypesNsMap().put(
            typeNsElem.getAttribute(TYPE_NSMAP_TYPE_NAME_ATTR),
            typeNsElem.getAttribute(TYPE_NSMAP_TYPE_NS_ATTR));
      }
    }
  }

}
