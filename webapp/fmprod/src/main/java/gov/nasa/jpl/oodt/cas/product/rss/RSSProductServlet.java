//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.product.rss;

//JDK imports
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.text.ParseException;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.CatalogException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Product;
import gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Reference;
import gov.nasa.jpl.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import gov.nasa.jpl.oodt.cas.commons.xml.XMLUtils;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import jpl.eda.util.DateConvert;

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
  private static XmlRpcFileManagerClient fm = null;

  /* rss config */
  private RSSConfig conf;

  /* our log stream */
  private Logger LOG = Logger.getLogger(RSSProductServlet.class.getName());

  private static final Map NS_MAP = new HashMap();

  public static final String COPYRIGHT_BOILER_PLATE = "Copyright 2006: California Institute of Technology";

  public static final String RSS_FORMAT_STR = "E, dd MMM yyyy HH:mm:ss z";

  public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
      RSS_FORMAT_STR);

  static {
    NS_MAP.put("cas", "http://oodt.jpl.nasa.gov/1.0/cas");
  }

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

    String fileManagerUrl = config.getServletContext().getInitParameter(
        "filemgr.url");
    if (fileManagerUrl == null) {
      // try the default port
      fileManagerUrl = "http://localhost:9000";
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
      top = Integer.valueOf(topN).intValue();
    }

    String requestUrl = req.getRequestURL().toString();
    String base = requestUrl.substring(0, requestUrl.lastIndexOf('/'));

    Metadata channelMet = new Metadata();
    channelMet.addMetadata("ProductType", productTypeName);
    channelMet.addMetadata("ProductTypeId", productTypeId);
    channelMet.addMetadata("TopN", String.valueOf(topN));
    channelMet.addMetadata("BaseUrl", base);

    List products = null;

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
      e.printStackTrace();
      LOG
          .log(Level.WARNING,
              "Exception getting products from Catalog: Message: "
                  + e.getMessage());
      return;
    }

    if (products != null && products.size() > 0) {
      String channelDesc = null;

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
        XMLUtils
            .addAttribute(doc, rss, "xmlns:cas", (String) NS_MAP.get("cas"));
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

        for (Iterator i = products.iterator(); i.hasNext();) {
          Product p = (Product) i.next();

          String productTypeIdStr = p.getProductType().getProductTypeId();
          ProductType productType = null;

          try {
            productType = fm.getProductTypeById(productTypeIdStr);
          } catch (RepositoryManagerException e) {
            e.printStackTrace();
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
      ignore.printStackTrace();
      return null;
    }
  }

  private List<Reference> safeGetProductReferences(Product p) {
    try {
      return fm.getProductReferences(p);
    } catch (CatalogException ignore) {
      ignore.printStackTrace();
      return null;
    }
  }

  private void getFileManager(String fileManagerUrl) {
    try {
      this.fm = new XmlRpcFileManagerClient(new URL(fileManagerUrl));
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

}
