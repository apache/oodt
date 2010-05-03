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


package gov.nasa.jpl.oodt.cas.crawl;

//OODT imports
import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction;
import gov.nasa.jpl.oodt.cas.crawl.action.CrawlerActionRepo;
import gov.nasa.jpl.oodt.cas.crawl.config.ProductCrawlerBean;
import gov.nasa.jpl.oodt.cas.filemgr.ingest.Ingester;
import gov.nasa.jpl.oodt.cas.filemgr.ingest.StdIngester;
import gov.nasa.jpl.oodt.cas.filemgr.metadata.CoreMetKeys;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

//JDK imports
import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * An abstract base class for Product Crawling. This class provides methods to
 * communicate with the file manager and parse met files that show how to ingest
 * a particular Product into the File Manager.
 * </p>
 * 
 */
public abstract class ProductCrawler extends ProductCrawlerBean {

    /* our log stream */
    protected static Logger LOG = Logger.getLogger(ProductCrawler.class
            .getName());

    // filter to only find directories when doing a listFiles
    protected static FileFilter DIR_FILTER = new FileFilter() {
        public boolean accept(File file) {
            return file.isDirectory();
        }
    };

    // filter to only find product files, not met files
    protected static FileFilter FILE_FILTER = new FileFilter() {
        public boolean accept(File file) {
            return file.isFile();
        }
    };

    private CrawlerActionRepo actionRepo;

    private Ingester ingester;

    public void crawl() {
        this.crawl(new File(this.getProductPath()));
    }

