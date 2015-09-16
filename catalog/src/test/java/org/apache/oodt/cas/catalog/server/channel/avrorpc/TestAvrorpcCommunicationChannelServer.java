package org.apache.oodt.cas.catalog.server.channel.avrorpc;

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.catalog.mapping.InMemoryIngestMapperFactory;
import org.apache.oodt.cas.catalog.repository.MemoryBasedCatalogRepositoryFactory;
import org.apache.oodt.cas.catalog.server.channel.CommunicationChannelClient;
import org.apache.oodt.cas.catalog.server.channel.CommunicationChannelServer;
import org.apache.oodt.cas.catalog.struct.impl.index.DataSourceIndexFactory;
import org.apache.oodt.cas.catalog.struct.impl.index.InMemoryIndexFactory;
import org.apache.oodt.cas.catalog.struct.impl.transaction.UuidTransactionIdFactory;
import org.apache.oodt.cas.catalog.system.CatalogFactory;
import org.apache.oodt.cas.catalog.system.impl.CatalogServiceLocal;
import org.apache.oodt.cas.catalog.system.impl.CatalogServiceLocalFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class TestAvrorpcCommunicationChannelServer extends TestCase {

    private CommunicationChannelServer server;
    private CommunicationChannelClient client;

    private CatalogServiceLocal cs;
    private File testDir;

    public void setUp() {
        try {
            //copied start
            File tempFile = File.createTempFile("foo1", "bar2");
            tempFile.deleteOnExit();
            testDir = new File(tempFile.getParentFile(), "cas-catalog2");

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
            //copied end


            AvrorpcCommunicationChannelServerFactory serverFactory = new AvrorpcCommunicationChannelServerFactory();
            serverFactory.setCatalogServiceFactory(factory);

            server = serverFactory.createCommunicationChannelServer();
            server.setPort(9999);

            server.setCatalogService(cs);
            server.startup();

            AvrorpcCommunicationChannelClientFactory clientFactory = new AvrorpcCommunicationChannelClientFactory();
            clientFactory.setChunkSize(200);
            clientFactory.setConnectionTimeout(2000);
            clientFactory.setRequestTimeout(2000);

            clientFactory.setServerUrl("http://localhost:" + server.getPort());
            client = clientFactory.createCommunicationChannelClient();

        }catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.getMessage());
        }
    }

    public void tearDown() {
        try {
            FileUtils.forceDelete(this.testDir);
        } catch (IOException e) {
            e.printStackTrace();
            TestCase.fail(e.getMessage());
        }
    }

    public void testServer(){
        assertNotNull(server);
        assertNotNull(client);
        try {
            assertNotNull(client.getCalalogProperties());
            assertEquals(server.getCurrentCatalogIds(),client.getCurrentCatalogIds());

            //System.out.println("x = " + client.getCurrentCatalogIds());
        } catch (Exception e) {
            e.printStackTrace();
            assert(false);
        }
    }


    private InMemoryIngestMapperFactory getOracleIngestMapperFactory(
            String tmpDirPath) throws SQLException, IOException {
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

    private DataSourceIndexFactory getInMemoryDSFactory(String tmpDirPath)
            throws IOException, SQLException {
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
