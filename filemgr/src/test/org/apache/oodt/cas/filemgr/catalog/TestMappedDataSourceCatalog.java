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

//OODT imports
import gov.nasa.jpl.oodt.cas.commons.pagination.PaginationUtils;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Product;
import gov.nasa.jpl.oodt.cas.filemgr.structs.ProductPage;
import gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Query;
import gov.nasa.jpl.oodt.cas.filemgr.structs.QueryCriteria;
import gov.nasa.jpl.oodt.cas.filemgr.structs.RangeQueryCriteria;
import gov.nasa.jpl.oodt.cas.filemgr.structs.TermQueryCriteria;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.CatalogException;
import gov.nasa.jpl.oodt.cas.filemgr.validation.ValidationLayer;

//JDK imports
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class TestMappedDataSourceCatalog extends TestDataSourceCatalog {

    /**
     * 
     */
    public TestMappedDataSourceCatalog() {
        super();
        System
                .getProperties()
                .setProperty(
                        "gov.nasa.jpl.oodt.cas.filemgr.catalog.mappeddatasource.mapFile",
                        "./src/testdata/testcatalog.typemap.properties");
        setCatalog(getCatalog());

    }

    protected Catalog getCatalog() {
        try {
            return new HsqlFriendlyMappedDataSourceCatalogFactory().createCatalog();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.TestDataSourceCatalog#getSchemaPath()
     */
    @Override
    protected String getSchemaPath() {
        return "./src/testdata/testcat.mapped.sql";
    }

    class HsqlFriendlyMappedDataSourceCatalogFactory extends
            MappedDataSourceCatalogFactory {

        /**
         * @throws FileNotFoundException
         * @throws IOException
         */
        public HsqlFriendlyMappedDataSourceCatalogFactory()
                throws FileNotFoundException, IOException {
            super();
        }

        /*
         * (non-Javadoc)
         * 
         * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.MappedDataSourceCatalogFactory#createCatalog()
         */
        @Override
        public Catalog createCatalog() {
            return new HsqlFriendlyMappedDataSourceCatalog(this.dataSource,
                    this.validationLayer, this.fieldIdStr, this.pageSize,
                    this.cacheUpdateMinutes, this.typeMap);
        }

    }

    class HsqlFriendlyMappedDataSourceCatalog extends MappedDataSourceCatalog {

        private final Logger LOG = Logger
                .getLogger(HsqlFriendlyMappedDataSourceCatalog.class.getName());

        public HsqlFriendlyMappedDataSourceCatalog(DataSource ds,
                ValidationLayer valLayer, boolean fieldid, int pageSize,
                long cacheUpdateMin, Properties typeMap) {
            super(ds, valLayer, fieldid, pageSize, cacheUpdateMin, typeMap);
        }

        /*
         * (non-Javadoc)
         * 
         * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.DataSourceCatalog#pagedQuery(gov.nasa.jpl.oodt.cas.filemgr.structs.Query,
         *      gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType, int)
         */
        public ProductPage pagedQuery(Query query, ProductType type, int pageNum)
                throws CatalogException {
            String origProductTypeName = type.getName();
            type.setName(getProductTypeTableName(origProductTypeName));
            ProductPage page = doPagedQuery(query, type, pageNum);
            type.setName(origProductTypeName);
            return page;
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

        private ProductPage doPagedQuery(Query query, ProductType type,
                int pageNum) throws CatalogException {
            int totalPages = PaginationUtils.getTotalPage(safeResultListSize(
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

            List productIds = doPaginateQuery(query, type, pageNum);

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

        private List doPaginateQuery(Query query, ProductType type, int pageNum)
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
                numResults = safeResultListSize(query, type);
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

        private int safeResultListSize(Query query, ProductType type)
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
