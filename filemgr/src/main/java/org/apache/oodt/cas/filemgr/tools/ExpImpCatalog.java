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

import org.apache.oodt.cas.filemgr.catalog.Catalog;
import org.apache.oodt.cas.filemgr.exceptions.FileManagerException;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.Metadata;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Tool to export from a source file manager catalog and import into a dest
 * file manager catalog.
 * </p>
 * 
 */
public class ExpImpCatalog {


    /* the client to the source catalog to export */
    private FileManagerClient sourceClient = null;

    /* the client to the dest catalog to import into */
    private FileManagerClient destClient = null;

    /* a source catalog I/F to export from (if no fm client is desired) */
    private Catalog srcCatalog = null;

    /* a dest catalog I/F to import into (if no fm client is desired) */
    private Catalog destCatalog = null;

    /* whether or not we should ensure a product doesn't exist before copying */
    private boolean ensureUnique = false;

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(ExpImpCatalog.class
            .getName());

    /**
     * Default Constructor.
     * 
     * @param sUrl
     *            The url to the source file manager to export from.
     * @param dUrl
     *            The url to the dest file manager to import into.
     * @param unique
     *            Whether or not the import tool should ensure that the product
     *            from the source does not exist in the dest.
     */
    public ExpImpCatalog(URL sUrl, URL dUrl, boolean unique) {
        try {
            sourceClient = RpcCommunicationFactory.createClient(sUrl);
        } catch (ConnectionException e) {
            LOG.log(Level.WARNING, "Unable to connect to source filemgr: ["
                    + sUrl + "]");
            throw new RuntimeException(e);
        }

        try {
            destClient = RpcCommunicationFactory.createClient(dUrl);
        } catch (ConnectionException e) {
            LOG.log(Level.WARNING, "Unable to connect to dest filemgr: ["
                    + dUrl + "]");
            throw new RuntimeException(e);
        }

        this.ensureUnique = unique;
    }

    public ExpImpCatalog(String sPropFilePath, String dPropFilePath,
            boolean unique) throws InstantiationException {
        this.ensureUnique = unique;

        LOG.log(Level.INFO, "Constructing tool using catalog interfaces");
        // first load the source prop file
        try {
            System.getProperties().load(
                    new File(sPropFilePath).toURI().toURL().openStream());
        } catch (Exception e) {
            throw new InstantiationException(e.getMessage());
        }

        // now construct the source catalog
        String srcCatFactoryStr = System.getProperty("filemgr.catalog.factory");
        LOG.log(Level.INFO, "source catalog factory: [" + srcCatFactoryStr
                + "]");
        this.srcCatalog = GenericFileManagerObjectFactory
                .getCatalogServiceFromFactory(srcCatFactoryStr);

        // first load the dest prop file
        try {
            System.getProperties().load(
                    new File(dPropFilePath).toURI().toURL().openStream());
        } catch (Exception e) {
            throw new InstantiationException(e.getMessage());
        }

        String destCatFactoryStr = System
                .getProperty("filemgr.catalog.factory");
        LOG
                .log(Level.INFO, "dest catalog factory: [" + destCatFactoryStr
                        + "]");
        this.destCatalog = GenericFileManagerObjectFactory
                .getCatalogServiceFromFactory(destCatFactoryStr);

    }

    public void doExpImport(List sourceProductTypes)
        throws FileManagerException, RepositoryManagerException, CatalogException {

        if (this.sourceClient != null && this.destClient != null) {
            // do validation of source/dest types
            // otherwise, user is on their own discretion
            List destProductTypes = destClient.getProductTypes();

            if (!typesExist(sourceProductTypes, destProductTypes)) {
                throw new FileManagerException(
                        "The source product types must be present in the dest file manager!");
            } else {
                LOG
                        .log(Level.INFO,
                                "Source types and Dest types match: beginning processing");
            }
        } else {
          LOG.log(Level.INFO,
              "Skipping type validation: catalog i/f impls being used.");
        }

        // we'll use the get product page method for each product type
        // paginate through products using source product type

      for (Object sourceProductType : sourceProductTypes) {
        ProductType type = (ProductType) sourceProductType;
        try {
          exportTypeToDest(type);
        } catch (CatalogException e) {
          LOG.log(Level.WARNING, "Error exporting product type: ["
                                 + type.getName() + "] from source to dest: Message: "
                                 + e.getMessage(), e);
          throw e;
        }
      }

    }

