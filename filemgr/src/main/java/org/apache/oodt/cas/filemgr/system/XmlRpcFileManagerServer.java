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

import org.apache.oodt.cas.filemgr.catalog.Catalog;
import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.util.XmlRpcStructFactory;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.xmlrpc.WebServer;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @deprecated replaced by avro-rpc
 */
@Deprecated
public class XmlRpcFileManagerServer implements FileManagerServer {

    /* the port to run the XML RPC web server on, default is 1999 */
    protected int port = 1999;

    /* our xml rpc web server */
    private WebServer webServer = null;

    /* file manager tools */
    FileManager fileManager;
    
    public XmlRpcFileManagerServer(int port){
        this.port = port;
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
    public boolean startUp() throws Exception {
        webServer = new WebServer(this.port);
        webServer.addHandler("filemgr", this);
        webServer.start();
        this.fileManager = new FileManager();
        this.loadConfiguration();
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
    
    public boolean refreshConfigAndPolicy() {

        boolean success = fileManager.refreshConfigAndPolicy();
        try {
        String transferFactory = null;

        transferFactory = System.getProperty("filemgr.datatransfer.factory",
                "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory");

        DataTransfer dataTransfer = GenericFileManagerObjectFactory
                .getDataTransferServiceFromFactory(transferFactory);

        dataTransfer
                .setFileManagerUrl(new URL("http://localhost:" + port));
        fileManager.setDataTransfer(dataTransfer);


            fileManager.loadConfiguration();
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        }
        return success;

    }

    public boolean transferringProduct(Hashtable<String, Object> productHash) {
        return fileManager.transferringProduct(XmlRpcStructFactory.getProductFromXmlRpc(productHash));
    }

    public Map<String, Object> getCurrentFileTransfer() {
        FileTransferStatus status = fileManager.getCurrentFileTransfer();
        if (status == null) {
            return new Hashtable<String, Object>();
        } else
            return XmlRpcStructFactory.getXmlRpcFileTransferStatus(status);
    }

    public Vector<Map<String, Object>> getCurrentFileTransfers() {
        List<FileTransferStatus> currentTransfers = fileManager.getCurrentFileTransfers();

        if (currentTransfers != null && currentTransfers.size() > 0) {
            return XmlRpcStructFactory
                    .getXmlRpcFileTransferStatuses(currentTransfers);
        } else
            return new Vector<>();
    }

    public double getRefPctTransferred(Hashtable<String, Object> refHash) {
        return fileManager.getRefPctTransferred(XmlRpcStructFactory
                .getReferenceFromXmlRpc(refHash));
    }

    public boolean removeProductTransferStatus(Hashtable<String, Object> productHash) {
        return fileManager.removeProductTransferStatus(XmlRpcStructFactory.getProductFromXmlRpc(productHash));
    }

    public boolean isTransferComplete(Hashtable<String, Object> productHash) {
        return fileManager.isTransferComplete(XmlRpcStructFactory.getProductFromXmlRpc(productHash)) ;
    }

    public Map<String, Object> pagedQuery(
            Hashtable<String, Object> queryHash,
            Hashtable<String, Object> productTypeHash,
            int pageNum) throws CatalogException {

        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        Query query = XmlRpcStructFactory.getQueryFromXmlRpc(queryHash);

        return XmlRpcStructFactory.getXmlRpcProductPage(fileManager.pagedQuery(query, type, pageNum));
    }


    public Map<String, Object> getFirstPage(
            Hashtable<String, Object> productTypeHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);

        return XmlRpcStructFactory.getXmlRpcProductPage(fileManager.getFirstPage(type));
    }

    public Map<String, Object> getLastPage(
            Hashtable<String, Object> productTypeHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);

        return XmlRpcStructFactory.getXmlRpcProductPage(fileManager.getLastPage(type));
    }

    public Map<String, Object> getNextPage(
            Hashtable<String, Object> productTypeHash,
            Hashtable<String, Object> currentPageHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        ProductPage currPage = XmlRpcStructFactory
                .getProductPageFromXmlRpc(currentPageHash);

        return XmlRpcStructFactory.getXmlRpcProductPage(fileManager.getNextPage(type, currPage));
    }

    public double getProductPctTransferred(Hashtable<String, Object> productHash) {
        return this.fileManager.getProductPctTransferred(XmlRpcStructFactory.getProductFromXmlRpc(productHash));
    }


