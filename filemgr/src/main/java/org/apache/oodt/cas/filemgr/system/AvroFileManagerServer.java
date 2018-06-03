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

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.apache.oodt.cas.filemgr.catalog.Catalog;
import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.avrotypes.*;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.util.AvroTypeFactory;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
/**
 * @author radu
 *
 * <p>Implementaion of FileManagerServer that uses apache avro-ipc API.</p>
 */
public class AvroFileManagerServer implements AvroFileManager, FileManagerServer {

    private static final Logger logger = LoggerFactory.getLogger(AvroFileManagerServer.class);

    /*port for server*/
    protected int port = 1999;

    private Server server;

    /* file manager tools */
    private FileManager fileManager;

    public AvroFileManagerServer(int port){
        this.port = port;
    }

    @Override
    public boolean startUp() throws Exception {
        server = new NettyServer(new SpecificResponder(AvroFileManager.class,this),new InetSocketAddress(this.port));
        server.start();
        try {
            this.fileManager = new FileManager();
            this.loadConfiguration();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void loadConfiguration() throws IOException {
        fileManager.loadConfiguration();

        String transferFactory = null;

        transferFactory = System.getProperty("filemgr.datatransfer.factory",
                "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory");

        DataTransfer dataTransfer = GenericFileManagerObjectFactory
                .getDataTransferServiceFromFactory(transferFactory);

        dataTransfer
                .setFileManagerUrl(new URL("http://localhost:" + port));

        fileManager.setDataTransfer(dataTransfer);

    }


    @Override
    public boolean shutdown() {
        this.server.close();
        return true;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void setCatalog(Catalog catalog) {
        this.fileManager.setCatalog(catalog);

    }

    @Override
    public boolean refreshConfigAndPolicy() throws AvroRemoteException {
        return this.fileManager.refreshConfigAndPolicy();
    }

    @Override
    public boolean transferringProduct(AvroProduct p) throws AvroRemoteException {
        return this.fileManager.transferringProduct(AvroTypeFactory.getProduct(p));
    }

    @Override
    public AvroFileTransferStatus getCurrentFileTransfer() throws AvroRemoteException {
        return AvroTypeFactory.getAvroFileTransferStatus(this.fileManager.getCurrentFileTransfer());
    }

    @Override
    public List<AvroFileTransferStatus> getCurrentFileTransfers() throws AvroRemoteException {
        List<AvroFileTransferStatus> avroFileTransferStatuses = new ArrayList<AvroFileTransferStatus>();
        for (FileTransferStatus fts : this.fileManager.getCurrentFileTransfers()){
            avroFileTransferStatuses.add(AvroTypeFactory.getAvroFileTransferStatus(fts));
        }
        return avroFileTransferStatuses;
    }

    @Override
    public double getProductPctTransferred(AvroProduct product) throws AvroRemoteException {
        return this.fileManager.getProductPctTransferred(AvroTypeFactory.getProduct(product));
    }

    @Override
    public double getRefPctTransferred(AvroReference reference) throws AvroRemoteException {
        return this.fileManager.getRefPctTransferred(AvroTypeFactory.getReference(reference));
    }

    @Override
    public boolean removeProductTransferStatus(AvroProduct product) throws AvroRemoteException {
        return this.fileManager.removeProductTransferStatus(AvroTypeFactory.getProduct(product));
    }

    @Override
    public boolean isTransferComplete(AvroProduct product) throws AvroRemoteException {
        return this.fileManager.isTransferComplete(AvroTypeFactory.getProduct(product));
    }

    @Override
    public AvroProductPage pagedQuery(AvroQuery query, AvroProductType type, int pageNum) throws AvroRemoteException{
        try {
            return AvroTypeFactory.getAvroProductPage(this.fileManager.pagedQuery(
                    AvroTypeFactory.getQuery(query),
                    AvroTypeFactory.getProductType(type),
                    pageNum
            ));
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public AvroProductPage getFirstPage(AvroProductType type) throws AvroRemoteException {
        logger.debug("Getting first page for type: {}", type.getName());
        ProductPage firstPage = this.fileManager.getFirstPage(AvroTypeFactory.getProductType(type));
        logger.debug("Found first page for product type: {} -> {}", type.getName(), firstPage);
        if (firstPage == null) {
            logger.warn("No first page found for product type: {}", type.getName());
            return null;
        }

        return AvroTypeFactory.getAvroProductPage(firstPage);
    }

    @Override
    public AvroProductPage getLastPage(AvroProductType type) throws AvroRemoteException {
        return AvroTypeFactory.getAvroProductPage(
                this.fileManager.getLastPage(AvroTypeFactory.getProductType(type)));
    }

    @Override
    public AvroProductPage getNextPage(AvroProductType type, AvroProductPage currPage) throws AvroRemoteException {

        return AvroTypeFactory.getAvroProductPage(this.fileManager
                        .getNextPage(AvroTypeFactory.getProductType(type), AvroTypeFactory.getProductPage(currPage)));
    }

    @Override
    public AvroProductPage getPrevPage(AvroProductType type, AvroProductPage currPage) throws AvroRemoteException {
        return AvroTypeFactory.getAvroProductPage(
                this.fileManager.getPrevPage(
                        AvroTypeFactory.getProductType(type),
                        AvroTypeFactory.getProductPage(currPage)));
    }

    @Override
    public boolean setProductTransferStatus(AvroProduct product) throws AvroRemoteException {
        try {
            return this.fileManager.setProductTransferStatus(AvroTypeFactory.getProduct(product));
        } catch (CatalogException e) {
            throw new AvroRemoteException(e);
        }
    }

    @Override
    public int getNumProducts(AvroProductType type) throws AvroRemoteException {
        try {
            return this.fileManager.getNumProducts(AvroTypeFactory.getProductType(type));
        } catch (CatalogException e) {
            throw new AvroRemoteException(e);
        }
    }

    @Override
    public List<AvroProduct> getTopNProductsByProductType(int n, AvroProductType type) throws AvroRemoteException {
        List<AvroProduct> avroProducts = new ArrayList<AvroProduct>();
        try {
            for (Product p : this.fileManager.getTopNProductsByProductType(n, AvroTypeFactory.getProductType(type))){
                avroProducts.add(AvroTypeFactory.getAvroProduct(p));
            }
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return avroProducts;
    }

    @Override
    public List<AvroProduct> getTopNProducts(int n) throws AvroRemoteException {
        List<AvroProduct> avroProducts = new ArrayList<AvroProduct>();
        try {
            for (Product p : this.fileManager.getTopNProducts(n)){
                avroProducts.add(AvroTypeFactory.getAvroProduct(p));
            }
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return avroProducts;
    }

    @Override
    public boolean hasProduct(String productName) throws AvroRemoteException {
        try {
            return this.fileManager.hasProduct(productName);
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public AvroMetadata getMetadata(AvroProduct product) throws AvroRemoteException {
        try {
            return AvroTypeFactory.getAvroMetadata(this.fileManager.getMetadata(AvroTypeFactory.getProduct(product)));
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public AvroMetadata getReducedMetadata(AvroProduct product, List<String> elements) throws AvroRemoteException {
        try {
            return AvroTypeFactory.getAvroMetadata(this.fileManager.getReducedMetadata(AvroTypeFactory.getProduct(product), elements));
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public List<AvroProductType> getProductTypes() throws AvroRemoteException {
        List<AvroProductType> avroProductTypes = new ArrayList<AvroProductType>();
        try {
            for (ProductType pt : this.fileManager.getProductTypes()){
                avroProductTypes.add(AvroTypeFactory.getAvroProductType(pt));
            }
        } catch (RepositoryManagerException e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return avroProductTypes;
    }

    @Override
    public List<AvroReference> getProductReferences(AvroProduct product) throws AvroRemoteException {
        List<AvroReference> avroProductTypes = new ArrayList<AvroReference>();
        try {
            for (Reference r : this.fileManager.getProductReferences(AvroTypeFactory.getProduct(product))){
                avroProductTypes.add(AvroTypeFactory.getAvroReference(r));
            }
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return avroProductTypes;
    }

    @Override
    public AvroProduct getProductById(String productId) throws AvroRemoteException {
        try {
            return AvroTypeFactory.getAvroProduct(this.fileManager.getProductById(productId));
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public AvroProduct getProductByName(String productName) throws AvroRemoteException {
        try {
            return AvroTypeFactory.getAvroProduct(this.fileManager.getProductByName(productName));
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public List<AvroProduct> getProductsByProductType(AvroProductType type) throws AvroRemoteException {
        List<AvroProduct> avroProducts = new ArrayList<AvroProduct>();
        try {
            List<Product> products = this.fileManager.getProductsByProductType(AvroTypeFactory.getProductType(type));
            if (products != null) {
                for (Product p : products) {
                    avroProducts.add(AvroTypeFactory.getAvroProduct(p));
                }
            }
            return avroProducts;
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public List<AvroElement> getElementsByProductType(AvroProductType type) throws AvroRemoteException {
        List<AvroElement> avroElements = new ArrayList<AvroElement>();
        try {
            for (Element e : this.fileManager.getElementsByProductType(AvroTypeFactory.getProductType(type))) {
                avroElements.add(AvroTypeFactory.getAvroElement(e));
            }
            return avroElements;
        } catch (ValidationLayerException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public AvroElement getElementById(String elementId) throws AvroRemoteException {
        try {
            return AvroTypeFactory.getAvroElement(this.fileManager.getElementById(elementId));
        } catch (ValidationLayerException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public AvroElement getElementByName(String elementName) throws AvroRemoteException {
        try {
            return AvroTypeFactory.getAvroElement(this.fileManager.getElementByName(elementName));
        } catch (ValidationLayerException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public List<AvroQueryResult> complexQuery(AvroComplexQuery avroComplexQuery) throws AvroRemoteException {
        List<AvroQueryResult> avroQueryResults = new ArrayList<AvroQueryResult>();
        try {
            for (QueryResult qr : this.fileManager.complexQuery(AvroTypeFactory.getComplexQuery(avroComplexQuery))){
                avroQueryResults.add(AvroTypeFactory.getAvroQueryResult(qr));
            }
            return avroQueryResults;
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public List<AvroProduct> query(AvroQuery avroQuery, AvroProductType avroProductType) throws AvroRemoteException {
        List<AvroProduct> avroProducts = new ArrayList<AvroProduct>();
        try {
            for (Product p : this.fileManager.query(AvroTypeFactory.getQuery(avroQuery), AvroTypeFactory.getProductType(avroProductType))){
                avroProducts.add(AvroTypeFactory.getAvroProduct(p));
            }
            return avroProducts;
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public AvroProductType getProductTypeByName(String productTypeName) throws AvroRemoteException {
        try {
            return AvroTypeFactory.getAvroProductType(this.fileManager.getProductTypeByName(productTypeName));
        } catch (RepositoryManagerException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public AvroProductType getProductTypeById(String productTypeId) throws AvroRemoteException {
        try {
            return AvroTypeFactory.getAvroProductType(this.fileManager.getProductTypeById(productTypeId));
        } catch (RepositoryManagerException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public boolean updateMetadata(AvroProduct product, AvroMetadata met) throws AvroRemoteException {
        try {
            return this.fileManager.updateMetadata(AvroTypeFactory.getProduct(product), AvroTypeFactory.getMetadata(met));
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public String addProductType(AvroProductType type) throws AvroRemoteException {
        try {
            return this.fileManager.addProductType(AvroTypeFactory.getProductType(type));
        } catch (RepositoryManagerException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public String catalogProduct(AvroProduct product) throws AvroRemoteException {
        try {
            return this.fileManager.catalogProduct(AvroTypeFactory.getProduct(product));
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public boolean addMetadata(AvroProduct product, AvroMetadata met) throws AvroRemoteException {
        try {
            return this.fileManager.addMetadata(AvroTypeFactory.getProduct(product), AvroTypeFactory.getMetadata(met)) != null;
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public boolean addProductReferences(AvroProduct product) throws AvroRemoteException {
        try {
            return this.fileManager.addProductReferences(AvroTypeFactory.getProduct(product));
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public String ingestProduct(AvroProduct p, AvroMetadata m, boolean clientTransfer) throws AvroRemoteException {
        try {
            return this.fileManager.ingestProduct(AvroTypeFactory.getProduct(p), AvroTypeFactory.getMetadata(m), clientTransfer);
        } catch (VersioningException e) {
            throw new AvroRemoteException(e.getMessage());
        } catch (RepositoryManagerException e) {
            throw new AvroRemoteException(e.getMessage());
        } catch (DataTransferException e) {
            throw new AvroRemoteException(e.getMessage());
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public ByteBuffer retrieveFile(String filePath, int offset, int numBytes) throws AvroRemoteException {
        try {
            return ByteBuffer.wrap(this.fileManager.retrieveFile(filePath, offset, numBytes));
        } catch (DataTransferException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public boolean transferFile(String filePath, ByteBuffer fileData, int offset, int numBytes) throws AvroRemoteException {
        return this.fileManager.transferFile(filePath,fileData.array(),offset,numBytes);
    }

    @Override
    public boolean moveProduct(AvroProduct p, String newPath) throws AvroRemoteException {
        try {
            return this.fileManager.moveProduct(AvroTypeFactory.getProduct(p), newPath);
        } catch (DataTransferException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public boolean removeFile(String filePath) throws AvroRemoteException {
        try {
            return this.fileManager.removeFile(filePath);
        } catch (DataTransferException e) {
            throw new AvroRemoteException(e.getMessage());
        } catch (IOException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public boolean modifyProduct(AvroProduct p) throws AvroRemoteException {
        try {
            return this.fileManager.modifyProduct(AvroTypeFactory.getProduct(p));
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public boolean removeProduct(AvroProduct p) throws AvroRemoteException {
        try {
            return this.fileManager.removeProduct(AvroTypeFactory.getProduct(p));
        } catch (CatalogException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public AvroMetadata getCatalogValues(AvroMetadata m, AvroProductType productType) throws AvroRemoteException {
        try {
            return AvroTypeFactory.getAvroMetadata(this.fileManager.getCatalogValues(
                    AvroTypeFactory.getMetadata(m), AvroTypeFactory.getProductType(productType)
            ));
        } catch (RepositoryManagerException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public AvroMetadata getOrigValues(AvroMetadata m, AvroProductType productType) throws AvroRemoteException {
        try {
            return AvroTypeFactory.getAvroMetadata(this.fileManager.getOrigValues(
                    AvroTypeFactory.getMetadata(m),
                    AvroTypeFactory.getProductType(productType)
            ));
        } catch (RepositoryManagerException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public AvroQuery getCatalogQuery(AvroQuery query, AvroProductType productType) throws AvroRemoteException {
        try {
            return AvroTypeFactory.getAvroQuery(this.fileManager.getCatalogQuery(
                    AvroTypeFactory.getQuery(query),
                    AvroTypeFactory.getProductType(productType)
            ));
        } catch (RepositoryManagerException e) {
            throw new AvroRemoteException(e.getMessage());
        } catch (QueryFormulationException e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

}