    public void doExpImport() throws RepositoryManagerException, FileManagerException, CatalogException {
        if (sourceClient == null) {
          throw new RuntimeException(
              "Cannot request exp/imp of all product types if no filemgr url specified!");
        }
        List sourceProductTypes = sourceClient.getProductTypes();
        doExpImport(sourceProductTypes);
    }

    private void exportTypeToDest(ProductType type) throws CatalogException, RepositoryManagerException {
        ProductPage page;

        if (this.srcCatalog != null) {
            page = srcCatalog.getFirstPage(type);
        } else {
            page = sourceClient.getFirstPage(type);
        }

        if (page == null) {
          return;
        }

        exportProductsToDest(page.getPageProducts(), type);
        while (!page.isLastPage()) {
            if (this.srcCatalog != null) {
                page = srcCatalog.getNextPage(type, page);
            } else {
              page = sourceClient.getNextPage(type, page);
            }
            if (page == null) {
              break;
            }
            exportProductsToDest(page.getPageProducts(), type);
        }
    }

    private void exportProductsToDest(List products, ProductType type)
        throws CatalogException, RepositoryManagerException {
        if (products != null && products.size() > 0) {
          for (Object product : products) {
            Product p = (Product) product;

            if (ensureUnique) {
              boolean hasProduct = safeHasProductTypeByName(p
                  .getProductName());
              if (hasProduct) {
                LOG.log(Level.INFO, "Skipping product: ["
                                    + p.getProductName()
                                    + "]: ensure unique enabled: "
                                    + "product exists in dest catalog");
                continue;
              }
            }

            p.setProductType(type);
            if (sourceClient != null) {
              p
                  .setProductReferences(sourceClient
                      .getProductReferences(p));
            } else {
              p.setProductReferences(srcCatalog.getProductReferences(p));
            }

            Metadata met;

            if (sourceClient != null) {
              met = sourceClient.getMetadata(p);
            } else {
              met = srcCatalog.getMetadata(p);
            }

            LOG
                .log(
                    Level.INFO,
                    "Source Product: ["
                    + p.getProductName()
                    + "]: Met Extraction and "
                    + "Reference Extraction successful: writing to dest file manager");

            // OODT-543
            if (sourceClient != null) {
              // remove the default CAS fields for metadata
              met.removeMetadata("CAS.ProductId");
              met.removeMetadata("CAS.ProductReceivedTime");
              met.removeMetadata("CAS.ProductName");
            }

            Product destProduct = new Product();
            // copy through
            destProduct.setProductName(p.getProductName());
            destProduct.setProductStructure(p.getProductStructure());
            destProduct.setProductType((destClient != null) ? destClient
                .getProductTypeById(type.getProductTypeId()) : type);
            destProduct.setTransferStatus(p.getTransferStatus());

            LOG.log(Level.INFO, "Cataloging Product: ["
                                + p.getProductName() + "]");
            String destProductId;
            if (destCatalog != null) {
              destCatalog.addProduct(destProduct);
              destProductId = destProduct.getProductId();
            } else {
              destProductId = destClient.catalogProduct(destProduct);
            }
            LOG.log(Level.INFO, "Catalog successful: dest product id: ["
                                + destProductId + "]");
            destProduct.setProductId(destProductId);

            LOG.log(Level.INFO, "Adding references for dest product: ["
                                + destProductId + "]");
            destProduct.setProductReferences(p.getProductReferences());
            if (destCatalog != null) {
              destCatalog.addProductReferences(destProduct);
            } else {
              destClient.addProductReferences(destProduct);
            }
            LOG.log(Level.INFO,
                "Reference addition successful for dest product: ["
                + destProductId + "]");

            LOG.log(Level.INFO, "Adding metadata for dest product: ["
                                + destProductId + "]");
            if (destCatalog != null) {
              destCatalog.addMetadata(met, destProduct);
            } else {
              destClient.addMetadata(destProduct, met);
            }
            LOG.log(Level.INFO,
                "Met addition successful for dest product: ["
                + destProductId + "]");

            LOG.log(Level.INFO, "Successful import of product: ["
                                + p.getProductName() + "] into dest file manager");
          }
        }
    }

