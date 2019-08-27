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

package org.apache.oodt.cas.product.jaxrs.resources;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.oodt.cas.metadata.Metadata;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Implements tests for methods in the {@link MetadataResource} class.
 * @author rlaidlaw
 * @version $Revision$
 */
public class MetadataResourceTest
{
  /**
   * Tests that {@link MetadataResource metadata resources} are marshalled to
   * the expected XML format.
   * @throws IOException if the {@link Diff} constructor fails
   * @throws JAXBException if the {@link JAXBContext} or {@link Marshaller} fail
   * @throws SAXException if the {@link Diff} constructor fails
   */
  @Test
  public void testXmlMarshalling() throws IOException, JAXBException,
    SAXException
  {
    // Generate the expected output.
    String expectedXml =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
      + "<metadata>"
      + "<keyval><key>1</key><val>one</val></keyval>"
      + "<keyval><key>2</key><val>two</val></keyval>"
      + "<keyval><key>3</key><val>three</val></keyval>"
      + "<keyval><key>4</key><val>a</val><val>b</val><val>c</val></keyval>"
      + "</metadata>";

    // Create a MetadataResource using a Metadata instance.
    HashMap<String, Object> metadataEntries = new HashMap<>();
    metadataEntries.put("1", "one");
    metadataEntries.put("2", "two");
    metadataEntries.put("3", "three");
    List<String> list = new ArrayList<String>();
    list.add("a");
    list.add("b");
    list.add("c");
    metadataEntries.put("4", list);

    Metadata metadata = new Metadata();
    metadata.addMetadata(metadataEntries);
    MetadataResource resource = new MetadataResource(metadata);


    // Set up a JAXB context and marshall the ReferenceResource to XML.
    JAXBContext context = JAXBContext.newInstance(resource.getClass());
    Marshaller marshaller = context.createMarshaller();
    StringWriter writer = new StringWriter();
    marshaller.marshal(resource, writer);

    // Compare the expected and actual outputs.
    XMLUnit.setIgnoreWhitespace(true);
    XMLUnit.setIgnoreComments(true);
    XMLUnit.setIgnoreAttributeOrder(true);
    Diff diff = new Diff(expectedXml, writer.toString());
    diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
    assertTrue("The output XML was different to the expected XML: "
      + diff.toString(), diff.similar());
  }
}
