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

package org.apache.oodt.cas.filemgr.ingest;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.CacheException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;

//JDK imports
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author bfoster
 * @author mattmann
 * 
 * <p>
 * A cached way to determine product ingestion against a File Manager.
 * </p>
 * 
 */
public class LocalCache implements Cache {

    private HashSet<String> uniqueElements;

    private String uniqueElementName;

    private List<String> uniqueElementProductTypeNames;

    private String rangeQueryElementName;

    private String startOfQuery;

    private String endOfQuery;

    private FileManagerClient fm;

    private static final Logger LOG = Logger.getLogger(LocalCache.class
            .getName());

    /**
     * 
     * @param fmUrl
     * @param rangeQueryElementName
     * @param startOfQuery
     * @param endOfQuery
     */
    public LocalCache(URL fmUrl, String rangeQueryElementName,
            String startOfQuery, String endOfQuery) {
        this(fmUrl, null, null, rangeQueryElementName, startOfQuery, endOfQuery);

    }

    /**
     * 
     * @param fmUrl
     * @param uniqueElementName
     * @param rangeQueryElementName
     * @param startOfQuery
     * @param endOfQuery
     */
    public LocalCache(URL fmUrl, String uniqueElementName,
            List<String> uniqueElementProductTypes, String rangeQueryElementName,
            String startOfQuery, String endOfQuery) {
        this.uniqueElements = new HashSet<String>();
        this.uniqueElementName = uniqueElementName;
        this.uniqueElementProductTypeNames = uniqueElementProductTypes;
        this.rangeQueryElementName = rangeQueryElementName;
        this.startOfQuery = startOfQuery;
        this.endOfQuery = endOfQuery;
        setFileManager(fmUrl);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#sync(java.util.List)
     */
    public void sync(List<String> uniqueElementProductTypeNames) throws CacheException {
        sync(DEFAULT_UNIQUE_MET_KEY, uniqueElementProductTypeNames);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#sync(java.lang.String,
     *      java.util.List)
     */
    public void sync(String uniqueElementName,
            List<String> uniqueElementProductTypeNames) throws CacheException {
        try {
            this.uniqueElementName = uniqueElementName;            
            this.uniqueElementProductTypeNames = uniqueElementProductTypeNames;
            List<Product> products = new Vector<Product>();
            for (String productType : this.uniqueElementProductTypeNames) {
                products.addAll(getProductsOverDateRange(
                    this.rangeQueryElementName, productType, this.startOfQuery,
                    this.endOfQuery));
            }
            clear();
            for (Product product : products) {
                String value = getValueForMetadata(product, uniqueElementName);
                this.uniqueElements.add(value);
            }
        } catch (Exception e) {
            throw new CacheException("Failed to sync with database : "
                    + e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#clear()
     */
    public void clear() {
        this.uniqueElements.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#size()
     */
    public int size() {
        return this.uniqueElements.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#contains(java.lang.String)
     */
    public boolean contains(String productName) {
        return this.uniqueElements.contains(productName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#setFileManager(java.net.URL)
     */
    public void setFileManager(URL fmUrl) {
        try {
            this.fm = RpcCommunicationFactory.createClient(fmUrl);
        } catch (ConnectionException e) {
            LOG.log(Level.WARNING,
                    "Exception setting file manager connection to: [" + fmUrl
                            + "]");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#sync()
     */
    public void sync() throws CacheException {
        if (this.uniqueElementName == null || (this.uniqueElementProductTypeNames == null || (
            this.uniqueElementProductTypeNames.size() == 0))) {
            throw new CacheException(
                    "Both uniqueElementName and uniqueElementProductTypeName must "
                            + "be defined in order to use this form of the sync operation!");
        }

        sync(this.uniqueElementName, this.uniqueElementProductTypeNames);

    }
    
    /* (non-Javadoc)
     * @see org.apache.oodt.cas.filemgr.ingest.Cache#getFileManagerUrl()
     */
    public URL getFileManagerUrl() {
        return this.fm.getFileManagerUrl();
    }

    /**
     * Performs a {@link FileManagerClient#hasProduct(String)} check
     * against a live File Manager, bypassing the {@link Cache}.
     * 
     * @param uniqueElementName
     *            The product identifier element to identify whether the product
     *            was received yet.
     * @param uniqueElementValue
     *            The value of the product identifier element.
     * @param productTypeName
     *            The {@link ProductType} of the Product that you would like to
     *            check existence for.
     * @return True if the given Product (identified by its
     *         <code>uniqueElement</code>) exists, False otherwise.
     * @throws CacheException
     *             If any error occurs.
     */
    public boolean liveHasProduct(String uniqueElementName,
            String uniqueElementValue, String productTypeName)
            throws CacheException {
        Query query = new Query();
        query.addCriterion(new TermQueryCriteria(uniqueElementName,
                uniqueElementValue));
        try {
            return !(fm.query(query, fm.getProductTypeByName(productTypeName))
                    .isEmpty());
        } catch (Exception e) {
            throw new CacheException(
                    "Unable to check for product reception from file manager: ["
                            + fm.getFileManagerUrl() + "]: Message: "
                            + e.getMessage(), e);
        }
    }

    /**
     * @return the uniqueElementName
     */
    public String getUniqueElementName() {
        return uniqueElementName;
    }

    /**
     * @param uniqueElementName
     *            the uniqueElementName to set
     */
    public void setUniqueElementName(String uniqueElementName) {
        this.uniqueElementName = uniqueElementName;
    }

    /**
     * @return the uniqueElementProductTypeNames
     */
    public List<String> getUniqueElementProductTypeNames() {
        return uniqueElementProductTypeNames;
    }

    /**
     * @param uniqueElementProductTypeNames
     *            the uniqueElementProductTypeNames to set
     */
    public void setUniqueElementProductTypeNames(
            List<String> uniqueElementProductTypeNames) {
        this.uniqueElementProductTypeNames = uniqueElementProductTypeNames;
    }

    private List<Product> getProductsOverDateRange(String elementName, String productType,
            String startOfQuery, String endOfQuery) throws CacheException {
        List<Product> products = new Vector<Product>();
        try {        
            Query query = new Query();
            query.addCriterion(new RangeQueryCriteria(elementName,
                    startOfQuery, endOfQuery));
            if(this.uniqueElementProductTypeNames != null && 
                    this.uniqueElementProductTypeNames.size() > 0){
                for (String productTypeName : this.uniqueElementProductTypeNames) {
                    products.addAll(getProducts(query, productTypeName));
                }
                
            }
        } catch (Exception e) {
            throw new CacheException("Failed to query for product via element "
                    + elementName + " and range " + startOfQuery + " to "
                    + endOfQuery + " : " + e.getMessage(), e);
        }
        
        return products;
    }

    private List<Product> getProducts(Query query, String productType)
            throws CacheException {
        try {
            return fm.query(query, fm.getProductTypeByName(productType));
        } catch (Exception e) {
            throw new CacheException("Failed to get product list for query "
                    + query + " : " + e.getMessage(), e);
        }
    }

    private String getValueForMetadata(Product product,
            String metadataElementName) throws CacheException {
        try {
            return fm.getMetadata(product).getMetadata(metadataElementName);
        } catch (Exception e) {
            throw new CacheException("Failed to get metadata value for "
                    + metadataElementName + " : " + e.getMessage(), e);
        }
    }

    public void finalize() throws IOException {
        if (fm != null) {
            fm.close();
        }
    }
}
