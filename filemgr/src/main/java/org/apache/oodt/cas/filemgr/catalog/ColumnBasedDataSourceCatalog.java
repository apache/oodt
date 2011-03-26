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
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.util.SqlParser;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.commons.date.DateUtils;

//JDK imports
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

//Apache imports
import org.apache.commons.lang.StringUtils;

/**
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Implementation of a {@link Catalog} that is backed by a {@link DataSource}
 * front-end to a SQL DBMS Column-based table structure.
 * </p>
 * 
 */
public class ColumnBasedDataSourceCatalog extends AbstractCatalog {

    /* our sql data source */
    protected DataSource dataSource = null;

    /* our log stream */
    private static Logger LOG = Logger.getLogger(ColumnBasedDataSourceCatalog.class.getName());

    /* our validation layer */
    private ValidationLayer validationLayer = null;

    private Set<String> dbVectorElements;
    private Set<String> dbIntegerTypes;
    
    /* size of pages of products within the catalog */
    protected int pageSize = -1;
    
    /**
     * <p>
     * Default Constructor
     * </p>.
     * @throws  
     */
    public ColumnBasedDataSourceCatalog(DataSource ds, ValidationLayer valLayer, int pageSize, Set<String> dbIntegerTypes, Set<String> dbVectorElements) {
    	this.dataSource = ds;
        this.validationLayer = valLayer;
        this.pageSize = pageSize;
        this.dbIntegerTypes = dbIntegerTypes;
        this.dbVectorElements = dbVectorElements;
        
        Connection conn = null;
        Statement statement = null;
        try {
        	conn = ds.getConnection();
        	statement = conn.createStatement();
        	statement.execute("alter session set NLS_DATE_FORMAT = 'YYYY-MM-DD'");
        	statement.execute("alter session set NLS_TIME_FORMAT = 'HH24:MI:SS.FF3'");
        	statement.execute("alter session set NLS_TIMESTAMP_FORMAT = 'YYYY-MM-DD\"T\"HH24:MI:SS.FF3'");
        	statement.execute("alter session set NLS_TIME_TZ_FORMAT = 'HH24:MI:SS.FF3TZH:TZM'");
        	statement.execute("alter session set NLS_TIMESTAMP_FORMAT='YYYY-MM-DD\"T\"HH24:MI:SS.FF3\"Z\"'");
        	statement.execute("alter session set NLS_TIMESTAMP_TZ_FORMAT='YYYY-MM-DD\"T\"HH24:MI:SS.FF3TZH:TZM'");
        }catch (Exception e) {
        }finally {
        	try { conn.close(); }catch (Exception e) {}
        	try { statement.close(); }catch (Exception e) {}
        }
    }
    
    public int getPageSize() {
    	return this.pageSize;
    }
    
    protected boolean isVector(Element element) {
    	return this.dbVectorElements.contains(element.getElementId());
    }
    
