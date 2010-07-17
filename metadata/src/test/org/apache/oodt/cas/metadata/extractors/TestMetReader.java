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

//JDK imports
import java.io.File;

//OODT imports
import org.apache.oodt.cas.metadata.MetExtractor; //for javadoc
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test case for the {@link MetReaderExtractor} {@link MetExtractor}
 * </p>.
 */
public class TestMetReader extends TestCase {

    private MetReaderExtractor extractor;

    private static final String expectedProductType = "GenericFile";

    private static final String expectedFilename = "testfile.txt";

    private static final String expectedFileLocation = ".";

    private static final String testFile = "testfile2.txt";

    private static final String FILENAME = "Filename";

    private static final String FILE_LOCATION = "FileLocation";

    private static final String PRODUCT_TYPE = "ProductType";

    public TestMetReader() {
        extractor = new MetReaderExtractor();
    }

    public void testExtractMetadata() {
        Metadata met = null;

        try {
            met = extractor.extractMetadata(getClass().getResource(testFile).getFile());
        } catch (MetExtractionException e) {
            fail(e.getMessage());
        }

        assertNotNull(met);
        assertNotNull(met.getHashtable());
        assertNotNull(met.getHashtable().keySet());
        assertEquals(3, met.getHashtable().keySet().size());
        assertTrue(met.containsKey(FILENAME));
        assertEquals(met.getMetadata(FILENAME), expectedFilename);
        assertTrue(met.containsKey(FILE_LOCATION));
        assertEquals(met.getMetadata(FILE_LOCATION), expectedFileLocation);
        assertTrue(met.containsKey(PRODUCT_TYPE));
        assertEquals(met.getMetadata(PRODUCT_TYPE), expectedProductType);

    }

}
