// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.cas.metadata;

import org.apache.oodt.commons.xml.XMLUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 *
 * Test class for SerializableMetadata.
 *
 * @author bfoster
 * @author mattmann
 *
 */
public class TestSerializableMetadata extends TestCase {

  private final String[] encodings = new String[] { "UTF-8", "iso-8859-1",
      "windows-1252", "UTF-16", "US-ASCII" };

  public TestSerializableMetadata() {
    super();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testSerialization() throws Exception {
    for (int i = 0; i < encodings.length; i++) {
      String encoding = encodings[i];
      boolean useCDATA = false;
      for (int j = 0; j < 2; j++, useCDATA = true) {
        SerializableMetadata sm = new SerializableMetadata(encoding, useCDATA);
        sm.addMetadata("key1", "val1");
        sm.addMetadata("key2", "val2");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(sm);
        oos.flush();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
            out.toByteArray()));
        SerializableMetadata sm2 = (SerializableMetadata) ois.readObject();
        oos.close();
        ois.close();

        assertNotNull(sm2);
        assertNotNull(sm2.getMap());
        assertEquals(2, sm2.getMap().size());
        assertNotNull(sm2.getMetadata("key1"));
        assertEquals("val1", sm2.getMetadata("key1"));
        assertNotNull(sm2.getMetadata("key2"));
        assertEquals("val2", sm2.getMetadata("key2"));
        assertNotNull(sm2.getEncoding());
        assertEquals(encoding, sm2.getEncoding());
        assertEquals(useCDATA, sm2.isUsingCDATA());
      }
    }
  }

  public void testXmlStreaming() throws Exception {
    for (int i = 0; i < encodings.length; i++) {
      String encoding = encodings[i];
      boolean useCDATA = false;
      for (int j = 0; j < 2; j++, useCDATA = true) {
        SerializableMetadata metadata1 = new SerializableMetadata(encoding,
            useCDATA);
        metadata1.addMetadata("Name1", "Value1");
        metadata1.addMetadata("Name2", "Value/2");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        metadata1.writeMetadataToXmlStream(out);

        SerializableMetadata metadata2 = new SerializableMetadata(encoding,
            useCDATA);
        metadata2.loadMetadataFromXmlStream(new ByteArrayInputStream(out
            .toByteArray()));
        out.close();

        assertNotNull(metadata2);
        assertNotNull(metadata2.getMap());
        assertEquals(2, metadata2.getMap().size());
        assertNotNull(metadata2.getMetadata("Name1"));
        assertEquals("Value1", metadata2.getMetadata("Name1"));
        assertNotNull(metadata2.getMetadata("Name2"));
        assertEquals("Value/2", metadata2.getMetadata("Name2"));
      }
    }
  }

  public void testMetadataConversion() throws Exception {
    for (int i = 0; i < encodings.length; i++) {
      String encoding = encodings[i];
      Metadata m = new Metadata();
      m.addMetadata("key1", "val1");
      m.addMetadata("key2", "val2");

      SerializableMetadata sm = new SerializableMetadata(m, encoding, false);
      Metadata mConv = sm.getMetadata();

      assertNotNull(mConv);
      assertNotNull(mConv.getMap());
      assertEquals(2, mConv.getMap().size());
      assertNotNull(mConv.getMetadata("key1"));
      assertEquals("val1", mConv.getMetadata("key1"));
      assertNotNull(mConv.getMetadata("key2"));
      assertEquals("val2", mConv.getMetadata("key2"));
    }
  }

  public void testWriteRead() {
    SerializableMetadata metadata = new SerializableMetadata();
    metadata.addMetadata("Name1", "Value1");
    metadata.addMetadata("Name2", "Value2");

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    try {
      XMLUtils.writeXmlToStream(metadata.toXML(), out);
    } catch (Exception e) {
      fail(e.getMessage());
    }

    SerializableMetadata metadata2 = null;
    try {
      metadata2 = new SerializableMetadata(new ByteArrayInputStream(out
          .toByteArray()));
    } catch (Exception e) {
      fail(e.getMessage());
    }

    assertNotNull(metadata2);
    assertNotNull(metadata2.getMap());

    assertEquals(2, metadata2.getMap().size());
    assertNotNull(metadata2.getMetadata("Name1"));
    assertEquals("Value1", metadata2.getMetadata("Name1"));
    assertNotNull(metadata2.getMetadata("Name2"));
    assertEquals("Value2", metadata2.getMetadata("Name2"));
  }

  public void testNamespace() throws Exception {
    SerializableMetadata metadata = new SerializableMetadata();
    metadata.addMetadata("Name1", "Value1");
    metadata.addMetadata("Name2", "Value2");

    // write xml DOM to string
    ByteArrayOutputStream array = new ByteArrayOutputStream();
    metadata.writeMetadataToXmlStream(array);

    // read string into new xml DOM
    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
    f.setNamespaceAware(true);
    Document doc = f.newDocumentBuilder().parse(new ByteArrayInputStream(array.toByteArray()));

    final String NS = "http://oodt.jpl.nasa.gov/1.0/cas";
    final String PREFIX = "cas";

    // compare namespaces in DOM before and after the write/read operation
    Element before = metadata.toXML().getDocumentElement();
    String nsBefore = before.getNamespaceURI();
    String preBefore = before.getPrefix();
    assertEquals(NS, nsBefore);
    assertEquals(PREFIX, preBefore);

    Element after = doc.getDocumentElement();
    String nsAfter = after.getNamespaceURI();
    String preAfter = after.getPrefix();
    assertEquals(NS, nsAfter);
    assertEquals(PREFIX, preAfter);
  }

}
