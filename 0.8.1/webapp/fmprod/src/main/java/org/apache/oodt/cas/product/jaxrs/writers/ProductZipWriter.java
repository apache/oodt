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

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.apache.oodt.cas.product.jaxrs.resources.ProductResource;

/**
 * A {@link Provider} that writes {@link ProductResource product resources} to
 * output streams for HTTP responses with "application/zip" content-type.
 * @author rlaidlaw
 * @version $Revision$
 */
@Provider
@Produces("application/zip")
public class ProductZipWriter implements MessageBodyWriter<ProductResource>
{
  @Override
  public long getSize(ProductResource resource, Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
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
  public void writeTo(ProductResource resource, Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType,
    MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException
  {
    // Create a zip file for the product resource.
    File zipFile = new ProductZipper().createZipFile(resource);

    // Add the zip file to the HTTP response entity stream.
    httpHeaders.add("Content-Type", "application/zip");
    httpHeaders.add("Content-Disposition",
      "attachment; filename=\"" + zipFile.getName() + "\"");
    FileInputStream fis = new FileInputStream(zipFile);
    IOUtils.copy(fis, entityStream);
    fis.close();
  }
}
