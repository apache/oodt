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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.product.service.exceptions.NotFoundException;
import org.apache.oodt.cas.product.service.responders.Responder;
import org.apache.oodt.cas.product.service.responders.ResponderFactory;

/**
 * A JAX-RS resource representing a {@link Product}.
 * @author rlaidlaw
 * @version $Revision$
 */
public class ProductResource extends Resource
{
  private static final Logger LOGGER = Logger.getLogger(ProductResource.class
    .getName());

  // The product associated with the resource.
  private Product product;

  // The metadata associated with the resource.
  private Metadata metadata;

  // The index of the reference within the product's reference list (default 0).
  private int index;



  /**
   * Default constructor.
   */
  public ProductResource()
  {
  }

  /**
   * This constructor can be used by DatasetResources to create
   * ProductResource instances.
   * @param product the product associated with the resource
   * @param metadata the metadata associated with the resource
   * @param workingDir the working directory for streaming files
   */
  public ProductResource(Product product, Metadata metadata,
    File workingDir)
  {
    this.product = product;
    this.metadata = metadata;
    setWorkingDir(workingDir);
  }



  /**
   * Gets an HTTP response for a {@link Product} from the file manager.
   * @param productID the ID of the product
   * @param refIndex the index of a specific reference within the product's list
   * of references
   * @param format the requested format of the response
   * @return HTTP response containing the requested product
   */
  @GET
  @Path("/{path: product|data}")
  public Response getResponse(@QueryParam("productID") String productID,
    @QueryParam("refIndex") int refIndex,
    @QueryParam("format") String format)
  {
    try
    {
      setWorkingDir(getContextWorkingDir());
      XmlRpcFileManagerClient client = getContextClient();
      product = client.getProductById(productID);
      product.setProductReferences(client.getProductReferences(product));
      metadata = client.getMetadata(product);
      index = refIndex;

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
   * Gets the product.
   * @return the product
   */
  public Product getProduct()
  {
    return product;
  }



  /**
   * Gets the metadata.
   * @return the metadata
   */
  public Metadata getMetadata()
  {
    return metadata;
  }



  /**
   * Gets the reference index.
   * @return the index
   */
  public int getIndex()
  {
    return index;
  }



  /**
   * Sets the product.
   * @param product the product to set
   */
  public void setProduct(Product product)
  {
    this.product = product;
  }



  /**
   * Sets the metadata.
   * @param metadata the metadata to set
   */
  public void setMetadata(Metadata metadata)
  {
    this.metadata = metadata;
  }



  /**
   * Sets the index.
   * @param index the index to set
   */
  public void setIndex(int index)
  {
    this.index = index;
  }
}
