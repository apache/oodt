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


package org.apache.oodt.cas.product.rss;

//OODT imports
import org.apache.oodt.commons.xml.XMLUtils;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * Reader class for {@link RSSConfig}s from {@link File}s.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class RSSConfigReader implements RSSConfigReaderMetKeys {

  /**
   * Reads an {@link RSSConfig} from a {@link File}.
   * 
   * @param file
   *          The {@link File} representation of the {@link RSSConfig}.
   * @return An {@link RSSConfig} initialized with the information read from the
   *         given {@link File}.
   * @throws FileNotFoundException
   *           If the {@link File} is not found.
   */
  public static RSSConfig readConfig(File file) throws FileNotFoundException {
    RSSConfig conf = new RSSConfig();

    Document doc = XMLUtils.getDocumentRoot(new FileInputStream(file));
    Element rootElem = doc.getDocumentElement();
    conf.setChannelLink(rootElem.getAttribute(CHANNEL_LINK_ATTR));

    readNamespaces(rootElem, conf);
    readTags(rootElem, conf);
    return conf;
  }
  
  protected static void readNamespaces(Element root, RSSConfig conf) {
    NodeList namespaceList = root.getElementsByTagName(NAMESPACE_TAG);
    if (namespaceList != null && namespaceList.getLength() > 0) {
      for (int i = 0; i < namespaceList.getLength(); i++) {
        Element namespaceElem = (Element) namespaceList.item(i);
        RSSNamespace namespace = new RSSNamespace();
        namespace.setPrefix(namespaceElem.getAttribute(NAMESPACE_ATTR_PREFIX));
        namespace.setUri(namespaceElem.getAttribute(NAMESPACE_ATTR_URI));
        conf.getNamespaces().add(namespace);
      }
    }
  }

  protected static void readTags(Element root, RSSConfig conf) {
    NodeList tagList = root.getElementsByTagName(TAG_TAG);
    if (tagList != null && tagList.getLength() > 0) {
      for (int i = 0; i < tagList.getLength(); i++) {
        Element tagElem = (Element) tagList.item(i);
        RSSTag tag = new RSSTag();
        tag.setName(tagElem.getAttribute(TAG_ATTR_NAME));

        // check to see if it has a source
        if (tagElem.getAttributeNode(TAG_ATTR_SOURCE) != null) {
          tag.setSource(tagElem.getAttribute(TAG_ATTR_SOURCE));
        }

        readAttrs(tagElem, tag);
        conf.getTags().add(tag);
      }
    }
  }

  protected static void readAttrs(Element tagElem, RSSTag tag) {
    NodeList attrTagList = tagElem.getElementsByTagName(ATTRIBUTE_TAG);

    if (attrTagList != null && attrTagList.getLength() > 0) {
      for (int i = 0; i < attrTagList.getLength(); i++) {
        Element attrTag = (Element) attrTagList.item(i);
        RSSTagAttribute attr = new RSSTagAttribute();
        attr.setName(attrTag.getAttribute(ATTRIBUTE_ATTR_NAME));
        attr.setValue(attrTag.getAttribute(ATTRIBUTE_ATTR_VALUE));
        tag.getAttrs().add(attr);
      }
    }

  }
}
