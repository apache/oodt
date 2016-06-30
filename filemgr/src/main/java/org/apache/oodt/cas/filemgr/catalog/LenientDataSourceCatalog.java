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

// OODT imports

import org.apache.oodt.cas.filemgr.structs.BooleanQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.metadata.Metadata;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * @author luca
 * @version $Revision$
 * 
 * <p>
 * Extension of {@link DataSourceCatalog} that can accomodate dynamic fields.
 * </p>
 * 
 */
public class LenientDataSourceCatalog extends DataSourceCatalog {

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(LenientDataSourceCatalog.class.getName());
    
    // ISO date/time format for CAS.ProductReceivedTime 
		private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		
		// date/time format for database
		private SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");


    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public LenientDataSourceCatalog(DataSource ds, ValidationLayer valLayer,
				    boolean fieldId, int pageSize, long cacheUpdateMin, boolean productIdString, boolean orderedValues) {
    	
	super(ds, valLayer, fieldId, pageSize, cacheUpdateMin, productIdString, orderedValues);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#addMetadata(org.apache.oodt.cas.metadata.Metadata,
     *      org.apache.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void addMetadata(Metadata m, Product product)
            throws CatalogException {
    	
    		// replace "CAS.ProductId"
    		m.removeMetadata("CAS.ProductId");
    		m.addMetadata("CAS.ProductId", product.getProductId());
    	
    		// map containing metadata type (id, name) pairs
        Map<String, String> metadataTypes = getMetadataTypes(m, product);

        // loop over metadata types
        for (Map.Entry<String, String> metadataId : metadataTypes.entrySet()) {
        	String metadataName = metadataId.getValue();
        	
            List<String> values = m.getAllMetadata(metadataName);

            if (values == null) {
                LOG.log(Level.WARNING, "No Metadata specified for product ["
                        + product.getProductName() + "] for required field ["
                        + metadataName
                        + "]: Attempting to continue processing metadata");
                continue;
            }

          for (String value : values) {
            try {
              addMetadataValue(metadataId, product, value);
            } catch (Exception e) {
              LOG.log(Level.SEVERE, e.getMessage());
              LOG
                  .log(
                      Level.WARNING,
                      "Exception ingesting metadata. Error inserting field: ["
                      + metadataId
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
    
    // Utility method to return a map of metadata (field id, field name)
    private Map<String, String> getMetadataTypes(Metadata m, Product product) throws CatalogException {
    	
  		// map containing metadata type (id, name) pairs
      Map<String, String> metadataTypes = new LinkedHashMap<String,String>();

      if (getValidationLayer()!=null) {
      	// validation layer: add valid metadata elements 
        try {
            for (Element element : getValidationLayer().getElements(product.getProductType())) {
            	metadataTypes.put(element.getElementId(), element.getElementName());
            }
            
        } catch (ValidationLayerException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new CatalogException(
                    "ValidationLayerException when trying to obtain element list for product type: "
                            + product.getProductType().getName()
                            + ": Message: " + e.getMessage(), e);
        }
        
      } else {
      	
      	// no validation layer: add ALL metadata elements
      	// use (key, key) pairs (i.e. metadata id == metadata name)
      	for (String key : m.getAllKeys()) {
      		metadataTypes.put(key, key);
      	}
      	
      }
      
      return metadataTypes;
      
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.catalog.Catalog#addMetadata(org.apache.oodt.cas.metadata.Metadata,
     *      org.apache.oodt.cas.filemgr.structs.Product)
     */
    public synchronized void removeMetadata(Metadata m, Product product)
            throws CatalogException {
    	
  			// map containing metadata type (id, name) pairs
      	Map<String, String> metadataTypes = getMetadataTypes(m, product);
            
        // loop over metadata types
        for (Map.Entry<String, String> metadataId : metadataTypes.entrySet()) {
           	String metadataName = metadataId.getValue();
            	
            List<String> values = m.getAllMetadata(metadataName);

            if (values != null) {
              for (String value : values) {
                try {
                  removeMetadataValue(metadataId.getKey(), product, value);
                } catch (Exception e) {
                  LOG.log(Level.SEVERE, e.getMessage());
                  LOG
                      .log(
                          Level.WARNING,
                          "Exception removing metadata. Error deleting field: ["
                          + metadataId
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
		+ "WHERE product_id = '" + product.getProductId()+"'";
            if(this.orderedValues) {
              metadataSql += " ORDER BY pkey";
            }

            LOG.log(Level.FINE, "getMetadata: Executing: " + metadataSql);
            rs = statement.executeQuery(metadataSql);
            
            // parse SQL results
            m = populateProductMetadata(rs, product);

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
    
    /** Method to populate the product metadata from a SQL ResultSet. **/
    private Metadata populateProductMetadata(ResultSet rs, Product product) throws CatalogException, SQLException {
     
    	Metadata m = new Metadata();

      if (getValidationLayer()!=null) {
        
      	// validation layer: retrieve valid metadata elements
        List<Element> elements;

      	try {
            elements = getValidationLayer().getElements(product.getProductType());
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
      
      } else {
      	
      	// no validation layer - add all (name, value) pairs for this product_id query
        while (rs.next()) {
            // right now, we just support STRING
            String elemValue = rs.getString("metadata_value");
            String elemId = rs.getString("element_id");

            elemValue = (elemValue != null ? elemValue : "");
            m.addMetadata(elemId, elemValue);    
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
            	
            	  if (getValidationLayer()!=null) {
            	  	// validation layer: column "element_id" contains the element identifier (e.g. "urn:oodt:ProductReceivedTime")
	                elementIds.append(" AND (element_id = '" + this.getValidationLayer().getElementByName(elems.get(0))
                                                            .getElementId() + "'");
	                for (int i = 1; i < elems.size(); i++) {
                      elementIds.append(
                          " OR element_id = '" + this.getValidationLayer().getElementByName(elems.get(i)).getElementId()
                          + "'");
                    }
	                elementIds.append(")");
            	 
            	  } else {
            	  	// no validation layer: column "element_id" contains the element name (e.g. "CAS.ProductReceivedTime")
	                elementIds.append(" AND (element_id = '" + elems.get(0) + "'");
	                for (int i = 1; i < elems.size(); i++) {
                      elementIds.append(" OR element_id = '" + elems.get(i) + "'");
                    }
	                elementIds.append(")");
	                
            	  }
            	  
            }
            String metadataSql = "SELECT element_id,metadata_value FROM "
                    + product.getProductType().getName() + "_metadata"
		+ " WHERE product_id = " + quoteIt(product.getProductId()) + elementIds.toString();
            if(this.orderedValues) {
              metadataSql += " ORDER BY pkey";
            }

            LOG.log(Level.FINE, "getMetadata: Executing: " + metadataSql);
            rs = statement.executeQuery(metadataSql);

            // parse SQL results
            m = populateProductMetadata(rs, product);


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

    private synchronized void addMetadataValue(Map.Entry<String, String> key,
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
                valueClauseSql.append("(").append(quoteIt(product.getProductId())).append(", '").append(key)
                              .append("', '").append(value).append("')");
            } else {
                valueClauseSql.append("(").append(product.getProductId()).append(", ").append(key).append(", '")
                              .append(value).append("')");
            }

            String metaIngestSql = ("INSERT INTO " + metadataTable
                                    + " (product_id, element_id, metadata_value) ")
                    + valueClauseSql.toString();
            LOG
                    .log(Level.FINE, "addMetadataValue: Executing: "
                            + metaIngestSql);
            statement.execute(metaIngestSql);
            
            // synchronize CAS.ProductReceivedTime with products.product_datetime
            if (key.equals("CAS.ProductReceivedTime") && this.productIdString) {
            		// convert from "2012-12-18T09:00:14.068-08:00" --> "2012-12-18T09:00:14.068-0800" --> "2012-12-18T10:00:14"
            		String datetime = dbFormat.format( isoFormat.parse( value.replaceAll(":00$", "00") ));
            		String updateDateTimeSql = "UPDATE products SET product_datetime='"+datetime+"' WHERE product_id="+quoteIt(product.getProductId());
            		LOG.log(Level.FINE, "addMetadataValue: Executing: "+updateDateTimeSql);
            		statement.execute(updateDateTimeSql);
            }
            
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

    private synchronized void removeMetadataValue(String elementId,
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
                metRemoveSql += "ELEMENT_ID = '" + elementId
                        + "' AND ";
                metRemoveSql += "METADATA_VALUE = '" + value + "'";
            } else {
                metRemoveSql += "PRODUCT_ID = " + product.getProductId()
                        + " AND ";
                metRemoveSql += "ELEMENT_ID = " + elementId
                        + " AND ";
                metRemoveSql += "METADATA_VALUE = " + value;
            }

            LOG.log(Level.FINE, "removeMetadataValue: Executing: "
                    + metRemoveSql);
            statement.execute(metRemoveSql);
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

    /**
     * Overridden method from superclass to allow for null validation layer.
     */
    protected int getResultListSize(Query query, ProductType type)
            throws CatalogException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        int resultCount = 0;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String getProductSql;
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

                if (fieldIdStringFlag) {
                  if (getValidationLayer() != null) {
                    elementIdStr =
                        "'" + this.getValidationLayer().getElementByName(criteria.getElementName()).getElementId()
                        + "'";
                  } else {
                    elementIdStr = "'" + criteria.getElementName() + "'";
                  }
                } else {
                  if (getValidationLayer() != null) {
                    elementIdStr = this.getValidationLayer().getElementByName(criteria.getElementName()).getElementId();
                  } else {
                    elementIdStr = criteria.getElementName();
                  }
                }

                String clause;

                if (!gotFirstClause) {
                  clause = "(p.element_id = " + elementIdStr + " AND ";
                  if (criteria instanceof TermQueryCriteria) {
                    clause += " metadata_value LIKE '%"
                              + ((TermQueryCriteria) criteria).getValue()
                              + "%') ";
                  } else if (criteria instanceof RangeQueryCriteria) {
                    String startVal = ((RangeQueryCriteria) criteria)
                        .getStartValue();
                    String endVal = ((RangeQueryCriteria) criteria)
                        .getEndValue();
                    boolean inclusive = ((RangeQueryCriteria) criteria)
                        .getInclusive();

                    if ((startVal != null && !startVal.equals(""))
                        || (endVal != null && !endVal.equals(""))) {
                      clause += " metadata_value ";

                      boolean gotStart = false;

                      if (startVal != null && !startVal.equals("")) {
                        if (inclusive) {
                          clause += ">= '" + startVal + "'";
                        } else {
                          clause += "> '" + startVal + "'";
                        }
                        gotStart = true;
                      }

                      if (endVal != null && !endVal.equals("")) {
                        if (gotStart) {
                          if (inclusive) {
                            clause += " AND metadata_value <= '"
                                      + endVal + "'";
                          } else {
                            clause += " AND metadata_value < '"
                                      + endVal + "'";
                          }
                        } else if (inclusive) {
                          clause += "<= '" + endVal + "'";
                        } else {
                          clause += "< '" + endVal + "'";
                        }
                      }

                      clause += ") ";
                    }
                  }
                  whereClause.append(clause);
                  gotFirstClause = true;
                } else {
                  String subSelectTblName = "p" + clauseNum;
                  String subSelectQuery = subSelectQueryBase
                                          + "WHERE (element_id = " + elementIdStr
                                          + " AND ";
                  if (criteria instanceof TermQueryCriteria) {
                    subSelectQuery += " metadata_value LIKE '%"
                                      + ((TermQueryCriteria) criteria).getValue()
                                      + "%')";
                  } else if (criteria instanceof RangeQueryCriteria) {
                    String startVal = ((RangeQueryCriteria) criteria)
                        .getStartValue();
                    String endVal = ((RangeQueryCriteria) criteria)
                        .getEndValue();

                    if (startVal != null || endVal != null) {
                      subSelectQuery += " metadata_value ";

                      boolean gotStart = false;

                      if (startVal != null && !startVal.equals("")) {
                        subSelectQuery += ">= '" + startVal + "'";
                        gotStart = true;
                      }

                      if (endVal != null && !endVal.equals("")) {
                        if (gotStart) {
                          subSelectQuery += " AND metadata_value <= '"
                                            + endVal + "'";
                        } else {
                          subSelectQuery += "<= '" + endVal + "'";
                        }
                      }

                      subSelectQuery += ") ";

                    }
                  }

                  fromClause.append("INNER JOIN (").append(subSelectQuery).append(") ").append(subSelectTblName)
                            .append(" ON ").append(subSelectTblName).append(".product_id = p.product_id ");

                }
              }
            }

            getProductSql = "SELECT COUNT(DISTINCT p.product_id) AS numResults " + fromClause.toString();
            if (gotFirstClause) {
                getProductSql += whereClause.toString();
            }


            LOG.log(Level.FINE, "catalog get num results: executing: "
                    + getProductSql);

            rs = statement.executeQuery(getProductSql);

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
    
    /**
     * Overridden method from superclass to allow for null validation layer.
     */
    protected String getSqlQuery(QueryCriteria queryCriteria, ProductType type) throws ValidationLayerException, CatalogException {
      StringBuilder sqlQuery = new StringBuilder();
      if (queryCriteria instanceof BooleanQueryCriteria) {
          BooleanQueryCriteria bqc = (BooleanQueryCriteria) queryCriteria;
          if (bqc.getOperator() == BooleanQueryCriteria.NOT) {
          	  if (!this.productIdString) {
          	  	new StringBuilder("SELECT DISTINCT product_id FROM " + type.getName() + "_metadata WHERE product_id NOT IN "
                           + "(" + this.getSqlQuery(bqc.getTerms().get(0), type) + ")");
          	  } else {
               	new StringBuilder("SELECT DISTINCT products.product_id FROM products," + type.getName() + "_metadata"
               					 + " WHERE products.product_id="+type.getName() + "_metadata.product_id"
          	  			     + " AND products.product_id NOT IN (" + this.getSqlQuery(bqc.getTerms().get(0), type) +
                                  ")");
          	  }
          } else {
              sqlQuery.append("(").append(this.getSqlQuery(bqc.getTerms().get(0), type));
              String op = bqc.getOperator() == BooleanQueryCriteria.AND ? "INTERSECT" : "UNION";
              for (int i = 1; i < bqc.getTerms().size(); i++) {
                sqlQuery.append(") ").append(op).append(" (").append(this.getSqlQuery(bqc.getTerms().get(i), type));
              }
              sqlQuery.append(")");
          }
      }else {
      	  String elementIdStr = queryCriteria.getElementName();
      	  if (this.getValidationLayer()!=null) {
      	  	elementIdStr = this.getValidationLayer().getElementByName(queryCriteria.getElementName()).getElementId();
      	  }
          
          if (fieldIdStringFlag) {
            elementIdStr = "'" + elementIdStr + "'";
          }
          if (!this.productIdString) {
          	new StringBuilder("SELECT DISTINCT product_id FROM " + type.getName() + "_metadata WHERE element_id = " + elementIdStr +
               " AND ");
          } else {
          	new StringBuilder("SELECT DISTINCT products.product_id FROM products," + type.getName() + "_metadata"
          	         + " WHERE products.product_id="+type.getName() + "_metadata.product_id" 
          			     + " AND element_id = " + elementIdStr + " AND ");
          }
          if (queryCriteria instanceof TermQueryCriteria) {
              sqlQuery.append("metadata_value = '" + ((TermQueryCriteria) queryCriteria).getValue() + "'");
          } else if (queryCriteria instanceof RangeQueryCriteria) {
              RangeQueryCriteria rqc = (RangeQueryCriteria) queryCriteria;
              String rangeSubQuery = null;
              if (rqc.getStartValue() != null) {
                rangeSubQuery =
                    "metadata_value" + (rqc.getInclusive() ? " >= " : " > ") + "'" + rqc.getStartValue() + "'";
              }
              if (rqc.getEndValue() != null) {
                  if (rangeSubQuery == null) {
                    rangeSubQuery =
                        "metadata_value" + (rqc.getInclusive() ? " <= " : " < ") + "'" + rqc.getEndValue() + "'";
                  } else {
                    rangeSubQuery =
                        "(" + rangeSubQuery + " AND metadata_value" + (rqc.getInclusive() ? " <= " : " < ") + "'" + rqc
                            .getEndValue() + "')";
                  }
              }
              sqlQuery.append(rangeSubQuery);
          } else {
              throw new CatalogException("Invalid QueryCriteria [" + queryCriteria.getClass().getCanonicalName() + "]");
          }
      }

      return sqlQuery.toString();
  }

}
