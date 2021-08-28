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

package org.apache.oodt.cas.product.jaxrs.services;

import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.product.exceptions.CasProductException;
import org.apache.oodt.cas.product.jaxrs.exceptions.BadRequestException;
import org.apache.oodt.cas.product.jaxrs.exceptions.NotFoundException;
import org.apache.oodt.cas.product.jaxrs.resources.DatasetResource;
import org.apache.oodt.cas.product.jaxrs.resources.ProductResource;
import org.apache.oodt.cas.product.jaxrs.resources.ReferenceResource;
import org.apache.oodt.cas.product.jaxrs.resources.TransferResource;
import org.apache.oodt.cas.product.jaxrs.resources.TransfersResource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 * Service class that handles HTTP requests and returns file manager entities
 * such as {@link Reference references} and {@link Product products} as
 * JAX-RS resources converted to different formats.
 * @author rlaidlaw
 * @version $Revision$
 */
public class CasProductJaxrsService
{
  private static final Logger LOGGER = LoggerFactory.getLogger(CasProductJaxrsService.class);

  // The servlet context, which is used to retrieve context parameters.
  @Context
  private ServletContext context;



  /**
   * Gets an HTTP response that represents a {@link Reference} from a {@link
   * Product} from the file manager.
   * @param productId the ID of the product that the reference belongs to
   * @param refIndex the index of the reference within the product's list of
   * references
   * @return an HTTP response that represents a {@link Reference} from a {@link
   * Product} from the file manager
   */
  @GET
  @Path("reference")
  @Produces({"application/octet-stream", "application/xml", "application/json",
    "application/atom+xml", "application/rdf+xml", "application/rss+xml",
    "application/zip"})
  public ReferenceResource getReference(
    @QueryParam("productId") String productId,
    @QueryParam("refIndex") int refIndex)
  {
    if (productId == null || productId.trim().equals(""))
    {
      throw new BadRequestException("This URL requires a productId query "
        + "parameter with a product ID value, "
        + "e.g. /reference?productId=1787a257-df87-11e2-8a2d-e3f6264e86c5");
    }

    try
    {
      FileManagerClient client = getContextClient();
      Product product = client.getProductById(productId);
      List<Reference> references = client.getProductReferences(product);

      return new ReferenceResource(productId, refIndex,
        references.get(refIndex), getContextWorkingDir());
    }
    catch (Exception e)
    {
      String msg = String.format("Unable to find the requested resource: %s", e.getMessage());
      LOGGER.info(msg, e);
      throw new NotFoundException(msg);
    }
  }



  /**
   * Gets an HTTP response that represents a {@link Product} from the file
   * manager.
   * @param productId the ID of the product
   * @return an HTTP response that represents a {@link Product} from the file
   * manager
   */
  @GET
  @Path("product")
  @Produces({"application/xml", "application/json", "application/atom+xml",
    "application/rdf+xml", "application/rss+xml", "application/zip"})
  public ProductResource getProduct(@QueryParam("productId") String productId)
  {
    if (productId == null || productId.trim().equals(""))
    {
      throw new BadRequestException("This URL requires a productId query "
        + "parameter with a product ID value, "
        + "e.g. /product?productId=1787a257-df87-11e2-8a2d-e3f6264e86c5");
    }

    try
    {
      FileManagerClient client = getContextClient();

      // Find the product.
      Product product = client.getProductById(productId);
      product.setProductReferences(client.getProductReferences(product));

      // Create the product resource, add the product data and return the
      // resource as the HTTP response.
      return new ProductResource(product, client.getMetadata(product),
        product.getProductReferences(), getContextWorkingDir());
    }
    catch (Exception e)
    {
      String msg = String.format("Unable to find the requested resource: %s", e.getMessage());
      LOGGER.info(msg, e);
      throw new NotFoundException(msg);
    }
  }



  /**
   * Gets an HTTP response that represents a set of {@link Product products}
   * from the file manager.
   * @param productTypeId the ID of the {@link ProductType} for the data set or
   * "ALL" to denote all product types
   * @return an HTTP response that represents a set of {@link Product products}
   * from the file manager
   */
  @GET
  @Path("dataset")
  @Produces({"application/xml", "application/json", "application/atom+xml",
   "application/rdf+xml", "application/rss+xml", "application/zip"})
  public DatasetResource getDataset(
    @QueryParam("productTypeId") String productTypeId)
  {
    if (productTypeId == null || productTypeId.trim().equals(""))
    {
      throw new BadRequestException("This URL requires a productTypeId query "
        + "parameter and either a product type ID value or 'ALL' for all "
        + "product types.");
    }

    try
    {
      FileManagerClient client = getContextClient();

      String datasetId;
      String datasetName;
      Metadata datasetMetadata;

      List<ProductType> productTypes = new Vector<ProductType>();
      if (productTypeId.equals("ALL"))
      {
        productTypes = client.getProductTypes();
        datasetId = productTypeId;
        datasetName = productTypeId;
        datasetMetadata = new Metadata();
        datasetMetadata.addMetadata("ProductType", productTypeId);
      }
      else
      {
        ProductType productType = client.getProductTypeById(productTypeId);
        productTypes.add(productType);
        datasetId = productType.getProductTypeId();
        datasetName = productType.getName();
        datasetMetadata = productType.getTypeMetadata();
      }

      String productDirPath = getContextWorkingDir().getCanonicalPath()
        + "/" + datasetName;

      DatasetResource resource = new DatasetResource(datasetId, datasetName,
        datasetMetadata, getContextWorkingDir());

      // Add all products of the chosen type(s) to the dataset.
      for (ProductType productType : productTypes)
      {
        for (Product product : client.getProductsByProductType(productType))
        {
          product.setProductReferences(client.getProductReferences(product));
          resource.addProductResource(new ProductResource(product,
            client.getMetadata(product), product.getProductReferences(),
            new File(productDirPath)));
        }
      }
      return resource;
    }
    catch (Exception e)
    {
      String msg = String.format("Unable to find the requested resource: %s", e.getMessage());
      LOGGER.info(msg, e);
      throw new NotFoundException(msg);
    }
  }



