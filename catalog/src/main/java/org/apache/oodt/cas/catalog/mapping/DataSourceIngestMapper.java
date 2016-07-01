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
package org.apache.oodt.cas.catalog.mapping;

//JDK imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
//SQL imports
import javax.sql.DataSource;

//OODT imports
import org.apache.oodt.cas.catalog.exception.CatalogRepositoryException;
import org.apache.oodt.cas.catalog.page.CatalogReceipt;
import org.apache.oodt.cas.catalog.page.IndexPager;
import org.apache.oodt.cas.catalog.page.IngestReceipt;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.struct.TransactionIdFactory;
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;
import org.apache.oodt.commons.date.DateUtils;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Ingest Mapper that indexes to an DataSource Database
 * <p>
 */
public class DataSourceIngestMapper implements IngestMapper {

	private DataSource dataSource;
	
	private static final String CATALOG_ID = "' AND CATALOG_ID = '";
	
	private static final String CAT_TRANS_FACTORY = "CAT_TRANS_FACTORY";
	
	private static final String CAT_TRANS_ID = "CAT_TRANS_ID";
	
	public DataSourceIngestMapper(String user, String pass, String driver,
			String jdbcUrl) {
		this.dataSource = DatabaseConnectionBuilder.buildDataSource(user, pass,
                driver, jdbcUrl);
	}
	
	@Override
	public synchronized void deleteAllMappingsForCatalog(String catalogId)
			throws CatalogRepositoryException, SQLException {
		try (Connection conn = this.dataSource.getConnection();
		    Statement stmt = conn.createStatement()) {
			stmt.execute("DELETE FROM CatalogServiceMapper WHERE CATALOG_ID = '" + catalogId + "'");
		}
	}

	@Override
	public synchronized void deleteAllMappingsForCatalogServiceTransactionId(
			TransactionId<?> catalogServiceTransactionId)
			throws CatalogRepositoryException, SQLException {
		try (Connection conn = this.dataSource.getConnection();
		    Statement stmt = conn.createStatement()){
			stmt.execute("DELETE FROM CatalogServiceMapper WHERE CAT_SERV_TRANS_ID = '" + catalogServiceTransactionId + "'");
			conn.commit();
		}
	}

	@Override
	public synchronized void deleteTransactionIdMapping(
			TransactionId<?> catalogTransactionId, String catalogId)
			throws CatalogRepositoryException, SQLException {
		try (Connection conn = this.dataSource.getConnection();
		    Statement stmt = conn.createStatement()){
			stmt.execute("DELETE FROM CatalogServiceMapper WHERE CAT_TRANS_ID = '" + catalogTransactionId + CATALOG_ID + catalogId + "'");
			conn.commit();
		}
	}

