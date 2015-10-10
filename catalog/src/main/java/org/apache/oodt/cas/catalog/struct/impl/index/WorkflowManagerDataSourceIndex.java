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
package org.apache.oodt.cas.catalog.struct.impl.index;

//JDK imports
import org.apache.oodt.cas.catalog.exception.CatalogIndexException;
import org.apache.oodt.cas.catalog.exception.QueryServiceException;
import org.apache.oodt.cas.catalog.page.IndexPager;
import org.apache.oodt.cas.catalog.page.IngestReceipt;
import org.apache.oodt.cas.catalog.query.*;
import org.apache.oodt.cas.catalog.struct.Index;
import org.apache.oodt.cas.catalog.struct.QueryService;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.struct.TransactionIdFactory;
import org.apache.oodt.cas.catalog.struct.impl.transaction.LongTransactionIdFactory;
import org.apache.oodt.cas.catalog.term.Term;
import org.apache.oodt.cas.catalog.term.TermBucket;
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;
import org.apache.oodt.commons.util.DateConvert;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

//SQL imports
//OODT imports
//EDA imports

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A queriable index for querying for original cas-workflow instance metadata (not for cas-workflow2)
 * <p>
 */
public class WorkflowManagerDataSourceIndex implements Index, QueryService {

	private static final Logger LOG = Logger.getLogger(WorkflowManagerDataSourceIndex.class.getName());
	
	protected DataSource dataSource;
	
	public WorkflowManagerDataSourceIndex(String user, String pass, String driver, String jdbcUrl) throws InstantiationException {
		this.dataSource = DatabaseConnectionBuilder.buildDataSource(user, pass, driver, jdbcUrl);
	}
	
	public List<TransactionId<?>> getPage(IndexPager indexPage)
			throws CatalogIndexException {
		// TODO Auto-generated method stub
		return null;
	}

	public Properties getProperties() throws CatalogIndexException {
		return new Properties();
	}

	public String getProperty(String key) throws CatalogIndexException {
		return null;
	}

	public TransactionIdFactory getTransactionIdFactory() throws CatalogIndexException {
		return new LongTransactionIdFactory();
	}

