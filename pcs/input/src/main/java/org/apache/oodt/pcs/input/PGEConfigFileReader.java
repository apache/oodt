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

//JDK imports
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * 
 * <p>
 * A Reader for reading the constructs defined in a {@link PGEConfigurationFile}.
 * The constructs are read and a new {@link PGEConfigurationFile} object is
 * constructed and returned.
 * </p>
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class PGEConfigFileReader {

  /**
   * <p>
   * Default Constructor
   * </p>
   */
  public PGEConfigFileReader() {
  }

  /**
   * 
   * <p>
   * Reads the PGE Configuration file from the given <code>url</code>.
   * </p>
   * 
   * @param url
   *          The {@link URL} pointer to the pge configuration file.
   * @return A new {@link PGEConfigurationFile} object, created from the
   *         specified URL.
   * @throws PGEConfigFileException
   *           If there is an error reading the url.
   */
  public PGEConfigurationFile read(URL url) throws PGEConfigFileException {
    PGEConfigurationFile configFile;

    try {
      configFile = read(url.openStream());
    } catch (IOException e) {
      throw new PGEConfigFileException("Unable to read PGE "
          + "configuration file from url: " + url + ": Message: "
          + e.getMessage());
    }

    return configFile;
  }

  /**
   * 
   * <p>
   * Reads a PGEConfigurationFile from the specified <code>is</code>
   * InputStream.
   * </p>
   * 
   * @param is
   *          The InputStream to read the PGEConfigurationFile from.
   * @return A new {@link PGEConfigurationFile}, created from the specified
   *         InputStream.
   * @throws PGEConfigFileException
   *           If any error occurs.
   */
  public PGEConfigurationFile read(InputStream is)
      throws PGEConfigFileException {
    PGEConfigurationFile configFile;

    DocumentBuilderFactory factory;
    DocumentBuilder parser;
    Document document;

    if (is == null) {
      return null;
    }

    InputSource inputSource = new InputSource(is);

    try {
      factory = DocumentBuilderFactory.newInstance();
      parser = factory.newDocumentBuilder();
      document = parser.parse(inputSource);
    } catch (Exception parseException) {
      parseException.printStackTrace();
      return null;
    }

    // okay, construct the PGEConfigurationFile now
    configFile = new PGEConfigurationFile();

    Element pgeConf = document.getDocumentElement();

    NodeList pgeGroups = pgeConf.getElementsByTagName("group");

    for (int i = 0; i < pgeGroups.getLength(); i++) {
      // get the name of the group
      Element group = (Element) pgeGroups.item(i);
      String groupName = group.getAttribute("name");

      if (groupName.equals("PGENameGroup")) {
        addPGEName(configFile, group);
      } else if (groupName.equals("InputProductFiles")) {
        addInputProductFiles(configFile, group);
      } else if (groupName.equals("StaticFileIdentificationFiles")) {
        addSFIFFiles(configFile, group);
      } else if (groupName.equals("DynamicAuxiliaryInputFiles")) {
        addDynamicAuxInputFiles(configFile, group);
      } else if (groupName.equals("RecordedAuxiliaryInputFiles")) {
        addRecAuxInputFiles(configFile, group);
      } else if (groupName.equals("ProductPathGroup")) {
        addProductPath(configFile, group);
      } else if (groupName.equals("MonitorGroup")) {
        addMonitorGroup(configFile, group);
      } else if (groupName.equals("MonitorLevel")) {
        addMonitorLevels(configFile, group);
      } else {
        // pge specific groups, just add generic groups
        addPGESpecificGroup(configFile, group);
      }
    }

    return configFile;
  }

  private void addPGESpecificGroup(PGEConfigurationFile configFile,
      Element group) throws PGEConfigFileException {

    List scalars = PGEXMLFileUtils.getScalars(group);
    List vectors = PGEXMLFileUtils.getVectors(group);
    List matrixs = PGEXMLFileUtils.getMatrixs(group);

    PGEGroup pgeGroup = new PGEGroup(group.getAttribute("name"));

    for (Object scalar : scalars) {
      PGEScalar s = (PGEScalar) scalar;
      pgeGroup.addScalar(s);
    }

    for (Object vector : vectors) {
      PGEVector v = (PGEVector) vector;
      pgeGroup.addVector(v);
    }

    for (Object matrix : matrixs) {
      PGEMatrix m = (PGEMatrix) matrix;
      pgeGroup.addMatrix(m);
    }

    configFile.getPgeSpecificGroups().put(pgeGroup.getName(), pgeGroup);

  }

  private void addMonitorLevels(PGEConfigurationFile configFile, Element group) {

    List scalars = PGEXMLFileUtils.getScalars(group);

    if (scalars != null && scalars.size() > 0) {
      for (Object scalar1 : scalars) {
        PGEScalar scalar = (PGEScalar) scalar1;
        configFile.getMonitorLevelGroup().addScalar(scalar);
      }
    }

  }

  private void addMonitorGroup(PGEConfigurationFile configFile, Element group)
      throws PGEConfigFileException {

    List scalars = PGEXMLFileUtils.getScalars(group);

    // the list should be not be null
    if (scalars == null) {
      throw new PGEConfigFileException(
          "There is no monitor path or monitor filename format defined in the MonitorGroup!");
    }

    PGEScalar monPath = null, monFilenameFormat = null;

    for (Object scalar1 : scalars) {
      PGEScalar scalar = (PGEScalar) scalar1;

      if (scalar.getName().equals("MonitorPath")) {
        monPath = scalar;
      } else if (scalar.getName().equals("MonitorFilenameFormat")) {
        monFilenameFormat = scalar;
      }
    }

    configFile.setMonitorPath(monPath);
    configFile.setMonitorFilenameFormat(monFilenameFormat);

  }

  private void addProductPath(PGEConfigurationFile configFile, Element group)
      throws PGEConfigFileException {
    List scalars = PGEXMLFileUtils.getScalars(group);

    // the list should be size 1
    if (scalars == null || (scalars.size() != 1)) {
      throw new PGEConfigFileException(
          "There is no product path defined in the configuration file, or there is more than one scalar listed in the ProductPathGroup!");
    }

    PGEScalar scalar = (PGEScalar) scalars.get(0);

    // the name of the product path should be ProductPath

    if (!scalar.getName().equals("ProductPath")) {
      throw new PGEConfigFileException(
          "The product path should be defined as a scalar with the name \"ProductPath\"!");
    }

    configFile.setProductPath(scalar);
  }

  private void addPGEName(PGEConfigurationFile configFile, Element group)
      throws PGEConfigFileException {

    // get the scalars, there should be only one
    List scalars = PGEXMLFileUtils.getScalars(group);

    // the list should be size 1
    if (scalars == null || (scalars.size() != 1)) {
      throw new PGEConfigFileException(
          "There is no PGEName defined in the configuration file, or there is more than one scalar listed in the PGENameGroup");
    }

    // the name of the scalar should be PGEName
    PGEScalar scalar = (PGEScalar) scalars.get(0);

    if (!scalar.getName().equals("PGEName")) {
      throw new PGEConfigFileException(
          "The name of the PGE should be defined as a scalar with the name \"PGEName\"!");
    }

    // okay, we're set, set the PGE Name
    configFile.setPgeName(scalar);

  }

  private void addInputProductFiles(PGEConfigurationFile configFile,
      Element group) throws PGEConfigFileException {
    addScalarFilesToGroup(group, configFile.getInputProductFiles());
    addVectorFilesToGroup(group, configFile.getInputProductFiles());
  }

  private void addSFIFFiles(PGEConfigurationFile configFile, Element group)
      throws PGEConfigFileException {
    addScalarFilesToGroup(group, configFile.getStaticFileIdentificationFiles());
  }

  private void addDynamicAuxInputFiles(PGEConfigurationFile configFile,
      Element group) throws PGEConfigFileException {
    addScalarFilesToGroup(group, configFile.getDynamicAuxiliaryInputFiles());
  }

  private void addRecAuxInputFiles(PGEConfigurationFile configFile,
      Element group) throws PGEConfigFileException {
    addScalarFilesToGroup(group, configFile.getRecordedAuxiliaryInputFiles());
  }

  private void addScalarFilesToGroup(Element group, PGEGroup pgeGroup) {
    // get the scalars, and add them to the group
    List scalars = PGEXMLFileUtils.getScalars(group);

    if (scalars != null && scalars.size() > 0) {
      for (Object scalar1 : scalars) {
        PGEScalar scalar = (PGEScalar) scalar1;
        pgeGroup.addScalar(scalar);
      }
    }
  }

  private void addVectorFilesToGroup(Element group, PGEGroup pgeGroup)
      throws PGEConfigFileException {
    // get the vectors, and add them to the group
    List vectors = PGEXMLFileUtils.getVectors(group);

    if (vectors != null && vectors.size() > 0) {
      for (Object vector1 : vectors) {
        PGEVector vector = (PGEVector) vector1;
        pgeGroup.addVector(vector);
      }
    }
  }

}
