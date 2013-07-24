/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.oodt.cas.product.rss;

import java.io.ByteArrayInputStream;
import java.util.List;

import junit.framework.TestCase;

import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Document;

/**
 * Tests the {@link RSSConfigReader} class.
 * @author rlaidlaw
 * @version $Revision$
 */
public class RSSConfigReaderTest extends TestCase
{
  /**
   * Tests the readNamespaces method.
   */
  public void testReadNamespaces()
  {
    String testXmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<rss:rssconf xmlns:rss=\"http://oodt.apache.org/ns/rss\">"
        + "  <namespace prefix=\"cas\" uri=\"http://oodt.apache.org/ns/cas\"/>"
        + "  <tag name=\"cas:source\" source=\"[ProductType]\"/>"
        + "  <namespace prefix=\"pge\" uri=\"http://oodt.apache.org/ns/pge\"/>"
        + "  <tag name=\"pge:tag\" source=\"[ProductType]\"/>"
        + "  <namespace prefix=\"rss\" uri=\"http://oodt.apache.org/ns/rss\"/>"
        + "  <tag name=\"rss:enclosure\">"
        + "    <attribute name=\"x\" value=\"X\"/>"
        + "    <attribute name=\"y\" value=\"Y\"/>"
        + "    <attribute name=\"z\" value=\"Z\"/>"
        + "  </tag>"
        + "</rss:rssconf>";

    Document testXmlDocument =
        XMLUtils.getDocumentRoot(new ByteArrayInputStream(testXmlString
            .getBytes()));

    RSSConfig conf = new RSSConfig();
    RSSConfigReader.readNamespaces(testXmlDocument.getDocumentElement(), conf);

    List<RSSNamespace> namespaceList = conf.getNamespaces();
    assertNotNull(namespaceList);
    assertFalse(namespaceList.isEmpty());
    assertEquals(3, namespaceList.size());

    RSSNamespace namespaceA = namespaceList.get(0);
    RSSNamespace namespaceB = namespaceList.get(1);
    RSSNamespace namespaceC = namespaceList.get(2);

    assertNotNull(namespaceA);
    assertNotNull(namespaceB);
    assertNotNull(namespaceC);

    assertEquals("cas", namespaceA.getPrefix());
    assertEquals("pge", namespaceB.getPrefix());
    assertEquals("rss", namespaceC.getPrefix());

    assertEquals("http://oodt.apache.org/ns/cas",
        namespaceA.getUri());

    assertEquals("http://oodt.apache.org/ns/pge",
        namespaceB.getUri());

    assertEquals("http://oodt.apache.org/ns/rss",
        namespaceC.getUri());
  }

  /**
   * Tests the readTags and readAttrs methods.
   */
  public void testReadTagsAndAttrs()
  {
    String testXmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<rss:rssconf xmlns:rss=\"http://oodt.apache.org/ns/rss\">"
        + " <namespace prefix=\"cas\" uri=\"http://oodt.apache.org/ns/cas\"/>"
        + " <tag name=\"cas:source\" source=\"[ProductType]\"/>"
        + " <namespace prefix=\"georss\" uri=\"http://www.georss.org/georss\"/>"
        + " <tag name=\"georss:point\" source=\"[Latitude] [Longitude]\">"
        + "  <attribute name=\"one\" value=\"1\"/>"
        + " </tag>"
        + " <tag name=\"enclosure\" source=\"\">"
        + "  <attribute name=\"two\" value=\"2\"/>"
        + "  <attribute name=\"three\" value=\"3\"/>"
        + " </tag>"
        + " <tag name=\"enclosure\">"
        + "  <attribute name=\"four\" value=\"4\"/>"
        + "  <attribute name=\"five\" value=\"5\"/>"
        + "  <attribute name=\"six\" value=\"6\"/>"
        + " </tag>"
        + "</rss:rssconf>";

    Document testXmlDocument =
        XMLUtils.getDocumentRoot(new ByteArrayInputStream(testXmlString
            .getBytes()));

    RSSConfig conf = new RSSConfig();
    RSSConfigReader.readTags(testXmlDocument.getDocumentElement(), conf);

    List<RSSTag> tagList = conf.getTags();
    assertNotNull(tagList);
    assertFalse(tagList.isEmpty());
    assertEquals(4, tagList.size());

    RSSTag tagA = tagList.get(0);
    RSSTag tagB = tagList.get(1);
    RSSTag tagC = tagList.get(2);
    RSSTag tagD = tagList.get(3);

    assertNotNull(tagA);
    assertNotNull(tagB);
    assertNotNull(tagC);
    assertNotNull(tagD);

    assertEquals("cas:source", tagA.getName());
    assertEquals("georss:point", tagB.getName());
    assertEquals("enclosure", tagC.getName());
    assertEquals("enclosure", tagD.getName());

    assertEquals("[ProductType]", tagA.getSource());
    assertEquals("[Latitude] [Longitude]", tagB.getSource());
    assertEquals("", tagC.getSource());
    assertNull(tagD.getSource());

    List<RSSTagAttribute> attributesA = tagA.getAttrs();
    List<RSSTagAttribute> attributesB = tagB.getAttrs();
    List<RSSTagAttribute> attributesC = tagC.getAttrs();
    List<RSSTagAttribute> attributesD = tagD.getAttrs();

    assertNotNull(attributesA);
    assertNotNull(attributesB);
    assertNotNull(attributesC);
    assertNotNull(attributesD);

    assertTrue(attributesA.isEmpty());
    assertEquals(1, attributesB.size());
    assertEquals(2, attributesC.size());
    assertEquals(3, attributesD.size());

    assertEquals("one", attributesB.get(0).getName());
    assertEquals("two", attributesC.get(0).getName());
    assertEquals("three", attributesC.get(1).getName());
    assertEquals("four", attributesD.get(0).getName());
    assertEquals("five", attributesD.get(1).getName());
    assertEquals("six", attributesD.get(2).getName());

    assertEquals("1", attributesB.get(0).getValue());
    assertEquals("2", attributesC.get(0).getValue());
    assertEquals("3", attributesC.get(1).getValue());
    assertEquals("4", attributesD.get(0).getValue());
    assertEquals("5", attributesD.get(1).getValue());
    assertEquals("6", attributesD.get(2).getValue());
  }
}

