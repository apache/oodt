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

//OODT imports
import org.apache.oodt.cas.filemgr.catalog.Catalog;
import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.datatransfer.TransferStatusTracker;
import org.apache.oodt.cas.filemgr.metadata.ProductMetKeys;
import org.apache.oodt.cas.filemgr.metadata.extractors.FilemgrMetExtractor;
import org.apache.oodt.cas.filemgr.repository.RepositoryManager;
import org.apache.oodt.cas.filemgr.structs.*;
import org.apache.oodt.cas.filemgr.structs.exceptions.*;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryFilter;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.structs.query.QueryResultComparator;
import org.apache.oodt.cas.filemgr.structs.query.filter.ObjectTimeEvent;
import org.apache.oodt.cas.filemgr.structs.query.filter.TimeEvent;
import org.apache.oodt.cas.filemgr.structs.type.TypeHandler;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.versioning.Versioner;
import org.apache.oodt.cas.filemgr.versioning.VersioningUtils;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.commons.date.DateUtils;
import org.apache.oodt.config.Component;
import org.apache.oodt.config.ConfigurationManager;
import org.apache.oodt.config.ConfigurationManagerFactory;

//JDK imports
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import com.google.common.collect.Lists;
import org.slf4j.LoggerFactory;

/**
 * @author radu
 *
 * <p>Manages the {@link Catalog}, {@link RepositoryManager} and {@link DataTransfer}.
 * Without the rpc logic.</p>
 */
public class FileManager {

