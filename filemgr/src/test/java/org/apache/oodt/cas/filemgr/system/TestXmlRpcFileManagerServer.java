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

package org.apache.oodt.cas.filemgr.system;

import org.apache.oodt.cas.filemgr.metadata.ProductMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;

/**
 * Test harness for the XmlRpcFileManager.
 *
 * @since OODT-72
 */
public class TestXmlRpcFileManagerServer extends AbstractFileManagerServerTest {

    private static Logger LOG = LoggerFactory.getLogger(TestXmlRpcFileManagerServer.class);

    /**
     * @since OODT-72
     */
    public void testExpandProductMet() {
        FileManagerClient fmc = null;
        try {
            fmc = new XmlRpcFileManagerClient(new URL("http://localhost:" + FM_PORT));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Metadata met = null;
        try {
            met = fmc.getMetadata(fmc.getProductByName("test.txt"));
        } catch (CatalogException e) {
            LOG.error(e.getMessage(), e);
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
            LOG.error(e.getMessage(), e);
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

    @Override
    protected void setProperties() {
        System.setProperty("filemgr.server", "org.apache.oodt.cas.filemgr.system.rpc.XmlRpcFileManagerServerFactory");
        System.setProperty("filemgr.client", "org.apache.oodt.cas.filemgr.system.rpc.XmlRpcFileManagerClientFactory");
    }

    @Override
    protected FileManagerServer newFileManagerServer(int port) throws Exception {
        return new XmlRpcFileManagerServer(port);
    }

    @Override
    protected boolean shouldExpandProduct() {
        return true;
    }
}
