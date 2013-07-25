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
 * Generates default HTTP responses where the format is not specified.
 * @author rlaidlaw
 * @version $Revision$
 */
public class NullResponder implements Responder
{

  @Override
  public Response createResponse(ReferenceResource resource)
  {
    return Response.status(Response.Status.BAD_REQUEST)
      .entity("Format not specified.").build();
  }

  @Override
  public Response createResponse(ProductResource resource)
  {
    return Response.status(Response.Status.BAD_REQUEST)
      .entity("Format not specified.").build();
  }

  @Override
  public Response createResponse(DatasetResource resource)
  {
    return Response.status(Response.Status.BAD_REQUEST)
      .entity("Format not specified.").build();
  }

  @Override
  public Response createResponse(TransferResource resource)
  {
    return Response.status(Response.Status.BAD_REQUEST)
      .entity("Format not specified.").build();
  }
}
