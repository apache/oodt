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
import java.util.List;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
//OODT imports
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.xml.XMLUtils;

/**
 * 
 * A Servlet that exports ProductType metaata information using the <a
 * href="http://www.w3.org">W3C's</a> <a href="http://www.w3.org/RDF/"> Resource
 * Description Framework</a>.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 * 
 */
public class RDFDatasetServlet extends HttpServlet {

  /* serial Version UID */
  private static final long serialVersionUID = -3660991271642533985L;

  /* our client to the file manager */
  private static FileManagerClient fClient = null;

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
  public RDFDatasetServlet() {
  }

  /**
   * Initializes the servlet.
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    try {
      this.rdfConf = RDFUtils.initRDF(config);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new ServletException(e.getMessage());
    }

    String fileManagerUrl = null;
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
      throws ServletException, java.io.IOException {

    // need to know the type of product to get products for
    String productTypeName = req.getParameter("type");
    String productTypeId = req.getParameter("typeID");
    ProductTypeFilter filter = new ProductTypeFilter(req.getParameter("filter"));
    ProductType type = null;

    List<ProductType> productTypes = new Vector<ProductType>();

    if (productTypeName.equals("ALL")) {
      productTypes = safeGetProductTypes();
    } else {

      try {
        type = fClient.getProductTypeById(productTypeId);
        productTypes.add(type);
      } catch (RepositoryManagerException e) {
        LOG.log(Level.SEVERE,
            "Unable to obtain product type from product type id: ["
                + productTypeId + "]: Message: " + e.getMessage());
        return;
      }
    }

    String requestUrl = req.getRequestURL().toString();
    String origBase = requestUrl.substring(0, requestUrl.lastIndexOf('/'));
    String base = origBase.substring(0, origBase.lastIndexOf('/')) + "/dataset";

    // allow override through the "baseUrl" GET parameter
    if (req.getParameter("baseUrl") != null) {
      base = req.getParameter("baseUrl");
    }
    
    if (productTypes != null && productTypes.size() > 0) {
      List<ProductType> subsetList = new Vector<ProductType>();
      
      // perform filtering
      for(ProductType pt: productTypes){
        if(filter.filter(pt)){
          subsetList.add(pt);
        }
      }
      
      outputRDF(subsetList, base, resp);
    }

  }

  public void outputRDF(List<ProductType> productTypes, String base, HttpServletResponse resp)
      throws ServletException {

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      Document doc = factory.newDocumentBuilder().newDocument();

      Element rdf = XMLUtils.addNode(doc, doc, "rdf:RDF");
      RDFUtils.addNamespaces(doc, rdf, this.rdfConf);

      for (Iterator<ProductType> i = productTypes.iterator(); i.hasNext();) {
        ProductType type = i.next();
        
        Element productTypeRdfDesc = XMLUtils.addNode(doc, rdf, this.rdfConf
            .getTypeNs(type.getName())
            + ":" + type.getName());
        XMLUtils.addAttribute(doc, productTypeRdfDesc, "rdf:about", base
            + "?typeID=" + type.getProductTypeId());

        // for all of its metadata keys and values, loop through them
        // and add RDF nodes underneath the RdfDesc for this product

        if (type.getTypeMetadata() != null) {
          for (Iterator<String> j = type.getTypeMetadata().getHashtable().keySet()
              .iterator(); j.hasNext();) {
            String key = (String) j.next();

            List<String> vals = type.getTypeMetadata().getAllMetadata(key);

            if (vals != null && vals.size() > 0) {

              for (Iterator<String> k = vals.iterator(); k.hasNext();) {
                String val = (String) k.next();
                //OODT-665 fix, take keys like 
                //PRODUCT Experiment Type
                //and transform it into ProductExperimentType
                String outputKey = key;
                if (outputKey.indexOf(" ") != -1) {
                  outputKey = StringUtils.join(WordUtils.capitalizeFully(outputKey).split(
                      " "));
                }
                
                val = StringEscapeUtils.escapeXml(val);
                Element rdfElem = RDFUtils.getRDFElement(outputKey, val,
                    this.rdfConf, doc);
                productTypeRdfDesc.appendChild(rdfElem);
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

  private List<ProductType> safeGetProductTypes() {
    List<ProductType> types = null;

    try {
      types = fClient.getProductTypes();
    } catch (RepositoryManagerException e) {
      e.printStackTrace();
      LOG.log(Level.WARNING, "Error retrieving product types: Message: "
          + e.getMessage());
    }

    return types;
  }

}
