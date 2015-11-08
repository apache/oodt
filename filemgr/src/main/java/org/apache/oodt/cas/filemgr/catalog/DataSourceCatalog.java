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
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.util.DbStructFactory;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.commons.pagination.PaginationUtils;
import org.apache.oodt.commons.util.DateConvert;

//SPRING imports
import org.springframework.util.StringUtils;



//JDK imports
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * @author mattmann
 * @author bfoster
 * @author luca
 * @version $Revision$
 * 
 * <p>
 * Implementation of a {@link Catalog} that is backed by a {@link DataSource}
 * front-end to a SQL DBMS.
 * </p>
 * 
 */
public class DataSourceCatalog implements Catalog {

  public static final int INT = 60;
  /* our sql data source */
    protected DataSource dataSource = null;

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(DataSourceCatalog.class.getName());

    /* our validation layer */
    private ValidationLayer validationLayer = null;

    /* boolean flag on whether or not product type id and element id are strings */
    protected boolean fieldIdStringFlag = false;

    /* size of pages of products within the catalog */
    protected int pageSize = -1;
    
    /* flag to indicate whether the "product_id" key type should be treated as a string */
    boolean productIdString = false;

    protected boolean orderedValues = false;

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
    public DataSourceCatalog(DataSource ds, ValidationLayer valLayer,
			     boolean fieldId, int pageSize, long cacheUpdateMin, boolean productIdString, boolean orderedValues) {
        this.dataSource = ds;
        this.validationLayer = valLayer;
        fieldIdStringFlag = fieldId;
        this.pageSize = pageSize;
        cacheUpdateMinutes = cacheUpdateMin;
        this.productIdString = productIdString;
        this.orderedValues = orderedValues;
    }
    
