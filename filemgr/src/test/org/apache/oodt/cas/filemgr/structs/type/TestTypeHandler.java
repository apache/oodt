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

package org.apache.oodt.cas.filemgr.structs.type;

import org.apache.oodt.commons.database.DatabaseConnectionBuilder;
import org.apache.oodt.commons.database.SqlScript;
import org.apache.oodt.commons.pagination.PaginationUtils;
import org.apache.oodt.cas.filemgr.catalog.Catalog;
import org.apache.oodt.cas.filemgr.catalog.DataSourceCatalog;
import org.apache.oodt.cas.filemgr.catalog.DataSourceCatalogFactory;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManager;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.metadata.Metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.xmlrpc.XmlRpcException;

import junit.framework.TestCase;

public class TestTypeHandler extends TestCase {
    
    String tmpDirPath;
    
    DataSource publicDataSource;
    
    XmlRpcFileManager fmServer;
    
    int FILEMGR_PORT = 9999;
    
    public TestTypeHandler() {
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

        // now override the val layer ones
        System.setProperty("org.apache.oodt.cas.filemgr.validation.dirs",
                "file://"
                        + new File("./src/testdata/xmlrpc-struct-factory")
                                .getAbsolutePath());
        
        System.setProperty("org.apache.oodt.cas.filemgr.repositorymgr.dirs",
                "file://"
                        + new File("./src/testdata/xmlrpc-struct-factory")
                                .getAbsolutePath());

        // override quote fields
        System.setProperty(
                "org.apache.oodt.cas.filemgr.catalog.datasource.quoteFields",
                "true");

    }
   
