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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.util.PathUtils;
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
public class DatasetResource
{
  // The product type associated with the resource.
  private ProductType productType;

  // The list of product resources associated with the resource.
  private List<ProductResource> productResources =
    new ArrayList<ProductResource>();

  // The path to the working directory used to store temporary files for
  // responses.
  private String workingDirPath;

  @Context
  private ServletContext context;



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
      XmlRpcFileManagerClient client = new XmlRpcFileManagerClient(
        new URL(PathUtils.replaceEnvVariables(
          context.getInitParameter("filemgr.url"))));
      productType = client.getProductTypeById(typeID);

      // Set a working directory to store product files.
      setWorkingDirPath(PathUtils.replaceEnvVariables(
        context.getInitParameter("filemgr.working.dir")));
      String productDirPath = getWorkingDirPath();
      productDirPath += productDirPath.endsWith("/") ? "" : "/";
      productDirPath += productType.getName();

      // Add all products of the chosen type to the dataset.
      for (Product product : client.getProductsByProductType(productType))
      {
        product.setProductReferences(client.getProductReferences(product));
        productResources.add(new ProductResource(product,
          client.getMetadata(product), productDirPath));
      }

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
   * Gets the working directory path.
   * @return the workingDirPath
   */
  public String getWorkingDirPath()
  {
    return workingDirPath;
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
   * @param context the servlet context to set
   */
  public void setServletContext(ServletContext context)
  {
    this.context = context;
  }
}
