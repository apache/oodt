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
import org.apache.oodt.cas.product.jaxrs.resources.ReferenceResource;
import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link Provider} that writes {@link ReferenceResource reference resources}
 * to output streams for HTTP responses with "application/rss+xml" content-type.
 * @author rlaidlaw
 * @version $Revision$
 */
@Provider
@Produces("application/rss+xml")
public class ReferenceRssWriter extends RssWriter
  implements MessageBodyWriter<ReferenceResource>
{
  private static final Logger LOGGER = Logger.getLogger(ReferenceRssWriter.class
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
    try
    {
      String productId = resource.getProductId();
      int refIndex = resource.getRefIndex();

      String base = getBaseUri() + "reference";
      String query = "?productId=" + productId + "&refIndex=" + refIndex;
      String idLink = base + query;
      String fileLink = base + ".file" + query;
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
      XMLUtils.addNode(doc, channel, "title", "Reference(" + refIndex +
        ") from Product " + productId);

      Element atomLink = XMLUtils.addNode(doc, channel, "atom:link");
      XMLUtils.addAttribute(doc, atomLink, "href", selfLink);
      XMLUtils.addAttribute(doc, atomLink, "rel", "self");
      XMLUtils.addAttribute(doc, atomLink, "type", "application/rss+xml");

      XMLUtils.addNode(doc, channel, "link", idLink);
      XMLUtils.addNode(doc, channel, "description", "reference");

      XMLUtils.addNode(doc, channel, "language", LANGUAGE);
      XMLUtils.addNode(doc, channel, "copyright", COPYRIGHT);
      XMLUtils.addNode(doc, channel, "pubDate", currentDate);
      XMLUtils.addNode(doc, channel, "category", "Reference");
      XMLUtils.addNode(doc, channel, "generator", GENERATOR);
      XMLUtils.addNode(doc, channel, "lastBuildDate", currentDate);

      Element item = XMLUtils.addNode(doc, channel, "item");
      XMLUtils.addNode(doc, item, "title", "reference (" + refIndex +
        ") for product " + productId);
      XMLUtils.addNode(doc, item, "link", fileLink);
      XMLUtils.addNode(doc, item, "description",
        "A file manager reference entity");
      XMLUtils.addNode(doc, item, "guid", idLink);

      XMLUtils.writeXmlToStream(doc, entityStream);
    }
    catch (ParserConfigurationException e)
    {
      String message = "Unable to create RSS XML document for RSS response.";
      LOGGER.log(Level.WARNING, message, e);
      throw new InternalServerErrorException(message);
    }
  }
}
