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

package org.apache.oodt.cas.pge.writers;

//JDK imports

import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.metadata.Metadata;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

//OODT imports
//Junit imports

/**
 * 
 * Test harness for the MetadataKeyReplacementWriter.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class MetadataKeyReplacementTemplateWriterTest extends TestCase {

  private static final Logger LOG = Logger
      .getLogger(MetadataKeyReplacementTemplateWriterTest.class.getName());

  private static final String expected = "Welcome to ApacheCon Paul and Chris! You are a member of the following projects OODT,Tika,SIS,Gora.";

  public void testGenerateTemplate() throws IOException {
    MetadataKeyReplacerTemplateWriter writer = new MetadataKeyReplacerTemplateWriter();
    String templateSourcePath = "./src/test/resources/metkeyreplace.template";
    String outPath = File.createTempFile("foo", "bar").getParentFile()
        .getAbsolutePath();
    if (!outPath.endsWith("/"))
      outPath += "/";

    outPath += "generated.template";

    Metadata met = new Metadata();
    met.addMetadata("Person1", "Paul");
    met.addMetadata("Person2", "Chris");
    met.addMetadata("Projects", "OODT");
    met.addMetadata("Projects", "Tika");
    met.addMetadata("Projects", "SIS");
    met.addMetadata("Projects", "Gora");

    try {
      writer.generateFile(outPath, met, LOG, templateSourcePath);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      fail(e.getMessage());
    }

    assertTrue(new File(outPath).exists());
    String fileString = FileUtils.readFileToString(new File(outPath));
    assertNotNull(fileString);
    assertEquals(expected, fileString);
  }
}
