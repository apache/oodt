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

import org.apache.oodt.cas.product.jaxrs.exceptions.InternalServerErrorException;
import org.apache.oodt.cas.product.jaxrs.resources.TransferResource;
import org.apache.oodt.cas.product.jaxrs.resources.TransfersResource;
import org.apache.oodt.commons.util.DateConvert;
import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link Provider} that writes {@link TransfersResource transfers resources}
 * to output streams for HTTP responses with "application/rss+xml" content-type.
 * @author rlaidlaw
 * @version $Revision$
 */
@Provider
@Produces("application/rss+xml")
public class TransfersRssWriter extends RssWriter
  implements MessageBodyWriter<TransfersResource>
{
  private static final Logger LOGGER = Logger.getLogger(TransfersRssWriter.class
    .getName());



  @Override
  public long getSize(TransfersResource resource, Class<?> type,
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
  public void writeTo(TransfersResource resource, Class<?> type,
    Type genericType, Annotation[] annotations, MediaType mediaType,
    MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException
  {
    try
    {
      String base = getBaseUri() + "transfers";
      String query = "?productId=" + resource.getProductId();
      String idLink = base + query;
      String selfLink = base + ".rss" + query;
      String currentDate = dateFormatter.format(new Date());

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      Document doc = factory.newDocumentBuilder().newDocument();
      Element rss = XMLUtils.addNode(doc, doc, "rss");
      XMLUtils.addAttribute(doc, rss, "version", "2.0");
      XMLUtils.addAttribute(doc, rss, "xmlns:atom",
        "http://www.w3.org/2005/Atom");

      Element channel = XMLUtils.addNode(doc, rss, "channel");
      XMLUtils.addNode(doc, channel, "title", "File Manager Transfers");

      Element atomLink = XMLUtils.addNode(doc, channel, "atom:link");
      XMLUtils.addAttribute(doc, atomLink, "href", selfLink);
      XMLUtils.addAttribute(doc, atomLink, "rel", "self");
      XMLUtils.addAttribute(doc, atomLink, "type", "application/rss+xml");

      XMLUtils.addNode(doc, channel, "link", idLink);
      XMLUtils.addNode(doc, channel, "description",
        "Files currently being transferred to the file manager repository.");

      XMLUtils.addNode(doc, channel, "language", LANGUAGE);
      XMLUtils.addNode(doc, channel, "copyright", COPYRIGHT);
      XMLUtils.addNode(doc, channel, "pubDate", currentDate);
      XMLUtils.addNode(doc, channel, "category", "data transfer");
      XMLUtils.addNode(doc, channel, "generator", GENERATOR);
      XMLUtils.addNode(doc, channel, "lastBuildDate", currentDate);

      for (TransferResource transferResource : resource.getTransferResources())
      {
        String dataStoreRef = transferResource.getDataStoreReference();
        String transferLink = getBaseUri() + "transfer?dataStoreRef="
          + dataStoreRef;
        Element item = XMLUtils.addNode(doc, channel, "item");
        XMLUtils.addNode(doc, item, "title", dataStoreRef);
        XMLUtils.addNode(doc, item, "link", transferLink);
        XMLUtils.addNode(doc, item, "description",
            "MIME Type: " + transferResource.getMimeTypeName()
          + ", Product: " + transferResource.getProductName()
          + ", Product Type: " + transferResource.getProductTypeName());
        XMLUtils.addNode(doc, item, "guid", transferLink);

        Date productReceivedTime = DateConvert.isoParse(transferResource
          .getProductReceivedTime());
        if (productReceivedTime != null)
        {
          XMLUtils.addNode(doc, item, "pubDate", dateFormatter
            .format(productReceivedTime));
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
