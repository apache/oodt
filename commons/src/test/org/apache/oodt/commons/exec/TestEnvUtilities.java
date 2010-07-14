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

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
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

}
