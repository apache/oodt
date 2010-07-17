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
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

//JDK imports
import java.io.File;
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test Suite for the {@link ExternMetExtractor}
 * </p>.
 */
public class TestExternMetExtractor extends TestCase {

    private ExternMetExtractor extractor;

    private static final String FILENAME = "Filename";

    private static final String FILE_LOCATION = "FileLocation";

    private static final String PRODUCT_TYPE = "ProductType";

    private static final String configFilePath = "extern-config.xml";

    private static final String extractFilePath = "testfile.txt";

    private static final String expectedFilename = "testfile.txt";

    private static final String expectedFileLocation = ".";

    private static final String expectedProductType = "GenericFile";

    public void testExtractor() {
        try {
            extractor = new ExternMetExtractor();
        } catch (InstantiationException e) {
            fail(e.getMessage());
        }

        Metadata met = null;
        
        try {
            met = extractor.extractMetadata(new File(getClass().getResource(extractFilePath).getFile()),
                    new File(getClass().getResource(configFilePath).getFile()));
        } catch (MetExtractionException e) {
            fail(e.getMessage());
        }

        assertNotNull(met);
        assertTrue(new File(getClass().getResource("testfile.txt.met").getFile()).exists());
        assertTrue(met.containsKey(FILENAME));
        assertTrue(met.containsKey(FILE_LOCATION));
        assertTrue(met.containsKey(PRODUCT_TYPE));

        assertEquals(expectedFilename, met.getMetadata(FILENAME));
        assertEquals(expectedFileLocation, met.getMetadata(FILE_LOCATION));
        assertEquals(expectedProductType, met.getMetadata(PRODUCT_TYPE));

    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        File generatedMetFile = new File(getClass().getResource("testfile.txt.met").getFile());
        if (generatedMetFile.exists()) {
            generatedMetFile.delete();
        }
    }

}
