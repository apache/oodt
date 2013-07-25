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

import javax.ws.rs.core.Response;

import org.apache.oodt.cas.product.service.resources.DatasetResource;
import org.apache.oodt.cas.product.service.resources.ProductResource;
import org.apache.oodt.cas.product.service.resources.ReferenceResource;
import org.apache.oodt.cas.product.service.resources.TransferResource;

/**
 * Specifies behavior for an HTTP response generator for different resources.
 * @author rlaidlaw
 * @version $Revision$
 */
public interface Responder
{
  /**
   * Creates an HTTP response for a {@link ReferenceResource}.
   * @param resource the reference resource for which to create the response
   * @return the appropriate HTTP response for the given resource
   */
  Response createResponse(ReferenceResource resource);

  /**
   * Creates an HTTP response for a {@link ProductResource}.
   * @param resource the product resource for which to create the response
   * @return the appropriate HTTP response for the given resource
   */
  Response createResponse(ProductResource resource);

  /**
   * Creates an HTTP response for a {@link DatasetResource}.
   * @param resource the dataset resource for which to create the response
   * @return the appropriate HTTP response for the given resource
   */
  Response createResponse(DatasetResource resource);

  /**
   * Creates an HTTP response for a {@link TransferResource}.
   * @param resource the transfer resource for which to create the response
   * @return the appropriate HTTP response for the given resource
   */
  Response createResponse(TransferResource resource);
}
