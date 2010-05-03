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


package gov.nasa.jpl.oodt.cas.filemgr.catalog;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.metadata.CoreMetKeys;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Product;
import gov.nasa.jpl.oodt.cas.filemgr.structs.ProductPage;
import gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Query;
import gov.nasa.jpl.oodt.cas.filemgr.structs.QueryCriteria;
import gov.nasa.jpl.oodt.cas.filemgr.structs.RangeQueryCriteria;
import gov.nasa.jpl.oodt.cas.filemgr.structs.TermQueryCriteria;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.CatalogException;
import gov.nasa.jpl.oodt.cas.commons.database.DatabaseConnectionBuilder;
import gov.nasa.jpl.oodt.cas.commons.pagination.PaginationUtils;
import gov.nasa.jpl.oodt.cas.commons.database.SqlScript;
import gov.nasa.jpl.oodt.cas.filemgr.util.SqlParser;
import gov.nasa.jpl.oodt.cas.filemgr.validation.ValidationLayer;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

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

    private Catalog myCat;

    private String tmpDirPath = null;

    private static final int catPageSize = 20;

    public TestDataSourceCatalog() {
        // set the log levels
        System.setProperty("java.util.logging.config.file", new File(
                "./src/main/resources/logging.properties").getAbsolutePath());

        // first load the example configuration
        try {
            System.getProperties().load(
                    new FileInputStream(
                            "./src/main/resources/filemgr.properties"));
        } catch (Exception e) {
            fail(e.getMessage());
        }

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
                "gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.jdbc.url",
                "jdbc:hsqldb:file:" + tmpDirPath + "/testCat;shutdown=true");

        System.setProperty(
                "gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.jdbc.user",
                "sa");
        System.setProperty(
                "gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.jdbc.pass",
                "");
        System.setProperty(
                "gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.jdbc.driver",
                "org.hsqldb.jdbcDriver");

        // now override the val layer ones
        System.setProperty("gov.nasa.jpl.oodt.cas.filemgr.validation.dirs",
                "file://"
                        + new File("./src/testdata/xmlrpc-struct-factory")
                                .getAbsolutePath());

        // override quote fields
        System.setProperty(
                "gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.quoteFields",
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
        // make sure it's the first one on the 2nd page

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
        ProductPage page = myCat.getNextPage(type, myCat.getFirstPage(type));
        assertNotNull(page);
        assertNotNull(page.getPageProducts());
        assertEquals(1, page.getPageProducts().size());
        assertEquals(2, page.getTotalPages());
        assertNotNull(page.getPageProducts().get(0));
        Product retProd = ((Product) page.getPageProducts().get(0));
        assertEquals("ShouldBeFirstForPage.txt", retProd.getProductName());

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
        return (HsqlDbFriendlyDataSourceCatalog) new HsqlDbFriendlyDataSourceCatalogFatory()
                .createCatalog();
    }
    
    private void createSchema() {
        String url = System
                .getProperty("gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.jdbc.url");
        String user = System
                .getProperty("gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.jdbc.user");
        String pass = System
                .getProperty("gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.jdbc.pass");
        String driver = System
                .getProperty("gov.nasa.jpl.oodt.cas.filemgr.catalog.datasource.jdbc.driver");

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

    class HsqlDbFriendlyDataSourceCatalogFatory extends
            DataSourceCatalogFactory {

        public HsqlDbFriendlyDataSourceCatalogFatory() {
            super();
        }

        /*
         * (non-Javadoc)
         * 
         * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.CatalogFactory#createCatalog()
         */
        public Catalog createCatalog() {
            return new HsqlDbFriendlyDataSourceCatalog(dataSource,
                    validationLayer, fieldIdStr, pageSize, cacheUpdateMinutes);
        }
    }

    class HsqlDbFriendlyDataSourceCatalog extends DataSourceCatalog {

        public HsqlDbFriendlyDataSourceCatalog(DataSource ds,
                ValidationLayer valLayer, boolean fieldId, int pageSize,
                long cacheUpdateMin) {
            super(ds, valLayer, fieldId, pageSize, cacheUpdateMin);
        }

        private final Logger LOG = Logger
                .getLogger(HsqlDbFriendlyDataSourceCatalog.class.getName());

        /*
         * (non-Javadoc)
         * 
         * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.DataSourceCatalog#pagedQuery(gov.nasa.jpl.oodt.cas.filemgr.structs.Query,
         *      gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType, int)
         */
        public ProductPage pagedQuery(Query query, ProductType type, int pageNum)
                throws CatalogException {
            int totalPages = PaginationUtils.getTotalPage(getResultListSize(
                    query, type), this.pageSize);

            /*
             * if there are 0 total pages in the result list size then don't
             * bother returning a valid product page instead, return blank
             * ProductPage
             */
            if (totalPages == 0) {
                return ProductPage.blankPage();
            }

            ProductPage retPage = new ProductPage();
            retPage.setPageNum(pageNum);
            retPage.setPageSize(this.pageSize);
            retPage.setTotalPages(totalPages);

            List productIds = paginateQuery(query, type, pageNum);

            if (productIds != null && productIds.size() > 0) {
                List products = new Vector(productIds.size());

                for (Iterator i = productIds.iterator(); i.hasNext();) {
                    String productId = (String) i.next();
                    Product p = getProductById(productId);
                    products.add(p);
                }

                retPage.setPageProducts(products);
            }

            return retPage;
        }

        /*
         * (non-Javadoc)
         * 
         * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.DataSourceCatalog#getFirstPage(gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType)
         */
        public ProductPage getFirstPage(ProductType type) {
            Query query = new Query();
            ProductPage firstPage = null;

            try {
                firstPage = pagedQuery(query, type, 1);
            } catch (CatalogException e) {
                LOG.log(Level.WARNING,
                        "Exception getting first page: Message: "
                                + e.getMessage());
            }
            return firstPage;
        }

        /*
         * (non-Javadoc)
         * 
         * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.DataSourceCatalog#getNextPage(gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType,
         *      gov.nasa.jpl.oodt.cas.filemgr.structs.ProductPage)
         */
        public ProductPage getNextPage(ProductType type, ProductPage currentPage) {
            if (currentPage == null) {
                return getFirstPage(type);
            }

            if (currentPage.isLastPage()) {
                return currentPage;
            }

            ProductPage nextPage = null;
            Query query = new Query();

            try {
                nextPage = pagedQuery(query, type, currentPage.getPageNum() + 1);
            } catch (CatalogException e) {
                LOG.log(Level.WARNING, "Exception getting next page: Message: "
                        + e.getMessage());
            }

            return nextPage;
        }

        private List paginateQuery(Query query, ProductType type, int pageNum)
                throws CatalogException {
            Connection conn = null;
            Statement statement = null;
            ResultSet rs = null;

            List productIds = null;
            boolean doSkip = true;
            int numResults = -1;

            if (pageNum == -1) {
                doSkip = false;
            } else {
                numResults = getResultListSize(query, type);
            }

            try {
                conn = dataSource.getConnection();
                statement = conn.createStatement(
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);

                String getProductSql = "";
                String tableName = type.getName() + "_metadata";
                String subSelectQueryBase = "SELECT product_id FROM "
                        + tableName + " ";
                StringBuffer selectClause = new StringBuffer(
                        "SELECT DISTINCT p.product_id ");
                StringBuffer fromClause = new StringBuffer("FROM " + tableName
                        + " p ");
                StringBuffer whereClause = new StringBuffer("WHERE ");

                boolean gotFirstClause = false;
                int clauseNum = 0;

                if (query.getCriteria() != null
                        && query.getCriteria().size() > 0) {
                    for (Iterator i = query.getCriteria().iterator(); i
                            .hasNext();) {
                        QueryCriteria criteria = (QueryCriteria) i.next();
                        clauseNum++;

                        String elementIdStr = null;

                        if (fieldIdStringFlag) {
                            elementIdStr = "'"
                                    + this.getValidationLayer()
                                            .getElementByName(
                                                    criteria.getElementName())
                                            .getElementId() + "'";
                        } else {
                            elementIdStr = this
                                    .getValidationLayer()
                                    .getElementByName(criteria.getElementName())
                                    .getElementId();
                        }

                        String clause = null;

                        if (!gotFirstClause) {
                            clause = "(p.element_id = " + elementIdStr
                                    + " AND ";
                            if (criteria instanceof TermQueryCriteria) {
                                clause += " metadata_value LIKE '%"
                                        + ((TermQueryCriteria) criteria)
                                                .getValue() + "%') ";
                            } else if (criteria instanceof RangeQueryCriteria) {
                                String startVal = ((RangeQueryCriteria) criteria)
                                        .getStartValue();
                                String endVal = ((RangeQueryCriteria) criteria)
                                        .getEndValue();
                                boolean inclusive = ((RangeQueryCriteria) criteria)
                                        .getInclusive();

                                if ((startVal != null && !startVal.equals(""))
                                        || (endVal != null && !endVal
                                                .equals(""))) {
                                    clause += " metadata_value ";

                                    boolean gotStart = false;

                                    if (startVal != null
                                            && !startVal.equals("")) {
                                        if (inclusive)
                                            clause += ">= '" + startVal + "'";
                                        else
                                            clause += "> '" + startVal + "'";
                                        gotStart = true;
                                    }

                                    if (endVal != null && !endVal.equals("")) {
                                        if (gotStart) {
                                            if (inclusive)
                                                clause += " AND metadata_value <= '"
                                                        + endVal + "'";
                                            else
                                                clause += " AND metadata_value < '"
                                                        + endVal + "'";
                                        } else if (inclusive)
                                            clause += "<= '" + endVal + "'";
                                        else
                                            clause += "< '" + endVal + "'";
                                    }

                                    clause += ") ";
                                }
                            }

                            whereClause.append(clause);
                            gotFirstClause = true;
                        } else {
                            String subSelectTblName = "p" + clauseNum;
                            String subSelectQuery = subSelectQueryBase
                                    + "WHERE (element_id = " + elementIdStr
                                    + " AND ";
                            if (criteria instanceof TermQueryCriteria) {
                                subSelectQuery += " metadata_value LIKE '%"
                                        + ((TermQueryCriteria) criteria)
                                                .getValue() + "%')";
                            } else if (criteria instanceof RangeQueryCriteria) {
                                String startVal = ((RangeQueryCriteria) criteria)
                                        .getStartValue();
                                String endVal = ((RangeQueryCriteria) criteria)
                                        .getEndValue();

                                if (startVal != null || endVal != null) {
                                    subSelectQuery += " metadata_value ";

                                    boolean gotStart = false;

                                    if (startVal != null
                                            && !startVal.equals("")) {
                                        subSelectQuery += ">= '" + startVal
                                                + "'";
                                        gotStart = true;
                                    }

                                    if (endVal != null && !endVal.equals("")) {
                                        if (gotStart) {
                                            subSelectQuery += " AND metadata_value <= '"
                                                    + endVal + "'";
                                        } else
                                            subSelectQuery += "<= '" + endVal
                                                    + "'";
                                    }

                                    subSelectQuery += ") ";

                                }
                            }
                            fromClause.append("INNER JOIN (" + subSelectQuery
                                    + ") " + subSelectTblName + " ON "
                                    + subSelectTblName
                                    + ".product_id = p.product_id ");

                        }
                    }
                }
                getProductSql = selectClause.toString() + fromClause.toString();
                if (gotFirstClause) {
                    getProductSql += whereClause.toString();
                }

                LOG.log(Level.FINE, "catalog query: executing: "
                        + getProductSql);

                rs = statement.executeQuery(getProductSql);
                productIds = new Vector();

                if (doSkip) {
                    int startNum = (pageNum - 1) * pageSize;

                    if (startNum > numResults) {
                        startNum = 0;
                    }

                    // must call next first, or else no relative cursor
                    if (rs.next()) {
                        // grab the first one
                        int numGrabbed = -1;

                        if (pageNum == 1) {
                            numGrabbed = 1;
                            productIds.add(rs.getString("product_id"));
                        } else {
                            numGrabbed = 0;
                        }

                        // now move the cursor to the correct position
                        if (pageNum != 1) {
                            rs.relative(startNum - 1);
                        }

                        // grab the rest
                        while (rs.next() && numGrabbed < pageSize) {
                            String productId = rs.getString("product_id");
                            productIds.add(productId);
                            numGrabbed++;
                        }
                    }

                } else {
                    while (rs.next()) {
                        String productId = rs.getString("product_id");
                        productIds.add(productId);
                    }
                }

                if (productIds.size() == 0) {
                    productIds = null;
                }

            } catch (Exception e) {
                e.printStackTrace();
                LOG.log(Level.WARNING, "Exception performing query. Message: "
                        + e.getMessage());
                try {
                    conn.rollback();
                } catch (SQLException e2) {
                    LOG.log(Level.SEVERE,
                            "Unable to rollback query transaction. Message: "
                                    + e2.getMessage());
                }
                throw new CatalogException(e.getMessage());
            } finally {

                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ignore) {
                    }

                    rs = null;
                }

                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException ignore) {
                    }

                    statement = null;
                }

                if (conn != null) {
                    try {
                        conn.close();

                    } catch (SQLException ignore) {
                    }

                    conn = null;
                }
            }

            return productIds;
        }

        private int getResultListSize(Query query, ProductType type)
                throws CatalogException {
            Connection conn = null;
            Statement statement = null;
            ResultSet rs = null;

            int resultCount = 0;

            try {
                conn = dataSource.getConnection();
                statement = conn.createStatement();

                String getProductSql = "";
                String tableName = type.getName() + "_metadata";
                String subSelectQueryBase = "SELECT product_id FROM "
                        + tableName + " ";
                StringBuffer selectClause = new StringBuffer(
                        "SELECT COUNT(DISTINCT p.product_id) AS numResults ");
                StringBuffer fromClause = new StringBuffer("FROM " + tableName
                        + " p ");
                StringBuffer whereClause = new StringBuffer("WHERE ");

                boolean gotFirstClause = false;
                int clauseNum = 0;

                if (query.getCriteria() != null
                        && query.getCriteria().size() > 0) {
                    for (Iterator i = query.getCriteria().iterator(); i
                            .hasNext();) {
                        QueryCriteria criteria = (QueryCriteria) i.next();
                        clauseNum++;

                        String elementIdStr = null;

                        if (fieldIdStringFlag) {
                            elementIdStr = "'"
                                    + this.getValidationLayer()
                                            .getElementByName(
                                                    criteria.getElementName())
                                            .getElementId() + "'";
                        } else {
                            elementIdStr = this
                                    .getValidationLayer()
                                    .getElementByName(criteria.getElementName())
                                    .getElementId();
                        }

                        String clause = null;

                        if (!gotFirstClause) {
                            clause = "(p.element_id = " + elementIdStr
                                    + " AND ";
                            if (criteria instanceof TermQueryCriteria) {
                                clause += " metadata_value LIKE '%"
                                        + ((TermQueryCriteria) criteria)
                                                .getValue() + "%') ";
                            } else if (criteria instanceof RangeQueryCriteria) {
                                String startVal = ((RangeQueryCriteria) criteria)
                                        .getStartValue();
                                String endVal = ((RangeQueryCriteria) criteria)
                                        .getEndValue();
                                boolean inclusive = ((RangeQueryCriteria) criteria)
                                        .getInclusive();

                                if ((startVal != null && !startVal.equals(""))
                                        || (endVal != null && !endVal
                                                .equals(""))) {
                                    clause += " metadata_value ";

                                    boolean gotStart = false;

                                    if (startVal != null
                                            && !startVal.equals("")) {
                                        if (inclusive)
                                            clause += ">= '" + startVal + "'";
                                        else
                                            clause += "> '" + startVal + "'";
                                        gotStart = true;
                                    }

                                    if (endVal != null && !endVal.equals("")) {
                                        if (gotStart) {
                                            if (inclusive)
                                                clause += " AND metadata_value <= '"
                                                        + endVal + "'";
                                            else
                                                clause += " AND metadata_value < '"
                                                        + endVal + "'";
                                        } else if (inclusive)
                                            clause += "<= '" + endVal + "'";
                                        else
                                            clause += "< '" + endVal + "'";
                                    }

                                    clause += ") ";
                                }
                            }
                            whereClause.append(clause);
                            gotFirstClause = true;
                        } else {
                            String subSelectTblName = "p" + clauseNum;
                            String subSelectQuery = subSelectQueryBase
                                    + "WHERE (element_id = " + elementIdStr
                                    + " AND ";
                            if (criteria instanceof TermQueryCriteria) {
                                subSelectQuery += " metadata_value LIKE '%"
                                        + ((TermQueryCriteria) criteria)
                                                .getValue() + "%')";
                            } else if (criteria instanceof RangeQueryCriteria) {
                                String startVal = ((RangeQueryCriteria) criteria)
                                        .getStartValue();
                                String endVal = ((RangeQueryCriteria) criteria)
                                        .getEndValue();

                                if (startVal != null || endVal != null) {
                                    subSelectQuery += " metadata_value ";

                                    boolean gotStart = false;

                                    if (startVal != null
                                            && !startVal.equals("")) {
                                        subSelectQuery += ">= '" + startVal
                                                + "'";
                                        gotStart = true;
                                    }

                                    if (endVal != null && !endVal.equals("")) {
                                        if (gotStart) {
                                            subSelectQuery += " AND metadata_value <= '"
                                                    + endVal + "'";
                                        } else
                                            subSelectQuery += "<= '" + endVal
                                                    + "'";
                                    }

                                    subSelectQuery += ") ";

                                }
                            }

                            fromClause.append("INNER JOIN (" + subSelectQuery
                                    + ") " + subSelectTblName + " ON "
                                    + subSelectTblName
                                    + ".product_id = p.product_id ");

                        }
                    }
                }

                getProductSql = selectClause.toString() + fromClause.toString();
                if (gotFirstClause) {
                    getProductSql += whereClause.toString();
                }

                LOG.log(Level.FINE, "catalog get num results: executing: "
                        + getProductSql);

                rs = statement.executeQuery(getProductSql);

                while (rs.next()) {
                    resultCount = rs.getInt("numResults");
                }

            } catch (Exception e) {
                e.printStackTrace();
                LOG.log(Level.WARNING,
                        "Exception performing get num results. Message: "
                                + e.getMessage());
                try {
                    conn.rollback();
                } catch (SQLException e2) {
                    LOG.log(Level.SEVERE,
                            "Unable to rollback get num results transaction. Message: "
                                    + e2.getMessage());
                }
                throw new CatalogException(e.getMessage());
            } finally {

                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ignore) {
                    }

                    rs = null;
                }

                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException ignore) {
                    }

                    statement = null;
                }

                if (conn != null) {
                    try {
                        conn.close();

                    } catch (SQLException ignore) {
                    }

                    conn = null;
                }
            }

            return resultCount;
        }
    }

}