  /**
   * Gets an HTTP response that represents the status of a currently active
   * file transfer for the file manager.
   * @param dataStoreRef the data store reference for the file being transferred
   * @return an HTTP response that represents the status of a currently active
   * file transfer for the file manager
   */
  @GET
  @Path("transfer")
  @Produces({"application/xml", "application/json", "application/atom+xml",
    "application/rdf+xml", "application/rss+xml"})
  public TransferResource getTransfer(
    @QueryParam("dataStoreRef") String dataStoreRef)
  {
    if (dataStoreRef == null || dataStoreRef.trim().equals(""))
    {
      throw new BadRequestException("This URL requires a dataStoreRef query "
        + "parameter and a data store reference value, "
        + "e.g. /transfer?dataStoreRef=file:/repository/test.txt/test.txt");
    }

    try
    {
      FileManagerClient client = getContextClient();
      for (FileTransferStatus status : client.getCurrentFileTransfers())
      {
        Reference reference = status.getFileRef();
        if (dataStoreRef.equals(reference.getDataStoreReference()))
        {
          Product product = status.getParentProduct();
          Metadata metadata = client.getMetadata(product);
          return new TransferResource(product, metadata, status);
        }
      }

      throw new Exception("Unable to find a current file transfer status for"
        + "data store reference: " + dataStoreRef);
    }
    catch (Exception e)
    {
      String msg = String.format("Unable to find the requested resource: %s", e.getMessage());
      LOGGER.info(msg, e);
      throw new NotFoundException(msg);
    }
  }



  /**
   * Gets an HTTP response that represents the statuses of all currently active
   * file transfers for the file manager, optionally filtered by product ID.
   * @param productId the ID of a product or ALL to denote all products
   * @return an HTTP response that represents the statuses of all currently
   * active file transfers for the file manager
   */
  @GET
  @Path("transfers")
  @Produces({"application/xml", "application/json", "application/atom+xml",
          "application/rdf+xml", "application/rss+xml"})
  public TransfersResource getTransfers(
          @QueryParam("productId") String productId)
  {
    if (productId == null || productId.trim().equals(""))
    {
      throw new BadRequestException("This URL requires a productId query "
              + "parameter and either a valid product ID value or 'ALL' for all "
              + "products.");
    }

    try
    {
      List<TransferResource> transferResources =
              new ArrayList<TransferResource>();
      FileManagerClient client = getContextClient();
      for (FileTransferStatus status : client.getCurrentFileTransfers())
      {
        Product product = status.getParentProduct();
        if(productId.equals("ALL") || productId.equals(product.getProductId()))
        {
          Metadata metadata = client.getMetadata(product);
          transferResources.add(
                  new TransferResource(product, metadata, status));
        }
      }
      return new TransfersResource(productId, transferResources);
    }
    catch (Exception e)
    {
      String msg = String.format("Unable to find the requested resource: %s", e.getMessage());
      LOGGER.info(msg, e);
      throw new NotFoundException(msg);
    }
  }



  /**
   * Gets the file manager's working directory from the servlet context.
   * @return the file manager working directory
   * @throws Exception if an object cannot be retrieved from the context
   * attribute
   */
  public File getContextWorkingDir() throws CasProductException {
    Object workingDirObject = context.getAttribute("workingDir");
    if (workingDirObject != null && workingDirObject instanceof File)
    {
      return (File) workingDirObject;
    }

    String msg = "Unable to get the file manager working directory from the servlet context";
    LOGGER.info(msg);
    throw new CasProductException(msg);
  }



  /**
   * Gets the file manager client instance from the servlet context.
   * @return the file manager client instance from the servlet context attribute
   * @throws Exception if an object cannot be retrieved from the context
   * attribute
   */
  public FileManagerClient getContextClient()
      throws CasProductException {
    // Get the file manager client from the servlet context.
    Object clientObject = context.getAttribute("client");
    if (clientObject != null &&
        clientObject instanceof FileManagerClient)
    {
      return (FileManagerClient) clientObject;
    }

    String msg = "Unable to get the file manager client from the servlet context";
    LOGGER.info(msg);
    throw new CasProductException(msg);
  }
}
