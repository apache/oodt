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

import org.apache.commons.io.IOUtils;
import org.apache.oodt.cas.product.jaxrs.exceptions.BadRequestException;
import org.apache.oodt.cas.product.jaxrs.exceptions.NotFoundException;
import org.apache.oodt.cas.product.jaxrs.resources.ReferenceResource;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;

/**
 * A {@link Provider} that writes {@link ReferenceResource reference resources}
 * to output streams for HTTP responses with different file content-types.
 * @author rlaidlaw
 * @version $Revision$
 */
@Provider
@Produces("application/octet-stream")
public class ReferenceFileWriter implements MessageBodyWriter<ReferenceResource>
{
  private static final Logger LOGGER = Logger.getLogger(ReferenceFileWriter
    .class.getName());



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
    String contentType;
    String dataStoreReference = resource.getDataStoreReference();
    MimeType mimeType = resource.getMimeType();
    if (mimeType != null &&
        mimeType.getName() != null &&
       !mimeType.getName().equals(""))
    {
      contentType = mimeType.getName();
    }
    else
    {
      contentType = new Tika().detect(dataStoreReference);
    }

    try
    {
      File file = new File(new URI(dataStoreReference));
      if (!file.exists() || file.isDirectory())
      {
        String message =
          "Could not locate the reference source file(s) in the data store.";
        LOGGER.log(Level.FINE, message);
        throw new BadRequestException(message);
      }

      httpHeaders.add("Content-Type", contentType);
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
  }
}
