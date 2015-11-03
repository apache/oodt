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
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Class representing custom configurations for RDF XML outputs.
 * @author rlaidlaw
 * @version $Revision$
 */
public class RdfConfiguration
{
  // Constants used to identify items in the configuration file.
  private static final String RDF_RES_ATTR = "rdf:resource";
  private static final String NS_OUTER = "namespaces";
  private static final String NS_INNER = "ns";
  private static final String NS_KEY = "name";
  private static final String NS_VALUE = "value";
  private static final String REWRITE_OUTER = "rewrite";
  private static final String REWRITE_INNER = "key";
  private static final String REWRITE_KEY = "from";
  private static final String REWRITE_VALUE = "to";
  private static final String RESLINK_OUTER = "resourcelinks";
  private static final String RESLINK_INNER = "key";
  private static final String RESLINK_KEY = "name";
  private static final String RESLINK_VALUE = "link";
  private static final String KEY_OUTER = "keynsmap";
  private static final String KEY_INNER = "key";
  private static final String KEY_KEY = "name";
  private static final String KEY_VALUE = "ns";
  private static final String KEY_DEFAULT = "default";
  private static final String TYPE_OUTER = "typesnsmap";
  private static final String TYPE_INNER = "type";
  private static final String TYPE_KEY = "name";
  private static final String TYPE_VALUE = "ns";
  private static final String TYPE_DEFAULT = "default";

  private Map<String, String> nsMap = new ConcurrentHashMap<String, String>();
  private Map<String, String> rewriteMap = new ConcurrentHashMap<String, String>();
  private Map<String, String> resLinkMap = new ConcurrentHashMap<String, String>();
  private Map<String, String> keyNsMap = new ConcurrentHashMap<String, String>();
  private Map<String, String> typesNsMap = new ConcurrentHashMap<String, String>();
  private String defaultKeyNs = null;
  private String defaultTypeNs = null;



  /**
   * Initializes the parameters in the configuration object using values from
   * the supplied file.
   * @param file the configuration file
   * @throws IOException if the file does not exist and cannot be read
   */
  public void initialize(File file) throws IOException
  {
    FileInputStream fis = new FileInputStream(file);
    Document doc = XMLUtils.getDocumentRoot(fis);
    Element root = doc.getDocumentElement();

    nsMap = readConfiguration(root,
      NS_OUTER, NS_INNER, NS_KEY, NS_VALUE);

    rewriteMap = readConfiguration(root,
      REWRITE_OUTER, REWRITE_INNER, REWRITE_KEY, REWRITE_VALUE);

    resLinkMap = readConfiguration(root,
      RESLINK_OUTER, RESLINK_INNER, RESLINK_KEY, RESLINK_VALUE);

    keyNsMap = readConfiguration(root,
      KEY_OUTER, KEY_INNER, KEY_KEY, KEY_VALUE);

    typesNsMap = readConfiguration(root,
      TYPE_OUTER, TYPE_INNER, TYPE_KEY, TYPE_VALUE);

    Element keyNsRoot = XMLUtils.getFirstElement(KEY_OUTER, root);
    defaultKeyNs = keyNsRoot.getAttribute(KEY_DEFAULT);

    Element typeNsRoot = XMLUtils.getFirstElement(TYPE_OUTER, root);
    defaultTypeNs = typeNsRoot.getAttribute(TYPE_DEFAULT);

    fis.close();
  }



  private Map<String, String> readConfiguration(Element element,
    String outerTag, String innerTag, String key, String value)
  {
    Map<String, String> map = new ConcurrentHashMap<String, String>();

    Element outer = XMLUtils.getFirstElement(outerTag, element);
    NodeList nodeList = outer.getElementsByTagName(innerTag);
    if (nodeList != null && nodeList.getLength() > 0)
    {
      for (int i = 0; i < nodeList.getLength(); i++)
      {
        Element inner = (Element) nodeList.item(i);
        map.put(inner.getAttribute(key), inner.getAttribute(value));
      }
    }

    return map;
  }



  /**
   * Gets the namespace map for the configuration.
   * @return the namespace map for the configuration
   */
  public Map<String, String> getNamespaces()
  {
    return nsMap;
  }



  /**
   * Gets the requested value from the key namespace map if it is found,
   * otherwise gets the default key namespace.
   * @param key the map key to use for retrieving the namespace
   * @return the namespace value for the map key if found, otherwise the default
   * namespace
   */
  public String getKeyNamespace(String key)
  {
    return keyNsMap.containsKey(key) ? keyNsMap.get(key) : defaultKeyNs;
  }



  /**
   * Gets the requested value from the type namespace map if it is found,
   * otherwise gets the default type namespace.
   * @param key the map key to use for retrieving the namespace
   * @return the namespace value for the map key if found, otherwise the default
   * namespace
   */
  public String getTypeNamespace(String key)
  {
    return typesNsMap.containsKey(key) ? typesNsMap.get(key) : defaultTypeNs;
  }



  /**
   * Creates an {@link Element} for a {@link Document}.
   * @param key a map key used to search this configuration's maps
   * @param value the value for the element
   * @param document the document context of the element
   * @return a new element constructed within the rules and constraints of this
   * configuration
   */
  public Element createElement(String key, String value, Document document)
  {
    // Apply the rewrite rules.
    String tagName = rewriteMap.containsKey(key) ? rewriteMap.get(key) : key;
    if (tagName.contains(" ")) {
      tagName = StringUtils.join(WordUtils.capitalizeFully(tagName).split(
          " "));
    }

    // Get the tag's namespace or the default namespace.
    String namespace = keyNsMap.containsKey(key)
      ? keyNsMap.get(key) : defaultKeyNs;

    // Create the element.
    Element element;
    if (resLinkMap.containsKey(key))
    {
      element = document.createElement(namespace + ":" + tagName);
      String linkBase = resLinkMap.get(key);
      linkBase += linkBase.endsWith("/") ? "" : "/";
      element.setAttribute(RDF_RES_ATTR, linkBase + value);
    }
    else
    {
      element = document.createElement(namespace + ":" + tagName);
      element.appendChild(document.createTextNode(StringEscapeUtils.escapeXml(value)));
    }

    return element;
  }
}
