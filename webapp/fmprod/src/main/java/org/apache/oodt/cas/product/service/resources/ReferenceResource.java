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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.product.service.exceptions.NotFoundException;
import org.apache.oodt.cas.product.service.responders.Responder;
import org.apache.oodt.cas.product.service.responders.ResponderFactory;

/**
 * A JAX-RS resource representing a {@link Reference}.
 * @author rlaidlaw
 * @version $Revision$
 */
@Path("/reference")
public class ReferenceResource extends Resource
{
  private static final Logger LOGGER = Logger.getLogger(ReferenceResource.class
    .getName());

  // The reference associated with the resource.
  private Reference reference;



  /**
   * Gets an HTTP response for a {@link Reference} from a {@link Product} from
   * the file manager.
   * @param productID the ID of the product that the reference belongs to
   * @param refIndex the index of the reference within the product's list of
   * references
   * @param format the requested response format
   * @return a response representing the reference in the requested format
   */
  @GET
  public Response getResponse(@QueryParam("productID") String productID,
    @QueryParam("refIndex") int refIndex,
    @QueryParam("format") String format)
  {
    try
    {
      setWorkingDir(getContextWorkingDir());
      XmlRpcFileManagerClient client = getContextClient();
      Product product = client.getProductById(productID);
      List<Reference> references = client.getProductReferences(product);
      reference = references.get(refIndex);

      Responder responder = ResponderFactory.createResponder(format);
      return responder.createResponse(this);
    }
    catch (Exception e)
    {
      String message = "Unable to find the requested resource.";
      LOGGER.log(Level.FINE, message, e);
      throw new NotFoundException(message + " " + e.getMessage());
    }
  }



  /**
   * Gets the reference.
   * @return the reference
   */
  public Reference getReference()
  {
    return reference;
  }



  /**
   * Sets the reference.
   * @param reference the reference to set
   */
  public void setReference(Reference reference)
  {
    this.reference = reference;
  }
}
