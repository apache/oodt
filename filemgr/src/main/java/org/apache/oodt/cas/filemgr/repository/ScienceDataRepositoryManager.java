/**
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

//JDK imports
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

//OODT imports
import org.apache.oodt.cas.filemgr.repository.RepositoryManager;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.util.DbStructFactory;

/**
 * 
 * Leverages the information in the {@link ScienceDataCatalog}'s dataset table
 * to list out {@link ProductType}s.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class ScienceDataRepositoryManager implements RepositoryManager {

  private static final Logger LOG = Logger
      .getLogger(ScienceDataRepositoryManager.class.getName());

  private DataSource dataSource;

  public ScienceDataRepositoryManager(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.repository.RepositoryManager#addProductType
   * (org.apache.oodt.cas.filemgr.structs.ProductType)
   */
  public void addProductType(ProductType productType)
      throws RepositoryManagerException {
    String sql = "INSERT INTO dataset (longName, shortName, description) VALUES ('"
        + productType.getName()
        + "', '"
        + productType.getName()
        + "', '"
        + productType.getDescription() + "'";
    Connection conn = null;
    Statement statement = null;

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      statement.execute(sql);
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }
      }

    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.repository.RepositoryManager#getProductTypeById
   * (java.lang.String)
   */
  public ProductType getProductTypeById(String productTypeId)
      throws RepositoryManagerException {
    String sql = "SELECT dataset_id, shortName, longName, source, referenceURL, description from dataset WHERE dataset_id = "
        + productTypeId;
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    ProductType productType = null;

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      LOG.log(Level.FINE, "Executing: [" + sql + "]");
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        productType = DbStructFactory.toScienceDataProductType(rs);
      }
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (Exception ignore) {
        }
      }

      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }
      }

    }

    return productType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.repository.RepositoryManager#getProductTypeByName
   * (java.lang.String)
   */
  public ProductType getProductTypeByName(String productTypeName)
      throws RepositoryManagerException {
    String sql = "SELECT dataset_id, shortName, longName, source, referenceURL, description from dataset WHERE shortName = '"
        + productTypeName + "'";
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    ProductType productType = null;

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        productType = DbStructFactory.toScienceDataProductType(rs);
      }
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (Exception ignore) {
        }
      }

      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }
      }

    }

    return productType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.repository.RepositoryManager#getProductTypes ()
   */
  public List<ProductType> getProductTypes() throws RepositoryManagerException {
    String sql = "SELECT dataset_id, shortName, longName, source, referenceURL, description from dataset ORDER BY dataset_id DESC";
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    List<ProductType> productTypes = new Vector<ProductType>();

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        productTypes.add(DbStructFactory.toScienceDataProductType(rs));
      }
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (Exception ignore) {
        }
      }

      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }
      }
    }

    return productTypes;

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.repository.RepositoryManager#modifyProductType
   * (org.apache.oodt.cas.filemgr.structs.ProductType)
   */
  public void modifyProductType(ProductType productType)
      throws RepositoryManagerException {
    String sql = "UPDATE dataset SET shortName='" + productType.getName()
        + "',description='" + productType.getDescription()
        + "' WHERE dataset_id = " + productType.getProductTypeId();

    Connection conn = null;
    Statement statement = null;

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      statement.execute(sql);
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }
      }

    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.repository.RepositoryManager#removeProductType
   * (org.apache.oodt.cas.filemgr.structs.ProductType)
   */
  public void removeProductType(ProductType productType)
      throws RepositoryManagerException {
    String sql = "DELETE FROM dataset WHERE dataset_id = "
        + productType.getProductTypeId();

    Connection conn = null;
    Statement statement = null;

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      statement.execute(sql);
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }
      }

    }

  }

}
