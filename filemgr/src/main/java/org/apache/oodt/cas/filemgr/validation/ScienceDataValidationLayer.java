/**
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
 * 
 * Determines the mapping of {@Element}s to {@link ProductType}s by
 * reading the {@link org.apache.oodt.cas.filemgr.catalog.ScienceDataCatalog} catalog schema tables concerning parameters and datasets.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class ScienceDataValidationLayer implements ValidationLayer {

  private static Logger LOG = Logger.getLogger(ScienceDataValidationLayer.class.getName());
  private DataSource ds;

  public ScienceDataValidationLayer(DataSource ds) {
    this.ds = ds;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.validation.ValidationLayer#addElement(gov
   * .nasa.jpl.oodt.cas.filemgr.structs.Element)
   */
  public void addElement(Element element) throws ValidationLayerException {
    String sql = "INSERT INTO parameter (dataset_id, longName, shortName, description) "
        + "VALUES (-1, '"
        + element.getElementName()
        + "','"
        + element.getElementName() + "', '" + element.getDescription() + "')";
    Connection conn = null;
    Statement statement = null;

    try {
      conn = ds.getConnection();
      statement = conn.createStatement();
      statement.execute(sql);
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }
      }

    }

  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.apache.oodt.cas.filemgr.validation.ValidationLayer#
   * addElementToProductType(org.apache.oodt.cas.filemgr.structs.ProductType,
   * org.apache.oodt.cas.filemgr.structs.Element)
   */
  public void addElementToProductType(ProductType productType, Element element)
      throws ValidationLayerException {
    String sql = "INSERT INTO dpMap (dataset_id, parameter_id) VALUES ("
        + productType.getProductTypeId() + ", " + element.getElementId() + ")";
    Connection conn = null;
    Statement statement = null;

    try {
      conn = ds.getConnection();
      statement = conn.createStatement();
      statement.execute(sql);
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }
      }

    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.validation.ValidationLayer#getElementById
   * (java.lang.String)
   */
  public Element getElementById(String elementId)
      throws ValidationLayerException {
    String sql = "SELECT parameter_id, shortName, description from parameter WHERE parameter_id = "
        + elementId;
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    Element element = null;

    try {
      conn = ds.getConnection();
      statement = conn.createStatement();
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        element = DbStructFactory.toScienceDataElement(rs);
      }
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (Exception ignore) {
        }
      }

      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }
      }

    }

    return element;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.validation.ValidationLayer#getElementByName
   * (java.lang.String)
   */
  public Element getElementByName(String elementName)
      throws ValidationLayerException {
    String sql = "SELECT parameter_id, shortName, description from parameter WHERE shortName = '"
        + elementName + "'";
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    Element element = null;

    try {
      conn = ds.getConnection();
      statement = conn.createStatement();
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        element = DbStructFactory.toScienceDataElement(rs);
      }
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (Exception ignore) {
        }
      }

      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }
      }

    }

    return element;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.filemgr.validation.ValidationLayer#getElements()
   */
  public List<Element> getElements() throws ValidationLayerException {
    String sql = "SELECT parameter_id, shortName, description from parameter ORDER by parameter_id DESC";
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    List<Element> elements = new Vector<Element>();

    try {
      conn = ds.getConnection();
      statement = conn.createStatement();
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        elements.add(DbStructFactory.toScienceDataElement(rs));
      }
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (Exception ignore) {
        }
      }

      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }
      }

    }

    return elements;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.validation.ValidationLayer#getElements(gov
   * .nasa.jpl.oodt.cas.filemgr.structs.ProductType)
   */
  public List<Element> getElements(ProductType productType) {
    String sql = "SELECT parameter_id, shortName, description from parameter WHERE dataset_id = "
        + productType.getProductTypeId() + " ORDER by parameter_id DESC";
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    List<Element> elements = new Vector<Element>();

    try {
      conn = ds.getConnection();
      statement = conn.createStatement();
      rs = statement.executeQuery(sql);
      while (rs.next()) {
        elements.add(DbStructFactory.toScienceDataElement(rs));
      }
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (Exception ignore) {
        }
      }

      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }
      }

    }

    return elements;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.validation.ValidationLayer#modifyElement(
   * org.apache.oodt.cas.filemgr.structs.Element)
   */
  public void modifyElement(Element element) throws ValidationLayerException {
    String sql = "UPDATE parameter SET longName='" + element.getElementName()
        + "', shortName='" + element.getElementName() + "',description='"
        + element.getDescription() + "' WHERE parameter_id = "
        + element.getElementId();

    Connection conn = null;
    Statement statement = null;

    try {
      conn = ds.getConnection();
      statement = conn.createStatement();
      statement.execute(sql);
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }
      }

    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.validation.ValidationLayer#removeElement(
   * org.apache.oodt.cas.filemgr.structs.Element)
   */
  public void removeElement(Element element) throws ValidationLayerException {
    String sql = "DELETE FROM parameter WHERE parameter_id = "
        + element.getElementId();

    Connection conn = null;
    Statement statement = null;

    try {
      conn = ds.getConnection();
      statement = conn.createStatement();
      statement.execute(sql);
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }
      }

    }

  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.apache.oodt.cas.filemgr.validation.ValidationLayer#
   * removeElementFromProductType
   * (org.apache.oodt.cas.filemgr.structs.ProductType,
   * org.apache.oodt.cas.filemgr.structs.Element)
   */
  public void removeElementFromProductType(ProductType productType,
      Element element) throws ValidationLayerException {
    String sql = "DELETE FROM dpMap WHERE parameter_id = "
        + element.getElementId() + " and dataset_id = "
        + productType.getProductTypeId();

    Connection conn = null;
    Statement statement = null;

    try {
      conn = ds.getConnection();
      statement = conn.createStatement();
      statement.execute(sql);
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }
      }

    }

  }

}
