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

import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.IngestException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.filemgr.versioning.VersioningUtils;
import org.apache.oodt.cas.metadata.MetExtractor;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//JDK imports

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * An implementation of the {@link Ingester} interface that uses the following
 * pieces of {@link Metadata} information to determine how to ingest a
 * {@link Product}:
 *
 * <ul>
 * <li>Filename - The name of the Product file to ingest.</li>
 * <li>ProductType - The type of the Product to ingest.</li>
 * <li>FileLocation - A full path pointer to directory containing the Product
 * file to ingest.</li>
 * </ul>
 *
 * The {@link Ingester} supports overriding certain {@link Product} properties,
 * including:
 *
 * <ul>
 * <li>Specification of <code>ProductStructure</code> parameter that will
 * tell the {@link Ingester} whether or not the {@link Product} is a directory
 * or a regular file.</li>
 * <li>Specification of <code>ProductName</code> parameter that will tell the
 * {@link Ingester} a name for the {@link Product} to use (default is using the
 * specified {@link Metadata} field, <code>Filename</code>.</li>
 * </ul>
 * </p>.
 */
public class StdIngester implements Ingester, CoreMetKeys {

    /* our log stream */
    private final static Logger LOG = Logger.getLogger(StdIngester.class
            .getName());

    /* our file manager client */
    private FileManagerClient fmClient = null;

    /* client transfer service factory */
    private String clientTransferServiceFactory = null;

    public StdIngester(String transferService) {
        this.clientTransferServiceFactory = transferService;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.oodt.cas.filemgr.ingest.Ingester#ingest(java.net.URL,
     *      java.io.File, org.apache.oodt.cas.metadata.MetExtractor,
     *      java.io.File)
     */
    public String ingest(URL fmUrl, File prodFile, MetExtractor extractor,
            File metConfFile) throws IngestException {
        Metadata met;
        try {
            met = extractor.extractMetadata(prodFile, metConfFile);
        } catch (MetExtractionException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new IngestException("Met extraction exception on product: ["
                    + prodFile + "]: Message: " + e.getMessage(), e);
        }

        return ingest(fmUrl, prodFile, met);
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.oodt.cas.filemgr.ingest.Ingester#ingest(java.net.URL,
	 * java.util.List, org.apache.oodt.cas.metadata.MetExtractor, java.io.File)
	 */
	public void ingest(URL fmUrl, List<String> prodFiles,
			MetExtractor extractor, File metConfFile) {
		if (prodFiles != null && prodFiles.size() > 0) {
            for (String prodFilePath : prodFiles) {
                String productID;

                try {
                    productID = ingest(fmUrl, new File(prodFilePath),
                        extractor, metConfFile);
                    LOG.log(Level.INFO, "Product: [" + prodFilePath
                                        + "] ingested successfully! ID: [" + productID
                                        + "]");
                } catch (IngestException e) {
                    LOG.log(Level.WARNING,
                        "IngestException handling product: ["
                        + prodFilePath
                        + "]: Exception: ["
                        + e.getMessage()
                        + "]: Continuing ingest of remainder of products.");
                }

            }
		}

	}

    /*
     * (non-Javadoc)
     *
     * @see org.apache.oodt.cas.filemgr.ingest.Ingester#ingest(java.net.URL,
     *      java.io.File, org.apache.oodt.cas.metadata.Metadata)
     */
    public String ingest(URL fmUrl, File prodFile, Metadata met)
            throws IngestException {
        checkOrSetFileManager(fmUrl);
        String productType = met.getMetadata(PRODUCT_TYPE);
        String fileLocation = met.getMetadata(FILE_LOCATION);
        String fileName = met.getMetadata(FILENAME);

        if (!check(productType, PRODUCT_TYPE)
                || !check(fileLocation, FILE_LOCATION)
                || !check(fileName, FILENAME)) {
            throw new IngestException("Must specify: " + PRODUCT_TYPE + " and "
                    + FILENAME + " and " + FILE_LOCATION
                    + " within metadata file. Cannot ingest product: ["
                    + prodFile.getAbsolutePath() + "]");
        }

        // allow user to override default product name (Filename)
        String productName = met.getMetadata(PRODUCT_NAME) != null ? met
                .getMetadata(PRODUCT_NAME) : fileName;

        // check to see if product structure was specified
        String productStructure = met.getMetadata(PRODUCT_STRUCTURE);
        if (productStructure == null) {
            // try and guess the structure
            if (prodFile.isDirectory()) {
                productStructure = Product.STRUCTURE_HIERARCHICAL;
            } else {
                productStructure = Product.STRUCTURE_FLAT;
            }
        }

        // create the product
        Product product = new Product();
        product.setProductName(productName);
        product.setProductStructure(productStructure);
        product.setProductType(getProductType(productType));

        List<String> references = new Vector<String>();
        if (!fileLocation.endsWith(File.separator)) {
            fileLocation += File.separator;
        }

        String fullFilePath = fileLocation + fileName;

        references.add(new File(fullFilePath).toURI().toString());

        if (productStructure.equals(Product.STRUCTURE_HIERARCHICAL)) {
            references.addAll(VersioningUtils.getURIsFromDir(new File(
                    fullFilePath)));
        }

        // build refs and attach to product
        VersioningUtils.addRefsFromUris(product, references);

        LOG.log(Level.INFO, "StdIngester: ingesting product: " + PRODUCT_NAME
                + ": [" + productName + "]: " + PRODUCT_TYPE + ": ["
                + productType + "]: " + FILE_LOCATION + ": [" + fileLocation
                + "]");

        String productID;

        try {
            productID = fmClient.ingestProduct(product, met, true);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "exception ingesting product: ["
                    + productName + "]: Message: " + e.getMessage());
            throw new IngestException("exception ingesting product: ["
                    + productName + "]: Message: " + e.getMessage());
        }

        return productID;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.oodt.cas.filemgr.ingest.Ingester#hasProduct(java.net.URL,
     *      java.io.File)
     */
    public boolean hasProduct(URL fmUrl, File prodFile) throws CatalogException{
        return hasProduct(fmUrl, prodFile.getName());
    }

    private boolean check(String property, String propName) {
        if (property == null) {
            LOG.log(Level.WARNING, "Property: [" + propName
                    + "] is not provided");
            return false;
        } else {
            return true;
        }
    }

    private void checkOrSetFileManager(URL url) {
        if (this.fmClient != null && this.fmClient.getFileManagerUrl() != null) {

            try {
                if (!this.fmClient.getFileManagerUrl().toURI().equals(url.toURI())) {
                    setFileManager(url);
                }
            } catch (URISyntaxException e) {
                LOG.log(Level.SEVERE, "Could not convert URL to URI, Message :" +e.getMessage());
            }
        } else {
            setFileManager(url);
        }
    }

    private void setFileManager(URL url) {
        try {
            fmClient = RpcCommunicationFactory.createClient(url);

            LOG.log(Level.INFO, "StdIngester: connected to file manager: ["
                    + url + "]");
            // instantiate the client transfer object
            // the crawler will have the client perform the transfer
            DataTransfer dataTransfer = GenericFileManagerObjectFactory
                    .getDataTransferServiceFromFactory(this.clientTransferServiceFactory);
            dataTransfer.setFileManagerUrl(url);
            fmClient.setDataTransfer(dataTransfer);
        } catch (ConnectionException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Unable to connect to file manager: [" + url
                    + "]: message: " + e.getMessage());
        }

    }

    private ProductType getProductType(String productTypeName) {
        ProductType type = null;

        try {
            type = fmClient.getProductTypeByName(productTypeName);
        } catch (RepositoryManagerException e) {
            LOG.log(Level.WARNING, "Unable to obtain product type: ["
                    + productTypeName + "] from File Manager at: ["
                    + fmClient.getFileManagerUrl() + "]: Message: "
                    + e.getMessage());
        }

        return type;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.oodt.cas.filemgr.ingest.Ingester#hasProduct(java.net.URL,
     *      java.lang.String)
     */
    public boolean hasProduct(URL fmUrl, String productName) throws CatalogException{
        checkOrSetFileManager(fmUrl);
        try {
            return fmClient.hasProduct(productName);
        } catch (CatalogException e) {
            LOG.log(Level.WARNING,
                    "Unable to check for existance of product: [" + productName
                            + "]: Message: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        if (fmClient != null) {
            fmClient.close();
        }
    }
}
