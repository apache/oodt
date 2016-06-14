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
package org.apache.oodt.cas.filemgr.catalog;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.util.DbStructFactory;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.commons.date.DateUtils;
import org.apache.oodt.commons.pagination.PaginationUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

/**
 * 
 * Provides a Catalog implementation that facilitate large-scale ingest of
 * observational data into a relational schema.
 * 
 * @author ahart
 * @author cgoodale
 * @author mattmann
 * 
 */
public class ScienceDataCatalog implements Catalog {

  // Our SQL data source
  protected DataSource dataSource = null;

  // Our WRM val layer
  protected ValidationLayer validationLayer = null;

  protected int pageSize = -1;

  // Our log stream
  private Logger LOG = Logger.getLogger(ScienceDataCatalog.class
      .getName());

  public ScienceDataCatalog(DataSource dataSource,
      ValidationLayer validationLayer, int pageSize) {
    this.dataSource = dataSource;
    this.validationLayer = validationLayer;
    this.pageSize = pageSize;
  }

  public void addMetadata(Metadata m, Product p) throws CatalogException {

    try {
      // First, understand which dataset this metadata belongs to
      // by examining the dataset_id metadata field
      int datasetId = Integer.parseInt(m.getMetadata("dataset_id"));

      // Then, create a record for the granule in the database,
      // and store its id.
      int granuleId = this.createGranule(datasetId,
          m.getMetadata("granule_filename"));

      // Then, create a record for any parameters detected in file
      // that do not already exist in the database
      Metadata paramMetadata = getMetadataSubset(m, Pattern.compile("param_*"));

      for (Enumeration keys = paramMetadata.getHashTable().keys(); keys
          .hasMoreElements();) {

        String key = (String) keys.nextElement();
        String keyName = key.substring(6); // trim "param_"

        int paramId = this.createParameter(datasetId, keyName);
        LOG.log(Level.INFO, "Currently extracting data for variable '"
            + keyName + "' ");

        // Create a record for each dataPoint for each parameter
        // detected in the file

        List<String> dataPoints = m.getAllMetadata("data_" + keyName);

        LOG.log(Level.INFO, "Will now extract " + dataPoints.size()
            + " data points for variable '" + keyName + "'... ");

        StringBuffer queryBuffer = new StringBuffer(
            "INSERT INTO `dataPoint` (`granule_id`,`dataset_id`,`parameter_id`,"
                + "`latitude`,`longitude`,`vertical`,`time`,`value`) VALUES ");
        // grab a count of items in the dataPoints list

        LOG.log(Level.INFO, "there are " + dataPoints.size()
            + " data points in the List");

        // CGOODALE int to count how many times this thing loops
        int iterCount = 0;

        for (String dataPoint : dataPoints) {

          // Decompose each dataPoint into its component parts:
          // lat,lon,vertical,time,value
          String[] components = dataPoint.split(",");

          // Append the dataPoint to the StringBuffer
          queryBuffer.append(" ( ");
          queryBuffer.append(granuleId); // granuleId
          queryBuffer.append(" , ");
          queryBuffer.append(datasetId); // datasetId
          queryBuffer.append(" , ");
          queryBuffer.append(paramId); // parameterId
          queryBuffer.append(" , ");
          queryBuffer.append(components[0]); // Lat
          queryBuffer.append(" , ");
          queryBuffer.append(components[1]); // Lon
          queryBuffer.append(" , ");
          queryBuffer.append(components[2]); // vertical
          queryBuffer.append(" , ");
          queryBuffer.append('"'); // Conversion from ISO to MySQL Compliant
          // DateTime Format
          queryBuffer.append(components[3].substring(0, 4)); // YYYY
          queryBuffer.append("-");
          queryBuffer.append(components[3].substring(4, 6)); // MM
          queryBuffer.append("-");
          queryBuffer.append(components[3].substring(6, 8)); // DD
          queryBuffer.append(" ");
          queryBuffer.append(components[3].substring(9, 11)); // HH
          queryBuffer.append(":");
          queryBuffer.append(components[3].substring(11, 13)); // mm
          // no datasets contain seconds at this time
          // including this code would cause some datasets to
          // parse Z, into seconds
          queryBuffer.append('"'); // End time
          queryBuffer.append(" , ");
          queryBuffer.append(components[4]); // Value
          queryBuffer.append(" ) ");

          queryBuffer.append(',');// Statement seperator

          // Periodically commit the query
          if (iterCount > 100) {

            queryBuffer.deleteCharAt(queryBuffer.length() - 1);
            queryBuffer.append(";");

            // Commit the query;
            this.commitQuery(queryBuffer, null);

            // RESET query
            queryBuffer = new StringBuffer(
                "INSERT INTO `dataPoint` (`granule_id`,`dataset_id`,`parameter_id`,`latitude`,"
                    + "`longitude`,`vertical`,`time`,`value`) VALUES ");

            // RESET iterCount
            iterCount = 0;
          } else {
            // Increment the counter
            iterCount++;
          }
        }

        // Commit any remaining data points
        queryBuffer.deleteCharAt(queryBuffer.length() - 1);
        queryBuffer.append(";");
        this.commitQuery(queryBuffer, null);

        LOG.log(Level.INFO, "Extracted " + dataPoints.size()
            + " data points for variable '" + keyName + "' ");
      }

    } catch (Exception e) {
      LOG.log(Level.WARNING,
          "Exception adding product metadata. Message: " + e.getMessage());
      LOG.log(Level.SEVERE, e.getMessage());
      throw new CatalogException(e.getMessage(), e);
    }
  }

