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

//JDK imports
import java.io.File;
import java.net.URLEncoder;

//OODT imports
import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.metadata.MetExtractor; //for javadoc
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.MetadataTestCase;

/**
 * 
 * Test case for the {@link MetReaderExtractor} {@link MetExtractor}
 */
public class TestMetReader extends MetadataTestCase {

    private MetReaderExtractor extractor;

    private static final String expectedProductType = "GenericFile";

    private static final String expectedFilename = "testfile.txt";

    private static final String FILENAME = "Filename";

    private static final String FILE_LOCATION = "FileLocation";

    private static final String PRODUCT_TYPE = "ProductType";
    
    private String expectedFileLocation;
    
    private File extractFile;
    
    public TestMetReader(String name) {
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
        assertEquals(met.getMetadata(FILENAME), expectedFilename);
        assertTrue(met.containsKey(FILE_LOCATION));
        assertEquals(met.getMetadata(FILE_LOCATION), expectedFileLocation);
        assertTrue(met.containsKey(PRODUCT_TYPE));
        assertEquals(met.getMetadata(PRODUCT_TYPE), expectedProductType);

    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.metadata.MetadataTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
      super.setUp();
      String extractFilename = "/testfile2.txt";
      String sampleMetFilename = "/testfile2.txt.met";
      this.extractFile = super.getTestDataFile(extractFilename);
      this.expectedFileLocation = this.extractFile.getParent();
      
      // replace the FileLocation met field in the sample met file
      // with the actual file location of the extractFile
      File sampleMetFile = super.getTestDataFile(sampleMetFilename);
      String sampleMetFileContents = FileUtils.readFileToString(sampleMetFile);
      String extractFileLocKey = "[EXTRACT_FILE_LOC]";
      sampleMetFileContents = sampleMetFileContents.replace(extractFileLocKey, URLEncoder.encode(extractFile.getParent(), "UTF-8"));
      FileUtils.writeStringToFile(sampleMetFile, sampleMetFileContents, "UTF-8");

      this.extractor = new MetReaderExtractor();

    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.metadata.MetadataTestCase#tearDown()
     */
    @Override
    public void tearDown() throws Exception {
      super.tearDown();
    }

}
