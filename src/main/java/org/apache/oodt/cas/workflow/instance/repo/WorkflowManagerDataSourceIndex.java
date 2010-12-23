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
package org.apache.oodt.cas.workflow.instance.repo;

//JDK imports
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//SQL imports
import javax.sql.DataSource;

//OODT imports
import org.apache.oodt.cas.catalog.exception.CatalogIndexException;
import org.apache.oodt.cas.catalog.exception.IngestServiceException;
import org.apache.oodt.cas.catalog.exception.QueryServiceException;
import org.apache.oodt.cas.catalog.page.IndexPager;
import org.apache.oodt.cas.catalog.page.IngestReceipt;
import org.apache.oodt.cas.catalog.query.ComparisonQueryExpression;
import org.apache.oodt.cas.catalog.query.NotQueryExpression;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.query.QueryLogicalGroup;
import org.apache.oodt.cas.catalog.query.StdQueryExpression;
import org.apache.oodt.cas.catalog.struct.Index;
import org.apache.oodt.cas.catalog.struct.IngestService;
import org.apache.oodt.cas.catalog.struct.QueryService;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.struct.TransactionIdFactory;
import org.apache.oodt.cas.catalog.struct.impl.transaction.UuidTransactionIdFactory;
import org.apache.oodt.cas.catalog.term.Term;
import org.apache.oodt.cas.catalog.term.TermBucket;
import org.apache.oodt.commons.date.DateUtils;
import static org.apache.oodt.cas.workflow.metadata.WorkflowMetKeys.*;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A queriable index for querying for cas-workflow instance metadata
 * <p>
 */
public class WorkflowManagerDataSourceIndex implements Index, QueryService, IngestService {

	private static final Logger LOG = Logger.getLogger(WorkflowManagerDataSourceIndex.class.getName());
	
	protected DataSource dataSource;
	
