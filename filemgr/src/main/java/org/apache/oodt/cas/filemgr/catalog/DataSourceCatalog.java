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

//OODT imports
import org.apache.oodt.cas.filemgr.structs.BooleanQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.util.DbStructFactory;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;
import org.apache.oodt.commons.pagination.PaginationUtils;
import org.apache.oodt.commons.util.DateConvert;

//JDK imports
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

//Apache imports
import org.apache.commons.lang.StringUtils;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Implementation of a {@link Catalog} that is backed by a {@link DataSource}
 * front-end to a SQL DBMS.
 * </p>
 * 
 */
public class DataSourceCatalog extends AbstractCatalog {

    /* our sql data source */
    protected DataSource dataSource = null;

    /* our log stream */
    private static Logger LOG = Logger.getLogger(DataSourceCatalog.class.getName());

    /* our validation layer */
    private ValidationLayer validationLayer = null;

    /* boolean flag on whether or not product type id and element id are strings */
    protected boolean fieldIdStringFlag = false;

    /* size of pages of products within the catalog */
    protected int pageSize = -1;

    /*
     * cache of products per product type: [productTypeId]=>([ISO8601 time of
     * last update]=>[List of products])
     */
    private static TreeMap<String, TreeMap<String, Object>> PRODUCT_CACHE = new TreeMap<String, TreeMap<String, Object>>();

    /*
     * the amount of minutes inbetween the time in which we should update the
     * PRODUCT_CACHE
     */
    private long cacheUpdateMinutes = 0L;

    /**
     * <p>
     * Default Constructor
     * </p>.
     * @throws  
     */
    public DataSourceCatalog(String jdbcUrl, String user, String pass, String driver, ValidationLayer valLayer,
            boolean fieldId, int pageSize, long cacheUpdateMin) {
        this.dataSource = DatabaseConnectionBuilder.buildDataSource(user, pass,
                driver, jdbcUrl);
        this.validationLayer = valLayer;
        fieldIdStringFlag = fieldId;
        this.pageSize = pageSize;
        cacheUpdateMinutes = cacheUpdateMin;
    }
    
