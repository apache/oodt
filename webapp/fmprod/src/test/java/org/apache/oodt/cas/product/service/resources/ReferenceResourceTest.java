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

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.local.LocalConduit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Implements tests for methods in the {@link ReferenceResource} class.
 * @author rlaidlaw
 * @version $Revision$
 */
public class ReferenceResourceTest extends ResourceTestBase
{
  /**
   * Starts up the web server.
   */
  @BeforeClass
  public static void setUpBeforeClass()
  {
    startWebServer(new ReferenceResource());
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
   * Tests the response for a request to the 'reference' URL with query
   * parameters specifying a 'file' response format.
   */
  @Test
  public void testGetFileResponseFlatProduct()
  {
    WebClient client = WebClient.create(SERVER_URL);
    WebClient.getConfig(client).getRequestContext()
      .put(LocalConduit.DIRECT_DISPATCH, Boolean.TRUE);
    client.accept("text/plain");
    client.path("/reference");
    client.query("productID", getGenericFileFlatProductId());
    client.query("refIndex", "0");
    client.query("format", "file");

    Response response = client.get();
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      "text/plain", response.getMetadata().get("Content-Type").get(0));
    assertEquals("Incorrect content disposition in response",
      "attachment; filename=\"test.txt\"",
      response.getMetadata().get("Content-Disposition").get(0));
  }



  /**
   * Tests the response for a request to the 'reference' URL with query
   * parameters specifying a 'file' response format.
   */
  @Test
  public void testGetFileResponseHierarchicalProduct()
  {
    WebClient client = WebClient.create(SERVER_URL);
    WebClient.getConfig(client).getRequestContext()
      .put(LocalConduit.DIRECT_DISPATCH, Boolean.TRUE);
    client.accept("text/plain");
    client.path("/reference");
    client.query("productID", getGenericFileHierarchicalProductId());
    client.query("refIndex", "1");
    client.query("format", "file");

    Response response = client.get();
    assertEquals("Incorrect response status.", 200, response.getStatus());
    assertEquals("Incorrect content type in response.",
      "text/plain", response.getMetadata().get("Content-Type").get(0));
  }



  /**
   * Tests the default response for a request to the 'reference' URL where no
   * query parameters are specified other than the product ID.
   */
  @Test
  public void testGetDefaultResponseHierarchicalProduct()
  {
    WebClient client = WebClient.create(SERVER_URL);
    WebClient.getConfig(client).getRequestContext()
      .put(LocalConduit.DIRECT_DISPATCH, Boolean.TRUE);
    client.accept("text/plain");
    client.path("/reference");
    client.query("productID", getGenericFileHierarchicalProductId());

    Response response = client.get();
    assertEquals("Incorrect response status.", 400, response.getStatus());
    assertEquals("Incorrect response message.",
      "Format not specified.",
      response.getEntity().toString());
  }



  /**
   * Tests the response for a request to the 'reference' URL with query
   * parameters specifying a 'zip' response format.
   */
  @Test
  public void testGetZipResponse()
  {
    WebClient client = WebClient.create(SERVER_URL);
    WebClient.getConfig(client).getRequestContext()
      .put(LocalConduit.DIRECT_DISPATCH, Boolean.TRUE);
    client.accept("application/zip");
    client.path("/reference");
    client.query("productID", getGenericFileFlatProductId());
    client.query("refIndex", "0");
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
