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

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import javax.sql.DataSource;

//OODT imports
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;
import org.apache.oodt.commons.database.SqlScript;
import org.apache.oodt.cas.filemgr.util.SqlParser;
import org.apache.oodt.cas.metadata.Metadata;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test suite for the {@link DataSourceCatalog} and
 * {@link DataSourceCatalogFactory}.
 * </p>.
 */
public class TestDataSourceCatalog extends TestCase {

    protected Catalog myCat;

    private String tmpDirPath = null;

    private static final int catPageSize = 20;

    public TestDataSourceCatalog() {
        // set the log levels
        System.setProperty("java.util.logging.config.file", new File(
                "./src/main/resources/logging.properties").getAbsolutePath());

        if(System.getProperty("overrideProperties") == null){

            try {
                System.getProperties().load(
                        new FileInputStream(
                                "./src/main/resources/filemgr.properties"));
            } catch (Exception e) {
                fail(e.getMessage());
            }


        // first load the example configuration


        // get a temp directory
        File tempDir = null;
        File tempFile = null;

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

        tmpDirPath += "testCat";

        // now override the catalog ones
        System.setProperty(
                "org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.url",
                "jdbc:hsqldb:file:" + tmpDirPath + "/testCat;shutdown=true");

        System.setProperty(
                "org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.user",
                "sa");
        System.setProperty(
                "org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.pass",
                "");
        System.setProperty(
                "org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.driver",
                "org.hsqldb.jdbcDriver");
        }
        else{
            try {
                System.getProperties().load(
                        new FileInputStream(
                                System.getProperty("overrideProperties")));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
        // now override the val layer ones
        System.setProperty("org.apache.oodt.cas.filemgr.validation.dirs",
                "file://"
                        + new File("./src/testdata/xmlrpc-struct-factory")
                                .getAbsolutePath());

        // override quote fields
        System.setProperty(
                "org.apache.oodt.cas.filemgr.catalog.datasource.quoteFields",
                "true");

        myCat = getCatalog();

    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        // now create the basic schema for the DB
        createSchema();
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
                for (int i = 0; i < tmpFiles.length; i++) {
                    tmpFiles[i].delete();
                }

                tmpDir.delete();
            }

        }
    }

