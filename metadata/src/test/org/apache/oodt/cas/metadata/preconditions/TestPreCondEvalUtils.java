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


package org.apache.oodt.cas.metadata.preconditions;

// Metadata Imports
import org.apache.oodt.cas.metadata.MetadataTestCase;

//JDK imports
import java.io.File;
import java.util.LinkedList;

//Spring imports
import org.junit.Before;
import org.springframework.context.support.FileSystemXmlApplicationContext;

//Junit imports


/**
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test suite for the {@link PreCondEvalUtils} class
 * </p>.
 */
public class TestPreCondEvalUtils extends MetadataTestCase {

    LinkedList<String> preconditions;
    
    private PreCondEvalUtils evalUtils;

    public TestPreCondEvalUtils(String name) {
        super(name);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.preconditions = new LinkedList<String>();
        this.preconditions.add("CheckThatDataFileSizeIsGreaterThanZero");
        this.preconditions.add("CheckThatDataFileExists");
        this.preconditions.add("CheckDataFileMimeType");
        File preCondFile = getTestDataFile("/met_extr_preconditions.xml");
        this.evalUtils = new PreCondEvalUtils(new FileSystemXmlApplicationContext("file:" + preCondFile.getAbsolutePath()));
    }

    public void testEval() throws Throwable {
        // Test file is also the config file we used, neat!
        File prodFile = getTestDataFile("/met_extr_preconditions.xml");
        assertTrue(this.evalUtils.eval(this.preconditions, prodFile));
    }
}
