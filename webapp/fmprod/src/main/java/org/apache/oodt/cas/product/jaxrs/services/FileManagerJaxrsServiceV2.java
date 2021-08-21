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
import java.nio.file.Files;
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
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.*;
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
import org.apache.oodt.cas.product.jaxrs.resources.FMStatusResource;
import org.apache.oodt.cas.product.jaxrs.resources.ProductPageResource;
import org.apache.oodt.cas.product.jaxrs.resources.ProductResource;
import org.apache.oodt.cas.product.jaxrs.resources.ProductTypeListResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class for Proposing Apache OODT-2.0 FileManager REST-APIs This handles HTTP requests and
 * returns file manager entities JAX-RS resources converted to different formats.
 *
 * @author ngimhana (Nadeeshan Gimhana)
 * @author pavinduLakshan (Pavindu Lakshan)
 */
public class FileManagerJaxrsServiceV2 {

  private static Logger logger = LoggerFactory.getLogger(FileManagerJaxrsServiceV2.class);

  private static final String DATA_TRANSFER_FACTORY = 
          "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory";

  // The servlet context, which is used to retrieve context parameters.
  @Context private ServletContext context;
  
  private java.nio.file.Path tmpDir;
  
  public FileManagerJaxrsServiceV2() throws IOException {
    tmpDir = Files.createTempDirectory("oodt");
  }

  /**
   * Gets an HTTP request that represents a {@link ProductTypeListResource} from the file manager.
   *
   * @return an HTTP response that represents a {@link ProductTypeListResource} from the file manager
   */
  @GET
  @Path("productTypes")
  @Produces({
          "application/xml",
          "application/json",
          "application/atom+xml",
          "application/rdf+xml",
          "application/rss+xml"
  })
  public ProductTypeListResource getProductTypes() throws WebApplicationException {
    try {
      FileManagerClient client = getContextClient();
      List<ProductType> productTypes = client.getProductTypes();
      return new ProductTypeListResource(productTypes);
    } catch (Exception e) {
      throw new NotFoundException(e.getMessage());
    }
  }

  /**
   * This method is for calculating the total number of products in the file manager and return it
   *
   * @return the total number of products in the file manager
   */
  public int getTotalNumOfProducts() throws WebApplicationException {
    try {
      int totalFiles = 0;
      FileManagerClient client = getContextClient();
      List<ProductType> productTypes = client.getProductTypes();
      for(ProductType productType: productTypes){
        totalFiles += client.getNumProducts(productType);
      }
      return totalFiles;
    } catch (Exception e) {
      throw new InternalServerErrorException(e.getMessage());
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
      @QueryParam("productName") String productName,
      @QueryParam("currentProductPage") int currentProductPage)
      throws WebApplicationException {

    try {
      FileManagerClient client = getContextClient();
      Query query = new Query();
      ProductPage productPage;
      if (!StringUtils.isEmpty(productName)) {
        Product product = client.getProductByName(productName);
        List<Product> products = new ArrayList<Product>();
        products.add(product);
        productPage = new ProductPage(1, 1, 1, products);
      }
      else {
        productPage = client.pagedQuery(query,client.getProductTypeByName(productTypeName),currentProductPage);
      }
      return getProductPageResource(client, productPage);
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

    ProductPageResource pageResource = new ProductPageResource(
            genericFile, proMetaDataList, proReferencesList, getContextWorkingDir());
    int totalProducts = genericFile.getPageProducts().size() == 1 ? 1 : getTotalNumOfProducts();
    pageResource.setTotalProducts(totalProducts);
    return pageResource;
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
      logger.error(message, e);
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
      StdIngester ingester = new StdIngester(getDataTransferFactoryClass());

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
              .substring(0, inputProductFile.getAbsolutePath().lastIndexOf(File.separator))));

      String ingest = ingester.ingest(fmURL, inputProductFile, prodMeta);
      return Response.ok(ingest).build();
    } catch (Exception e) {
      logger.error("Failed to ingest product", e);
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
      StdIngester ingester = new StdIngester(getDataTransferFactoryClass());

      Metadata prodMeta = new SerializableMetadata(metadataFileInputStream);

      // Add Several Metadata to Metadata File
      prodMeta.addMetadata(CoreMetKeys.FILENAME, inputProductFile.getName());
      prodMeta.addMetadata(CoreMetKeys.PRODUCT_NAME, inputProductFile.getName());

      // Product File Location. should Only provide File Path without FileName
      prodMeta.addMetadata(
          CoreMetKeys.FILE_LOCATION,
          (inputProductFile
              .getAbsolutePath()
              .substring(0, inputProductFile.getAbsolutePath().lastIndexOf(File.separator))));

      String ingest = ingester.ingest(fmURL, inputProductFile, prodMeta);
      return Response.ok(ingest).build();
    } catch (Exception e) {
      logger.error("Failed to ingest product", e);
      throw new InternalServerErrorException(e.getMessage());
    }
  }

  /**
   * This method uses to Write an InputStream to File in the Server
   *
   * @param inputStream
   * @param fileName
   */
  private File writeToFileServer(InputStream inputStream, String fileName) throws IOException {
    java.nio.file.Path ingestedFile = tmpDir.resolve(fileName);
    try (OutputStream outputStream = new FileOutputStream(ingestedFile.toFile())) {
      int read = 0;
      byte[] bytes = new byte[1024];
      while ((read = inputStream.read(bytes)) != -1) {
        outputStream.write(bytes, 0, read);
      }
      outputStream.flush();
    }
    
    return ingestedFile.toFile();
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
  
  private String getDataTransferFactoryClass() {
    String factoryClass = DATA_TRANSFER_FACTORY;
    logger.debug("Using data transfer factory: {}", factoryClass);
    return factoryClass;
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
