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


package org.apache.oodt.cas.filemgr.system;

//OODT imports
import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.filemgr.util.SqlParser;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test suite for the {@link XmlRpcFileManagerClient}
 * </p>.
 */
@Deprecated
public class TestRpcFileManagerClient extends TestCase {

    private static int FM_PORT = 50001;

    private FileManagerServer fm;
    
    private String luceneCatLoc;

    private static final String transferServiceFacClass = "org.apache.oodt.cas."
            + "filemgr.datatransfer.LocalDataTransferFactory";

    private Properties initialProperties = new Properties(
      System.getProperties());

    public TestRpcFileManagerClient() {
    }

    
    /**
     * @since OODT-161
     * 
     */
    public void testGetReducedMetadata() {
        List arrayListElems = new ArrayList();
        List vectorElemList = new Vector();
        List linkedListElemList = new LinkedList();

        arrayListElems.add(CoreMetKeys.FILENAME);
        vectorElemList.add(CoreMetKeys.FILENAME);
        linkedListElemList.add(CoreMetKeys.FILENAME);

        try {
            FileManagerClient fmc = RpcCommunicationFactory.createClient(new URL(
                    "http://localhost:" + FM_PORT));
            Metadata reducedMet = null;
            List pTypes = fmc.getProductTypes();
            assertNotNull(pTypes);
            assertTrue(pTypes.size() > 0);
            ProductType genericFileType = fmc.getProductTypeByName("GenericFile");

            assertNotNull(genericFileType);
            List products = fmc.getProductsByProductType(genericFileType);
            assertNotNull(products);
            assertTrue(products.size() > 0);
            Product product = (Product) products.get(0);
            assertNotNull(product);

            reducedMet = fmc.getReducedMetadata(product, arrayListElems);
            assertNotNull(reducedMet);
            assertTrue(reducedMet.containsKey(CoreMetKeys.FILENAME));
            assertEquals(reducedMet.getHashTable().keySet().size(), 1);

            reducedMet = fmc.getReducedMetadata(product, vectorElemList);
            assertNotNull(reducedMet);
            assertTrue(reducedMet.containsKey(CoreMetKeys.FILENAME));
            assertEquals(reducedMet.getHashTable().keySet().size(), 1);

            reducedMet = fmc.getReducedMetadata(product, linkedListElemList);
            assertNotNull(reducedMet);
            assertTrue(reducedMet.containsKey(CoreMetKeys.FILENAME));
            assertEquals(reducedMet.getHashTable().keySet().size(), 1);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }finally {
            fm.shutdown();

        }

    }
    
    public void testIngest() throws Exception {
        URL ingestUrl = this.getClass().getResource("/ingest");
        URL refUrl = this.getClass().getResource("/ingest/test.txt");

        Metadata prodMet = new Metadata();
        prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(
          ingestUrl.getFile()).getCanonicalPath());
        prodMet.addMetadata(CoreMetKeys.FILENAME, "test.txt");
        prodMet.addMetadata(CoreMetKeys.PRODUCT_NAME, "TestFile");
        prodMet.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");

