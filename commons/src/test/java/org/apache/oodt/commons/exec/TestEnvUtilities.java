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

package org.apache.oodt.commons.exec;

//JDK imports
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

//Apache Commons
import org.apache.commons.lang.SystemUtils;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @author mstarch
 * @version $Revision$
 * 
 * <p>
 * Test case for {@link EnvUtilities}
 * </p>.
 */
public class TestEnvUtilities extends TestCase {

    private static final String envVarStr = "TOMCAT_HOME=/usr/local/tomcat\nPROMPT=\\u \\p\n";

    private static final String expectedVarStr = "TOMCAT_HOME=/usr/local/tomcat\nPROMPT=\\\\u \\\\p\n";

    /**
     * @since OODT-178
     * 
     */
    public void testPreProcessInputStream() {
        ByteArrayInputStream is = new ByteArrayInputStream(envVarStr.getBytes());
        InputStream translatedIs = null;
        try {
            translatedIs = EnvUtilities.preProcessInputStream(is);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(translatedIs);
        String translatedEnvStr = null;
        try {
            translatedEnvStr = EnvUtilities.slurp(translatedIs);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertNotNull(translatedEnvStr);
        assertEquals(translatedEnvStr, expectedVarStr);

    }
    /**
     * Tests two environment variables that should exist in any build 
     * environment. USER, HOME
     * By calling (EnvUtilities.getEnv(String))
     */
    public void testSetEnvironmentVar() {
        //Test if an only if HOME and USER is defined (Assumed to be true on unix)
        if (SystemUtils.IS_OS_UNIX) {
            //Makes the assumption that System.properties() is correct.
            String userHomeTruth = System.getProperty("user.home");
            String userNameTruth = System.getProperty("user.name");
            //Test values
            String userHomeTest = EnvUtilities.getEnv("HOME");
            String userNameTest = EnvUtilities.getEnv("USER");
            //Check all three tests
            assertEquals(userHomeTruth,userHomeTest);
            assertEquals(userNameTruth,userNameTest);
        } 
    }
    /**
     * Tests two environment variables that should exist in any build 
     * environment. USER, HOME
     * By getting the environment (EnvUtilities.getEnv()) and reading from this.
     */
    public void testGetEnvironment() {
        //Test if an only if HOME and USER is defined (Assumed to be true on unix)
        if (SystemUtils.IS_OS_UNIX) {
            //Makes the assumption that System.properties() is correct.
            String userHomeTruth = System.getProperty("user.home");
            String userNameTruth = System.getProperty("user.name");
            Properties env = EnvUtilities.getEnv();
            //Test values
            String userHomeTest = env.getProperty("HOME");
            String userNameTest = env.getProperty("USER");
            //Check all three tests
            assertEquals(userHomeTruth,userHomeTest);
            assertEquals(userNameTruth,userNameTest);
        } 
    }
    /**
     * Tests for consistency between the two methods for getting environment variables
     * in EnvUtilities calling getEnv(String) and calling getEnv().getProperty(String).
     */
    public void testGetEnvironmentConsistency() {
        //Test if an only if HOME and USER is defined (Assumed to be true on unix)
        if (SystemUtils.IS_OS_UNIX) {
            Properties env = EnvUtilities.getEnv();
            //Test values
            String userHomeTest1 = env.getProperty("HOME");
            String userNameTest1 = env.getProperty("USER");
            String userHomeTest2 = EnvUtilities.getEnv("HOME");
            String userNameTest2 = EnvUtilities.getEnv("USER");
            //Check all three tests
            assertEquals(userHomeTest1,userHomeTest2);
            assertEquals(userNameTest1,userNameTest2);
        }
    }
    
    public void testStaticEnvironment(){
      if(SystemUtils.IS_OS_UNIX){
        Properties env = EnvUtilities.getEnv();
        Properties env2 = EnvUtilities.getEnv();
        
        assertEquals(env, env2);
      }
    }
}
