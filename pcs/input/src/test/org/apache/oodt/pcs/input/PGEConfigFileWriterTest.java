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
import org.apache.oodt.commons.xml.XMLUtils;

//JDK imports
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.w3c.dom.Document;

//Junit imports
import junit.framework.TestCase;

/**
 * <p>
 * A Testcase for the PGEConfigFileWriter.
 * </p>
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class PGEConfigFileWriterTest extends TestCase {

  private PGEConfigurationFile configFile = null;

  private PGEConfigFileWriter configFileWriter = null;

  /**
     * 
     */
  public PGEConfigFileWriterTest() {
    configFile = new PGEConfigurationFile();
    configFileWriter = new PGEConfigFileWriter(configFile);
  }

  public void testWriteRead() {
    configFile.setPgeName(new PGEScalar("PGEName", "fts_sliceipp"));

    Document configFileDoc = null;

    try {
      configFileDoc = configFileWriter.getConfigFileXml();
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      fail(e.getMessage());
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    XMLUtils.writeXmlToStream(configFileDoc, out);

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    PGEConfigFileReader reader = new PGEConfigFileReader();

    PGEConfigurationFile readConfigFile = null;

    try {
      readConfigFile = reader.read(in);
    } catch (PGEConfigFileException e) {
      fail(e.getMessage());
    }

    assertNotNull(readConfigFile);
    assertEquals("fts_sliceipp", readConfigFile.getPgeName().getValue());

  }

}