    /**
     * Constructor that assumes productIdString=false
     * to support current subclasses.
     * @param ds the datasource.
     * @param valLayer the validation layer
     * @param fieldId the feildid flag
     * @param pageSize the page size
     * @param cacheUpdateMin the min cache update.
     */
    public DataSourceCatalog(DataSource ds, ValidationLayer valLayer,
        boolean fieldId, int pageSize, long cacheUpdateMin) {
    	this(ds, valLayer, fieldId, pageSize, cacheUpdateMin, false, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#addMetadata(org.apache.oodt.cas.metadata.Metadata,
     *      org.apache.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void addMetadata(Metadata m, Product product)
            throws CatalogException {
        List<Element> metadataTypes;

        try {
            metadataTypes = validationLayer.getElements(product
                    .getProductType());
        } catch (ValidationLayerException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new CatalogException(
                    "ValidationLayerException when trying to obtain element list for product type: "
                            + product.getProductType().getName()
                            + ": Message: " + e.getMessage(), e);
        }

      for (Element element : metadataTypes) {
        List<String> values = m.getAllMetadata(element.getElementName());

        if (values == null) {
          LOG.log(Level.WARNING, "No Metadata specified for product ["
                                 + product.getProductName() + "] for required field ["
                                 + element.getElementName()
                                 + "]: Attempting to continue processing metadata");
          continue;
        }

        for (String value : values) {
          try {
            addMetadataValue(element, product, value);
          } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
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
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#addMetadata(org.apache.oodt.cas.metadata.Metadata,
     *      org.apache.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void removeMetadata(Metadata m, Product product)
            throws CatalogException {
        List<Element> metadataTypes;

        try {
            metadataTypes = validationLayer.getElements(product
                    .getProductType());
        } catch (ValidationLayerException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new CatalogException(
                    "ValidationLayerException when trying to obtain element list for product type: "
                            + product.getProductType().getName()
                            + ": Message: " + e.getMessage(), e);
        }

      for (Element element : metadataTypes) {
        List<String> values = m.getAllMetadata(element.getElementName());

        if (values != null) {
          for (String value : values) {
            try {
              removeMetadataValue(element, product, value);
            } catch (Exception e) {
              LOG.log(Level.SEVERE, e.getMessage());
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

            String addProductSql;
            String productTypeIdStr;

          productTypeIdStr = fieldIdStringFlag ? "'"
                                                 + product.getProductType().getProductTypeId() + "'"
                                               : product.getProductType().getProductTypeId();

						if (!productIdString) {
							
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
				
				        // read "product_id" value that was automatically assigned by the database
				        String productId = "";
				
				        String getProductIdSql = "SELECT MAX(product_id) AS max_id FROM products";
				
				        rs = statement.executeQuery(getProductIdSql);
				
				        while (rs.next()) {
				            productId = String.valueOf(rs.getInt("max_id"));
				        }
				        
		            product.setProductId(productId);
		            conn.commit();
							
						} else {
							
							// reuse the existing product id if possible, or generate a new UUID string
            	String productId = product.getProductId();
            	if (!StringUtils.hasText(productId)) {
                  productId = UUID.randomUUID().toString();
                }
            	// insert product in database
            	addProductSql = "INSERT INTO products (product_id, product_name, product_structure, product_transfer_status, product_type_id, product_datetime) "
                    + "VALUES ('"
                    + productId
                    + "', '"
                    + product.getProductName()
                    + "', '"
                    + product.getProductStructure()
                    + "', '"
                    + product.getTransferStatus()
                    + "', "
                    + productTypeIdStr
                    +", now()"
                    + ")";                       

            	LOG.log(Level.FINE, "addProduct: Executing: " + addProductSql);
            	statement.execute(addProductSql);
            	
              product.setProductId(productId);
              conn.commit();

						}


        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception adding product. Message: "
                    + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback addProduct transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
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

            String modifyProductSql = "UPDATE products SET product_name='"
                    + product.getProductName() + "', product_structure='"
                    + product.getProductStructure()
                    + "', product_transfer_status='"
                    + product.getTransferStatus() + "' "
                    + "WHERE product_id = " + quoteIt(product.getProductId());

            LOG.log(Level.FINE, "modifyProduct: Executing: "
                                + modifyProductSql);
            statement.execute(modifyProductSql);
            conn.commit();

            // now update the refs
            updateReferences(product);

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Exception modifying product. Message: "
                    + e.getMessage());
            try {
              assert conn != null;
              conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback modifyProduct transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
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

            String deleteProductSql = "DELETE FROM products WHERE product_id = "
                    + quoteIt(product.getProductId());

            LOG
                    .log(Level.FINE, "removeProduct: Executing: "
                            + deleteProductSql);
            statement.execute(deleteProductSql);
            deleteProductSql = "DELETE FROM "
                    + product.getProductType().getName() + "_metadata "
                    + " WHERE product_id = " + quoteIt(product.getProductId());
            LOG
                    .log(Level.FINE, "removeProduct: Executing: "
                            + deleteProductSql);
            statement.execute(deleteProductSql);
            deleteProductSql = "DELETE FROM "
                    + product.getProductType().getName() + "_reference "
                    + " WHERE product_id = " + quoteIt(product.getProductId());
            LOG
                    .log(Level.FINE, "removeProduct: Executing: "
                            + deleteProductSql);
            statement.execute(deleteProductSql);
            conn.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception removing product. Message: "
                    + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback removeProduct transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
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

            String modifyProductSql = "UPDATE products SET product_transfer_status='"
                    + product.getTransferStatus()
                    + "' "
                    + "WHERE product_id = " + quoteIt(product.getProductId());

            LOG.log(Level.FINE, "setProductTransferStatus: Executing: "
                    + modifyProductSql);
            statement.execute(modifyProductSql);
            conn.commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception setting transfer status for product. Message: "
                            + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback setProductTransferStatus transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
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

        String productRefTable = product.getProductType().getName()
                + "_reference";

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

          for (Reference r : product.getProductReferences()) {
            String addRefSql = "INSERT INTO "
                               + productRefTable
                               + " "
                               + "(product_id, product_orig_reference, product_datastore_reference, product_reference_filesize, product_reference_mimetype) "
                               + "VALUES ("
                               + quoteIt(product.getProductId())
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
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception adding product references. Message: "
                            + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback addProductReferences transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
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

        Product product = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductSql = "SELECT * " + "FROM products "
                    + "WHERE product_id = " + quoteIt(productId);

            LOG.log(Level.FINE, "getProductById: Executing: " + getProductSql);
            rs = statement.executeQuery(getProductSql);

            while (rs.next()) {
                product = DbStructFactory.getProduct(rs, false, productIdString);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception getting product. Message: "
                    + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getProductById transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return product;
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
                product = DbStructFactory.getProduct(rs, false, productIdString);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception getting product. Message: "
                    + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getProductByName transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return product;
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

        List<Reference> references = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            StringBuilder getProductRefSql = new StringBuilder("SELECT * FROM "
                    + product.getProductType().getName() + "_reference"
		+ " WHERE product_id = " + quoteIt(product.getProductId()));

            if(this.orderedValues) {
              getProductRefSql.append(" ORDER BY pkey");
            }

            LOG.log(Level.FINE, "getProductReferences: Executing: "
                    + getProductRefSql);
            rs = statement.executeQuery(getProductRefSql.toString());

            references = new Vector<Reference>();
            while (rs.next()) {
                Reference r = DbStructFactory.getReference(rs);
                references.add(r);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception getting product type. Message: "
                    + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getProductTypeById transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return references;
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

            String getProductSql = "SELECT products.* " + "FROM products "
                    + "ORDER BY products.product_id DESC";

            LOG.log(Level.FINE, "getProducts: Executing: " + getProductSql);
            rs = statement.executeQuery(getProductSql);
            products = new Vector<Product>();

            while (rs.next()) {
                Product product = DbStructFactory.getProduct(rs, false, productIdString);
                products.add(product);
            }

            if (products.size() == 0) {
                products = null;
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception getting products. Message: "
                    + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getProductstransaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return products;
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

        List<Product> products = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductSql;
            String productTypeIdStr;

          productTypeIdStr = fieldIdStringFlag ? "'" + type.getProductTypeId() + "'" : type.getProductTypeId();

            getProductSql = "SELECT products.* " + "FROM products "
                    + "WHERE products.product_type_id = " + productTypeIdStr;

            LOG.log(Level.FINE, "getProductsByProductType: Executing: "
                    + getProductSql);
            rs = statement.executeQuery(getProductSql);
            products = new Vector<Product>();

            while (rs.next()) {
                Product product = DbStructFactory.getProduct(rs, false, productIdString);
                products.add(product);
            }

            if (products.size() == 0) {
                products = null;
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception getting products. Message: "
                    + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback getProductsByProductType transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

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

            StringBuilder metadataSql = new StringBuilder("SELECT * FROM "
                    + product.getProductType().getName() + "_metadata"
		+ " WHERE product_id = " + quoteIt(product.getProductId()));
 
	    if(this.orderedValues) {
          metadataSql.append(" ORDER BY pkey");
        }

            LOG.log(Level.FINE, "getMetadata: Executing: " + metadataSql);
            rs = statement.executeQuery(metadataSql.toString());
            
            m = new Metadata();
            List<Element> elements;

            try {
                elements = validationLayer.getElements(product.getProductType());
            } catch (ValidationLayerException e) {
                LOG.log(Level.SEVERE, e.getMessage());
                throw new CatalogException(
                        "ValidationLayerException when trying to obtain element list for product type: "
                                + product.getProductType().getName()
                                + ": Message: " + e.getMessage(), e);
            }

            while (rs.next()) {
              for (Element e : elements) {
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
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception getting metadata. Message: "
                    + e.getMessage());
            throw new CatalogException(e.getMessage(), e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

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

            StringBuilder elementIds = new StringBuilder("");
            if (elems.size() > 0) {
                elementIds.append(" AND (element_id = '")
                          .append(this.validationLayer.getElementByName(elems.get(0)).getElementId
                              ()).append("'");
                for (int i = 1; i < elems.size(); i++) {
                  elementIds.append(" OR element_id = '").append(this.validationLayer.getElementByName(elems.get(i))
                                                                                     .getElementId()).append("'");
                }
                elementIds.append(")");
            }
            StringBuilder metadataSql = new StringBuilder("SELECT element_id,metadata_value FROM "
                    + product.getProductType().getName() + "_metadata"
		+ " WHERE product_id = " + quoteIt(product.getProductId()) + elementIds);
            if(this.orderedValues) {
              metadataSql.append(" ORDER BY pkey");
            }

            LOG.log(Level.FINE, "getMetadata: Executing: " + metadataSql);
            rs = statement.executeQuery(metadataSql.toString());

            m = new Metadata();
            List<Element> elements;

            try {
                elements = validationLayer.getElements(product.getProductType());
            } catch (ValidationLayerException e) {
                LOG.log(Level.SEVERE, e.getMessage());
                throw new CatalogException(
                        "ValidationLayerException when trying to obtain element list for product type: "
                                + product.getProductType().getName()
                                + ": Message: " + e.getMessage(), e);
            }

            while (rs.next()) {
              for (Element e : elements) {
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
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception getting metadata. Message: "
                    + e.getMessage());
            throw new CatalogException(e.getMessage(), e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return m;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#query(org.apache.oodt.cas.filemgr.structs.Query
     *      org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    public List<String> query(Query query, ProductType type) throws CatalogException {
        return paginateQuery(query, type, -1);
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
        List<Product> products = null;

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();
            statement.setMaxRows(n);

            StringBuilder getProductSql = new StringBuilder("SELECT products.* " + "FROM products ");

            if (type != null && type.getProductTypeId() != null) {
                if (fieldIdStringFlag) {
                    getProductSql.append("WHERE products.product_type_id = '").append(type.getProductTypeId())
                                 .append("' ");
                } else {
                    getProductSql.append("WHERE products.product_type_id = ").append(type.getProductTypeId())
                                 .append(" ");
                }

            }

            getProductSql.append("ORDER BY products.product_id DESC");

            LOG.log(Level.FINE, "getTopNProducts: executing: " + getProductSql.toString());

            rs = statement.executeQuery(getProductSql.toString());
            products = new Vector<Product>();

            while (rs.next()) {
                Product product = DbStructFactory.getProduct(rs, false, productIdString);
                products.add(product);
            }

            if (products.size() == 0) {
                products = null;
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception getting top N products. Message: "
                            + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback get top N products. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(),e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return products;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#getValidationLayer()
     */
    public ValidationLayer getValidationLayer() {
    	// note that validationLayer may be null to allow for leniency in subclasses
    	return validationLayer;
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
          StringBuilder valueClauseSql = new StringBuilder();

          valueClauseSql.append("VALUES ");

            // now do the value clause
            if (fieldIdStringFlag) {
                valueClauseSql.append("(").append(product.getProductId()).append(", '").append(element.getElementId())
                              .append("', '").append(value).append("')");
            } else {
                valueClauseSql.append("(").append(product.getProductId()).append(", ").append(element.getElementId())
                              .append(", '").append(value).append("')");
            }

            String metaIngestSql = ("INSERT INTO " + metadataTable
                                    + " (product_id, element_id, metadata_value) ")
                    + valueClauseSql.toString();
            LOG
                    .log(Level.FINE, "addMetadataValue: Executing: "
                            + metaIngestSql);
            statement.execute(metaIngestSql);
            conn.commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception adding metadata value. Message: "
                    + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback add metadata value. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

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
            StringBuilder metRemoveSql = new StringBuilder("DELETE FROM " + metadataTable + " WHERE ");
            if (fieldIdStringFlag) {
                metRemoveSql.append("PRODUCT_ID = '").append(product.getProductId()).append("' AND ");
                metRemoveSql.append("ELEMENT_ID = '").append(element.getElementId()).append("' AND ");
                metRemoveSql.append("METADATA_VALUE = '").append(value).append("'");
            } else {
                metRemoveSql.append("PRODUCT_ID = ").append(product.getProductId()).append(" AND ");
                metRemoveSql.append("ELEMENT_ID = ").append(element.getElementId()).append(" AND ");
                metRemoveSql.append("METADATA_VALUE = ").append(value);
            }

            LOG.log(Level.FINE, "removeMetadataValue: Executing: "
                    + metRemoveSql);
            statement.execute(metRemoveSql.toString());
            conn.commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception removing metadata value. Message: "
                            + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback remove metadata value. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
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

        int numProducts = -1;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            StringBuilder getProductSql = new StringBuilder("SELECT COUNT(products.product_id) AS numProducts "
                    + "FROM products ");

            if (fieldIdStringFlag) {
                getProductSql.append("WHERE products.product_type_id = '").append(type.getProductTypeId()).append("' ");
            } else {
                getProductSql.append("WHERE products.product_type_id = ").append(type.getProductTypeId()).append(" ");
            }

            LOG.log(Level.FINE, "getNumProducts: executing: " + getProductSql.toString());

            rs = statement.executeQuery(getProductSql.toString());

            while (rs.next()) {
                numProducts = rs.getInt("numProducts");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception getting num products. Message: "
                    + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback get num products. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return numProducts;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.util.Pagination#getFirstPage(org.apache.oodt.cas.filemgr.structs.ProductType)
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
     * @see org.apache.oodt.cas.filemgr.util.Pagination#getLastProductPage(org.apache.oodt.cas.filemgr.structs.ProductType)
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
     * @see org.apache.oodt.cas.filemgr.util.Pagination#getNextPage(org.apache.oodt.cas.filemgr.structs.ProductType,
     *      org.apache.oodt.cas.filemgr.structs.ProductPage)
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
     * @see org.apache.oodt.cas.filemgr.util.Pagination#getPrevPage(org.apache.oodt.cas.filemgr.structs.ProductType,
     *      org.apache.oodt.cas.filemgr.structs.ProductPage)
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
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#pagedQuery(org.apache.oodt.cas.filemgr.structs.Query,
     *      org.apache.oodt.cas.filemgr.structs.ProductType, int)
     */
    public ProductPage pagedQuery(Query query, ProductType type, int pageNum)
            throws CatalogException {
        int totalPages = PaginationUtils.getTotalPage(getResultListSize(query,
                type), this.pageSize);

        /*
         * if there are 0 total pages in the result list size then don't bother
         * returning a valid product page instead, return blank ProductPage
         */
        if (totalPages == 0) {
            return ProductPage.blankPage();
        }

        ProductPage retPage = new ProductPage();
        retPage.setPageNum(pageNum);
        retPage.setPageSize(this.pageSize);
        retPage.setTotalPages(totalPages);

        List<String> productIds = paginateQuery(query, type, pageNum);

        if (productIds != null && productIds.size() > 0) {
            List<Product> products = new Vector<Product>(productIds.size());

          for (String productId : productIds) {
            Product p = getProductById(productId);
            products.add(p);
          }

            retPage.setPageProducts(products);
        }

        return retPage;
    }

    protected int getResultListSize(Query query, ProductType type)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        int resultCount = 0;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            StringBuilder getProductSql = new StringBuilder("");
            String tableName = type.getName() + "_metadata";
            String subSelectQueryBase = "SELECT product_id FROM " + tableName
                    + " ";
          StringBuilder fromClause = new StringBuilder("FROM " + tableName
                    + " p ");
            StringBuilder whereClause = new StringBuilder("WHERE ");

            boolean gotFirstClause = false;
            int clauseNum = 0;

            if (query.getCriteria() != null && query.getCriteria().size() > 0) {
              for (QueryCriteria criteria : query.getCriteria()) {
                clauseNum++;

                String elementIdStr;

                elementIdStr =
                    fieldIdStringFlag ? "'" + this.validationLayer.getElementByName(criteria.getElementName())
                                                                  .getElementId() + "'"
                                      : this.validationLayer.getElementByName(criteria.getElementName()).getElementId();

                StringBuilder clause = new StringBuilder();

                if (!gotFirstClause) {
                  clause.append("(p.element_id = ").append(elementIdStr).append(" AND ");
                  if (criteria instanceof TermQueryCriteria) {
                    clause.append(" metadata_value LIKE '%").append(((TermQueryCriteria) criteria).getValue())
                          .append("%') ");
                  } else if (criteria instanceof RangeQueryCriteria) {
                    String startVal = ((RangeQueryCriteria) criteria)
                        .getStartValue();
                    String endVal = ((RangeQueryCriteria) criteria)
                        .getEndValue();
                    boolean inclusive = ((RangeQueryCriteria) criteria)
                        .getInclusive();

                    if ((startVal != null && !startVal.equals(""))
                        || (endVal != null && !endVal.equals(""))) {
                      clause.append(" metadata_value ");

                      boolean gotStart = false;

                      if (startVal != null && !startVal.equals("")) {
                        if (inclusive) {
                          clause.append(">= '").append(startVal).append("'");
                        } else {
                          clause.append("> '").append(startVal).append("'");
                        }
                        gotStart = true;
                      }

                      if (endVal != null && !endVal.equals("")) {
                        if (gotStart) {
                          if (inclusive) {
                            clause.append(" AND metadata_value <= '").append(endVal).append("'");
                          } else {
                            clause.append(" AND metadata_value < '").append(endVal).append("'");
                          }
                        } else if (inclusive) {
                          clause.append("<= '").append(endVal).append("'");
                        } else {
                          clause.append("< '").append(endVal).append("'");
                        }
                      }

                      clause.append(") ");
                    }
                  }
                  whereClause.append(clause);
                  gotFirstClause = true;
                } else {
                  String subSelectTblName = "p" + clauseNum;
                  StringBuilder subSelectQuery = new StringBuilder(subSelectQueryBase
                                                                   + "WHERE (element_id = " + elementIdStr
                                                                   + " AND ");
                  if (criteria instanceof TermQueryCriteria) {
                    subSelectQuery.append(" metadata_value LIKE '%")
                                  .append(((TermQueryCriteria) criteria).getValue()).append("%')");
                  } else if (criteria instanceof RangeQueryCriteria) {
                    String startVal = ((RangeQueryCriteria) criteria)
                        .getStartValue();
                    String endVal = ((RangeQueryCriteria) criteria)
                        .getEndValue();

                    if (startVal != null || endVal != null) {
                      subSelectQuery.append(" metadata_value ");

                      boolean gotStart = false;

                      if (startVal != null && !startVal.equals("")) {
                        subSelectQuery.append(">= '").append(startVal).append("'");
                        gotStart = true;
                      }

                      if (endVal != null && !endVal.equals("")) {
                        if (gotStart) {
                          subSelectQuery.append(" AND metadata_value <= '").append(endVal).append("'");
                        } else {
                          subSelectQuery.append("<= '").append(endVal).append("'");
                        }
                      }

                      subSelectQuery.append(") ");

                    }
                  }

                  fromClause.append("INNER JOIN (").append(subSelectQuery.toString()).append(") ")
                            .append(subSelectTblName).append(" ON ").append(subSelectTblName)
                            .append(".product_id = p.product_id ");

                }
              }
            }

            getProductSql.append("SELECT COUNT(DISTINCT p.product_id) AS numResults ").append(fromClause.toString());
            if (gotFirstClause) {
                getProductSql.append(whereClause.toString());
            }


            LOG.log(Level.FINE, "catalog get num results: executing: "
                    + getProductSql.toString());

            rs = statement.executeQuery(getProductSql.toString());

            while (rs.next()) {
                resultCount = rs.getInt("numResults");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception performing get num results. Message: "
                            + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback get num results transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return resultCount;
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

            try {
                Date lastUpdateTimeDate = DateConvert.isoParse(lastUpdateTime);

                long timeDifferenceMilis = currentTime.getTime()
                        - lastUpdateTimeDate.getTime();
                long timeDifferenceSeconds = timeDifferenceMilis * 1000;
                long timeDifferenceMinutes = timeDifferenceSeconds / INT;

              return timeDifferenceMinutes < cacheUpdateMinutes;
            } catch (Exception e) {
                LOG.log(Level.WARNING,
                        "Unable to parse last update time for product type: ["
                                + productTypeId + "]: Message: "
                                + e.getMessage());
                return false;
            }
        }

    }

    @SuppressWarnings("unchecked")
	private List<Product> getProductsFromCache(String productTypeId) {
        List<Product> products;

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

    @SuppressWarnings("unused")
	private List<Product> getProductsByProductTypeCached(ProductType type) {
        List<Product> products;
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
                return null;
            }
        }

        return products;
    }

    private List<String> paginateQuery(Query query, ProductType type, int pageNum)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        boolean doSkip = true;
        int numResults = -1;

        if (pageNum == -1) {
            doSkip = false;
        } else {
            numResults = getResultListSize(query, type);
        }

        try {
            
            conn = dataSource.getConnection();
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            StringBuilder getProductSql = new StringBuilder();
            
            if (!productIdString) {
            	
	            if (query.getCriteria().size() == 0) {
	                getProductSql.append("SELECT DISTINCT product_id FROM ").append(type.getName()).append("_metadata");
	            }else if (query.getCriteria().size() == 1) {
	                getProductSql.append(this.getSqlQuery(query.getCriteria().get(0), type));
	            }else {
	                getProductSql.append(this.getSqlQuery(new BooleanQueryCriteria(query.getCriteria(), BooleanQueryCriteria
                        .AND), type));
	            }
	            getProductSql.append(" ORDER BY product_id DESC ");
            
            } else {
            	
              if (query.getCriteria().size() == 0) {
                getProductSql.append("SELECT DISTINCT products.product_id FROM products, ").append(type.getName())
                             .append("_metadata").append(" WHERE products.product_id=").append(type.getName())
                             .append("_metadata.product_id");
              }	else if (query.getCriteria().size() == 1) {
                getProductSql.append(this.getSqlQuery(query.getCriteria().get(0), type));
              }	else {
                getProductSql.append(this.getSqlQuery(new BooleanQueryCriteria(query.getCriteria(), BooleanQueryCriteria
                    .AND), type));
              }
              getProductSql.append(" ORDER BY products.product_datetime DESC ");
              
            }
            
            LOG.log(Level.FINE, "catalog query: executing: " + getProductSql.toString());

            rs = statement.executeQuery(getProductSql.toString());

            List<String> productIds = new Vector<String>();
            if (doSkip) {
                int startNum = (pageNum - 1) * pageSize;

                if (startNum > numResults) {
                    startNum = 0;
                }

                // must call next first, or else no relative cursor
                if (rs.next()) {
                    // grab the first one
                    int numGrabbed;

                    if (pageNum == 1) {
                        numGrabbed = 1;
                        productIds.add(rs.getString("product_id"));
                    } else {
                        numGrabbed = 0;
                    }

                    // now move the cursor to the correct position
                    if (pageNum != 1) {
                        rs.relative(startNum - 1);
                    }

                    // grab the rest
                    while (rs.next() && numGrabbed < pageSize) {
                        String productId = rs.getString("product_id");
                        productIds.add(productId);
                        numGrabbed++;
                    }
                }

            } else {
                while (rs.next()) {
                    String productId = rs.getString("product_id");
                    productIds.add(productId);
                }
            }
            
            return productIds;

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception performing query. Message: "
                    + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback query transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

    }
    
    protected String getSqlQuery(QueryCriteria queryCriteria, ProductType type) throws ValidationLayerException, CatalogException {
        StringBuilder sqlQuery = new StringBuilder();
        if (queryCriteria instanceof BooleanQueryCriteria) {
            BooleanQueryCriteria bqc = (BooleanQueryCriteria) queryCriteria;
            if (bqc.getOperator() == BooleanQueryCriteria.NOT) {            	
            		if (!this.productIdString) {
            			sqlQuery.append("SELECT DISTINCT product_id FROM ").append(type.getName())
                                .append("_metadata WHERE product_id ").append("NOT IN (")
                                .append(this.getSqlQuery(bqc.getTerms().get(0), type)).append(")");
            		} else {
            			sqlQuery.append("SELECT DISTINCT products.product_id FROM products,").append(type.getName())
                                .append("_metadata").append(" WHERE products.product_id=").append(type.getName())
                                .append("_metadata.product_id").append(" AND products.product_id NOT IN (")
                                .append(this.getSqlQuery(bqc.getTerms().get
                                    (0), type)).append(")");
            		}
            }else {
                sqlQuery.append("(").append(this.getSqlQuery(bqc.getTerms().get(0), type));
                String op = bqc.getOperator() == BooleanQueryCriteria.AND ? "INTERSECT" : "UNION";
                for (int i = 1; i < bqc.getTerms().size(); i++) {
                  sqlQuery.append(") ").append(op).append(" (").append(this.getSqlQuery(bqc.getTerms().get(i), type));
                }
                sqlQuery.append(")");
            }
        }else {
        	  String elementIdStr = this.validationLayer.getElementByName(queryCriteria.getElementName()).getElementId();
            if (fieldIdStringFlag) {
              elementIdStr = "'" + elementIdStr + "'";
            }
            if (!this.productIdString) {
            	sqlQuery.append("SELECT DISTINCT product_id FROM ").append(type.getName())
                        .append("_metadata WHERE element_id = ").append(elementIdStr).append(" AND ");
            } else {
            	sqlQuery.append("SELECT DISTINCT products.product_id FROM products,").append(type.getName())
                        .append("_metadata").append(" WHERE products.product_id=").append(type.getName())
                        .append("_metadata.product_id").append(" AND element_id = ").append(elementIdStr)
                        .append(" AND ");
            }
            if (queryCriteria instanceof TermQueryCriteria) {
                sqlQuery.append("metadata_value = '").append(((TermQueryCriteria) queryCriteria).getValue())
                        .append("'");
            } else if (queryCriteria instanceof RangeQueryCriteria) {
                RangeQueryCriteria rqc = (RangeQueryCriteria) queryCriteria;
                String rangeSubQuery = null;
                if (rqc.getStartValue() != null) {
                  rangeSubQuery =
                      "metadata_value" + (rqc.getInclusive() ? " >= " : " > ") + "'" + rqc.getStartValue() + "'";
                }
                if (rqc.getEndValue() != null) {
                  rangeSubQuery =
                      rangeSubQuery == null ? "metadata_value" + (rqc.getInclusive() ? " <= " : " < ") + "'" + rqc
                          .getEndValue() + "'"
                                            : "(" + rangeSubQuery + " AND metadata_value" + (rqc.getInclusive() ? " <= "
                                                                                                                : " < ")
                                              + "'"
                                              + rqc.getEndValue() + "')";
                }
                sqlQuery.append(rangeSubQuery);
            } else {
                throw new CatalogException("Invalid QueryCriteria [" + queryCriteria.getClass().getCanonicalName() + "]");
            }
        }

        return sqlQuery.toString();
    }

    private synchronized void updateReferences(Product product)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;

        String productRefTable = product.getProductType().getName()
                + "_reference";

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            // first remove the refs
            String deleteProductSql = "DELETE FROM "
                    + product.getProductType().getName() + "_reference "
                    + " WHERE product_id = " + quoteIt(product.getProductId());
            LOG.log(Level.FINE, "updateProductReferences: Executing: "
                    + deleteProductSql);
            statement.execute(deleteProductSql);

            // now add the new ones back in
          for (Reference r : product.getProductReferences()) {
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
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception updating product references. Message: "
                            + e.getMessage());
            try {
              if (conn != null) {
                conn.rollback();
              }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback updateProductReferences transaction. Message: "
                                + e2.getMessage());
            }
            throw new CatalogException(e.getMessage(), e);
        } finally {

          if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

          }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

    }
    
    /**
     * Utility method to quote the "productId" value 
     * if the column type is "string".
     * @param productId the product id
     * @return the quoted productId
     */
    protected String quoteIt(String productId) {
      return this.productIdString ? "'" + productId + "'" : productId;
    }

}
