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

package org.apache.oodt.cas.filemgr.validation;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.util.DbStructFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

//JDK imports

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * An implementation of a ValidationLayer that queries a {@link DataSource}
 * backed database for validation information.
 * </p>
 * 
 */
public class DataSourceValidationLayer implements ValidationLayer {

    /* our log stream */
    private static final Logger LOG = Logger
            .getLogger(DataSourceValidationLayer.class.getName());

    /* our data source */
    private DataSource dataSource = null;

    /* should we quote product_type_id? */
    private boolean quoteFields = false;

    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public DataSourceValidationLayer(DataSource ds, boolean fieldQuote) {
        dataSource = ds;
        quoteFields = fieldQuote;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#addElement(org.apache.oodt.cas.filemgr.structs.Element)
     */
    public void addElement(Element element) throws ValidationLayerException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String addMetaElemSql = "INSERT INTO elements (element_name, dc_element, element_description) VALUES ('"
                    + element.getElementName()
                    + ", '"
                    + element.getDCElement()
                    + "', '" + element.getDescription() + "')";

            LOG.log(Level.FINE, "addMetadataElement: Executing: "
                    + addMetaElemSql);
            statement.execute(addMetaElemSql);

            String elementId = "";

            String getMetaIdSql = "SELECT MAX(element_id) AS max_id FROM elements";
            LOG.log(Level.FINE, "addElement: Executing: " + getMetaIdSql);
            rs = statement.executeQuery(getMetaIdSql);

            while (rs.next()) {
                elementId = String.valueOf(rs.getInt("max_id"));
            }

            element.setElementId(elementId);
            conn.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG
                    .log(Level.WARNING, "Exception adding element "
                            + element.getElementName() + ". Message: "
                            + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback addElement transaction. Message: "
                                + e2.getMessage());
            }
            throw new ValidationLayerException(e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#modifyElement(org.apache.oodt.cas.filemgr.structs.Element)
     */
    public void modifyElement(Element element) throws ValidationLayerException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String elementSql = "UPDATE elements SET element_name = '"
                    + element.getElementName() + "', dc_element='"
                    + element.getDCElement() + "', " + "element_description='"
                    + element.getDescription() + "' WHERE " + "element_id = "
                    + element.getElementId();

            LOG.log(Level.FINE, "modifyElement: Executing: " + elementSql);
            statement.execute(elementSql);
            conn.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception modifying element. Message: "
                    + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback modifyElement transaction. Message: "
                                + e2.getMessage());
            }
            throw new ValidationLayerException(e);
        } finally {

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#removeElement(org.apache.oodt.cas.filemgr.structs.Element)
     */
    public void removeElement(Element element) throws ValidationLayerException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String deleteElementSql = "DELETE FROM elements WHERE element_id = "
                    + element.getElementId();

            LOG
                    .log(Level.FINE, "removeElement: Executing: "
                            + deleteElementSql);
            statement.execute(deleteElementSql);
            conn.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception removing element. Message: "
                    + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback removeElement transaction. Message: "
                                + e2.getMessage());
            }
            throw new ValidationLayerException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#addElementToProductType(org.apache.oodt.cas.filemgr.structs.ProductType,
     *      org.apache.oodt.cas.filemgr.structs.Element)
     */
    public void addElementToProductType(ProductType type, Element element)
            throws ValidationLayerException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String addMetaElemSql = "INSERT INTO product_type_element_map (product_type_id, element_id) VALUES(";

            if (quoteFields) {
                addMetaElemSql += "'" + type.getProductTypeId() + "',";
            } else {
                addMetaElemSql += type.getProductTypeId() + ",";
            }
            addMetaElemSql += " " + element.getElementId() + ")";

            LOG.log(Level.FINE, "addElementToProductType: Executing: "
                    + addMetaElemSql);
            statement.execute(addMetaElemSql);
            conn.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception adding element "
                    + element.getElementName() + " to product type "
                    + type.getName() + " . Message: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback addElementToProductType transaction. Message: "
                                + e2.getMessage());
            }
            throw new ValidationLayerException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#removeElementFromProductType(org.apache.oodt.cas.filemgr.structs.ProductType,
     *      org.apache.oodt.cas.filemgr.structs.Element)
     */
    public void removeElementFromProductType(ProductType type, Element element)
            throws ValidationLayerException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String deleteElemSql = "DELETE FROM product_type_element_map WHERE product_type_id = ";

            if (quoteFields) {
                deleteElemSql += "'" + type.getProductTypeId() + "'";
            } else {
                deleteElemSql += type.getProductTypeId();
            }

            deleteElemSql += " AND element_id = " + element.getElementId();

            LOG.log(Level.FINE, "removeElementFromProductType: Executing: "
                    + deleteElemSql);
            statement.execute(deleteElemSql);
            conn.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception removing element "
                    + element.getElementName() + " from product type "
                    + type.getName() + ". Message: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback removeElementFromProductType transaction. Message: "
                                + e2.getMessage());
            }
            throw new ValidationLayerException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

    }

    public void addParentToProductType(ProductType type, String parent)
            throws ValidationLayerException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String addParentInfoSql = "INSERT INTO sub_to_super_map (product_type_id, parent_id) VALUES(";
            if (quoteFields) {
                addParentInfoSql += "'" + type.getProductTypeId() + "','"
                        + parent + "')";
            } else {
                addParentInfoSql += type.getProductTypeId() + "," + parent
                        + ")";
            }

            LOG.log(Level.FINE, "addParentToProductType: Executing: "
                    + addParentInfoSql);
            statement.execute(addParentInfoSql);
            conn.commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception adding parent info to product type "
                            + type.getName() + " . Message: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback addParentToProductType transaction. Message: "
                                + e2.getMessage());
            }
            throw new ValidationLayerException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignore) {
                }

            }
        }
    }

    public void removeParentFromProductType(ProductType type, String parent)
            throws ValidationLayerException {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();

            String deleteParSql = "DELETE FROM sub_to_super_map WHERE product_type_id = ";

            if (quoteFields) {
                deleteParSql += "'" + type.getProductTypeId()
                        + "' AND parent_id ='" + parent + "'";
            } else {
                deleteParSql += type.getProductTypeId() + " AND parent_id ="
                        + parent;
            }

            LOG.log(Level.FINE, "removeParentFromProductType: Executing: "
                    + deleteParSql);
            statement.execute(deleteParSql);
            conn.commit();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING,
                    "Exception removing parent from product type "
                            + type.getName() + ". Message: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e2) {
                LOG.log(Level.SEVERE,
                        "Unable to rollback removeParentFromProductType transaction. Message: "
                                + e2.getMessage());
            }
            throw new ValidationLayerException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignore) {
                }

            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#getElements(org.apache.oodt.cas.filemgr.structs.ProductType)
     */
    public List<Element> getElements(ProductType type)
            throws ValidationLayerException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        List<Element> elements;

        elements = new Vector<Element>();

        String currProduct = type.getProductTypeId();
        while (currProduct != null) {

            try {
                conn = dataSource.getConnection();
                statement = conn.createStatement();

                String elementSql = "SELECT elements.* from elements, product_type_element_map WHERE product_type_element_map.product_type_id = ";

                if (quoteFields) {
                    elementSql += "'" + currProduct + "'";
                } else {
                    elementSql += currProduct;
                }

                elementSql += " AND product_type_element_map.element_id = elements.element_id";

                LOG.log(Level.FINE, "getElements: Executing: " + elementSql);
                rs = statement.executeQuery(elementSql);

                while (rs.next()) {
                    Element element = DbStructFactory.getElement(rs);
                    elements.add(element);
                }

            } catch (Exception e) {
                LOG.log(Level.SEVERE, e.getMessage());
                LOG.log(Level.WARNING, "Exception reading elements. Message: "
                        + e.getMessage());
                throw new ValidationLayerException(e);
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

            // advance to the product parent
            try {
                conn = dataSource.getConnection();
                statement = conn.createStatement();

                String getParentSql = "SELECT parent_id from sub_to_super_map where product_type_id = ";
                if (quoteFields) {
                    getParentSql += "'" + currProduct + "'";
                } else {
                    getParentSql += currProduct;
                }

                LOG.log(Level.FINE, "getElements: Executing: " + getParentSql);
                rs = statement.executeQuery(getParentSql);

                currProduct = null;
                while (rs.next()) {
                    currProduct = DbStructFactory.getParent(rs);
                }

            } catch (Exception e) {
                LOG.log(Level.SEVERE, e.getMessage());
                LOG.log(Level.WARNING,
                        "Exception reading product parent. Message: "
                                + e.getMessage());
                throw new ValidationLayerException(e);
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

        }

        return elements;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#getElements()
     */
    public List<Element> getElements() throws ValidationLayerException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        List<Element> elements = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String dataTypeSql = "SELECT * from elements";

            LOG.log(Level.FINE, "getElements: Executing: " + dataTypeSql);
            rs = statement.executeQuery(dataTypeSql);

            elements = new Vector<Element>();

            while (rs.next()) {
                Element element = DbStructFactory.getElement(rs);
                LOG.log(Level.FINE, "getElements: adding element: "
                        + element.getElementName());
                elements.add(element);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception reading elements. Message: "
                    + e.getMessage());
            throw new ValidationLayerException(e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return elements;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#getElementById(java.lang.String)
     */
    public Element getElementById(String elementId)
            throws ValidationLayerException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        Element element = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String elementSql = "SELECT * from elements WHERE element_id = "
                    + elementId;

            LOG.log(Level.FINE, "getElementById: Executing: " + elementSql);
            rs = statement.executeQuery(elementSql);

            while (rs.next()) {
                element = DbStructFactory.getElement(rs);
                LOG.log(Level.FINE, "getElementById: adding element: "
                        + element.getElementName());
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception reading element. Message: "
                    + e.getMessage());
            throw new ValidationLayerException(e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return element;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#getElementByName(java.lang.String)
     */
    public Element getElementByName(String elementName)
            throws ValidationLayerException {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        Element element = null;

        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();

            String elementSql = "SELECT * from elements WHERE element_name = "
                    + elementName;

            LOG.log(Level.FINE, "getElementByName: Executing: " + elementSql);
            rs = statement.executeQuery(elementSql);

            while (rs.next()) {
                element = DbStructFactory.getElement(rs);
                LOG.log(Level.FINE, "getElementByName: adding element: "
                        + element.getElementName());
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception reading element. Message: "
                    + e.getMessage());
            throw new ValidationLayerException(e);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }

            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignore) {
                }

            }

            if (conn != null) {
                try {
                    conn.close();

                } catch (SQLException ignore) {
                }

            }
        }

        return element;
    }
}
