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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.product.service.exceptions.NotFoundException;
import org.apache.oodt.cas.product.service.resources.DatasetResource;
import org.apache.oodt.cas.product.service.resources.ProductResource;
import org.apache.oodt.cas.product.service.resources.ReferenceResource;
import org.apache.oodt.cas.product.service.resources.TransferResource;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;

/**
 * Generates HTTP responses for resources using their MIME types.
 * @author rlaidlaw
 * @version $Revision$
 */
public class FileResponder implements Responder
{
  private static final Logger LOGGER = Logger.getLogger(FileResponder.class
    .getName());

  @Override
  public Response createResponse(ReferenceResource resource)
  {
    return createReferenceResponse(resource.getReference());
  }



  @Override
  public Response createResponse(ProductResource resource)
  {
    Product product = resource.getProduct();
    List<Reference> references = product.getProductReferences();
    int index = resource.getIndex();
    if (index < 0 || index >= references.size())
    {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity("Index out of range.").build();
    }
    return createReferenceResponse(references.get(index));
  }



  @Override
  public Response createResponse(DatasetResource resource)
  {
    return Response.status(Response.Status.BAD_REQUEST)
      .entity("Format not valid for this resource type.").build();
  }



  @Override
  public Response createResponse(TransferResource resource)
  {
    return Response.status(Response.Status.BAD_REQUEST)
      .entity("Format not valid for this resource type.").build();
  }



  /*
   * Creates a response for a given reference.
   */
  private Response createReferenceResponse(Reference reference)
  {
    String contentType;
    String dataStoreReference = reference.getDataStoreReference();
    MimeType mimeType = reference.getMimeType();
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
          "Unable to locate the reference source file(s) in the data store.";
        LOGGER.log(Level.FINE, message);
        return Response.status(Response.Status.BAD_REQUEST)
          .entity(message).build();
      }

      ResponseBuilder response = Response.ok(file);
      response.type(contentType);
      response.header("Content-Disposition",
        "attachment; filename=\"" + file.getName() + "\"");
      return response.build();
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
