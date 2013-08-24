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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * A resource representing information on data transfers for the file manager.
 * @author rlaidlaw
 * @version $Revision$
 */
@Path("/transfer")
public class TransferResource extends Resource
{
  // Additional response status constant not found in Response.Status class.
  private static final int RESPONSE_STATUS_NOT_IMPLEMENTED = 501;



  /**
   * Gets information about current transfers in progress.
   * @return HTTP response with information on current transfers
   */
  @GET
  public Response getResponse()
  {
    return Response.status(RESPONSE_STATUS_NOT_IMPLEMENTED)
      .entity("Transfer information via JAX-RS has not been implemented yet.")
      .build();
  }
}
