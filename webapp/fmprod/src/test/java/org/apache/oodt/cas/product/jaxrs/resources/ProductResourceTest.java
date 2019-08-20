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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Implements tests for methods in the {@link ProductResource} class.
 * @author rlaidlaw
 * @version $Revision$
 */
public class ProductResourceTest
{
  /**
   * Tests that {@link ProductResource product resources} are marshalled to
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
    // Create a ProductResource using ProductType, Reference, Metadata and
    // Product instances.
    HashMap<String, Object> metadataEntries = new HashMap<>();
    metadataEntries.put("CAS.Test", "test value");
    Metadata metadata = new Metadata();
    metadata.addMetadata(metadataEntries);

    Reference reference = new Reference("original", "dataStore", 1000,
      new MimeTypes().forName("text/plain"));
    List<Reference> references = new ArrayList<Reference>();
    references.add(reference);

    ProductType productType = new ProductType("1", "GenericFile", "test type",
      "repository", "versioner");

    Product product = new Product();
    product.setProductId("123");
    product.setProductName("test.txt");
    product.setProductStructure(Product.STRUCTURE_FLAT);
    product.setProductType(productType);

    ProductResource resource = new ProductResource(product, metadata,
      references, new File("/tmp"));


    // Generate the expected output.
    String expectedXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<product>"
        + "<id>" + product.getProductId() + "</id>"
        + "<name>" + product.getProductName() + "</name>"
        + "<structure>" + product.getProductStructure() + "</structure>"
        + "<type>" + productType.getName() + "</type>"
        + "<metadata>"
        + "<keyval>"
        + "<key>" + metadata.getAllKeys().get(0) + "</key>"
        + "<val>" + metadata.getAllValues().get(0) + "</val>"
        + "</keyval>"
        + "</metadata>"
        + "<references>"
        + "<reference>"
        + "<productId>" + product.getProductId() + "</productId>"
        + "<refIndex>0</refIndex>"
        + "<dataStoreReference>"
        +    reference.getDataStoreReference()
        + "</dataStoreReference>"
        + "<originalReference>"
        +    reference.getOrigReference()
        + "</originalReference>"
        + "<mimeType>" + reference.getMimeType().getName() + "</mimeType>"
        + "<fileSize>" + reference.getFileSize() + "</fileSize>"
        + "</reference>"
        + "</references>"
        + "</product>";


    // Set up a JAXB context and marshall the ProductResource to XML.
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
