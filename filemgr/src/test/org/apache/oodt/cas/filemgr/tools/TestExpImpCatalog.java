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


package org.apache.oodt.cas.filemgr.tools;

//OODT imports
import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManager;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class TestExpImpCatalog extends TestCase {

    private static final int FM_PORT = 50010;

    private XmlRpcFileManager fm;

    private XmlRpcFileManager fm2;

    private ExpImpCatalog expImp;

    private static final String transferServiceFacClass = "org.apache.oodt.cas."
            + "filemgr.datatransfer.LocalDataTransferFactory";

    public TestExpImpCatalog() {
    }
    
    public void testEnsureUniqueOn(){
        expImp.setEnsureUnique(true);
        
        try {
            expImp.doExpImport();
            expImp.doExpImport();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        // now test that 1 producs exist in the catalog, and that
        // its name is test.txt
        
        try {
            XmlRpcFileManagerClient fmClient = new XmlRpcFileManagerClient(
                    new URL("http://localhost:" + (FM_PORT + 1)));
            assertEquals(1, fmClient.getNumProducts(fmClient
                    .getProductTypeByName("GenericFile")));

            List prods = fmClient.getProductsByProductType(fmClient
                    .getProductTypeByName("GenericFile"));
            assertNotNull(prods);
            assertEquals(1, prods.size());

            int countProds = 0;

            for (Iterator i = prods.iterator(); i.hasNext();) {
                Product p = (Product) i.next();
                if (p.getProductName().equals("test.txt")) {
                    countProds++;
                }
            }

            assertEquals(1, countProds);
            fmClient = null;
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }        
    }

    public void testEnsureUniqueOff() {
        expImp.setEnsureUnique(false);
        
        try {
            expImp.doExpImport();
            expImp.doExpImport();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        // now test that 2 producs exist in the catalog, and that
        // both of their names are test.txt

        // now test that test.txt exists in cat 2
        try {
            XmlRpcFileManagerClient fmClient = new XmlRpcFileManagerClient(
                    new URL("http://localhost:" + (FM_PORT + 1)));
            assertEquals(2, fmClient.getNumProducts(fmClient
                    .getProductTypeByName("GenericFile")));

            List prods = fmClient.getProductsByProductType(fmClient
                    .getProductTypeByName("GenericFile"));
            assertNotNull(prods);
            assertEquals(2, prods.size());

            int countProds = 0;

            for (Iterator i = prods.iterator(); i.hasNext();) {
                Product p = (Product) i.next();
                if (p.getProductName().equals("test.txt")) {
                    countProds++;
                }
            }

            assertEquals(2, countProds);
            fmClient = null;
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    public void testExpImp() {
        try {
            expImp.doExpImport();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        // now test that test.txt exists in cat 2
        try {
            XmlRpcFileManagerClient fmClient = new XmlRpcFileManagerClient(
                    new URL("http://localhost:" + (FM_PORT + 1)));
            Product prod = fmClient.getProductByName("test.txt");
            assertNotNull(prod);
            Metadata met = fmClient.getMetadata(prod);
            assertNotNull(met);
            assertTrue(met.containsKey(CoreMetKeys.PRODUCT_STRUCTURE));
            prod.setProductReferences(fmClient.getProductReferences(prod));
            assertNotNull(prod.getProductReferences());
            assertEquals(1, prod.getProductReferences().size());
            fmClient = null;
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        System.out.println("Test set up");
        fm = startXmlRpcFileManager(FM_PORT, new File(
                "./src/testdata/ingest/cat").getCanonicalPath());
        fm2 = startXmlRpcFileManager(FM_PORT + 1, new File(
                "./src/testdata/ingest/cat2").getCanonicalPath());
        ingestTestFiles();
        try {
            expImp = new ExpImpCatalog(new URL("http://localhost:" + FM_PORT),
                    new URL("http://localhost:" + (FM_PORT + 1)), true);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        fm.shutdown();
        fm = null;
        fm2.shutdown();
        fm2 = null;

        // blow away lucene cat
        deleteAllFiles(new File("./src/testdata/ingest/cat").getCanonicalPath());
        deleteAllFiles(new File("./src/testdata/ingest/cat2")
                .getCanonicalPath());

        // blow away test file
        deleteAllFiles("/tmp/test.txt");
    }

    private void ingestTestFiles() {
        Metadata prodMet = null;
        StdIngester ingester = new StdIngester(transferServiceFacClass);

        try {
            prodMet = new Metadata(new FileInputStream(
                    "./src/testdata/ingest/test.txt.met"));

            // now add the right file location
            prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(
                    "./src/testdata/ingest").getCanonicalPath());
            ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(
                    "./src/testdata/ingest/test.txt"), prodMet);
            
            prodMet.replaceMetadata(CoreMetKeys.PRODUCT_NAME, "TestTypeFile");
            prodMet.replaceMetadata(CoreMetKeys.PRODUCT_TYPE, "TestType");
            ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(
                "./src/testdata/ingest/test.txt"), prodMet);
        } catch (Exception e) {
            fail(e.getMessage());
        }
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

    private XmlRpcFileManager startXmlRpcFileManager(int port, String catPath) {
        XmlRpcFileManager fileMgr = null;
        
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
        System.setProperty("filemgr.catalog.factory",
                "org.apache.oodt.cas.filemgr.catalog.LuceneCatalogFactory");
        System.setProperty(
                "org.apache.oodt.cas.filemgr.catalog.lucene.idxPath",
                catPath);

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
            fileMgr = new XmlRpcFileManager(port);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        return fileMgr;
    }

}
