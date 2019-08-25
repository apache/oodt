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
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.MetadataTestCase;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Tests the CopyAndRewriteExtractor.
 */
public class TestCopyAndRewriteExtractor extends MetadataTestCase {

  private CopyAndRewriteExtractor extractor;

  private static final String FILENAME = "Filename";

  private static final String FILE_LOCATION = "FileLocation";

  private static final String PRODUCT_TYPE = "ProductType";

  private static final String expectedProductType = "NewProductTypeGenericFile";
  
  private static final String expectedFilename = "testfile.txt";

  private String expectedFileLocation;
  
  private File confFile;
  
  private File sampleMetFile;
  
  private File extractFile;
  
  public TestCopyAndRewriteExtractor(String name) {
    super(name);
  }
  
  

  public void testExtractMetadata() {
    Metadata met = null;

    try {
      met = extractor.extractMetadata(this.extractFile.getCanonicalPath());
    } catch (Exception e) {
      fail(e.getMessage());
    }

    assertNotNull(met);
    assertNotNull(met.getMap());
    assertNotNull(met.getMap().keySet());
    assertEquals(3, met.getMap().keySet().size());
    assertTrue(met.containsKey(FILENAME));
    assertEquals(expectedFilename, met.getMetadata(FILENAME));
    assertTrue(met.containsKey(PRODUCT_TYPE));
    assertEquals(expectedProductType, met.getMetadata(PRODUCT_TYPE));
    assertTrue(met.containsKey(FILE_LOCATION));
    assertEquals("The expected file location: [" + expectedFileLocation
        + "] does not match " + "the obtained file location: ["
        + met.getMetadata(FILE_LOCATION) + "]", expectedFileLocation, met
        .getMetadata(FILE_LOCATION));
  }

  public void testReplaceOrigMetFilePath() {
    Metadata met = null;

    try {
      met = extractor.extractMetadata(this.extractFile.getCanonicalPath());
    } catch (Exception e) {
      fail(e.getMessage());
    }

    assertNotNull(met);
  }



  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    String confFilename = "/copyandrewrite.test.conf";
    String sampleMetFilename = "/samplemet.xml";
    String extractFilename = "/testfile.txt";
    String origMetFilePath = "orig.met.file.path";
    
    // get all the needed files staged
    this.sampleMetFile = super.getTestDataFile(sampleMetFilename);
    this.extractFile = super.getTestDataFile(extractFilename);
    
    
    // this is a java properties file
    this.confFile = super.getTestDataFile(confFilename);
    
    // we need to compute and override orig.met.file.path
    Properties confProps = new Properties();
    confProps.load(new FileInputStream(confFile));
    confProps.setProperty(origMetFilePath, sampleMetFile.getAbsolutePath());
    confProps.store(new FileOutputStream(confFile), null);
    

    try {
      this.expectedFileLocation = "/new/loc/"
          + this.extractFile.getParentFile().getCanonicalPath();
    } catch (Exception ignore) {
    }
    
    
    CopyAndRewriteConfig config = new CopyAndRewriteConfig();
    try {
      config.load(new FileInputStream(this.confFile));
    } catch (Exception e) {
      fail(e.getMessage());
    }

    this.extractor = new CopyAndRewriteExtractor();
    this.extractor.setConfigFile(config);
    
  }



  /* (non-Javadoc)
   * @see org.apache.oodt.cas.metadata.MetadataTestCase#tearDown()
   */
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    if(this.confFile != null) this.confFile = null;
    if(this.sampleMetFile != null) this.sampleMetFile = null;
    if(this.extractFile != null) this.extractFile = null;
    this.expectedFileLocation = null;
  }
  
  
}