    /**
     * @return Returns the ensureUnique.
     */
    public boolean isEnsureUnique() {
        return ensureUnique;
    }

    /**
     * @param ensureUnique
     *            The ensureUnique to set.
     */
    public void setEnsureUnique(boolean ensureUnique) {
        this.ensureUnique = ensureUnique;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
        throws RepositoryManagerException, FileManagerException, MalformedURLException, InstantiationException,
        CatalogException {
        String sourceUrl = null, destUrl = null, srcCatPropFile = null, destCatPropFile = null;
        boolean unique = false;
        List types = null;

        String usage = "ExpImpCatalog [options] \n" + "--source <url>\n"
                + "--dest <url>\n " + "--unique\n"
                + "[--types <comma separate list of product type names>]\n"
                + "[--sourceCatProps <file> --destCatProps <file>]\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--source")) {
                sourceUrl = args[++i];
            } else if (args[i].equals("--dest")) {
                destUrl = args[++i];
            } else if (args[i].equals("--unique")) {
                unique = true;
            } else if (args[i].equals("--types")) {
                String[] typesAndIdsEnc = args[++i].split(",");

                types = new Vector(typesAndIdsEnc.length);
              for (String aTypesAndIdsEnc : typesAndIdsEnc) {
                String[] typeIdToks = aTypesAndIdsEnc.split("\\|");
                ProductType type = new ProductType();
                type.setName(typeIdToks[0]);
                type.setProductTypeId(typeIdToks[1]);
                types.add(type);
              }
            } else if (args[i].equals("--sourceCatProps")) {
                srcCatPropFile = args[++i];
            } else if (args[i].equals("--destCatProps")) {
                destCatPropFile = args[++i];
            }
        }

        if (((sourceUrl == null || destUrl == null) && (srcCatPropFile == null || destCatPropFile == null)) || (
            sourceUrl != null && destUrl != null && (srcCatPropFile != null || destCatPropFile != null))) {
            System.err.println(usage);
            System.exit(1);
        }

        ExpImpCatalog tool;

        if (srcCatPropFile != null) {
            tool = new ExpImpCatalog(srcCatPropFile, destCatPropFile, unique);
        } else {
          tool = new ExpImpCatalog(new URL(sourceUrl), new URL(destUrl),
              unique);
        }

        if (types != null && types.size() > 0) {
            tool.doExpImport(types);
        } else {
          tool.doExpImport();
        }
    }

    private boolean typesExist(List sourceList, List destList) {
        if (sourceList == null || (sourceList.size() == 0)) {
            return false;
        }

        if (destList == null || (destList.size() == 0)) {
            return false;
        }

        // iterate through the source types and try and find the type in the
        // destList
      for (Object aSourceList : sourceList) {
        ProductType type = (ProductType) aSourceList;
        if (!typeInList(type, destList)) {
          LOG.log(Level.WARNING, "Source type: [" + type.getName()
                                 + "] not present in dest file manager");
          return false;
        }
      }

        return true;
    }

    private boolean typeInList(ProductType type, List typeList) {
        if (typeList == null || (typeList.size() == 0)) {
            return false;
        }

      for (Object aTypeList : typeList) {
        ProductType destType = (ProductType) aTypeList;
        if (destType.getProductTypeId().equals(type.getProductTypeId())
            && destType.getName().equals(type.getName())) {
          return true;
        }
      }

        return false;
    }

    private boolean safeHasProductTypeByName(String productName) {

        if (destCatalog != null) {
            try {
                return (destCatalog.getProductByName(productName) != null);
            } catch (CatalogException e) {
                LOG.log(Level.SEVERE, e.getMessage());
                LOG
                        .log(Level.WARNING,
                                "Exceptiong checking for product type by name: ["
                                        + productName + "]: Message: "
                                        + e.getMessage());
                return false;
            }
        } else {
            try {
                return destClient.hasProduct(productName);
            } catch (CatalogException e) {
                LOG.log(Level.SEVERE, e.getMessage());
                LOG
                        .log(Level.WARNING,
                                "Exceptiong checking for product type by name: ["
                                        + productName + "]: Message: "
                                        + e.getMessage());
                return false;
            }

        }
    }
}
