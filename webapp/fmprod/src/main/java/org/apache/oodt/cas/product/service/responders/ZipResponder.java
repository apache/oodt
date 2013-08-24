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

package org.apache.oodt.cas.product.service.responders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.product.service.exceptions.BadRequestException;
import org.apache.oodt.cas.product.service.exceptions.NotFoundException;
import org.apache.oodt.cas.product.service.resources.DatasetResource;
import org.apache.oodt.cas.product.service.resources.ProductResource;
import org.apache.oodt.cas.product.service.resources.ReferenceResource;
import org.apache.oodt.cas.product.service.resources.TransferResource;

/**
 * Generates HTTP responses using the application/zip MIME type.
 * @author rlaidlaw
 * @version $Revision$
 */
public class ZipResponder implements Responder
{
  private static final Logger LOGGER = Logger.getLogger(ZipResponder.class
    .getName());

  @Override
  public Response createResponse(ReferenceResource resource)
  {
    try
    {
      File file = createZipFile(resource);
      ResponseBuilder response = Response.ok(file);
      response.type("application/zip");
      response.header("Content-Disposition",
        "attachment; filename=\"" + file.getName() + "\"");
      return response.build();
    }
    catch (Exception e)
    {
      throw new NotFoundException(
        "Unable to get the requested resource in application/zip format. "
          + e.getMessage());
    }
  }



  @Override
  public Response createResponse(ProductResource resource)
  {
    try
    {
      File file = createZipFile(resource);
      ResponseBuilder response = Response.ok(file);
      response.type("application/zip");
      response.header("Content-Disposition",
        "attachment; filename=\"" + file.getName() + "\"");
      return response.build();
    }
    catch (Exception e)
    {
      throw new NotFoundException(
        "Unable to get the requested resource in application/zip format. "
          + e.getMessage());
    }
  }



  @Override
  public Response createResponse(DatasetResource resource)
  {
    try
    {
      File file = createZipFile(resource);
      ResponseBuilder response = Response.ok(file);
      response.type("application/zip");
      response.header("Content-Disposition",
        "attachment; filename=\"" + file.getName() + "\"");
      return response.build();
    }
    catch (Exception e)
    {
      throw new NotFoundException(
        "Unable to get the requested resource in application/zip format. "
          + e.getMessage());
    }
  }



  @Override
  public Response createResponse(TransferResource resource)
  {
    return Response.status(Response.Status.BAD_REQUEST)
      .entity("Format not valid for this resource type.").build();
  }



  /**
   * Creates a zip file for a single reference.
   * @throws Exception (FileNotFoundException, IOException, URISyntaxException,
   * ZipException)
   */
  private File createZipFile(ReferenceResource resource) throws Exception
  {
    // Create the working directory if it doesn't already exist.
    File workingDir = resource.getWorkingDir();
    if (!workingDir.exists() && !workingDir.mkdirs())
    {
      String message = "Unable to create the working directory ("
        + workingDir.getAbsolutePath() + ") to build the zip file.";
      LOGGER.log(Level.FINE, message);
      throw new IOException(message);
    }

    // Retrieve the reference file.
    Reference reference = resource.getReference();
    File refFile = new File(new URI(reference.getDataStoreReference()));
    if (!refFile.exists())
    {
      throw new BadRequestException("Unable to locate the source file for the" +
        " reference.");
    }

    // Try to remove previously created zip files that have the same name.
    String workingDirPath = workingDir.getCanonicalPath();
    workingDirPath += workingDirPath.endsWith("/") ? "" : "/";
    File file = new File(workingDirPath + refFile.getName() + ".zip");
    if (file.exists() && !file.delete())
    {
      LOGGER.log(Level.FINE, "Unable to delete an existing zip file ("
        + file.getAbsolutePath()
        + ") before creating a new zip file with the same name.");
    }

    // Add the reference file to the zip file.
    ZipFile zipFile = new ZipFile(file);
    ZipParameters parameters = new ZipParameters();
    parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
    parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
    zipFile.addFile(refFile, parameters);

    return file;
  }



