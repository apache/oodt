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
import java.util.HashMap;
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
 * Implements tests for methods in the {@link TransferResource} class.
 * @author rlaidlaw
 * @version $Revision$
 */
public class TransferResourceTest
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
    // Create a TransferResource using ProductType, Product, Metadata, Reference
    // and FileTransferStatus instances.
    HashMap<String, Object> metadataEntries = new HashMap<>();
    metadataEntries.put("CAS.ProductReceivedTime", "2013-09-12T16:25:50.662Z");
    Metadata metadata = new Metadata();
    metadata.addMetadata(metadataEntries);

    Reference reference = new Reference("original", "dataStore", 1000,
      new MimeTypes().forName("text/plain"));

    ProductType productType = new ProductType("1", "GenericFile", "test type",
      "repository", "versioner");

    Product product = new Product();
    product.setProductId("123");
    product.setProductName("test product");
    product.setProductStructure(Product.STRUCTURE_FLAT);
    product.setProductType(productType);

    FileTransferStatus status = new FileTransferStatus(reference, 1000, 100,
      product);

    TransferResource resource = new TransferResource(product, metadata, status);


    // Generate the expected output.
    String expectedXml =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
      + "<transfer>"
      + "<productName>" + product.getProductName() + "</productName>"
      + "<productId>" + product.getProductId() + "</productId>"
      + "<productTypeName>" + productType.getName() + "</productTypeName>"
      + "<dataStoreReference>"
      +   reference.getDataStoreReference()
      + "</dataStoreReference>"
      + "<origReference>"
      +   reference.getOrigReference()
      + "</origReference>"
      + "<mimeType>" + reference.getMimeType().getName() + "</mimeType>"
      + "<fileSize>" + reference.getFileSize() + "</fileSize>"
      + "<totalBytes>" + reference.getFileSize() + "</totalBytes>"
      + "<bytesTransferred>"
      +    status.getBytesTransferred()
      + "</bytesTransferred>"
      + "<percentComplete>"
      +    status.computePctTransferred() * 100
      + "</percentComplete>"
      + "<productReceivedTime>"
      +    metadata.getAllValues().get(0)
      + "</productReceivedTime>"
      + "</transfer>";

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
