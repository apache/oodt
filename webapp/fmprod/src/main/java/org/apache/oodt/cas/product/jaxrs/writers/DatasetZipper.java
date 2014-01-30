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
import java.util.logging.Level;
import java.util.logging.Logger;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.product.jaxrs.exceptions.InternalServerErrorException;
import org.apache.oodt.cas.product.jaxrs.resources.DatasetResource;
import org.apache.oodt.cas.product.jaxrs.resources.MetadataResource;
import org.apache.oodt.cas.product.jaxrs.resources.ProductResource;

/**
 * Utility class that zips up a {@link DatasetResource}, including the dataset's
 * products and metadata.
 * @author rlaidlaw
 * @version $Revision$
 */
public class DatasetZipper
{
  private static final Logger LOGGER = Logger.getLogger(DatasetZipper.class
    .getName());

  private ProductZipper productZipper = new ProductZipper();

  /**
   * Creates a zip archive of the supplied {@link DatasetResource dataset
   * resource} including all of the dataset's products and metadata.
   * @param resource the dataset resource to archive
   * @return the file reference for the zip archive of the dataset resource
   */
  public File createZipFile(DatasetResource resource)
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

      // Try to remove a previously created zip file with the same name.
      File file = new File(workingDir.getCanonicalPath() + "/"
        + resource.getName() + ".zip");
      if (file.exists() && !file.delete())
      {
        String message = "Unable to delete an existing zip file ("
          + file.getAbsolutePath()
          + ") before creating a new zip file with the same name.";
        LOGGER.log(Level.FINE, message);
        throw new IOException(message);
      }

      // Set up the zip file for the dataset.
      ZipFile zipFile = new ZipFile(file);
      ZipParameters parameters = new ZipParameters();
      parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
      parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

      // Create zip archives for each product and add them to the dataset zip.
      for (ProductResource productResource : resource.getProductResources())
      {
        File refFile = productZipper.createZipFile(productResource);
        zipFile.addFile(refFile, parameters);
        if (refFile.exists() && !refFile.delete())
        {
          String message = "Unable to delete a temporary product zip ("
            + refFile.getAbsolutePath()
            + ") after adding it to the dataset zip.";
          LOGGER.log(Level.FINE, message);
          throw new IOException(message);
        }
      }

      // Add the dataset's metadata to the zip.
      MetadataResource metadataResource = resource.getMetadataResource();
      Metadata metadata = metadataResource.getMetadata();

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      SerializableMetadata serMetadata = new SerializableMetadata(metadata);
      serMetadata.writeMetadataToXmlStream(os);
      ByteArrayInputStream bis = new ByteArrayInputStream(os.toByteArray());

      parameters.setFileNameInZip(resource.getName() + ".met");
      parameters.setSourceExternalStream(true);
      zipFile.addStream(bis, parameters);

      return file;
    }

    catch (ZipException e)
    {
      String message = "Unable to create a zip archive of the dataset.";
      LOGGER.log(Level.FINE, message, e);
      throw new InternalServerErrorException(message + " " + e.getMessage());
    }
    catch (IOException e)
    {
      String message = "Encountered I/O problems while trying to create a zip "
        + "archive of the dataset.";
      LOGGER.log(Level.FINE, message, e);
      throw new InternalServerErrorException(message + " " + e.getMessage());
    }
  }
}
