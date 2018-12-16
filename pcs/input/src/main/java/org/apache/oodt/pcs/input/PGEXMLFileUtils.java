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

//OODT imports
import org.apache.oodt.commons.xml.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//JDK imports

/**
 * <p>
 * Low-level reading API to get {@link PGEVector}s, {@link PGEScalar}s, and
 * {@link PGEMatrix}s from underlying OCO XML files.
 * </p>
 * 
 * @author mattmann
 * @version $Revision$
 */
public final class PGEXMLFileUtils {

  /* our log stream */
  private static final Logger LOG = Logger.getLogger(PGEXMLFileUtils.class
      .getName());

  public static Map getMatrixsAsMap(Element group)
      throws PGEConfigFileException {
    // get the nodelist for the matrixs
    NodeList matrixs = group.getElementsByTagName("matrix");

    // if it's null, return null
    if (matrixs.getLength()==0) {
      return Collections.emptyMap();
    }

    Map matrixMap = new ConcurrentHashMap(matrixs.getLength());

    // for each matrix in the list, create a PGEMatrix with the name
    // attribute and the appropriate value

    for (int i = 0; i < matrixs.getLength(); i++) {
      Element matrix = (Element) matrixs.item(i);

      // get the name of the matrix
      String matrixName = matrix.getAttribute("name");

      // get the number of cols
      NodeList rowNodeList = matrix.getElementsByTagName("tr");

      // there has to be at least one 1
      if (rowNodeList == null || (rowNodeList.getLength() <= 0)) {
        throw new PGEConfigFileException(
            "there must be at least one row in a matrix!");
      }

      PGEMatrix pgeMatrix = new PGEMatrix();
      pgeMatrix.setName(matrixName);

      for (int j = 0; j < rowNodeList.getLength(); j++) {
        Element rowElem = (Element) rowNodeList.item(j);

        // get the number of cols
        NodeList colNodeList = rowElem.getElementsByTagName("td");

        // there must be at least one colum in each row
        if (colNodeList == null || (colNodeList.getLength() <= 0)) {
          throw new PGEConfigFileException(
              "there must be at least one column a matrix row!");
        }

        if (pgeMatrix.getNumCols() == 0) {
          // then set it
          pgeMatrix.setNumCols(colNodeList.getLength());
        }

        List colList = new Vector(colNodeList.getLength());

        for (int k = 0; k < colNodeList.getLength(); k++) {
          Element colElement = (Element) colNodeList.item(k);
          colList.add(DOMUtil.getSimpleElementText(colElement));
        }

        pgeMatrix.getRows().add(colList);
      }

      matrixMap.put(pgeMatrix.getName(), pgeMatrix);
    }
    return matrixMap;
  }

  public static List getMatrixs(Element group) throws PGEConfigFileException {
    // get the nodelist for the matrixs
    NodeList matrixs = group.getElementsByTagName("matrix");

    // if it's null, return null
    if (matrixs.getLength()==0) {
      return Collections.emptyList();
    }

    List matrixList = new Vector(matrixs.getLength());

    // for each matrix in the list, create a PGEMatrix with the name
    // attribute and the appropriate value

    for (int i = 0; i < matrixs.getLength(); i++) {
      Element matrix = (Element) matrixs.item(i);

      // get the name of the matrix
      String matrixName = matrix.getAttribute("name");

      // get the number of cols
      NodeList rowNodeList = matrix.getElementsByTagName("tr");

      // there has to be at least one 1
      if (rowNodeList == null || (rowNodeList.getLength() <= 0)) {
        throw new PGEConfigFileException(
            "there must be at least one row in a matrix!");
      }

      PGEMatrix pgeMatrix = new PGEMatrix();
      pgeMatrix.setName(matrixName);

      for (int j = 0; j < rowNodeList.getLength(); j++) {
        Element rowElem = (Element) rowNodeList.item(j);

        // get the number of cols
        NodeList colNodeList = rowElem.getElementsByTagName("td");

        // there must be at least one colum in each row
        if (colNodeList == null || (colNodeList.getLength() <= 0)) {
          throw new PGEConfigFileException(
              "there must be at least one column a matrix row!");
        }

        if (pgeMatrix.getNumCols() == 0) {
          // then set it
          pgeMatrix.setNumCols(colNodeList.getLength());
        }

        List colList = new Vector(colNodeList.getLength());

        for (int k = 0; k < colNodeList.getLength(); k++) {
          Element colElement = (Element) colNodeList.item(k);
          colList.add(DOMUtil.getSimpleElementText(colElement));
        }

        pgeMatrix.getRows().add(colList);
      }

      matrixList.add(pgeMatrix);
    }

    return matrixList;
  }

