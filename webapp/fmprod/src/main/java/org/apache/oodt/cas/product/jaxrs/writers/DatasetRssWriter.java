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
import java.text.ParseException;
import java.util.Date;
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

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.product.jaxrs.configurations.RssConfiguration;
import org.apache.oodt.cas.product.jaxrs.configurations.RssNamespace;
import org.apache.oodt.cas.product.jaxrs.exceptions.InternalServerErrorException;
import org.apache.oodt.cas.product.jaxrs.resources.DatasetResource;
import org.apache.oodt.cas.product.jaxrs.resources.ProductResource;
import org.apache.oodt.commons.util.DateConvert;
import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link Provider} that writes {@link DatasetResource dataset resources} to
 * output streams for HTTP responses with "application/rss+xml" content-type.
 * @author rlaidlaw
 * @version $Revision$
 */
@Provider
@Produces("application/rss+xml")
public class DatasetRssWriter extends RssWriter
  implements MessageBodyWriter<DatasetResource>
{
  private static final Logger LOGGER = Logger.getLogger(DatasetRssWriter.class
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
    String productTypeName = resource.getName();
    String productTypeId = resource.getId();
    String base = getBaseUri() + "dataset";
    String query = "?productTypeId=" + productTypeId;
    String idLink = base + query;
    String selfLink = base + ".rss" + query;
    String currentDate = dateFormatter.format(new Date());

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);

    RssConfiguration configuration = getConfiguration();

    try
    {
      Document doc = factory.newDocumentBuilder().newDocument();
      Element rss = XMLUtils.addNode(doc, doc, "rss");
      XMLUtils.addAttribute(doc, rss, "version", "2.0");
      XMLUtils.addAttribute(doc, rss, "xmlns:atom",
        "http://www.w3.org/2005/Atom");

      if (configuration != null)
      {
        // Add namespaces defined in the configuration file.
        for (RssNamespace namespace : configuration.getNamespaceList())
        {
          XMLUtils.addAttribute(doc, rss, "xmlns:" + namespace.getPrefix(),
            namespace.getUriString());
        }

        // Get channel link information, if specified in the configuration.
        String channelLink = configuration.getChannelLink();
        if (channelLink != null && !channelLink.equals(""))
        {
          Metadata channelMetadata = new Metadata();
          channelMetadata.addMetadata("ProductType", productTypeName);
          channelMetadata.addMetadata("ProductTypeId", productTypeId);
          channelMetadata.addMetadata("BaseUrl", base);

          idLink = PathUtils.replaceEnvVariables(channelLink, channelMetadata);
        }
      }

      Element channel = XMLUtils.addNode(doc, rss, "channel");
      XMLUtils.addNode(doc, channel, "title", productTypeName);

      Element atomLink = XMLUtils.addNode(doc, channel, "atom:link");
      XMLUtils.addAttribute(doc, atomLink, "href", selfLink);
      XMLUtils.addAttribute(doc, atomLink, "rel", "self");
      XMLUtils.addAttribute(doc, atomLink, "type", "application/rss+xml");

      XMLUtils.addNode(doc, channel, "link", idLink);
      XMLUtils.addNode(doc, channel, "description", productTypeName);

      XMLUtils.addNode(doc, channel, "language", LANGUAGE);
      XMLUtils.addNode(doc, channel, "copyright", COPYRIGHT);
      XMLUtils.addNode(doc, channel, "pubDate", currentDate);
      XMLUtils.addNode(doc, channel, "category", productTypeName);
      XMLUtils.addNode(doc, channel, "generator", GENERATOR);
      XMLUtils.addNode(doc, channel, "lastBuildDate", currentDate);

      for (ProductResource productResource : resource.getProductResources())
      {
        String productLink = getBaseUri() + "product?productId="
          + productResource.getProductId();
        Element item = XMLUtils.addNode(doc, channel, "item");
        XMLUtils.addNode(doc, item, "title", productResource.getProductName());
        XMLUtils.addNode(doc, item, "link", productLink);
        XMLUtils.addNode(doc, item, "description", productResource
          .getProductTypeName());
        XMLUtils.addNode(doc, item, "guid", productLink);

        Metadata metadata = productResource.getMetadataResource().getMetadata();
        Date productReceivedTime = DateConvert.isoParse(metadata
          .getMetadata("CAS.ProductReceivedTime"));
        if (productReceivedTime != null)
        {
          XMLUtils.addNode(doc, item, "pubDate", dateFormatter
            .format(productReceivedTime));
        }

        // Append additional tags defined in the configuration file.
        if (configuration != null)
        {
          configuration.appendTags(metadata, doc, item);
        }
      }

      XMLUtils.writeXmlToStream(doc, entityStream);
    }
    catch (ParserConfigurationException e)
    {
      String message = "Unable to create RSS XML document for RSS response.";
      LOGGER.log(Level.WARNING, message, e);
      throw new InternalServerErrorException(message);
    }
    catch (ParseException e)
    {
      String message = "Unable to create RSS XML document for RSS response.";
      LOGGER.log(Level.WARNING, message, e);
      throw new InternalServerErrorException(message);
    }
  }
}
