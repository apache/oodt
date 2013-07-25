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

package org.apache.oodt.cas.product.service.responders;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.product.service.resources.DatasetResource;
import org.apache.oodt.cas.product.service.resources.ProductResource;
import org.apache.oodt.cas.product.service.resources.ReferenceResource;
import org.apache.oodt.cas.product.service.resources.TransferResource;
import org.junit.Test;

/**
 * Implements tests for methods in the {@link ZipResponder} class.
 * @author rlaidlaw
 * @version $Revision$
 */
public class ZipResponderTest
{
  /**
   * Tests the status code and message of the {@link Response} returned by the
   * createResponse method when a {@link ReferenceResource} is supplied as the
   * argument.
   * @throws Exception (IOException) and this would be considered a test
   * failure, so it's propagated upwards for JUnit to handle
   */
  @Test
  public void testCreateResponseFlatReferenceResource() throws Exception
  {
    File workingDir = new File("./src/test/resources/filemgr/workingDir");

    Reference reference = new Reference();
    reference.setMimeType("text/plain");
    reference.setDataStoreReference("file:"
      + new File("./src/test/resources/filemgr/ingest/flat/test.txt")
        .getCanonicalPath());

    ReferenceResource resource = new ReferenceResource();
    resource.setReference(reference);
    resource.setWorkingDirPath(workingDir.getCanonicalPath());

    Responder responder = new ZipResponder();
    Response response = responder.createResponse(resource);
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      "application/zip", response.getMetadata().get("Content-Type").get(0));
    assertEquals("Incorrect content disposition in response",
      "attachment; filename=\"test.txt.zip\"",
      response.getMetadata().get("Content-Disposition").get(0));

