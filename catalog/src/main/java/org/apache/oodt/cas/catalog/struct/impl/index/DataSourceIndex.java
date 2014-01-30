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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
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
import org.apache.commons.lang.StringUtils;
import org.apache.oodt.cas.catalog.exception.CatalogIndexException;
import org.apache.oodt.cas.catalog.exception.IngestServiceException;
import org.apache.oodt.cas.catalog.exception.QueryServiceException;
import org.apache.oodt.cas.catalog.page.IndexPager;
import org.apache.oodt.cas.catalog.page.IngestReceipt;
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.catalog.page.ProcessedPageInfo;
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
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;
import org.apache.oodt.commons.date.DateUtils;

/**
 * 
 * DataSource Indexer which supports both ingest and query
 * 
 * @author bfoster
 * @version $Revision$
 *
 */
public class DataSourceIndex implements Index, IngestService, QueryService {

	private static final Logger LOG = Logger.getLogger(DataSourceIndex.class.getName());
	
	protected DataSource dataSource;
	protected boolean useUTF8;
	
	public DataSourceIndex(String user, String pass, String driver, String jdbcUrl, boolean useUTF8) {
		this.dataSource = DatabaseConnectionBuilder.buildDataSource(user, pass, driver, jdbcUrl);
		this.useUTF8 = useUTF8;
	}
	
	public IndexPager getPager(PageInfo pageInfo) throws CatalogIndexException {
		return new IndexPager(new ProcessedPageInfo(pageInfo.getPageSize(), pageInfo.getPageNum(), this.getNumOfTransactions()));
	}
	