    public int getPageSize() {
    	return this.pageSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#addMetadata(gov.nasa.jpl.oodt.cas.metadata.Metadata,
     *      gov.nasa.jpl.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void addMetadata(Metadata m, Product product)
            throws CatalogException {
        List<Element> metadataTypes = null;

        try {
            metadataTypes = validationLayer.getElements(product
                    .getProductType());
        } catch (ValidationLayerException e) {
            e.printStackTrace();
            throw new CatalogException(
                    "ValidationLayerException when trying to obtain element list for product type: "
                            + product.getProductType().getName()
                            + ": Message: " + e.getMessage());
        }

        for (Iterator<Element> i = metadataTypes.iterator(); i.hasNext();) {
            Element element = i.next();
            List<String> values = m.getAllMetadata(element.getElementName());

            if (values == null) {
                LOG.log(Level.WARNING, "No Metadata specified for product ["
                        + product.getProductName() + "] for required field ["
                        + element.getElementName()
                        + "]: Attempting to continue processing metadata");
                continue;
            }

            for (Iterator<String> j = values.iterator(); j.hasNext();) {
                String value = j.next();

                try {
                    addMetadataValue(element, product, value);
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG
                            .log(
                                    Level.WARNING,
                                    "Exception ingesting metadata. Error inserting field: ["
                                            + element.getElementId()
                                            + "=>"
                                            + value
                                            + "]: for product: ["
                                            + product.getProductName()
                                            + "]: Message: "
                                            + e.getMessage()
                                            + ": Attempting to continue processing metadata");
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#addMetadata(gov.nasa.jpl.oodt.cas.metadata.Metadata,
     *      gov.nasa.jpl.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void removeMetadata(Metadata m, Product product)
            throws CatalogException {
        List<Element> metadataTypes = null;

        try {
            metadataTypes = validationLayer.getElements(product
                    .getProductType());
        } catch (ValidationLayerException e) {
            e.printStackTrace();
            throw new CatalogException(
                    "ValidationLayerException when trying to obtain element list for product type: "
                            + product.getProductType().getName()
                            + ": Message: " + e.getMessage());
        }

        for (Iterator<Element> i = metadataTypes.iterator(); i.hasNext();) {
            Element element = i.next();
            List<String> values = m.getAllMetadata(element.getElementName());

            if (values != null) {
                for (Iterator<String> j = values.iterator(); j.hasNext();) {
                    String value = j.next();

                    try {
                        removeMetadataValue(element, product, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOG
                                .log(
                                        Level.WARNING,
                                        "Exception removing metadata. Error deleting field: ["
                                                + element.getElementId()
                                                + "=>"
                                                + value
                                                + "]: for product: ["
                                                + product.getProductName()
                                                + "]: Message: "
                                                + e.getMessage()
                                                + ": Attempting to continue processing metadata");
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#addProduct(gov.nasa.jpl.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void addProduct(Product product)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String addProductSql = null;
            String productTypeIdStr = null;

            if (fieldIdStringFlag) {
                productTypeIdStr = "'"
                        + product.getProductType().getProductTypeId() + "'";
            } else {
                productTypeIdStr = product.getProductType().getProductTypeId();
            }

            addProductSql = "INSERT INTO products (product_name, product_structure, product_transfer_status, product_type_id) "
                    + "VALUES ('"
                    + product.getProductName()
                    + "', '"
                    + product.getProductStructure()
                    + "', '"
                    + product.getTransferStatus()
                    + "', "
                    + productTypeIdStr
                    + ")";

            LOG.log(Level.FINE, "addProduct: Executing: " + addProductSql);
            statement.execute(addProductSql);

            String productId = new String();

            String getProductIdSql = "SELECT MAX(product_id) AS max_id FROM products";

            rs = statement.executeQuery(getProductIdSql);

            while (rs.next()) {
                productId = String.valueOf(rs.getInt("max_id"));
            }

            product.setProductId(productId);
            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception adding product. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback addProduct transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#modifyProduct(gov.nasa.jpl.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void modifyProduct(Product product)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String modifyProductSql = "UPDATE products SET product_name='"
                    + product.getProductName() + "', product_structure='"
                    + product.getProductStructure()
                    + "', product_transfer_status='"
                    + product.getTransferStatus() + "' "
                    + "WHERE product_id = " + product.getProductId();

            LOG
                    .log(Level.FINE, "modifyProduct: Executing: "
                            + modifyProductSql);
            statement.execute(modifyProductSql);
            conn.commit();

            // now update the refs
            updateReferences(product);

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception modifying product. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback modifyProduct transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#removeProduct(gov.nasa.jpl.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void removeProduct(Product product)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String deleteProductSql = "DELETE FROM products WHERE product_id = "
                    + product.getProductId();

            LOG
                    .log(Level.FINE, "removeProduct: Executing: "
                            + deleteProductSql);
            statement.execute(deleteProductSql);
            deleteProductSql = "DELETE FROM "
                    + product.getProductType().getName() + "_metadata "
                    + " WHERE product_id = " + product.getProductId();
            LOG
                    .log(Level.FINE, "removeProduct: Executing: "
                            + deleteProductSql);
            statement.execute(deleteProductSql);
            deleteProductSql = "DELETE FROM "
                    + product.getProductType().getName() + "_reference "
                    + " WHERE product_id = " + product.getProductId();
            LOG
                    .log(Level.FINE, "removeProduct: Executing: "
                            + deleteProductSql);
            statement.execute(deleteProductSql);
            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception removing product. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback removeProduct transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#setProductTransferStatus(gov.nasa.jpl.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void setProductTransferStatus(Product product)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String modifyProductSql = "UPDATE products SET product_transfer_status='"
                    + product.getTransferStatus()
                    + "' "
                    + "WHERE product_id = " + product.getProductId();

            LOG.log(Level.FINE, "setProductTransferStatus: Executing: "
                    + modifyProductSql);
            statement.execute(modifyProductSql);
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception setting transfer status for product. Message: "
                            + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback setProductTransferStatus transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#addProductReferences(gov.nasa.jpl.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void addProductReferences(Product product)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        String productRefTable = product.getProductType().getName()
                + "_reference";

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            for (Iterator<Reference> i = product.getProductReferences().iterator(); i
                    .hasNext();) {
                Reference r = i.next();

                String addRefSql = "INSERT INTO "
                        + productRefTable
                        + " "
                        + "(product_id, product_orig_reference, product_datastore_reference, product_reference_filesize, product_reference_mimetype) "
                        + "VALUES ("
                        + product.getProductId()
                        + ", '"
                        + r.getOrigReference()
                        + "', '"
                        + r.getDataStoreReference()
                        + "', "
                        + r.getFileSize()
                        + ",'"
                        + ((r.getMimeType() == null) ? "" : r.getMimeType()
                                .getName()) + "')";

                LOG.log(Level.FINE, "addProductReferences: Executing: "
                        + addRefSql);
                statement.execute(addRefSql);
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception adding product references. Message: "
                            + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback addProductReferences transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#getProductById(java.lang.String)
     */
    public Product getProductById(String productId) throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        Product product = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductSql = "SELECT * " + "FROM products "
                    + "WHERE product_id = " + productId;

            LOG.log(Level.FINE, "getProductById: Executing: " + getProductSql);
            rs = statement.executeQuery(getProductSql);

            while (rs.next()) {
                product = DbStructFactory.getProduct(rs, false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception getting product. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getProductById transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return product;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#getProductByName(java.lang.String)
     */
    public Product getProductByName(String productName) throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        Product product = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductSql = "SELECT products.* " + "FROM products "
                    + "WHERE product_name = '" + productName + "'";

            LOG
                    .log(Level.FINE, "getProductByName: Executing: "
                            + getProductSql);
            rs = statement.executeQuery(getProductSql);

            while (rs.next()) {
                product = DbStructFactory.getProduct(rs, false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception getting product. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getProductByName transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return product;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#getProductReferences(gov.nasa.jpl.oodt.cas.filemgr.structs.Product)
     */
    public List<Reference> getProductReferences(Product product) throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        List<Reference> references = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductRefSql = "SELECT * FROM "
                    + product.getProductType().getName() + "_reference"
                    + " WHERE product_id = " + product.getProductId();

            LOG.log(Level.FINE, "getProductReferences: Executing: "
                    + getProductRefSql);
            rs = statement.executeQuery(getProductRefSql);

            references = new Vector<Reference>();
            while (rs.next()) {
                Reference r = DbStructFactory.getReference(rs);
                references.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception getting product type. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getProductTypeById transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return references;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#getProducts()
     */
    public List<Product> getProducts() throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        List<Product> products = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductSql = "SELECT products.* " + "FROM products "
                    + "ORDER BY products.product_id DESC";

            LOG.log(Level.FINE, "getProducts: Executing: " + getProductSql);
            rs = statement.executeQuery(getProductSql);
            products = new Vector<Product>();

            while (rs.next()) {
                Product product = DbStructFactory.getProduct(rs, false);
                products.add(product);
            }

            if (products.size() == 0) {
                products = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception getting products. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getProductstransaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return products;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#getProductsByProductType(gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType)
     */
    public List<Product> getProductsByProductType(ProductType type)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        List<Product> products = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductSql = null;
            String productTypeIdStr = null;

            if (fieldIdStringFlag) {
                productTypeIdStr = "'" + type.getProductTypeId() + "'";
            } else {
                productTypeIdStr = type.getProductTypeId();
            }

            getProductSql = "SELECT products.* " + "FROM products "
                    + "WHERE products.product_type_id = " + productTypeIdStr;

            LOG.log(Level.FINE, "getProductsByProductType: Executing: "
                    + getProductSql);
            rs = statement.executeQuery(getProductSql);
            products = new Vector<Product>();

            while (rs.next()) {
                Product product = DbStructFactory.getProduct(rs, false);
                products.add(product);
            }

            if (products.size() == 0) {
                products = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception getting products. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getProductsByProductType transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return products;
    }

    public Metadata getMetadata(Product product) throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        Metadata m = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String metadataSql = "SELECT * FROM "
                    + product.getProductType().getName() + "_metadata "
                    + " WHERE product_id = " + product.getProductId();

            LOG.log(Level.FINE, "getMetadata: Executing: " + metadataSql);
            rs = statement.executeQuery(metadataSql);
            
            m = new Metadata();
            List<Element> elements = null;

            try {
                elements = validationLayer.getElements(product.getProductType());
            } catch (ValidationLayerException e) {
                e.printStackTrace();
                throw new CatalogException(
                        "ValidationLayerException when trying to obtain element list for product type: "
                                + product.getProductType().getName()
                                + ": Message: " + e.getMessage());
            }

            while (rs.next()) {
                for (Iterator<Element> i = elements.iterator(); i.hasNext();) {
                    Element e = i.next();

                    // right now, we just support STRING
                    String elemValue = rs.getString("metadata_value");
                    String elemId = rs.getString("element_id");

                    if (elemId.equals(e.getElementId())) {
                        elemValue = (elemValue != null ? elemValue : "");
                        m.addMetadata(e.getElementName(), elemValue);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception getting metadata. Message: "
                    + e.getMessage());
            throw new CatalogException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return m;
    }
    
    public Metadata getReducedMetadata(Product product, List<String> elems) throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        Metadata m = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String elementIds = "";
            if (elems.size() > 0) {
                elementIds += " AND (element_id = '" + this.validationLayer.getElementByName(elems.get(0)).getElementId() + "'";
                for (int i = 1; i < elems.size(); i++) 
                    elementIds += " OR element_id = '" + this.validationLayer.getElementByName(elems.get(i)).getElementId() + "'";
                elementIds += ")";
            }
            String metadataSql = "SELECT element_id,metadata_value FROM "
                    + product.getProductType().getName() + "_metadata"
                    + " WHERE product_id = " + product.getProductId() + elementIds;

            LOG.log(Level.FINE, "getMetadata: Executing: " + metadataSql);
            rs = statement.executeQuery(metadataSql);

            m = new Metadata();
            List<Element> elements = null;

            try {
                elements = validationLayer.getElements(product.getProductType());
            } catch (ValidationLayerException e) {
                e.printStackTrace();
                throw new CatalogException(
                        "ValidationLayerException when trying to obtain element list for product type: "
                                + product.getProductType().getName()
                                + ": Message: " + e.getMessage());
            }

            while (rs.next()) {
                for (Iterator<Element> i = elements.iterator(); i.hasNext();) {
                    Element e = i.next();

                    // right now, we just support STRING
                    String elemValue = rs.getString("metadata_value");
                    String elemId = rs.getString("element_id");

                    if (elemId.equals(e.getElementId())) {
                        elemValue = (elemValue != null ? elemValue : "");
                        m.addMetadata(e.getElementName(), elemValue);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception getting metadata. Message: "
                    + e.getMessage());
            throw new CatalogException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return m;
    }
    
    public List<Metadata> getReducedMetadata(Query query, ProductType type,
			List<String> elementNames) throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            List<String> elementIds = new Vector<String>();
            if (elementNames != null)
            	for (String elementName : elementNames)
            		elementIds.add(this.getElement(elementName, type).getElementId());
            String elementQuery = "";
            if (elementIds.size() > 0)
            	elementQuery = "(element_id = '" + StringUtils.join(elementIds.iterator(), "' OR element_id = '") + "') AND ";
            String getProductSql = "SELECT product_id,element_id,metadata_value FROM " + type.getName() + "_metadata WHERE (" + elementQuery + " product_id IN (" + this.getProductSqlQuery(query, type) + ")) ORDER BY product_id DESC ";
            LOG.log(Level.FINE, "catalog query: executing: " + getProductSql);
            rs = statement.executeQuery(getProductSql);

            HashMap<String, Metadata> metadataMap = new HashMap<String, Metadata>();
            List<Element> elements = new Vector<Element>();
            if (elementNames != null) {
            	for (String elementName : elementNames)
            		elements.add(this.getElement(elementName, type));
            }else {
            	elements.addAll(this.validationLayer.getElements(type));
            }
            while (rs.next()) {
                for (Element element : elements) {

                    // right now, we just support STRING
                	String productId = rs.getString("product_id");
                    String elemValue = rs.getString("metadata_value");
                    String elemId = rs.getString("element_id");

                    if (elemId.equals(element.getElementId())) {
                        elemValue = (elemValue != null ? elemValue : "");
                    	Metadata m = metadataMap.get(productId);
                    	if (m == null)
                    		m = new Metadata();
                        m.addMetadata(element.getElementName(), elemValue);
                        metadataMap.put(productId, m);
                    }
                }
            }
            return new Vector<Metadata>(metadataMap.values());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception performing query. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback query transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {
            try {
                rs.close();
            } catch (Exception ignore) {}

            try {
                statement.close();
            } catch (Exception ignore) {}

            try {
                conn.close();
            } catch (Exception ignore) {}
        }	
     }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#query(gov.nasa.jpl.oodt.cas.filemgr.structs.Query
     *      gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType)
     */
    public List<String> query(Query query, ProductType type) throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            
            String getProductSql = this.getProductSqlQuery(query, type) + " ORDER BY product_id DESC ";
            LOG.log(Level.FINE, "catalog query: executing: " + getProductSql);
            rs = statement.executeQuery(getProductSql);

            List<String> productIds = new Vector<String>();
            while (rs.next()) {
                String productId = rs.getString("product_id");
                productIds.add(productId);
            }
            return productIds;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception performing query. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback query transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {
            try {
                rs.close();
            } catch (Exception ignore) {}

            try {
                statement.close();
            } catch (Exception ignore) {}

            try {
                conn.close();
            } catch (Exception ignore) {}
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#getTopNProducts(int)
     */
    public List<Product> getTopNProducts(int n) throws CatalogException {
        return getTopNProducts(n, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#getTopNProducts(int,
     *      gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType)
     */
    public List<Product> getTopNProducts(int n, ProductType type)
            throws CatalogException {
        List<Product> products = null;

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();
            statement.setMaxRows(n);

            String getProductSql = "SELECT products.* " + "FROM products ";

            if (type != null && type.getProductTypeId() != null) {
                if (fieldIdStringFlag) {
                    getProductSql += "WHERE products.product_type_id = '"
                            + type.getProductTypeId() + "' ";
                } else {
                    getProductSql += "WHERE products.product_type_id = "
                            + type.getProductTypeId() + " ";
                }

            }

            getProductSql += "ORDER BY products.product_id DESC";

            LOG.log(Level.FINE, "getTopNProducts: executing: " + getProductSql);

            rs = statement.executeQuery(getProductSql);
            products = new Vector<Product>();

            while (rs.next()) {
                Product product = DbStructFactory.getProduct(rs, false);
                products.add(product);
            }

            if (products.size() == 0) {
                products = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception getting top N products. Message: "
                            + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback get top N products. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return products;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#getValidationLayer()
     */
    public ValidationLayer getValidationLayer() throws CatalogException {
        if (validationLayer == null) {
            throw new CatalogException("Validation Layer is null!");
        } else {
            return validationLayer;
        }
    }

    private synchronized void addMetadataValue(Element element,
            Product product, String value) throws CatalogException {

        Connection conn = null;
        Statement statement = null;

        String metadataTable = product.getProductType().getName() + "_metadata";

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            // build up the sql statement
            StringBuffer insertClauseSql = new StringBuffer();
            StringBuffer valueClauseSql = new StringBuffer();

            insertClauseSql.append("INSERT INTO " + metadataTable
                    + " (product_id, element_id, metadata_value) ");
            valueClauseSql.append("VALUES ");

            // now do the value clause
            if (fieldIdStringFlag) {
                valueClauseSql.append("(" + product.getProductId() + ", '"
                        + element.getElementId() + "', '" + value + "')");
            } else {
                valueClauseSql.append("(" + product.getProductId() + ", "
                        + element.getElementId() + ", '" + value + "')");
            }

            String metaIngestSql = insertClauseSql.toString()
                    + valueClauseSql.toString();
            LOG
                    .log(Level.FINE, "addMetadataValue: Executing: "
                            + metaIngestSql);
            statement.execute(metaIngestSql);
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception adding metadata value. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback add metadata value. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }
    }

    private synchronized void removeMetadataValue(Element element,
            Product product, String value) throws CatalogException {

        Connection conn = null;
        Statement statement = null;

        String metadataTable = product.getProductType().getName() + "_metadata";

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            // build up the sql statement
            String metRemoveSql = "DELETE FROM " + metadataTable + " WHERE ";
            if (fieldIdStringFlag) {
                metRemoveSql += "PRODUCT_ID = '" + product.getProductId()
                        + "' AND ";
                metRemoveSql += "ELEMENT_ID = '" + element.getElementId()
                        + "' AND ";
                metRemoveSql += "METADATA_VALUE = '" + value + "'";
            } else {
                metRemoveSql += "PRODUCT_ID = " + product.getProductId()
                        + " AND ";
                metRemoveSql += "ELEMENT_ID = " + element.getElementId()
                        + " AND ";
                metRemoveSql += "METADATA_VALUE = " + value;
            }

            LOG.log(Level.FINE, "removeMetadataValue: Executing: "
                    + metRemoveSql);
            statement.execute(metRemoveSql);
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception removing metadata value. Message: "
                            + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback remove metadata value. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#getNumProducts(gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType)
     */
    public int getNumProducts(ProductType type) throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        int numProducts = -1;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String getProductSql = "SELECT COUNT(products.product_id) AS numProducts "
                    + "FROM products ";

            if (fieldIdStringFlag) {
                getProductSql += "WHERE products.product_type_id = '"
                        + type.getProductTypeId() + "' ";
            } else {
                getProductSql += "WHERE products.product_type_id = "
                        + type.getProductTypeId() + " ";
            }

            LOG.log(Level.FINE, "getNumProducts: executing: " + getProductSql);

            rs = statement.executeQuery(getProductSql);

            while (rs.next()) {
                numProducts = rs.getInt("numProducts");
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception getting num products. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback get num products. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

        return numProducts;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.util.Pagination#getFirstPage(gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType)
     */
    public ProductPage getFirstPage(ProductType type) {
        Query query = new Query();
        ProductPage firstPage = null;

        try {
            firstPage = pagedQuery(query, type, 1);
        } catch (CatalogException e) {
            LOG.log(Level.WARNING, "Exception getting first page: Message: "
                    + e.getMessage());
        }
        return firstPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.util.Pagination#getLastProductPage(gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType)
     */
    public ProductPage getLastProductPage(ProductType type) {
        ProductPage lastPage = null;
        ProductPage firstPage = getFirstPage(type);
        Query query = new Query();
        try {
            lastPage = pagedQuery(query, type, firstPage.getTotalPages());
        } catch (CatalogException e) {
            LOG.log(Level.WARNING, "Exception getting last page: Message: "
                    + e.getMessage());
        }

        return lastPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.util.Pagination#getNextPage(gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType,
     *      gov.nasa.jpl.oodt.cas.filemgr.structs.ProductPage)
     */
    public ProductPage getNextPage(ProductType type, ProductPage currentPage) {
        if (currentPage == null) {
            return getFirstPage(type);
        }

        if (currentPage.isLastPage()) {
            return currentPage;
        }

        ProductPage nextPage = null;
        Query query = new Query();

        try {
            nextPage = pagedQuery(query, type, currentPage.getPageNum() + 1);
        } catch (CatalogException e) {
            LOG.log(Level.WARNING, "Exception getting next page: Message: "
                    + e.getMessage());
        }

        return nextPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.util.Pagination#getPrevPage(gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType,
     *      gov.nasa.jpl.oodt.cas.filemgr.structs.ProductPage)
     */
    public ProductPage getPrevPage(ProductType type, ProductPage currentPage) {
        if (currentPage == null) {
            return getFirstPage(type);
        }

        if (currentPage.isFirstPage()) {
            return currentPage;
        }
        ProductPage prevPage = null;
        Query query = new Query();

        try {
            prevPage = pagedQuery(query, type, currentPage.getPageNum() - 1);
        } catch (CatalogException e) {
            LOG.log(Level.WARNING, "Exception getting prev page: Message: "
                    + e.getMessage());
        }

        return prevPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog#pagedQuery(gov.nasa.jpl.oodt.cas.filemgr.structs.Query,
     *      gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType, int)
     */
    public ProductPage pagedQuery(Query query, ProductType type, int pageNum)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            
            String getProductSql = this.getProductSqlQuery(query, type) + " ORDER BY product_id DESC ";
            LOG.log(Level.FINE, "catalog query: executing: " + getProductSql);
            rs = statement.executeQuery(getProductSql);
            
            int rsSize = -1;
            if (rs.last())
            	rsSize = rs.getRow();
            else 
            	return ProductPage.blankPage();
            
            List<String> productIds = new Vector<String>();
            if (pageNum > 0) {
            	int pageLoc = ((pageNum - 1) * pageSize) + 1;
                if (pageLoc > rsSize)
                	rs.absolute(pageNum = 1);
                else 
                    rs.absolute(pageLoc);

                // grab the rest
                int numGrabbed = 0;
                do {
                    String productId = rs.getString("product_id");
                    productIds.add(productId);
                    numGrabbed++;
                }while (numGrabbed < pageSize && rs.next());

            } else {
                while (rs.next()) {
                    String productId = rs.getString("product_id");
                    productIds.add(productId);
                }
            }
            
            ProductPage retPage = new ProductPage();
            retPage.setPageNum(pageNum);
            retPage.setPageSize(this.pageSize);
//            retPage.setTotalPages((int) Math.ceil((double) rsSize / (double) this.pageSize));
            retPage.setNumOfHits(rsSize);
            
            List<Product> products = new Vector<Product>(productIds.size());
            for (Iterator<String> i = productIds.iterator(); i.hasNext();) 
                products.add(getProductById(i.next()));
            retPage.setPageProducts(products);
            
            return retPage;
            
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Exception performing query. Message: "
                    + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback query transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {
            try {
                rs.close();
            } catch (Exception ignore) {}

            try {
                statement.close();
            } catch (Exception ignore) {}

            try {
                conn.close();
            } catch (Exception ignore) {}
        }
    }
    
    
    protected String getProductSqlQuery(Query query, ProductType type) throws CatalogException, ValidationLayerException, QueryFormulationException {
        String getProductSql = null;
        if (query.getCriteria().size() == 0) {
            getProductSql = "SELECT DISTINCT product_id FROM " + type.getName() + "_metadata";
        }else if (query.getCriteria().size() == 1) {
            getProductSql = this.getSqlQuery(query.getCriteria().get(0), type);
        }else {
            getProductSql = this.getSqlQuery(new BooleanQueryCriteria(query.getCriteria(), BooleanQueryCriteria.AND), type);
        }
        return getProductSql;
    }

    private boolean stillFresh(String productTypeId) {
        Date currentTime = new Date();

        if (PRODUCT_CACHE.get(productTypeId) == null) {
            return false;
        } else {
            TreeMap<String, Object> productListAndUpdateTime = PRODUCT_CACHE
                    .get(productTypeId);
            String lastUpdateTime = (String) productListAndUpdateTime
                    .get("lastUpdateTime");
            Date lastUpdateTimeDate = null;

            try {
                lastUpdateTimeDate = DateConvert.isoParse(lastUpdateTime);

                long timeDifferenceMilis = currentTime.getTime()
                        - lastUpdateTimeDate.getTime();
                long timeDifferenceSeconds = timeDifferenceMilis * 1000;
                long timeDifferenceMinutes = timeDifferenceSeconds / 60;

                if (timeDifferenceMinutes >= cacheUpdateMinutes) {
                    return false;
                } else {
                    return true;
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING,
                        "Unable to parse last update time for product type: ["
                                + productTypeId + "]: Message: "
                                + e.getMessage());
                return false;
            }
        }

    }

    private List<Product> getProductsFromCache(String productTypeId) {
        List<Product> products = null;

        if (PRODUCT_CACHE.get(productTypeId) == null) {
            return null;
        } else {
            TreeMap<String, Object> productListAndUpdateTime = PRODUCT_CACHE
                    .get(productTypeId);
            products = (List<Product>) productListAndUpdateTime.get("productList");
        }

        return products;
    }

    private void flagCacheUpdate(String productTypeId, List<Product> products) {
        Date currentDateTime = new Date();
        String isoDateStr = DateConvert.isoFormat(currentDateTime);
        TreeMap<String, Object> productListAndUpdateTime = new TreeMap<String, Object>();
        productListAndUpdateTime.put("productList", products);
        productListAndUpdateTime.put("lastUpdateTime", isoDateStr);
        PRODUCT_CACHE.put(productTypeId, productListAndUpdateTime);
    }

    private List<Product> getProductsByProductTypeCached(ProductType type) {
        List<Product> products = null;
        // check the product cache first
        if (stillFresh(type.getProductTypeId())) {
            products = getProductsFromCache(type.getProductTypeId());
        } else {
            // go get a fresh set
            try {
                products = getProductsByProductType(type);
                flagCacheUpdate(type.getProductTypeId(), products);
            } catch (CatalogException e) {
                LOG.log(Level.WARNING,
                        "CatalogException getting cached products for type: ["
                                + type.getProductTypeId() + "]: Message: "
                                + e.getMessage());
                return products;
            }
        }

        return products;
    }
    
    private String getSqlQuery(QueryCriteria queryCriteria, ProductType type) throws ValidationLayerException, CatalogException {
        String sqlQuery = null;
        if (queryCriteria instanceof BooleanQueryCriteria) {
            BooleanQueryCriteria bqc = (BooleanQueryCriteria) queryCriteria;
            if (bqc.getOperator() == BooleanQueryCriteria.NOT) {
                sqlQuery = "SELECT DISTINCT product_id FROM " + type.getName() + "_metadata WHERE product_id NOT IN (" + this.getSqlQuery(bqc.getTerms().get(0), type) + ")";
            }else {
                sqlQuery = "(" + this.getSqlQuery(bqc.getTerms().get(0), type);
                String op = bqc.getOperator() == BooleanQueryCriteria.AND ? "INTERSECT" : "UNION";
                for (int i = 1; i < bqc.getTerms().size(); i++) 
                    sqlQuery += ") " + op + " (" + this.getSqlQuery(bqc.getTerms().get(i), type);
                sqlQuery += ")";
            }
        }else {
            String elementIdStr = this.getElement(queryCriteria.getElementName(), type).getElementId();
            if (fieldIdStringFlag) 
                elementIdStr = "'" + elementIdStr + "'";
            sqlQuery = "SELECT DISTINCT product_id FROM " + type.getName() + "_metadata WHERE element_id = " + elementIdStr + " AND ";
            if (queryCriteria instanceof TermQueryCriteria) {
                sqlQuery += "metadata_value = '" + ((TermQueryCriteria) queryCriteria).getValue() + "'";
            } else if (queryCriteria instanceof RangeQueryCriteria) {
                RangeQueryCriteria rqc = (RangeQueryCriteria) queryCriteria;
                String rangeSubQuery = null;
                if (rqc.getStartValue() != null)
                    rangeSubQuery = "metadata_value" + (rqc.getInclusive() ? " >= " : " > ") + "'" + rqc.getStartValue() + "'";
                if (rqc.getEndValue() != null) {
                    if (rangeSubQuery == null)
                        rangeSubQuery = "metadata_value" + (rqc.getInclusive() ? " <= " : " < ") + "'" + rqc.getEndValue() + "'";
                    else
                        rangeSubQuery = "(" + rangeSubQuery + " AND metadata_value" + (rqc.getInclusive() ? " <= " : " < ") + "'" + rqc.getEndValue() + "')";
                }
                sqlQuery += rangeSubQuery;
            } else {
                throw new CatalogException("Invalid QueryCriteria [" + queryCriteria.getClass().getCanonicalName() + "]");
            }
        }

        return sqlQuery;
    }
    
    private Element getElement(String elementName, ProductType productType) throws ValidationLayerException {
    	for (Element element : this.validationLayer.getElements(productType)) 
    		if (element.getElementName().equals(elementName))
    			return element;
    	return null;
    }

    private synchronized void updateReferences(Product product)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        String productRefTable = product.getProductType().getName()
                + "_reference";

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            // first remove the refs
            String deleteProductSql = "DELETE FROM "
                    + product.getProductType().getName() + "_reference "
                    + " WHERE product_id = " + product.getProductId();
            LOG.log(Level.FINE, "updateProductReferences: Executing: "
                    + deleteProductSql);
            statement.execute(deleteProductSql);

            // now add the new ones back in
            for (Iterator<Reference> i = product.getProductReferences().iterator(); i
                    .hasNext();) {
                Reference r = i.next();

                String addRefSql = "INSERT INTO "
                        + productRefTable
                        + " "
                        + "(product_id, product_orig_reference, product_datastore_reference, product_reference_filesize,"
                        + "product_reference_mimetype) " + "VALUES ("
                        + product.getProductId() + ", '" + r.getOrigReference()
                        + "', '" + r.getDataStoreReference() + "', "
                        + r.getFileSize() + ",'" + r.getMimeType().getName()
                        + "')";

                LOG.log(Level.FINE, "updateProductReferences: Executing: "
                        + addRefSql);
                statement.execute(addRefSql);
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception updating product references. Message: "
                            + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback updateProductReferences transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage());
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

                conn = null;
            }
        }

    }

}
