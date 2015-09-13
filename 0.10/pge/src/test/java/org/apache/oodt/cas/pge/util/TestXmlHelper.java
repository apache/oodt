/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.oodt.cas.pge.util;

import static org.apache.oodt.cas.pge.config.PgeConfigMetKeys.IMPORT_TAG;
import static org.apache.oodt.cas.pge.util.XmlHelper.getFile;
import static org.apache.oodt.cas.pge.util.XmlHelper.getImports;
import static org.apache.oodt.cas.pge.util.XmlHelper.getNamespace;
import static org.apache.oodt.cas.pge.util.XmlHelper.getRootElement;

import java.io.FileNotFoundException;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.oodt.cas.metadata.Metadata;

import junit.framework.TestCase;

public class TestXmlHelper extends TestCase {

   private static final String BLANK_PGE_CONFIG = "src/test/resources/blank-pge-config.xml";
   private static final String IMPORTS_ONLY_PGE_CONFIG = "src/test/resources/imports-only-pge-config.xml";

   // Tests that only root node is required.
   public void testLoadBlankXmlFile() throws FileNotFoundException {
      Element elem = getRootElement(BLANK_PGE_CONFIG);
      assertNotNull(elem);
      assertEquals(1, elem.getChildNodes().getLength());
      assertEquals("#text", elem.getChildNodes().item(0).getNodeName());
      assertEquals("", elem.getChildNodes().item(0).getNodeValue().trim());
   }

   public void testGetImports() throws Exception {
      Element elem = getRootElement(IMPORTS_ONLY_PGE_CONFIG);
      Metadata metadata = new Metadata();
      List<Pair<String, String>> imports = getImports(elem, metadata);
      assertEquals(2, imports.size());
      assertEquals("blank", imports.get(0).getFirst());
      assertEquals("blank-pge-config.xml", imports.get(0).getSecond());
      assertEquals(null, imports.get(1).getFirst());
      assertEquals("pge-config.xml", imports.get(1).getSecond());
   }

   public void testGetNamespace() throws Exception {
      Element elem = getRootElement(IMPORTS_ONLY_PGE_CONFIG);
      Metadata metadata = new Metadata();

      NodeList importTags = elem.getElementsByTagName(IMPORT_TAG);
      assertEquals("blank", getNamespace((Element) importTags.item(0), metadata));
      assertEquals("blank-pge-config.xml", getFile((Element) importTags.item(0), metadata));
      assertNull(getNamespace((Element) importTags.item(1), metadata));
      assertEquals("pge-config.xml", getFile((Element) importTags.item(1), metadata));
   }
}