	protected int getNumOfTransactions() throws CatalogIndexException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT COUNT(transaction_id) AS numTransIds FROM transactions");
			if (rs.next())
				return rs.getInt("numTransIds");
			else
				throw new Exception("Failed to query for number of transactions");
		}catch (Exception e) {
			throw new CatalogIndexException("Failed to get number of transactions : " + e.getMessage(), e);
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

	/**
	 * {@inheritDoc}
	 */
	public List<TransactionId<?>> getPage(IndexPager indexPage) throws CatalogIndexException {
//		Connection conn = null;
//		Statement stmt = null;
//		ResultSet rs = null;
//		try {
//			conn = this.dataSource.getConnection();
//			stmt = conn.createStatement();
//			rs = stmt.executeQuery("SELECT transaction_id,transaction_class,transaction_date FROM transactions");
//			int startLoc = pager.getPageNum() * pager.getPageSize();
//			int endLoc = startLoc + pager.getPageSize();
//			List<IngestReceipt> receipts = new Vector<IngestReceipt>();
//			for (int i = startLoc; i < endLoc && rs.next(); i++) {
//				receipts.add(new IngestReceipt(((TransactionId<?>) Class.forName(rs.getString("transaction_class")).getConstructor(String.class).newInstance(rs.getString("transaction_id"))), DateUtils.toCalendar(rs.getString("transaction_date"), DateUtils.FormatType.LOCAL_FORMAT).getTime()));
//			}
//			return rs.next();
//		}catch (Exception e) {
//			throw new CatalogIndexException("Failed to check for transaction id '" + transactionId + "' : " + e.getMessage(), e);
//		}finally {
//			try {
//				conn.close();
//			}catch(Exception e) {}
//			try {
//				stmt.close();
//			}catch(Exception e) {}
//			try {
//				rs.close();
//			}catch(Exception e) {}
//		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Properties getProperties() throws CatalogIndexException {
		return new Properties();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getProperty(String key) throws CatalogIndexException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public TransactionIdFactory getTransactionIdFactory()
			throws CatalogIndexException {
		return new UuidTransactionIdFactory();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasTransactionId(TransactionId<?> transactionId)
			throws CatalogIndexException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT DISTINCT transaction_id FROM transactions WHERE transaction_id = '" + transactionId + "'");
			return rs.next();
		}catch (Exception e) {
			throw new CatalogIndexException("Failed to check for transaction id '" + transactionId + "' : " + e.getMessage(), e);
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

	/**
	 * {@inheritDoc}
	 */
	public boolean delete(TransactionId<?> transactionId)
			throws IngestServiceException {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.createStatement();
			stmt.execute("DELETE FROM transactions WHERE transaction_id = '" + transactionId + "'");
			stmt.execute("DELETE FROM transaction_terms WHERE transaction_id = '" + transactionId + "'");
			conn.commit();
			return true;
		}catch (Exception e) {
			throw new IngestServiceException("Failed to delete transaction id '" + transactionId + "' : " + e.getMessage(), e);
		}finally {
			try {
				conn.close();
			}catch(Exception e) {}
			try {
				stmt.close();
			}catch(Exception e) {}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IngestReceipt ingest(List<TermBucket> termBuckets) throws IngestServiceException {
		Connection conn = null;
		Statement stmt = null;
		TransactionId<?> catalogTransactionId = null;
		try {
			catalogTransactionId = this.getTransactionIdFactory().createNewTransactionId();
			conn = this.dataSource.getConnection();
			stmt = conn.createStatement();
			Calendar calendar = DateUtils.getCurrentLocalTime();
			stmt.execute("INSERT INTO transactions VALUES ('" + catalogTransactionId + "','" + DateUtils.toString(calendar) + "')");
			for (TermBucket termBucket : termBuckets) {
				for (Term term : termBucket.getTerms()) {
					for (String value : term.getValues()) {
						try {
							stmt.execute("INSERT INTO transaction_terms VALUES ('" + catalogTransactionId + "','" + termBucket.getName() + "','" + term.getName() + "','" + (this.useUTF8 ? URLEncoder.encode(value, "UTF8") : value) + "')");
						}catch (Exception e) {
							LOG.log(Level.WARNING, "Failed to ingest term: '" + catalogTransactionId + "','" + termBucket.getName() + "','" + term.getName() + "','" + value + "'");
						}
					}
				}
			}
			conn.commit();
			return new IngestReceipt(catalogTransactionId, calendar.getTime());
		}catch (Exception e) {
			throw new IngestServiceException("Failed to ingest metadata for transaction id '" + catalogTransactionId + "' : " + e.getMessage(), e);
		}finally {
			try {
				conn.close();
			}catch(Exception e) {}
			try {
				stmt.close();
			}catch(Exception e) {}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean reduce(TransactionId<?> transactionId,
			List<TermBucket> termBuckets) throws IngestServiceException {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.createStatement();
			for (TermBucket termBucket : termBuckets) 
				for (Term term : termBucket.getTerms()) 
					for (String value : term.getValues()) 
						try {
							stmt.execute("DELETE FROM transaction_terms WHERE transaction_id = '" + transactionId + "' AND bucket_name = '" + termBucket.getName() + "' AND term_name = '" + term.getName() + "' AND term_value = '" + (this.useUTF8 ? URLEncoder.encode(value, "UTF8") : value) + "'");
						}catch (Exception e) {
							LOG.log(Level.WARNING, "Failed to delete term: '" + transactionId + "','" + termBucket.getName() + "','" + term.getName() + "','" + value + "'");
						}
			conn.commit();
			return true;
		}catch (Exception e) {
			throw new IngestServiceException("Failed to delete transaction id '" + transactionId + "' : " + e.getMessage(), e);
		}finally {
			try {
				conn.close();
			}catch(Exception e) {}
			try {
				stmt.close();
			}catch(Exception e) {}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IngestReceipt update(TransactionId<?> transactionId,
			List<TermBucket> termBuckets) throws IngestServiceException {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.createStatement();
			for (TermBucket termBucket : termBuckets) 
				for (Term term : termBucket.getTerms()) 
					for (String value : term.getValues())
						try {
							stmt.execute("DELETE FROM transaction_terms WHERE transaction_id = '" + transactionId + "' AND bucket_name = '" + termBucket.getName() + "' AND term_name = '" + term.getName() + "'");
							stmt.execute("INSERT INTO transaction_terms VALUES ('" + transactionId + "','" + termBucket.getName() + "','" + term.getName() + "','" + (this.useUTF8 ? URLEncoder.encode(value, "UTF8") : value) + "')");
						}catch (Exception e) {
							LOG.log(Level.WARNING, "Failed to ingest term: '" + transactionId + "','" + termBucket.getName() + "','" + term.getName() + "','" + value + "'");
						}
			Calendar calendar = DateUtils.getCurrentLocalTime();
			stmt.execute("UPDATE transactions SET transaction_date = '" + DateUtils.toString(calendar) + "' WHERE transaction_id = '" + transactionId + "'");
			return new IngestReceipt(transactionId, calendar.getTime());
		}catch (Exception e) {
			throw new IngestServiceException("Failed to ingest metadata for transaction id '" + transactionId + "' : " + e.getMessage(), e);
		}finally {
			try {
				conn.close();
			}catch(Exception e) {}
			try {
				stmt.close();
			}catch(Exception e) {}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<TermBucket> getBuckets(TransactionId<?> transactionId)
			throws QueryServiceException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			HashMap<String, TermBucket> termBuckets = new HashMap<String, TermBucket>();
			conn = this.dataSource.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT bucket_name,term_name,term_value FROM transaction_terms WHERE transaction_id = '" + transactionId + "'");
			while (rs.next()) {
                String bucketName = rs.getString("bucket_name");
                String termName = rs.getString("term_name");
                String termValue = rs.getString("term_value");
                TermBucket bucket = termBuckets.get(bucketName);
                if (bucket == null)
                	bucket = new TermBucket(bucketName);
                Term term = new Term(termName, Collections.singletonList((this.useUTF8 ? URLDecoder.decode(termValue, "UTF8") : termValue)));
                bucket.addTerm(term);
                termBuckets.put(bucketName, bucket);
			}
			return new Vector<TermBucket>(termBuckets.values());
		}catch (Exception e) {
			throw new QueryServiceException("Failed to get term buckets for transaction id '" + transactionId + "' : " + e.getMessage(), e);
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

	/**
	 * {@inheritDoc}
	 */
	public Map<TransactionId<?>, List<TermBucket>> getBuckets(
			List<TransactionId<?>> transactionIds) throws QueryServiceException {
		HashMap<TransactionId<?>, List<TermBucket>> map = new HashMap<TransactionId<?>, List<TermBucket>>();
		for (TransactionId<?> transactionId : transactionIds) 
			map.put(transactionId, this.getBuckets(transactionId));
		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IngestReceipt> query(QueryExpression queryExpression)
			throws QueryServiceException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.dataSource.getConnection();
			stmt = conn.createStatement();
			String sqlQuery = "SELECT DISTINCT transaction_id,transaction_date FROM transactions WHERE transaction_id IN (" + this.getSqlQuery(queryExpression) + ")";
	        LOG.log(Level.INFO, "Performing Query: " + sqlQuery);
			rs = stmt.executeQuery(sqlQuery);	

			List<IngestReceipt> receipts = new Vector<IngestReceipt>();
			while (rs.next()) 
				receipts.add(new IngestReceipt(this.getTransactionIdFactory().createTransactionId(rs.getString("transaction_id")), DateUtils.toCalendar(rs.getString("transaction_date"), DateUtils.FormatType.LOCAL_FORMAT).getTime()));
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
			String sqlQuery = "SELECT DISTINCT transaction_id,transaction_date FROM transactions WHERE transaction_id IN (" + this.getSqlQuery(queryExpression) + ")";
	        LOG.log(Level.INFO, "Performing Query: " + sqlQuery);
			rs = stmt.executeQuery(sqlQuery);	

			List<IngestReceipt> receipts = new Vector<IngestReceipt>();
			int index = 0;
			while (startIndex > index && rs.next()) index++;
			while (rs.next() && index++ <= endIndex) 
				receipts.add(new IngestReceipt(this.getTransactionIdFactory().createTransactionId(rs.getString("transaction_id")), DateUtils.toCalendar(rs.getString("transaction_date"), DateUtils.FormatType.LOCAL_FORMAT).getTime()));
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
			String sqlQuery = "SELECT COUNT(transaction_id) AS numTransactions FROM transactions WHERE transaction_id IN (" + this.getSqlQuery(queryExpression) + ")";
	        LOG.log(Level.INFO, "Performing Query: " + sqlQuery);
			rs = stmt.executeQuery(sqlQuery);	

			int numTransactions = 0;
            while (rs.next())
            	numTransactions = rs.getInt("numTransactions");
            
			return numTransactions;
		}catch (Exception e) {
			throw new QueryServiceException("Failed to get size of query : " + e.getMessage(), e);
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
		String bucketNameFilter = "";
		if (queryExpression.getBucketNames() != null) {
			if (queryExpression.getBucketNames().size() == 1)
				bucketNameFilter += "bucket_name = '" + queryExpression.getBucketNames().iterator().next() + "' AND ";
			else if (queryExpression.getBucketNames().size() > 1)
				bucketNameFilter += "(bucket_name = '" + StringUtils.join(queryExpression.getBucketNames().iterator(), "' OR bucket_name = '") + "') AND ";
		}
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
            
            sqlQuery = "SELECT DISTINCT transaction_id FROM transaction_terms WHERE " + bucketNameFilter + " term_name = '" + cqe.getTerm().getName() + "' AND (";
        	for (int i = 0; i < cqe.getTerm().getValues().size(); i++) {
        		String value = cqe.getTerm().getValues().get(i);
                sqlQuery += "term_value " + operator + " '" + (this.useUTF8 ? URLEncoder.encode(value, "UTF-8") : value) + "'";
	            if ((i + 1) < cqe.getTerm().getValues().size())
	            	sqlQuery += " OR ";
        	}
        	sqlQuery += ")";
        }else if (queryExpression instanceof NotQueryExpression) {
        	NotQueryExpression nqe = (NotQueryExpression) queryExpression;
            sqlQuery = "SELECT DISTINCT transaction_id FROM transaction_terms WHERE " + bucketNameFilter + " NOT (" + this.getSqlQuery(nqe.getQueryExpression()) + ")";
        }else if (queryExpression instanceof StdQueryExpression) {
            sqlQuery = "SELECT DISTINCT transaction_id FROM transaction_terms " + bucketNameFilter;
        }else {
            throw new QueryServiceException("Invalid QueryExpression '" + queryExpression.getClass().getCanonicalName() + "'");
        }
        return sqlQuery;
    }

}
	