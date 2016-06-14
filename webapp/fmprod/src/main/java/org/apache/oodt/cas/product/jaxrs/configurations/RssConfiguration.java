/**
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.oodt.cas.product.jaxrs.configurations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Class representing custom configurations for RSS XML outputs.
 * @author rlaidlaw
 * @version $Revision$
 */
public class RssConfiguration
{
  private static final String CHANNEL_LINK = "channelLink";
  private static final String NAMESPACE = "namespace";
  private static final String NAMESPACE_KEY = "prefix";
  private static final String NAMESPACE_VALUE = "uri";
  private static final String TAG = "tag";
  private static final String TAG_NAME = "name";
  private static final String TAG_SOURCE = "source";
  private static final String ATTRIBUTE = "attribute";
  private static final String ATTRIBUTE_NAME = "name";
  private static final String ATTRIBUTE_VALUE = "value";

  private List<RssNamespace> namespaceList = new Vector<RssNamespace>();
  private List<RssTag> tagList = new Vector<RssTag>();
  private String channelLink = null;



  /**
   * Initializes the parameters in the configuration object using values from
   * the supplied file.
   * @param file the configuration file
   * @throws IOException if the file does not exist and cannot be read
   */
  public void initialize(File file) throws IOException
  {
    FileInputStream fis = new FileInputStream(file);
    Document document = XMLUtils.getDocumentRoot(fis);
    Element root = document.getDocumentElement();

    channelLink = root.getAttribute(CHANNEL_LINK);
    namespaceList = readNamespaces(root);
    tagList = readTags(root);

    fis.close();
  }



  private List<RssNamespace> readNamespaces(Element root)
  {
    List<RssNamespace> namespaces = new Vector<RssNamespace>();
    NodeList nodeList = root.getElementsByTagName(NAMESPACE);
    if (nodeList != null && nodeList.getLength() > 0)
    {
      for (int i = 0; i < nodeList.getLength(); i++)
      {
        Element element = (Element) nodeList.item(i);
        namespaces.add(new RssNamespace(element.getAttribute(NAMESPACE_KEY),
          element.getAttribute(NAMESPACE_VALUE)));
      }
    }
    return namespaces;
  }



  private List<RssTag> readTags(Element root)
  {
    List<RssTag> tags = new Vector<RssTag>();
    NodeList list = root.getElementsByTagName(TAG);
    if (list != null && list.getLength() > 0)
    {
      for (int i = 0; i < list.getLength(); i++)
      {
        Element element = (Element) list.item(i);
        tags.add(new RssTag(element.getAttribute(TAG_NAME),
          element.getAttribute(TAG_SOURCE), readAttributes(element)));
      }
    }
    return tags;
  }



  private List<RssTagAttribute> readAttributes(Element element)
  {
    List<RssTagAttribute> attributes = new Vector<RssTagAttribute>();
    NodeList nodeList = element.getElementsByTagName(ATTRIBUTE);
    if (nodeList != null && nodeList.getLength() > 0)
    {
      for (int i = 0; i < nodeList.getLength(); i++)
      {
        Element attribute = (Element) nodeList.item(i);
        attributes.add(new RssTagAttribute(
          attribute.getAttribute(ATTRIBUTE_NAME),
          attribute.getAttribute(ATTRIBUTE_VALUE)));
      }
    }
    return attributes;
  }



  /**
   * Gets the list of namespaces.
   * @return the list of namespaces
   */
  public List<RssNamespace> getNamespaceList()
  {
    return namespaceList;
  }



  /**
   * Gets the channel link URL string.
   * @return the channel link URL string
   */
  public String getChannelLink()
  {
    return channelLink;
  }



  /**
   * Appends elements (tags) defined in a configuration file to a specific
   * parent element.
   * @param metadata the metadata for the product
   * @param document the document to which these elements belong
   * @param parent the parent element to attach these elements to
   */
  public void appendTags(Metadata metadata, Document document,
    Element parent)
  {
    for (RssTag tag : tagList)
    {
      String tagName = tag.getName();
      if (tagName.contains(" ")) {
        tagName = StringUtils.join(WordUtils.capitalizeFully(tagName).split(
            " "));
      }

      
      // Create a new element for the tag.
      Element element = XMLUtils.addNode(document, parent, tagName);

      // Add a value for the tag from the tag source.
      if (tag.getSource() != null)
      {
        element.appendChild(document.createTextNode(StringEscapeUtils.escapeXml(PathUtils
          .replaceEnvVariables(tag.getSource(), metadata))));
      }

      // Add attributes to the tag as defined in the configuration.
      for (RssTagAttribute attribute : tag.getAttributes())
      {
        element.setAttribute(attribute.getName(), PathUtils.replaceEnvVariables(
          attribute.getValue(), metadata));
      }
    }
  }
}
