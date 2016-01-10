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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 
 * <p>
 * A SAX-based event handler to parse PGE data out of an underlying XML file.
 * </p>
 * 
 * 
 * @author mattmann
 * @version $Revision$
 */
public class PGEDataHandler extends DefaultHandler implements PGEDataParseKeys {

  /* our log stream */
  private static final Logger LOG = Logger.getLogger(PGEDataHandler.class
      .getName());

  /* scalars to be set as tags are encountered */
  private Map scalars = new ConcurrentHashMap();

  /* vectors to be set as tags are encountered */
  private Map vectors = new ConcurrentHashMap();

  /* matrices to be set as tags are encountered */
  private Map matrices = new ConcurrentHashMap();

  /* the status of the parse handler */
  private int parseStatus = UNSET;

  private boolean vecElement;

  private boolean matrixElement;

  private PGEScalar currentScalar = null;

  private PGEVector currentVector = null;

  private PGEMatrix currentMatrix = null;

  private StringBuffer charBuf;

  // needed for parsing matrix
  private int currentRow = 0;

  private int currentCol = 0;

  public PGEDataHandler() {
    vecElement = false;
    matrixElement = false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
   * java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException {
    if (notParsing()) {
      // only care about if it's a scalar/vector or matrix
      if (qName.equals(SCALAR_TAG_NAME)) {
        parseStatus = PARSING_SCALAR;
        currentScalar = new PGEScalar();
        currentScalar.setName(attributes.getValue(NAME_ATTR));
        charBuf = new StringBuffer();
      } else if (qName.equals(VECTOR_TAG_NAME)) {
        parseStatus = PARSING_VEC;
        currentVector = new PGEVector();
        currentVector.setName(attributes.getValue(NAME_ATTR));
      } else if (qName.equals(MATRIX_TAG_NAME)) {
        parseStatus = PARSING_MATRIX;
        currentMatrix = new PGEMatrix(attributes.getValue(NAME_ATTR), Integer
            .parseInt(attributes.getValue(ROWS_ATTR)), Integer
            .parseInt(attributes.getValue(COLS_ATTR)));
      }
    } else if (isParsingScalar()) {
      // shouldn't encountere another tag here
      throw new SAXException("Parsing scalar: [" + this.currentScalar.getName()
          + "]: Should not encounter another tag within");
    } else if (isParsingVector()) {
      if (qName.equals(VECTOR_ELEMENT_TAG)) {
        vecElement = true;
        charBuf = new StringBuffer();
      }

    } else if (isParsingMatrix()) {
      if (qName.equals(MATRIX_COL_TAG)) {
        matrixElement = true;
        charBuf = new StringBuffer();
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String localName, String qName)
      throws SAXException {
    if (qName.equals(SCALAR_TAG_NAME)) {
      this.currentScalar.setValue(this.charBuf.toString());
      this.scalars.put(this.currentScalar.getName(), this.currentScalar);
      clearCharBuf();
      parseStatus = UNSET;
    } else if (qName.equals(VECTOR_ELEMENT_TAG)) {
      this.currentVector.getElements().add(this.charBuf.toString());
      clearCharBuf();
      this.vecElement = false;
    } else if (qName.equals(VECTOR_TAG_NAME)) {
      // add the vector
      if (this.currentVector != null) {
        this.vectors.put(this.currentVector.getName(), this.currentVector);
        parseStatus = UNSET;
      }
    } else if (qName.equals(MATRIX_TAG_NAME)) {
      if (this.currentMatrix != null) {
        this.matrices.put(this.currentMatrix.getName(), this.currentMatrix);

        parseStatus = UNSET;
        this.currentCol = 0;
        this.currentRow = 0;
      }
    } else if (qName.equals(MATRIX_ROW_TAG)) {
      this.currentRow++;
      this.currentCol = 0;
    } else if (qName.equals(MATRIX_COL_TAG)) {
      this.currentMatrix.addValue(this.charBuf.toString(), this.currentRow,
          this.currentCol);
      this.currentCol++;
      clearCharBuf();
      this.matrixElement = false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
   */
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (isParsingScalar() || isParsingVectorElement()
        || isParsingMatrixElement()) {
      this.charBuf.append(ch, start, length);
    }
  }

  /**
   * @return the matrices
   */
  public Map getMatrices() {
    return matrices;
  }

  /**
   * @param matrices
   *          the matrices to set
   */
  public void setMatrices(Map matrices) {
    this.matrices = matrices;
  }

  /**
   * @return the scalars
   */
  public Map getScalars() {
    return scalars;
  }

  /**
   * @param scalars
   *          the scalars to set
   */
  public void setScalars(Map scalars) {
    this.scalars = scalars;
  }

  /**
   * @return the vectors
   */
  public Map getVectors() {
    return vectors;
  }

  /**
   * @param vectors
   *          the vectors to set
   */
  public void setVectors(Map vectors) {
    this.vectors = vectors;
  }

  public boolean isParsingScalar() {
    return parseStatus == PARSING_SCALAR;
  }

  public boolean isParsingVector() {
    return parseStatus == PARSING_VEC;
  }

  public boolean isParsingMatrix() {
    return parseStatus == PARSING_MATRIX;
  }

  public boolean isParsingMatrixElement() {
    return matrixElement;
  }

  public boolean isParsingVectorElement() {
    return vecElement;
  }

  public boolean notParsing() {
    return parseStatus == UNSET;
  }

  private void clearCharBuf() {
    this.charBuf.setLength(0);
    this.charBuf = null;
  }

}
