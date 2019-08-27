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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.activation.DataHandler;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.metadata.MetExtractor;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.metadata.extractors.MetReaderExtractor;
import org.apache.oodt.cas.product.exceptions.CasProductException;
import org.apache.oodt.cas.product.jaxrs.enums.ErrorType;
import org.apache.oodt.cas.product.jaxrs.exceptions.BadRequestException;
import org.apache.oodt.cas.product.jaxrs.exceptions.InternalServerErrorException;
import org.apache.oodt.cas.product.jaxrs.exceptions.NotFoundException;
import org.apache.oodt.cas.product.jaxrs.filters.CORSFilter;
import org.apache.oodt.cas.product.jaxrs.resources.FMStatusResource;
import org.apache.oodt.cas.product.jaxrs.resources.ProductPageResource;
import org.apache.oodt.cas.product.jaxrs.resources.ProductResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class for Proposing Apache OODT-2.0 FileManager REST-APIs This handles HTTP requests and
 * returns file manager entities JAX-RS resources converted to different formats.
 *
 * @author ngimhana (Nadeeshan Gimhana)
 */
public class FileManagerJaxrsServiceV2 {

  private static Logger logger = LoggerFactory.getLogger(CORSFilter.class);

  // The servlet context, which is used to retrieve context parameters.
  @Context private ServletContext context;