  public static Map getScalarsAsMap(Element group) {
    // get the nodelist for scalars
    NodeList scalars = group.getElementsByTagName("scalar");

    // if it's null, return null
    if (scalars.getLength()==0) {
      return Collections.emptyMap();
    }

    Map scalarMap = new ConcurrentHashMap(scalars.getLength());

    // for each scalar in the list, create a PGEScalar with the name
    // attribute, and appropriate value
    for (int i = 0; i < scalars.getLength(); i++) {
      Element scalar = (Element) scalars.item(i);

      // name of the scalar is in the name attribute
      String scalarName = scalar.getAttribute("name");

      // get the value of it
      String scalarValue = DOMUtil.getSimpleElementText(scalar);

      scalarMap.put(scalarName, new PGEScalar(scalarName, scalarValue));
    }

    return scalarMap;
  }

  public static List getScalars(Element group) {
    // get the nodelist for scalars
    NodeList scalars = group.getElementsByTagName("scalar");

    // if it's null, return null
    if (scalars.getLength()==0) {
      return Collections.emptyList();
    }

    List scalarList = new Vector(scalars.getLength());

    // for each scalar in the list, create a PGEScalar with the name
    // attribute, and appropriate value
    for (int i = 0; i < scalars.getLength(); i++) {
      Element scalar = (Element) scalars.item(i);

      // name of the scalar is in the name attribute
      String scalarName = scalar.getAttribute("name");

      // get the value of it
      String scalarValue = DOMUtil.getSimpleElementText(scalar);

      scalarList.add(new PGEScalar(scalarName, scalarValue));
    }

    return scalarList;
  }

  public static Map getVectorsAsMap(Element group)
      throws PGEConfigFileException {
    // get the nodelist for scalars
    NodeList vectors = group.getElementsByTagName("vector");

    // if it's null, return null
    if (vectors.getLength()==0) {
      return Collections.emptyMap();
    }

    Map vectorMap = new ConcurrentHashMap(vectors.getLength());

    // for each vector in the list, create a PGEVector with the name
    // attribute, and appropriate value
    for (int i = 0; i < vectors.getLength(); i++) {
      Element vector = (Element) vectors.item(i);

      // name of the vector is in the name attribute
      String vectorName = vector.getAttribute("name");

      PGEVector vec = new PGEVector();
      vec.setName(vectorName);

      // get the nodelist of elements
      NodeList vecElems = vector.getElementsByTagName("element");

      if (vecElems == null || (vecElems.getLength() <= 0)) {
        throw new PGEConfigFileException(
            "There must be at least one element in a PGEVector!");
      }

      List vecElemList = new Vector(vecElems.getLength());

      for (int j = 0; j < vecElems.getLength(); j++) {
        Element vecElem = (Element) vecElems.item(j);
        vecElemList.add(DOMUtil.getSimpleElementText(vecElem));
      }

      vec.setElements(vecElemList);
      vectorMap.put(vec.getName(), vec);
    }

    return vectorMap;
  }

  public static List getVectors(Element group) throws PGEConfigFileException {
    // get the nodelist for scalars
    NodeList vectors = group.getElementsByTagName("vector");

    // if it's null, return null
    if (vectors.getLength()==0) {
      return Collections.emptyList();
    }

    List vectorList = new Vector(vectors.getLength());

    // for each vector in the list, create a PGEVector with the name
    // attribute, and appropriate value
    for (int i = 0; i < vectors.getLength(); i++) {
      Element vector = (Element) vectors.item(i);

      // name of the vector is in the name attribute
      String vectorName = vector.getAttribute("name");

      PGEVector vec = new PGEVector();
      vec.setName(vectorName);

      // get the nodelist of elements
      NodeList vecElems = vector.getElementsByTagName("element");

      if (vecElems == null || (vecElems.getLength() <= 0)) {
        throw new PGEConfigFileException(
            "There must be at least one element in a PGEVector!");
      }

      List vecElemList = new Vector(vecElems.getLength());

      for (int j = 0; j < vecElems.getLength(); j++) {
        Element vecElem = (Element) vecElems.item(j);
        vecElemList.add(DOMUtil.getSimpleElementText(vecElem));
      }

      vec.setElements(vecElemList);
      vectorList.add(vec);
    }

    return vectorList;
  }

  public static Document getDocumentRoot(String xmlFile) {
    // open up the XML file
    DocumentBuilderFactory factory;
    DocumentBuilder parser;
    Document document;
    InputSource inputSource;

    InputStream xmlInputStream;

    try {
      xmlInputStream = new File(xmlFile).toURI().toURL().openStream();
    } catch (IOException e) {
      LOG.log(Level.WARNING, "IOException when getting input stream from ["
          + xmlFile + "]: returning null document root");
      return null;
    }

    inputSource = new InputSource(xmlInputStream);

    try {
      factory = DocumentBuilderFactory.newInstance();
      parser = factory.newDocumentBuilder();
      document = parser.parse(inputSource);
    } catch (Exception e) {
      LOG.warning("Unable to parse xml file [" + xmlFile + "]." + "Reason is ["
          + e + "]");
      return null;
    }

    return document;
  }

}