  public int createGranule(int datasetId, String filename)
      throws CatalogException, SQLException {
    // Detect duplicate granule
    Connection conn = null;
    Statement statement = null;
    boolean alreadyExists = false;
    int granuleId = 0;
    String queryExists = "SELECT granule_id FROM `granule` WHERE `filename`='"
        + filename + "' AND `dataset_id`='" + datasetId + "'; ";
    try {
      conn = this.dataSource.getConnection();
      conn.setAutoCommit(true);
      statement = conn.createStatement();
      ResultSet rs = statement.executeQuery(queryExists);
      while (rs.next()) {
        granuleId = rs.getInt("granule_id");
      }
      alreadyExists = (granuleId > 0);
    } catch (SQLException e) {
      LOG.log(Level.WARNING,
          "SQL Exception querying for granule existence. Last query was: "
              + queryExists + " , Message: " + e.getMessage());
      throw new SQLException(e.getMessage(), e);
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

    if (!alreadyExists) {
      String query = "INSERT INTO `granule` (`dataset_id`,`filename`) VALUES ('"
          + datasetId + "','" + filename + "');";
      try {
        String maxQuery = "SELECT MAX(granule_id) AS max_id FROM granule";
        granuleId = this.commitQuery(new StringBuffer(query), maxQuery);
      } catch (Exception e) {
        throw new CatalogException(e.getMessage(), e);
      }
    }

    // Return the granule id
    return granuleId;
  }

  public int createParameter(int datasetId, String longName)
      throws CatalogException, SQLException {
    // Detect duplicate parameter
    Connection conn = null;
    Statement statement = null;
    boolean alreadyExists = false;
    int parameterId = 0;
    String queryExists = "SELECT parameter_id FROM `parameter` WHERE `longName`='"
        + longName + "' AND `dataset_id`='" + datasetId + "'; ";
    try {
      conn = this.dataSource.getConnection();
      conn.setAutoCommit(true);
      int count = 0;
      statement = conn.createStatement();
      ResultSet rs = statement.executeQuery(queryExists);
      while (rs.next()) {
        parameterId = rs.getInt("parameter_id");
      }
      alreadyExists = (parameterId > 0);
    } catch (SQLException e) {
      LOG.log(Level.WARNING,
          "SQL Exception querying for parameter existence. Last query was: "
              + queryExists + " , Message: " + e.getMessage());
      throw new SQLException(e.getMessage(), e);
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

    if (!alreadyExists) {
      // Insert the parameter into the database
      String query = "INSERT INTO `parameter` (`longName`,`dataset_id`,`description`) VALUES ('"
          + longName + "','" + datasetId + "','') ";

      try {
        String maxQuery = "SELECT MAX(parameter_id) AS max_id FROM parameter";
        parameterId = this.commitQuery(new StringBuffer(query), maxQuery);
      } catch (Exception e) {
        throw new CatalogException(e.getMessage(), e);
      }
    }

    // Return the id of the parameter
    return parameterId;
  }

  public int commitQuery(StringBuffer query, String maxQuery)
      throws SQLException {
    Connection conn = null;
    Statement statement = null;
    int returnId = 0;

    // Try to commit the query
    try {
      conn = this.dataSource.getConnection();
      conn.setAutoCommit(false);
      statement = conn.createStatement();
      statement.execute(query.toString());

      if (maxQuery != null) {
        ResultSet rs = statement.executeQuery(maxQuery);
        while (rs.next()) {
          returnId = rs.getInt("max_id");
        }
      }
      conn.commit();
      return returnId;

    } catch (SQLException e) {
      // TODO Auto-generated catch block
      LOG.log(Level.WARNING,
          "SQL Exception adding product metadata. Last query was: " + query
              + " , Message: " + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(
            Level.SEVERE,
            "Unable to rollback addMetadata transaction. Message: "
                + e2.getMessage());
      }
      throw new SQLException(e.getMessage(), e);
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

  public void addProduct(Product product) throws CatalogException {
    // TODO Auto-generated method stub

  }

  public void addProductReferences(Product product) throws CatalogException {
    // TODO Auto-generated method stub

  }

  public Metadata getMetadata(Product product) throws CatalogException {
    Metadata met = new Metadata();
    met.addMetadata("Filename", product.getProductName());
    met.addMetadata("ProductType", product.getProductType().getName());
    met.addMetadata("FileLocation", "Unknown");
    met.addMetadata("CAS.ProductReceivedTime",
        DateUtils.toString(Calendar.getInstance()));
    return met;
  }

  public int getNumProducts(ProductType productType) throws CatalogException {
    String sql = "SELECT COUNT(granule_id) as num_products FROM granule "
        + "          WHERE dataset_id IN (SELECT dataset_id FROM dataset "
        + "                               WHERE shortName = '"
        + productType.getName() + "'" + "                           )";

    ResultSet rs = null;
    Statement statement = null;
    Connection conn = null;
    int numProducts = 0;

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        numProducts = rs.getInt("num_products");
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

    return numProducts;
  }

  public Product getProductById(String productId) throws CatalogException {
    String sql = "SELECT granule_id, dataset_id, filename FROM granule WHERE granule_id = "
        + productId;
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    Product product = null;

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        product = DbStructFactory.toScienceDataProduct(rs);
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

    return product;
  }

  public Product getProductByName(String productName) throws CatalogException {
    String sql = "SELECT granule_id, dataset_id, filename FROM granule WHERE fileName = '"
        + productName + "'";
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    Product product = null;

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        product = DbStructFactory.toScienceDataProduct(rs);
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

    return product;
  }

  public List<Reference> getProductReferences(Product product)
      throws CatalogException {
    return Collections.EMPTY_LIST;
  }

  public List<Product> getProducts() throws CatalogException {
    String sql = "SELECT granule_id, dataset_id, filename FROM granule ORDER BY granule_id DESC";
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    List<Product> products = new Vector<Product>();

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        products.add(DbStructFactory.toScienceDataProduct(rs));
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

    return products;
  }

  public List<Product> getProductsByProductType(ProductType productType)
      throws CatalogException {
    String sql = "SELECT granule_id, dataset_id, filename FROM granule WHERE dataset_id = "
        + productType.getProductTypeId() + " ORDER BY granule_id DESC";
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    List<Product> products = new Vector<Product>();

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        products.add(DbStructFactory.toScienceDataProduct(rs));
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

    return products;
  }

  public Metadata getReducedMetadata(Product product, List<String> metList)
      throws CatalogException {

    Metadata met = this.getMetadata(product);
    Metadata finalMet = new Metadata();
    for (String metKey : metList) {
      finalMet.addMetadata(metKey, met.getAllMetadata(metKey));
    }

    return finalMet;

  }

  public List<Product> getTopNProducts(int num) throws CatalogException {
    String sql = "SELECT granule_id, dataset_id, filename FROM granule ORDER BY granule_id DESC LIMIT "
        + num;
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    List<Product> products = new Vector<Product>();

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      LOG.log(Level.INFO, "Executing: [" + sql + "]");
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        products.add(DbStructFactory.toScienceDataProduct(rs));
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

    return products;
  }

  public List<Product> getTopNProducts(int num, ProductType productType)
      throws CatalogException {
    String sql = "SELECT granule_id, dataset_id, filename FROM granule WHERE dataset_id = "
        + productType.getProductTypeId()
        + " ORDER BY granule_id DESC LIMIT "
        + num;
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    List<Product> products = new Vector<Product>();

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      LOG.log(Level.INFO, "Executing: [" + sql + "]");
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        products.add(DbStructFactory.toScienceDataProduct(rs));
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

    return products;
  }

  public ValidationLayer getValidationLayer() {
    return this.validationLayer;
  }

  public void modifyProduct(Product arg0) throws CatalogException {
    // TODO Auto-generated method stub

  }

  public List<String> query(Query arg0, ProductType arg1)
      throws CatalogException {
    // TODO Auto-generated method stub
    return null;
  }

  public void removeMetadata(Metadata arg0, Product arg1)
      throws CatalogException {
    // TODO Auto-generated method stub

  }

  public void removeProduct(Product arg0) throws CatalogException {
    // TODO Auto-generated method stub

  }

  public void setProductTransferStatus(Product arg0) throws CatalogException {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.util.Pagination#getFirstPage(org.apache.oodt
   * .cas.filemgr.structs.ProductType)
   */
  public ProductPage getFirstPage(ProductType type) {
    Query query = new Query();
    ProductPage firstPage = null;

    try {
      firstPage = pagedQuery(query, type, 1);
    } catch (CatalogException e) {
      LOG.log(Level.WARNING,
          "Exception getting first page: Message: " + e.getMessage());
    }
    return firstPage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.util.Pagination#getLastProductPage(org.apache
   * .oodt.cas.filemgr.structs.ProductType)
   */
  public ProductPage getLastProductPage(ProductType type) {
    ProductPage lastPage = null;
    ProductPage firstPage = getFirstPage(type);
    Query query = new Query();
    try {
      lastPage = pagedQuery(query, type, firstPage.getTotalPages());
    } catch (CatalogException e) {
      LOG.log(Level.WARNING,
          "Exception getting last page: Message: " + e.getMessage());
    }

    return lastPage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.util.Pagination#getNextPage(org.apache.oodt
   * .cas.filemgr.structs.ProductType,
   * org.apache.oodt.cas.filemgr.structs.ProductPage)
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
      LOG.log(Level.WARNING,
          "Exception getting next page: Message: " + e.getMessage());
    }

    return nextPage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.util.Pagination#getPrevPage(org.apache.oodt
   * .cas.filemgr.structs.ProductType,
   * org.apache.oodt.cas.filemgr.structs.ProductPage)
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
      LOG.log(Level.WARNING,
          "Exception getting prev page: Message: " + e.getMessage());
    }

    return prevPage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.catalog.Catalog#pagedQuery(org.apache.oodt.
   * cas.filemgr.structs.Query, org.apache.oodt.cas.filemgr.structs.ProductType,
   * int)
   */
  public ProductPage pagedQuery(Query query, ProductType type, int pageNum)
      throws CatalogException {
    int totalPages = PaginationUtils.getTotalPage(
        getResultListSize(query, type), this.pageSize);

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

  private List<String> paginateQuery(Query query, ProductType type, int pageNum)
      throws CatalogException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    List<String> productIds = new Vector<String>();

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

      String getProductSql = "SELECT DISTINCT granule_id FROM granule WHERE "
          + "dataset_id = " + type.getProductTypeId()
          + " ORDER BY granule_id DESC ";
      LOG.log(Level.FINE, "Executing: [" + getProductSql + "]");
      rs = statement.executeQuery(getProductSql);

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
            productIds.add(rs.getString("granule_id"));
          } else {
            numGrabbed = 0;
          }

