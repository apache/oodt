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

import java.net.URL;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.product.service.exceptions.NotFoundException;
import org.apache.oodt.cas.product.service.responders.Responder;
import org.apache.oodt.cas.product.service.responders.ResponderFactory;

/**
 * A JAX-RS resource representing a {@link Reference}.
 * @author rlaidlaw
 * @version $Revision$
 */
@Path("/reference")
public class ReferenceResource
{
  // The reference associated with the resource.
  private Reference reference;

  // The path to the working directory used to store temporary files for
  // responses.
  private String workingDirPath;

  @Context
  private ServletContext context;



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
      setWorkingDirPath(PathUtils.replaceEnvVariables(
        context.getInitParameter("filemgr.working.dir")));
      XmlRpcFileManagerClient client = new XmlRpcFileManagerClient(
        new URL(PathUtils.replaceEnvVariables(
          context.getInitParameter("filemgr.url"))));
      Product product = client.getProductById(productID);
      List<Reference> references = client.getProductReferences(product);
      this.reference = references.get(refIndex);
      Responder responder = ResponderFactory.createResponder(format);
      return responder.createResponse(this);
    }
    catch (Exception e)
    {
      throw new NotFoundException("The requested resource could not be found. "
        + e.getMessage());
    }
  }



  /**
   * Gets the reference.
   *
   * @return the reference
   */
  public Reference getReference()
  {
    return reference;
  }



  /**
   * Gets the working directory path.
   * @return the workingDirPath
   */
  public String getWorkingDirPath()
  {
    return workingDirPath;
  }



  /**
   * Sets the servlet context.
   * @param context the servlet context to set.
   */
  public void setServletContext(ServletContext context)
  {
    this.context = context;
  }



  /**
   * Sets the reference.
   * @param reference the reference to set
   */
  public void setReference(Reference reference)
  {
    this.reference = reference;
  }



  /**
   * Sets the working directory path.
   * @param workingDirPath the workingDirPath to set
   */
  public void setWorkingDirPath(String workingDirPath)
  {
    this.workingDirPath = workingDirPath;
  }
}
