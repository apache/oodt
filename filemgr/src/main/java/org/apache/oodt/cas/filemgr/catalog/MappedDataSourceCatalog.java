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

package org.apache.oodt.cas.filemgr.catalog;

//JDK imports
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;

//OODT imports
import org.apache.oodt.cas.filemgr.catalog.DataSourceCatalog;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * An extension to the {@link DataSourceCatalog} where product type names are
 * mapped to the table names in the database (to deal with large product type
 * names, which Oracle doesn't like)
 * </p>.
 */
public class MappedDataSourceCatalog extends DataSourceCatalog {

    private Properties typeMap;

    /**
     * Constructs a new MappedDataSourceCatalog.
     * 
     * @param ds
     *            The {@link DataSource} connection to a DBMS catalog.
     * @param valLayer
     *            The {@link ValidationLayer} storign the element policy.
     * @param fieldId
     *            Whether or not field ids should be quoted.
     * @param pageSize
     *            The page size of returned products via Pagination API.
     * @param cacheUpdateMin
     *            The amount of minutes inbetween cache updates (if needed).
     * @param typeMap
     *            The mapping properties mapping product type names to table
     *            name.
     */
    public MappedDataSourceCatalog(DataSource ds, ValidationLayer valLayer,
            boolean fieldId, int pageSize, long cacheUpdateMin,
            Properties typeMap) {
        super(ds, valLayer, fieldId, pageSize, cacheUpdateMin);
        this.typeMap = typeMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.DataSourceCatalog#addMetadata(
     *      org.apache.oodt.cas.metadata.Metadata,
     *      org.apache.oodt.cas.filemgr.structs.Product)
     */
    @Override
    public synchronized void addMetadata(Metadata metadata, Product product)
            throws CatalogException {
        String origProductTypeName = product.getProductType().getName();
        product.getProductType().setName(
                getProductTypeTableName(origProductTypeName));
        super.addMetadata(metadata, product);
        product.getProductType().setName(origProductTypeName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.DataSourceCatalog#addProductReferences(
     *      org.apache.oodt.cas.filemgr.structs.Product)
     */
    @Override
    public synchronized void addProductReferences(Product product)
            throws CatalogException {
        String origProductTypeName = product.getProductType().getName();
        product.getProductType().setName(
                getProductTypeTableName(origProductTypeName));
        super.addProductReferences(product);
        product.getProductType().setName(origProductTypeName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.DataSourceCatalog#getMetadata(
     *      org.apache.oodt.cas.filemgr.structs.Product)
     */
    @Override
    public Metadata getMetadata(Product product) throws CatalogException {
        String origProductTypeName = product.getProductType().getName();
        product.getProductType().setName(
                getProductTypeTableName(origProductTypeName));
        Metadata met = super.getMetadata(product);
        product.getProductType().setName(origProductTypeName);
        return met;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.DataSourceCatalog#getProductReferences(
     *      org.apache.oodt.cas.filemgr.structs.Product)
     */
    @Override
    public List<Reference> getProductReferences(Product product) throws CatalogException {
        String origProductTypeName = product.getProductType().getName();
        product.getProductType().setName(
                getProductTypeTableName(origProductTypeName));
        List<Reference> refs = super.getProductReferences(product);
        product.getProductType().setName(origProductTypeName);
        return refs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.DataSourceCatalog#modifyProduct(
     *      org.apache.oodt.cas.filemgr.structs.Product)
     */
    @Override
    public synchronized void modifyProduct(Product product)
            throws CatalogException {
        String origProductTypeName = product.getProductType().getName();
        product.getProductType().setName(
                getProductTypeTableName(origProductTypeName));
        super.modifyProduct(product);
        product.getProductType().setName(origProductTypeName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.DataSourceCatalog#removeMetadata(
     *      org.apache.oodt.cas.metadata.Metadata,
     *      org.apache.oodt.cas.filemgr.structs.Product)
     */
    @Override
    public synchronized void removeMetadata(Metadata metadata, Product product)
            throws CatalogException {
        String origProductTypeName = product.getProductType().getName();
        product.getProductType().setName(
                getProductTypeTableName(origProductTypeName));
        super.removeMetadata(metadata, product);
        product.getProductType().setName(origProductTypeName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.DataSourceCatalog#removeProduct(
     *      org.apache.oodt.cas.filemgr.structs.Product)
     */
    @Override
    public synchronized void removeProduct(Product product)
            throws CatalogException {
        String origProductTypeName = product.getProductType().getName();
        product.getProductType().setName(
                getProductTypeTableName(origProductTypeName));
        super.removeProduct(product);
        product.getProductType().setName(origProductTypeName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.DataSourceCatalog#pagedQuery(org.apache.oodt.cas.filemgr.structs.Query,
     *      org.apache.oodt.cas.filemgr.structs.ProductType, int)
     */
    @Override
    public ProductPage pagedQuery(Query query, ProductType type, int pageNum)
            throws CatalogException {
        String origProductTypeName = type.getName();
        type.setName(getProductTypeTableName(origProductTypeName));
        ProductPage page = super.pagedQuery(query, type, pageNum);
        type.setName(origProductTypeName);
        return page;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.DataSourceCatalog#query(org.apache.oodt.cas.filemgr.structs.Query,
     *      org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    @Override
    public List<String> query(Query query, ProductType type) throws CatalogException {
        String origProductTypeName = type.getName();
        type.setName(getProductTypeTableName(origProductTypeName));
        List<String> results = super.query(query, type);
        type.setName(origProductTypeName);
        return results;
    }

    protected String getProductTypeTableName(String origName) {
        if (typeMap != null && typeMap.containsKey(origName)) {
            return typeMap.getProperty(origName);
        } else {
            return origName;
        }
    }

}