	public boolean hasTransactionId(TransactionId<?> transactionId)
			throws CatalogIndexException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT DISTINCT workflow_instance_id FROM workflow_instance_metadata WHERE workflow_instance_id = '" + transactionId + "'");	
			return rs.next();
		}catch (Exception e) {
			throw new CatalogIndexException("Failed to check for workflow id '" + transactionId + "' : " + e.getMessage(), e);
		}finally {
			try {
				conn.close();
			}catch(Exception e) {}
			try {
				stmt.close();
			}catch(Exception e) {}
			try {
				rs.close();
			}catch(Exception e) {}
		}
	}

	public List<TermBucket> getBuckets(TransactionId<?> transactionId)
			throws QueryServiceException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM workflow_instance_metadata WHERE workflow_instance_id = '" + transactionId + "'");	
			
			TermBucket tb = new TermBucket("Workflows");
			while (rs.next()) {
                String key = rs.getString("workflow_met_key");
                String value = URLDecoder.decode(rs.getString("workflow_met_val"), "UTF-8");
                tb.addTerm(new Term(key, Collections.singletonList(value)));
            }
			return Collections.singletonList(tb);
		}catch (Exception e) {
			throw new QueryServiceException("Failed to get Workflow Instance Metadata for workflow id '" + transactionId + "' : " + e.getMessage(), e);
		}finally {
			try {
				conn.close();
			}catch(Exception e) {}
			try {
				stmt.close();
			}catch(Exception e) {}
			try {
				rs.close();
			}catch(Exception e) {}
		}
	}

	public Map<TransactionId<?>, List<TermBucket>> getBuckets(
			List<TransactionId<?>> transactionIds) throws QueryServiceException {
		Map<TransactionId<?>, List<TermBucket>> returnMap = new HashMap<TransactionId<?>, List<TermBucket>>();
		for (TransactionId<?> transactionId : transactionIds) 
			returnMap.put(transactionId, this.getBuckets(transactionId));
		return returnMap;
	}

	public List<IngestReceipt> query(QueryExpression queryExpression)
			throws QueryServiceException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.createStatement();
			String sqlQuery = "SELECT workflow_instance_id,start_date_time FROM workflow_instances WHERE workflow_instance_id IN (" + this.getSqlQuery(queryExpression) + ")";
	        LOG.log(Level.INFO, "Performing Query: " + sqlQuery);
			rs = stmt.executeQuery(sqlQuery);
			
			List<IngestReceipt> receipts = new Vector<IngestReceipt>();
			while (rs.next()) 
                receipts.add(new IngestReceipt(new LongTransactionIdFactory().createTransactionId(rs.getString("workflow_instance_id")), DateConvert.isoParse(rs.getString("start_date_time"))));
			return receipts;
		}catch (Exception e) {
			throw new QueryServiceException("Failed to query Workflow Instances Database : " + e.getMessage(), e);
		}finally {
			try {
				conn.close();
			}catch(Exception e) {}
			try {
				stmt.close();
			}catch(Exception e) {}
			try {
				rs.close();
			}catch(Exception e) {}
		}
	}
	
	public List<IngestReceipt> query(QueryExpression queryExpression, int startIndex, int endIndex) throws QueryServiceException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.createStatement();
			String sqlQuery = "SELECT workflow_instance_id,start_date_time FROM workflow_instances WHERE workflow_instance_id IN (" + this.getSqlQuery(queryExpression) + ")";
	        LOG.log(Level.INFO, "Performing Query: " + sqlQuery);
			rs = stmt.executeQuery(sqlQuery);
			
			List<IngestReceipt> receipts = new Vector<IngestReceipt>();
			int index = 0;
			while (startIndex > index && rs.next()) index++;
			while (rs.next() && index++ <= endIndex) 
				receipts.add(new IngestReceipt(new LongTransactionIdFactory().createTransactionId(rs.getString("workflow_instance_id")), DateConvert.isoParse(rs.getString("start_date_time"))));
			return receipts;
		}catch (Exception e) {
			throw new QueryServiceException("Failed to query Workflow Instances Database : " + e.getMessage(), e);
		}finally {
			try {
				conn.close();
			}catch(Exception e) {}
			try {
				stmt.close();
			}catch(Exception e) {}
			try {
				rs.close();
			}catch(Exception e) {}
		}
	}
	
	public int sizeOf(QueryExpression queryExpression)
			throws QueryServiceException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.createStatement();
			String sqlQuery = "SELECT COUNT(workflow_instance_id) AS numInstances FROM workflow_instances WHERE workflow_instance_id IN (" + this.getSqlQuery(queryExpression) + ")";
			LOG.log(Level.INFO, "Performing Query: " + sqlQuery);
			rs = stmt.executeQuery(sqlQuery);

			int numInstances = 0;
			while (rs.next())
				numInstances = rs.getInt("numInstances");

			return numInstances;
		} catch (Exception e) {
			throw new QueryServiceException(
					"Failed to get size of query in Workflow Instances Database : "
							+ e.getMessage(), e);
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
			}
			try {
				stmt.close();
			} catch (Exception e) {
			}
			try {
				rs.close();
			} catch (Exception e) {
			}
		}
	}
	
    private String getSqlQuery(QueryExpression queryExpression) throws QueryServiceException, UnsupportedEncodingException {
        StringBuilder sqlQuery = new StringBuilder();
        if (queryExpression instanceof QueryLogicalGroup) {
        	QueryLogicalGroup qlg = (QueryLogicalGroup) queryExpression;
            sqlQuery.append("(").append(this.getSqlQuery(qlg.getExpressions().get(0)));
            String op = qlg.getOperator() == QueryLogicalGroup.Operator.AND ? "INTERSECT" : "UNION";
            for (int i = 1; i < qlg.getExpressions().size(); i++) 
                sqlQuery.append(") ").append(op).append(" (").append(this.getSqlQuery(qlg.getExpressions().get(i)));
            sqlQuery.append(")");
        }else if (queryExpression instanceof ComparisonQueryExpression){
        	ComparisonQueryExpression cqe = (ComparisonQueryExpression) queryExpression;
        	String operator;
            if (cqe.getOperator().equals(ComparisonQueryExpression.Operator.EQUAL_TO)) {
            	operator = "=";
            } else if (cqe.getOperator().equals(ComparisonQueryExpression.Operator.GREATER_THAN)) {
            	operator = ">";
            } else if (cqe.getOperator().equals(ComparisonQueryExpression.Operator.GREATER_THAN_EQUAL_TO)) {
            	operator = ">=";
            } else if (cqe.getOperator().equals(ComparisonQueryExpression.Operator.LESS_THAN)) {
            	operator = "<";
            } else if (cqe.getOperator().equals(ComparisonQueryExpression.Operator.LESS_THAN_EQUAL_TO)) {
            	operator = "<=";
            } else {
                throw new QueryServiceException("Invalid ComparisonQueryExpression Operator '" + cqe.getOperator() + "'");
            }
            
            sqlQuery.append(
				"SELECT DISTINCT workflow_instance_id FROM workflow_instance_metadata WHERE " + "workflow_met_key = '")
					.append(cqe.getTerm().getName()).append("' AND (");
        	for (int i = 0; i < cqe.getTerm().getValues().size(); i++) {
        		String value = cqe.getTerm().getValues().get(i);
                sqlQuery.append("workflow_met_val ").append(operator).append(" '")
						.append(URLEncoder.encode(value, "UTF-8")).append("'");
	            if ((i + 1) < cqe.getTerm().getValues().size())
	            	sqlQuery.append("OR");
        	}
        	sqlQuery.append(")");
        }else if (queryExpression instanceof NotQueryExpression) {
        	NotQueryExpression nqe = (NotQueryExpression) queryExpression;
            sqlQuery.append("SELECT DISTINCT workflow_instance_id FROM workflow_instance_metadata WHERE NOT (")
					.append(this
						.getSqlQuery(nqe.getQueryExpression())).append(")");
        }else if (queryExpression instanceof StdQueryExpression) {
            sqlQuery.append("SELECT DISTINCT workflow_instance_id FROM workflow_instance_metadata");
        }else {
            throw new QueryServiceException("Invalid QueryExpression '" + queryExpression.getClass().getCanonicalName() + "'");
        }
        return sqlQuery.toString();
    }

}