    // Clean the working directory.
    FileUtils.cleanDirectory(workingDir);
  }



  /**
   * Tests the status code and message of the {@link Response} returned by the
   * createResponse method when a {@link ReferenceResource} is supplied as the
   * argument.
   * @throws Exception (IOException) and this would be considered a test
   * failure, so it's propagated upwards for JUnit to handle
   */
  @Test
  public void testCreateResponseHierarchicalReferenceResource() throws Exception
  {
    File workingDir = new File("./src/test/resources/filemgr/workingDir");

    Reference reference = new Reference();
    reference.setMimeType("text/plain");
    reference.setDataStoreReference("file:"
      + new File("./src/test/resources/filemgr/ingest/hierarchical/test")
        .getCanonicalPath());

    ReferenceResource resource = new ReferenceResource();
    resource.setReference(reference);
    resource.setWorkingDirPath(workingDir.getCanonicalPath());

    Responder responder = new ZipResponder();
    Response response = responder.createResponse(resource);
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      "application/zip", response.getMetadata().get("Content-Type").get(0));
    assertEquals("Incorrect content disposition in response",
      "attachment; filename=\"test.zip\"",
      response.getMetadata().get("Content-Disposition").get(0));

    // Clean the working directory.
    FileUtils.cleanDirectory(workingDir);
  }



  /**
   * Tests the status code and message of the {@link Response} returned by the
   * createResponse method when a {@link ProductResource} is supplied as the
   * argument.
   * @throws Exception (FileNotFoundException, IOException, ZipException) and
   * any of these would be considered test failures, so they're propagated
   * upwards for JUnit to handle
   */
  @Test
  public void testCreateResponseFlatProductResource() throws Exception
  {
    File workingDir = new File("./src/test/resources/filemgr/workingDir");

    // Create a reference.
    File file = new File("./src/test/resources/filemgr/ingest/flat/test.txt");
    Reference reference = new Reference();
    reference.setMimeType("text/plain");
    reference.setDataStoreReference("file:" + file.getCanonicalPath());
    List<Reference> references = new ArrayList<Reference>();
    references.add(reference);

    // Create metadata for the product.
    FileInputStream fis = new FileInputStream(
      "./src/test/resources/filemgr/ingest/flat/test.txt.met");
    Metadata metadata = new SerializableMetadata(fis);
    fis.close();
    metadata.addMetadata(CoreMetKeys.FILE_LOCATION,
      new File("./src/test/resources/filemgr/ingest/flat").getCanonicalPath());
    metadata.addMetadata(CoreMetKeys.FILENAME, "test.txt");
    metadata.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
    metadata.addMetadata(CoreMetKeys.PRODUCT_STRUCTURE, Product.STRUCTURE_FLAT);

    // Create a product and add the reference.
    Product product = new Product();
    product.setProductName(file.getName());
    product.setProductReferences(references);

    // Add the product and metadata to the product resource.
    ProductResource resource = new ProductResource();
    resource.setProduct(product);
    resource.setMetadata(metadata);
    resource.setWorkingDirPath(workingDir.getCanonicalPath());

    // Test the response.
    Responder responder = new ZipResponder();
    Response response = responder.createResponse(resource);
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      "application/zip", response.getMetadata().get("Content-Type").get(0));
    assertEquals("Incorrect content disposition in response",
      "attachment; filename=\"test.txt.zip\"",
      response.getMetadata().get("Content-Disposition").get(0));

    // Clean the working directory.
    FileUtils.cleanDirectory(workingDir);
  }



  /**
   * Tests the status code and message of the {@link Response} returned by the
   * createResponse method when a {@link ProductResource} is supplied as the
   * argument.
   * @throws Exception (FileNotFoundException, IOException, ZipException) and
   * any of these would be considered test failures, so they're propagated
   * upwards for JUnit to handle
   */
  @Test
  public void testCreateResponseHierarchicalProductResource() throws Exception
  {
    File workingDir = new File("./src/test/resources/filemgr/workingDir");

    // Create references.
    String topDirPath = "./src/test/resources/filemgr/ingest/hierarchical/test";
    File topDir = new File(topDirPath);

    Reference reference = new Reference();
    reference.setMimeType("text/plain");
    reference.setDataStoreReference("file:" + topDir.getCanonicalPath());
    List<Reference> references = new ArrayList<Reference>();
    references.add(reference);

    // Create metadata for the product.
    FileInputStream fis = new FileInputStream(
      "./src/test/resources/filemgr/ingest/hierarchical/test.met");
    Metadata metadata = new SerializableMetadata(fis);
    fis.close();
    metadata.addMetadata(CoreMetKeys.FILE_LOCATION,
      new File("./src/test/resources/filemgr/ingest/hierarchical")
        .getCanonicalPath());
    metadata.addMetadata(CoreMetKeys.FILENAME, "test");
    metadata.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
    metadata.addMetadata(CoreMetKeys.PRODUCT_STRUCTURE,
      Product.STRUCTURE_HIERARCHICAL);

    // Create a product and add the references.
    Product product = new Product();
    product.setProductName(topDir.getName());
    product.setProductReferences(references);

    // Add the product and metadata to the product resource.
    ProductResource resource = new ProductResource();
    resource.setProduct(product);
    resource.setMetadata(metadata);
    resource.setWorkingDirPath(workingDir.getCanonicalPath());

    // Test the response.
    Responder responder = new ZipResponder();
    Response response = responder.createResponse(resource);
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      "application/zip", response.getMetadata().get("Content-Type").get(0));
    assertEquals("Incorrect content disposition in response",
      "attachment; filename=\"test.zip\"",
      response.getMetadata().get("Content-Disposition").get(0));

    // Clean the working directory.
    FileUtils.cleanDirectory(workingDir);
  }



  /**
   * Tests the status code and message of the {@link Response} returned by the
   * createResponse method when a {@link DatasetResource} is supplied as the
   * argument.
   * @throws Exception (FileNotFoundException, IOException, ZipException) and
   * any of these would be considered test failures, so they're propagated
   * upwards for JUnit to handle
   */
  @Test
  public void testCreateResponseDatasetResource() throws Exception
  {
    File workingDir = new File("./src/test/resources/filemgr/workingDir");

    // Create a product type.
    ProductType productType = new ProductType();
    productType.setName("GenericFile");
    productType.setProductTypeId("urn:oodt:GenericFile");


    // Create a hierarchical product.
    String topDirPath = "./src/test/resources/filemgr/ingest/hierarchical/test";
    File topDir = new File(topDirPath);

    Reference hRef = new Reference();
    hRef.setMimeType("text/plain");
    hRef.setDataStoreReference("file:" + topDir.getCanonicalPath());
    List<Reference> hRefs = new ArrayList<Reference>();
    hRefs.add(hRef);

    FileInputStream hfis = new FileInputStream(
      "./src/test/resources/filemgr/ingest/hierarchical/test.met");
    Metadata hMeta = new SerializableMetadata(hfis);
    hfis.close();
    hMeta.addMetadata(CoreMetKeys.FILE_LOCATION,
      new File("./src/test/resources/filemgr/ingest/hierarchical")
        .getCanonicalPath());
    hMeta.addMetadata(CoreMetKeys.FILENAME, "test");
    hMeta.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
    hMeta.addMetadata(CoreMetKeys.PRODUCT_STRUCTURE,
      Product.STRUCTURE_HIERARCHICAL);

    Product hProd = new Product();
    hProd.setProductName(topDir.getName());
    hProd.setProductReferences(hRefs);


    // Create a flat product.
    File file = new File("./src/test/resources/filemgr/ingest/flat/test.txt");
    Reference fRef = new Reference();
    fRef.setMimeType("text/plain");
    fRef.setDataStoreReference("file:" + file.getCanonicalPath());
    List<Reference> fRefs = new ArrayList<Reference>();
    fRefs.add(fRef);

    FileInputStream ffis = new FileInputStream(
      "./src/test/resources/filemgr/ingest/flat/test.txt.met");
    Metadata fMeta = new SerializableMetadata(ffis);
    ffis.close();
    fMeta.addMetadata(CoreMetKeys.FILE_LOCATION,
      new File("./src/test/resources/filemgr/ingest/flat").getCanonicalPath());
    fMeta.addMetadata(CoreMetKeys.FILENAME, "test.txt");
    fMeta.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
    fMeta.addMetadata(CoreMetKeys.PRODUCT_STRUCTURE, Product.STRUCTURE_FLAT);

    Product fProd = new Product();
    fProd.setProductName(file.getName());
    fProd.setProductReferences(fRefs);


    // Create a dataset resource and add the products to it.
    DatasetResource resource = new DatasetResource();
    resource.setProductType(productType);
    resource.setWorkingDirPath(workingDir.getCanonicalPath());
    String productDirPath = resource.getWorkingDirPath() + "/"
      + productType.getName();

    List<ProductResource> resources = new ArrayList<ProductResource>();
    resources.add(new ProductResource(hProd, hMeta, productDirPath));
    resources.add(new ProductResource(fProd, fMeta, productDirPath));
    resource.setProductResources(resources);

    // Test the zip response for the dataset product.
    Responder responder = new ZipResponder();
    Response response = responder.createResponse(resource);
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      "application/zip", response.getMetadata().get("Content-Type").get(0));
    assertEquals("Incorrect content disposition in response",
      "attachment; filename=\"GenericFile.zip\"",
      response.getMetadata().get("Content-Disposition").get(0));

    // Clean the working directory.
    FileUtils.cleanDirectory(workingDir);
  }



  /**
   * Tests the status code and message of the {@link Response} returned by the
   * createResponse method when a {@link TransferResource} is supplied as the
   * argument.
   */
  @Test
  public void testCreateResponseTransferResource()
  {
    Responder responder = new ZipResponder();
    Response response = responder.createResponse(new TransferResource());
    assertEquals("Incorrect response status.", 400, response.getStatus());
    assertEquals("Incorrect response message.",
      "Format not valid for this resource type.",
      response.getEntity().toString());
  }
}
