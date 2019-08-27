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
 * Implements tests for methods in the {@link DatasetResource} class.
 * @author rlaidlaw
 * @version $Revision$
 */
public class DatasetResourceTest
{
  /**
   * Tests that {@link DatasetResource dataset resources} are marshalled to
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
    // Create a ProductType.
    ProductType productType = new ProductType("1", "GenericFile", "test type",
      "repository", "versioner");

    // Create a ProductResource using Reference, Metadata and Product instances.
    Reference reference1 = new Reference("original1", "dataStore1", 500,
      new MimeTypes().forName("text/plain"));
    List<Reference> references1 = new ArrayList<Reference>();
    references1.add(reference1);

    HashMap<String, Object> metadataEntries1 = new HashMap<>();
    metadataEntries1.put("product1_meta", "test1");
    Metadata metadata1 = new Metadata();
    metadata1.addMetadata(metadataEntries1);

    Product product1 = new Product();
    product1.setProductId("123");
    product1.setProductName("test.txt");
    product1.setProductType(productType);

    ProductResource productResource1 = new ProductResource(product1, metadata1,
      references1, new File("/tmp"));


    // Create another ProductResource using Reference, Metadata and Product
    // instances.
    Reference reference2 = new Reference("original2", "dataStore2", 1000,
      new MimeTypes().forName("application/pdf"));
    List<Reference> references2 = new ArrayList<Reference>();
    references2.add(reference2);

    HashMap<String, Object> metadataEntries2 = new HashMap<>();
    metadataEntries2.put("product2_meta", "test2");
    Metadata metadata2 = new Metadata();
    metadata2.addMetadata(metadataEntries2);

    Product product2 = new Product();
    product2.setProductId("456");
    product2.setProductName("test2.txt");
    product2.setProductType(productType);

    ProductResource productResource2 = new ProductResource(product2, metadata2,
      references2, new File("/tmp"));

    // Create a DatasetResource using ProductType, Metadata and ProductResource
    // instances.
    HashMap<String, Object> metadataEntries3 = new HashMap<>();
    metadataEntries3.put("dataset_meta", "test3");
    Metadata metadata3 = new Metadata();
    metadata3.addMetadata(metadataEntries3);

    DatasetResource resource = new DatasetResource(
      productType.getProductTypeId(), productType.getName(), metadata3,
      new File("/tmp"));
    resource.addProductResource(productResource1);
    resource.addProductResource(productResource2);


    // Generate the expected output.
    String expectedXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
      + "<dataset>"
      +   "<id>" + productType.getProductTypeId() + "</id>"
      +   "<name>" + productType.getName() + "</name>"
      +   "<metadata>"
      +     "<keyval>"
      +       "<key>" + metadata3.getAllKeys().get(0) + "</key>"
      +       "<val>" + metadata3.getAllValues().get(0) + "</val>"
      +     "</keyval>"
      +   "</metadata>"
      +   "<products>"
      +     "<product>"
      +       "<id>" + product1.getProductId() + "</id>"
      +       "<name>" + product1.getProductName() + "</name>"
      +       "<type>" + productType.getName() + "</type>"
      +       "<metadata>"
      +         "<keyval>"
      +           "<key>" + metadata1.getAllKeys().get(0) + "</key>"
      +           "<val>" + metadata1.getAllValues().get(0) + "</val>"
      +         "</keyval>"
      +       "</metadata>"
      +       "<references>"
      +         "<reference>"
      +           "<productId>" + product1.getProductId() + "</productId>"
      +           "<refIndex>0</refIndex>"
      +           "<dataStoreReference>"
      +              reference1.getDataStoreReference()
      +           "</dataStoreReference>"
      +           "<originalReference>"
      +              reference1.getOrigReference()
      +           "</originalReference>"
      +           "<mimeType>"
      +              reference1.getMimeType().getName()
      +           "</mimeType>"
      +           "<fileSize>" + reference1.getFileSize() + "</fileSize>"
      +         "</reference>"
      +       "</references>"
      +     "</product>"
      +     "<product>"
      +       "<id>" + product2.getProductId() + "</id>"
      +       "<name>" + product2.getProductName() + "</name>"
      +       "<type>" + productType.getName() + "</type>"
      +       "<metadata>"
      +         "<keyval>"
      +           "<key>" + metadata2.getAllKeys().get(0) + "</key>"
      +           "<val>" + metadata2.getAllValues().get(0) + "</val>"
      +         "</keyval>"
      +       "</metadata>"
      +       "<references>"
      +         "<reference>"
      +           "<productId>" + product2.getProductId() + "</productId>"
      +           "<refIndex>0</refIndex>"
      +           "<dataStoreReference>"
      +              reference2.getDataStoreReference()
      +           "</dataStoreReference>"
      +           "<originalReference>"
      +              reference2.getOrigReference()
      +           "</originalReference>"
      +           "<mimeType>"
      +              reference2.getMimeType().getName()
      +           "</mimeType>"
      +           "<fileSize>" + reference2.getFileSize() + "</fileSize>"
      +         "</reference>"
      +       "</references>"
      +     "</product>"
      +   "</products>"
      + "</dataset>";


    // Set up a JAXB context and marshall the DatasetResource to XML.
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
