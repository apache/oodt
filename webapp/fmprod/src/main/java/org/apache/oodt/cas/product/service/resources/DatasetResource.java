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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.product.service.exceptions.BadRequestException;
import org.apache.oodt.cas.product.service.exceptions.NotFoundException;
import org.apache.oodt.cas.product.service.responders.Responder;
import org.apache.oodt.cas.product.service.responders.ResponderFactory;

/**
 * A JAX-RS resource representing a dataset - a set of {@link Product} objects
 * maintained by the file manager.
 * @author rlaidlaw
 * @version $Revision$
 */
@Path("/dataset")
public class DatasetResource extends Resource
{
  private static final Logger LOGGER = Logger.getLogger(DatasetResource.class
    .getName());

  // The product type associated with the resource.
  private ProductType productType;

  // The list of product resources associated with the resource.
  private List<ProductResource> productResources =
    new ArrayList<ProductResource>();



  /**
   * Gets an HTTP response for a set of {@link Product} objects from the file
   * manager.
   * @param typeID the ID of the {@link ProductType} for the data set
   * @param format the requested MIME type for the {@link Response}
   * @return HTTP response containing a set of products
   */
  @GET
  public Response getResponse(@QueryParam("typeID") String typeID,
    @QueryParam("format") String format)
  {
    if (typeID == null)
    {
      throw new BadRequestException("The typeID parameter is required.");
    }

    try
    {
      setWorkingDir(getContextWorkingDir());
      XmlRpcFileManagerClient client = getContextClient();
      productType = client.getProductTypeById(typeID);
      String productDirPath = getContextWorkingDir().getCanonicalPath()
        + "/" + productType.getName();

      // Add all products of the chosen type to the dataset.
      for (Product product : client.getProductsByProductType(productType))
      {
        product.setProductReferences(client.getProductReferences(product));
        productResources.add(new ProductResource(product,
          client.getMetadata(product), new File(productDirPath)));
      }

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
   * Gets the dataset's product type.
   * @return the product type
   */
  public ProductType getProductType()
  {
    return productType;
  }



  /**
   * Gets the dataset's list of product resources.
   * @return the product resources
   */
  public List<ProductResource> getProductResources()
  {
    return productResources;
  }



  /**
   * Sets the productType.
   * @param productType the productType to set
   */
  public void setProductType(ProductType productType)
  {
    this.productType = productType;
  }



  /**
   * Sets the productResources.
   * @param productResources the productResources to set
   */
  public void setProductResources(List<ProductResource> productResources)
  {
    this.productResources = productResources;
  }
}
