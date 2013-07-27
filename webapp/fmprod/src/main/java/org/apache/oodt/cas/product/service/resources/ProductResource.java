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

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.product.service.exceptions.NotFoundException;
import org.apache.oodt.cas.product.service.responders.Responder;
import org.apache.oodt.cas.product.service.responders.ResponderFactory;

/**
 * A JAX-RS resource representing a {@link Product}.
 * @author rlaidlaw
 * @version $Revision$
 */
public class ProductResource
{
  // The product associated with the resource.
  private Product product;

  // The metadata associated with the resource.
  private Metadata metadata;

  // The index of the reference within the product's reference list (default 0).
  private int index;

  // The path to the working directory used to store temporary files for
  // responses.
  private String workingDirPath;

  @Context
  private ServletContext context;



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
   * @param workingDirPath the working directory for streaming files
   */
  public ProductResource(Product product, Metadata metadata,
    String workingDirPath)
  {
    this.product = product;
    this.metadata = metadata;
    this.workingDirPath = workingDirPath;
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
      setWorkingDirPath(PathUtils.replaceEnvVariables(
        context.getInitParameter("filemgr.working.dir")));
      XmlRpcFileManagerClient client = new XmlRpcFileManagerClient(
        new URL(PathUtils.replaceEnvVariables(
          context.getInitParameter("filemgr.url"))));
      product = client.getProductById(productID);
      product.setProductReferences(client.getProductReferences(product));
      metadata = client.getMetadata(product);
      index = refIndex;

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
   * Gets the working directory path.
   * @return the workingDirPath
   */
  public String getWorkingDirPath()
  {
    return workingDirPath;
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



  /**
   * Sets the working directory path.
   * @param workingDirPath the workingDirPath to set
   */
  public void setWorkingDirPath(String workingDirPath)
  {
    this.workingDirPath = workingDirPath;
  }



  /**
   * Sets the servlet context.
   * @param context the servlet context to set.
   */
  public void setServletContext(ServletContext context)
  {
    this.context = context;
  }
}
