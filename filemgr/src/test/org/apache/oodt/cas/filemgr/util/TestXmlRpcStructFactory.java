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

package org.apache.oodt.cas.filemgr.util;

import org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory;
import org.apache.oodt.cas.filemgr.structs.ExtractorSpec;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.type.TypeHandler;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManager;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import junit.framework.TestCase;

public class TestXmlRpcStructFactory extends TestCase {

    final int FILEMGR_PORT = 9999;
    
    XmlRpcFileManager fmServer;
 
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        startXmlRpcFileManager();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        fmServer.shutdown();
    }

    public void testProductTypeMethods() throws RepositoryManagerException, MalformedURLException, ConnectionException {
        XmlRpcFileManagerClient fmClient = new XmlRpcFileManagerClient(new URL("http://localhost:" + FILEMGR_PORT));
        fmClient.setDataTransfer(new LocalDataTransferFactory().createDataTransfer());
        
        ProductType productType = fmClient.getProductTypeByName("GenericFile");
        Hashtable<String, Object> productTypeHash = XmlRpcStructFactory.getXmlRpcProductType(productType);
        ProductType convBackProdType = XmlRpcStructFactory.getProductTypeFromXmlRpc(productTypeHash);
        
        assertTrue(productType.getProductTypeId().equals(convBackProdType.getProductTypeId()));
        assertTrue(productType.getName().equals(convBackProdType.getName()));
        assertTrue(productType.getDescription().equals(convBackProdType.getDescription()));
        assertTrue(productType.getVersioner().equals(convBackProdType.getVersioner()));
        assertTrue(productType.getProductRepositoryPath().equals(convBackProdType.getProductRepositoryPath()));
        for (int i = 0; i < productType.getExtractors().size(); i++) {
            ExtractorSpec spec1 = productType.getExtractors().get(i);
            ExtractorSpec spec2 = convBackProdType.getExtractors().get(i);
            assertTrue(spec1.getClassName().equals(spec2.getClassName()));
            assertTrue(spec1.getConfiguration().equals(spec2.getConfiguration()));
        }
        for (int i = 0; i < productType.getHandlers().size(); i++) {
            TypeHandler handler1 = productType.getHandlers().get(i);
            TypeHandler handler2 = convBackProdType.getHandlers().get(i);
            assertTrue(handler1.getClass().getCanonicalName().equals(handler2.getClass().getCanonicalName()));
            assertTrue(handler1.getElementName().equals(handler2.getElementName()));
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
        
        System.setProperty("filemgr.catalog.factory",
                "org.apache.oodt.cas.filemgr.catalog.LuceneCatalogFactory");
        System.setProperty(
                "org.apache.oodt.cas.filemgr.catalog.lucene.idxPath",
                "./src/testdata/ingest/cat");

        // now override the repo mgr policy
        try {
            System.setProperty(
                    "org.apache.oodt.cas.filemgr.repositorymgr.dirs",
                    "file://"
                            + new File("./src/testdata/xmlrpc-struct-factory")
                                    .getCanonicalPath());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // now override the val layer ones
        System.setProperty("org.apache.oodt.cas.filemgr.validation.dirs",
                "file://"
                        + new File("./src/testdata/xmlrpc-struct-factory")
                                .getAbsolutePath());

        // set up mime repo path
        System.setProperty(
                "org.apache.oodt.cas.filemgr.mime.type.repository", new File(
                        "./src/main/resources/mime-types.xml").getAbsolutePath());

        try {
            fmServer = new XmlRpcFileManager(FILEMGR_PORT);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
}
