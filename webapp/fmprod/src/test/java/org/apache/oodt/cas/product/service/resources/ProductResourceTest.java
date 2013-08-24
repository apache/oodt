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

package org.apache.oodt.cas.product.service.resources;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.local.LocalConduit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Implements tests for methods in the {@link ProductResource} class.
 * @author rlaidlaw
 * @version $Revision$
 */
public class ProductResourceTest extends ResourceTestBase
{
  /**
   * Starts up the web server.
   */
  @BeforeClass
  public static void setUpBeforeClass()
  {
    startWebServer(new ProductResource());
  }



  /**
   * Shuts down the web server.
   */
  @AfterClass
  public static void tearDownAfterClass()
  {
    stopWebServer();
  }



  /**
   * Tests the response for a request to the 'product' URL with query
   * parameters specifying a 'file' response format.
   */
  @Test
  public void testGetFileResponseFlatProduct()
  {
    String contentType = MediaType.TEXT_PLAIN;
    WebClient client = WebClient.create(SERVER_URL);
    WebClient.getConfig(client).getRequestContext()
      .put(LocalConduit.DIRECT_DISPATCH, Boolean.TRUE);
    client.accept(contentType);
    client.path("/product");
    client.query("productID", getGenericFileFlatProductId());
    client.query("refIndex", "0");
    client.query("format", "file");

    Response response = client.get();
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      contentType, response.getMetadata().get("Content-Type").get(0));
    assertEquals("Incorrect content disposition in response",
      "attachment; filename=\"test.txt\"",
      response.getMetadata().get("Content-Disposition").get(0));
  }



  /**
   * Tests the response for a request to the 'product' URL with query
   * parameters specifying a 'file' response format.
   */
  @Test
  public void testGetFileResponseHierarchicalProduct()
  {
    String contentType = MediaType.TEXT_PLAIN;
    WebClient client = WebClient.create(SERVER_URL);
    WebClient.getConfig(client).getRequestContext()
      .put(LocalConduit.DIRECT_DISPATCH, Boolean.TRUE);
    client.accept(contentType);
    client.path("/product");
    client.query("productID", getGenericFileHierarchicalProductId());
    client.query("refIndex", "1");
    client.query("format", "file");

    Response response = client.get();
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      contentType, response.getMetadata().get("Content-Type").get(0));
  }



  /**
   * Tests the response for a request to the 'product' URL with query
   * parameters specifying a 'zip' response format.
   */
  @Test
  public void testGetZipResponse()
  {
    WebClient client = WebClient.create(SERVER_URL);
    WebClient.getConfig(client).getRequestContext()
      .put(LocalConduit.DIRECT_DISPATCH, Boolean.TRUE);
    client.accept("application/zip");
    client.path("/product");
    client.query("productID", getGenericFileFlatProductId());
    client.query("format", "zip");

    Response response = client.get();
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      "application/zip", response.getMetadata().get("Content-Type").get(0));
    assertEquals("Incorrect content disposition in response",
      "attachment; filename=\"test.txt.zip\"",
      response.getMetadata().get("Content-Disposition").get(0));
  }



  /**
   * Tests the response for a request to the 'data' URL with query
   * parameters specifying a 'zip' response format.
   */
  @Test
  public void testGetZipResponseDataUrl()
  {
    WebClient client = WebClient.create(SERVER_URL);
    WebClient.getConfig(client).getRequestContext()
      .put(LocalConduit.DIRECT_DISPATCH, Boolean.TRUE);
    client.accept("application/zip");
    client.path("/data");
    client.query("productID", getGenericFileFlatProductId());
    client.query("format", "zip");

    Response response = client.get();
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      "application/zip", response.getMetadata().get("Content-Type").get(0));
    assertEquals("Incorrect content disposition in response",
      "attachment; filename=\"test.txt.zip\"",
      response.getMetadata().get("Content-Disposition").get(0));
  }
}
