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

import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.util.DateConvert;
import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import static org.apache.oodt.cas.product.rss.RSSConfigMetKeys.RSS_TRANSFER_CONF_KEY;


/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Servlet that supports the <a
 * href="http://feedvalidator.org/docs/rss2.html">RSS 2.0</a> specification for
 * delivering Product Transfer Feeds.
 * </p>
 * 
 */
public class RSSProductTransferServlet extends HttpServlet {

    /* serial Version UID */
    private static final long serialVersionUID = -7983832512818339079L;

    /* our client to the file manager */
    private FileManagerClient fClient = null;

    /* RSS config */
    private RSSConfig rssconf;

    /* our log stream */
    private Logger LOG = Logger.getLogger(RSSProductTransferServlet.class
            .getName());

    public static final String COPYRIGHT_BOILER_PLATE = "Copyright 2010: Apache Software Foundation";

    public static final String RSS_FORMAT_STR = "E, dd MMM yyyy HH:mm:ss z";

    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
            RSS_FORMAT_STR);

    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public RSSProductTransferServlet() {
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

      fClient = null;

        try {
            fClient = RpcCommunicationFactory.createClient(new URL(fileManagerUrl));
        } catch (MalformedURLException e) {
            LOG.log(Level.SEVERE,
                    "Unable to initialize file manager url in RSS Servlet: [url="
                            + fileManagerUrl + "], Message: " + e.getMessage());
        } catch (ConnectionException e) {
            LOG.log(Level.SEVERE,
                    "Unable to initialize file manager url in RSS Servlet: [url="
                            + fileManagerUrl + "], Message: " + e.getMessage());
        }

        try
        {
          rssconf = RSSConfigReader.readConfig(new File(PathUtils
              .replaceEnvVariables(config.getServletContext().getInitParameter(
                  (RSS_TRANSFER_CONF_KEY)))));
        }
        catch (FileNotFoundException e)
        {
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
        List currentTransfers;

        try {
            currentTransfers = fClient.getCurrentFileTransfers();
        } catch (DataTransferException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception getting current transfers from file manager: Message: "
                            + e.getMessage());
            return;
        }

        String requestUrl = req.getRequestURL().toString();
        String base = requestUrl.substring(0, requestUrl.lastIndexOf('/'));

        if (currentTransfers != null && currentTransfers.size() > 0) {
            String channelDesc = "Current Files Being Transferred to the File Manager";

            try {

                DocumentBuilderFactory factory = DocumentBuilderFactory
                        .newInstance();
                factory.setNamespaceAware(true);
                Document doc = factory.newDocumentBuilder().newDocument();

                Element rss = XMLUtils.addNode(doc, doc, "rss");
                XMLUtils.addAttribute(doc, rss, "version", "2.0");

                // add namespace attributes from config file to rss tag
                for (RSSNamespace namespace : rssconf.getNamespaces())
                {
                  XMLUtils.addAttribute(doc, rss,
                      "xmlns:" + namespace.getPrefix(), namespace.getUri());
                }

                Element channel = XMLUtils.addNode(doc, rss, "channel");

                XMLUtils.addNode(doc, channel, "title",
                        "File Manager Transfers");
                XMLUtils.addNode(doc, channel, "link", base
                        + "/viewTransfers");
                XMLUtils.addNode(doc, channel, "description", channelDesc);

                String buildPubDate = dateFormatter.format(new Date());

                XMLUtils.addNode(doc, channel, "language", "en-us");
                XMLUtils.addNode(doc, channel, "copyright",
                        COPYRIGHT_BOILER_PLATE);
                XMLUtils.addNode(doc, channel, "pubDate", buildPubDate);
                XMLUtils.addNode(doc, channel, "category", "data transfer");
                XMLUtils.addNode(doc, channel, "generator", "CAS File Manager");
                XMLUtils.addNode(doc, channel, "lastBuildDate", buildPubDate);

              for (Object currentTransfer : currentTransfers) {
                FileTransferStatus status = (FileTransferStatus) currentTransfer;

                Element item = XMLUtils.addNode(doc, channel, "item");

                XMLUtils.addNode(doc, item, "title", status
                    .getParentProduct().getProductName());
                XMLUtils.addNode(doc, item, "description", status
                    .getParentProduct().getProductType().getName());
                XMLUtils.addNode(doc, item, "link", base
                                                    + "/viewTransfer?ref="
                                                    + status.getFileRef().getOrigReference() + "&size="
                                                    + status.getFileRef().getFileSize());

                Metadata m = null;

                try {
                  m = fClient.getMetadata(status.getParentProduct());

                  String productReceivedTime = m
                      .getMetadata("CAS.ProductReceivedTime");
                  Date receivedTime = null;

                  try {
                    receivedTime = DateConvert
                        .isoParse(productReceivedTime);
                  } catch (ParseException ignore) {
                  }

                  if (receivedTime != null) {
                    XMLUtils.addNode(doc, item, "pubDate",
                        dateFormatter.format(receivedTime));
                  }

                  // set product transfer metadata
                  m.addMetadata("BytesTransferred",
                      "" + status.getBytesTransferred());
                  m.addMetadata("TotalBytes",
                      "" + status.getFileRef().getFileSize());
                  m.addMetadata("PercentComplete",
                      "" + status.computePctTransferred());

                } catch (CatalogException ignore) {
                }

                // add additional elements from the RSSConfig
                for (RSSTag tag : rssconf.getTags()) {
                  item.appendChild(RSSUtils.emitRSSTag(tag, m, doc, item));
                }

              }

                DOMSource source = new DOMSource(doc);
                TransformerFactory transFactory = TransformerFactory
                        .newInstance();
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

}
