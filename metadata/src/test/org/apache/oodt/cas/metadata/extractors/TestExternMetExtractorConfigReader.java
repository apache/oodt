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


package gov.nasa.jpl.oodt.cas.metadata.extractors;

//JDK imports
import java.io.File;
import java.io.FileNotFoundException;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Test suite for the {@link ExternConfigReader}
 * </p>.
 */
public class TestExternMetExtractorConfigReader extends TestCase {

    private static final String configFilePath = "./src/main/resources/examples/extern-config.xml";

    private static final String expectedBinPathEnding = "src/test/gov/nasa/jpl/oodt/cas/metadata/extractors/testExtractor";

    private static final String arg1 = ExternMetExtractorMetKeys.DATA_FILE_PLACE_HOLDER;

    private static final String arg2 = ExternMetExtractorMetKeys.MET_FILE_PLACE_HOLDER;
    
    private static final String arg3 = "-Dtrue=always";

    private static final String arg4 = "foo";

    private static final String arg5 = "bar";

    private static final String arg5ending = "/test\\ boo";

    public void testReadConfig() {
        ExternalMetExtractorConfig config = null;
        try {
            config = (ExternalMetExtractorConfig) new ExternConfigReader().parseConfigFile(new File(configFilePath));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(config);
        assertNotNull(config.getWorkingDirPath());
        assertEquals("", config.getWorkingDirPath());
        assertTrue(config.getExtractorBinPath().endsWith(expectedBinPathEnding));
        assertTrue(config.getExtractorBinPath().indexOf("[") == -1);
        assertTrue(config.getExtractorBinPath().indexOf("]") == -1);
        assertNotNull(config.getArgList());
        assertEquals(6, config.getArgList().length);
        assertEquals(arg1, config.getArgList()[0]);
        assertEquals(arg2, config.getArgList()[1]);
        assertEquals(arg3, config.getArgList()[2]);
        assertEquals(arg4, config.getArgList()[3]);
        assertEquals(arg5, config.getArgList()[4]);
        assertTrue(config.getArgList()[5].endsWith(arg5ending));

    }
}
