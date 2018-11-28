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

package org.apache.oodt.cas.product.jaxrs.writers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.product.jaxrs.exceptions.InternalServerErrorException;
import org.apache.oodt.cas.product.jaxrs.exceptions.NotFoundException;
import org.apache.oodt.cas.product.jaxrs.resources.MetadataResource;
import org.apache.oodt.cas.product.jaxrs.resources.ProductResource;

/**
 * Utility class that zips up a {@link ProductResource}, including the product's
 * references and metadata.
 * @author rlaidlaw
 * @version $Revision$
 */
public class ProductZipper
{
  private static final Logger LOGGER = Logger.getLogger(ProductZipper.class
    .getName());

  /**
   * Creates a zip archive of the supplied {@link ProductResource product
   * resource} including all of the product's references and metadata.
   * @param resource the product resource to archive
   * @return the file reference for the zip archive of the product resource
   */
  public File createZipFile(ProductResource resource)
  {
    try
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

      // Try to remove previously created zip files that have the same name.
      File file = new File(workingDir.getCanonicalPath() + "/"
        + resource.getProductName() + ".zip");
      if (file.exists() && !file.delete())
      {
        String message = "Unable to delete an existing zip file ("
          + file.getAbsolutePath()
          + ") before creating a new zip file with the same name.";
        LOGGER.log(Level.FINE, message);
        throw new IOException(message);
      }

      // Set up the zip file.
      ZipFile zipFile = new ZipFile(file);
      ZipParameters parameters = new ZipParameters();
      parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
      parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

      // Add all of the product's references to the zip file.
      // Assumes that for hierarchical products, the first reference is the root
      // directory and all of its contents are included in the product.
      List<Reference> references = resource.getProductReferences();
      Reference rootReference = references.get(0);
      File rootFile = new File(new URI(rootReference.getDataStoreReference()));
      if (rootFile.isDirectory())
      {
        // Add the directory and all of its contents.
        zipFile.addFolder(rootFile, parameters);
      }
      else
      {
        // Add each file in the list of references.
        for (Reference reference : references)
        {
          zipFile.addFile(new File(new URI(reference.getDataStoreReference())),
            parameters);
        }
      }

      // Add the product's metadata to the zip file.
      MetadataResource metadataResource = resource.getMetadataResource();
      Metadata metadata = metadataResource.getMetadata();

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      SerializableMetadata serMetadata = new SerializableMetadata(metadata);
      serMetadata.writeMetadataToXmlStream(os);
      ByteArrayInputStream bis = new ByteArrayInputStream(os.toByteArray());

      parameters.setFileNameInZip(resource.getProductName() + ".met");
      parameters.setSourceExternalStream(true);
      zipFile.addStream(bis, parameters);

      return file;
    }

    catch (URISyntaxException e)
    {
      String message =
        "Problem with the data store URI(s) for the product's reference(s).";
      LOGGER.log(Level.FINE, message, e);
      throw new NotFoundException(message + " " + e.getMessage());
    }
    catch (ZipException e)
    {
      String message = "Unable to create a zip archive of the product.";
      LOGGER.log(Level.FINE, message, e);
      throw new InternalServerErrorException(message + " " + e.getMessage());
    }
    catch (IOException e)
    {
      String message = "Encountered I/O problems while trying to create a zip "
        + "archive of the product.";
      LOGGER.log(Level.FINE, message, e);
      throw new InternalServerErrorException(message + " " + e.getMessage());
    }
  }
}
