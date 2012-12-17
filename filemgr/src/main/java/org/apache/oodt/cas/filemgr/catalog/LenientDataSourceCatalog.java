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

//JDK imports
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
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

    /**
     * <p>
     * Default Constructor
     * </p>.
     * @throws  
     */
    public LenientDataSourceCatalog(DataSource ds, ValidationLayer valLayer,
            boolean fieldId, int pageSize, long cacheUpdateMin, boolean productIdString) {
    	
    		super(ds, valLayer, fieldId, pageSize, cacheUpdateMin, productIdString);

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
        for (String metadataId : metadataTypes.keySet()) {
        	String metadataName = metadataTypes.get(metadataId);
        	
            List<String> values = m.getAllMetadata(metadataName);

            if (values == null) {
                LOG.log(Level.WARNING, "No Metadata specified for product ["
                        + product.getProductName() + "] for required field ["
                        + metadataName
                        + "]: Attempting to continue processing metadata");
                continue;
            }

            for (Iterator<String> j = values.iterator(); j.hasNext();) {
                String value = j.next();

                try {
                    addMetadataValue(metadataId, product, value);
                } catch (Exception e) {
                    e.printStackTrace();
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
            e.printStackTrace();
            throw new CatalogException(
                    "ValidationLayerException when trying to obtain element list for product type: "
                            + product.getProductType().getName()
                            + ": Message: " + e.getMessage());
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
        for (String metadataId : metadataTypes.keySet()) {
           	String metadataName = metadataTypes.get(metadataId);
            	
            List<String> values = m.getAllMetadata(metadataName);

            if (values != null) {
                for (Iterator<String> j = values.iterator(); j.hasNext();) {
                    String value = j.next();

                    try {
                        removeMetadataValue(metadataId, product, value);
                    } catch (Exception e) {
                        e.printStackTrace();
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
                    + " WHERE product_id = '" + product.getProductId()+"'";

            LOG.log(Level.FINE, "getMetadata: Executing: " + metadataSql);
            rs = statement.executeQuery(metadataSql);
            
            // parse SQL results
            m = populateProductMetadata(rs, product);

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
    
    /** Method to populate the product metadata from a SQL ResultSet. **/
    private Metadata populateProductMetadata(ResultSet rs, Product product) throws CatalogException, SQLException {
     
    	Metadata m = new Metadata();

      if (getValidationLayer()!=null) {
        
      	// validation layer: retrieve valid metadata elements
        List<Element> elements = null;

      	try {
            elements = getValidationLayer().getElements(product.getProductType());
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

            String elementIds = "";
            if (elems.size() > 0) {
            	
            	  if (getValidationLayer()!=null) {
            	  	// validation layer: column "element_id" contains the element identifier (e.g. "urn:oodt:ProductReceivedTime")
	                elementIds += " AND (element_id = '" + this.getValidationLayer().getElementByName(elems.get(0)).getElementId() + "'";
	                for (int i = 1; i < elems.size(); i++) 
	                    elementIds += " OR element_id = '" + this.getValidationLayer().getElementByName(elems.get(i)).getElementId() + "'";
	                elementIds += ")";
            	 
            	  } else {
            	  	// no validation layer: column "element_id" contains the element name (e.g. "CAS.ProductReceivedTime")
	                elementIds += " AND (element_id = '" + elems.get(0) + "'";
	                for (int i = 1; i < elems.size(); i++) 
	                    elementIds += " OR element_id = '" + elems.get(i) + "'";
	                elementIds += ")";
	                
            	  }
            	  
            }
            String metadataSql = "SELECT element_id,metadata_value FROM "
                    + product.getProductType().getName() + "_metadata"
                    + " WHERE product_id = " + quoteIt(product.getProductId()) + elementIds;

            LOG.log(Level.FINE, "getMetadata: Executing: " + metadataSql);
            rs = statement.executeQuery(metadataSql);

            // parse SQL results
            m = populateProductMetadata(rs, product);


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

    private synchronized void addMetadataValue(String key,
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
                valueClauseSql.append("(" + quoteIt(product.getProductId()) + ", '"
                        + key + "', '" + value + "')");
            } else {
                valueClauseSql.append("(" + product.getProductId() + ", "
                        + key + ", '" + value + "')");
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

            String getProductSql = "";
            String tableName = type.getName() + "_metadata";
            String subSelectQueryBase = "SELECT product_id FROM " + tableName
                    + " ";
            StringBuffer selectClause = new StringBuffer(
                    "SELECT COUNT(DISTINCT p.product_id) AS numResults ");
            StringBuffer fromClause = new StringBuffer("FROM " + tableName
                    + " p ");
            StringBuffer whereClause = new StringBuffer("WHERE ");

            boolean gotFirstClause = false;
            int clauseNum = 0;

            if (query.getCriteria() != null && query.getCriteria().size() > 0) {
                for (Iterator<QueryCriteria> i = query.getCriteria().iterator(); i.hasNext();) {
                    QueryCriteria criteria = i.next();
                    clauseNum++;

                    String elementIdStr = null;

                    if (fieldIdStringFlag) {
                    		if (getValidationLayer()!=null) {
                    			elementIdStr = "'" + this.getValidationLayer().getElementByName(criteria.getElementName()).getElementId() + "'";
                    		} else {
                    			elementIdStr = "'" + criteria.getElementName() + "'";
                    		}
                    } else {
                    	if (getValidationLayer()!=null) {
                        elementIdStr = this.getValidationLayer().getElementByName(criteria.getElementName()).getElementId();
                    	} else {
                    		elementIdStr = criteria.getElementName();
                    	}
                    }

                    String clause = null;

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
                                    if (inclusive)
                                        clause += ">= '" + startVal + "'";
                                    else
                                        clause += "> '" + startVal + "'";
                                    gotStart = true;
                                }

                                if (endVal != null && !endVal.equals("")) {
                                    if (gotStart) {
                                        if (inclusive)
                                            clause += " AND metadata_value <= '"
                                                    + endVal + "'";
                                        else
                                            clause += " AND metadata_value < '"
                                                    + endVal + "'";
                                    } else if (inclusive)
                                        clause += "<= '" + endVal + "'";
                                    else
                                        clause += "< '" + endVal + "'";
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
                                    } else
                                        subSelectQuery += "<= '" + endVal + "'";
                                }

                                subSelectQuery += ") ";

                            }
                        }

                        fromClause.append("INNER JOIN (" + subSelectQuery
                                + ") " + subSelectTblName + " ON "
                                + subSelectTblName
                                + ".product_id = p.product_id ");

                    }
                }
            }

            getProductSql = selectClause.toString() + fromClause.toString();
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
            e.printStackTrace();
            LOG.log(Level.WARNING,
                    "Exception performing get num results. Message: "
                            + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback get num results transaction. Message: "
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

        return resultCount;
    }
    
    /**
     * Overridden method from superclass to allow for null validation layer.
     */
    protected String getSqlQuery(QueryCriteria queryCriteria, ProductType type) throws ValidationLayerException, CatalogException {
      String sqlQuery = null;
      if (queryCriteria instanceof BooleanQueryCriteria) {
          BooleanQueryCriteria bqc = (BooleanQueryCriteria) queryCriteria;
          if (bqc.getOperator() == BooleanQueryCriteria.NOT) {
          	  if (!this.productIdString) {
          	  	sqlQuery = "SELECT DISTINCT product_id FROM " + type.getName() + "_metadata WHERE product_id NOT IN (" + this.getSqlQuery(bqc.getTerms().get(0), type) + ")";
          	  } else {
               	sqlQuery = "SELECT DISTINCT products.product_id FROM products," + type.getName() + "_metadata"
               					 + " WHERE products.product_id="+type.getName() + "_metadata.product_id" 
          	  			     + " AND products.product_id NOT IN (" + this.getSqlQuery(bqc.getTerms().get(0), type) + ")";
          	  }
          } else {
              sqlQuery = "(" + this.getSqlQuery(bqc.getTerms().get(0), type);
              String op = bqc.getOperator() == BooleanQueryCriteria.AND ? "INTERSECT" : "UNION";
              for (int i = 1; i < bqc.getTerms().size(); i++) 
                  sqlQuery += ") " + op + " (" + this.getSqlQuery(bqc.getTerms().get(i), type);
              sqlQuery += ")";
          }
      }else {
      	  String elementIdStr = queryCriteria.getElementName();
      	  if (this.getValidationLayer()!=null) {
      	  	elementIdStr = this.getValidationLayer().getElementByName(queryCriteria.getElementName()).getElementId();
      	  }
          
          if (fieldIdStringFlag) 
              elementIdStr = "'" + elementIdStr + "'";
          if (!this.productIdString) {
          	sqlQuery = "SELECT DISTINCT product_id FROM " + type.getName() + "_metadata WHERE element_id = " + elementIdStr + " AND ";
          } else {
          	sqlQuery = "SELECT DISTINCT products.product_id FROM products," + type.getName() + "_metadata"
          	         + " WHERE products.product_id="+type.getName() + "_metadata.product_id" 
          			     + " AND element_id = " + elementIdStr + " AND ";
          }
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

}
