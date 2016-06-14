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
package org.apache.oodt.cas.catalog.system.impl;

//JDK imports

import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.catalog.exception.CatalogServiceException;
import org.apache.oodt.cas.catalog.mapping.InMemoryIngestMapperFactory;
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
import org.apache.oodt.cas.catalog.page.Page;
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.catalog.page.QueryPager;
import org.apache.oodt.cas.catalog.page.TransactionReceipt;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.query.parser.ParseException;
import org.apache.oodt.cas.catalog.query.parser.QueryParser;
import org.apache.oodt.cas.catalog.query.parser.TokenMgrError;
import org.apache.oodt.cas.catalog.repository.MemoryBasedCatalogRepositoryFactory;
import org.apache.oodt.cas.catalog.struct.impl.index.DataSourceIndexFactory;
import org.apache.oodt.cas.catalog.struct.impl.index.InMemoryIndexFactory;
import org.apache.oodt.cas.catalog.struct.impl.transaction.UuidTransactionIdFactory;
import org.apache.oodt.cas.catalog.system.CatalogFactory;
import org.apache.oodt.cas.metadata.Metadata;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

//OODT imports

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 */
public class TestCatalogServiceLocal extends TestCase {

	private CatalogServiceLocal cs;
	private File testDir;
  private static Logger LOG = Logger.getLogger(TestCatalogServiceLocal.class.getName());
	public void setUp() {
		try {
			File tempFile = File.createTempFile("foo", "bar");
			tempFile.deleteOnExit();
			testDir = new File(tempFile.getParentFile(), "cas-catalog");
	
			CatalogServiceLocalFactory factory = new CatalogServiceLocalFactory();
			factory
					.setCatalogRepositoryFactory(new MemoryBasedCatalogRepositoryFactory());
			factory.setIngestMapperFactory(this
					.getOracleIngestMapperFactory(testDir.getAbsolutePath() + "/mapper"));
			factory.setOneCatalogFailsAllFail(true);
			factory.setSimplifyQueries(true);
			factory.setPluginStorageDir("/dev/null");
			factory.setRestrictIngestPermissions(false);
			factory.setRestrictQueryPermissions(false);
			factory.setTransactionIdFactory(UuidTransactionIdFactory.class
					.getCanonicalName());
			cs = factory.createCatalogService();
	
			CatalogFactory catalogFactory = new CatalogFactory();
			catalogFactory.setCatalogId("TestCatalog1");
			catalogFactory.setDictionaryFactories(null);
			catalogFactory
					.setIndexFactory(getInMemoryDSFactory(testDir.getAbsolutePath() + "/index/1/"));
			catalogFactory.setRestrictIngestPermissions(false);
			catalogFactory.setRestrictQueryPermissions(false);
			cs.addCatalog(catalogFactory.createCatalog());
			catalogFactory.setCatalogId("TestCatalog2");
			catalogFactory
					.setIndexFactory(getInMemoryDSFactory(testDir.getAbsolutePath() + "/index/2/"));
			cs.addCatalog(catalogFactory.createCatalog());
		}catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage());
			TestCase.fail(e.getMessage());
		}
	}
	
	public void tearDown() {
		try {
			FileUtils.forceDelete(this.testDir);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getMessage());
			TestCase.fail(e.getMessage());
		}
	}

	public void testDataSourceCatalogIngestQueryAndDelete()
			throws CatalogServiceException, ParseException, TokenMgrError {
		// test ingest
		Metadata m = new Metadata();
		m.addMetadata("testkey1", "testval1");
		TransactionReceipt tr = cs.ingest(m);
		Vector<TransactionReceipt> receipts = new Vector<TransactionReceipt>();
		receipts.add(tr);
		List<TransactionalMetadata> metadatas = cs.getMetadata(receipts);
		assertEquals(metadatas.size(), 1);
		Metadata ingestedMetadata = metadatas.get(0).getMetadata();
		assertEquals(ingestedMetadata.getMetadata("testkey1"), "testval1");
		assertEquals(ingestedMetadata.getAllMetadata("testkey1").size(), 2);

		// test ingest update
		m.replaceMetadata(
				CatalogServiceLocal.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY, tr.getTransactionId().toString());
		m.replaceMetadata(CatalogServiceLocal.ENABLE_UPDATE_MET_KEY, "true");
		tr = cs.ingest(m);
		receipts = new Vector<TransactionReceipt>();
		receipts.add(tr);
		metadatas = cs.getMetadata(receipts);
		assertEquals(metadatas.size(), 1);
		ingestedMetadata = metadatas.get(0).getMetadata();
		assertEquals(ingestedMetadata.getMetadata("testkey1"), "testval1");
		assertEquals(ingestedMetadata.getAllMetadata("testkey1").size(), 2);

		// test query using querypager
		QueryExpression qe = QueryParser
				.parseQueryExpression("testkey1 == 'testval1'");
		QueryPager pager = cs.query(qe);
		metadatas = cs.getNextPage(pager);
		assertEquals(metadatas.size(), 1);
		ingestedMetadata = metadatas.get(0).getMetadata();
		assertEquals(ingestedMetadata.getMetadata("testkey1"), "testval1");
		assertEquals(ingestedMetadata.getAllMetadata("testkey1").size(), 2);

		// test query using std paging
		qe = QueryParser.parseQueryExpression("testkey1 == 'testval1'");
		Page page = cs.getPage(new PageInfo(20, PageInfo.FIRST_PAGE), qe);
		metadatas = cs.getMetadata(page);
		assertEquals(metadatas.size(), 1);
		ingestedMetadata = metadatas.get(0).getMetadata();
		assertEquals(ingestedMetadata.getMetadata("testkey1"), "testval1");
		assertEquals(ingestedMetadata.getAllMetadata("testkey1").size(), 2);

		// test query using std paging with catalog restriction
		qe = QueryParser.parseQueryExpression("testkey1 == 'testval1'");
		page = cs.getPage(new PageInfo(20, PageInfo.FIRST_PAGE), qe,
				Collections.singleton("TestCatalog1"));
		metadatas = cs.getMetadata(page);
		assertEquals(metadatas.size(), 1);
		ingestedMetadata = metadatas.get(0).getMetadata();
		assertEquals(ingestedMetadata.getMetadata("testkey1"), "testval1");
		assertEquals(ingestedMetadata.getAllMetadata("testkey1").size(), 1);

		// test delete
		m = new Metadata();
		m.addMetadata(CatalogServiceLocal.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY, tr.getTransactionId().toString());
		cs.delete(m);
		assertEquals(cs.getMetadata(Collections.singletonList(tr)).size(), 0);
	}

	private InMemoryIngestMapperFactory getOracleIngestMapperFactory(
			String tmpDirPath) {
		String user = "sa";
		String pass = "";
		String driver = "org.hsqldb.jdbcDriver";
		String url = "jdbc:hsqldb:file:" + tmpDirPath + ";shutdown=true";

		InMemoryIngestMapperFactory factory = new InMemoryIngestMapperFactory();
		factory.setDriver(driver);
		factory.setJdbcUrl(url);
		factory.setPass(pass);
		factory.setUser(user);
		factory.setTablesFile(this.getClass().getResource("/test-mapper-cat.sql").getPath());
		return factory;
	}

	private DataSourceIndexFactory getInMemoryDSFactory(String tmpDirPath) {
		String user = "sa";
		String pass = "";
		String driver = "org.hsqldb.jdbcDriver";
		String url = "jdbc:hsqldb:file:" + tmpDirPath + ";shutdown=true";

		InMemoryIndexFactory indexFactory = new InMemoryIndexFactory();
		indexFactory.setDriver(driver);
		indexFactory.setJdbcUrl(url);
		indexFactory.setPass(pass);
		indexFactory.setUser(user);
		indexFactory.setTablesFile(this.getClass().getResource("/test-index-cat.sql").getPath());
		return indexFactory;
	}

}
