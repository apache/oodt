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

package org.apache.oodt.cas.metadata.extractors;

//OODT imports
import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.MetadataTestCase;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.exec.ExecHelper;

//JDK imports
import java.io.File;
import java.net.URLEncoder;

/**
 * 
 * Test Suite for the {@link ExternMetExtractor} 
 *          .
 */
public class TestExternMetExtractor extends MetadataTestCase {

  private ExternMetExtractor extractor;

  private static final String FILENAME = "Filename";

  private static final String FILE_LOCATION = "FileLocation";

  private static final String PRODUCT_TYPE = "ProductType";

  private static final String expectedFilename = "/testfile.txt";

  private static final String expectedProductType = "GenericFile";
  
  private File extractFile;
  
  private File confFile;
  
  private File metFile;
  
  private String expectedFileLocation;
  
  public TestExternMetExtractor(String name){
    super(name);
  }

  public void testExtractor() {

    Metadata met = null;

    try {
      met = extractor.extractMetadata(this.extractFile, this.confFile);
    } catch (MetExtractionException e) {
      fail(e.getMessage());
    }

    assertNotNull(met);
    assertTrue(this.metFile.exists());
    assertTrue(met.containsKey(FILENAME));
    assertTrue(met.containsKey(FILE_LOCATION));
    assertTrue(met.containsKey(PRODUCT_TYPE));

    assertEquals(expectedFilename.substring(1), met.getMetadata(FILENAME));
    assertEquals("Expected: ["+this.expectedFileLocation+"]; Actual: ["+met.getMetadata(FILE_LOCATION)+"]", this.expectedFileLocation, met.getMetadata(FILE_LOCATION));
    assertEquals(expectedProductType, met.getMetadata(PRODUCT_TYPE));

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.metadata.MetadataTestCase#tearDown()
   */
  public void tearDown() throws Exception {
    super.tearDown();
    if (this.metFile != null && this.metFile.exists()) {
      this.metFile.delete();
      this.metFile = null;
    }
    
    if(this.confFile != null) this.confFile = null;
    if(this.extractFile != null) this.extractFile = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.metadata.MetadataTestCase#setUp()
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    String configFilename = "/extern-config.xml";
    String extractFilename = "/testfile.txt";
    String extractorFilename = "/testExtractor";
    String sampleMetFilename = "/samplemet.xml";
    
    this.confFile = super.getTestDataFile(configFilename);
    this.extractFile = super.getTestDataFile(extractFilename);
    this.metFile = new File(this.extractFile.getCanonicalPath()+".met");
    this.expectedFileLocation = this.extractFile.getParent();
    
    File extractorFile = super.getTestDataFile(extractorFilename);
    
    // make it executable
    // yes this is ghetto
    String chmodCmd = "chmod +x "+extractorFile.getAbsolutePath();
    ExecHelper.execUsingShell(chmodCmd);
    
    // replace the FileLocation met field in the sample met file
    // with the actual file location of the extractFile
    File sampleMetFile = super.getTestDataFile(sampleMetFilename);
    String sampleMetFileContents = FileUtils.readFileToString(sampleMetFile);
    String extractFileLocKey = "[EXTRACT_FILE_LOC]";
    sampleMetFileContents = sampleMetFileContents.replace(extractFileLocKey, URLEncoder.encode(extractFile.getParent(), "UTF-8"));
    FileUtils.writeStringToFile(sampleMetFile, sampleMetFileContents, "UTF-8");
    
    // replace the path to the sample met file inside of testExtractor
    String extractorFileContents = FileUtils.readFileToString(extractorFile);
    String sampleMetFilePathKey = "<TEST_SAMPLE_MET_PATH>";
    extractorFileContents = extractorFileContents.replace(sampleMetFilePathKey, sampleMetFile.getAbsolutePath());
    FileUtils.writeStringToFile(extractorFile, extractorFileContents);
    
    // replace path in confFile named TEST_PATH
    String testPathKey = "TEST_PATH";
    String confFileContents = FileUtils.readFileToString(this.confFile);
    Metadata replaceMet = new Metadata();
    replaceMet.addMetadata(testPathKey, extractorFile.getParent());
    confFileContents = PathUtils.replaceEnvVariables(confFileContents, replaceMet);
    FileUtils.writeStringToFile(this.confFile, confFileContents);


    try {
      extractor = new ExternMetExtractor();
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

}
