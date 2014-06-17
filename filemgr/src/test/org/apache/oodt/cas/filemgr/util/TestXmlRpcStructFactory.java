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

//OODT imports
import org.apache.oodt.cas.filemgr.datatransfer.InPlaceDataTransferFactory;
import org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory;
import org.apache.oodt.cas.filemgr.structs.ExtractorSpec;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.type.TypeHandler;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManager;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;

//Google imports
import com.google.common.collect.Lists;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link XmlRpcStructFactory}.
 *
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 */
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

    public void testProductMethods() throws Exception {
       XmlRpcFileManagerClient fmClient = new XmlRpcFileManagerClient(new URL("http://localhost:" + FILEMGR_PORT));
       fmClient.setDataTransfer(new InPlaceDataTransferFactory().createDataTransfer());

       Product product = new Product();
       Product roundTripProduct = XmlRpcStructFactory.getProductFromXmlRpc(
             XmlRpcStructFactory.getXmlRpcProduct(product));
       assertEquals(product, roundTripProduct);

       product = new Product();
       product.setProductId("TestId");
       product.setProductName("TestName");
       product.setProductReferences(Lists.newArrayList(new Reference("file:///original/path", null, 2)));
       product.setProductStructure("Flat");
       product.setProductType(fmClient.getProductTypeByName("GenericFile"));
       product.setRootRef(new Reference("file:///original/root", "file:///datastore/root", 3));
       roundTripProduct = XmlRpcStructFactory.getProductFromXmlRpc(XmlRpcStructFactory.getXmlRpcProduct(product));
       assertEquals(product, roundTripProduct);
       Metadata m = new Metadata();
       m.addMetadata("TestKey", "TestValue");
       
       roundTripProduct = fmClient.getProductById(
             fmClient.ingestProduct(product, m, true));
       assertEquals(product, roundTripProduct);
    }

    private void assertEquals(Product product1, Product product2) {
       if (product1 == null) {
          assertNull(product2);
          return;
       }
       assertEquals(product1.getProductId(), product2.getProductId());       
       assertEquals(product1.getProductName(), product2.getProductName());
       assertEquals(product1.getProductStructure(), product2.getProductStructure());
       assertEquals(product1.getTransferStatus(), product2.getTransferStatus());
       if (product1.getProductReferences() == null) {
          assertEquals(product1.getProductReferences(), product2.getProductReferences());
       } else {
          for (int i = 0; i < product1.getProductReferences().size(); i++) {
             assertEquals(product1.getProductReferences().get(i),
                   product2.getProductReferences().get(i));
          }
       }
       if (product1.getProductType() == null) {
          assertEquals(product1.getProductType(), product2.getProductType());
       } else {
          assertEquals(product1.getProductType().getDescription(), product2.getProductType().getDescription());          
          assertEquals(product1.getProductType().getName(), product2.getProductType().getName());          
          assertEquals(product1.getProductType().getProductRepositoryPath(), product2.getProductType().getProductRepositoryPath());          
          assertEquals(product1.getProductType().getProductTypeId(), product2.getProductType().getProductTypeId());          
          assertEquals(product1.getProductType().getVersioner(), product2.getProductType().getVersioner());          
       }
       assertEquals(product1.getRootRef(), product2.getRootRef());
    }

    private void assertEquals(Reference ref1, Reference ref2) {
       if (ref1 == null) {
          assertNull(ref2);
          return;
       }
       assertNotNull(ref2.getDataStoreReference());
       assertEquals(ref1.getFileSize(), ref2.getFileSize());
       assertEquals(ref1.getOrigReference(), ref2.getOrigReference());
       assertEquals(ref1.getMimeType(), ref2.getMimeType());
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
                "org.apache.oodt.cas.filemgr.catalog.MockCatalogFactory");

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
