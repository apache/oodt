// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.pcs.input;

import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//JDK imports

/**
 * <p>
 * A Configuration File Writer for PGEs.
 * </p>
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public final class PGEConfigFileWriter implements PGEConfigFileKeys,
    PGEDataParseKeys {

  /* the PGE configuration file that we're writing */
  private PGEConfigurationFile configFile = null;

  /* our log stream */
  private static Logger LOG = Logger.getLogger(PGEConfigFileWriter.class
      .getName());

  /*
   * whether or not the values in the XML file should be URLEncoded: if true,
   * the values will be encoded using UTF-8.
   */
  private boolean urlEncoding = false;

  /* schema location for the PGE input.xsd file */
  private String schemaLocation = null;

  /**
   * <p>
   * Default Constructor
   * </p>
   * .
   * 
   * @param config
   *          The ConfigurationFile that this writer is responsible for writing.
   * 
   */
  public PGEConfigFileWriter(PGEConfigurationFile config) {
    this.configFile = config;
  }

  /**
   * <p>
   * Writes the ConfigurationFile to the specified filePath.
   * </p>
   * 
   * @param filePath
   *          The filePath of the XML config file to write.
   * @throws Exception
   *           If any error occurs.
   */
  public void writeToXmlFile(String filePath) throws Exception {
    XMLUtils.writeXmlFile(getConfigFileXml(), filePath);
  }

  /**
   * 
   * @return An XML DOM {@link Document} representation of the internal
   *         PGEConfigurationFile.
   * @throws Exception
   *           If any error occurs.
   */
  public Document getConfigFileXml() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    Document document;

    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      document = builder.newDocument();

      Element root = (Element) document.createElement(PGE_INPUT_TAG_NAME);
      root.setAttribute("xmlns:xsi",
          "http://www.w3.org/2001/XMLSchema-instance");
      root
          .setAttribute(
              "xsi:noNamespaceSchemaLocation",
              (schemaLocation == null || (schemaLocation.equals("")) ? "input.xsd"
                  : schemaLocation));
      document.appendChild(root);

      if (configFile != null) {
        // write the PGE Name group
        if (configFile.getPgeName() != null) {
          PGEGroup pgeNameGroup = new PGEGroup(PGE_NAME_GROUP);
          pgeNameGroup.addScalar(configFile.getPgeName());
          root.appendChild(getGroupElement(pgeNameGroup, document));
        }

        // write the input product files
        root.appendChild(getGroupElement(configFile.getInputProductFiles(),
            document));

        // write the static file identification files
        root.appendChild(getGroupElement(configFile
            .getStaticFileIdentificationFiles(), document));

        // write the dynamic auxilliary files
        root.appendChild(getGroupElement(configFile
            .getDynamicAuxiliaryInputFiles(), document));

        // write the recorded auxilliary files
        root.appendChild(getGroupElement(configFile
            .getRecordedAuxiliaryInputFiles(), document));

        // write the product path group
        if (configFile.getProductPath() != null) {
          PGEGroup productPathGroup = new PGEGroup(PRODUCT_PATH_GROUP);
          productPathGroup.addScalar(configFile.getProductPath());
          root.appendChild(getGroupElement(productPathGroup, document));
        }

        // write the monitor level group
        root.appendChild(getGroupElement(configFile.getMonitorLevelGroup(),
            document));

        // write the monitor group
        if (configFile.getMonitorFilenameFormat() != null
            && configFile.getMonitorPath() != null) {
          PGEGroup monitorGroup = new PGEGroup(MONITOR_GROUP);
          monitorGroup.addScalar(configFile.getMonitorPath());
          monitorGroup.addScalar(configFile.getMonitorFilenameFormat());
          root.appendChild(getGroupElement(monitorGroup, document));
        }

        // write the pge specific groups
        for (String pgeSpecificGroupName : configFile.getPgeSpecificGroups().keySet()) {
          PGEGroup pgeSpecificGroup = (PGEGroup) configFile
              .getPgeSpecificGroups().get(pgeSpecificGroupName);

          root.appendChild(getGroupElement(pgeSpecificGroup, document));
        }

      }

      return document;

    } catch (ParserConfigurationException pce) {
      LOG.log(Level.WARNING, "Error generating pge configuration file!: "
          + pce.getMessage());
      throw new Exception("Error generating pge configuration file!: "
          + pce.getMessage());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw e;
    }

  }

  private Element getGroupElement(PGEGroup group, Document document)
      throws Exception {
    Element groupElem = document.createElement(GROUP_TAG_NAME);
    groupElem.setAttribute(NAME_ATTR, group.getName());

    if (group.getNumScalars() > 0) {
      for (String scalarName : group.getScalars().keySet()) {
        PGEScalar scalar = group.getScalar(scalarName);

        Element scalarElem = document.createElement(SCALAR_TAG_NAME);
        scalarElem.setAttribute(NAME_ATTR, scalar.getName());

        if (scalar.getValue() == null) {
          throw new Exception("Attempt to write null value for scalar: ["
                              + scalarName + "] to PGE config file!");
        }

        if (urlEncoding) {
          try {
            scalarElem.appendChild(document.createTextNode(URLEncoder.encode(
                scalar.getValue(), "UTF-8")));
          } catch (UnsupportedEncodingException e) {
            LOG.log(Level.WARNING,
                "Error creating text node for scalar element: "
                + scalar.getName() + " in pge group: " + group.getName()
                + " Message: " + e.getMessage());
          }

        } else {
          scalarElem.appendChild(document.createTextNode(scalar.getValue()));
        }

        groupElem.appendChild(scalarElem);
      }

    }

    if (group.getNumVectors() > 0) {
      for (String vectorName : group.getVectors().keySet()) {
        PGEVector vector = group.getVector(vectorName);

        Element vectorElem = document.createElement(VECTOR_TAG_NAME);
        vectorElem.setAttribute(NAME_ATTR, vector.getName());

        for (Object o : vector.getElements()) {
          String element = (String) o;

          if (element == null) {
            throw new Exception("Attempt to write null value for vector: ["
                                + vectorName + "] to PGE config file!");
          }

          Element elementElem = document.createElement(VECTOR_ELEMENT_TAG);
          if (urlEncoding) {
            try {
              elementElem.appendChild(document.createTextNode(URLEncoder
                  .encode(element, "UTF-8")));
            } catch (UnsupportedEncodingException e) {
              LOG.log(Level.WARNING,
                  "Error creating text node for vector element: "
                  + vector.getName() + " in pge group: " + group.getName()
                  + " Message: " + e.getMessage());
            }
          } else {
            elementElem.appendChild(document.createTextNode(element));
          }

          vectorElem.appendChild(elementElem);
        }

        groupElem.appendChild(vectorElem);
      }
    }

    if (group.getNumMatrixs() > 0) {
      for (String matrixName : group.getMatrixs().keySet()) {
        PGEMatrix matrix = group.getMatrix(matrixName);

        Element matrixElem = document.createElement(MATRIX_TAG_NAME);
        matrixElem.setAttribute(NAME_ATTR, matrix.getName());

        int rowNum = 0;
        for (List<Object> objects : matrix.getRows()) {
          List rowValues = (List) objects;

          Element rowElem = document.createElement(MATRIX_ROW_TAG);

          int colNum = 0;
          for (Object rowValue : rowValues) {
            String colValue = (String) rowValue;
            Element colElem = document.createElement(MATRIX_COL_TAG);

            if (colValue == null) {
              throw new Exception("Attempt to write null value for matrix: ["
                                  + matrixName + "]: " + "(" + rowNum + "," + colNum + ")");
            }

            if (urlEncoding) {
              try {
                colElem.appendChild(document.createTextNode(URLEncoder.encode(
                    colValue, "UTF-8")));
              } catch (UnsupportedEncodingException e) {
                LOG.log(Level.WARNING,
                    "Error creating node for matrix element: "
                    + matrix.getName() + " (" + rowNum + "," + colNum
                    + ") in pge group: " + group.getName() + " Message: "
                    + e.getMessage());
              }

            } else {
              colElem.appendChild(document.createTextNode(colValue));
            }

            colNum++;
          }

          rowNum++;
        }

        groupElem.appendChild(matrixElem);
      }
    }

    if (group.getNumGroups() > 0) {
      for (String groupName : group.getGroups().keySet()) {
        PGEGroup subgroup = group.getGroup(groupName);
        Element subgroupElem = getGroupElement(subgroup, document);
        groupElem.appendChild(subgroupElem);
      }

    }

    return groupElem;

  }

  /**
   * @return Returns the urlEncoding.
   */
  public boolean isUrlEncoding() {
    return urlEncoding;
  }

  /**
   * @param urlEncoding
   *          The urlEncoding to set.
   */
  public void setUrlEncoding(boolean urlEncoding) {
    this.urlEncoding = urlEncoding;
  }

  /**
   * @return Returns the schemaLocation.
   */
  public String getSchemaLocation() {
    return schemaLocation;
  }

  /**
   * @param schemaLocation
   *          The schemaLocation to set.
   */
  public void setSchemaLocation(String schemaLocation) {
    this.schemaLocation = schemaLocation;
  }

}
