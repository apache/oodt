package org.apache.oodt.cas.filemgr.cli.action;

import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.structs.*;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class DummyFileManagerClient implements FileManagerClient {


    URL url;

    public DummyFileManagerClient(URL url,boolean testConnection){
        this.url = url;
    }


    @Override
    public boolean refreshConfigAndPolicy() {
        return false;
    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    public boolean transferringProduct(Product product) throws DataTransferException {
        return false;
    }

    @Override
    public boolean removeProductTransferStatus(Product product) throws DataTransferException {
        return false;
    }

    @Override
    public boolean isTransferComplete(Product product) throws DataTransferException {
        return false;
    }

    @Override
    public boolean moveProduct(Product product, String newPath) throws DataTransferException {
        return false;
    }

    @Override
    public boolean modifyProduct(Product product) throws CatalogException {
        return false;
    }

    @Override
    public boolean removeProduct(Product product) throws CatalogException {
        return false;
    }

    @Override
    public FileTransferStatus getCurrentFileTransfer() throws DataTransferException {
        return null;
    }

    @Override
    public List<FileTransferStatus> getCurrentFileTransfers() throws DataTransferException {
        return null;
    }

    @Override
    public double getProductPctTransferred(Product product) throws DataTransferException {
        return 0;
    }

    @Override
    public double getRefPctTransferred(Reference reference) throws DataTransferException {
        return 0;
    }

    @Override
    public ProductPage pagedQuery(Query query, ProductType type, int pageNum) throws CatalogException {
        return null;
    }

    @Override
    public ProductPage getFirstPage(ProductType type) throws CatalogException {
        return null;
    }

    @Override
    public ProductPage getLastPage(ProductType type) throws CatalogException {
        return null;
    }

    @Override
    public ProductPage getNextPage(ProductType type, ProductPage currPage) throws CatalogException {
        return null;
    }

    @Override
    public ProductPage getPrevPage(ProductType type, ProductPage currPage) throws CatalogException {
        return null;
    }

    @Override
    public String addProductType(ProductType type) throws RepositoryManagerException {
        return null;
    }

    @Override
    public boolean hasProduct(String productName) throws CatalogException {
        return false;
    }

    @Override
    public int getNumProducts(ProductType type) throws CatalogException {
        return 0;
    }

    @Override
    public List<Product> getTopNProducts(int n) throws CatalogException {
        return null;
    }

    @Override
    public List<Product> getTopNProducts(int n, ProductType type) throws CatalogException {
        return null;
    }

    @Override
    public void setProductTransferStatus(Product product) throws CatalogException {

    }

    @Override
    public void addProductReferences(Product product) throws CatalogException {

    }

    @Override
    public void addMetadata(Product product, Metadata metadata) throws CatalogException {

    }

    @Override
    public boolean updateMetadata(Product product, Metadata met) throws CatalogException {
        return false;
    }

    @Override
    public String catalogProduct(Product product) throws CatalogException {
        return null;
    }

    @Override
    public Metadata getMetadata(Product product) throws CatalogException {
        return null;
    }

    @Override
    public Metadata getReducedMetadata(Product product, List<?> elements) throws CatalogException {
        return null;
    }

    @Override
    public boolean removeFile(String filePath) throws DataTransferException {
        return false;
    }

    @Override
    public byte[] retrieveFile(String filePath, int offset, int numBytes) throws DataTransferException {
        return new byte[0];
    }

    @Override
    public void transferFile(String filePath, byte[] fileData, int offset, int numBytes) throws DataTransferException {

    }

    @Override
    public List<Product> getProductsByProductType(ProductType type) throws CatalogException {
        return null;
    }

    @Override
    public List<Element> getElementsByProductType(ProductType type) throws ValidationLayerException {
        return null;
    }

    @Override
    public Element getElementById(String elementId) throws ValidationLayerException {
        return null;
    }

    @Override
    public Element getElementByName(String elementName) throws ValidationLayerException {
        return null;
    }

    @Override
    public List<QueryResult> complexQuery(ComplexQuery complexQuery) throws CatalogException {
        return null;
    }

    @Override
    public List<Product> query(Query query, ProductType type) throws CatalogException {
        return null;
    }

    @Override
    public ProductType getProductTypeByName(String productTypeName) throws RepositoryManagerException {
        return null;
    }

    @Override
    public ProductType getProductTypeById(String productTypeId) throws RepositoryManagerException {
        return null;
    }

    @Override
    public List<ProductType> getProductTypes() throws RepositoryManagerException {
        return null;
    }

    @Override
    public List<Reference> getProductReferences(Product product) throws CatalogException {
        return null;
    }

    @Override
    public Product getProductById(String productId) throws CatalogException {
        return null;
    }

    @Override
    public Product getProductByName(String productName) throws CatalogException {
        return null;
    }

    @Override
    public String ingestProduct(Product product, Metadata metadata, boolean clientTransfer) throws Exception {
        return null;
    }

    @Override
    public Metadata getCatalogValues(Metadata metadata, ProductType productType) throws XmlRpcException, IOException {
        return null;
    }

    @Override
    public Metadata getOrigValues(Metadata metadata, ProductType productType) throws XmlRpcException, IOException {
        return null;
    }

    @Override
    public Query getCatalogQuery(Query query, ProductType productType) throws XmlRpcException, IOException {
        return null;
    }

    @Override
    public URL getFileManagerUrl() {
        return this.url;
    }

    @Override
    public void setFileManagerUrl(URL fileManagerUrl) {

    }

    @Override
    public DataTransfer getDataTransfer() {
        return null;
    }

    @Override
    public void setDataTransfer(DataTransfer dataTransfer) {

    }

    @Override
    public void close() throws IOException {

    }
}
