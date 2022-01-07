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
import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
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
 * Implements tests for methods in the {@link TransfersResource} class.
 * @author rlaidlaw
 * @version $Revision$
 */
public class TransfersResourceTest
{
  /**
   * Tests that {@link TransferResource transfer resources} are marshalled to
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
    // Create a FileTransferStatus instance using Metadata, Reference,
    // ProductType and Product instances.
    HashMap<String, Object> metadataEntries1 = new HashMap<>();
    metadataEntries1.put("CAS.ProductReceivedTime", "2013-09-12T16:25:50.662Z");
    Metadata metadata1 = new Metadata();
    metadata1.addMetadata(metadataEntries1);

    Reference reference1 = new Reference("original1", "dataStore1", 1000,
      new MimeTypes().forName("text/plain"));

    ProductType productType1 = new ProductType("1", "TestType", "test type 1",
      "repository1", "versioner1");

    Product product1 = new Product();
    product1.setProductId("123");
    product1.setProductName("test product");
    product1.setProductStructure(Product.STRUCTURE_FLAT);
    product1.setProductType(productType1);

    FileTransferStatus status1 = new FileTransferStatus(reference1, 1000, 100,
      product1);


    // Create another FileTransferStatus instance using Metadata, Reference,
    // ProductType and Product instances.
    HashMap<String, Object> metadataEntries2 = new HashMap<>();
    metadataEntries2.put("CAS.ProductReceivedTime", "2011-04-11T11:59:59.662Z");
    Metadata metadata2 = new Metadata();
    metadata2.addMetadata(metadataEntries2);

    Reference reference2 = new Reference("original2", "dataStore2", 500,
      new MimeTypes().forName("application/pdf"));

    ProductType productType2 = new ProductType("2", "TestType2", "test type 2",
        "repository2", "versioner2");

    Product product2 = new Product();
    product2.setProductId("456");
    product2.setProductName("test product 2");
    product2.setProductStructure(Product.STRUCTURE_HIERARCHICAL);
    product2.setProductType(productType2);

    FileTransferStatus status2 = new FileTransferStatus(reference2, 500, 200,
      product2);


    // Create a TransfersResource using the two FileTransferStatus instances to
    // generate TransferResource instances for the TransfersResource.
    List<TransferResource> resources = new ArrayList<>();
    resources.add(new TransferResource(product1, metadata1, status1));
    resources.add(new TransferResource(product2, metadata2, status2));
    TransfersResource resource = new TransfersResource("ALL", resources);

    // Generate the expected output.
    String expectedXml =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
      + "<transfers productId=\"ALL\">"
      + "<transfer>"
      + "<productName>" + product1.getProductName() + "</productName>"
      + "<productId>" + product1.getProductId() + "</productId>"
      + "<productTypeName>" + productType1.getName() + "</productTypeName>"
      + "<dataStoreReference>"
      +    reference1.getDataStoreReference()
      + "</dataStoreReference>"
      + "<origReference>" + reference1.getOrigReference() + "</origReference>"
      + "<mimeType>" + reference1.getMimeType().getName() + "</mimeType>"
      + "<fileSize>" + reference1.getFileSize() + "</fileSize>"
      + "<totalBytes>" + reference1.getFileSize() + "</totalBytes>"
      + "<bytesTransferred>"
      +    status1.getBytesTransferred()
      + "</bytesTransferred>"
      + "<percentComplete>"
      +    status1.computePctTransferred() * 100
      + "</percentComplete>"
      + "<productReceivedTime>"
      +    metadata1.getAllValues().get(0)
      + "</productReceivedTime>"
      + "</transfer>"
      + "<transfer>"
      + "<productName>" + product2.getProductName() + "</productName>"
      + "<productId>" + product2.getProductId() + "</productId>"
      + "<productTypeName>" + productType2.getName() + "</productTypeName>"
      + "<dataStoreReference>"
      +    reference2.getDataStoreReference()
      + "</dataStoreReference>"
      + "<origReference>" + reference2.getOrigReference() + "</origReference>"
      + "<mimeType>" + reference2.getMimeType().getName() + "</mimeType>"
      + "<fileSize>" + reference2.getFileSize() + "</fileSize>"
      + "<totalBytes>" + reference2.getFileSize() + "</totalBytes>"
      + "<bytesTransferred>"
      +    status2.getBytesTransferred()
      + "</bytesTransferred>"
      + "<percentComplete>"
      +    status2.computePctTransferred() * 100
      + "</percentComplete>"
      + "<productReceivedTime>"
      +    metadata2.getAllValues().get(0)
      + "</productReceivedTime>"
      + "</transfer>"
      +"</transfers>";

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
