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
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.product.service.resources.DatasetResource;
import org.apache.oodt.cas.product.service.resources.ProductResource;
import org.apache.oodt.cas.product.service.resources.ReferenceResource;
import org.apache.oodt.cas.product.service.resources.TransferResource;
import org.junit.Test;

/**
 * Implements tests for methods in the {@link FileResponder} class.
 * @author rlaidlaw
 * @version $Revision$
 */
public class FileResponderTest
{
  /**
   * Tests the {@link Response} returned by the createResponse method when a
   * {@link ReferenceResource} is supplied as the argument, where the reference
   * has both a file extension and a MIME type.
   * @throws Exception (IOException) if getCanonicalPath fails.  This would be
   * considered a test failure, so it is propagated upwards for JUnit to handle
   */
  @Test
  public void testCreateResponseReferenceResourceWithMimeTypeAndExtension()
    throws Exception
  {
    Reference reference = new Reference();
    reference.setMimeType("text/plain");
    reference.setDataStoreReference("file:"
      + new File("./src/test/resources/filemgr/ingest/flat/test.txt")
        .getCanonicalPath());

    ReferenceResource resource = new ReferenceResource();
    resource.setReference(reference);

    Responder responder = new FileResponder();
    Response response = responder.createResponse(resource);
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      "text/plain", response.getMetadata().get("Content-Type").get(0));
    assertEquals("Incorrect content disposition in response",
      "attachment; filename=\"test.txt\"",
      response.getMetadata().get("Content-Disposition").get(0));
  }



  /**
   * Tests the {@link Response} returned by the createResponse method when a
   * {@link ReferenceResource} is supplied as the argument, where the reference
   * has a file extension but no MIME type.
   * @throws Exception (IOException) if getCanonicalPath fails.  This would be
   * considered a test failure, so it is propagated upwards for JUnit to handle
   */
  @Test
  public void testCreateResponseReferenceResourceWithExtensionNoMimeType()
    throws Exception
  {
    Reference reference = new Reference();
    reference.setDataStoreReference("file:"
      + new File("./src/test/resources/filemgr/ingest/flat/test.txt")
        .getCanonicalPath());

    ReferenceResource resource = new ReferenceResource();
    resource.setReference(reference);

    Responder responder = new FileResponder();
    Response response = responder.createResponse(resource);
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      "text/plain", response.getMetadata().get("Content-Type").get(0));
    assertEquals("Incorrect content disposition in response",
      "attachment; filename=\"test.txt\"",
      response.getMetadata().get("Content-Disposition").get(0));
  }



  /**
   * Tests the {@link Response} returned by the createResponse method when a
   * {@link ReferenceResource} is supplied as the argument, where the reference
   * has a MIME type but no file extension.
   * @throws Exception (IOException) if getCanonicalPath fails.  This would be
   * considered a test failure, so it is propagated upwards for JUnit to handle
   */
  @Test
  public void testCreateResponseReferenceResourceWithMimeTypeNoExtension()
    throws Exception
  {
    Reference reference = new Reference();
    reference.setMimeType("text/plain");
    reference.setDataStoreReference("file:"
      + new File("./src/test/resources/filemgr/ingest/flat/test")
        .getCanonicalPath());

    ReferenceResource resource = new ReferenceResource();
    resource.setReference(reference);

    Responder responder = new FileResponder();
    Response response = responder.createResponse(resource);
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      "text/plain", response.getMetadata().get("Content-Type").get(0));
    assertEquals("Incorrect content disposition in response",
      "attachment; filename=\"test\"",
      response.getMetadata().get("Content-Disposition").get(0));
  }



  /**
   * Tests the {@link Response} returned by the createResponse method when a
   * {@link ReferenceResource} is supplied as the argument, where the reference
   * has neither a MIME type nor a file extension.
   * @throws Exception (IOException) if getCanonicalPath fails.  This would be
   * considered a test failure, so it is propagated upwards for JUnit to handle
   */
  @Test
  public void testCreateResponseReferenceResourceNoMimeTypeOrExtension()
    throws Exception
  {
    Reference reference = new Reference();
    reference.setDataStoreReference("file:"
      + new File("./src/test/resources/filemgr/ingest/flat/test")
        .getCanonicalPath());

    ReferenceResource resource = new ReferenceResource();
    resource.setReference(reference);

    Responder responder = new FileResponder();
    Response response = responder.createResponse(resource);
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      "application/octet-stream",
      response.getMetadata().get("Content-Type").get(0));
    assertEquals("Incorrect content disposition in response",
      "attachment; filename=\"test\"",
      response.getMetadata().get("Content-Disposition").get(0));
  }



  /**
   * Tests the status code and content of the {@link Response} returned by the
   * createResponse method when a {@link ProductResource} is supplied as the
   * argument.
   * @throws Exception (IOException) if getCanonicalPath fails.  This would be
   * considered a test failure, so it is propagated upwards for JUnit to handle
   */
  @Test
  public void testCreateResponseProductResource() throws Exception
  {
    String topDirPath = "./src/test/resources/filemgr/ingest/hierarchical/test";
    Reference reference1 = new Reference();
    reference1.setMimeType("text/plain");
    reference1.setDataStoreReference("file:" + new File(topDirPath +
      "/file.txt").getCanonicalPath());

    Reference reference2 = new Reference();
    reference2.setMimeType("text/plain");
    reference2.setDataStoreReference("file:" + new File(topDirPath +
      "/subdirectory/sub-file.txt").getCanonicalPath());

    List<Reference> references = new ArrayList<Reference>();
    references.add(reference1);
    references.add(reference2);

    Product product = new Product();
    product.setProductReferences(references);

    ProductResource resource = new ProductResource();
    resource.setProduct(product);
    resource.setIndex(0);

    Responder responder = new FileResponder();
    Response response = responder.createResponse(resource);

    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      "text/plain", response.getMetadata().get("Content-Type").get(0));
    assertEquals("Incorrect content disposition in response",
      "attachment; filename=\"file.txt\"",
      response.getMetadata().get("Content-Disposition").get(0));

    resource.setIndex(1);
    response = responder.createResponse(resource);
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      "text/plain", response.getMetadata().get("Content-Type").get(0));
    assertEquals("Incorrect content disposition in response",
      "attachment; filename=\"sub-file.txt\"",
      response.getMetadata().get("Content-Disposition").get(0));
  }



  /**
   * Tests the status code and message of the {@link Response} returned by the
   * createResponse method when a {@link ProductResource} is supplied as the
   * argument and the requested reference does not exist.
   */
  @Test
  public void testCreateResponseProductResourceNoReferences()
  {
    List<Reference> references = new ArrayList<Reference>();
    Product product = new Product();
    product.setProductReferences(references);

    ProductResource resource = new ProductResource();
    resource.setProduct(product);
    resource.setIndex(-1);

    Responder responder = new FileResponder();
    Response response = responder.createResponse(resource);
    assertEquals("Incorrect response status.", 400, response.getStatus());
    assertEquals("Incorrect response message.",
      "Index out of range.", response.getEntity().toString());

    resource.setIndex(0);
    response = responder.createResponse(resource);
    assertEquals("Incorrect response status.", 400, response.getStatus());
    assertEquals("Incorrect response message.",
      "Index out of range.", response.getEntity().toString());
  }



  /**
   * Tests the status code and message of the {@link Response} returned by the
   * createResponse method when a {@link DatasetResource} is supplied as the
   * argument.
   */
  @Test
  public void testCreateResponseDatasetResource()
  {
    Responder responder = new FileResponder();
    Response response = responder.createResponse(new DatasetResource());
    assertEquals("Incorrect response status.", 400, response.getStatus());
    assertEquals("Incorrect response message.",
      "Format not valid for this resource type.",
      response.getEntity().toString());
  }



  /**
   * Tests the status code and message of the {@link Response} returned by the
   * createResponse method when a {@link TransferResource} is supplied as the
   * argument.
   */
  @Test
  public void testCreateResponseTransferResource()
  {
    Responder responder = new FileResponder();
    Response response = responder.createResponse(new TransferResource());
    assertEquals("Incorrect response status.", 400, response.getStatus());
    assertEquals("Incorrect response message.",
      "Format not valid for this resource type.",
      response.getEntity().toString());
  }
}