        StdIngester ingester = new StdIngester(transferServiceFacClass);
        String productId = ingester.ingest(
          new URL("http://localhost:" + FM_PORT),
          new File(refUrl.getFile()), prodMet);
        FileManagerClient fmc = RpcCommunicationFactory.createClient(new URL(
                "http://localhost:" + FM_PORT));
        Metadata m = fmc.getMetadata(fmc.getProductById(productId));
        assertEquals(m.getMetadata("Filename"), "test.txt");
        deleteAllFiles("/tmp/test-type");
    }

    /**
     * @since OODT-404
     *
     */
    public void testMetadataPersistence() throws Exception {
        URL ingestUrl = this.getClass().getResource("/ingest");
        URL refUrl = this.getClass().getResource("/ingest/test-file-3.txt");
        URL metUrl = this.getClass().getResource("/ingest/test-file-3.txt.met");

        Metadata prodMet = null;
        StdIngester ingester = new StdIngester(transferServiceFacClass);
        prodMet = new SerializableMetadata(new FileInputStream(
            metUrl.getFile()));
        // now add the right file location
        prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(
            ingestUrl.getFile()).getCanonicalPath());
        String productId = ingester.ingest(
            new URL("http://localhost:" + FM_PORT),
            new File(refUrl.getFile()), prodMet);
        FileManagerClient fmc = RpcCommunicationFactory.createClient(new URL(
                "http://localhost:" + FM_PORT));
        Metadata m = fmc.getMetadata(fmc.getProductById(productId));
        assertEquals(m.getAllMetadata("TestElement").size(), 4);
        assertEquals(m.getMetadata("TestElement"), "fe");
    }

    
    public void testComplexQuery() throws Exception {
        URL ingestUrl = this.getClass().getResource("/ingest");
        URL refUrl1 = this.getClass().getResource("/ingest/test-file-1.txt");
        URL refUrl2 = this.getClass().getResource("/ingest/test-file-2.txt");

        StdIngester ingester = new StdIngester(transferServiceFacClass);

        //ingest first file
        Metadata prodMet = new Metadata();
        prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(
            ingestUrl.getFile()).getCanonicalPath());
        prodMet.addMetadata(CoreMetKeys.FILENAME, "test-file-1.txt");
        prodMet.addMetadata(CoreMetKeys.PRODUCT_NAME, "TestFile1");
        prodMet.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
        ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(
            refUrl1.getFile()), prodMet);           
        
        //ingest second file
        prodMet.replaceMetadata(CoreMetKeys.FILENAME, "test-file-2.txt");
        prodMet.replaceMetadata(CoreMetKeys.PRODUCT_NAME, "TestFile2");
        ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(
            refUrl2.getFile()), prodMet);   
        
        //perform complex query
        ComplexQuery complexQuery = new ComplexQuery();
        List<String> reducedMetadata = new Vector<String>();
        reducedMetadata.add(CoreMetKeys.FILENAME);
        complexQuery.setReducedMetadata(reducedMetadata);
        List<String> productTypeNames = new Vector<String>();
        productTypeNames.add("GenericFile");
        complexQuery.setReducedProductTypeNames(productTypeNames);
        complexQuery.setSortByMetKey(CoreMetKeys.FILENAME);
        complexQuery.setToStringResultFormat("$" + CoreMetKeys.FILENAME);
        complexQuery.addCriterion(SqlParser.parseSqlWhereClause("Filename != 'test.txt'"));
        FileManagerClient fmc = RpcCommunicationFactory.createClient(new URL(
                "http://localhost:" + FM_PORT));
        List<QueryResult> queryResults = fmc.complexQuery(complexQuery);
        assertEquals("[test-file-1.txt, test-file-2.txt]", queryResults.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        FM_PORT++;
        startFileManager();
        ingestTestFile();
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        fm.shutdown();
        fm = null;
        
        // blow away lucene cat
        deleteAllFiles(luceneCatLoc);

        // blow away test file
        deleteAllFiles("/tmp/test.txt");

        // Reset the System properties to initial values.
        System.setProperties(initialProperties);
    }

    private void deleteAllFiles(String startDir) {
        File startDirFile = new File(startDir);
        File[] delFiles = startDirFile.listFiles();

        if (delFiles != null && delFiles.length > 0) {
            for (int i = 0; i < delFiles.length; i++) {
                delFiles[i].delete();
            }
        }

        startDirFile.delete();

    }

    private void ingestTestFile() {
        Metadata prodMet = null;
        StdIngester ingester = new StdIngester(transferServiceFacClass);

        try {
            URL ingestUrl = this.getClass().getResource("/ingest");
            URL refUrl = this.getClass().getResource("/ingest/test.txt");
            URL metUrl = this.getClass().getResource("/ingest/test.txt.met");

            prodMet = new SerializableMetadata(new FileInputStream(
                new File(metUrl.getFile())));

            // now add the right file location
            prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(
                ingestUrl.getFile()).getCanonicalPath());
            prodMet.addMetadata(CoreMetKeys.FILENAME, "test.txt");
            prodMet.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
            ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(
                refUrl.getFile()), prodMet);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }


    private void startFileManager() {

        Properties properties = new Properties(System.getProperties());

        // first make sure to load properties for the file manager
        // and make sure to load logging properties as well

        // set the log levels
        URL loggingPropertiesUrl = this.getClass().getResource(
            "/test.logging.properties");
        properties.setProperty("java.util.logging.config.file", new File(
            loggingPropertiesUrl.getFile()).getAbsolutePath());

        // first load the example configuration
        try {
            URL filemgrPropertiesUrl = this.getClass().getResource(
                "/filemgr.properties");
            properties.load(
                new FileInputStream(new File(filemgrPropertiesUrl.getFile())));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // override the catalog to use: we'll use lucene
        try {
            URL ingestUrl = this.getClass().getResource("/ingest");
            luceneCatLoc = new File(ingestUrl.getFile()).getCanonicalPath()
                + "/cat";
        } catch (Exception e) {
            fail(e.getMessage());
        }

        properties.setProperty("filemgr.catalog.factory",
                "org.apache.oodt.cas.filemgr.catalog.LuceneCatalogFactory");
        properties.setProperty(
                "org.apache.oodt.cas.filemgr.catalog.lucene.idxPath",
                luceneCatLoc);

        // now override the repo mgr policy
        URL fmpolicyUrl = this.getClass().getResource("/ingest/fmpolicy");
        try {
            properties.setProperty(
                "org.apache.oodt.cas.filemgr.repositorymgr.dirs",
                "file://" + new File(fmpolicyUrl.getFile()).getCanonicalPath());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // now override the val layer ones
        properties.setProperty("org.apache.oodt.cas.filemgr.validation.dirs",
            "file://" + new File(fmpolicyUrl.getFile()).getAbsolutePath());

        // set up mime repo path
        URL mimeTypesUrl = this.getClass().getResource("/mime-types.xml");
        properties.setProperty(
            "org.apache.oodt.cas.filemgr.mime.type.repository",
            new File(mimeTypesUrl.getFile()).getAbsolutePath());

        System.setProperties(properties);

        try {
            fm = RpcCommunicationFactory.createServer(FM_PORT);
            fm.startUp();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
