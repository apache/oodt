package gov.nasa.jpl.oodt.cas.catalog.system.impl;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.sql.DataSource;

import gov.nasa.jpl.oodt.cas.catalog.exception.CatalogServiceException;
import gov.nasa.jpl.oodt.cas.catalog.mapping.OracleIngestMapperFactory;
import gov.nasa.jpl.oodt.cas.catalog.metadata.TransactionalMetadata;
import gov.nasa.jpl.oodt.cas.catalog.page.Page;
import gov.nasa.jpl.oodt.cas.catalog.page.PageInfo;
import gov.nasa.jpl.oodt.cas.catalog.page.QueryPager;
import gov.nasa.jpl.oodt.cas.catalog.page.TransactionReceipt;
import gov.nasa.jpl.oodt.cas.catalog.query.QueryExpression;
import gov.nasa.jpl.oodt.cas.catalog.query.parser.ParseException;
import gov.nasa.jpl.oodt.cas.catalog.query.parser.QueryParser;
import gov.nasa.jpl.oodt.cas.catalog.query.parser.TokenMgrError;
import gov.nasa.jpl.oodt.cas.catalog.repository.MemoryBasedCatalogRepositoryFactory;
import gov.nasa.jpl.oodt.cas.catalog.struct.impl.index.DataSourceIndexFactory;
import gov.nasa.jpl.oodt.cas.catalog.struct.impl.transaction.UuidTransactionIdFactory;
import gov.nasa.jpl.oodt.cas.catalog.system.CatalogFactory;
import gov.nasa.jpl.oodt.cas.catalog.system.impl.CatalogServiceLocal;
import gov.nasa.jpl.oodt.cas.catalog.system.impl.CatalogServiceLocalFactory;
import gov.nasa.jpl.oodt.cas.commons.database.DatabaseConnectionBuilder;
import gov.nasa.jpl.oodt.cas.commons.database.SqlScript;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import junit.framework.TestCase;

/**
 * 
 * @author bfoster
 *
 */
public class TestCatalogServiceLocal extends TestCase {

	private CatalogServiceLocal cs;
	
	public TestCatalogServiceLocal() throws ClassNotFoundException, InstantiationException, IllegalAccessException, CatalogServiceException, IOException, SQLException {
        File tempFile = File.createTempFile("foo", "bar");
        tempFile.deleteOnExit();
        File tempDir = tempFile.getParentFile();
        String tmpDirPath = tempDir.getAbsolutePath();
		
		CatalogServiceLocalFactory factory = new CatalogServiceLocalFactory();
		factory.setCatalogRepositoryFactory(new MemoryBasedCatalogRepositoryFactory());
		factory.setIngestMapperFactory(this.getOracleIngestMapperFactory(tmpDirPath));
		factory.setOneCatalogFailsAllFail(true);
		factory.setSimplifyQueries(true);
		factory.setPluginStorageDir("/dev/null");
		factory.setRestrictIngestPermissions(false);
		factory.setRestrictQueryPermissions(false);
		factory.setTransactionIdFactory(UuidTransactionIdFactory.class.getCanonicalName());
		cs = factory.createCatalogService();
		
		CatalogFactory catalogFactory = new CatalogFactory();
		catalogFactory.setCatalogId("TestCatalog1");
		catalogFactory.setDictionaryFactories(null);
		catalogFactory.setIndexFactory(getInMemoryDSFactory(tmpDirPath + "/1/"));
		catalogFactory.setRestrictIngestPermissions(false);
		catalogFactory.setRestrictQueryPermissions(false);
		cs.addCatalog(catalogFactory.createCatalog());
		catalogFactory.setCatalogId("TestCatalog2");
		catalogFactory.setIndexFactory(getInMemoryDSFactory(tmpDirPath + "/2/"));
		cs.addCatalog(catalogFactory.createCatalog());
	}
	