    public void crawl(File dirRoot) {
        // Load actions
        if (this.getApplicationContext() != null)
            (this.actionRepo = new CrawlerActionRepo())
                    .loadActionsFromBeanFactory(this.getApplicationContext(), this
                            .getActionIds());

        // create ingester
        this.ingester = new StdIngester(this.getClientTransferer());

        if (dirRoot == null || ((dirRoot != null && !dirRoot.exists())))
            throw new IllegalArgumentException("dir root is null or non existant!");

        // start crawling
        Stack<File> stack = new Stack<File>();
        stack.push(dirRoot.isDirectory() ? dirRoot : dirRoot.getParentFile());
        while (!stack.isEmpty()) {
            File dir = (File) stack.pop();
            LOG.log(Level.INFO, "Crawling " + dir);

            File[] productFiles = null;
            if (this.isCrawlForDirs()) {
                productFiles = dir.listFiles(DIR_FILTER);
            } else {
                productFiles = dir.listFiles(FILE_FILTER);
            }

            for (int j = 0; j < productFiles.length; j++) {
                try {
                    this.handleFile(productFiles[j]);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Failed to process file : "
                            + e.getMessage());
                }
            }

            if (!this.isNoRecur()) {
                File[] subdirs = dir.listFiles(DIR_FILTER);
                if (subdirs != null)
                    for (int j = 0; j < subdirs.length; j++)
                        stack.push(subdirs[j]);
            }
        }
    }

    private synchronized boolean containsRequiredMetadata(
            Metadata productMetadata) {
        for (int i = 0; i < this.getRequiredMetadata().size(); i++) {
            if (productMetadata.getMetadata((String) this.getRequiredMetadata()
                    .get(i)) == null) {
                LOG.log(Level.WARNING, "Missing required metadata field "
                        + this.getRequiredMetadata().get(i));
                return false;
            }
        }
        return true;
    }
    
    private void addKnownMetadata(File product, Metadata productMetadata) {
        if (productMetadata.getMetadata(CoreMetKeys.PRODUCT_NAME) == null)
			productMetadata.addMetadata(CoreMetKeys.PRODUCT_NAME, product
					.getName());  
        if (productMetadata.getMetadata(CoreMetKeys.FILENAME) == null)
			productMetadata.addMetadata(CoreMetKeys.FILENAME, product
					.getName());
        if (productMetadata.getMetadata(CoreMetKeys.FILE_LOCATION) == null)
			productMetadata.addMetadata(CoreMetKeys.FILE_LOCATION, product
					.getAbsoluteFile().getParentFile().getAbsolutePath());
    }

    private void handleFile(File product) {
        LOG.log(Level.INFO, "Handling file " + product);
        if (this.passesPreconditions(product)) {
            Metadata productMetadata = new Metadata();
            productMetadata.addMetadata(this.getGlobalMetadata().getHashtable());
            productMetadata.addMetadata(this.getMetadataForProduct(product).getHashtable(), true);
            this.addKnownMetadata(product, productMetadata);
            
            boolean isRequiredMetadataPresent = this.containsRequiredMetadata(productMetadata);
            boolean isPreIngestActionsComplete = this.performPreIngestActions(product, productMetadata);
            
            if (this.isSkipIngest()) {
                LOG.log(Level.INFO, "Skipping ingest of product: ["
                    + product.getAbsolutePath() + "]");
            } else {
                if (isRequiredMetadataPresent
                        && isPreIngestActionsComplete
                        && this.ingest(product, productMetadata)) {
                    LOG.log(Level.INFO, "Successful ingest of product: ["
                            + product.getAbsolutePath() + "]");
                    this
                            .performPostIngestOnSuccessActions(product,
                                    productMetadata);
                } else {
                    LOG.log(Level.WARNING, "Failed to ingest product: ["
                            + product.getAbsolutePath()
                            + "]: performing postIngestFail actions");
                    this.performPostIngestOnFailActions(product, productMetadata);
                }
            }
        } else {
            LOG.log(Level.WARNING,
                    "Failed to pass preconditions for ingest of product: ["
                            + product.getAbsolutePath() + "]");
        }
    }

    private boolean ingest(File product, Metadata productMetdata) {
        try {
            LOG.log(Level.INFO, "ProductCrawler: Ready to ingest product: ["
                    + product + "]: ProductType: ["
                    + productMetdata.getMetadata(PRODUCT_TYPE) + "]");
            String productId = ingester.ingest(new URL(this.getFilemgrUrl()),
                    product, productMetdata);
            LOG.log(Level.INFO, "Successfully ingested product: [" + product
                    + "]: product id: " + productId);
        } catch (Exception e) {
            LOG.log(Level.WARNING,
                    "ProductCrawler: Exception ingesting product: [" + product
                            + "]: Message: " + e.getMessage()
                            + ": attempting to continue crawling", e);
            return false;
        }
        return true;
    }

    protected abstract boolean passesPreconditions(File product);

    protected abstract Metadata getMetadataForProduct(File product);

    private boolean performPreIngestActions(File product,
            Metadata productMetadata) {
        if (this.actionRepo != null)
            return this.performProductCrawlerActions(this.actionRepo
                    .getPreIngestActions(), product, productMetadata);
        else
            return true;
    }

    private boolean performPostIngestOnSuccessActions(File product,
            Metadata productMetadata) {
        if (this.actionRepo != null)
            return this.performProductCrawlerActions(this.actionRepo
                    .getPostIngestOnSuccessActions(), product, productMetadata);
        else
            return true;
    }

    private boolean performPostIngestOnFailActions(File product,
            Metadata productMetadata) {
        if (this.actionRepo != null)
            return this.performProductCrawlerActions(this.actionRepo
                    .getPostIngestOnFailActions(), product, productMetadata);
        else
            return true;
    }

    private boolean performProductCrawlerActions(List<CrawlerAction> actions,
            File product, Metadata productMetadata) {
        boolean allSucceeded = true;
        for (CrawlerAction action : actions) {
            try {
                LOG.log(Level.INFO, "Performing action (id = "
                        + action.getId() + " : description = " 
                        + action.getDescription() + ")");
                if (!action.performAction(product,
                        productMetadata))
                    throw new Exception("Action (id = "
                            + action.getId() + " : description = " 
                            + action.getDescription() 
                            + ") returned false");
            } catch (Exception e) {
                allSucceeded = false;
                LOG.log(Level.WARNING, "Failed to perform crawler action : "
                        + e.getMessage());
            }
        }
        return allSucceeded;
    }

}