  /**
   * Creates a zip file containing all of the reference files in the supplied
   * resource's product, along with the supplied resource's metadata.
   * @throws Exception (FileNotFoundException, IOException, URISyntaxException,
   * ZipException)
   */
  private File createZipFile(ProductResource resource) throws Exception
  {
    // Create the working directory if it doesn't already exist.
    File workingDir = resource.getWorkingDir();
    if (!workingDir.exists() && !workingDir.mkdirs())
    {
      String message = "Unable to create the working directory ("
        + workingDir.getAbsolutePath() + ") to build the zip file.";
      LOGGER.log(Level.FINE, message);
      throw new IOException(message);
    }

    Product product = resource.getProduct();

    // Try to remove previously created zip files that have the same name.
    File file = new File(workingDir.getCanonicalPath() + "/"
      + product.getProductName() + ".zip");
    if (file.exists() && !file.delete())
    {
      LOGGER.log(Level.FINE, "Unable to delete an existing zip file ("
        + file.getAbsolutePath()
        + ") before creating a new zip file with the same name.");
    }

    // Add all of the product's references to the zip file.
    ZipFile zipFile = new ZipFile(file);
    ZipParameters parameters = new ZipParameters();
    parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
    parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

    // Assumes that for hierarchical products, the first reference is the root
    // directory and all contents of this directory are included in the product.
    List<Reference> references = product.getProductReferences();
    Reference rootReference = references.get(0);
    File rootFile = new File(new URI(rootReference.getDataStoreReference()));
    if (rootFile.isDirectory())
    {
      zipFile.addFolder(rootFile, parameters);
    }
    else
    {
      for (Reference reference : references)
      {
        zipFile.addFile(new File(new URI(reference.getDataStoreReference())),
          parameters);
      }
    }

    // Add the product's metadata to the zip.
    parameters.setFileNameInZip(product.getProductName() + ".met");
    parameters.setSourceExternalStream(true);
    zipFile.addStream(getMetadataInputStream(resource.getMetadata()),
      parameters);

    return file;
  }



  /**
   * Creates a zip file containing all of the zipped products thet belong to the
   * data set's product type.
   * @throws Exception (FileNotFoundException, IOException, URISyntaxException,
   * ZipException)
   */
  private File createZipFile(DatasetResource resource) throws Exception
  {
    // Create the working directory if it doesn't already exist.
    File workingDir = resource.getWorkingDir();
    if (!workingDir.exists() && !workingDir.mkdirs())
    {
      String message = "Unable to create the working directory ("
        + workingDir.getAbsolutePath() + ") to build the zip file.";
      LOGGER.log(Level.FINE, message);
      throw new IOException(message);
    }

    ProductType productType = resource.getProductType();

    // Try to remove a previously created zip file with the same name.
    File file = new File(workingDir.getCanonicalPath() + "/"
      + productType.getName() + ".zip");
    if (file.exists() && !file.delete())
    {
      LOGGER.log(Level.FINE, "Unable to delete an existing zip file ("
        + file.getAbsolutePath()
        + ") before creating a new zip file with the same name.");
    }

    // Add all of the product's references to the zip file.
    ZipFile zipFile = new ZipFile(file);
    ZipParameters parameters = new ZipParameters();
    parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
    parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

    // Create zip archives for each product and add them to the dataset zip.
    for (ProductResource productResource : resource.getProductResources())
    {
      File refFile = createZipFile(productResource);
      zipFile.addFile(refFile, parameters);
      if (refFile.exists() && !refFile.delete())
      {
        LOGGER.log(Level.FINE, "Unable to delete a temporary product zip ("
          + refFile.getAbsolutePath()
          + ") after adding it to the dataset zip.");
      }
    }

    // Add the dataset's metadata to the zip.
    parameters.setFileNameInZip(productType.getName() + ".met");
    parameters.setSourceExternalStream(true);
    zipFile.addStream(getMetadataInputStream(productType.getTypeMetadata()),
      parameters);

    return file;
  }



  /**
   * Creates an {@link InputStream} of metadata information.
   * @param metadata the metadata object to add to the input stream
   * @return an input stream containing the metadata information
   * @throws IOException if the data cannot be written to the stream
   */
  private InputStream getMetadataInputStream(Metadata metadata)
    throws IOException
  {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    SerializableMetadata serMetadata = new SerializableMetadata(metadata);
    serMetadata.writeMetadataToXmlStream(os);
    return new ByteArrayInputStream(os.toByteArray());
  }
}