    protected boolean isString(Element element, ProductType productType) throws Exception {
        Connection conn = null;
    	ResultSet tables = null;
    	ResultSet columns = null;

        try {
        	conn = this.dataSource.getConnection();
        	DatabaseMetaData metaData = conn.getMetaData();
        	
        	if (this.isVector(element))
        		tables = metaData.getTables(null, null, element.getElementName(), new String[] { "TABLE" });
        	else 
        		tables = metaData.getTables(null, null, productType.getName() + "_metadata", new String[] { "TABLE" });
        	
	    	if (tables.next()) {
	    		columns = metaData.getColumns(null, null, tables.getString("TABLE_NAME"), null);
	        	while (columns.next())
	        		if (columns.getString("COLUMN_NAME").equals(element.getElementName()))
	        			return !dbIntegerTypes.contains(columns.getString("TYPE_NAME").toLowerCase());
	    	}
	    	throw new Exception("Failed to determine type for element '" + element.getElementName() + "'");
    	}catch (Exception e) {
    		throw new Exception("Failed to determine if element is string for element '" + element.getElementName() + "' : " + e.getMessage(),e);
    	}finally {
    		try { conn.close(); } catch (Exception e) {}
    		try { tables.close(); } catch (Exception e) {}
    		try { columns.close(); } catch (Exception e) {}
    	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#addMetadata(org.apache.oodt.cas.metadata.Metadata,
     *      org.apache.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void addMetadata(Metadata metadata, Product product)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            List<String> scalarElementNames = new Vector<String>();
            List<String> scalarElementValues = new Vector<String>();
            
            List<Element> elements = this.validationLayer.getElements(product.getProductType());
            for (Element element : elements) {
            	if (metadata.getMetadata(element.getElementName()) != null) {
	            	if (!this.isVector(element)) {
	            		scalarElementNames.add(element.getElementName());
	            		scalarElementValues.add(metadata.getMetadata(element.getElementName()));
	            	}else {
	            		for (String value : metadata.getAllMetadata(element.getElementName())) {
	            			String sqlInsert = String.format("INSERT INTO %s (ProductId, %s) VALUES (%s , %s)", 
	            					element.getElementName(), 
	            					element.getElementName(), 
	            					product.getProductId(),
	            					(this.isString(element, product.getProductType()) ? "'" + value + "'" : value));
	            			statement.execute(sqlInsert);
	            		}
	            	}
            	}
            }

            String scalarInsert = "INSERT INTO " + product.getProductType().getName() + "_metadata" 
            	+ "(" + StringUtils.join(scalarElementNames, ",") + ")"
            	+ " VALUES (" + StringUtils.join(scalarElementValues, ",") + ")";
            statement.execute(scalarInsert);

            conn.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception adding product metadata. Message: "
                    + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (Exception e2) {
                LOG.log(Level.WARNING,
                        "Unable to rollback addMetadata transaction. Message: "
                                + e2.getMessage(), e2);
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {
            try { statement.close(); } catch (Exception ignore) {}
            try { conn.close(); } catch (Exception ignore) {}
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#addMetadata(org.apache.oodt.cas.metadata.Metadata,
     *      org.apache.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void removeMetadata(Metadata m, Product product)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();
            
            String removeSql = "DELETE FROM " + product.getProductType().getName() + "_vw WHERE ProductId = " + product.getProductId();
            statement.execute(removeSql);
            
            conn.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception removing product metadata. Message: "
                    + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (Exception e2) {
                LOG.log(Level.WARNING,
                        "Unable to rollback removeMetadata transaction. Message: "
                                + e2.getMessage(), e2);
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {
            try { statement.close(); } catch (Exception ignore) {}
            try { conn.close(); } catch (Exception ignore) {}
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#addProduct(org.apache.oodt.cas.filemgr.structs.Product)
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

            String insertSql = "INSERT INTO products (ProductName, ProductStructure, ProductTransferStatus, ProductType, ProductRecievedTime) "
                    + "VALUES ('"
                    + product.getProductName()
                    + "', '"
                    + product.getProductStructure()
                    + "', '"
                    + product.getTransferStatus()
                    + "', "
                    + product.getProductType().getName()
                    + "', "
                    + DateUtils.toString(DateUtils.getCurrentLocalTime())
                    + ")";

            statement.execute(insertSql);

            String productId = new String();

            String getProductIdSql = "SELECT MAX(product_id) AS max_id FROM products";

            rs = statement.executeQuery(getProductIdSql);

            while (rs.next()) {
                productId = String.valueOf(rs.getInt("max_id"));
            }

            product.setProductId(productId);
            conn.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception adding product. Message: "
                    + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (Exception e2) {
                LOG.log(Level.WARNING,
                        "Unable to rollback addProduct transaction. Message: "
                                + e2.getMessage(), e2);
            }
            throw new CatalogException("Failed to add product " + product.getProductId() + " : " + e.getMessage(), e);
        } finally {
        	try { rs.close(); } catch (Exception ignore) {}
        	try { statement.close(); } catch (Exception ignore) {}
        	try { conn.close(); } catch (Exception ignore) {}
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#modifyProduct(org.apache.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void modifyProduct(Product product)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String modifyProductSql = "UPDATE products SET "
            		+ "ProductName = '" + product.getProductName() 
            		+ "'," 
            		+ "ProductStructure = '"
                    + product.getProductStructure()
                    + "',"
                    + "ProductTransferStatus = '"
                    + product.getTransferStatus() 
                    + "' WHERE ProductId = " + product.getProductId();

            LOG.log(Level.FINE, "modifyProduct: Executing: " + modifyProductSql);
            statement.execute(modifyProductSql);
            conn.commit();

            // now update the refs
            updateReferences(product);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception modifying product. Message: "
                    + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (Exception e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback modifyProduct transaction. Message: "
                                + e2.getMessage(), e);
            }
            throw new CatalogException("Failed to modify product '" + product.getProductId() + "' : " + e.getMessage(), e);
        } finally {
        	try { statement.close(); } catch (Exception ignore) {}
        	try { conn.close(); } catch (Exception ignore) {}
        }

    }

	private synchronized void updateReferences(Product product)
			throws CatalogException {
		Connection conn = null;
		Statement statement = null;

		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			statement = conn.createStatement();

			// first remove the refs
			String deleteProductSql = "DELETE FROM "
					+ product.getProductType().getName() + "_reference"
					+ " WHERE ProductId = " + product.getProductId();
			LOG.log(Level.FINE, "updateProductReferences: Executing: "
					+ deleteProductSql);
			statement.execute(deleteProductSql);

			// now add the new ones back in
			for (Reference reference : product.getProductReferences()) {

				String addRefSql = "INSERT INTO "
						+ product.getProductType().getName() + "_reference"
						+ " "
						+ "(ProductId, OriginalReference, DataStoreReference, FileSize, MimeType) " 
						+ "VALUES ("
						+ product.getProductId() 
						+ ", '" 
						+ reference.getOrigReference()
						+ "', '" 
						+ reference.getDataStoreReference() 
						+ "', "
						+ reference.getFileSize() 
						+ ",'" 
                        + ((reference.getMimeType() == null) ? "" : reference.getMimeType()
                                .getName()) + "')";					

				LOG.log(Level.FINE, "updateProductReferences: Executing: "
						+ addRefSql);
				statement.execute(addRefSql);
			}

			conn.commit();

		} catch (Exception e) {
			LOG.log(Level.SEVERE,
					"Exception updating product references. Message: "
							+ e.getMessage(), e);
			try {
				conn.rollback();
			} catch (SQLException e2) {
				LOG.log(Level.WARNING,
						"Unable to rollback updateProductReferences transaction. Message: "
								+ e2.getMessage(), e2);
			}
			throw new CatalogException(e.getMessage());
		} finally {
			try { statement.close(); } catch (Exception ignore) {}
			try { conn.close(); } catch (Exception ignore) {}
		}

	}

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#removeProduct(org.apache.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void removeProduct(Product product)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String deleteProductSql = "DELETE FROM products WHERE ProductId = "
                    + product.getProductId();

            LOG.log(Level.FINE, "removeProduct: Executing: " + deleteProductSql);
            statement.execute(deleteProductSql);
//            deleteProductSql = "DELETE FROM "
//                    + product.getProductType().getName() + "_metadata "
//                    + " WHERE product_id = " + product.getProductId();
//            LOG.log(Level.FINE, "removeProduct: Executing: "
//                            + deleteProductSql);
//            statement.execute(deleteProductSql);
//            deleteProductSql = "DELETE FROM "
//                    + product.getProductType().getName() + "_reference "
//                    + " WHERE product_id = " + product.getProductId();
//            LOG
//                    .log(Level.FINE, "removeProduct: Executing: "
//                            + deleteProductSql);
//            statement.execute(deleteProductSql);
            conn.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception removing product. Message: "
                    + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (Exception e2) {
                LOG.log(Level.WARNING,
                        "Unable to rollback removeProduct transaction. Message: "
                                + e2.getMessage(), e2);
            }
            throw new CatalogException("Failec to remove product '" + product.getProductId() + "' : " + e.getMessage(), e);
        } finally {
        	try { statement.close(); } catch (Exception ignore) {}
        	try { conn.close(); } catch (Exception ignore) {}
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#setProductTransferStatus(org.apache.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void setProductTransferStatus(Product product)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String modifyProductSql = "UPDATE products SET ProductTransferStatus = '"
                    + product.getTransferStatus()
                    + "' "
                    + "WHERE ProductId = " + product.getProductId();

            LOG.log(Level.FINE, "setProductTransferStatus: Executing: "
                    + modifyProductSql);
            statement.execute(modifyProductSql);
            conn.commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "Exception setting transfer status for product. Message: "
                            + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (Exception e2) {
                LOG.log(Level.WARNING,
                        "Unable to rollback setProductTransferStatus transaction. Message: "
                                + e2.getMessage(), e2);
            }
            throw new CatalogException("Failed to set ProductTransferStatus for product '" + product.getProductId() + "' : " + e.getMessage(), e);
        } finally {
        	try { statement.close(); } catch (Exception ignore) {}
        	try { conn.close(); } catch (Exception ignore) {}
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#addProductReferences(org.apache.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void addProductReferences(Product product)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            for (Reference reference : product.getProductReferences()) {

                String addRefSql = "INSERT INTO "
                        + product.getProductType().getName() + "_reference"
                        + " "
                        + "(ProductId, OriginalReference, DataStoreReference, FileSize, MimeType) "
                        + "VALUES ("
                        + product.getProductId()
                        + ", '"
                        + reference.getOrigReference()
                        + "', '"
                        + reference.getDataStoreReference()
                        + "', "
                        + reference.getFileSize()
                        + ",'"
                        + ((reference.getMimeType() == null) ? "" : reference.getMimeType()
                                .getName()) + "')";

                LOG.log(Level.FINE, "addProductReferences: Executing: " + addRefSql);
                statement.execute(addRefSql);
            }

            conn.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "Exception adding product references. Message: "
                            + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (Exception e2) {
                LOG.log(Level.WARNING,
                        "Unable to rollback addProductReferences transaction. Message: "
                                + e2.getMessage(), e2);
            }
            throw new CatalogException("Failed to add product references for product '" + product.getProductId() + "' : " + e.getMessage(), e);
        } finally {
        	try { statement.close(); } catch (Exception ignore) {}
        	try { conn.close(); } catch (Exception ignore) {}
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getProductById(java.lang.String)
     */
    public Product getProductById(String productId) throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductSql = "SELECT * " + "FROM products "
                    + "WHERE ProductId = " + productId;

            LOG.log(Level.FINE, "getProductById: Executing: " + getProductSql);
            rs = statement.executeQuery(getProductSql);

            if (rs.next()) {
            	Product product = new Product();
            	product.setProductId(rs.getString("ProductId"));
            	product.setProductName(rs.getString("ProductName"));
            	product.setProductStructure(rs.getString("ProductStructure"));
            	ProductType productType = new ProductType();
            	productType.setName(rs.getString("ProductType"));
            	product.setProductType(productType);
            	product.setTransferStatus(rs.getString("ProductTransferStatus"));
                return product;
            }else {
            	throw new Exception("Failed to load product for product id = '" + productId + "'");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception getting product. Message: "
                    + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (Exception e2) {
                LOG.log(Level.WARNING,
                        "Unable to rollback getProductById transaction. Message: "
                                + e2.getMessage(), e2);
            }
            throw new CatalogException("Failed to get product by id for product '" + productId + "' : " + e.getMessage(), e);
        } finally {
        	try { rs.close(); } catch (Exception ignore) {}
        	try { statement.close(); } catch (Exception ignore) {}
        	try { conn.close(); } catch (Exception ignore) {}
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getProductByName(java.lang.String)
     */
    public Product getProductByName(String productName) throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductSql = "SELECT * " + "FROM products "
                    + "WHERE ProductName = '" + productName + "'";

            LOG.log(Level.FINE, "getProductByName: Executing: "
                            + getProductSql);
            rs = statement.executeQuery(getProductSql);

            if (rs.next()) {
            	Product product = new Product();
            	product.setProductId(rs.getString("ProductId"));
            	product.setProductName(rs.getString("ProductName"));
            	product.setProductStructure(rs.getString("ProductStructure"));
            	ProductType productType = new ProductType();
            	productType.setName(rs.getString("ProductType"));
            	product.setProductType(productType);
            	product.setTransferStatus(rs.getString("TransferStatus"));
                return product;
            }else {
            	throw new Exception("Failed to load product by name '" + productName + "'");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception getting product. Message: "
                    + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (Exception e2) {
                LOG.log(Level.WARNING,
                        "Unable to rollback getProductByName transaction. Message: "
                                + e2.getMessage(), e2);
            }
            throw new CatalogException("Failed to get product by name for product '" + productName + "' : " + e.getMessage(), e);
        } finally {
        	try { rs.close(); } catch (Exception ignore) {}
        	try { statement.close(); } catch (Exception ignore) {}
        	try { conn.close(); } catch (Exception ignore) {}
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getProductReferences(org.apache.oodt.cas.filemgr.structs.Product)
     */
    public List<Reference> getProductReferences(Product product) throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductRefSql = "SELECT * FROM "
                    + product.getProductType().getName() + "_reference"
                    + " WHERE ProductId = " + product.getProductId();

            LOG.log(Level.FINE, "getProductReferences: Executing: "
                    + getProductRefSql);
            rs = statement.executeQuery(getProductRefSql);

            Vector<Reference> references = new Vector<Reference>();
            while (rs.next()) {
            	Reference reference = new Reference();
            	reference.setOrigReference(rs.getString("OriginalReference"));
            	reference.setDataStoreReference(rs.getString("DataStoreReference"));
            	reference.setFileSize(rs.getInt("FileSize"));
            	reference.setMimeType(rs.getString("MimeType"));
                references.add(reference);
            }
            
            return references;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception getting product type. Message: "
                    + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (Exception e2) {
                LOG.log(Level.WARNING,
                        "Unable to rollback getProductByReferences transaction. Message: "
                                + e2.getMessage(), e2);
            }
            throw new CatalogException("Failed to get product by references for product '" + product.getProductId() + "' : " + e.getMessage(), e);
        } finally {
        	try { rs.close(); } catch (Exception ignore) {}
        	try { statement.close(); } catch (Exception ignore) {}
        	try { conn.close(); } catch (Exception ignore) {}
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getProducts()
     */
    public List<Product> getProducts() throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        List<Product> products = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductSql = "SELECT * FROM products ORDER BY ProductId DESC";

            LOG.log(Level.FINE, "getProducts: Executing: " + getProductSql);
            rs = statement.executeQuery(getProductSql);
            products = new Vector<Product>();

            while (rs.next()) {
            	Product product = new Product();
            	product.setProductId(rs.getString("ProductId"));
            	product.setProductName(rs.getString("ProductName"));
            	product.setProductStructure(rs.getString("ProductStructure"));
            	ProductType productType = new ProductType();
            	productType.setName(rs.getString("ProductType"));
            	product.setProductType(productType);
            	product.setTransferStatus(rs.getString("TransferStatus"));
                products.add(product);
            }

            return (products.size() > 0) ? products : null;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception getting products. Message: "
                    + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (Exception e2) {
                LOG.log(Level.WARNING,
                        "Unable to rollback getProducts transaction. Message: "
                                + e2.getMessage(), e2);
            }
            throw new CatalogException("Failed to get products : " + e.getMessage(), e);
        } finally {
        	try { rs.close(); } catch (Exception ignore) {}
        	try { statement.close(); } catch (Exception ignore) {}
        	try { conn.close(); } catch (Exception ignore) {}
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getProductsByProductType(org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    public List<Product> getProductsByProductType(ProductType type)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductSql = "SELECT * FROM products WHERE ProductType = '" + type.getName() + "'";

            LOG.log(Level.FINE, "getProductsByProductType: Executing: "
                    + getProductSql);
            rs = statement.executeQuery(getProductSql);
            
            Vector<Product> products = new Vector<Product>();
            while (rs.next()) {
            	Product product = new Product();
            	product.setProductId(rs.getString("ProductId"));
            	product.setProductName(rs.getString("ProductName"));
            	product.setProductStructure(rs.getString("ProductStructure"));
            	ProductType productType = new ProductType();
            	productType.setName(rs.getString("ProductType"));
            	product.setProductType(productType);
            	product.setTransferStatus(rs.getString("TransferStatus"));
                products.add(product);
            }

            return (products.size() > 0) ? products : null;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception getting products. Message: "
                    + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.WARNING,
                        "Unable to rollback getProductsByProductType transaction. Message: "
                                + e2.getMessage(), e2);
            }
            throw new CatalogException("Failed to get products for product type '" + type.getName() + "' : " + e.getMessage(), e);
        } finally {
        	try { rs.close(); } catch (Exception ignore) {}
        	try { statement.close(); } catch (Exception ignore) {}
        	try { conn.close(); } catch (Exception ignore) {}
        }
    }

