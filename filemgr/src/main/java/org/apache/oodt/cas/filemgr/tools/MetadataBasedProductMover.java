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

package org.apache.oodt.cas.filemgr.tools;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A tool to move products based on their metadata attributes.
 * </p>
 * 
 */
public class MetadataBasedProductMover {

    public static final double DOUBLE = 1000.0;
    /*
             * the metadata path sepc string, e.g.,
             * /path/to/final/loc/[LocalDay]/[Filename]
             */
    private String pathSpec = null;

    /* the client to the file manager */
    private FileManagerClient fmgrClient = null;

    /* our log stream */
    private static final Logger LOG = Logger
            .getLogger(MetadataBasedProductMover.class.getName());

    /**
     * Default constructor.
     * 
     * @param pathSpec
     *            A path specification based on the product metadata for the
     *            final data store location.
     * @param fmUrl
     *            A string URL to the file manager.
     * @throws InstantiationException
     *             If the passed in url is malformed.
     */
    public MetadataBasedProductMover(String pathSpec, String fmUrl)
            throws InstantiationException {
        this.pathSpec = pathSpec;
        try {
            this.fmgrClient = RpcCommunicationFactory.createClient(new URL(fmUrl));
        } catch (MalformedURLException e) {
            throw new InstantiationException(e.getMessage());
        } catch (ConnectionException e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    public void moveProducts(ProductType type) throws CatalogException, URISyntaxException, DataTransferException {
        // paginate through the product list

        ProductPage page = fmgrClient.getFirstPage(type);

        for (int i = 0; i < page.getTotalPages(); i++) {
            if (page.getPageProducts() != null
                    && page.getPageProducts().size() > 0) {
                for (Product p : page.getPageProducts()) {
                    p.setProductReferences(fmgrClient.getProductReferences(p));
                    Metadata met = fmgrClient.getMetadata(p);
                    Reference r = ((Reference) p.getProductReferences().get(0));
                    String newLocPath = PathUtils.replaceEnvVariables(
                        this.pathSpec, met);

                    if (locationsMatch(r.getDataStoreReference(), newLocPath)) {
                        LOG.log(Level.INFO,
                            "Current and New locations match. " + p.getProductName() + " was not moved.");
                        continue;
                    }

                    LOG.log(Level.INFO, "Moving product: ["
                                        + p.getProductName() + "] from: ["
                                        + new File(new URI(r.getDataStoreReference()))
                                        + "] to: [" + newLocPath + "]");
                    long timeBefore = System.currentTimeMillis();
                    fmgrClient.moveProduct(p, newLocPath);
                    long timeAfter = System.currentTimeMillis();
                    double seconds = ((timeAfter - timeBefore) * 1.0) / DOUBLE;
                    LOG.log(Level.INFO, "Product: [" + p.getProductName()
                                        + "] move successful: took: [" + seconds
                                        + "] seconds");
                }

                if (!page.isLastPage()) {
                    page = fmgrClient.getNextPage(type, page);
                }
            }
        }
    }

    private boolean locationsMatch(String currentLocation, String newLocation) throws java.net.URISyntaxException {
    	String currentLocationURI = new URI(currentLocation).getSchemeSpecificPart();
    	String newLocationURI = new URI(newLocation).getSchemeSpecificPart();

        return currentLocationURI.equals(newLocationURI);
    	
    }
    
    public void moveProducts(String typeName)
        throws RepositoryManagerException, CatalogException, DataTransferException, URISyntaxException {
        moveProducts(fmgrClient.getProductTypeByName(typeName));
    }

    /**
     * @param args
     */
    public static void main(String[] args)
        throws URISyntaxException, CatalogException, RepositoryManagerException, DataTransferException,
        InstantiationException {
        String typeName = null, pathSpec = null, fmUrlStr = null;
        String usage = "MetadataBasedProductMover [options]\n"
                + "--typeName <product type>\n"
                + "--fileManagerUrl <url to file manager>\n"
                + "--pathSpec <path spec using '[' and ']' to delimit met fields>\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--typeName")) {
                typeName = args[++i];
            } else if (args[i].equals("--pathSpec")) {
                pathSpec = args[++i];
            } else if (args[i].equals("--fileManagerUrl")) {
                fmUrlStr = args[++i];
            }
        }

        if (typeName == null || pathSpec == null || fmUrlStr == null) {
            System.err.println(usage);
            System.exit(1);
        }

        MetadataBasedProductMover mover = new MetadataBasedProductMover(
                pathSpec, fmUrlStr);
        mover.moveProducts(typeName);
    }

}
