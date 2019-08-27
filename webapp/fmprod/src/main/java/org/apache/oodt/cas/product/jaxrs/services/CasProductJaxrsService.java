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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.product.exceptions.CasProductException;
import org.apache.oodt.cas.product.jaxrs.enums.ErrorType;
import org.apache.oodt.cas.product.jaxrs.exceptions.BadRequestException;
import org.apache.oodt.cas.product.jaxrs.exceptions.NotFoundException;
import org.apache.oodt.cas.product.jaxrs.resources.DatasetResource;
import org.apache.oodt.cas.product.jaxrs.resources.ProductResource;
import org.apache.oodt.cas.product.jaxrs.resources.ReferenceResource;
import org.apache.oodt.cas.product.jaxrs.resources.TransferResource;
import org.apache.oodt.cas.product.jaxrs.resources.TransfersResource;

/**
 * Service class that handles HTTP requests and returns file manager entities such as {@link
 * Reference references} and {@link Product products} as JAX-RS resources converted to different
 * formats.
 *
 * @author rlaidlaw
 * @version $Revision$
 */
public class CasProductJaxrsService {
  private static final Logger LOGGER = Logger.getLogger(CasProductJaxrsService.class.getName());

  // The servlet context, which is used to retrieve context parameters.
  @Context private ServletContext context;

