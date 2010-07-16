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
import org.apache.oodt.cas.filemgr.util.SqlParser;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
public class TestXmlRpcFileManagerClient extends TestCase {

    private static final int FM_PORT = 50001;

    private XmlRpcFileManager fm;
    
    private String luceneCatLoc;

    private static final String transferServiceFacClass = "org.apache.oodt.cas."
            + "filemgr.datatransfer.LocalDataTransferFactory";

    public TestXmlRpcFileManagerClient() {
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
            XmlRpcFileManagerClient fmc = new XmlRpcFileManagerClient(new URL(
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
            assertEquals(reducedMet.getHashtable().keySet().size(), 1);

            reducedMet = fmc.getReducedMetadata(product, vectorElemList);
            assertNotNull(reducedMet);
            assertTrue(reducedMet.containsKey(CoreMetKeys.FILENAME));
            assertEquals(reducedMet.getHashtable().keySet().size(), 1);

            reducedMet = fmc.getReducedMetadata(product, linkedListElemList);
            assertNotNull(reducedMet);
            assertTrue(reducedMet.containsKey(CoreMetKeys.FILENAME));
            assertEquals(reducedMet.getHashtable().keySet().size(), 1);
            
        } catch (Exception e) {
            fail(e.getMessage());
        }finally {
            fm.shutdown();
        }

    }
    
    public void testIngest() throws Exception {            
        Metadata prodMet = new Metadata();
        prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(
                "./src/testdata/ingest").getCanonicalPath());
        prodMet.addMetadata(CoreMetKeys.FILENAME, "test.txt");
        prodMet.addMetadata(CoreMetKeys.PRODUCT_NAME, "TestFile");
        prodMet.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
        
        StdIngester ingester = new StdIngester(transferServiceFacClass);
        String productId = ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(
            "./src/testdata/ingest/test.txt"), prodMet);     
        XmlRpcFileManagerClient fmc = new XmlRpcFileManagerClient(new URL(
                "http://localhost:" + FM_PORT));
        Metadata m = fmc.getMetadata(fmc.getProductById(productId));
        assertEquals(m.getMetadata("Filename"), "test.txt");
        deleteAllFiles("/tmp/test-type");
    }
    
    public void testComplexQuery() throws Exception {
        StdIngester ingester = new StdIngester(transferServiceFacClass);

        //ingest first file
        Metadata prodMet = new Metadata();
        prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(
                "./src/testdata/ingest").getCanonicalPath());
        prodMet.addMetadata(CoreMetKeys.FILENAME, "test-file-1.txt");
        prodMet.addMetadata(CoreMetKeys.PRODUCT_NAME, "TestFile1");
        prodMet.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
        ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(
            "./src/testdata/ingest/test-file-1.txt"), prodMet);           
        
        //ingest second file
        prodMet.replaceMetadata(CoreMetKeys.FILENAME, "test-file-2.txt");
        prodMet.replaceMetadata(CoreMetKeys.PRODUCT_NAME, "TestFile2");
        ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(
            "./src/testdata/ingest/test-file-2.txt"), prodMet);   
        
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
        XmlRpcFileManagerClient fmc = new XmlRpcFileManagerClient(new URL(
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
        startXmlRpcFileManager();
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
            prodMet = new Metadata(new FileInputStream(
                    "./src/testdata/ingest/test.txt.met"));

            // now add the right file location
            prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(
                    "./src/testdata/ingest").getCanonicalPath());
            prodMet.addMetadata(CoreMetKeys.FILENAME, "test.txt");
            prodMet.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
            ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(
                    "./src/testdata/ingest/test.txt"), prodMet);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void startXmlRpcFileManager() {
        // first make sure to load properties for the file manager
        // and make sure to load logging properties as well

        // set the log levels
        System.setProperty("java.util.logging.config.file", new File(
                "./src/main/resources/logging.properties").getAbsolutePath());

        // first load the example configuration
        try {
            System.getProperties().load(
                    new FileInputStream("./src/main/resources/filemgr.properties"));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // override the catalog to use: we'll use lucene
        try {
            luceneCatLoc = new File("./src/testdata/ingest/cat")
                    .getCanonicalPath();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        System.setProperty("filemgr.catalog.factory",
                "org.apache.oodt.cas.filemgr.catalog.LuceneCatalogFactory");
        System.setProperty(
                "org.apache.oodt.cas.filemgr.catalog.lucene.idxPath",
                luceneCatLoc);

        // now override the repo mgr policy
        try {
            System.setProperty(
                    "org.apache.oodt.cas.filemgr.repositorymgr.dirs",
                    "file://"
                            + new File("./src/testdata/ingest/fmpolicy")
                                    .getCanonicalPath());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // now override the val layer ones
        System.setProperty("org.apache.oodt.cas.filemgr.validation.dirs",
                "file://"
                        + new File("./src/testdata/ingest/fmpolicy")
                                .getAbsolutePath());

        // set up mime repo path
        System.setProperty(
                "org.apache.oodt.cas.filemgr.mime.type.repository", new File(
                        "./src/main/resources/mime-types.xml").getAbsolutePath());

        try {
            fm = new XmlRpcFileManager(FM_PORT);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
