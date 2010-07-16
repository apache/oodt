/*
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

//JDK imports
import java.io.File;
import java.io.FileInputStream;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class TestCopyAndRewriteExtractor extends TestCase {

    private CopyAndRewriteExtractor extractor;

    private static final String FILENAME = "Filename";

    private static final String FILE_LOCATION = "FileLocation";

    private static final String PRODUCT_TYPE = "ProductType";

    private static final String confFilePath = "./src/test/gov/nasa/jpl/oodt/cas/metadata/extractors/copyandrewrite.test.conf";

    private static final String extractFilePath = "./src/test/gov/nasa/jpl/oodt/cas/metadata/extractors/testfile.txt";

    private static final String expectedFilename = "testfile.txt";

    private static final String expectedProductType = "NewProductTypeGenericFile";

    private static String expectedFileLocation = null;

    static {
        try {
            expectedFileLocation = "/new/loc/"
                    + new File(extractFilePath).getParentFile()
                            .getCanonicalPath();
        } catch (Exception ignore) {
        }
    }

    public TestCopyAndRewriteExtractor() {
        CopyAndRewriteConfig config = new CopyAndRewriteConfig();
        try {
            config.load(new FileInputStream(confFilePath));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        extractor = new CopyAndRewriteExtractor();
        extractor.setConfigFile(config);
    }

    public void testExtractMetadata() {
        Metadata met = null;

        try {
            met = extractor.extractMetadata(extractFilePath);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(met);
        assertNotNull(met.getHashtable());
        assertNotNull(met.getHashtable().keySet());
        assertEquals(3, met.getHashtable().keySet().size());
        assertTrue(met.containsKey(FILENAME));
        assertEquals(expectedFilename, met.getMetadata(FILENAME));
        assertTrue(met.containsKey(PRODUCT_TYPE));
        assertEquals(expectedProductType, met.getMetadata(PRODUCT_TYPE));
        assertTrue(met.containsKey(FILE_LOCATION));
        assertEquals("The expected file location: [" + expectedFileLocation
                + "] does not match " + "the obtained file location: ["
                + met.getMetadata(FILE_LOCATION) + "]", expectedFileLocation,
                met.getMetadata(FILE_LOCATION));
    }

    public void testReplaceOrigMetFilePath() {
        Metadata met = null;

        try {
            met = extractor.extractMetadata(extractFilePath);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(met);
    }
}