    public Map<String, Object> getPrevPage(
            Hashtable<String, Object> productTypeHash,
            Hashtable<String, Object> currentPageHash) {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        ProductPage currPage = XmlRpcStructFactory
                .getProductPageFromXmlRpc(currentPageHash);

        return XmlRpcStructFactory.getXmlRpcProductPage(fileManager.getPrevPage(type, currPage));
    }

    public String addProductType(Hashtable<String, Object> productTypeHash)
            throws RepositoryManagerException {
        ProductType productType = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);

        return fileManager.addProductType(productType);

    }

    public synchronized boolean setProductTransferStatus(
            Hashtable<String, Object> productHash)
            throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);

        return fileManager.setProductTransferStatus(product);
    }

    public int getNumProducts(Hashtable<String, Object> productTypeHash)
            throws CatalogException {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);

        return fileManager.getNumProducts(type);
    }

    public Vector<Map<String, Object>> getTopNProducts(int n)
            throws CatalogException {
            return XmlRpcStructFactory.getXmlRpcProductList(fileManager.getTopNProducts(n));
    }


    public Vector<Map<String, Object>> getTopNProducts(int n,
                                                       Hashtable<String, Object> productTypeHash)
            throws CatalogException {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
            return XmlRpcStructFactory.getXmlRpcProductList(fileManager.getTopNProductsByProductType(n, type));
    }


    public boolean hasProduct(String productName) throws CatalogException {
        return fileManager.hasProduct(productName);
    }

    public Hashtable<String, Object> getMetadata(
            Hashtable<String, Object> productHash) throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return fileManager.getMetadata(product).getHashTable();
    }

    public Hashtable<String, Object> getReducedMetadata(
            Hashtable<String, Object> productHash, Vector<String> elements)
            throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return fileManager.getReducedMetadata(product, elements).getHashTable();
    }

    public Vector<Map<String, Object>> getProductTypes()
            throws RepositoryManagerException {
            return XmlRpcStructFactory.getXmlRpcProductTypeList(fileManager.getProductTypes());
    }

    public Vector<Map<String, Object>> getProductReferences(
            Hashtable<String, Object> productHash)
            throws CatalogException {
            Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
            return XmlRpcStructFactory.getXmlRpcReferences(fileManager.getProductReferences(product));
    }

    public Map<String, Object> getProductById(String productId)
            throws CatalogException {
        Product product = fileManager.getProductById(productId);
        return XmlRpcStructFactory.getXmlRpcProduct(product);
    }

    public Map<String, Object> getProductByName(String productName)
            throws CatalogException {

        Product product = fileManager.getProductByName(productName);
        return XmlRpcStructFactory.getXmlRpcProduct(product);
    }

    public Vector<Map<String, Object>> getProductsByProductType(
            Hashtable<String, Object> productTypeHash)
            throws CatalogException {
        ProductType type = XmlRpcStructFactory.getProductTypeFromXmlRpc(productTypeHash);
        return XmlRpcStructFactory.getXmlRpcProductList(fileManager.getProductsByProductType(type));
    }

    public Vector<Map<String, Object>> getElementsByProductType(
            Hashtable<String, Object> productTypeHash) throws ValidationLayerException {
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        return XmlRpcStructFactory.getXmlRpcElementList(fileManager.getElementsByProductType(type));
    }

    public Map<String, Object> getElementById(String elementId)
            throws ValidationLayerException {
            return XmlRpcStructFactory.getXmlRpcElement(fileManager.getElementById(elementId));
    }

    public Map<String, Object> getElementByName(String elementName)
            throws ValidationLayerException {
            return XmlRpcStructFactory.getXmlRpcElement(fileManager.getElementByName(elementName));
    }

    public Vector<Map<String, Object>> complexQuery(
            Hashtable<String, Object> complexQueryHash) throws CatalogException {
            ComplexQuery complexQuery = XmlRpcStructFactory
                    .getComplexQueryFromXmlRpc(complexQueryHash);
            return XmlRpcStructFactory.getXmlRpcQueryResults(fileManager.complexQuery(complexQuery));
    }

    public Vector<Map<String, Object>> query(
            Hashtable<String, Object> queryHash,
            Hashtable<String, Object> productTypeHash)
            throws CatalogException {
        Query query = XmlRpcStructFactory.getQueryFromXmlRpc(queryHash);
        ProductType type = XmlRpcStructFactory
                .getProductTypeFromXmlRpc(productTypeHash);
        return XmlRpcStructFactory.getXmlRpcProductList(fileManager.query(query, type));
    }

    public Map<String, Object> getProductTypeByName(String productTypeName)
            throws RepositoryManagerException {
        return XmlRpcStructFactory.getXmlRpcProductType(fileManager.getProductTypeByName(productTypeName));
    }

    public Map<String, Object> getProductTypeById(String productTypeId)
            throws RepositoryManagerException {
            return XmlRpcStructFactory.getXmlRpcProductType(fileManager.getProductTypeById(productTypeId));
    }

    public boolean updateMetadata(Hashtable<String, Object> productHash,
                                               Hashtable<String, Object> metadataHash) throws CatalogException{
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        Metadata met = new Metadata();
        met.addMetadata(metadataHash);
        fileManager.updateMetadata(product, met);
        return true;
    }

    public String catalogProduct(Hashtable<String, Object> productHash)
            throws CatalogException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return fileManager.catalogProduct(p);
    }

    public synchronized boolean addMetadata(Hashtable<String, Object> productHash,
                                            Hashtable<String, String> metadata) throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        Metadata met = new Metadata();
        met.addMetadata((Hashtable)metadata);
        return fileManager.addMetadata(product, met) != null;
    }

    public synchronized boolean addProductReferences(Hashtable<String, Object> productHash)
            throws CatalogException {
        Product product = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return fileManager.addProductReferences(product);
    }

    public String ingestProduct(Hashtable<String, Object> productHash,
                                Hashtable<String, String> metadata, boolean clientTransfer)
            throws VersioningException, RepositoryManagerException,
            DataTransferException, CatalogException {

        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);

        Metadata m = new Metadata();
        m.addMetadata((Hashtable)metadata);

        return fileManager.ingestProduct(p, m, clientTransfer);
    }

    public byte[] retrieveFile(String filePath, int offset, int numBytes)
            throws DataTransferException {
        return fileManager.retrieveFile(filePath, offset, numBytes);
    }

    public boolean transferFile(String filePath, byte[] fileData, int offset,
                                int numBytes) {
        return fileManager.transferFile(filePath, fileData, offset, numBytes);
    }

    public boolean moveProduct(Hashtable<String, Object> productHash, String newPath)
            throws DataTransferException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return fileManager.moveProduct(p, newPath);
    }

    public boolean removeFile(String filePath) throws DataTransferException, IOException {
        return fileManager.removeFile(filePath);
    }

    public boolean modifyProduct(Hashtable<?, ?> productHash) throws CatalogException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return fileManager.modifyProduct(p);
    }

    public boolean removeProduct(Hashtable<String, Object> productHash) throws CatalogException {
        Product p = XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        return fileManager.removeProduct(p);
    }

    public Hashtable<String, Object> getCatalogValues(
            Hashtable<String, Object> metadataHash,
            Hashtable<String, Object> productTypeHash)
            throws RepositoryManagerException {
        Metadata m = new Metadata();
        m.addMetadata(metadataHash);
        ProductType productType = XmlRpcStructFactory.getProductTypeFromXmlRpc(productTypeHash);
        return fileManager.getCatalogValues(m, productType).getHashTable();
    }

    public Hashtable<String, Object> getOrigValues(
            Hashtable<String, Object> metadataHash,
            Hashtable<String, Object> productTypeHash)
            throws RepositoryManagerException {
        Metadata m = new Metadata();
        m.addMetadata(metadataHash);
        ProductType productType = XmlRpcStructFactory.getProductTypeFromXmlRpc(productTypeHash);
        return fileManager.getOrigValues(m, productType).getHashTable();
    }

    public Map<String, Object> getCatalogQuery(
            Hashtable<String, Object> queryHash,
            Hashtable<String, Object> productTypeHash)
            throws RepositoryManagerException, QueryFormulationException {
        Query query = XmlRpcStructFactory.getQueryFromXmlRpc(queryHash);
        ProductType productType = XmlRpcStructFactory.getProductTypeFromXmlRpc(productTypeHash);
        return XmlRpcStructFactory.getXmlRpcQuery(fileManager.getCatalogQuery(query, productType));
    }

    public boolean shutdown() {
        if (this.webServer != null) {
            this.webServer.shutdown();
            this.webServer = null;
            return true;
        } else
            return false;
    }

}
