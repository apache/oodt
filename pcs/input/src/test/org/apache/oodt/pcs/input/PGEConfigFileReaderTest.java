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
import java.util.Iterator;

//Junit imports
import junit.framework.TestCase;

/**
 * <p>
 * A TestCase to test the PGEConfigFileReader
 * </p>
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class PGEConfigFileReaderTest extends TestCase {

  private PGEConfigurationFile configFile = null;

  protected void setUp() {
    PGEConfigFileReader reader = new PGEConfigFileReader();

    try {
      configFile = reader.read(this.getClass().getResource(
          "pge-config-example.xml"));
    } catch (PGEConfigFileException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      fail(e.getMessage());
    }
  }

  public void testPGEName() {
    assertEquals("The PGE Name: " + configFile.getPgeName().getValue()
        + " is not equal to TestPGE", "Test PGE", configFile.getPgeName()
        .getValue());
  }

  public void testInputProductFiles() {
    PGEGroup inputProductFiles = configFile.getInputProductFiles();

    assertEquals("There is not 4 input product files!", 4, inputProductFiles
        .getNumScalars());
    assertEquals(
        "The VCID17Stream does not have the correct value!",
        "/data/o04521/10/oco_10_200090503_o04521_p125_vcid17_mcf200901051234ops.hdf",
        inputProductFiles.getScalar("VCID17Stream").getValue());

    assertEquals(
        "The VCID34Stream does not have the correct value!",
        "/data/o04521/10/oco_10_200090503_o04521_p125_vcid34_mcf200901051234ops.hdf",
        inputProductFiles.getScalar("VCID34Stream").getValue());
  }

  public void testSFIFInputFiles() {
    PGEGroup sfifFiles = configFile.getStaticFileIdentificationFiles();

    assertEquals("There is not 2 sfif files!", 2, sfifFiles.getNumScalars());

    assertEquals(
        "The SFIF34Stream does not have the correct value!",
        "/data/o04521/10/oco_10_200090503_o04521_p125_sfif34_mcf200901051234ops.hdf",
        sfifFiles.getScalar("SFIF34Stream").getValue());
  }

  public void testDynamicAuxFiles() {
    PGEGroup dynAuxFiles = configFile.getDynamicAuxiliaryInputFiles();

    assertEquals("There is not 1 dynamic aux file!", 1, dynAuxFiles
        .getNumScalars());

    assertEquals(
        "The AuxStream1 does not have the correct value!",
        "/data/o04521/10/oco_10_200090503_o04521_p125_aux1_mcf200901051234ops.aux",
        dynAuxFiles.getScalar("AuxStream1").getValue());
  }

  public void testRecordedAuxFiles() {
    PGEGroup recAuxFiles = configFile.getRecordedAuxiliaryInputFiles();

    assertEquals("There is not 1 recorded aux file!", 1, recAuxFiles
        .getNumScalars());

    assertEquals(
        "The RecAuxStream1 does not have the correct value!",
        "/data/o04521/10/oco_10_200090503_o04521_p125_raux1_mcf200901051234ops.raux",
        recAuxFiles.getScalar("RecAuxStream1").getValue());
  }

  public void testProductPath() {
    assertEquals("The product path: " + configFile.getProductPath().getValue()
        + " is not equal to /data", "/data", configFile.getProductPath()
        .getValue());
  }

  public void testMonitorPath() {
    assertEquals("The monitor path: " + configFile.getMonitorPath().getValue()
        + " is not equal to /data/monitor", "/data/monitor", configFile
        .getMonitorPath().getValue());
  }

  public void testMonitorFilenameFormat() {
    assertEquals("The monitor filename format: "
        + configFile.getMonitorFilenameFormat().getValue()
        + " is not equal to .monitor", ".monitor", configFile
        .getMonitorFilenameFormat().getValue());
  }

  public void testMonitorLevels() {
    assertEquals("The MonIO level was not set to High!", "High", configFile
        .getMonitorLevelGroup().getScalar("MonIO").getValue());
    assertEquals("The MonAlg level was not set to Off!", "Off", configFile
        .getMonitorLevelGroup().getScalar("MonAlg").getValue());
    assertEquals("The MonControl level was not set to Med!", "Med", configFile
        .getMonitorLevelGroup().getScalar("MonControl").getValue());
  }

  public void testPGESpecificInfo() {

    PGEGroup pgeSpecific = (PGEGroup) configFile.getPgeSpecificGroups().get(
        "MyPGEInfo");

    assertNotNull(pgeSpecific);

    // test that the scalar is read
    assertEquals("The test scalar 1 does not have a value of Scalar1",
        "Scalar1", pgeSpecific.getScalar("TestScalar1").getValue());

    // test that the vector is read
    PGEVector testVec = pgeSpecific.getVector("TestVector1");

    String testVal = "/data/1/2/3/file.file";
    boolean hasValue = false;

    for (Iterator i = testVec.getElements().iterator(); i.hasNext();) {
      String elem = (String) i.next();
      if (elem.equals(testVal)) {
        hasValue = true;
      }
    }

    assertTrue("The vector TestVector1 does not have the value " + testVal
        + "!", hasValue);

    // test that the matrix was read right
    PGEMatrix testMatrix = pgeSpecific.getMatrix("TestMatrix1");

    // row 1, col 0 should be 44
    assertEquals("The matrix value isn't 44!", 44, Integer
        .parseInt((String) testMatrix.getValue(1, 0)));
  }
}
