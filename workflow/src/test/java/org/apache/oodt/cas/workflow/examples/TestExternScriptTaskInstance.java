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


package org.apache.oodt.cas.workflow.examples;

//OODT imports

import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

//JDK imports
//APACHE imports

/**
 * @author davoodi
 * @version $Revision$
 * @since OODT-226
 * 
 * <p>
 * Unit test for running an external script as a task instance.
 * </p>.
 */
public class TestExternScriptTaskInstance extends TestCase {

    private static Logger LOG = Logger.getLogger(TestExternScriptTaskInstance.class.getName());
    private static final String testScriptPath = new File(
            "./src/test/resources/myScript.sh").getAbsolutePath();

    private ExternScriptTaskInstance myIns;

    private Metadata myMet;

    /**
     * <p>
     * Default Constructor
     * </p>
     */
    public TestExternScriptTaskInstance() {
        myIns = new ExternScriptTaskInstance();
    }

    public void testExternsalScript() throws RepositoryException {

        myMet = new Metadata();
        myMet.addMetadata("Args", "Faranak");
        myMet.addMetadata("Args", "Davoodi");
        assertNotNull(myMet);
        WorkflowTaskConfiguration myConfig = new WorkflowTaskConfiguration();
        myConfig.addConfigProperty("PathToScript", testScriptPath);
        myConfig.addConfigProperty("ShellType", "/bin/bash");
        assertNotNull(myConfig);
        myIns.run(myMet, myConfig);
        String outputFileStr = null;
        try {
            outputFileStr = FileUtils.readFileToString(new File(
                    "./src/test/resources/myScript-Output.txt"), outputFileStr);
            String expectedStr = "Hi my first name is Faranak and my last name is Davoodi.";
            assertEquals(expectedStr.trim(), outputFileStr.trim());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LOG.log(Level.SEVERE, e.getMessage());
        }

    }
}