    public void testAddAndGetMetadata() throws SQLException, MalformedURLException, ConnectionException {
        Metadata met = new Metadata();
        met.addMetadata("DataVersion", "4.0");
        met.addMetadata("ProductName", "test");
        Product testProduct = getTestProduct();

        XmlRpcFileManagerClient fmClient = new XmlRpcFileManagerClient(new URL("http://localhost:" + FILEMGR_PORT));
        try {
            testProduct.setProductType(fmClient.getProductTypeByName("GenericFile"));
            testProduct.setProductId(fmClient.ingestProduct(testProduct, met, false));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        try {
            Metadata retMet = fmClient.getMetadata(testProduct);
            assertNotNull(retMet);
            assertTrue(retMet.containsKey("DataVersion"));
            assertEquals("4.0", retMet
                    .getMetadata("DataVersion"));
        } catch (CatalogException e) {
            fail(e.getMessage());
        }
        
        Statement statement = publicDataSource.getConnection().createStatement();
        ResultSet rs = statement.executeQuery("SELECT metadata_value FROM GenericFile_metadata WHERE element_id = 'urn:test:DataVersion';");
        rs.next();
        assertEquals(rs.getString("metadata_value"), "04.00");
    }
    
    public void testQuery() throws MalformedURLException, ConnectionException, CatalogException {
        Metadata met = new Metadata();
        met.addMetadata("DataVersion", "4.0");
        met.addMetadata("ProductName", "test");
        Product testProduct = getTestProduct();

        ProductType genericFile = null;
        XmlRpcFileManagerClient fmClient = new XmlRpcFileManagerClient(new URL("http://localhost:" + FILEMGR_PORT));
        try {
            testProduct.setProductType(genericFile = fmClient.getProductTypeByName("GenericFile"));
            testProduct.setProductId(fmClient.ingestProduct(testProduct, met, false));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
        Query query = new Query();
        TermQueryCriteria termQuery = new TermQueryCriteria();
        termQuery.setElementName("DataVersion");
        termQuery.setValue("4.0");
        query.addCriterion(termQuery);
        
        List<Product> products = fmClient.query(query, genericFile);
        assertEquals(products.get(0).getProductId(), testProduct.getProductId());
    }
    
    public void testGetCatalogAndOrigValuesAndGetCatalogQuery() throws ConnectionException, RepositoryManagerException, XmlRpcException, IOException {
        Metadata met = new Metadata();
        met.addMetadata("DataVersion", "4.0");
        met.addMetadata("ProductName", "test");
        Product testProduct = getTestProduct();

        XmlRpcFileManagerClient fmClient = new XmlRpcFileManagerClient(new URL("http://localhost:" + FILEMGR_PORT));
        ProductType genericFile = fmClient.getProductTypeByName("GenericFile");
        assertEquals("04.00", (met = fmClient.getCatalogValues(met, genericFile)).getMetadata("DataVersion"));
        assertEquals("4.0", fmClient.getOrigValues(met, genericFile).getMetadata("DataVersion"));
        
        Query query = new Query();
        TermQueryCriteria termQuery = new TermQueryCriteria();
        termQuery.setElementName("DataVersion");
        termQuery.setValue("4.0");
        query.addCriterion(termQuery);
        query = fmClient.getCatalogQuery(query, genericFile);
        assertEquals("04.00", ((TermQueryCriteria) query.getCriteria().get(0)).getValue());
    }
    
    private static Product getTestProduct() throws MalformedURLException {
        Product testProduct = Product.getDefaultFlatProduct("test",
                "urn:oodt:GenericFile");
        List<Reference> refs = new LinkedList<Reference>();
        Reference ref = new Reference();
        ref.setOrigReference(new File("./src/testdata/ingest/test.txt").toURL().toExternalForm());
        ref.setFileSize(123);
        refs.add(ref);
        testProduct.setProductReferences(refs);
        return testProduct;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        createSchema();
        startXmlRpcFileManager();
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        fmServer.shutdown();
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
    
    private void createSchema() {
        String url = System
                .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.url");
        String user = System
                .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.user");
        String pass = System
                .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.pass");
        String driver = System
                .getProperty("org.apache.oodt.cas.filemgr.catalog.datasource.jdbc.driver");

        publicDataSource = DatabaseConnectionBuilder.buildDataSource(user, pass,
                driver, url);
        try {
            SqlScript coreSchemaScript = new SqlScript(new File(
                    "./src/testdata/testcat.sql").getAbsolutePath(), publicDataSource);
            coreSchemaScript.loadScript();
            coreSchemaScript.execute();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }
    
    private void startXmlRpcFileManager() {
        try {
            fmServer = new XmlRpcFileManager(FILEMGR_PORT);
            fmServer.setCatalog(new HsqlDbFriendlyDataSourceCatalogFatory().createCatalog());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    class HsqlDbFriendlyDataSourceCatalogFatory extends
            DataSourceCatalogFactory {

        public HsqlDbFriendlyDataSourceCatalogFatory() {
            super();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.apache.oodt.cas.filemgr.catalog.CatalogFactory#createCatalog()
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
         * @see org.apache.oodt.cas.filemgr.catalog.DataSourceCatalog#pagedQuery(org.apache.oodt.cas.filemgr.structs.Query,
         *      org.apache.oodt.cas.filemgr.structs.ProductType, int)
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
         * @see org.apache.oodt.cas.filemgr.catalog.DataSourceCatalog#getFirstPage(org.apache.oodt.cas.filemgr.structs.ProductType)
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
         * @see org.apache.oodt.cas.filemgr.catalog.DataSourceCatalog#getNextPage(org.apache.oodt.cas.filemgr.structs.ProductType,
         *      org.apache.oodt.cas.filemgr.structs.ProductPage)
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
                            elementIdStr = "'" + this.getValidationLayer().getElementByName(criteria.getElementName()).getElementId() + "'";
                        } else {
                            elementIdStr = this.getValidationLayer().getElementByName(criteria.getElementName()).getElementId();
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
                        if(pageNum != 1){
                            rs.relative(startNum-1);
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

        protected int getResultListSize(Query query, ProductType type)
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
                            elementIdStr = "'" + this.getValidationLayer().getElementByName(criteria.getElementName()).getElementId() + "'";
                        } else {
                            elementIdStr = this.getValidationLayer().getElementByName(criteria.getElementName()).getElementId();
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
