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


package org.apache.oodt.cas.filemgr.catalog;

import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.BooleanQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.metadata.Metadata;

import com.google.common.collect.Lists;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import org.junit.Ignore;

/**
 * @author woollard
 * @author mattmann
 * 
 * @version $Revision$
 * 
 * <p>
 * Test suite for the {@link LuceneCatalog} and {@link LuceneCatalogFactory}.
 * </p>.
 */
public class TestLuceneCatalog extends TestCase {
    private static Logger LOG = Logger.getLogger(TestLuceneCatalog.class.getName());
    private LuceneCatalog myCat;

    private String tmpDirPath = null;

    private static final int catPageSize = 20;

    private Properties initialProperties = new Properties(
      System.getProperties());

    public void setUpProperties() {

        Properties properties = new Properties(System.getProperties());

        // set the log levels
        URL loggingPropertiesUrl = this.getClass().getResource(
            "/test.logging.properties");
        System.setProperty("java.util.logging.config.file", new File(
            loggingPropertiesUrl.getFile()).getAbsolutePath());

        // first load the example configuration
        try {
            URL filemgrPropertiesUrl = this.getClass().getResource(
                "/filemgr.properties");
            properties.load(new FileInputStream(
                filemgrPropertiesUrl.getFile()));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // get a temp directory

        File tempDir = null;
        File tempFile;

        try {
            tempFile = File.createTempFile("foo", "bar");
            tempFile.deleteOnExit();
            tempDir = tempFile.getParentFile();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        tmpDirPath = tempDir.getAbsolutePath();
        if (!tmpDirPath.endsWith("/")) {
            tmpDirPath += "/";
        }

        tmpDirPath += "testCat/";

        // now override the catalog ones
        System.setProperty(
                "org.apache.oodt.cas.filemgr.catalog.lucene.idxPath",
                tmpDirPath);

        System.setProperty(
                "org.apache.oodt.cas.filemgr.catalog.lucene.pageSize", "20");

        System.setProperty(
                        "org.apache.oodt.cas.filemgr.catalog.lucene.commitLockTimeout.seconds",
                        "60");

        System.setProperty(
                        "org.apache.oodt.cas.filemgr.catalog.lucene.writeLockTimeout.seconds",
                        "60");

        System.setProperty(
                "org.apache.oodt.cas.filemgr.catalog.lucene.mergeFactor",
                "20");

        System.setProperty(
            "org.apache.oodt.cas.filemgr.catalog.datasource.lenientFields",
            "false");
        // now override the val layer ones
        URL examplesCoreUrl = this.getClass().getResource(
            "/examples/core");
        System.setProperty("org.apache.oodt.cas.filemgr.validation.dirs",
            "file://" + new File(examplesCoreUrl.getFile()).getAbsolutePath());

        //System.setProperties(properties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        setUpProperties();
        myCat = (LuceneCatalog) new LuceneCatalogFactory().createCatalog();
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        // now remove the temporary directory used

        if (tmpDirPath != null) {
            File tmpDir = new File(tmpDirPath);
            File[] tmpFiles = tmpDir.listFiles();

            if (tmpFiles != null && tmpFiles.length > 0) {
                for (File tmpFile : tmpFiles) {
                    tmpFile.delete();
                }

                tmpDir.delete();
            }

            if (myCat != null) {
                myCat = null;
            }
        }

      //  System.setProperties(initialProperties);
    }
    
    /**
    * @since OODT-382
    */
    public void testNoCatalogDirectoryQueries() {
        // Test querying against a catalog directory that has not yet been created or is empty. 
        // The LuceneCatalogFactory should be creating the index directory if not there.
        try {
            myCat.getTopNProducts(10);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testGetMetadata() throws CatalogException {
       Product product = getTestProduct();
       myCat.addProduct(product);
       myCat.addProductReferences(product);
       Metadata m = new Metadata();
       m.addMetadata(CoreMetKeys.FILE_LOCATION, Lists.newArrayList("/loc/1", "/loc/2"));
       myCat.addMetadata(m, product);
       Metadata rndTripMet = myCat.getMetadata(product);

       assertNotNull(rndTripMet);
       assertEquals(2, rndTripMet.getAllMetadata(CoreMetKeys.FILE_LOCATION).size());
       assertTrue(rndTripMet.getAllMetadata(CoreMetKeys.FILE_LOCATION).contains("/loc/1"));
       assertTrue(rndTripMet.getAllMetadata(CoreMetKeys.FILE_LOCATION).contains("/loc/2"));
    }
    
    public void testGetReducedMetadata() throws CatalogException {
       Product product = getTestProduct();
       myCat.addProduct(product);
       myCat.addProductReferences(product);
       Metadata m = new Metadata();
       m.addMetadata(CoreMetKeys.FILE_LOCATION, Lists.newArrayList("/loc/1", "/loc/2"));
       myCat.addMetadata(m, product);
       Metadata rndTripMet = myCat.getReducedMetadata(product,
             Lists.newArrayList(CoreMetKeys.FILE_LOCATION));

       assertNotNull(rndTripMet);
       assertEquals(2, rndTripMet.getAllMetadata(CoreMetKeys.FILE_LOCATION).size());
       assertTrue(rndTripMet.getAllMetadata(CoreMetKeys.FILE_LOCATION).contains("/loc/1"));
       assertTrue(rndTripMet.getAllMetadata(CoreMetKeys.FILE_LOCATION).contains("/loc/2"));
    }

    public void testGetReducedMetadataNull() throws CatalogException {
	      Product p = getTestProduct();
	      myCat.addProduct(p);
	      myCat.addProductReferences(p);
	      myCat.addMetadata(new Metadata(), p);

	      // should not throw NPE here
	      Metadata rndTripMet = myCat.getReducedMetadata(p, Lists.newArrayList(CoreMetKeys.FILENAME));

	      assertNotNull(rndTripMet);
	      // should return null if met key has no value
	      assertNull(rndTripMet.getAllMetadata(CoreMetKeys.FILENAME));
    }

    public void testRemoveProduct() {
        Product productToRemove = getTestProduct();

        // override name
        productToRemove.setProductName("removeme");
        try {
            myCat.addProduct(productToRemove);
            myCat.addMetadata(getTestMetadata("tempProduct"), productToRemove);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Product retProduct = null;
        ProductType type = new ProductType();
        type.setName("GenericFile");
        type.setProductTypeId("urn:oodt:GenericFile");

        try {
            retProduct = myCat.getProductByName("removeme");
            retProduct.setProductType(type);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertNotNull(retProduct);

        try {
            myCat.removeProduct(retProduct);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Product retProdAfterRemove = null;
        try {
            retProdAfterRemove = myCat.getProductByName("removeme");
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNull(retProdAfterRemove);

    }

    public void testModifyProduct() {
        Product testProduct = getTestProduct();
        try {
            myCat.addProduct(testProduct);
            myCat.addMetadata(getTestMetadata("tempProduct"), testProduct);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(testProduct);
        assertEquals("test", testProduct.getProductName());
        // now change something
        testProduct.setProductName("f002");
        try {
            myCat.modifyProduct(testProduct);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(testProduct);

        Product retProduct;
        try {
            retProduct = myCat.getProductByName("f002");
            assertNotNull(retProduct);
            assertEquals("f002", retProduct.getProductName());
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    /**
     * @since OODT-133
     * 
     */
    public void testFirstProductOnlyOnFirstPage() {
        // add catPageSize of the test Product
        // then add a product called "ShouldBeFirstForPage.txt"
        // make sure it's the first one on the 2nd page

        Product testProd = getTestProduct();
        Metadata met = getTestMetadata("test");

        for (int i = 0; i < catPageSize; i++) {
            try {
                myCat.addProduct(testProd);
                myCat.addMetadata(met, testProd);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        testProd.setProductName("ShouldBeFirstForPage.txt");
        met.replaceMetadata("CAS.ProdutName", "ShouldBeFirstForPage.txt");

        try {
            myCat.addProduct(testProd);
            myCat.addMetadata(met, testProd);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            assertNotNull(myCat.getProducts());
            assertEquals(21, myCat.getProducts().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ProductType type = new ProductType();
        type.setProductTypeId("urn:oodt:GenericFile");
        type.setName("GenericFile");
        assertNotNull(myCat.getFirstPage(type));
        assertNotNull(myCat.getFirstPage(type).getPageProducts());
        assertEquals(catPageSize, myCat.getFirstPage(type).getPageProducts()
                .size());
        ProductPage page = myCat.getNextPage(type, myCat.getFirstPage(type));
        assertNotNull(page);
        assertNotNull(page.getPageProducts());
        assertEquals(1, page.getPageProducts().size());
        assertEquals(2, page.getTotalPages());
        assertNotNull(page.getPageProducts().get(0));
        Product retProd = page.getPageProducts().get(0);
        assertEquals("ShouldBeFirstForPage.txt", retProd.getProductName());
    }

    public void testGetLastProductOnLastPage() {
        // add catPageSize of the test Product
        // then add a product called "ShouldBeFirstForPage.txt"
        // make sure it's the first one on the 2nd page

        Product testProd = getTestProduct();
        Metadata met = getTestMetadata("test");

        for (int i = 0; i < catPageSize; i++) {
            try {
                myCat.addProduct(testProd);
                myCat.addMetadata(met, testProd);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        testProd.setProductName("ShouldBeFirstForPage.txt");
        met.replaceMetadata("CAS.ProdutName", "ShouldBeFirstForPage.txt");

        try {
            myCat.addProduct(testProd);
            myCat.addMetadata(met, testProd);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            assertNotNull(myCat.getProducts());
            assertEquals(21, myCat.getProducts().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ProductType type = new ProductType();
        type.setProductTypeId("urn:oodt:GenericFile");
        type.setName("GenericFile");
        assertNotNull(myCat.getLastProductPage(type));
        assertNotNull(myCat.getLastProductPage(type).getPageProducts());
        assertEquals(1, myCat.getLastProductPage(type).getPageProducts()
                                       .size());
        ProductPage page = myCat.getLastProductPage(type);
        assertNotNull(page);
        assertNotNull(page.getPageProducts());
        assertEquals(1, page.getPageProducts().size());
        assertEquals(2, page.getTotalPages());
        List<Product> prods = page.getPageProducts();
        assertNotNull(page.getPageProducts().get(0));
        Product retProd = page.getPageProducts().get(0);
        assertEquals("ShouldBeFirstForPage.txt", retProd.getProductName());
    }

    public void testGetTopNProducts() {
        // add catPageSize of the test Product
        // then add a product called "ShouldBeFirstForPage.txt"
        // make sure it's the first one on the 2nd page

        Product testProd = getTestProduct();
        Metadata met = getTestMetadata("test");

        for (int i = 0; i < catPageSize; i++) {
            try {
                myCat.addProduct(testProd);
                myCat.addMetadata(met, testProd);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        testProd.setProductName("ShouldBeFirstForPage.txt");
        met.replaceMetadata("CAS.ProdutName", "ShouldBeFirstForPage.txt");

        try {
            myCat.addProduct(testProd);
            myCat.addMetadata(met, testProd);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            assertNotNull(myCat.getProducts());
            assertEquals(21, myCat.getProducts().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            assertNotNull(myCat.getTopNProducts(5));
            assertEquals(5, myCat.getTopNProducts(5).size());
            Product retProd = myCat.getTopNProducts(5).get(0);
            assertEquals("test", retProd.getProductName());
        } catch (CatalogException e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }

    }

    public void testGetNextPageNullType(){
        Product testProd = getTestProduct();
        Metadata met = getTestMetadata("test");

        for (int i = 0; i < catPageSize; i++) {
            try {
                myCat.addProduct(testProd);
                myCat.addMetadata(met, testProd);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        testProd.setProductName("ShouldBeFirstForPage.txt");
        met.replaceMetadata("CAS.ProdutName", "ShouldBeFirstForPage.txt");

        try {
            myCat.addProduct(testProd);
            myCat.addMetadata(met, testProd);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            assertNotNull(myCat.getProducts());
            assertEquals(21, myCat.getProducts().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ProductType type = new ProductType();
        type.setProductTypeId("urn:oodt:GenericFile");
        type.setName("GenericFile");
        assertNotNull(myCat.getFirstPage(type));
        assertNotNull(myCat.getFirstPage(type).getPageProducts());
        assertEquals(catPageSize, myCat.getFirstPage(type).getPageProducts()
                                       .size());

        ProductPage page = myCat.getNextPage(null, myCat.getFirstPage(type));


        assertNotNull(page);
        assertEquals(0, page.getPageNum());
        assertEquals(0, page.getTotalPages());
        assertEquals(0, page.getPageSize());
    }

    public void testGetNextPageNullCurrentPage(){
        Product testProd = getTestProduct();
        Metadata met = getTestMetadata("test");

        for (int i = 0; i < catPageSize; i++) {
            try {
                myCat.addProduct(testProd);
                myCat.addMetadata(met, testProd);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        testProd.setProductName("ShouldBeFirstForPage.txt");
        met.replaceMetadata("CAS.ProdutName", "ShouldBeFirstForPage.txt");

        try {
            myCat.addProduct(testProd);
            myCat.addMetadata(met, testProd);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            assertNotNull(myCat.getProducts());
            assertEquals(21, myCat.getProducts().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ProductType type = new ProductType();
        type.setProductTypeId("urn:oodt:GenericFile");
        type.setName("GenericFile");
        assertNotNull(myCat.getFirstPage(type));
        assertNotNull(myCat.getFirstPage(type).getPageProducts());
        assertEquals(catPageSize, myCat.getFirstPage(type).getPageProducts()
                                       .size());
        ProductPage page = myCat.getNextPage(type, null);
        assertNotNull(page);
        assertNotNull(page.getPageProducts());
        assertEquals(20, page.getPageProducts().size());
        assertEquals(2, page.getTotalPages());
        assertNotNull(page.getPageProducts().get(0));
        Product retProd = page.getPageProducts().get(0);
        assertEquals("test", retProd.getProductName());
    }

    public void testGetNextPageCurrentPageIsLastPage(){
        Product testProd = getTestProduct();
        Metadata met = getTestMetadata("test");

        for (int i = 0; i < catPageSize; i++) {
            try {
                myCat.addProduct(testProd);
                myCat.addMetadata(met, testProd);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        testProd.setProductName("ShouldBeFirstForPage.txt");
        met.replaceMetadata("CAS.ProdutName", "ShouldBeFirstForPage.txt");

        try {
            myCat.addProduct(testProd);
            myCat.addMetadata(met, testProd);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            assertNotNull(myCat.getProducts());
            assertEquals(21, myCat.getProducts().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ProductType type = new ProductType();
        type.setProductTypeId("urn:oodt:GenericFile");
        type.setName("GenericFile");
        assertNotNull(myCat.getFirstPage(type));
        assertNotNull(myCat.getFirstPage(type).getPageProducts());
        assertEquals(catPageSize, myCat.getFirstPage(type).getPageProducts()
                                       .size());
        ProductPage page = myCat.getNextPage(type, myCat.getLastProductPage(type));
        assertNotNull(page);
        assertNotNull(page.getPageProducts());
        assertEquals(1, page.getPageProducts().size());
        assertEquals(2, page.getTotalPages());
        assertNotNull(page.getPageProducts().get(0));
        Product retProd = page.getPageProducts().get(0);
        assertEquals("ShouldBeFirstForPage.txt", retProd.getProductName());
    }

    public void testGetPrevPage(){
        Product testProd = getTestProduct();
        Metadata met = getTestMetadata("test");

        for (int i = 0; i < catPageSize; i++) {
            try {
                myCat.addProduct(testProd);
                myCat.addMetadata(met, testProd);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        testProd.setProductName("ShouldBeFirstForPage.txt");
        met.replaceMetadata("CAS.ProdutName", "ShouldBeFirstForPage.txt");

        try {
            myCat.addProduct(testProd);
            myCat.addMetadata(met, testProd);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            assertNotNull(myCat.getProducts());
            assertEquals(21, myCat.getProducts().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ProductType type = new ProductType();
        type.setProductTypeId("urn:oodt:GenericFile");
        type.setName("GenericFile");
        assertNotNull(myCat.getFirstPage(type));
        assertNotNull(myCat.getFirstPage(type).getPageProducts());
        assertEquals(catPageSize, myCat.getFirstPage(type).getPageProducts()
                                       .size());
        ProductPage page2 = myCat.getNextPage(type, myCat.getFirstPage(type));
        ProductPage page = myCat.getPrevPage(type, page2);
        assertEquals(2, page2.getPageNum());
        assertEquals(1, page.getPageNum());
    }

    public void testGetPrevPageNullCurrentPage(){
        Product testProd = getTestProduct();
        Metadata met = getTestMetadata("test");

        for (int i = 0; i < catPageSize; i++) {
            try {
                myCat.addProduct(testProd);
                myCat.addMetadata(met, testProd);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        testProd.setProductName("ShouldBeFirstForPage.txt");
        met.replaceMetadata("CAS.ProdutName", "ShouldBeFirstForPage.txt");

        try {
            myCat.addProduct(testProd);
            myCat.addMetadata(met, testProd);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            assertNotNull(myCat.getProducts());
            assertEquals(21, myCat.getProducts().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ProductType type = new ProductType();
        type.setProductTypeId("urn:oodt:GenericFile");
        type.setName("GenericFile");
        assertNotNull(myCat.getFirstPage(type));
        assertNotNull(myCat.getFirstPage(type).getPageProducts());
        assertEquals(catPageSize, myCat.getFirstPage(type).getPageProducts()
                                       .size());
        ProductPage page = myCat.getPrevPage(type, null);
        assertNotNull(page);
        assertNotNull(page.getPageProducts());
        assertEquals(20, page.getPageProducts().size());
        assertEquals(2, page.getTotalPages());
        assertNotNull(page.getPageProducts().get(0));
        Product retProd = page.getPageProducts().get(0);
        assertEquals("test", retProd.getProductName());
    }

    public void testGetPrevPageCurrentPageIsFirstPage(){
        Product testProd = getTestProduct();
        Metadata met = getTestMetadata("test");

        for (int i = 0; i < catPageSize; i++) {
            try {
                myCat.addProduct(testProd);
                myCat.addMetadata(met, testProd);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        testProd.setProductName("ShouldBeFirstForPage.txt");
        met.replaceMetadata("CAS.ProdutName", "ShouldBeFirstForPage.txt");

        try {
            myCat.addProduct(testProd);
            myCat.addMetadata(met, testProd);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            assertNotNull(myCat.getProducts());
            assertEquals(21, myCat.getProducts().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ProductType type = new ProductType();
        type.setProductTypeId("urn:oodt:GenericFile");
        type.setName("GenericFile");
        assertNotNull(myCat.getFirstPage(type));
        assertNotNull(myCat.getFirstPage(type).getPageProducts());
        assertEquals(catPageSize, myCat.getFirstPage(type).getPageProducts()
                                       .size());
        ProductPage page = myCat.getPrevPage(type, myCat.getFirstPage(type));
        assertNotNull(page);
        assertNotNull(page.getPageProducts());
        assertEquals(20, page.getPageProducts().size());
        assertEquals(2, page.getTotalPages());
        assertNotNull(page.getPageProducts().get(0));
        Product retProd = page.getPageProducts().get(0);
        assertEquals("test", retProd.getProductName());
    }

    public void testGetPrevPageNullProductType(){
        Product testProd = getTestProduct();
        Metadata met = getTestMetadata("test");

        for (int i = 0; i < catPageSize; i++) {
            try {
                myCat.addProduct(testProd);
                myCat.addMetadata(met, testProd);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        testProd.setProductName("ShouldBeFirstForPage.txt");
        met.replaceMetadata("CAS.ProdutName", "ShouldBeFirstForPage.txt");

        try {
            myCat.addProduct(testProd);
            myCat.addMetadata(met, testProd);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            assertNotNull(myCat.getProducts());
            assertEquals(21, myCat.getProducts().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ProductType type = new ProductType();
        type.setProductTypeId("urn:oodt:GenericFile");
        type.setName("GenericFile");
        assertNotNull(myCat.getFirstPage(type));
        assertNotNull(myCat.getFirstPage(type).getPageProducts());
        assertEquals(catPageSize, myCat.getFirstPage(type).getPageProducts()
                                       .size());
        ProductPage page2 = myCat.getNextPage(type, myCat.getFirstPage(type));
        ProductPage page = myCat.getPrevPage(null, page2);
        assertNotNull(page);
        assertEquals(0, page.getPageNum());
        assertEquals(0, page.getPageSize());
        assertEquals(0, page.getTotalPages());

    }

    public void testGetTopNProductsByType() {

        Product testProd = getTestProduct();
        Metadata met = getTestMetadata("test");

        for (int i = 0; i < catPageSize; i++) {
            try {
                myCat.addProduct(testProd);
                myCat.addMetadata(met, testProd);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        testProd.setProductName("ShouldBeFirstForPage.txt");
        met.replaceMetadata("CAS.ProdutName", "ShouldBeFirstForPage.txt");

        try {
            myCat.addProduct(testProd);
            myCat.addMetadata(met, testProd);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            assertNotNull(myCat.getProducts());
            assertEquals(21, myCat.getProducts().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ProductType type = new ProductType();
        type.setProductTypeId("urn:oodt:GenericFile");
        type.setName("GenericFile");
        try {
            assertNotNull(myCat.getTopNProducts(5, type));
            assertEquals(5, myCat.getTopNProducts(5, type).size());
            Product retProd = myCat.getTopNProducts(5, type).get(0);
            assertEquals("test", retProd.getProductName());
        } catch (CatalogException e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }

    }

    /**
     * @since OODT-141
     */
    public void testTopResults(){
      Product testProduct = getTestProduct();
      try{
        myCat.addProduct(testProduct);
        myCat.addMetadata(getTestMetadata("tempProduct"), testProduct);
        myCat.getTopNProducts(20);
      }
      catch(Exception e){
        LOG.log(Level.SEVERE, e.getMessage());
        fail(e.getMessage());
      }
    }

    public void testAddProduct() {

        Product testProduct = getTestProduct();
        try {
            myCat.addProduct(testProduct);
            myCat.addMetadata(getTestMetadata("tempProduct"), testProduct);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        Product retProduct;
        try {
            retProduct = myCat.getProductByName("test");
            assertNotNull(retProduct);
            assertEquals("test", retProduct.getProductName());
            assertEquals(Product.STRUCTURE_FLAT, retProduct
                    .getProductStructure());
            assertNotNull(retProduct.getProductType());
            assertEquals("urn:oodt:GenericFile", retProduct.getProductType()
                    .getProductTypeId());
            assertEquals(Product.STATUS_TRANSFER, retProduct
                    .getTransferStatus());
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    public void testAddMetadata() {
        Metadata met = new Metadata();
        met.addMetadata("ProductStructure", Product.STRUCTURE_FLAT);

        Product testProduct = getTestProduct();
        try {
            myCat.addProduct(testProduct);
            myCat.addMetadata(met, testProduct);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        try {
            Metadata retMet = myCat.getMetadata(testProduct);
            assertNotNull(retMet);
            assertTrue(retMet.containsKey(CoreMetKeys.PRODUCT_STRUCTURE));
            assertEquals(Product.STRUCTURE_FLAT, retMet
                    .getMetadata(CoreMetKeys.PRODUCT_STRUCTURE));
        } catch (CatalogException e) {
            fail(e.getMessage());
        }

    }

    public void testRemoveMetadata() {
        Metadata met = new Metadata();
        met.addMetadata("Filename", "tempProduct");
        met.addMetadata("ProductStructure", "Flat");

        Product testProduct = getTestProduct();
        try {
            myCat.addProduct(testProduct);
            myCat.addMetadata(met, testProduct);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        try {
            myCat.removeMetadata(met, testProduct);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }

        try {
            Metadata retMet = myCat.getMetadata(testProduct);
            String retValue = retMet.getMetadata("Filename");
            assertNull(retValue);
        } catch (CatalogException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
        }
    }
    
    public void testPagedQuery(){
    	// Add a couple of Products and associated Metadata
    	Product testProduct = null;
    	for(int i = 0; i < catPageSize + 1; i++){
    		testProduct = Product.getDefaultFlatProduct("test" + i,
					"urn:oodt:GenericFile");
    		testProduct.getProductType().setName("GenericFile");
    		Reference ref = new Reference("file:///foo.txt", "file:///bar.txt", 100);
            Vector<Reference> references = new Vector<Reference>();
            references.add(ref);
            testProduct.setProductReferences(references);
    		Metadata met = new Metadata();
    		met.addMetadata("Filename", "tempProduct" + i);
    		met.addMetadata("ProductStructure", "Flat");
    		try {
                myCat.addProduct(testProduct);
                myCat.addMetadata(met, testProduct);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, e.getMessage());
                fail(e.getMessage());
            }
    	}
    	
    	// Formulate a test query
    	Query query = new Query();
    	BooleanQueryCriteria bqc = new BooleanQueryCriteria();
    	try{
    		bqc.setOperator(BooleanQueryCriteria.AND);
    	}catch (Exception e){
    		LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
    	}
    	TermQueryCriteria tqc = new TermQueryCriteria();
    	tqc.setElementName("ProductStructure");
    	tqc.setValue("Flat");
    	try{
    		bqc.addTerm(tqc);
    	}catch (Exception e){
    		LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
    	}
    	tqc = new TermQueryCriteria();
    	tqc.setElementName("Filename");
    	tqc.setValue("tempProduct1");
    	try{
    		bqc.addTerm(tqc);
    	}catch (Exception e){
    		LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
    	}
    	query.addCriterion(bqc);
    	
    	// Perform the query and validate results
    	ProductPage page = null;
    	try{
    		page = myCat.pagedQuery(query, testProduct.getProductType(), 1);
    	}catch (Exception e){
    		LOG.log(Level.SEVERE, e.getMessage());
            fail(e.getMessage());
    	}
    	assertEquals(page.getPageProducts().size(), 1);
    	assertEquals(page.getPageProducts().get(0).getProductName(), "test1");
    	assertEquals(page.getPageNum(), 1);
    	assertEquals(page.getTotalPages(), 1);
    }

    /*@Ignore
    public void testNullIndexPath(){
        System.clearProperty("org.apache.oodt.cas.filemgr.catalog.lucene.idxPath");
        Properties sysProps = System.getProperties();
        sysProps.remove("org.apache.oodt.cas.filemgr.catalog.lucene.idxPath");



        try{
            LuceneCatalogFactory fact = new LuceneCatalogFactory();
            fail( "Missing exception" );

        } catch( IllegalArgumentException e ) {
            Assert.assertThat(e.getMessage(), CoreMatchers.containsString("error initializing lucene catalog: "));
        }
    }*/

    public void testCreateCatalogException(){

        //TODO Use the TestAppender to make sure that an exception thrown is caught and logged.
    }

    private static Product getTestProduct() {
        Product testProduct = Product.getDefaultFlatProduct("test",
                "urn:oodt:GenericFile");
        testProduct.getProductType().setName("GenericFile");

        // set references
        Reference ref = new Reference("file:///foo.txt", "file:///bar.txt", 100);
        Vector references = new Vector();
        references.add(ref);
        testProduct.setProductReferences(references);

        return testProduct;
    }

    private static Metadata getTestMetadata(String prodName) {
        Metadata met = new Metadata();
        met.addMetadata("CAS.ProductName", prodName);
        return met;
    }

}