    public void testRemoveProduct() {
        Product productToRemove = getTestProduct();
        // override name
        productToRemove.setProductName("removeme");
        try {
            myCat.addProduct(productToRemove);
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
        // make sure it's the first one on the 1st page

        Product testProd = getTestProduct();
        Metadata met = getTestMetadata("test");

        for (int i = 0; i < this.catPageSize; i++) {
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
        assertNotNull(myCat.getFirstPage(type).getPageProducts().get(0));
        assertEquals("ShouldBeFirstForPage.txt", ((Product)myCat.getFirstPage(type).getPageProducts().get(0)).getProductName());
        ProductPage page = myCat.getNextPage(type, myCat.getFirstPage(type));
        assertNotNull(page);
        assertNotNull(page.getPageProducts());
        assertEquals(1, page.getPageProducts().size());
        assertEquals(2, page.getTotalPages());
        assertNotNull(page.getPageProducts().get(0));
        Product retProd = ((Product) page.getPageProducts().get(0));
        assertEquals("test", retProd.getProductName());

    }

    public void testAddProduct() {

        Product testProduct = getTestProduct();
        try {
            myCat.addProduct(testProduct);
        } catch (Exception e) {
            e.printStackTrace();
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
        testProduct.setProductId("1"); // need to link metadata to prod

        try {
            myCat.addMetadata(met, testProduct);
        } catch (Exception e) {
            e.printStackTrace();
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

        Product testProduct = getTestProduct();
        testProduct.setProductId("1"); // need to link metadata to prod

        try {
            myCat.addMetadata(met, testProduct);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        try {
            myCat.removeMetadata(met, testProduct);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        try {
            Metadata retMet = myCat.getMetadata(testProduct);
            String retValue = retMet.getMetadata("Filename");
            assertNull(retValue);
        } catch (CatalogException e) {
            fail(e.getMessage());
        }
    }

    public void testQuery() throws Exception {
        // ingest first file
        Product testProduct = getTestProduct();
        testProduct.setProductId("23");
        Metadata prodMet = new Metadata();
        prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(
                "./src/testdata/ingest").getCanonicalPath());
        prodMet.addMetadata(CoreMetKeys.FILENAME, "test-file-1.txt");
        prodMet.addMetadata("CAS.ProductName", "TestFile1");
        prodMet.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
        prodMet.addMetadata("NominalDate", "2008-01-20");
        prodMet.addMetadata("DataVersion", "3.6");
        myCat.addMetadata(prodMet, testProduct);

        // ingest second file
        testProduct.setProductId("24");
        prodMet.replaceMetadata(CoreMetKeys.FILENAME, "test-file-2.txt");
        prodMet.replaceMetadata("CAS.ProductName", "TestFile2");
        myCat.addMetadata(prodMet, testProduct);

        // ingest thrid file
        testProduct.setProductId("25");
        prodMet.replaceMetadata(CoreMetKeys.FILENAME, "test-file-2.txt");
        prodMet.replaceMetadata("CAS.ProductName", "TestFile3");
        prodMet.replaceMetadata("DataVersion", "4.6");
        myCat.addMetadata(prodMet, testProduct);

        // perform first query
        Query query = new Query();
        query
                .addCriterion(SqlParser
                        .parseSqlWhereClause("CAS.ProductName != 'TestFile3' AND (Filename == 'test-file-1.txt' OR Filename == 'test-file-2.txt') AND CAS.ProductName != 'TestFile2'"));
        System.out.println(query);
        List<String> productIds = myCat.query(query, testProduct
                .getProductType());
        System.out.println(productIds);
        assertEquals("[23]", productIds.toString());

        // perform second query
        query = new Query();
        query
                .addCriterion(SqlParser
                        .parseSqlWhereClause("Filename == 'test-file-1.txt' OR (Filename == 'test-file-2.txt' AND CAS.ProductName != 'TestFile2')"));
        System.out.println(query);
        productIds = myCat.query(query, testProduct.getProductType());
        System.out.println(productIds);
        assertEquals("[25, 23]", productIds.toString());

        // perform second query
        query = new Query();
        query
                .addCriterion(SqlParser
                        .parseSqlWhereClause("NominalDate == '2008-01-20' AND DataVersion >= '3.6' AND DataVersion <= '4.0'"));
        System.out.println(query);
        productIds = myCat.query(query, testProduct.getProductType());
        System.out.println(productIds);
        assertEquals("[24, 23]", productIds.toString());
    }

    protected String getSchemaPath() {
        return "./src/testdata/testcat.sql";
    }
    
    protected void setCatalog(Catalog cat){
        this.myCat = cat;
    }

    private Catalog getCatalog() {
        return new DataSourceCatalogFactory().createCatalog();
    }
    
    private void createSchema() {
        String url = System
                .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.url");
        String user = System
                .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.user");
        String pass = System
                .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.pass");
        String driver = System
                .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.driver");

        DataSource ds = DatabaseConnectionBuilder.buildDataSource(user, pass,
                driver, url);
        try {
            SqlScript coreSchemaScript = new SqlScript(
                    new File(getSchemaPath()).getAbsolutePath(), ds);
            coreSchemaScript.loadScript();
            coreSchemaScript.execute();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    private static Product getTestProduct() {
        Product testProduct = Product.getDefaultFlatProduct("test",
                "urn:oodt:GenericFile");
        testProduct.getProductType().setName("GenericFile");
        return testProduct;
    }

    private static Metadata getTestMetadata(String prodName) {
        Metadata met = new Metadata();
        met.addMetadata("CAS.ProductName", prodName);
        return met;
    }

}
