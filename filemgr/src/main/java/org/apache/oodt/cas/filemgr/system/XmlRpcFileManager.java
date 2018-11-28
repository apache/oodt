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

package org.apache.oodt.cas.filemgr.system;

//APACHE imports
import org.apache.xmlrpc.WebServer;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.commons.date.DateUtils;
import org.apache.oodt.cas.filemgr.catalog.Catalog;
import org.apache.oodt.cas.filemgr.metadata.ProductMetKeys;
import org.apache.oodt.cas.filemgr.metadata.extractors.FilemgrMetExtractor;
import org.apache.oodt.cas.filemgr.repository.RepositoryManager;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.ExtractorSpec;
import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryFilter;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.structs.query.QueryResultComparator;
import org.apache.oodt.cas.filemgr.structs.query.filter.ObjectTimeEvent;
import org.apache.oodt.cas.filemgr.structs.query.filter.TimeEvent;
import org.apache.oodt.cas.filemgr.structs.type.TypeHandler;
import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.util.XmlRpcStructFactory;
import org.apache.oodt.cas.filemgr.versioning.Versioner;
import org.apache.oodt.cas.filemgr.versioning.VersioningUtils;
import org.apache.oodt.cas.filemgr.datatransfer.TransferStatusTracker;

import com.google.common.collect.Lists;




//JDK imports
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * An XML RPC-based File manager.
 * </p>
 * 
 */
public class XmlRpcFileManager {

    /* the port to run the XML RPC web server on, default is 1999 */
    private int webServerPort = 1999;

    /* our Catalog */
    private Catalog catalog = null;

    /* our RepositoryManager */
    private RepositoryManager repositoryManager = null;

    /* our DataTransfer */
    private DataTransfer dataTransfer = null;

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(XmlRpcFileManager.class.getName());

    /* our xml rpc web server */
    private WebServer webServer = null;

    /* our data transfer status tracker */
    private TransferStatusTracker transferStatusTracker = null;
    
    /* whether or not to expand a product instance into met */
    private boolean expandProductMet;
    
    /**
     * <p>
     * Creates a new XmlRpcFileManager with the given metadata store factory,
     * and the given data store factory, on the given port.
     * </p>
     * 
     * @param port
     *            The web server port to run the XML Rpc server on, defaults to
     *            1999.
     */
    public XmlRpcFileManager(int port) throws Exception {
        webServerPort = port;

        // start up the web server
        webServer = new WebServer(webServerPort);
        webServer.addHandler("filemgr", this);
        webServer.start();

        this.loadConfiguration();
        LOG.log(Level.INFO, "File Manager started by "
                + System.getProperty("user.name", "unknown"));

    }
    
    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    public boolean isAlive() {
        return true;
    }
    
    public boolean refreshConfigAndPolicy() {
      boolean status = false;

      try {
        this.loadConfiguration();
        status = true;
      } catch (Exception e) {
        e.printStackTrace();
        LOG
            .log(
                Level.SEVERE,
                "Unable to refresh configuration for file manager " +
                "server: server may be in inoperable state: Message: "
                    + e.getMessage());
      }
  
      return status;
    }

    public boolean transferringProduct(Hashtable<String, Object> productHash) {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        transferStatusTracker.transferringProduct(p);
        return true;
    }

    public Hashtable<String, Object> getCurrentFileTransfer() {
        FileTransferStatus status = transferStatusTracker
                .getCurrentFileTransfer();
        if (status == null) {
            return new Hashtable<String, Object>();
        } else
            return XmlRpcStructFactory.getXmlRpcFileTransferStatus(status);
    }

    public Vector<Hashtable<String, Object>> getCurrentFileTransfers() {
        List<FileTransferStatus> currentTransfers = transferStatusTracker.getCurrentFileTransfers();

        if (currentTransfers != null && currentTransfers.size() > 0) {
            return XmlRpcStructFactory
                    .getXmlRpcFileTransferStatuses(currentTransfers);
        } else
            return new Vector<Hashtable<String, Object>>();
    }

    public double getProductPctTransferred(Hashtable<String, Object> productHash) {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        double pct = transferStatusTracker.getPctTransferred(product);
        return pct;
    }

    public double getRefPctTransferred(Hashtable<String, Object> refHash) {
        Reference reference = XmlRpcStructFactory
                .getReferenceFromXmlRpc(refHash);
        double pct = 0.0;

        try {
            pct = transferStatusTracker.getPctTransferred(reference);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception getting transfer percentage for ref: ["
                            + reference.getOrigReference() + "]: Message: "
                            + e.getMessage());
        }
        return pct;
    }

    public boolean removeProductTransferStatus(Hashtable<String, Object> productHash) {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        transferStatusTracker.removeProductTransferStatus(product);
        return true;
    }

    public boolean isTransferComplete(Hashtable<String, Object> productHash) {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return transferStatusTracker.isTransferComplete(product);
    }