    public Metadata getMetadata(Product product) throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            System.out.println(product.getProductType());
            String metadataSql = "SELECT * FROM "
                    + product.getProductType().getName() + "_vw "
                    + " WHERE ProductId = " + product.getProductId();

            LOG.log(Level.FINE, "getMetadata: Executing: " + metadataSql);
            rs = statement.executeQuery(metadataSql);
            
            Metadata metadata = new Metadata();
            List<Element> elements = this.validationLayer.getElements(product.getProductType());
            if (rs.next()) { 
                for (Element element : elements) {
                	try {
                		String value =  rs.getString(element.getElementName());
                		if (value == null)
                			throw new Exception("value null");
                		metadata.addMetadata(element.getElementName(), value);
                	}catch (Exception e) {
                        LOG.log(Level.WARNING, "Element '" + element.getElementName() + "' not found : " + e.getMessage());
                	}
                }
            }
            return metadata;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception getting metadata. Message: "
                    + e.getMessage(), e);
            throw new CatalogException("Failed to get metadata for product '" + product.getProductId() + "' : " + e.getMessage(), e);
        } finally {
        	try { rs.close(); } catch (Exception ignore) {}
        	try { statement.close(); } catch (SQLException ignore) {}
        	try { conn.close(); } catch (SQLException ignore) {}
        }
    }
    
    public Metadata getReducedMetadata(Product product, List<String> elems) throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String metadataSql = "SELECT * FROM "
                    + product.getProductType().getName() + "_vw "
                    + " WHERE ProductId = " + product.getProductId();

            LOG.log(Level.FINE, "getMetadata: Executing: " + metadataSql);
            rs = statement.executeQuery(metadataSql);
            
            Metadata metadata = new Metadata();
            List<Element> elements = this.validationLayer.getElements(product.getProductType());
            if (rs.next()) 
                for (Element element : elements) 
                	if (elems.contains(element.getElementName()))
                		metadata.addMetadata(element.getElementName(), rs.getString(element.getElementName()));
            
            return metadata;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception getting metadata. Message: "
                    + e.getMessage(), e);
            throw new CatalogException("Failed to get metadata for product '" + product.getProductId() + "' : " + e.getMessage(), e);
        } finally {
        	try { rs.close(); } catch (Exception ignore) {}
        	try { statement.close(); } catch (SQLException ignore) {}
        	try { conn.close(); } catch (SQLException ignore) {}
        }
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
            
        	String getProductSql = "SELECT * FROM " + type.getName();
        	if (query.getCriteria() != null)
        		getProductSql += " WHERE " + SqlParser.getInfixCriteriaString(query.getCriteria()).replaceAll("==", "=");;

            rs = statement.executeQuery(getProductSql);

            Vector<Metadata> metadatas = new Vector<Metadata>();
            List<Element> elements = this.validationLayer.getElements(type);
            while (rs.next()) {
                Metadata metadata = new Metadata();
                for (Element element : elements) 
                	if (elementNames.contains(element.getElementName()))
                		metadata.addMetadata(element.getElementName(), rs.getString(element.getElementName()));
                metadatas.add(metadata);
            }
            
            return metadatas;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception performing query. Message: "
                    + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (Exception e2) {
                LOG.log(Level.WARNING,
                        "Unable to rollback query transaction. Message: "
                                + e2.getMessage(), e2);
            }
            throw new CatalogException("Failed to query for reduced metadata : " + e.getMessage(), e);
        } finally {
            try { rs.close(); } catch (Exception ignore) {}
            try { statement.close(); } catch (Exception ignore) {}
            try { conn.close(); } catch (Exception ignore) {}
        }	
     }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#query(org.apache.oodt.cas.filemgr.structs.Query
     *      org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    public List<String> query(Query query, ProductType type) throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            
        	String getProductSql = "SELECT ProductId FROM " + type.getName() + "_vw";
        	if (query.getCriteria() != null && query.getCriteria().size() > 0)
        		getProductSql += " WHERE " + SqlParser.getInfixCriteriaString(query.getCriteria()).replaceAll("==", "=");

        	LOG.log(Level.FINE, "performing getProductSql '" + getProductSql + "'");
            rs = statement.executeQuery(getProductSql);

            Vector<String> productIds = new Vector<String>();
            while (rs.next()) 
            	productIds.add(rs.getString("ProductId"));
            
            return productIds;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception performing query. Message: "
                    + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (Exception e2) {
                LOG.log(Level.WARNING,
                        "Unable to rollback query transaction. Message: "
                                + e2.getMessage(), e2);
            }
            throw new CatalogException("Failed to query for reduced metadata : " + e.getMessage(), e);
        } finally {
            try { rs.close(); } catch (Exception ignore) {}
            try { statement.close(); } catch (Exception ignore) {}
            try { conn.close(); } catch (Exception ignore) {}
        }	
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getTopNProducts(int)
     */
    public List<Product> getTopNProducts(int n) throws CatalogException {
        return getTopNProducts(n, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getTopNProducts(int,
     *      org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    public List<Product> getTopNProducts(int n, ProductType type)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();
            statement.setMaxRows(n);

            String getProductSql = "SELECT * FROM products WHERE ProductType = '" + type.getName() + "' ORDER BY ProductRecievedTime DESC";

            LOG.log(Level.FINE, "getTopNProducts: executing: " + getProductSql);
            rs = statement.executeQuery(getProductSql);
            
            Vector<Product> products = new Vector<Product>();
            while (rs.next()) {
            	Product product = new Product();
            	product.setProductId(rs.getString("ProductId"));
            	product.setProductName(rs.getString("ProductName"));
            	product.setProductStructure(rs.getString("ProductStructure"));
            	ProductType productType = new ProductType();
            	productType.setName(rs.getString("ProductType"));
            	product.setProductType(productType);
            	product.setTransferStatus(rs.getString("TransferStatus"));
                products.add(product);
            }

            return (products.size() > 0) ? products : null;
        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "Exception getting top N products. Message: "
                            + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (Exception e2) {
                LOG.log(Level.WARNING,
                        "Unable to rollback get top N products. Message: "
                                + e2.getMessage(), e2);
            }
            throw new CatalogException("Failed to get top N products : " + e.getMessage(), e);
        } finally {
        	try { rs.close(); } catch (Exception ignore) {}
        	try { statement.close(); } catch (Exception ignore) {}
        	try { conn.close(); } catch (Exception ignore) {}

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getValidationLayer()
     */
    public ValidationLayer getValidationLayer() throws CatalogException {
        if (validationLayer == null) {
            throw new CatalogException("Validation Layer is null!");
        } else {
            return validationLayer;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getNumProducts(org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    public int getNumProducts(ProductType type) throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String getProductSql = "SELECT COUNT(ProductId) AS numProducts FROM products WHERE ProductType = '" + type.getName() + "'";
            
            LOG.log(Level.FINE, "getNumProducts: executing: " + getProductSql);
            rs = statement.executeQuery(getProductSql);

            int numProducts = -1;
            if (rs.next()) 
                numProducts = rs.getInt("numProducts");

            return numProducts;
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
        	try { rs.close(); } catch (Exception ignore) {}
        	try { statement.close(); } catch (Exception ignore) {}
        	try { conn.close(); } catch (Exception ignore) {}
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.util.Pagination#getFirstPage(org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    public ProductPage getFirstPage(ProductType type) {
        try {
            return pagedQuery(new Query(), type, 1);
        } catch (CatalogException e) {
            LOG.log(Level.SEVERE, "Exception getting first page: Message: "
                    + e.getMessage(), e);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.util.Pagination#getLastProductPage(org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    public ProductPage getLastProductPage(ProductType type) {
        try {
            return pagedQuery(new Query(), type, getFirstPage(type).getTotalPages());
        } catch (CatalogException e) {
            LOG.log(Level.SEVERE, "Exception getting last page: Message: "
                    + e.getMessage(), e);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.util.Pagination#getNextPage(org.apache.oodt.cas.filemgr.structs.ProductType,
     *      org.apache.oodt.cas.filemgr.structs.ProductPage)
     */
    public ProductPage getNextPage(ProductType type, ProductPage currentPage) {
        if (currentPage == null) 
            return getFirstPage(type);

        if (currentPage.isLastPage())
            return currentPage;

        try {
            return pagedQuery(new Query(), type, currentPage.getPageNum() + 1);
        } catch (CatalogException e) {
            LOG.log(Level.SEVERE, "Exception getting next page: Message: "
                    + e.getMessage(), e);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.util.Pagination#getPrevPage(org.apache.oodt.cas.filemgr.structs.ProductType,
     *      org.apache.oodt.cas.filemgr.structs.ProductPage)
     */
    public ProductPage getPrevPage(ProductType type, ProductPage currentPage) {
        if (currentPage == null) 
            return getFirstPage(type);

        if (currentPage.isFirstPage()) 
            return currentPage;

        try {
            return pagedQuery(new Query(), type, currentPage.getPageNum() - 1);
        } catch (CatalogException e) {
            LOG.log(Level.SEVERE, "Exception getting prev page: Message: "
                    + e.getMessage(), e);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#pagedQuery(org.apache.oodt.cas.filemgr.structs.Query,
     *      org.apache.oodt.cas.filemgr.structs.ProductType, int)
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
            
        	String getProductSql = "SELECT ProductId FROM " + type.getName() + "_vm" 
        		+ (query.getCriteria() != null ? " WHERE " + SqlParser.getInfixCriteriaString(query.getCriteria()) : "") 
        		+ " ORDER BY ProductRecievedTime DESC";
            rs = statement.executeQuery(getProductSql);

            int rsSize = -1;
            if (rs.last())
            	rsSize = rs.getRow();
            else 
            	return ProductPage.blankPage();
            
            Vector<String> productIds = new Vector<String>();
            if (pageNum > 0) {
            	int pageLoc = ((pageNum - 1) * pageSize) + 1;
                if (pageLoc > rsSize)
                	rs.absolute(pageNum = 1);
                else 
                    rs.absolute(pageLoc);

                // grab the rest
                int numGrabbed = 0;
                do {
                    String productId = rs.getString("ProductId");
                    productIds.add(productId);
                    numGrabbed++;
                }while (numGrabbed < pageSize && rs.next());

            } else {
                while (rs.next()) {
                    String productId = rs.getString("ProductId");
                    productIds.add(productId);
                }
            }
            
            ProductPage retPage = new ProductPage();
            retPage.setPageNum(pageNum);
            retPage.setPageSize(this.pageSize);
            retPage.setNumOfHits(rsSize);
            
            List<Product> products = new Vector<Product>(productIds.size());
            for (Iterator<String> i = productIds.iterator(); i.hasNext();) 
                products.add(getProductById(i.next()));
            retPage.setPageProducts(products);
            
            return retPage;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception performing query. Message: "
                    + e.getMessage(), e);
            try {
                conn.rollback();
            } catch (Exception e2) {
                LOG.log(Level.WARNING,
                        "Unable to rollback query transaction. Message: "
                                + e2.getMessage(), e2);
            }
            throw new CatalogException("Failed to query for reduced metadata : " + e.getMessage(), e);
        } finally {
            try { rs.close(); } catch (Exception ignore) {}
            try { statement.close(); } catch (Exception ignore) {}
            try { conn.close(); } catch (Exception ignore) {}
        }
    }
  
}