	public WorkflowManagerDataSourceIndex(DataSource dataSource) throws InstantiationException {
		this.dataSource = dataSource;
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
		return new UuidTransactionIdFactory();
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
                String key = URLDecoder.decode(rs.getString("workflow_met_key"), "UTF-8");
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
			String sqlQuery = this.getSqlQuery(queryExpression);
	        LOG.log(Level.INFO, "Performing Query: " + sqlQuery);
			rs = stmt.executeQuery(sqlQuery);	

			Vector<String> workflowInstanceIds = new Vector<String>();
			while (rs.next()) 
				workflowInstanceIds.add(rs.getString("workflow_instance_id"));
			
			try {
				rs.close();
			}catch(Exception e) {}
			
			List<IngestReceipt> receipts = new Vector<IngestReceipt>();
			for (String workflowInstanceId : workflowInstanceIds) {
                String creationDate = null;
                try {
                	sqlQuery = "SELECT workflow_met_val FROM workflow_instance_metadata WHERE workflow_instance_id = '" + workflowInstanceId + "' AND workflow_met_key = '" + URLEncoder.encode(CREATION_DATE, "UTF-8") + "'";
        	        LOG.log(Level.INFO, "Performing Query: " + sqlQuery);
        	        rs = stmt.executeQuery(sqlQuery);
        	        if (rs.next()) {
        	        	creationDate = URLDecoder.decode(rs.getString("workflow_met_val"), "UTF-8");
        	        }else {
        	        	creationDate = DateUtils.toString(DateUtils.toUtc(Calendar.getInstance()));
        	        	LOG.log(Level.WARNING, "CreationDate not found for '" + workflowInstanceId + "' . . . db might be out of sync . . . fixing CreationDate to '" + creationDate + "'");
        	        }
                }catch (Exception e) {
                	throw new QueryServiceException("Failed to get CreationDate for InstanceId '" + workflowInstanceId + "' : " + e.getMessage(), e);
                }
                receipts.add(new IngestReceipt(new UuidTransactionIdFactory().createTransactionId(workflowInstanceId), DateUtils.toCalendar(creationDate, DateUtils.FormatType.UTC_FORMAT).getTime()));
            }
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
	
	public List<IngestReceipt> query(QueryExpression queryExpression,
			int startIndex, int endIndex) throws QueryServiceException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.createStatement();
			String sqlQuery = this.getSqlQuery(queryExpression);
	        LOG.log(Level.INFO, "Performing Query: " + sqlQuery);
			rs = stmt.executeQuery(sqlQuery);	

			Vector<String> workflowInstanceIds = new Vector<String>();
			while (rs.next()) 
				workflowInstanceIds.add(rs.getString("workflow_instance_id"));
			
			try {
				rs.close();
			}catch(Exception e) {}
			
			List<IngestReceipt> receipts = new Vector<IngestReceipt>();
			for (int i = startIndex; i < endIndex; i++) {
				String workflowInstanceId = workflowInstanceIds.get(i);
                String creationDate = null;
                try {
                	sqlQuery = "SELECT workflow_met_val FROM workflow_instance_metadata WHERE workflow_instance_id = '" + workflowInstanceId + "' AND workflow_met_key = '" + URLEncoder.encode(CREATION_DATE, "UTF-8") + "'";
        	        LOG.log(Level.INFO, "Performing Query: " + sqlQuery);
        	        rs = stmt.executeQuery(sqlQuery);
        	        if (rs.next()) {
        	        	creationDate = URLDecoder.decode(rs.getString("workflow_met_val"), "UTF-8");
        	        }else {
        	        	creationDate = DateUtils.toString(DateUtils.toUtc(Calendar.getInstance()));
        	        	LOG.log(Level.WARNING, "CreationDate not found for '" + workflowInstanceId + "' . . . db might be out of sync . . . fixing CreationDate to '" + creationDate + "'");
        	        }
                }catch (Exception e) {
                	throw new QueryServiceException("Failed to get CreationDate for InstanceId '" + workflowInstanceId + "' : " + e.getMessage(), e);
                }
                receipts.add(new IngestReceipt(new UuidTransactionIdFactory().createTransactionId(workflowInstanceId), DateUtils.toCalendar(creationDate, DateUtils.FormatType.UTC_FORMAT).getTime()));
            }
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
			String sqlQuery = this.getSqlQuery(queryExpression);
	        LOG.log(Level.INFO, "Performing Query: " + sqlQuery);
			rs = stmt.executeQuery(sqlQuery);	

			int numWorkflowInstances = 0;
            while (rs.next())
            	numWorkflowInstances = rs.getInt("workflow_instance_id");
			
            return numWorkflowInstances;
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

    private String getSqlQuery(QueryExpression queryExpression) throws QueryServiceException, UnsupportedEncodingException {
        String sqlQuery = null;
        if (queryExpression instanceof QueryLogicalGroup) {
        	QueryLogicalGroup qlg = (QueryLogicalGroup) queryExpression;
            sqlQuery = "(" + this.getSqlQuery(qlg.getExpressions().get(0));
            String op = qlg.getOperator() == QueryLogicalGroup.Operator.AND ? "INTERSECT" : "UNION";
            for (int i = 1; i < qlg.getExpressions().size(); i++) 
                sqlQuery += ") " + op + " (" + this.getSqlQuery(qlg.getExpressions().get(i));
            sqlQuery += ")";
        }else if (queryExpression instanceof ComparisonQueryExpression){
        	ComparisonQueryExpression cqe = (ComparisonQueryExpression) queryExpression;
        	String operator = null;
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
            
            sqlQuery = "SELECT DISTINCT workflow_instance_id FROM workflow_instance_metadata WHERE workflow_met_key = '" + URLEncoder.encode(cqe.getTerm().getName(), "UTF-8") + "' AND (";
        	for (int i = 0; i < cqe.getTerm().getValues().size(); i++) {
        		String value = cqe.getTerm().getValues().get(i);
                sqlQuery += "workflow_met_val " + operator + " '" + URLEncoder.encode(value, "UTF-8") + "'";
	            if ((i + 1) < cqe.getTerm().getValues().size())
	            	sqlQuery += "OR";
        	}
        	sqlQuery += ")";
        }else if (queryExpression instanceof NotQueryExpression) {
        	NotQueryExpression nqe = (NotQueryExpression) queryExpression;
            sqlQuery = "SELECT DISTINCT workflow_instance_id FROM workflow_instance_metadata WHERE NOT (" + this.getSqlQuery(nqe.getQueryExpression()) + ")";
        }else if (queryExpression instanceof StdQueryExpression) {
            sqlQuery = "SELECT DISTINCT workflow_instance_id FROM workflow_instance_metadata";
        }else {
            throw new QueryServiceException("Invalid QueryExpression '" + queryExpression.getClass().getCanonicalName() + "'");
        }
        return sqlQuery;
    }

	public boolean delete(TransactionId<?> transactionId)
			throws IngestServiceException {
	    Connection conn = null;
		Statement statement = null;

		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			statement = conn.createStatement();

			String deleteSql = "DELETE FROM workflow_instance_metadata "
					+ "WHERE workflow_instance_id = '" + transactionId + "'";

			LOG.log(Level.FINE, "sql: Executing: " + deleteSql);
			statement.execute(deleteSql);
			conn.commit();
			return true;
		} catch (Exception e) {
			LOG.log(Level.WARNING,
					"Exception removing workflow instance metadata. Message: "
							+ e.getMessage());
			try {
				conn.rollback();
			} catch (SQLException e2) {
				LOG.log(Level.SEVERE,
						"Unable to rollback removeWorkflowInstanceMetadata "
								+ "transaction. Message: " + e2.getMessage());
			}
			throw new IngestServiceException(e.getMessage(), e);
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

	public IngestReceipt ingest(List<TermBucket> termBuckets) throws IngestServiceException {
        try {
        	String creationDate = null;
			TransactionId<?> transactionId = this.getTransactionIdFactory().createNewTransactionId(); 
	        for (TermBucket termBucket : termBuckets) {
				for (Term term : termBucket.getTerms()) {
					if (term.getName().equals(CREATION_DATE))
						creationDate = term.getFirstValue();
					for (String val : term.getValues()) {
						Connection conn = null;
						Statement statement = null;
	
						try {
							conn = dataSource.getConnection();
							conn.setAutoCommit(false);
							statement = conn.createStatement();
							String addMetSql = "INSERT INTO workflow_instance_metadata"
									+ " (workflow_instance_id,workflow_met_key,workflow_met_val) VALUES ('"
									+ transactionId
									+ "','"
									+ URLEncoder.encode(term.getName(), "UTF-8")
									+ "','"
									+ URLEncoder.encode(val, "UTF-8") + "')";
	
							LOG.log(Level.FINE, "sql: Executing: " + addMetSql);
							statement.execute(addMetSql);
	
							conn.commit();
						} catch (Exception e) {
							LOG.log(Level.WARNING, "Exception adding metadata ["
									+ term.getName() + "=>" + val
									+ "] to workflow inst: [" + transactionId
									+ "]. Message: " + e.getMessage(), e);
							try {
								conn.rollback();
							} catch (SQLException e2) {
								LOG.log(Level.SEVERE,
										"Unable to rollback addMetadataValue transaction. Message: "
												+ e2.getMessage(), e);
							}
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
				}
			}
			return new IngestReceipt(transactionId, DateUtils.toCalendar(creationDate, DateUtils.FormatType.UTC_FORMAT).getTime());
		} catch (Exception e) {
			throw new IngestServiceException("Failed to ingest : " + e.getMessage(), e);
		}
	}

	public boolean reduce(TransactionId<?> transactionId,
			List<TermBucket> termBuckets) throws IngestServiceException {
	    Connection conn = null;
	    Statement statement = null;

		try {
			for (TermBucket bucket: termBuckets) {
				for (Term term: bucket.getTerms()) {
					for (String val: term.getValues()) {
						conn = dataSource.getConnection();
						conn.setAutoCommit(false);
						statement = conn.createStatement();
	
						String deleteSql = "DELETE FROM workflow_instance_metadata "
								+ "WHERE workflow_instance_id = '"
								+ transactionId
								+ "' AND workflow_met_key = '"
								+ URLEncoder.encode(term.getName(), "UTF-8")
								+ "' AND workflow_met_val = '"
								+ URLEncoder.encode(val, "UTF-8") + "'";
	
						LOG.log(Level.FINE, "sql: Executing: " + deleteSql);
						statement.execute(deleteSql);
						conn.commit();	
					}
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.log(Level.WARNING,
					"Exception removing workflow instance metadata. Message: "
							+ e.getMessage());
			try {
				conn.rollback();
			} catch (SQLException e2) {
				LOG.log(Level.SEVERE,
						"Unable to rollback removeWorkflowInstanceMetadata "
								+ "transaction. Message: " + e2.getMessage());
			}
			throw new IngestServiceException(e.getMessage());
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

	public IngestReceipt update(TransactionId<?> transactionId,
			List<TermBucket> termBuckets) throws IngestServiceException {
		this.delete(transactionId);
		return this.ingest(termBuckets);
	}

}