    public Hashtable<String, Object> pagedQuery(
            Hashtable<String, Object> queryHash,
            Hashtable<String, Object> productTypeHash,
            int pageNum) throws CatalogException {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        Query query = XmlRpcStructFactory.getQueryFromXmlRpc(queryHash);

        ProductPage prodPage = null;

        try {
            prodPage = catalog.pagedQuery(this.getCatalogQuery(query, type), type, pageNum);

            if (prodPage == null) {
                prodPage = ProductPage.blankPage();
            } else {
                // it is possible here that the underlying catalog did not
                // set the ProductType
                // to obey the contract of the File Manager, we need to make
                // sure its set here
                setProductType(prodPage.getPageProducts());
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Catalog exception performing paged query for product type: ["
                            + type.getProductTypeId() + "] query: [" + query
                            + "]: Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        }

        return XmlRpcStructFactory.getXmlRpcProductPage(prodPage);
    }

    public Hashtable<String, Object> getFirstPage(
            Hashtable<String, Object> productTypeHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        ProductPage page = catalog.getFirstPage(type);
        try {
            setProductType(page.getPageProducts());
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                    "Unable to set product types for product page list: ["
                            + page + "]");
        }
        return XmlRpcStructFactory.getXmlRpcProductPage(page);
    }

    public Hashtable<String, Object> getLastPage(
            Hashtable<String, Object> productTypeHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        ProductPage page = catalog.getLastProductPage(type);
        try {
            setProductType(page.getPageProducts());
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                    "Unable to set product types for product page list: ["
                            + page + "]");
        }
        return XmlRpcStructFactory.getXmlRpcProductPage(page);
    }

    public Hashtable<String, Object> getNextPage(
            Hashtable<String, Object> productTypeHash,
            Hashtable<String, Object> currentPageHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        ProductPage currPage = XmlRpcStructFactory
                .getProductPageFromXmlRpc(currentPageHash);
        ProductPage page = catalog.getNextPage(type, currPage);
        try {
            setProductType(page.getPageProducts());
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                    "Unable to set product types for product page list: ["
                            + page + "]");
        }
        return XmlRpcStructFactory.getXmlRpcProductPage(page);
    }

