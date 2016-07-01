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
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.system.FileManagerServer;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

//JDK imports
//Junit imports

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class TestExpImpCatalog extends TestCase {
    private static Logger LOG = Logger.getLogger(TestExpImpCatalog.class.getName());
    private static final int FM_PORT = 50010;

    private FileManagerServer fm;

    private FileManagerServer fm2;

    private ExpImpCatalog expImp;

    private static final String transferServiceFacClass = "org.apache.oodt.cas."
            + "filemgr.datatransfer.LocalDataTransferFactory";

    private Properties initialProperties = new Properties(
        System.getProperties());

    public TestExpImpCatalog() {
    }
    
    public void testEnsureUniqueOn(){
        expImp.setEnsureUnique(true);
        
        try {
            expImp.doExpImport();
            expImp.doExpImport();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        // now test that 1 producs exist in the catalog, and that
        // its name is test.txt
        
        try {
            FileManagerClient fmClient = RpcCommunicationFactory.createClient(new URL("http://localhost:" + (FM_PORT + 1)));
            assertEquals(1, fmClient.getNumProducts(fmClient
                    .getProductTypeByName("GenericFile")));

            List prods = fmClient.getProductsByProductType(fmClient
                    .getProductTypeByName("GenericFile"));
            assertNotNull(prods);
            assertEquals(1, prods.size());

            int countProds = 0;

            for (Object prod : prods) {
                Product p = (Product) prod;
                if (p.getProductName().equals("test.txt")) {
                    countProds++;
                }
            }

            assertEquals(1, countProds);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }        
    }

    public void testEnsureUniqueOff() {
        expImp.setEnsureUnique(false);
        
        try {
            expImp.doExpImport();
            expImp.doExpImport();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        // now test that 2 producs exist in the catalog, and that
        // both of their names are test.txt

        // now test that test.txt exists in cat 2
        try {
            FileManagerClient fmClient = RpcCommunicationFactory.createClient(new URL("http://localhost:" + (FM_PORT + 1)));
            assertEquals(2, fmClient.getNumProducts(fmClient
                    .getProductTypeByName("GenericFile")));

            List prods = fmClient.getProductsByProductType(fmClient
                    .getProductTypeByName("GenericFile"));
            assertNotNull(prods);
            assertEquals(2, prods.size());

            int countProds = 0;

            for (Object prod : prods) {
                Product p = (Product) prod;
                if (p.getProductName().equals("test.txt")) {
                    countProds++;
                }
            }

            assertEquals(2, countProds);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

    }

    public void testExpImp() {
        try {
            expImp.doExpImport();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        // now test that test.txt exists in cat 2
        try {
            FileManagerClient fmClient = RpcCommunicationFactory.createClient(new URL("http://localhost:" + (FM_PORT + 1)));
            Product prod = fmClient.getProductByName("test.txt");
            assertNotNull(prod);
            Metadata met = fmClient.getMetadata(prod);
            assertNotNull(met);
            assertTrue(met.containsKey(CoreMetKeys.PRODUCT_STRUCTURE));
            prod.setProductReferences(fmClient.getProductReferences(prod));
            assertNotNull(prod.getProductReferences());
            assertEquals(1, prod.getProductReferences().size());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
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
        URL ingestUrl = this.getClass().getResource("/ingest");
        String cat1 = new File(ingestUrl.getFile()).getCanonicalPath() + "cat";
        String cat2 = new File(ingestUrl.getFile()).getCanonicalPath() + "cat2";
        fm = startFileManager(FM_PORT, cat1);
        fm.startUp();
        fm2 = startFileManager(FM_PORT + 1, cat2);
        fm2.startUp();
        ingestTestFiles();
        try {
            expImp = new ExpImpCatalog(new URL("http://localhost:" + FM_PORT),
                    new URL("http://localhost:" + (FM_PORT + 1)), true);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
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
        URL ingestUrl = this.getClass().getResource("/ingest");
        String cat1 = new File(ingestUrl.getFile()).getCanonicalPath() + "cat";
        String cat2 = new File(ingestUrl.getFile()).getCanonicalPath() + "cat2";
        deleteAllFiles(cat1);
        deleteAllFiles(cat2);

        // blow away test file
        deleteAllFiles("/tmp/test.txt");

        // Reset the System properties to initial values.
        System.setProperties(initialProperties);
    }

    private void ingestTestFiles() {
        Metadata prodMet;
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
            ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(
                refUrl.getFile()), prodMet);
            
            prodMet.replaceMetadata(CoreMetKeys.PRODUCT_NAME, "TestTypeFile");
            prodMet.replaceMetadata(CoreMetKeys.PRODUCT_TYPE, "TestType");
            ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(
                refUrl.getFile()), prodMet);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void deleteAllFiles(String startDir) {
        File startDirFile = new File(startDir);
        File[] delFiles = startDirFile.listFiles();

        if (delFiles != null && delFiles.length > 0) {
            for (File delFile : delFiles) {
                delFile.delete();
            }
        }

        startDirFile.delete();

    }

    private FileManagerServer startFileManager(int port, String catPath) {

        Properties properties = new Properties(System.getProperties());

        FileManagerServer fileMgr = null;
        
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
        properties.setProperty("filemgr.catalog.factory",
                "org.apache.oodt.cas.filemgr.catalog.LuceneCatalogFactory");
        properties.setProperty(
                "org.apache.oodt.cas.filemgr.catalog.lucene.idxPath",
                catPath);

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
            fileMgr = RpcCommunicationFactory.createServer(port);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        return fileMgr;
    }

}
