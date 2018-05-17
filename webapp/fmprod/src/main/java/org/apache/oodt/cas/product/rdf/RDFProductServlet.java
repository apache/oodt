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


package org.apache.oodt.cas.product.rdf;

//JDK imports
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.xml.XMLUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;
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

//OODT imports

/**
 * 
 * A Servlet that exports Product metaata information using the <a
 * href="http://www.w3.org">W3C's</a> <a href="http://www.w3.org/RDF/"> Resource
 * Description Framework</a>.
 * 
 * 
 * @author mattmann
 * @version $Revision$
 * 
 * 
 */
public class RDFProductServlet extends HttpServlet {

  /* serial Version UID */
  private static final long serialVersionUID = -3660991271646533985L;

  /* our client to the file manager */
  private FileManagerClient fClient = null;

  /* our log stream */
  private Logger LOG = Logger.getLogger(RDFProductServlet.class.getName());

  /* our RDF configuration */
  private RDFConfig rdfConf;

  /**
   * <p>
   * Default Constructor
   * </p>
   * .
   */
  public RDFProductServlet() {
  }

  /**
   * Initializes the servlet.
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    try {
      this.rdfConf = RDFUtils.initRDF(config);
    } catch (FileNotFoundException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new ServletException(e.getMessage());
    }

    String fileManagerUrl;
    try {
      fileManagerUrl = PathUtils.replaceEnvVariables(config.getServletContext().getInitParameter(
          "filemgr.url") );
    } catch (Exception e) {
      throw new ServletException("Failed to get filemgr url : " + e.getMessage(), e);
    }
    
    if (fileManagerUrl == null) {
      // try the default port
      fileManagerUrl = "http://localhost:9000";
    }

    fClient = null;

    try {
      fClient = RpcCommunicationFactory.createClient(new URL(fileManagerUrl));
    } catch (MalformedURLException e) {
      LOG.log(Level.SEVERE,
          "Unable to initialize file manager url in RDF Servlet: [url="
              + fileManagerUrl + "], Message: " + e.getMessage());
    } catch (ConnectionException e) {
      LOG.log(Level.SEVERE,
          "Unable to initialize file manager url in RDF Servlet: [url="
              + fileManagerUrl + "], Message: " + e.getMessage());
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
      throws ServletException {

    // need to know the type of product to get products for
    String productTypeName = req.getParameter("type");
    String productTypeId = req.getParameter("id");
    ProductType type = null;

    List<Product> products;

    try {
      if (productTypeName.equals("ALL")) {
        products = aggregatePagedProducts();
      } else {

        try {
          type = fClient.getProductTypeById(productTypeId);
        } catch (RepositoryManagerException e) {
          LOG.log(Level.SEVERE,
              "Unable to obtain product type from product type id: ["
                  + productTypeId + "]: Message: " + e.getMessage());
          return;
        }

        products = fClient.getProductsByProductType(type);
      }

    } catch (CatalogException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG
          .log(Level.WARNING,
              "Exception getting products from Catalog: Message: "
                  + e.getMessage());
      return;
    }

    String requestUrl = req.getRequestURL().toString();
    String base = requestUrl.substring(0, requestUrl.lastIndexOf('/'))
        + "/data";

    // allow override through the "baseUrl" GET parameter
    if (req.getParameter("baseUrl") != null) {
      base = req.getParameter("baseUrl");
    }

    if (products != null && products.size() > 0) {
      outputRDF(products, type, base, resp);
    }

  }

  public void outputRDF(List<Product> products, ProductType type, String base,
      HttpServletResponse resp) throws ServletException {

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      Document doc = factory.newDocumentBuilder().newDocument();

      Element rdf = XMLUtils.addNode(doc, doc, "rdf:RDF");
      RDFUtils.addNamespaces(doc, rdf, this.rdfConf);

      for (Product p : products) {
        String productTypeIdStr = p.getProductType().getProductTypeId();
        ProductType productType;

        if (type != null) {
          productType = type;
        } else {
          try {
            productType = fClient.getProductTypeById(productTypeIdStr);
          } catch (RepositoryManagerException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.SEVERE,
                "Unable to obtain product type from product type id: ["
                + ((Product) products.get(0)).getProductType()
                                             .getProductTypeId() + "]: Message: " + e.getMessage());
            return;
          }
        }

        p.setProductType(productType);

        Element productRdfDesc = XMLUtils.addNode(doc, rdf, this.rdfConf
                                                                .getTypeNs(productType.getName())
                                                            + ":" + productType.getName());
        XMLUtils.addAttribute(doc, productRdfDesc, "rdf:about", base
                                                                + "?productID=" + p.getProductId());

        // now add all its metadata
        Metadata prodMetadata = safeGetMetadata(p);

        // for all of its metadata keys and values, loop through them
        // and add RDF nodes underneath the RdfDesc for this product

        if (prodMetadata != null) {
          for (String key : prodMetadata.getMap().keySet()) {
            List<String> vals = prodMetadata.getAllMetadata(key);

            if (vals != null && vals.size() > 0) {

              for (String val : vals) {
                String outputKey = key;
                if (outputKey.contains(" ")) {
                  outputKey = StringUtils.join(WordUtils.capitalizeFully(outputKey).split(
                      " "));
                }

                val = StringEscapeUtils.escapeXml(val);
                Element rdfElem = RDFUtils.getRDFElement(outputKey, val,
                    this.rdfConf, doc);
                productRdfDesc.appendChild(rdfElem);
              }

            }
          }
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
    } catch (IOException e) {
      throw new ServletException(e);
    }
  }

  private List<Product> aggregatePagedProducts() {
    List<ProductType> types = safeGetProductTypes();
    List<Product> products = null;

    if (types != null && types.size() > 0) {
      products = new Vector<Product>();
      for (ProductType type : types) {
        ProductPage page;

        try {
          page = fClient.getFirstPage(type);

          if (page != null) {

            while (true) {
              products.addAll(page.getPageProducts());
              if (!page.isLastPage()) {
                page = fClient.getNextPage(type, page);
              } else {
                break;
              }
            }
          }
        } catch (Exception ignore) {
        }

      }
    }

    return products;
  }

  private Metadata safeGetMetadata(Product p) {
    Metadata met = null;

    try {
      met = fClient.getMetadata(p);
    } catch (CatalogException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Error retrieving metadata for product: ["
          + p.getProductId() + "]: Message: " + e.getMessage());
    }

    return met;
  }

  private List<ProductType> safeGetProductTypes() {
    List<ProductType> types = null;

    try {
      types = fClient.getProductTypes();
    } catch (RepositoryManagerException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Error retrieving product types: Message: "
          + e.getMessage());
    }

    return types;
  }

}