    /** Our log stream. Should be replaced by SLF4J logger */
    @Deprecated
    private static final Logger LOG = Logger.getLogger(FileManager.class.getName());
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FileManager.class);

    private Catalog catalog = null;

    /* our RepositoryManager */
    private RepositoryManager repositoryManager = null;

    /* our DataTransfer */
    private DataTransfer dataTransfer = null;

    /* our data transfer status tracker */
    private TransferStatusTracker transferStatusTracker = null;

    /* whether or not to expand a product instance into met */
    private boolean expandProductMet;
    
    /** Configuration Manager instance which will handle the configuration aspect in distributed/standalone manner */
    private ConfigurationManager configurationManager;

    public FileManager() throws Exception {
        List<String> propertiesFiles = new ArrayList<>();
  
        // set up the configuration, if there is any
        if (System.getProperty("org.apache.oodt.cas.filemgr.properties") != null) {
          propertiesFiles.add(System.getProperty("org.apache.oodt.cas.filemgr.properties"));
        }
      
        configurationManager = ConfigurationManagerFactory.getConfigurationManager(Component.FILE_MANAGER, propertiesFiles);
        LOG.log(Level.INFO, "File Manager started by "
                + System.getProperty("user.name", "unknown"));
    }

    public void setCatalog(Catalog catalog) {
        LOG.fine("Setting catalog: " + catalog.toString());
        this.catalog = catalog;
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

    public boolean transferringProduct(Product product) {
        transferStatusTracker.transferringProduct(product);
        return true;
    }

    public FileTransferStatus getCurrentFileTransfer() {
        return transferStatusTracker
                .getCurrentFileTransfer();
    }

    public List<FileTransferStatus> getCurrentFileTransfers() {
        return transferStatusTracker.getCurrentFileTransfers();
    }

    public double getProductPctTransferred(Product product) {

        double pct = transferStatusTracker.getPctTransferred(product);
        return pct;
    }

    public double getRefPctTransferred(Reference reference) {
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

    public boolean removeProductTransferStatus(Product product) {
        transferStatusTracker.removeProductTransferStatus(product);
        return true;
    }

    public boolean isTransferComplete(Product product) {
        return transferStatusTracker.isTransferComplete(product);
    }

    public ProductPage pagedQuery(
            Query query,
            ProductType type,
            int pageNum) throws CatalogException {

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

        return prodPage;
    }

    public ProductPage getFirstPage(ProductType type) {
        logger.debug("Getting first page for product type : {}", type.toString());
        ProductPage page = catalog.getFirstPage(type);
        try {
            setProductType(page.getPageProducts());
        } catch (Exception e) {
            logger.error("Unable to set product type for product page: {}", page, e);
        }
        return page;
    }

    public ProductPage getLastPage(ProductType type) {
        LOG.fine("Getting last page for : " + type.toString());
        logger.debug("Getting last page for : {}", type);
        ProductPage page = catalog.getLastProductPage(type);
        try {
            setProductType(page.getPageProducts());
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                    "Unable to set product types for product page list: ["
                            + page + "]");
        }
        return page;
    }

    public ProductPage getNextPage(ProductType type , ProductPage currPage) {
        LOG.fine("Getting next page for : " + type.toString());
        ProductPage page = catalog.getNextPage(type, currPage);
        try {
            setProductType(page.getPageProducts());
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                    "Unable to set product types for product page list: ["
                            + page + "]");
        }
        return page;
    }

    public ProductPage getPrevPage(ProductType type, ProductPage currPage) {
        LOG.fine("Getting previous page for : " + type.toString());
        ProductPage page = catalog.getPrevPage(type, currPage);
        try {
            setProductType(page.getPageProducts());
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                    "Unable to set product types for product page list: ["
                            + page + "]");
        }
        return page;
    }

    public String addProductType(ProductType productType) throws RepositoryManagerException {
        LOG.fine("Adding product type : " + productType.toString());
        repositoryManager.addProductType(productType);
        return productType.getProductTypeId();
    }

    public synchronized boolean setProductTransferStatus(
            Product product)
            throws CatalogException {
        catalog.setProductTransferStatus(product);
        return true;
    }

    public int getNumProducts(ProductType type)
            throws CatalogException {
        int numProducts = -1;

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

    public List<Product> getTopNProducts(int n)
            throws CatalogException {
        List<Product> topNProducts = null;

        try {
            topNProducts = catalog.getTopNProducts(n);
            return topNProducts;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception when getting topN products: Message: "
                            + e.getMessage());
            throw new CatalogException(e.getMessage());
        }
    }

    public List<Product> getTopNProductsByProductType(int n,
                                         ProductType type)
            throws CatalogException {
        List<Product> topNProducts = null;
        try {
            topNProducts = catalog.getTopNProducts(n, type);
            return topNProducts;
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

    public List<ProductType>  getProductTypes()
            throws RepositoryManagerException {
        try {
            return repositoryManager.getProductTypes();
        } catch (RepositoryManagerException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Unable to obtain product types from repository manager: Message: "
                            + e.getMessage());
            throw new RepositoryManagerException(e.getMessage());
        }
    }

    public List<Reference> getProductReferences(
            Product product)
            throws CatalogException {
        try {
            return catalog.getProductReferences(product);
        } catch (CatalogException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Unable to obtain references for product: ["
                    + product.getProductName() + "]: Message: "
                    + e.getMessage());
            throw new CatalogException(e.getMessage());
        }

    }

    public Product getProductById(String productId)
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
            return product;
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

    public Product getProductByName(String productName)
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
            return product;
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

    public List<Product> getProductsByProductType(
            ProductType type)
            throws CatalogException {
        try {

            return catalog.getProductsByProductType(type);

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Exception obtaining products by product type for type: ["
                            + type.getName() + "]: Message: " + e.getMessage());
            throw new CatalogException(e.getMessage());
        }
    }

    public List<Element> getElementsByProductType(
            ProductType type)
            throws ValidationLayerException {
        try {
            return catalog.getValidationLayer().getElements(type);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Exception obtaining elements for product type: ["
                            + type.getName() + "]: Message: " + e.getMessage());
            throw new ValidationLayerException(e.getMessage());
        }
    }

    public Element getElementById(String elementId)
            throws ValidationLayerException {
        try {
            return catalog.getValidationLayer().getElementById(elementId);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "exception retrieving element by id: ["
                    + elementId + "]: Message: " + e.getMessage());
            throw new ValidationLayerException(e.getMessage());
        }
    }

    public Element getElementByName(String elementName)
            throws ValidationLayerException {
        try {
            return catalog.getValidationLayer()
                    .getElementByName(elementName);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "exception retrieving element by name: ["
                    + elementName + "]: Message: " + e.getMessage());
            throw new ValidationLayerException(e.getMessage());
        }
    }

    public List<QueryResult> complexQuery(
            ComplexQuery complexQuery) throws CatalogException {
        try {

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

            return queryResults;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CatalogException("Failed to perform complex query : "
                    + e.getMessage());
        }
    }

    public ProductType getProductTypeByName(String productTypeName)
            throws RepositoryManagerException {

        ProductType pt = repositoryManager.getProductTypeByName(productTypeName);
        return pt;
    }
    public ProductType getProductTypeById(String productTypeId)
            throws RepositoryManagerException {
        try {
            return repositoryManager.getProductTypeById(productTypeId);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                    "Exception obtaining product type by id for product type: ["
                            + productTypeId + "]: Message: " + e.getMessage());
            throw new RepositoryManagerException(e.getMessage());
        }
    }

    public synchronized boolean updateMetadata(Product product,
                                               Metadata met) throws CatalogException{
        Metadata oldMetadata = catalog.getMetadata(product);
        catalog.removeMetadata(oldMetadata, product);
        catalog.addMetadata(met, product);
        return true;
    }

    public String ingestProduct(Product p,
                                Metadata m, boolean clientTransfer)
            throws VersioningException, RepositoryManagerException,
            DataTransferException, CatalogException {
        try {
            // first, create the product
            p.setTransferStatus(Product.STATUS_TRANSFER);
            catalogProduct(p);

            // now add the metadata
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

    public boolean moveProduct(Product p, String newPath)
            throws DataTransferException {

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

    public boolean modifyProduct(Product p) throws CatalogException {
        try {
            catalog.modifyProduct(p);
        } catch (CatalogException e) {
            LOG.log(Level.WARNING, "Exception modifying product: ["
                    + p.getProductId() + "]: Message: " + e.getMessage(), e);
            throw e;
        }

        return true;
    }

    public boolean removeProduct(Product p) throws CatalogException {
        try {
            catalog.removeProduct(p);
        } catch (CatalogException e) {
            LOG.log(Level.WARNING, "Exception modifying product: ["
                    + p.getProductId() + "]: Message: " + e.getMessage(), e);
            throw e;
        }

        return true;
    }

    public synchronized String catalogProduct(Product p)
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

    public synchronized Metadata addMetadata(Product p, Metadata m)
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
        met.addMetadata(metadata.getHashTable());

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

    public synchronized boolean addProductReferences(Product product)
            throws CatalogException {
        catalog.addProductReferences(product);
        return true;
    }

    private void setProductType(List<Product> products) throws Exception {
        logger.debug("Setting product types for products: {}", products);
        if (products != null && products.size() > 0) {
            for (Iterator<Product> i = products.iterator(); i.hasNext();) {
                Product p = i.next();
                try {
                    p.setProductType(repositoryManager.getProductTypeById(p
                            .getProductType().getProductTypeId()));
                } catch (RepositoryManagerException e) {
                    logger.error("Unable to set product type for product: {}", p, e);
                    throw new Exception(e.getMessage());
                }
            }
        }
    }

    public List<Product> query(Query query, ProductType productType) throws CatalogException {
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

    public Metadata getReducedMetadata(Product product, List<String> elements) throws CatalogException {
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

    public Metadata getMetadata(Product product) throws CatalogException {
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

    public Metadata getOrigValues(Metadata metadata, ProductType productType)
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

    public Metadata getCatalogValues(Metadata metadata, ProductType productType)
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

    public Query getCatalogQuery(Query query, ProductType productType)
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

    public void loadConfiguration() throws FileNotFoundException, IOException {
        
        try{
          this.configurationManager.loadConfiguration();
        }
        catch(Exception e){
          e.printStackTrace();
          throw new IOException(e.getLocalizedMessage());
        }
        
        String metaFactory = null, dataFactory = null;

        metaFactory = System.getProperty("filemgr.catalog.factory",
                "org.apache.oodt.cas.filemgr.catalog.DataSourceCatalogFactory");
        dataFactory = System
                .getProperty("filemgr.repository.factory",
                        "org.apache.oodt.cas.filemgr.repository.DataSourceRepositoryManagerFactory");

        catalog = GenericFileManagerObjectFactory
                .getCatalogServiceFromFactory(metaFactory);
        repositoryManager = GenericFileManagerObjectFactory
                .getRepositoryManagerServiceFromFactory(dataFactory);


        transferStatusTracker = new TransferStatusTracker(catalog);

        // got to start the server before setting up the transfer client since
        // it
        // checks for a live server

        expandProductMet = Boolean
                .getBoolean("org.apache.oodt.cas.filemgr.metadata.expandProduct");
    }

    public void setDataTransfer(DataTransfer dataTransfer){
        this.dataTransfer = dataTransfer;
    }

}
