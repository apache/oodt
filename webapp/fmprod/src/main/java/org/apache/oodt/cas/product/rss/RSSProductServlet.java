/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oodt.cas.product.rss;


//OODT imports

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.util.DateConvert;
import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//JDK imports

/**
 * A Servlet that supports the <a
 * href="http://feedvalidator.org/docs/rss2.html">RSS 2.0</a> specification for
 * delivering Product Feeds.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 * 
 */
public class RSSProductServlet extends HttpServlet {
  /* serial Version UID */
  private static final long serialVersionUID = -465321738239885777L;

  /* our client to the file manager */
  private static FileManagerClient fm = null;

  /* rss config */
  private RSSConfig conf;

  /* our log stream */
  private Logger LOG = Logger.getLogger(RSSProductServlet.class.getName());

  public static final String COPYRIGHT_BOILER_PLATE = "Copyright 2010: Apache Software Foundation";

  public static final String RSS_FORMAT_STR = "E, dd MMM yyyy HH:mm:ss z";

  public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
      RSS_FORMAT_STR);

  /**
   * Default constructor.
   */
  public RSSProductServlet() {
  }

  /**
   * Initializes the servlet.
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    String fileManagerUrl;
	try {
		fileManagerUrl = PathUtils.replaceEnvVariables(config.getServletContext().getInitParameter(
		    "filemgr.url") );
	} catch (Exception e) {
		throw new ServletException("Failed to get filemgr url : " + e.getMessage(), e);
	}

    this.getFileManager(fileManagerUrl);
    try {
      this.conf = RSSUtils.initRSS(config);
    } catch (FileNotFoundException e) {
      throw new ServletException(e);
    }
  }

  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, java.io.IOException {
    doIt(req, resp);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, java.io.IOException {
    doIt(req, resp);
  }

  public void doIt(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, java.io.IOException {

    // need to know the type of product to get products for
    String productTypeName = req.getParameter("channel");
    String productTypeId = req.getParameter("id");
    String topN = req.getParameter("topn");
    ProductType type = null;
    int top = 20;

    if (topN != null) {
      top = Integer.valueOf(topN);
    }

    String requestUrl = req.getRequestURL().toString();
    String base = requestUrl.substring(0, requestUrl.lastIndexOf('/'));

    Metadata channelMet = new Metadata();
    channelMet.addMetadata("ProductType", productTypeName);
    channelMet.addMetadata("ProductTypeId", productTypeId);
    channelMet.addMetadata("TopN", String.valueOf(topN));
    channelMet.addMetadata("BaseUrl", base);

    List products;

    try {
      if (productTypeName.equals("ALL")) {
        products = fm.getTopNProducts(top);
      } else {

        try {
          type = fm.getProductTypeById(productTypeId);
        } catch (RepositoryManagerException e) {
          LOG.log(Level.SEVERE,
              "Unable to obtain product type from product type id: ["
                  + productTypeId + "]: Message: " + e.getMessage());
          return;
        }

        products = fm.getTopNProducts(top, type);
      }

    } catch (CatalogException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG
          .log(Level.WARNING,
              "Exception getting products from Catalog: Message: "
                  + e.getMessage());
      return;
    }

    if (products != null && products.size() > 0) {
      String channelDesc;

      if (!productTypeName.equals("ALL")) {
        channelDesc = type.getDescription();
      } else {
        channelDesc = "ALL";
      }

      try {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc = factory.newDocumentBuilder().newDocument();

        Element rss = XMLUtils.addNode(doc, doc, "rss");
        XMLUtils.addAttribute(doc, rss, "version", "2.0");
        
        // add namespace attributes from config file to rss tag
        for (RSSNamespace namespace : this.conf.getNamespaces()) 
        {
          XMLUtils.addAttribute(doc, rss, "xmlns:" + namespace.getPrefix(), namespace.getUri());
        }
        
        Element channel = XMLUtils.addNode(doc, rss, "channel");

        XMLUtils.addNode(doc, channel, "title", productTypeName);
        XMLUtils.addNode(doc, channel, "link", RSSUtils.getChannelLink(
            this.conf.getChannelLink(), channelMet));
        XMLUtils.addNode(doc, channel, "description", channelDesc);

        String buildPubDate = dateFormatter.format(new Date());

        XMLUtils.addNode(doc, channel, "language", "en-us");
        XMLUtils.addNode(doc, channel, "copyright", COPYRIGHT_BOILER_PLATE);
        XMLUtils.addNode(doc, channel, "pubDate", buildPubDate);
        XMLUtils.addNode(doc, channel, "category", productTypeName);
        XMLUtils.addNode(doc, channel, "generator", "CAS File Manager");
        XMLUtils.addNode(doc, channel, "lastBuildDate", buildPubDate);

        for (Object product : products) {
          Product p = (Product) product;

          String productTypeIdStr = p.getProductType().getProductTypeId();
          ProductType productType;

          try {
            productType = fm.getProductTypeById(productTypeIdStr);
          } catch (RepositoryManagerException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.SEVERE,
                "Unable to obtain product type from product type id: ["
                + ((Product) products.get(0)).getProductType()
                                             .getProductTypeId() + "]: Message: " + e.getMessage());
            return;
          }

          p.setProductType(productType);
          p.setProductReferences(safeGetProductReferences(p));

          Element item = XMLUtils.addNode(doc, channel, "item");

          XMLUtils.addNode(doc, item, "title", p.getProductName());
          XMLUtils.addNode(doc, item, "description", p.getProductType()
                                                      .getName());
          XMLUtils.addNode(doc, item, "link", base + "/data?productID="
                                              + p.getProductId());

          Metadata m = this.safeGetMetadata(p);
          if (m == null){
            LOG.warning("Cannot identify metadata for product: "+p.getProductId()+": setting default met object and received time.");
            m = new Metadata();
            m.addMetadata("CAS.ProductReceivedTime", DateConvert.isoFormat(new Date()));
          }
          String productReceivedTime = m.getMetadata("CAS.ProductReceivedTime");
          Date receivedTime = null;

          try {
            receivedTime = DateConvert.isoParse(productReceivedTime);
          } catch (ParseException ignore) {
          }

          if (receivedTime != null) {
            XMLUtils.addNode(doc, item, "pubDate", dateFormatter
                .format(receivedTime));
          }

          // add met field for FileSize for use in RSS envelope
          if (p.getProductReferences() != null
              && p.getProductReferences().size() == 1) {
            m.addMetadata("FileSize", String.valueOf(p.getProductReferences()
                                                      .get(0).getFileSize()));
          }

          // add additional elements from the RSSConfig
          for (RSSTag tag : this.conf.getTags()) {
            item.appendChild(RSSUtils.emitRSSTag(tag, m, doc, item));
          }

        }

        DOMSource source = new DOMSource(doc);
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();
        transformer.setOutputProperty("indent", "yes");
        StreamResult result = new StreamResult(resp.getOutputStream());
        resp.setContentType("text/xml");
        transformer.transform(source, result);

      } catch (ParserConfigurationException e) {
        throw new ServletException(e);
      } catch (TransformerException e) {
        throw new ServletException(e);
      }

    }

  }

  private Metadata safeGetMetadata(Product p) {
    try {
      return fm.getMetadata(p);
    } catch (CatalogException ignore) {
      LOG.log(Level.SEVERE, ignore.getMessage());
      return null;
    }
  }

  private List<Reference> safeGetProductReferences(Product p) {
    try {
      return fm.getProductReferences(p);
    } catch (CatalogException ignore) {
      LOG.log(Level.SEVERE, ignore.getMessage());
      return null;
    }
  }

  private void getFileManager(String fileManagerUrl) {
    try {
      fm = RpcCommunicationFactory.createClient(new URL(fileManagerUrl));
    } catch (MalformedURLException e) {
      LOG.log(Level.SEVERE,
          "Unable to initialize file manager url in RSS Servlet: [url="
              + fileManagerUrl + "], Message: " + e.getMessage());
    } catch (ConnectionException e) {
      LOG.log(Level.SEVERE,
          "Unable to initialize file manager url in RSS Servlet: [url="
              + fileManagerUrl + "], Message: " + e.getMessage());
    }
  }

  @Override
  public void finalize() throws IOException {
    if (fm != null) {
      fm.close();
    }
  }
}
