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


package org.apache.oodt.cas.filemgr.ingest;

//JDK imports

import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.exceptions.CacheException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.system.FileManagerServer;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.commons.util.DateConvert;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

//OODT imports
// Jnit imports

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test suite for the {@link CachedIngester}.
 * </p>.
 */
public class TestCachedIngester extends TestCase {
    private static Logger LOG = Logger.getLogger(TestCachedIngester.class.getName());
    private static final int FM_PORT = 50010;

    private FileManagerServer fm;

    private String luceneCatLoc;

    private CachedIngester ingester;

    private static final String RANGE_QUERY_ELEM = "CAS.ProductReceivedTime";

    private static final String UNIQUE_ELEM = "CAS.ProductName";

    private static final String GENERIC_FILE_TYPE = "GenericFile";

    private static String DATE_RANGE_START;

    private static String DATE_RANGE_END;

    private static List cachedProductTypes = new Vector();

    private Properties initialProperties = new Properties(
        System.getProperties());

    static {
        Date startDate = new Date();
        Date endDate = new Date();
        endDate.setHours(startDate.getHours() + 1);
        DATE_RANGE_START = DateConvert.isoFormat(startDate);
        DATE_RANGE_END = DateConvert.isoFormat(endDate);
        cachedProductTypes.add(GENERIC_FILE_TYPE);

    }

    private static final String transferServiceFacClass = "org.apache.oodt.cas."
            + "filemgr.datatransfer.LocalDataTransferFactory";

    public TestCachedIngester() {
    }

    public void testHasProduct() {
        try {
            ingester.resynsc();
        } catch (CacheException e) {
            fail(e.getMessage());
        }
        try {
            assertTrue(ingester.hasProduct(new URL("http://localhost:"
                    + FM_PORT), "test.txt"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testIngest() {
        Metadata prodMet;

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
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // now make sure that the file is ingested
        try {
            FileManagerClient fmClient = RpcCommunicationFactory.createClient(
                    new URL("http://localhost:" + FM_PORT));
            Product p = fmClient.getProductByName("test.txt");
            assertNotNull(p);
            assertEquals(Product.STATUS_RECEIVED, p.getTransferStatus());
            assertTrue(fmClient.hasProduct("test.txt"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {

        startXmlRpcFileManager();
        ingestTestFile();
        Cache cache = null;

        try {
            cache = new LocalCache(new URL("http://localhost:" + FM_PORT),
                    UNIQUE_ELEM, cachedProductTypes, RANGE_QUERY_ELEM,
                    DATE_RANGE_START, DATE_RANGE_END);
        } catch (Exception e) {
            fail("This should never happen");
        }
        try {
            ingester = new CachedIngester(transferServiceFacClass, cache);
        } catch (InstantiationException e) {
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

        // blow away lucene cat
        deleteAllFiles(luceneCatLoc);

        // blow away test file
        deleteAllFiles("/tmp/test.txt");

        // Reset the System properties to initial values.
        System.setProperties(initialProperties);
    }

    private void ingestTestFile() {
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

    private void startXmlRpcFileManager() {

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
        try {
            URL fmpolicyUrl = this.getClass().getResource("/ingest/fmpolicy");
            properties.setProperty(
                    "org.apache.oodt.cas.filemgr.repositorymgr.dirs",
                    "file://"
                        + new File(fmpolicyUrl.getFile()).getCanonicalPath());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // now override the val layer ones
        URL examplesCoreUrl = this.getClass().getResource("/examples/core");
        properties.setProperty("org.apache.oodt.cas.filemgr.validation.dirs",
                "file://"
                    + new File(examplesCoreUrl.getFile()).getAbsolutePath());

        // set up mime repo path
        URL mimeTypesUrl = this.getClass().getResource("/mime-types.xml");
        properties.setProperty(
                "org.apache.oodt.cas.filemgr.mime.type.repository", new File(
                     mimeTypesUrl.getFile()).getAbsolutePath());

        System.setProperties(properties);

        try {
            fm = RpcCommunicationFactory.createServer(FM_PORT);
            fm.startUp();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }
    }

}
