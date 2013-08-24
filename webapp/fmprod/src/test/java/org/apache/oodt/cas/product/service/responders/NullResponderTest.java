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

import javax.ws.rs.core.Response;

import org.apache.oodt.cas.product.service.resources.DatasetResource;
import org.apache.oodt.cas.product.service.resources.ProductResource;
import org.apache.oodt.cas.product.service.resources.ReferenceResource;
import org.apache.oodt.cas.product.service.resources.TransferResource;
import org.junit.Test;

/**
 * Implements tests for methods in the {@link NullResponder} class.
 * @author rlaidlaw
 * @version $Revision$
 */
public class NullResponderTest
{
  /**
   * Tests the status code of the {@link Response} returned by the
   * {@link NullResponder#createResponse createResponse} method when a
   * {@link ReferenceResource} is supplied as the argument.
   */
  @Test
  public void testCreateResponseReferenceResource()
  {
    Responder responder = new NullResponder();
    Response response = responder.createResponse(new ReferenceResource());
    assertEquals("Incorrect response status.", 400, response.getStatus());
    assertEquals("Incorrect response message.",
        "Format not specified.", response.getEntity().toString());
  }



  /**
   * Tests the status code of the {@link Response} returned by the
   * {@link NullResponder#createResponse createResponse} method when a
   * {@link ProductResource} is supplied as the argument.
   */
  @Test
  public void testCreateResponseProductResource()
  {
    Responder responder = new NullResponder();
    Response response = responder.createResponse(new ProductResource());
    assertEquals("Incorrect response status.", 400, response.getStatus());
    assertEquals("Incorrect response message.",
        "Format not specified.", response.getEntity().toString());
  }



  /**
   * Tests the status code of the {@link Response} returned by the
   * {@link NullResponder#createResponse createResponse} method when a
   * {@link DatasetResource} is supplied as the argument.
   */
  @Test
  public void testCreateResponseDatasetResource()
  {
    Responder responder = new NullResponder();
    Response response = responder.createResponse(new DatasetResource());
    assertEquals("Incorrect response status.", 400, response.getStatus());
    assertEquals("Incorrect response message.",
        "Format not specified.", response.getEntity().toString());
  }



  /**
   * Tests the status code of the {@link Response} returned by the
   * {@link NullResponder#createResponse createResponse} method when a
   * {@link TransferResource} is supplied as the argument.
   */
  @Test
  public void testCreateResponseTransferResource()
  {
    Responder responder = new NullResponder();
    Response response = responder.createResponse(new TransferResource());
    assertEquals("Incorrect response status.", 400, response.getStatus());
    assertEquals("Incorrect response message.",
        "Format not specified.", response.getEntity().toString());
  }
}