  /**
   * Gets an HTTP response that represents a {@link Reference} from a {@link Product} from the file
   * manager.
   *
   * @param productId the ID of the product that the reference belongs to
   * @param refIndex the index of the reference within the product's list of references
   * @return an HTTP response that represents a {@link Reference} from a {@link Product} from the
   *     file manager
   */
  @GET
  @Path("reference")
  @Produces({
    "application/octet-stream",
    "application/xml",
    "application/json",
    "application/atom+xml",
    "application/rdf+xml",
    "application/rss+xml",
    "application/zip"
  })
  public ReferenceResource getReference(
      @QueryParam("productId") String productId, @QueryParam("refIndex") int refIndex)
      throws WebApplicationException {
    if (productId == null || productId.trim().equals("")) {
      throw new BadRequestException(
          ErrorType.BAD_REQUEST_EXCEPTION_REFERENCE_RESOURCE.getErrorType());
    }

    try {
      FileManagerClient client = getContextClient();
      Product product = client.getProductById(productId);
      List<Reference> references = client.getProductReferences(product);

      return new ReferenceResource(
          productId, refIndex, references.get(refIndex), getContextWorkingDir());
    } catch (Exception e) {
      // Just for Logging Purposes
      String message = "Unable to find the requested resource.";
      LOGGER.log(Level.FINE, message, e);

      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * Gets an HTTP response that represents a {@link Product} from the file manager.
   *
   * @param productId the ID of the product
   * @return an HTTP response that represents a {@link Product} from the file manager
   */
  @GET
  @Path("product")
  @Produces({
    "application/xml",
    "application/json",
    "application/atom+xml",
    "application/rdf+xml",
    "application/rss+xml",
    "application/zip"
  })
  public ProductResource getProduct(@QueryParam("productId") String productId)
      throws WebApplicationException {
    if (productId == null || productId.trim().equals("")) {
      throw new BadRequestException(
          ErrorType.BAD_REQUEST_EXCEPTION_PRODUCT_RESOURCE.getErrorType());
    }

    try {
      FileManagerClient client = getContextClient();

      // Find the product.
      Product product = client.getProductById(productId);
      product.setProductReferences(client.getProductReferences(product));

      // Create the product resource, add the product data and return the
      // resource as the HTTP response.
      return new ProductResource(
          product,
          client.getMetadata(product),
          product.getProductReferences(),
          getContextWorkingDir());
    } catch (Exception e) {
      // Just for Logging Purposes
      String message = "Unable to find the requested resource.";
      LOGGER.log(Level.FINE, message, e);

      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * Gets an HTTP response that represents a set of {@link Product products} from the file manager.
   *
   * @param productTypeId the ID of the {@link ProductType} for the data set or "ALL" to denote all
   *     product types
   * @return an HTTP response that represents a set of {@link Product products} from the file
   *     manager
   */
  @GET
  @Path("dataset")
  @Produces({
    "application/xml",
    "application/json",
    "application/atom+xml",
    "application/rdf+xml",
    "application/rss+xml",
    "application/zip"
  })
  public DatasetResource getDataset(@QueryParam("productTypeId") String productTypeId)
      throws WebApplicationException {
    if (productTypeId == null || productTypeId.trim().equals("")) {
      throw new BadRequestException(
          ErrorType.BAD_REQUEST_EXCEPTION_DATASET_RESOURCE.getErrorType());
    }

    try {
      FileManagerClient client = getContextClient();

      String datasetId;
      String datasetName;
      Metadata datasetMetadata;

      List<ProductType> productTypes = new Vector<ProductType>();
      if (productTypeId.equals("ALL")) {
        productTypes = client.getProductTypes();
        datasetId = productTypeId;
        datasetName = productTypeId;
        datasetMetadata = new Metadata();
        datasetMetadata.addMetadata("ProductType", productTypeId);
      } else {
        ProductType productType = client.getProductTypeById(productTypeId);
        productTypes.add(productType);
        datasetId = productType.getProductTypeId();
        datasetName = productType.getName();
        datasetMetadata = productType.getTypeMetadata();
      }

      String productDirPath = getContextWorkingDir().getCanonicalPath() + "/" + datasetName;

      DatasetResource resource =
          new DatasetResource(datasetId, datasetName, datasetMetadata, getContextWorkingDir());

      // Add all products of the chosen type(s) to the dataset.
      for (ProductType productType : productTypes) {
        for (Product product : client.getProductsByProductType(productType)) {
          product.setProductReferences(client.getProductReferences(product));
          resource.addProductResource(
              new ProductResource(
                  product,
                  client.getMetadata(product),
                  product.getProductReferences(),
                  new File(productDirPath)));
        }
      }
      return resource;
    } catch (Exception e) {
      // Just for Logging Purposes
      String message = "Unable to find the requested resource.";
      LOGGER.log(Level.FINE, message, e);

      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * Gets an HTTP response that represents the status of a currently active file transfer for the
   * file manager.
   *
   * @param dataStoreRef the data store reference for the file being transferred
   * @return an HTTP response that represents the status of a currently active file transfer for the
   *     file manager
   */
  @GET
  @Path("transfer")
  @Produces({
    "application/xml",
    "application/json",
    "application/atom+xml",
    "application/rdf+xml",
    "application/rss+xml"
  })
  public TransferResource getTransfer(@QueryParam("dataStoreRef") String dataStoreRef)
      throws WebApplicationException {
    if (dataStoreRef == null || dataStoreRef.trim().equals("")) {
      throw new BadRequestException(
          ErrorType.BAD_REQUEST_EXCEPTION_TRANSFER_RESOURCE.getErrorType());
    }

    try {
      FileManagerClient client = getContextClient();
      for (FileTransferStatus status : client.getCurrentFileTransfers()) {
        Reference reference = status.getFileRef();
        if (dataStoreRef.equals(reference.getDataStoreReference())) {
          Product product = status.getParentProduct();
          Metadata metadata = client.getMetadata(product);
          return new TransferResource(product, metadata, status);
        }
      }

      throw new NotFoundException(
          ErrorType.NOT_FOUND_EXCEPTION_TRANSFER_RESOURCE.getErrorType() + dataStoreRef);
    } catch (Exception e) {
      // Just for Logging Purposes
      String message = "Unable to find the requested resource.";
      LOGGER.log(Level.FINE, message, e);

      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * Gets an HTTP response that represents the statuses of all currently active file transfers for
   * the file manager, optionally filtered by product ID.
   *
   * @param productId the ID of a product or ALL to denote all products
   * @return an HTTP response that represents the statuses of all currently active file transfers
   *     for the file manager
   */
  @GET
  @Path("transfers")
  @Produces({
    "application/xml",
    "application/json",
    "application/atom+xml",
    "application/rdf+xml",
    "application/rss+xml"
  })
  public TransfersResource getTransfers(@QueryParam("productId") String productId)
      throws WebApplicationException {
    if (productId == null || productId.trim().equals("")) {
      throw new BadRequestException(
          ErrorType.BAD_REQUEST_EXCEPTION_DATASET_RESOURCE.getErrorType());
    }

    try {
      List<TransferResource> transferResources = new ArrayList<TransferResource>();
      FileManagerClient client = getContextClient();
      for (FileTransferStatus status : client.getCurrentFileTransfers()) {
        Product product = status.getParentProduct();
        if (productId.equals("ALL") || productId.equals(product.getProductId())) {
          Metadata metadata = client.getMetadata(product);
          transferResources.add(new TransferResource(product, metadata, status));
        }
      }
      return new TransfersResource(productId, transferResources);
    } catch (Exception e) {
      // Just for Logging Purposes
      String message = "Unable to find the requested resource.";
      LOGGER.log(Level.FINE, message, e);

      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * Gets the file manager's working directory from the servlet context.
   *
   * @return the file manager working directory
   * @throws Exception if an object cannot be retrieved from the context attribute
   */
  public File getContextWorkingDir() throws CasProductException {
    Object workingDirObject = context.getAttribute("workingDir");
    if (workingDirObject != null && workingDirObject instanceof File) {
      return (File) workingDirObject;
    }

    String message = ErrorType.CAS_PRODUCT_EXCEPTION_FILEMGR_WORKING_DIR_UNAVILABLE.getErrorType();
    LOGGER.log(Level.WARNING, message);
    throw new CasProductException(message);
  }

  /**
   * Gets the file manager client instance from the servlet context.
   *
   * @return the file manager client instance from the servlet context attribute
   * @throws Exception if an object cannot be retrieved from the context attribute
   */
  public FileManagerClient getContextClient() throws CasProductException {
    // Get the file manager client from the servlet context.
    Object clientObject = context.getAttribute("client");
    if (clientObject != null && clientObject instanceof FileManagerClient) {
      return (FileManagerClient) clientObject;
    }

    String message = ErrorType.CAS_PRODUCT_EXCEPTION_FILEMGR_CLIENT_UNAVILABLE.getErrorType();
    LOGGER.log(Level.WARNING, message);
    throw new CasProductException(message);
  }
}
