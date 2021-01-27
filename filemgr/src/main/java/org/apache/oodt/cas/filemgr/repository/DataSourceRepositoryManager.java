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

package org.apache.oodt.cas.filemgr.repository;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.util.DbStructFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//JDK imports
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;
import javax.sql.DataSource;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * An implementation of the {@link RepositoryManager} interface that is backed
 * by a {@link DataSource} for storing and retreiving {@link Product} policy in
 * the form of {@link ProductType}s.
 * </p>
 * 
 */
public class DataSourceRepositoryManager implements RepositoryManager {

    /* our sql data source */
    private DataSource dataSource = null;

    /* our log stream */
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceRepositoryManager.class);

    /**
     * 
     * <p>
     * Default Constructor
     * </p>.
     * 
     * @param ds
     *            The DataSource to initialize this repository manager with.
     */
    public DataSourceRepositoryManager(DataSource ds) {
        this.dataSource = ds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.repository.RepositoryManager#addProductType(org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    public synchronized void addProductType(ProductType productType)
            throws RepositoryManagerException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String addProductTypeSql = "INSERT INTO product_types (product_type_name, product_type_description, product_type_repository_path, product_type_versioner_class) "
                    + "VALUES ('"
                    + productType.getName()
                    + "', '"
                    + productType.getDescription()
                    + "', '"
                    + productType.getProductRepositoryPath()
                    + "', '"
                    + productType.getVersioner() + "')";

            LOG.info("addProductType: Executing: {}", addProductTypeSql);
            statement.execute(addProductTypeSql);

            String productTypeId = "";
            String getProductTypeIdSql = "SELECT MAX(product_type_id) AS max_id FROM product_types";

            rs = statement.executeQuery(getProductTypeIdSql);

            while (rs.next()) {
                productTypeId = String.valueOf(rs.getInt("max_id"));
            }

            productType.setProductTypeId(productTypeId);

            // create the references table
            String createRefSql = "CREATE TABLE product_reference_"
                    + productTypeId
                    + " (product_id int NOT NULL, product_orig_reference varchar(255), product_datastore_reference varchar(255))";
            LOG.info("addProductType: Executing: {}", createRefSql);
            statement.execute(createRefSql);

            // create the metadata table
            String createMetaSql = "CREATE TABLE product_metadata_"
                    + productTypeId
                    + " (product_id int NOT NULL, element_id int NOT NULL, metadata_value varchar(2000) NOT NULL)";
            LOG.info("addProductType: Executing: {}", createMetaSql);
            statement.execute(createMetaSql);
            conn.commit();

        } catch (Exception e) {
            LOG.warn("Exception adding product type: {}", e.getMessage(), e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.error("Unable to rollback addProductType transaction: {}", e2.getMessage(), e2);
            }
            throw new RepositoryManagerException(e.getMessage());
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
     * @see org.apache.oodt.cas.filemgr.repository.RepositoryManager#modifyProductType(org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    public synchronized void modifyProductType(ProductType productType)
            throws RepositoryManagerException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String modifyProductTypeSql = "UPDATE product_types SET product_type_name='"
                    + productType.getName()
                    + "', product_type_description='"
                    + productType.getDescription()
                    + "', "
                    + "product_type_versioner_class='"
                    + productType.getVersioner()
                    + "', product_type_repository_path='"
                    + productType.getProductRepositoryPath()
                    + "' "
                    + "WHERE product_type_id = "
                    + productType.getProductTypeId();

            LOG.info("modifyProductType: Executing: {}", modifyProductTypeSql);
            statement.execute(modifyProductTypeSql);
            conn.commit();

        } catch (Exception e) {
            LOG.warn("Exception modifying product type: {}",e.getMessage(), e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.error("Unable to rollback modifyProductType transaction: {}", e2.getMessage(), e2);
            }
            throw new RepositoryManagerException(e.getMessage());
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
     * @see org.apache.oodt.cas.filemgr.repository.RepositoryManager#removeProductType(org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    public void removeProductType(ProductType productType)
            throws RepositoryManagerException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String deleteProductTypeSql = "DELETE FROM product_types WHERE product_type_id = "
                    + productType.getProductTypeId();

            LOG.info("removeProductType: Executing: {}", deleteProductTypeSql);
            statement.execute(deleteProductTypeSql);

            // TODO: Decide if it makes sense to delete the references table
            // and the metadata table here. For now, we won't because maybe
            // they'll just want to remove the ability to deal with this product
            // type
            // rather than remove all of the metdata and references for the
            // products with it
            conn.commit();

        } catch (Exception e) {
            LOG.warn("Exception removing product type: {}", e.getMessage(), e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.error("Unable to rollback removeProductType transaction: {}", e2.getMessage(), e2);
            }
            throw new RepositoryManagerException(e.getMessage());
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
     * @see org.apache.oodt.cas.filemgr.repository.RepositoryManager#getProductTypeById(java.lang.String)
     */
    public ProductType getProductTypeById(String productTypeId)
            throws RepositoryManagerException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        ProductType productType = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductTypeSql = "SELECT * from product_types WHERE product_type_id = "
                    + productTypeId;

            LOG.info("getProductTypeById: Executing: {}", getProductTypeSql);
            rs = statement.executeQuery(getProductTypeSql);

            while (rs.next()) {
                productType = DbStructFactory.getProductType(rs);
            }

        } catch (Exception e) {
            LOG.warn("Exception getting product type: {}", e.getMessage(), e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.error("Unable to rollback getProductTypeById transaction: {}", e2.getMessage(), e2);
            }
            throw new RepositoryManagerException(e.getMessage());
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

        return productType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.repository.RepositoryManager#getProductTypeByName(java.lang.String)
     */
    public ProductType getProductTypeByName(String productTypeName)
            throws RepositoryManagerException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        ProductType productType = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductTypeSql = "SELECT * from product_types WHERE product_type_name = '"
                    + productTypeName + "'";

            LOG.info("getProductTypeByName: Executing: {}", getProductTypeSql);
            rs = statement.executeQuery(getProductTypeSql);

            while (rs.next()) {
                productType = DbStructFactory.getProductType(rs);
            }

        } catch (Exception e) {
            LOG.warn("Exception getting product type: {}", e.getMessage(), e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.error("Unable to rollback getProductTypeByName transaction: {}", e2.getMessage(), e2);
            }
            throw new RepositoryManagerException(e.getMessage());
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

        return productType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.repository.RepositoryManager#getProductTypes()
     */
    public List<ProductType> getProductTypes() throws RepositoryManagerException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        List<ProductType> productTypes = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductTypeSql = "SELECT * from product_types";

            LOG.info("getProductTypes: Executing: {}", getProductTypeSql);
            rs = statement.executeQuery(getProductTypeSql);

            productTypes = new Vector<ProductType>();
            while (rs.next()) {
                ProductType productType = DbStructFactory.getProductType(rs);
                productTypes.add(productType);
            }

            if (productTypes.size() == 0) {
                productTypes = null;
            }

        } catch (Exception e) {
            LOG.warn("Exception getting product types: {}", e.getMessage(), e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.error("Unable to rollback getProductTypes transaction: {}", e2.getMessage(), e2);
            }
            throw new RepositoryManagerException(e.getMessage());
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

        return productTypes;
    }

}
