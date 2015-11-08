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
import java.io.File;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

//Junit imports
import junit.framework.TestCase;

/**
 * <p>
 * Test suite for the {@link PGEDataHandler}
 * </p>
 * .
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class PGEDataHandlerTest extends TestCase {

  private static final String testFilePath = "./src/main/resources/pge-data-sax-example.xml";

  private static final String expectedVecValSixthElement = "5.462772487746047e-18";

  public void testGetScalars() {
    PGEDataHandler handler = new PGEDataHandler();
    doParse(handler);
    assertNotNull(handler.getScalars());
    assertNotNull(handler.getScalars().keySet());
    assertEquals(1, handler.getScalars().keySet().size());
    assertTrue(handler.getScalars().containsKey("foo"));
    assertNotNull(handler.getScalars().entrySet());
    PGEScalar scalar = (PGEScalar) handler.getScalars().get("foo");
    assertEquals("bar", scalar.getValue());

  }

  public void testGetMatrices() {
    PGEDataHandler handler = new PGEDataHandler();
    doParse(handler);
    assertNotNull(handler.getMatrices());
    assertNotNull(handler.getMatrices().keySet());
    assertEquals(1, handler.getMatrices().keySet().size());
    assertTrue(handler.getMatrices().containsKey("foomatrix"));
    PGEMatrix matrix = (PGEMatrix) handler.getMatrices().get("foomatrix");
    assertNotNull(matrix);
    assertEquals(2, matrix.getNumCols());
    assertEquals(2, matrix.getRows().size());
    assertEquals("194", matrix.getValue(0, 0));
    assertEquals("2.2", matrix.getValue(1, 1));
  }

  public void testGetVectors() {
    PGEDataHandler handler = new PGEDataHandler();
    doParse(handler);
    assertNotNull(handler.getVectors());
    assertNotNull(handler.getVectors().keySet());
    assertEquals(1, handler.getVectors().keySet().size());
    assertTrue(handler.getVectors().containsKey(
        "solar_degrad_stddev_pixel_strong_co2"));
    PGEVector vec = (PGEVector) handler.getVectors().get(
        "solar_degrad_stddev_pixel_strong_co2");
    assertNotNull(vec);
    assertEquals(expectedVecValSixthElement, vec.getElements().get(5));

  }

  private void doParse(PGEDataHandler handler) {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    try {
      // Parse the input
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(new File(testFilePath), handler);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      fail("exception reading data out of: [" + testFilePath + "]: Message: "
          + e.getMessage());
    }

  }

}