          // now move the cursor to the correct position
          if (pageNum != 1) {
            rs.relative(startNum - 1);
          }

          // grab the rest
          while (rs.next() && numGrabbed < pageSize) {
            String productId = rs.getString("granule_id");
            productIds.add(productId);
            numGrabbed++;
          }
        }

      } else {
        while (rs.next()) {
          String productId = rs.getString("granule_id");
          productIds.add(productId);
        }
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "Exception performing query. Message: " + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(Level.SEVERE, "Unable to rollback query transaction. Message: "
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

    return productIds;

  }

  private int getResultListSize(Query query, ProductType productType) {
    String sql = "SELECT COUNT(granule_id) as result_size FROM granule where dataset_id = "
        + productType.getProductTypeId();
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    int size = -1;

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      LOG.log(Level.INFO, "Executing: [" + sql + "]");
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        size = rs.getInt("result_size");
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

    return size;
  }

  private String getStartDateTime(Product product) {
    String sql = "SELECT MIN(time) as start_date_time FROM dataPoint "
        + "          WHERE granule_id IN (SELECT granule_id FROM granule "
        + "                               WHERE filename = '"
        + product.getProductName() + "'" + "                           )";

    ResultSet rs = null;
    Statement statement = null;
    Connection conn = null;
    String startDateTime = null;

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(rs.getDate("start_date_time"));
        startDateTime = DateUtils.toString(cal);
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

    return startDateTime;

  }

  private String getEndDateTime(Product product) {
    String sql = "SELECT MAX(time) as end_date_time FROM dataPoint "
        + "          WHERE granule_id IN (SELECT granule_id FROM granule "
        + "                               WHERE filename = '"
        + product.getProductName() + "'" + "                           )";

    ResultSet rs = null;
    Statement statement = null;
    Connection conn = null;
    String endDateTime = null;

    try {
      conn = this.dataSource.getConnection();
      statement = conn.createStatement();
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(rs.getDate("end_date_time"));
        endDateTime = DateUtils.toString(cal);
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

    return endDateTime;

  }

  /**
   * getMetadataSubset
   * 
   * Returns the subset of metadata keys in the provided Metadata object that
   * match against the regular expression specified in pattern. Note that
   * complete match is not required for success. This function utilizes the
   * 'lookingAt()' function of the java.util.regex.Matcher class to determine
   * whether the candidate string contains the pattern as a substring when
   * starting from the zeroth index (the beginning).
   * 
   * @param m
   *          The Metadata object containing keys from which a subset will be
   *          extracted
   * @param pattern
   *          The regular expression to use in determining matching keys
   * @return A Metadata object containing only those keys which matched
   *         'pattern'
   */
  private static Metadata getMetadataSubset(Metadata m, Pattern pattern) {

    Metadata subset = new Metadata();

    for (Object key : m.getMap().keySet()) {

      Matcher matcher = pattern.matcher((String) key);
      if (matcher.lookingAt()) {
        subset.addMetadata((String) key, m.getMetadata((String) key));
      }
    }

    return subset;
  }
}
