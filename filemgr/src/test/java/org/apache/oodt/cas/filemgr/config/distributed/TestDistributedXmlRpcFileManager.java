/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.cas.filemgr.config.distributed;

import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.metadata.ProductMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManager;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.config.distributed.cli.ConfigPublisher;
import org.apache.oodt.config.test.AbstractDistributedConfigurationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import static org.apache.oodt.config.Constants.Properties.ENABLE_DISTRIBUTED_CONFIGURATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDistributedXmlRpcFileManager extends AbstractDistributedConfigurationTest {

    private static final int FM_PORT = 9001;
    private static final String CONF_PUBLISHER_XML = "distributed/config/config-publisher.xml";
    private static final String TRANSFER_SERVICE_FACTORY_CLASS = "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory";

    private XmlRpcFileManager fileManager;

    @Before
    public void setUpTest() throws Exception {
        System.setProperty("org.apache.oodt.cas.cli.action.spring.config", "src/test/resources/distributed/config/cmd-line-actions.xml");
        System.setProperty("org.apache.oodt.cas.cli.option.spring.config", "src/test/resources/distributed/config/cmd-line-options.xml");
        System.setProperty(ENABLE_DISTRIBUTED_CONFIGURATION, "true");

        ConfigPublisher.main(new String[]{
                "-connectString", zookeeper.getConnectString(),
                "-config", CONF_PUBLISHER_XML,
                "-a", "publish"
        });

        try {
            fileManager = new XmlRpcFileManager(FM_PORT);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ingestFile();
    }

    @Test
    public void testDistributedConfigurationWithFileManager() {
        XmlRpcFileManagerClient fmc = null;
        try {
            fmc = new XmlRpcFileManagerClient(new URL("http://localhost:" + FM_PORT));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Metadata met = null;
        try {
            met = fmc.getMetadata(fmc.getProductByName("test.txt"));
        } catch (CatalogException e) {
            fail(e.getMessage());
        }

        assertNotNull(met);
        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_ID));
        assertNotNull(met.getMetadata(ProductMetKeys.PRODUCT_ID));
        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_NAME));
        assertNotNull(met.getMetadata(ProductMetKeys.PRODUCT_NAME));
        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_STRUCTURE));
        assertEquals("Flat", met.getMetadata(ProductMetKeys.PRODUCT_STRUCTURE));
        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_TRANSFER_STATUS));
        assertEquals(Product.STATUS_RECEIVED, met.getMetadata(ProductMetKeys.PRODUCT_TRANSFER_STATUS));

        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_ORIG_REFS));
        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_DATASTORE_REFS));
        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_FILE_SIZES));
        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_MIME_TYPES));

        assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_ORIG_REFS).size());
        assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_DATASTORE_REFS).size());
        assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_FILE_SIZES).size());
        assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_MIME_TYPES).size());

        URL refUrl = this.getClass().getResource("/ingest/test.txt");

        String origPath = null;
        try {
            origPath = new File(refUrl.getFile()).getCanonicalPath();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertEquals(origPath, met.getMetadata(ProductMetKeys.PRODUCT_ORIG_REFS));
        assertEquals("/tmp/test.txt/test.txt", met.getMetadata(ProductMetKeys.PRODUCT_DATASTORE_REFS));

        assertNotNull(met.getMetadata(ProductMetKeys.PRODUCT_FILE_SIZES));
        assertEquals("text/plain", met.getMetadata(ProductMetKeys.PRODUCT_MIME_TYPES));

        try {
            met = fmc.getReducedMetadata(fmc.getProductByName("test.txt"), Collections.EMPTY_LIST);
        } catch (CatalogException e) {
            fail(e.getMessage());
        }

        assertNotNull(met);
        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_ID));
        assertNotNull(met.getMetadata(ProductMetKeys.PRODUCT_ID));
        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_NAME));
        assertNotNull(met.getMetadata(ProductMetKeys.PRODUCT_NAME));
        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_STRUCTURE));
        assertEquals("Flat", met.getMetadata(ProductMetKeys.PRODUCT_STRUCTURE));
        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_TRANSFER_STATUS));
        assertEquals(Product.STATUS_RECEIVED, met.getMetadata(ProductMetKeys.PRODUCT_TRANSFER_STATUS));

        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_ORIG_REFS));
        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_DATASTORE_REFS));
        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_FILE_SIZES));
        assertTrue(met.containsKey(ProductMetKeys.PRODUCT_MIME_TYPES));

        assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_ORIG_REFS).size());
        assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_DATASTORE_REFS).size());
        assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_FILE_SIZES).size());
        assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_MIME_TYPES).size());

        origPath = null;
        try {
            origPath = new File(refUrl.getFile()).getCanonicalPath();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertEquals(origPath, met.getMetadata(ProductMetKeys.PRODUCT_ORIG_REFS));
        assertEquals("/tmp/test.txt/test.txt", met.getMetadata(ProductMetKeys.PRODUCT_DATASTORE_REFS));

        assertNotNull(met.getMetadata(ProductMetKeys.PRODUCT_FILE_SIZES));
        assertEquals("text/plain", met.getMetadata(ProductMetKeys.PRODUCT_MIME_TYPES));
    }

    private void ingestFile() {
        StdIngester ingester = new StdIngester(TRANSFER_SERVICE_FACTORY_CLASS);

        try {
            URL ingestUrl = this.getClass().getResource("/ingest");
            URL refUrl = this.getClass().getResource("/ingest/test.txt");
            URL metUrl = this.getClass().getResource("/ingest/test.txt.met");
            Metadata prodMet = new SerializableMetadata(new FileInputStream(new File(metUrl.getFile())));

            // now add the right file location
            prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(ingestUrl.getFile()).getCanonicalPath());
            prodMet.addMetadata(CoreMetKeys.FILENAME, "test.txt");
            prodMet.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
            ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(refUrl.getFile()), prodMet);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @After
    public void tearDownTest() throws Exception {
        if (fileManager != null) {
            fileManager.shutdown();
        }

        ConfigPublisher.main(new String[]{
                "-connectString", zookeeper.getConnectString(),
                "-config", CONF_PUBLISHER_XML,
                "-a", "clear"
        });

        System.clearProperty("org.apache.oodt.cas.cli.action.spring.config");
        System.clearProperty("org.apache.oodt.cas.cli.option.spring.config");
        System.clearProperty(ENABLE_DISTRIBUTED_CONFIGURATION);
    }
}
