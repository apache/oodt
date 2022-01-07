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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Implements tests for methods in the {@link ReferenceResource} class.
 * @author rlaidlaw
 * @version $Revision$
 */
public class ReferenceResourceTest
{
  /**
   * Tests that {@link ReferenceResource reference resources} are marshalled to
   * the expected XML format.
   * @throws IOException if the {@link Diff} constructor fails
   * @throws JAXBException if the {@link JAXBContext} or {@link Marshaller} fail
   * @throws MimeTypeException if {@link MimeTypes#forName(String)} fails
   * @throws SAXException if the {@link Diff} constructor fails
   */
  @Test
  public void testXmlMarshalling() throws IOException, JAXBException,
    MimeTypeException, SAXException
  {
    String productId = "123";
    int refIndex = 0;

    // Create a new ReferenceResource using a Reference instance.
    Reference reference = new Reference("original", "dataStore", 1000,
      new MimeTypes().forName("text/plain"));

    ReferenceResource resource = new ReferenceResource(productId, refIndex,
      reference, new File("/tmp"));


    // Generate the expected output.
    String expectedXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
      + "<reference>"
      + "<productId>" + productId + "</productId>"
      + "<refIndex>" + refIndex + "</refIndex>"
      + "<dataStoreReference>"
      +    reference.getDataStoreReference()
      + "</dataStoreReference>"
      + "<originalReference>"
      +    reference.getOrigReference()
      + "</originalReference>"
      + "<mimeType>" + reference.getMimeType().getName() + "</mimeType>"
      + "<fileSize>" + reference.getFileSize() +  "</fileSize>"
      + "</reference>";


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
    assertTrue("The output XML was different to the expected XML: "
      + diff.toString(), diff.identical());
  }
}
