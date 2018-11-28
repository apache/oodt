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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.io.IOUtils;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.product.jaxrs.exceptions.BadRequestException;
import org.apache.oodt.cas.product.jaxrs.exceptions.InternalServerErrorException;
import org.apache.oodt.cas.product.jaxrs.exceptions.NotFoundException;
import org.apache.oodt.cas.product.jaxrs.resources.ReferenceResource;

/**
 * A {@link Provider} that writes {@link ReferenceResource reference resources}
 * to output streams for HTTP responses with "application/zip" content-type.
 * @author rlaidlaw
 * @version $Revision$
 */
@Provider
@Produces("application/zip")
public class ReferenceZipWriter implements MessageBodyWriter<ReferenceResource>
{
  private static final Logger LOGGER = Logger.getLogger(ReferenceZipWriter.class
    .getName());



  @Override
  public long getSize(ReferenceResource resource, Class<?> type,
    Type genericType, Annotation[] annotations, MediaType mediaType)
  {
    return -1;
  }



  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return true;
  }



  @Override
  public void writeTo(ReferenceResource resource, Class<?> type,
    Type genericType, Annotation[] annotations, MediaType mediaType,
    MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException
  {
    // Create the working directory if it doesn't already exist.
    File workingDir = resource.getWorkingDir();
    if (!workingDir.exists() && !workingDir.mkdirs())
    {
      String message = "Unable to create the working directory ("
        + workingDir.getAbsolutePath() + ") to build the zip file.";
      LOGGER.log(Level.FINE, message);
      throw new InternalServerErrorException(message);
    }

    try
    {
      // Retrieve the reference file.
      Reference reference = resource.getReference();
      File refFile = new File(new URI(reference.getDataStoreReference()));
      if (!refFile.exists())
      {
        throw new BadRequestException("Unable to locate the source file for the"
          + " reference.");
      }

      // Try to remove previously created zip files that have the same name.
      String workingDirPath = workingDir.getCanonicalPath();
      workingDirPath += workingDirPath.endsWith("/") ? "" : "/";
      File file = new File(workingDirPath + refFile.getName() + ".zip");
      if (file.exists() && !file.delete())
      {
        String message = "Unable to delete an existing zip file ("
          + file.getAbsolutePath()
          + ") before creating a new zip file with the same name.";
        LOGGER.log(Level.FINE, message);
        throw new InternalServerErrorException(message);
      }

      // Add the reference file to the zip file.
      ZipFile zipFile = new ZipFile(file);
      ZipParameters parameters = new ZipParameters();
      parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
      parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
      zipFile.addFile(refFile, parameters);

      httpHeaders.add("Content-Type", "application/zip");
      httpHeaders.add("Content-Disposition",
        "attachment; filename=\"" + file.getName() + "\"");
      FileInputStream fis = new FileInputStream(file);
      IOUtils.copy(fis, entityStream);
      fis.close();
    }
    catch (URISyntaxException e)
    {
      String message =
        "Problem with the data store URI for the reference source file(s).";
      LOGGER.log(Level.FINE, message, e);
      throw new NotFoundException(message + " " + e.getMessage());
    }
    catch (ZipException e)
    {
      String message =
        "Unable to create a zip archive of the reference source file(s).";
      LOGGER.log(Level.FINE, message, e);
      throw new InternalServerErrorException(message + " " + e.getMessage());
    }
  }
}
