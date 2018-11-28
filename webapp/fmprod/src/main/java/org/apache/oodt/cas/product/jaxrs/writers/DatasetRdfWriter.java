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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.oodt.cas.product.jaxrs.configurations.RdfConfiguration;
import org.apache.oodt.cas.product.jaxrs.exceptions.InternalServerErrorException;
import org.apache.oodt.cas.product.jaxrs.resources.DatasetResource;
import org.apache.oodt.cas.product.jaxrs.resources.MetadataResource;
import org.apache.oodt.cas.product.jaxrs.resources.MetadataResource.MetadataEntry;
import org.apache.oodt.cas.product.jaxrs.resources.ProductResource;
import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link Provider} that writes {@link DatasetResource dataset resources} to
 * output streams for HTTP responses with "application/rdf+xml" content-type.
 * @author rlaidlaw
 * @version $Revision$
 */
@Provider
@Produces("application/rdf+xml")
public class DatasetRdfWriter extends RdfWriter
  implements MessageBodyWriter<DatasetResource>
{
  private static final Logger LOGGER = Logger.getLogger(DatasetRdfWriter.class
    .getName());



  @Override
  public long getSize(DatasetResource resource, Class<?> type,
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
  public void writeTo(DatasetResource resource, Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType,
    MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);

    RdfConfiguration configuration = getConfiguration();

    try
    {
      Document doc = factory.newDocumentBuilder().newDocument();

      Element rdf = XMLUtils.addNode(doc, doc, "rdf:RDF");
      XMLUtils.addAttribute(doc, rdf, RDF_NAMESPACE_NAME, RDF_NAMESPACE_VALUE);

      if (configuration != null)
      {
        Map<String, String> namespaceMap = configuration.getNamespaces();
        for (Entry<String, String> entry : namespaceMap.entrySet())
        {
          XMLUtils.addAttribute(doc, rdf, "xmlns:" + entry.getKey(),
            entry.getValue());
        }
      }
      else
      {
        XMLUtils.addAttribute(doc, rdf, CAS_NAMESPACE_NAME,
          CAS_NAMESPACE_VALUE);
      }

      for (ProductResource productResource : resource.getProductResources())
      {
        String productTypeName = productResource.getProductTypeName();
        String productId = productResource.getProductId();
        String productNs = configuration != null
          ? configuration.getTypeNamespace(productTypeName)
          : CAS_NAMESPACE_PREFIX;
        Element productRdf = XMLUtils.addNode(doc, rdf,
          productNs + ":" + productTypeName);

        XMLUtils.addAttribute(doc, productRdf, "rdf:about",
          getBaseUri() + "product?productId=" + productId);

        if (configuration != null)
        {
          MetadataResource metadataResource = productResource
            .getMetadataResource();
          for (MetadataEntry entry : metadataResource.getMetadataEntries())
          {
            for (String value : entry.getValues())
            {
              Element metaElement = configuration.createElement(entry.getKey(),
                value, doc);
              productRdf.appendChild(metaElement);
            }
          }
        }
      }
      XMLUtils.writeXmlToStream(doc, entityStream);
    }
    catch (ParserConfigurationException e)
    {
      String message = "Unable to build org.w3c.dom.Document for output.";
      LOGGER.log(Level.WARNING, message, e);
      throw new InternalServerErrorException(message);
    }
  }
}