  /**
   * Gets an HTTP request that represents a {@link ProductPage} from the file manager.
   *
   * @param productTypeName the Name of a productType
   * @return an HTTP response that represents a {@link ProductPage} from the file manager
   */
  @GET
  @Path("products")
  @Produces({
    "application/xml",
    "application/json",
    "application/atom+xml",
    "application/rdf+xml",
    "application/rss+xml"
  })
  public ProductPageResource getFirstPage(@QueryParam("productTypeName") String productTypeName)
      throws WebApplicationException {

    try {
      FileManagerClient client = getContextClient();

      // Get the first ProductPage
      ProductPage genericFile = client.getFirstPage(client.getProductTypeByName(productTypeName));

      return getProductPageResource(client, genericFile);

    } catch (Exception e) {
      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * Gets an HTTP request that represents a {@link ProductPage} from the file manager.
   *
   * @param productTypeName the Name of a productType
   * @param currentProductPage the current productPage
   * @return an HTTP response that represents a {@link ProductPage} from the file manager
   */
  @GET
  @Path("products")
  @Produces({
    "application/xml",
    "application/json",
    "application/atom+xml",
    "application/rdf+xml",
    "application/rss+xml"
  })
  public ProductPageResource getNextPage(
      @QueryParam("productTypeName") String productTypeName,
      @QueryParam("currentProductPage") int currentProductPage)
      throws WebApplicationException {

    try {
      FileManagerClient client = getContextClient();

      // Get the first ProductPage
      ProductPage firstpage = client.getFirstPage(client.getProductTypeByName(productTypeName));

      // Get the next ProductPage
      ProductPage nextPage =
          client.getNextPage(client.getProductTypeByName(productTypeName), firstpage);

      // Searching for the current page
      while (nextPage.getPageNum() != currentProductPage - 1) {
        nextPage = client.getNextPage(client.getProductTypeByName(productTypeName), nextPage);
      }

      // Get the next page from the current page
      ProductPage genericFile =
          client.getNextPage(client.getProductTypeByName(productTypeName), nextPage);

      // Return ProductPage resource
      return getProductPageResource(client, genericFile);

    } catch (Exception e) {
      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * This method is for creating a ProductPageResource Response and return it This method is for
   * creating a ProductPageResource {@link ProductPage} Response and return it
   *
   * @param client FileManager client
   * @param genericFile First/next/prev ProductPage
   */
  private ProductPageResource getProductPageResource(
      FileManagerClient client, ProductPage genericFile)
      throws CatalogException, CasProductException {

    // List for storing Metadata of the products in the ProductPage
    List<Metadata> proMetaDataList = new ArrayList<>();

    // List for storing References of the products in the ProductPage
    List<List<Reference>> proReferencesList = new ArrayList<>();

    for (Product pro : genericFile.getPageProducts()) {
      Metadata metadata = client.getMetadata(pro);
      List<Reference> productReferences = pro.getProductReferences();
      proMetaDataList.add(metadata);
      proReferencesList.add(productReferences);
    }

    return new ProductPageResource(
        genericFile, proMetaDataList, proReferencesList, getContextWorkingDir());
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
    logger.debug("Exception Thrown: {}", message);
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
    logger.debug("Exception Thrown: {}", message);
    throw new CasProductException(message);
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
      logger.debug("Exception Thrown: {}", message);

      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * Gets an HTTP response that represents a {@link Product} from the file manager.
   *
   * @param productFile the Product File to ingest
   * @param productType ProductType of the ingesting Product - eg.GenericFile
   * @param productStructure - Product Structure of the ingesting product - eg. Flat for
   *     Files/Hierarchy for Directory
   * @return an HTTP response that represents the Ingesting Id if success
   */
  @POST
  @Path("productWithFile")
  @Produces({"application/json", "application/xml"})
  @Consumes({MediaType.MULTIPART_FORM_DATA})
  public Response ingestProduct(
      @Multipart("productFile") Attachment productFile,
      @QueryParam("productType") String productType,
      @QueryParam("productStructure") String productStructure) {
    try {
      String transferServiceFacClass =
          "org.apache.oodt.cas." + "filemgr.datatransfer.LocalDataTransferFactory";

      // Take Data Handlers for Product and Metadata Files
      DataHandler productFileDataHandler = productFile.getDataHandler();

      // Get the input Streams of the CXF Attachments
      InputStream productFileInputStream = productFileDataHandler.getInputStream();

      // Write the Product File and MetaFiles to a temporary Files in server before ingest.
      File inputProductFile =
          writeToFileServer(
              productFileInputStream, productFile.getContentDisposition().getParameter("filename"));

      // Write Default Meta File
      String defaultMetaFileContent =
          "<cas:metadata xmlns:cas=\"http://oodt.jpl.nasa.gov/1.0/cas\">\n" + "</cas:metadata>";
      writeToFileServer(
          new ByteArrayInputStream(defaultMetaFileContent.getBytes(StandardCharsets.UTF_8)),
          productFile.getContentDisposition().getParameter("filename") + ".met");

      // Get File Manager and Its URL
      FileManagerClient client = getContextClient();
      URL fmURL = client.getFileManagerUrl();

      // Use StdIngester for Simple File Ingesting
      StdIngester ingester = new StdIngester(transferServiceFacClass);

      /*
       * Use MetaReaderExtracter to extract File Metadata from Product File
       *  * A Met Extractor that assumes that the .met file has already been generated.
       */
      MetExtractor metExtractor = new MetReaderExtractor();
      Metadata prodMeta = metExtractor.extractMetadata(inputProductFile);

      // Add Several Metadata to Metadata File
      prodMeta.addMetadata(CoreMetKeys.FILENAME, inputProductFile.getName());
      prodMeta.addMetadata(CoreMetKeys.PRODUCT_TYPE, productType);
      prodMeta.addMetadata(CoreMetKeys.PRODUCT_STRUCTURE, productStructure);
      prodMeta.addMetadata(CoreMetKeys.PRODUCT_NAME, inputProductFile.getName());

      // Product File Location. should Only provide File Path without FileName
      prodMeta.addMetadata(
          CoreMetKeys.FILE_LOCATION,
          (inputProductFile
              .getAbsolutePath()
              .substring(0, inputProductFile.getAbsolutePath().lastIndexOf("/"))));

      String ingest = ingester.ingest(fmURL, inputProductFile, prodMeta);
      return Response.ok(ingest).build();
    } catch (Exception e) {
      throw new InternalServerErrorException(e.getMessage());
    }
  }

  /**
   * Gets an HTTP response that represents a {@link Product} from the file manager.
   *
   * @param productFile the Product File to ingest
   * @param metadataFile the Metadata File to ingest
   * @return an HTTP response that represents the Ingesting Id if success
   */
  @POST
  @Path("productWithMeta")
  @Produces({"application/json", "application/xml"})
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response ingestProduct(
      @Multipart("productFile") Attachment productFile,
      @Multipart("metadataFile") Attachment metadataFile) {
    try {
      String transferServiceFacClass =
          "org.apache.oodt.cas." + "filemgr.datatransfer.LocalDataTransferFactory";

      // Take Data Handlers for Product and Metadata Files
      DataHandler productFileDataHandler = productFile.getDataHandler();
      DataHandler metadataFileDataHandler = metadataFile.getDataHandler();

      // Get the input Streams of the CXF Attachments
      InputStream productFileInputStream = productFileDataHandler.getInputStream();
      InputStream metadataFileInputStream = metadataFileDataHandler.getInputStream();

      // Write the Product File and MetaFiles to a temporary Files in server before ingest.
      File inputProductFile =
          writeToFileServer(
              productFileInputStream, productFile.getContentDisposition().getParameter("filename"));

      // Get File Manager and Its URL
      FileManagerClient client = getContextClient();
      URL fmURL = client.getFileManagerUrl();

      // Use StdIngester for Simple File Ingesting
      StdIngester ingester = new StdIngester(transferServiceFacClass);

      Metadata prodMeta = new SerializableMetadata(metadataFileInputStream);

      // Add Several Metadata to Metadata File
      prodMeta.addMetadata(CoreMetKeys.FILENAME, inputProductFile.getName());
      prodMeta.addMetadata(CoreMetKeys.PRODUCT_NAME, inputProductFile.getName());

      // Product File Location. should Only provide File Path without FileName
      prodMeta.addMetadata(
          CoreMetKeys.FILE_LOCATION,
          (inputProductFile
              .getAbsolutePath()
              .substring(0, inputProductFile.getAbsolutePath().lastIndexOf("/"))));

      String ingest = ingester.ingest(fmURL, inputProductFile, prodMeta);
      return Response.ok(ingest).build();
    } catch (Exception e) {
      throw new InternalServerErrorException(e.getMessage());
    }
  }

  /**
   * This method uses to Write an InputStream to File in the Server
   *
   * @param inputStream
   * @param fileName
   */
  private File writeToFileServer(InputStream inputStream, String fileName) {

    OutputStream outputStream = null;
    File file = new File("ingestedFiles/" + fileName);
    try {
      outputStream = new FileOutputStream(file);
      int read = 0;
      byte[] bytes = new byte[1024];
      while ((read = inputStream.read(bytes)) != -1) {
        outputStream.write(bytes, 0, read);
      }
      outputStream.flush();
      outputStream.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } finally {
      return file;
    }
  }

  /**
   * This method is for getting the current status of FileManager component
   *
   * @return Response object with status and FMProb URL
   */
  @GET
  @Path("fmprodstatus")
  @Produces({"application/json", "application/xml"})
  public Response getFmProductStatus() {
    try {
      FileManagerClient client = getContextClient();
      String URL = client.getFileManagerUrl().toString();
      boolean fmStatus = client.isAlive();
      FMStatusResource status = new FMStatusResource(URL, fmStatus, "Server Status");
      return Response.ok(status).build();
    } catch (Exception e) {
      throw new InternalServerErrorException(e.getMessage());
    }
  }

  /**
   * This method is for removing the ingested Products
   *
   * @return Response object with status and FMProb URL
   */
  @DELETE
  @Path("removeProduct")
  @Produces({"application/json", "application/xml"})
  public Response removeIngestedProduct(@QueryParam("productId") String productId) {
    try {
      FileManagerClient client = getContextClient();

      String URL = client.getFileManagerUrl().toString();
      boolean fmStatus = client.isAlive();

      boolean isRemoved = client.removeProduct(client.getProductById(productId));
      FMStatusResource status =
          new FMStatusResource(URL, fmStatus, "Product Removal Status: " + isRemoved);
      return Response.ok(status).build();
    } catch (Exception e) {
      throw new InternalServerErrorException(e.getMessage());
    }
  }

  //  /**
  //   * This method is for retrieve a ingested Products
  //   *
  //   * @return Response object with status and FMProb URL
  //   */
  //  @GET
  //  @Path("downloadProduct")
  //
  //  public Response downloadIngestedProduct(@QueryParam("productId") String productId) {
  //    try {
  //      FileManagerClient client = getContextClient();
  //
  //      String URL = client.getFileManagerUrl().toString();
  //      boolean fmStatus = client.isAlive();
  //
  ////      String d = client.getProductById(productId).;//      byte[] bytes = client
  //          byte [] bytes =
  // client.retrieveFile("/home/castle/Software_tools/apache-tomcat-9.0.20/bin/ingestedFiles/Hello_2", 0, 2000);
  //
  //
  //      //LOGGER.log(Level.INFO,"................."+d);
  //      // Path of a file
  //      String FILEPATH = "";
  //      File outputFile = new File(FILEPATH);
  //
  //      // Initialize a pointer
  //      // in file using OutputStream
  //      OutputStream os = new FileOutputStream(outputFile);
  //
  //      os.write(bytes);
  //
  //      // Starts writing the bytes in it
  //      os.write(bytes);
  //      System.out.println("Successfully"
  //          + " byte inserted");
  //
  //      // Close the file
  //      os.close();
  //      FMStatusResource status = new FMStatusResource(URL, fmStatus,"Product Retreval Status: " +
  // bytes.length);
  //      return Response.ok(status).build();
  //
  //    } catch (Exception e) {
  //      throw new InternalServerErrorException(e.getMessage());
  //    }
  //  }

}