	public void testDataSourceCatalogIngestQueryAndDelete() throws CatalogServiceException, ParseException, TokenMgrError {
		//test ingest
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

		//test ingest update
		m.replaceMetadata(CatalogServiceLocal.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY.toString(), tr.getTransactionId().toString());
		tr = cs.ingest(m);
		receipts = new Vector<TransactionReceipt>();
		receipts.add(tr);
		metadatas = cs.getMetadata(receipts);
		assertEquals(metadatas.size(), 1);
		ingestedMetadata = metadatas.get(0).getMetadata();
		assertEquals(ingestedMetadata.getMetadata("testkey1"), "testval1");
		assertEquals(ingestedMetadata.getAllMetadata("testkey1").size(), 2);

		//test query using querypager
		QueryExpression qe = QueryParser.parseQueryExpression("testkey1 == 'testval1'");
		QueryPager pager = cs.query(qe);
		metadatas = cs.getNextPage(pager);
		assertEquals(metadatas.size(), 1);
		ingestedMetadata = metadatas.get(0).getMetadata();
		assertEquals(ingestedMetadata.getMetadata("testkey1"), "testval1");
		assertEquals(ingestedMetadata.getAllMetadata("testkey1").size(), 2);

		//test query using std paging
		qe = QueryParser.parseQueryExpression("testkey1 == 'testval1'");
		Page page = cs.getPage(new PageInfo(20, PageInfo.FIRST_PAGE), qe);
		metadatas = cs.getMetadata(page);
		assertEquals(metadatas.size(), 1);
		ingestedMetadata = metadatas.get(0).getMetadata();
		assertEquals(ingestedMetadata.getMetadata("testkey1"), "testval1");
		assertEquals(ingestedMetadata.getAllMetadata("testkey1").size(), 2);

		//test query using std paging with catalog restriction
		qe = QueryParser.parseQueryExpression("testkey1 == 'testval1'");
		page = cs.getPage(new PageInfo(20, PageInfo.FIRST_PAGE), qe, Collections.singleton("TestCatalog1"));
		metadatas = cs.getMetadata(page);
		assertEquals(metadatas.size(), 1);
		ingestedMetadata = metadatas.get(0).getMetadata();
		assertEquals(ingestedMetadata.getMetadata("testkey1"), "testval1");
		assertEquals(ingestedMetadata.getAllMetadata("testkey1").size(), 1);

		//test delete
		m = new Metadata();
		m.addMetadata(CatalogServiceLocal.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY.toString(), tr.getTransactionId().toString());
		cs.delete(m);
		assertEquals(cs.getMetadata(Collections.singletonList(tr)).size(), 0);
	}
	
	private OracleIngestMapperFactory getOracleIngestMapperFactory(String tmpDirPath) throws SQLException, IOException {
        new File(tmpDirPath).deleteOnExit();

		OracleIngestMapperFactory factory = new OracleIngestMapperFactory();
		String user = "sa";
		String pass = "";
		String driver = "org.hsqldb.jdbcDriver";
		String url = "jdbc:hsqldb:file:" + tmpDirPath + "/testMapperCat;shutdown=true";
        DataSource ds = DatabaseConnectionBuilder.buildDataSource(user, pass,
                driver, url);
        SqlScript coreSchemaScript = new SqlScript(new File("./src/testdata/test-mapper-cat.sql").getAbsolutePath(), ds);
        coreSchemaScript.loadScript();
        coreSchemaScript.execute();
        
		factory.setDriver(driver);
		factory.setJdbcUrl(url);
		factory.setPass(pass);
		factory.setUser(user);	
		return factory;
	}
	
	private DataSourceIndexFactory getInMemoryDSFactory(String tmpDirPath) throws IOException, SQLException {
        new File(tmpDirPath).deleteOnExit();
        
        String user = "sa";
		String pass = "";
		String driver = "org.hsqldb.jdbcDriver";
		String url = "jdbc:hsqldb:file:" + tmpDirPath + "/testIndexCat;shutdown=true";
        DataSource ds = DatabaseConnectionBuilder.buildDataSource(user, pass,
                driver, url);
        SqlScript coreSchemaScript = new SqlScript(new File("./src/testdata/test-index-cat.sql").getAbsolutePath(), ds);
        coreSchemaScript.loadScript();
        coreSchemaScript.execute();

		DataSourceIndexFactory indexFactory = new DataSourceIndexFactory();
		indexFactory.setDriver(driver);
		indexFactory.setJdbcUrl(url);
		indexFactory.setPass(pass);
		indexFactory.setUser(user);
		return indexFactory;
	}
	
}
