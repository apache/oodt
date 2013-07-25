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

import static org.junit.Assert.*;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.local.LocalConduit;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.easymock.EasyMock;
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
  // The web server.
  private static Server server;



  /**
   * Starts a web server using the local transport protocol.  Uses a mock
   * servlet context to inject context parameters into the JAX-RS resource.
   */
  @BeforeClass
  public static void startWebServer()
  {
    // The JAX-RS resource to test.
    ProductResource resource = new ProductResource();

    // Create a web server for testing using local transport.
    JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
    sf.setTransportId(LocalTransportFactory.TRANSPORT_ID);
    sf.setServiceBean(resource);
    sf.setAddress("local://service");
    server = sf.create();

    // Use a mock servlet context for the resource.
    // This is done after creating the server to avoid being overwritten by
    // the server's default context.
    ServletContext mockContext = EasyMock.createNiceMock(ServletContext.class);
    EasyMock.expect(mockContext.getInitParameter("filemgr.url"))
      .andReturn(getFileManagerUrl()).anyTimes();
    EasyMock.expect(mockContext.getInitParameter("filemgr.working.dir"))
      .andReturn(getWorkingDirLocation()).anyTimes();
    EasyMock.replay(mockContext);
    resource.setServletContext(mockContext);
  }



  /**
   * Shuts down the web server.
   */
  @AfterClass
  public static void stopWebServer()
  {
    // Stop the server.
    server.stop();
    server.destroy();
  }



  /**
   * Tests the response for a request to the 'product' URL with query
   * parameters specifying a 'file' response format.
   */
  @Test
  public void testGetFileResponseFlatProduct()
  {
    String contentType = MediaType.TEXT_PLAIN;
    WebClient client = WebClient.create("local://service");
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
    WebClient client = WebClient.create("local://service");
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
    WebClient client = WebClient.create("local://service");
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
    WebClient client = WebClient.create("local://service");
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