    public Hashtable<String, Object> getPrevPage(
            Hashtable<String, Object> productTypeHash,
            Hashtable<String, Object> currentPageHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        ProductPage currPage = XmlRpcStructFactory
                .getProductPageFromXmlRpc(currentPageHash);
        ProductPage page = catalog.getPrevPage(type, currPage);
        try {
            setProductType(page.getPageProducts());
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                    "Unable to set product types for product page list: ["
                            + page + "]");
        }
        return XmlRpcStructFactory.getXmlRpcProductPage(page);
    }

    public String addProductType(Hashtable<String, Object> productTypeHash)
            throws RepositoryManagerException {
        ProductType productType = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        repositoryManager.addProductType(productType);
        return productType.getProductTypeId();

    }

    public synchronized boolean setProductTransferStatus(
            Hashtable<String, Object> productHash)
            throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        catalog.setProductTransferStatus(product);
        return true;
    }

    public int getNumProducts(Hashtable<String, Object> productTypeHash)
            throws CatalogException {
        int numProducts = -1;

        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);

        try {
            numProducts = catalog.getNumProducts(type);
        } catch (CatalogException e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception when getting num products: Message: "
                            + e.getMessage());
            throw new CatalogException(e.getMessage());
        }

        return numProducts;
    }

    public Vector<Hashtable<String, Object>> getTopNProducts(int n)
            throws CatalogException {
        List<Product> topNProducts = null;

        try {
            topNProducts = catalog.getTopNProducts(n);
            return XmlRpcStructFactory.getXmlRpcProductList(topNProducts);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception when getting topN products: Message: "
                            + e.getMessage());
            throw new CatalogException(e.getMessage());
        }
    }

    public Vector<Hashtable<String, Object>> getTopNProducts(int n,
            Hashtable<String, Object> productTypeHash)
            throws CatalogException {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        List<Product> topNProducts = null;

        try {
            topNProducts = catalog.getTopNProducts(n, type);
            return XmlRpcStructFactory.getXmlRpcProductList(topNProducts);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception when getting topN products by product type: ["
                            + type.getProductTypeId() + "]: Message: "
                            + e.getMessage());
            throw new CatalogException(e.getMessage());
        }

    }

    public boolean hasProduct(String productName) throws CatalogException {
        Product p = catalog.getProductByName(productName);
        return p != null
                && p.getTransferStatus().equals(Product.STATUS_RECEIVED);
    }

    public Hashtable<String, Object> getMetadata(
            Hashtable<String, Object> productHash) throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return this.getMetadata(product).getHashtable();
    }

    public Hashtable<String, Object> getReducedMetadata(
            Hashtable<String, Object> productHash, Vector<String> elements)
            throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return this.getReducedMetadata(product, elements).getHashtable();
    }

    public Vector<Hashtable<String, Object>> getProductTypes()
            throws RepositoryManagerException {
        List<ProductType> productTypeList = null;

        try {
            productTypeList = repositoryManager.getProductTypes();
            return XmlRpcStructFactory
                    .getXmlRpcProductTypeList(productTypeList);
        } catch (RepositoryManagerException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Unable to obtain product types from repository manager: Message: "
                            + e.getMessage());
            throw new RepositoryManagerException(e.getMessage());
        }
    }

    public Vector<Hashtable<String, Object>> getProductReferences(
            Hashtable<String, Object> productHash)
            throws CatalogException {
        List<Reference> referenceList = null;
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);

        try {
            referenceList = catalog.getProductReferences(product);
            return XmlRpcStructFactory.getXmlRpcReferences(referenceList);
        } catch (CatalogException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Unable to obtain references for product: ["
                    + product.getProductName() + "]: Message: "
                    + e.getMessage());
            throw new CatalogException(e.getMessage());
        }

    }

    public Hashtable<String, Object> getProductById(String productId)
            throws CatalogException {
        Product product = null;

        try {
            product = catalog.getProductById(productId);
            // it is possible here that the underlying catalog did not
            // set the ProductType
            // to obey the contract of the File Manager, we need to make
            // sure its set here
            product.setProductType(this.repositoryManager
                    .getProductTypeById(product.getProductType()
                            .getProductTypeId()));
            return XmlRpcStructFactory.getXmlRpcProduct(product);
        } catch (CatalogException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Unable to obtain product by id: ["
                    + productId + "]: Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        } catch (RepositoryManagerException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Unable to obtain product type by id: ["
                    + product.getProductType().getProductTypeId()
                    + "]: Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        }

    }

    public Hashtable<String, Object> getProductByName(String productName)
            throws CatalogException {
        Product product = null;

        try {
            product = catalog.getProductByName(productName);
            // it is possible here that the underlying catalog did not
            // set the ProductType
            // to obey the contract of the File Manager, we need to make
            // sure its set here
            product.setProductType(this.repositoryManager
                    .getProductTypeById(product.getProductType()
                            .getProductTypeId()));
            return XmlRpcStructFactory.getXmlRpcProduct(product);
        } catch (CatalogException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Unable to obtain product by name: ["
                    + productName + "]: Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        } catch (RepositoryManagerException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Unable to obtain product type by id: ["
                    + product.getProductType().getProductTypeId()
                    + "]: Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        }
    }

    public Vector<Hashtable<String, Object>> getProductsByProductType(
            Hashtable<String, Object> productTypeHash)
            throws CatalogException {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        List<Product> productList = null;

        try {
            productList = catalog.getProductsByProductType(type);
            return XmlRpcStructFactory.getXmlRpcProductList(productList);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Exception obtaining products by product type for type: ["
                            + type.getName() + "]: Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        }
    }

    public Vector<Hashtable<String, Object>> getElementsByProductType(
            Hashtable<String, Object> productTypeHash)
            throws ValidationLayerException {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        List<Element> elementList = null;

        try {
            elementList = catalog.getValidationLayer().getElements(type);
            return XmlRpcStructFactory.getXmlRpcElementList(elementList);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Exception obtaining elements for product type: ["
                            + type.getName() + "]: Message: " + e.getMessage());
            throw new ValidationLayerException(e.getMessage());
        }

    }

    public Hashtable<String, Object> getElementById(String elementId)
            throws ValidationLayerException {
        Element element = null;

        try {
            element = catalog.getValidationLayer().getElementById(elementId);
            return XmlRpcStructFactory.getXmlRpcElement(element);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "exception retrieving element by id: ["
                    + elementId + "]: Message: " + e.getMessage());
            throw new ValidationLayerException(e.getMessage());
        }
    }

    public Hashtable<String, Object> getElementByName(String elementName)
            throws ValidationLayerException {
        Element element = null;

        try {
            element = catalog.getValidationLayer()
                    .getElementByName(elementName);
            return XmlRpcStructFactory.getXmlRpcElement(element);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "exception retrieving element by name: ["
                    + elementName + "]: Message: " + e.getMessage());
            throw new ValidationLayerException(e.getMessage());
        }
    }

    public Vector<Hashtable<String, Object>> complexQuery(
            Hashtable<String, Object> complexQueryHash) throws CatalogException {
        try {
            ComplexQuery complexQuery = XmlRpcStructFactory
                    .getComplexQueryFromXmlRpc(complexQueryHash);

            // get ProductTypes
            List<ProductType> productTypes = null;
            if (complexQuery.getReducedProductTypeNames() == null) {
                productTypes = this.repositoryManager.getProductTypes();
            } else {
                productTypes = new Vector<ProductType>();
                for (String productTypeName : complexQuery
                        .getReducedProductTypeNames())
                    productTypes.add(this.repositoryManager
                            .getProductTypeByName(productTypeName));
            }

            // get Metadata
            List<QueryResult> queryResults = new LinkedList<QueryResult>();
            for (ProductType productType : productTypes) {
                List<String> productIds = catalog.query(this.getCatalogQuery(
                        complexQuery, productType), productType);
                for (String productId : productIds) {
                    Product product = catalog.getProductById(productId);
                    product.setProductType(productType);
                    QueryResult qr = new QueryResult(product, this
                            .getReducedMetadata(product, complexQuery
                                    .getReducedMetadata()));
                    qr.setToStringFormat(complexQuery
                            .getToStringResultFormat());
                    queryResults.add(qr);
                }
            }

            LOG.log(Level.INFO, "Query returned " + queryResults.size()
                    + " results");

            // filter query results
            if (complexQuery.getQueryFilter() != null) {
                queryResults = applyFilterToResults(queryResults, complexQuery
                        .getQueryFilter());
                LOG.log(Level.INFO, "Filter returned " + queryResults.size()
                        + " results");
            }

            // sort query results
            if (complexQuery.getSortByMetKey() != null)
                queryResults = sortQueryResultList(queryResults, complexQuery
                        .getSortByMetKey());

            return XmlRpcStructFactory.getXmlRpcQueryResults(queryResults);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CatalogException("Failed to perform complex query : "
                    + e.getMessage());
        }
    }
    
    public Vector<Hashtable<String, Object>> query(
            Hashtable<String, Object> queryHash,
            Hashtable<String, Object> productTypeHash)
            throws CatalogException {
        Query query = XmlRpcStructFactory.getQueryFromXmlRpc(queryHash);
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        return XmlRpcStructFactory.getXmlRpcProductList(this.query(query, type));
    }

    public Hashtable<String, Object> getProductTypeByName(String productTypeName)
            throws RepositoryManagerException {
        ProductType type = repositoryManager
                .getProductTypeByName(productTypeName);
        return XmlRpcStructFactory.getXmlRpcProductType(type);
    }

    public Hashtable<String, Object> getProductTypeById(String productTypeId)
            throws RepositoryManagerException {
        ProductType type = null;

        try {
            type = repositoryManager.getProductTypeById(productTypeId);
            return XmlRpcStructFactory.getXmlRpcProductType(type);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Exception obtaining product type by id for product type: ["
                            + productTypeId + "]: Message: " + e.getMessage());
            throw new RepositoryManagerException(e.getMessage());
        }
    }
    
    public synchronized boolean updateMetadata(Hashtable<String, Object> productHash, 
        Hashtable<String, Object> metadataHash) throws CatalogException{
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        Metadata met = new Metadata();
        met.addMetadata(metadataHash);
        Metadata oldMetadata = catalog.getMetadata(product);
        catalog.removeMetadata(oldMetadata, product);
        catalog.addMetadata(met, product);
        return true;
    }

    public synchronized String catalogProduct(Hashtable<String, Object> productHash)
            throws CatalogException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return catalogProduct(p);
    }

    public synchronized boolean addMetadata(Hashtable<String, Object> productHash,
            Hashtable<String, String> metadata) throws CatalogException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        Metadata m = new Metadata();
        m.addMetadata((Hashtable)metadata);
        return addMetadata(p, m) != null;
    }

    public synchronized boolean addProductReferences(Hashtable<String, Object> productHash)
            throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return addProductReferences(product);
    }

    public String ingestProduct(Hashtable<String, Object> productHash,
      Hashtable<String, String> metadata, boolean clientTransfer)
      throws VersioningException, RepositoryManagerException,
      DataTransferException, CatalogException {

    Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);

    try {
      // first, create the product
      p.setTransferStatus(Product.STATUS_TRANSFER);
      catalogProduct(p);

      // now add the metadata
      Metadata m = new Metadata();
      m.addMetadata((Hashtable)metadata);
      Metadata expandedMetdata = addMetadata(p, m);

      // version the product
      if (!clientTransfer || (clientTransfer
          && Boolean.getBoolean("org.apache.oodt.cas.filemgr.serverside.versioning"))) {
        Versioner versioner = null;
        try {
          versioner = GenericFileManagerObjectFactory
              .getVersionerFromClassName(p.getProductType().getVersioner());
          versioner.createDataStoreReferences(p, expandedMetdata);
        } catch (Exception e) {
          LOG.log(Level.SEVERE,
              "ingestProduct: VersioningException when versioning Product: "
                  + p.getProductName() + " with Versioner "
                  + p.getProductType().getVersioner() + ": Message: "
                  + e.getMessage());
          throw new VersioningException(e);
        }

        // add the newly versioned references to the data store
        addProductReferences(p);
      }

      if (!clientTransfer) {
        LOG.log(Level.FINEST,
              "File Manager: ingest: no client transfer enabled, "
                  + "server transfering product: [" + p.getProductName() + "]");

        // now transfer the product
        try {
          dataTransfer.transferProduct(p);
          // now update the product's transfer status in the data store
          p.setTransferStatus(Product.STATUS_RECEIVED);

          try {
            catalog.setProductTransferStatus(p);
          } catch (CatalogException e) {
            LOG.log(Level.SEVERE, "ingestProduct: CatalogException "
                + "when updating product transfer status for Product: "
                + p.getProductName() + " Message: " + e.getMessage());
            throw e;
          }
        } catch (Exception e) {
          LOG.log(Level.SEVERE,
              "ingestProduct: DataTransferException when transfering Product: "
                  + p.getProductName() + ": Message: " + e.getMessage());
          throw new DataTransferException(e);
        }
      }

      // that's it!
      return p.getProductId();
    } catch (Exception e) {
      e.printStackTrace();
      throw new CatalogException("Error ingesting product [" + p + "] : "
          + e.getMessage());
    }

  }

   public byte[] retrieveFile(String filePath, int offset, int numBytes)
         throws DataTransferException {
      FileInputStream is = null;
      try {
         byte[] fileData = new byte[numBytes];
         (is = new FileInputStream(filePath)).skip(offset);
         int bytesRead = is.read(fileData);
         if (bytesRead != -1) {
            byte[] fileDataTruncated = new byte[bytesRead];
            System.arraycopy(fileData, 0, fileDataTruncated, 0, bytesRead);
            return fileDataTruncated;
         } else {
            return new byte[0];
         }
      } catch (Exception e) {
         LOG.log(Level.SEVERE, "Failed to read '" + numBytes
               + "' bytes from file '" + filePath + "' at index '" + offset
               + "' : " + e.getMessage(), e);
         throw new DataTransferException("Failed to read '" + numBytes
               + "' bytes from file '" + filePath + "' at index '" + offset
               + "' : " + e.getMessage(), e);
      } finally {
         try { is.close(); } catch (Exception e) {}
      }
   }
    
    public boolean transferFile(String filePath, byte[] fileData, int offset,
            int numBytes) {
        File outFile = new File(filePath);
        boolean success = true;

        FileOutputStream fOut = null;

        if (outFile.exists()) {
            try {
                fOut = new FileOutputStream(outFile, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                LOG.log(Level.SEVERE,
                        "FileNotFoundException when trying to use RandomAccess file on "
                                + filePath + ": Message: " + e.getMessage());
                success = false;
            }
        } else {
            // create the output directory
            String outFileDirPath = outFile.getAbsolutePath().substring(0,
                    outFile.getAbsolutePath().lastIndexOf("/"));
            LOG.log(Level.INFO, "Outfile directory: " + outFileDirPath);
            File outFileDir = new File(outFileDirPath);
            outFileDir.mkdirs();

            try {
                fOut = new FileOutputStream(outFile, false);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                LOG.log(Level.SEVERE,
                        "FileNotFoundException when trying to use RandomAccess file on "
                                + filePath + ": Message: " + e.getMessage());
                success = false;
            }
        }

        if (success) {
            try {
                fOut.write(fileData, (int) offset, (int) numBytes);
            } catch (IOException e) {
                e.printStackTrace();
                LOG.log(Level.SEVERE, "IOException when trying to write file "
                        + filePath + ": Message: " + e.getMessage());
                success = false;
            } finally {
                if (fOut != null) {
                    try {
                        fOut.close();
                    } catch (Exception ignore) {
                    }

                    fOut = null;
                }
            }
        }

        outFile = null;
        return success;
    }

    public boolean moveProduct(Hashtable<String, Object> productHash, String newPath)
            throws DataTransferException {

        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);

        // first thing we care about is if the product is flat or heirarchical
        if (p.getProductStructure().equals(Product.STRUCTURE_FLAT)) {
            // we just need to get its first reference
            if (p.getProductReferences() == null
                    || (p.getProductReferences() != null && p
                            .getProductReferences().size() != 1)) {
                throw new DataTransferException(
                        "Flat products must have a single reference: cannot move");
            }

            // okay, it's fine to move it
            // first, we need to update the data store ref
            Reference r = (Reference) p.getProductReferences().get(0);
            if (r.getDataStoreReference().equals(
                    new File(newPath).toURI().toString())) {
                throw new DataTransferException("cannot move product: ["
                        + p.getProductName() + "] to same location: ["
                        + r.getDataStoreReference() + "]");
            }

            // create a copy of the current data store path: we'll need it to
            // do the data transfer
            Reference copyRef = new Reference(r);

            // update the copyRef to have the data store ref as the orig ref
            // the the newLoc as the new ref
            copyRef.setOrigReference(r.getDataStoreReference());
            copyRef.setDataStoreReference(new File(newPath).toURI().toString());

            p.getProductReferences().clear();
            p.getProductReferences().add(copyRef);

            // now transfer it
            try {
                this.dataTransfer.transferProduct(p);
            } catch (IOException e) {
                throw new DataTransferException(e.getMessage());
            }

            // now delete the original copy
            try {
                if (!new File(new URI(copyRef.getOrigReference())).delete()) {
                    LOG.log(Level.WARNING, "Deletion of original file: ["
                            + r.getDataStoreReference()
                            + "] on product move returned false");
                }
            } catch (URISyntaxException e) {
                throw new DataTransferException(
                        "URI Syntax exception trying to remove original product ref: Message: "
                                + e.getMessage());
            }

            // now save the updated reference
            try {
                this.catalog.modifyProduct(p);
                return true;
            } catch (CatalogException e) {
                throw new DataTransferException(e.getMessage());
            }
        } else
            throw new UnsupportedOperationException(
                    "Moving of heirarhical and stream products not supported yet");
    }

    public boolean removeFile(String filePath) throws DataTransferException, IOException {
      // TODO(bfoster): Clean this up so that it deletes by product not file.
      Product product = new Product();
      Reference r = new Reference();
      r.setDataStoreReference(filePath);
      product.setProductReferences(Lists.newArrayList(r));
      dataTransfer.deleteProduct(product);
      return true;
    }

    public boolean modifyProduct(Hashtable<?, ?> productHash) throws CatalogException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);

        try {
            catalog.modifyProduct(p);
        } catch (CatalogException e) {
            LOG.log(Level.WARNING, "Exception modifying product: ["
                    + p.getProductId() + "]: Message: " + e.getMessage(), e);
            throw e;
        }

        return true;
    }

    public boolean removeProduct(Hashtable<String, Object> productHash) throws CatalogException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);

        try {
            catalog.removeProduct(p);
        } catch (CatalogException e) {
            LOG.log(Level.WARNING, "Exception modifying product: ["
                    + p.getProductId() + "]: Message: " + e.getMessage(), e);
            throw e;
        }

        return true;
    }
    
    public Hashtable<String, Object> getCatalogValues(
            Hashtable<String, Object> metadataHash,
            Hashtable<String, Object> productTypeHash)
            throws RepositoryManagerException {
        Metadata m = new Metadata();
        m.addMetadata(metadataHash);
        ProductType productType = XmlRpcStructFactory.getProductTypeFromXmlRpc(productTypeHash);
        return this.getCatalogValues(m, productType).getHashtable();
    }
    
    public Hashtable<String, Object> getOrigValues(
            Hashtable<String, Object> metadataHash,
            Hashtable<String, Object> productTypeHash)
            throws RepositoryManagerException {
        Metadata m = new Metadata();
        m.addMetadata(metadataHash);
        ProductType productType = XmlRpcStructFactory.getProductTypeFromXmlRpc(productTypeHash);
        return this.getOrigValues(m, productType).getHashtable();
    }
    
    public Hashtable<String, Object> getCatalogQuery(
            Hashtable<String, Object> queryHash,
            Hashtable<String, Object> productTypeHash)
            throws RepositoryManagerException, QueryFormulationException {
        Query query = XmlRpcStructFactory.getQueryFromXmlRpc(queryHash);
        ProductType productType = XmlRpcStructFactory.getProductTypeFromXmlRpc(productTypeHash);
        return XmlRpcStructFactory.getXmlRpcQuery(this.getCatalogQuery(query, productType));
    }

    public static void main(String[] args) throws Exception {
        int portNum = -1;
        String usage = "FileManager --portNum <port number for xml rpc service>\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--portNum")) {
                portNum = Integer.parseInt(args[++i]);
            }
        }

        if (portNum == -1) {
            System.err.println(usage);
            System.exit(1);
        }
        
        @SuppressWarnings("unused")
        XmlRpcFileManager manager = new XmlRpcFileManager(portNum);

        for (;;)
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ignore) {
            }
    }

    public boolean shutdown() {
        if (this.webServer != null) {
            this.webServer.shutdown();
            this.webServer = null;
            return true;
        } else
            return false;
    }

    private synchronized String catalogProduct(Product p)
            throws CatalogException {
        try {
            catalog.addProduct(p);
        } catch (CatalogException e) {
            LOG.log(Level.SEVERE,
                    "ingestProduct: CatalogException when adding Product: "
                            + p.getProductName() + " to Catalog: Message: "
                            + e.getMessage());
            throw e;
        }

        return p.getProductId();
    }

    private synchronized Metadata addMetadata(Product p, Metadata m)
            throws CatalogException {
        
        //apply handlers
        try {
            m = this.getCatalogValues(m, p.getProductType());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to get handlers for product '" + p
                    + "' : " + e.getMessage());
        }
        
        // first do server side metadata extraction
        Metadata metadata = runExtractors(p, m);

        try {
            catalog.addMetadata(metadata, p);
        } catch (CatalogException e) {
            LOG.log(Level.SEVERE,
                    "ingestProduct: CatalogException when adding metadata "
                            + metadata + " for product: " + p.getProductName()
                            + ": Message: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "ingestProduct: General Exception when adding metadata "
                            + metadata + " for product: " + p.getProductName()
                            + ": Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        }

        return metadata;
    }

    private Metadata runExtractors(Product product, Metadata metadata) {
        // make sure that the product type definition is present
        try {
            product.setProductType(repositoryManager.getProductTypeById(product
                    .getProductType().getProductTypeId()));
        } catch (RepositoryManagerException e) {
            LOG.log(Level.SEVERE, "Failed to load ProductType " + product
              .getProductType().getProductTypeId(), e);
            return null;
        }

        Metadata met = new Metadata();
        met.addMetadata(metadata.getHashtable());

        if (product.getProductType().getExtractors() != null) {
            for (ExtractorSpec spec: product.getProductType().getExtractors()) {
                FilemgrMetExtractor extractor = GenericFileManagerObjectFactory
                        .getExtractorFromClassName(spec.getClassName());
                extractor.configure(spec.getConfiguration());
                LOG.log(Level.INFO, "Running Met Extractor: ["
                        + extractor.getClass().getName()
                        + "] for product type: ["
                        + product.getProductType().getName() + "]");
                try {
                    met = extractor.extractMetadata(product, met);
                } catch (MetExtractionException e) {
                    LOG.log(Level.SEVERE,
                            "Exception extractor metadata from product: ["
                                    + product.getProductName()
                                    + "]: using extractor: ["
                                    + extractor.getClass().getName()
                                    + "]: Message: " + e.getMessage(), e);
                }
            }
        }

        return met;
    }

    private synchronized boolean addProductReferences(Product product)
            throws CatalogException {
        catalog.addProductReferences(product);
        return true;
    }

    private void setProductType(List<Product> products) throws Exception {
        if (products != null && products.size() > 0) {
            for (Iterator<Product> i = products.iterator(); i.hasNext();) {
                Product p = i.next();
                try {
                    p.setProductType(repositoryManager.getProductTypeById(p
                            .getProductType().getProductTypeId()));
                } catch (RepositoryManagerException e) {
                    throw new Exception(e.getMessage());
                }
            }
        }
    }
    
    private List<Product> query(Query query, ProductType productType) throws CatalogException {
        List<String> productIdList = null;
        List<Product> productList = null;

        try {            
            productIdList = catalog.query(this.getCatalogQuery(query, productType), productType);

            if (productIdList != null && productIdList.size() > 0) {
                productList = new Vector<Product>(productIdList.size());
                for (Iterator<String> i = productIdList.iterator(); i.hasNext();) {
                    String productId = i.next();
                    Product product = catalog.getProductById(productId);
                    // it is possible here that the underlying catalog did not
                    // set the ProductType
                    // to obey the contract of the File Manager, we need to make
                    // sure its set here
                    product.setProductType(this.repositoryManager
                            .getProductTypeById(product.getProductType()
                                    .getProductTypeId()));
                    productList.add(product);
                }
                return productList;
            } else {
                return new Vector<Product>(); // null values not supported by XML-RPC
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Exception performing query against catalog for product type: ["
                            + productType.getName() + "] Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        }
    }
    
    private Metadata getReducedMetadata(Product product, List<String> elements) throws CatalogException {
        try {
            Metadata m = null;
            if (elements != null && elements.size() > 0) {
                m = catalog.getReducedMetadata(product, elements);
            }else {
                m = this.getMetadata(product);
            }
            if(this.expandProductMet) m = this.buildProductMetadata(product, m);            
            return this.getOrigValues(m, product.getProductType());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Exception obtaining metadata from catalog for product: ["
                            + product.getProductId() + "]: Message: "
                            + e.getMessage());
            throw new CatalogException(e.getMessage());
        }
    }
    
    private Metadata getMetadata(Product product) throws CatalogException {
        try {
            Metadata m = catalog.getMetadata(product);
            if(this.expandProductMet) m = this.buildProductMetadata(product, m);
            return this.getOrigValues(m, product.getProductType());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Exception obtaining metadata from catalog for product: ["
                            + product.getProductId() + "]: Message: "
                            + e.getMessage());
            throw new CatalogException(e.getMessage());
        }
    }
    
    private Metadata getOrigValues(Metadata metadata, ProductType productType) 
            throws RepositoryManagerException {
        List<TypeHandler> handlers = this.repositoryManager.getProductTypeById(
                productType.getProductTypeId()).getHandlers();
        if (handlers != null) {
            for (Iterator<TypeHandler> iter = handlers.iterator(); iter
                    .hasNext();)
                iter.next().postGetMetadataHandle(metadata);
        }
        return metadata;
    }
    
    private Metadata getCatalogValues(Metadata metadata, ProductType productType) 
            throws RepositoryManagerException {
        List<TypeHandler> handlers = this.repositoryManager.getProductTypeById(
                productType.getProductTypeId()).getHandlers();
        if (handlers != null) {
            for (Iterator<TypeHandler> iter = handlers.iterator(); iter
                    .hasNext();)
                iter.next().preAddMetadataHandle(metadata);
        }
        return metadata;
    }
    
    private Query getCatalogQuery(Query query, ProductType productType)
            throws RepositoryManagerException, QueryFormulationException {
        List<TypeHandler> handlers = this.repositoryManager.getProductTypeById(
                productType.getProductTypeId()).getHandlers();
        if (handlers != null) {
            for (Iterator<TypeHandler> iter = handlers.iterator(); iter
                    .hasNext();)
                iter.next().preQueryHandle(query);
        }
        return query;
    }

    @SuppressWarnings("unchecked")
    private List<QueryResult> applyFilterToResults(
            List<QueryResult> queryResults, QueryFilter queryFilter)
            throws Exception {
        List<TimeEvent> events = new LinkedList<TimeEvent>();
        for (QueryResult queryResult : queryResults) {
            Metadata m = new Metadata();
            m.addMetadata(queryFilter.getPriorityMetKey(), queryResult
                .getMetadata().getMetadata(queryFilter.getPriorityMetKey()));
            events.add(new ObjectTimeEvent<QueryResult>(
                DateUtils.getTimeInMillis(DateUtils.toCalendar(queryResult
                    .getMetadata().getMetadata(queryFilter.getStartDateTimeMetKey()),
                    DateUtils.FormatType.UTC_FORMAT), DateUtils.julianEpoch), 
                DateUtils.getTimeInMillis(DateUtils.toCalendar(queryResult.getMetadata()
                    .getMetadata(queryFilter.getEndDateTimeMetKey()),
                    DateUtils.FormatType.UTC_FORMAT),
                DateUtils.julianEpoch), queryFilter.getConverter()
                    .convertToPriority(this.getCatalogValues(m,
                    queryResult.getProduct().getProductType())
                    .getMetadata(queryFilter.getPriorityMetKey())),
                queryResult));
        }
        events = queryFilter.getFilterAlgor().filterEvents(events);
        List<QueryResult> filteredQueryResults = new LinkedList<QueryResult>();
        for (TimeEvent event : events)
            filteredQueryResults.add(((ObjectTimeEvent<QueryResult>) event)
                    .getTimeObject());

        return filteredQueryResults;
    }

    private List<QueryResult> sortQueryResultList(List<QueryResult> queryResults,
      String sortByMetKey) {
    QueryResult[] resultsArray = queryResults
        .toArray(new QueryResult[queryResults.size()]);
    QueryResultComparator qrComparator = new QueryResultComparator();
    qrComparator.setSortByMetKey(sortByMetKey);
    Arrays.sort(resultsArray, qrComparator);
    return Arrays.asList(resultsArray);
  }

    private Metadata buildProductMetadata(Product product, Metadata metadata)
      throws CatalogException {
    Metadata pMet = new Metadata();
    pMet.replaceMetadata(ProductMetKeys.PRODUCT_ID, product.getProductId() != null ? 
        product.getProductId():"unknown");
    pMet.replaceMetadata(ProductMetKeys.PRODUCT_NAME, product.getProductName() != null ? 
        product.getProductName():"unknown");
    pMet.replaceMetadata(ProductMetKeys.PRODUCT_STRUCTURE, product
        .getProductStructure() != null ? product.getProductStructure():"unknown");
    pMet.replaceMetadata(ProductMetKeys.PRODUCT_TRANSFER_STATUS, product
        .getTransferStatus() != null ? product.getTransferStatus():"unknown");
    pMet.replaceMetadata(ProductMetKeys.PRODUCT_ROOT_REFERENCE, product.getRootRef() != null ? 
        VersioningUtils
        .getAbsolutePathFromUri(product.getRootRef().getDataStoreReference()):"unknown");

    List<Reference> refs = product.getProductReferences();

    if (refs == null || (refs != null && refs.size() == 0)) {
      refs = this.catalog.getProductReferences(product);
    }

    for (Reference r : refs) {
      pMet.replaceMetadata(ProductMetKeys.PRODUCT_ORIG_REFS, r.getOrigReference() != null
          ? VersioningUtils
          .getAbsolutePathFromUri(r.getOrigReference()):"unknown");
      pMet.replaceMetadata(ProductMetKeys.PRODUCT_DATASTORE_REFS,
          r.getDataStoreReference() != null ? 
              VersioningUtils.getAbsolutePathFromUri(r.getDataStoreReference()):"unknown");
      pMet.replaceMetadata(ProductMetKeys.PRODUCT_FILE_SIZES, String.valueOf(r
          .getFileSize()));
      pMet.replaceMetadata(ProductMetKeys.PRODUCT_MIME_TYPES,
          r.getMimeType() != null ? r.getMimeType().getName() : "unknown");
    }

    return pMet;
  }
    
    private void loadConfiguration() throws FileNotFoundException, IOException {
    // set up the configuration, if there is any
    if (System.getProperty("org.apache.oodt.cas.filemgr.properties") != null) {
      String configFile = System
          .getProperty("org.apache.oodt.cas.filemgr.properties");
      LOG.log(Level.INFO,
          "Loading File Manager Configuration Properties from: [" + configFile
              + "]");
      System.getProperties().load(new FileInputStream(new File(configFile)));
    }

    String metaFactory = null, dataFactory = null, transferFactory = null;

    metaFactory = System.getProperty("filemgr.catalog.factory",
        "org.apache.oodt.cas.filemgr.catalog.DataSourceCatalogFactory");
    dataFactory = System
        .getProperty("filemgr.repository.factory",
            "org.apache.oodt.cas.filemgr.repository.DataSourceRepositoryManagerFactory");
    transferFactory = System.getProperty("filemgr.datatransfer.factory",
        "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory");

    catalog = GenericFileManagerObjectFactory
        .getCatalogServiceFromFactory(metaFactory);
    repositoryManager = GenericFileManagerObjectFactory
        .getRepositoryManagerServiceFromFactory(dataFactory);
    dataTransfer = GenericFileManagerObjectFactory
        .getDataTransferServiceFromFactory(transferFactory);

    transferStatusTracker = new TransferStatusTracker(catalog);

    // got to start the server before setting up the transfer client since
    // it
    // checks for a live server
    dataTransfer
        .setFileManagerUrl(new URL("http://localhost:" + webServerPort));

    expandProductMet = Boolean
        .getBoolean("org.apache.oodt.cas.filemgr.metadata.expandProduct");
  }

}