	@Override
	public synchronized TransactionId<?> getCatalogServiceTransactionId(
			TransactionId<?> catalogTransactionId, String catalogId)
			throws CatalogRepositoryException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		ResultSet rs = null;
    String query = "SELECT CAT_SERV_TRANS_ID,CAT_SERV_TRANS_FACTORY FROM CatalogServiceMapper WHERE CAT_TRANS_ID = '"+ catalogTransactionId + CATALOG_ID + catalogId + "'";
		try (Connection conn = this.dataSource.getConnection();
		    PreparedStatement pstmt = conn.prepareStatement(query)){
			rs = pstmt.executeQuery();
			while(rs.next()) {
			  return ((TransactionIdFactory) Class.forName(rs.getString("CAT_SERV_TRANS_FACTORY")).newInstance())
				  .createTransactionId(rs.getString("CAT_SERV_TRANS_ID"));
			}
			return null;
		}finally {
      if (rs != null) {
          rs.close();
      }
		}
	}

	@Override
	public synchronized TransactionId<?> getCatalogTransactionId(
			TransactionId<?> catalogServiceTransactionId, String catalogId)
			throws CatalogRepositoryException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		ResultSet rs = null;
    String query = "SELECT CAT_TRANS_ID,CAT_TRANS_FACTORY FROM CatalogServiceMapper WHERE CAT_SERV_TRANS_ID = '"+ catalogServiceTransactionId + CATALOG_ID + catalogId + "'";
		try (Connection conn = this.dataSource.getConnection();
		    PreparedStatement pstmt = conn.prepareStatement(query)){
			rs = pstmt.executeQuery();
			while(rs.next()) {
			  return ((TransactionIdFactory) Class.forName(rs.getString(CAT_TRANS_FACTORY)).newInstance())
				  .createTransactionId(rs.getString(CAT_TRANS_ID));
			}
			return null;
		}finally {
      if (rs != null) {
        rs.close();
      }
		}
	}

	@Override
	public synchronized Set<String> getCatalogIds(
			TransactionId<?> catalogServiceTransactionId)
			throws CatalogRepositoryException, SQLException {
		ResultSet rs = null;
    String query = "SELECT CATALOG_ID FROM CatalogServiceMapper WHERE CAT_SERV_TRANS_ID = '"+ catalogServiceTransactionId + "'";
		try (Connection conn = this.dataSource.getConnection();
		    PreparedStatement pstmt = conn.prepareStatement(query)){
			rs = pstmt.executeQuery();
			Set<String> catalogIds = new HashSet<>();
			while(rs.next()) {
			  catalogIds.add(rs.getString("CATALOG_ID"));
			}
			return catalogIds;
		}finally {
      if (rs != null) {
        rs.close();
      }
		}	
	}

	@Override
	public synchronized Set<TransactionId<?>> getPageOfCatalogTransactionIds(
			IndexPager indexPager, String catalogId)
			throws CatalogRepositoryException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		ResultSet rs = null;
		String query = "SELECT * FROM "
        +"( SELECT a.*, ROWNUM r FROM " 
        + "( SELECT CAT_TRANS_FACTORY,CAT_TRANS_ID FROM CatalogServiceMapper WHERE CatalogServiceMapper.CATALOG_ID = '" 
        + catalogId + "' ORDER BY CatalogServiceMapper.CAT_SERV_TRANS_ID DESC ) a "
        + "WHERE ROWNUM <= " + (indexPager.getPageSize() * (indexPager.getPageNum() + 1)) + " ) "
        + "WHERE r >= " + ((indexPager.getPageSize() * indexPager.getPageNum()) + 1);
		try (Connection conn = this.dataSource.getConnection();
		    PreparedStatement pstmt = conn.prepareStatement(query)){
			rs = pstmt.executeQuery();
			Set<TransactionId<?>> transactionIds = new HashSet<>();
			while(rs.next()) {
			  transactionIds.add(((TransactionIdFactory) Class.forName(rs.getString(CAT_TRANS_FACTORY)).newInstance())
				  .createTransactionId(rs.getString(CAT_TRANS_ID)));
			}
			return transactionIds;
		}finally {
      if (rs != null) {
        rs.close();
      }
		}
	}

	@Override
	public synchronized boolean hasCatalogServiceTransactionId(
			TransactionId<?> catalogServiceTransactionId)
			throws CatalogRepositoryException, SQLException {
		ResultSet rs = null;
		String query = "SELECT CAT_SERV_TRANS_ID FROM CatalogServiceMapper WHERE CAT_SERV_TRANS_ID = '"+ catalogServiceTransactionId + "'";
		try (Connection conn = this.dataSource.getConnection();
		    PreparedStatement pstmt = conn.prepareStatement(query)){
			rs = pstmt.executeQuery();
			return rs.next();
		}finally {
      if (rs != null) {
        rs.close();
      }
		}
	}

	@Override
	public synchronized void storeTransactionIdMapping(
			TransactionId<?> catalogServiceTransactionId,
			TransactionIdFactory catalogServiceTransactionIdFactory,
			CatalogReceipt catalogReceipt,
			TransactionIdFactory catalogTransactionIdFactory)
			throws CatalogRepositoryException, SQLException {
    Calendar calTime = DateUtils.getCurrentUtcTime();
    calTime.setTime(catalogReceipt.getTransactionDate());
	  String query = "INSERT INTO CatalogServiceMapper (CAT_SERV_TRANS_ID, CAT_SERV_TRANS_FACTORY, CAT_TRANS_ID, CAT_TRANS_FACTORY,"
	      + " CAT_TRANS_DATE, CATALOG_ID) VALUES ('" 
        + catalogServiceTransactionId + "', '" 
        + catalogServiceTransactionIdFactory.getClass().getName() + "', '" 
        + catalogReceipt.getTransactionId() + "', '" 
        + catalogTransactionIdFactory.getClass().getName() + "', '" 
        + DateUtils.toString(calTime) + "', '"
        + catalogReceipt.getCatalogId() + "')";
		try (Connection conn = this.dataSource.getConnection();
		    PreparedStatement pstmt = conn.prepareStatement(query)){
			pstmt.execute();
			conn.commit();
		}
	}

	@Override
	public CatalogReceipt getCatalogReceipt(
			TransactionId<?> catalogServiceTransactionId, String catalogId)
			throws CatalogRepositoryException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, ParseException {
		ResultSet rs = null;
		String query = "SELECT CAT_TRANS_ID, CAT_TRANS_FACTORY, CAT_TRANS_DATE FROM CatalogServiceMapper WHERE CAT_SERV_TRANS_ID = '"+ catalogServiceTransactionId + CATALOG_ID + catalogId + "'";
		try (Connection conn = this.dataSource.getConnection();
		    PreparedStatement pstmt = conn.prepareStatement(query)){
			rs = pstmt.executeQuery();
			if(rs.next()) {
				TransactionId<?> catalogTransactionId =  ((TransactionIdFactory) Class.forName(rs.getString(CAT_TRANS_FACTORY)).newInstance()).createTransactionId(rs.getString(CAT_TRANS_ID));
				Date transactionDate = DateUtils.toCalendar(rs.getString("CAT_TRANS_DATE"), DateUtils.FormatType.UTC_FORMAT).getTime();
				return new CatalogReceipt(new IngestReceipt(catalogTransactionId, transactionDate), catalogId);
			}else {
				return null;
			}
		}finally {
      if (rs != null) {
        rs.close();
      }
		}
	}

  public DataSource getDataSource() {
	return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
	this.dataSource = dataSource;
  }
}
