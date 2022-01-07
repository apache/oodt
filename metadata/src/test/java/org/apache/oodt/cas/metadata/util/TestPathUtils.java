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


package org.apache.oodt.cas.metadata.util;

//JDK imports
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;

//Junit imports
import junit.framework.TestCase;

/**
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Test Case Suite for the PathUtils class.
 * </p>
 * 
 */
public class TestPathUtils extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testReplaceEnvVariables() {
        this.singleValueTest();
        this.multiValueTest();
    }
    
    public void testDoDynamicReplacement() throws Exception {

        System.out.println("Timezone is :" + Calendar.getInstance().getTimeZone().getID());

        assertEquals("2008-01-20T16:30:28.000-000033", PathUtils
            .doDynamicReplacement("[UTC_TO_TAI(2008-01-20T16:29:55.000Z)]"));
        assertEquals("475000201.000",
            PathUtils.doDynamicReplacement(
                "[DATE_TO_SECS([UTC_TO_TAI(2008-01-20T16:29:55.000Z)], TAI_FORMAT, 1993-01-01)]"));
        assertEquals("2010-01-01", PathUtils.doDynamicReplacement("[DATE_ADD(2009-12-31, yyyy-MM-dd, 1, day)]"));
    }

    private void singleValueTest() {
        String key = "KEY";
        String pathString = "[" + key + "]";
        Metadata m = generateTestMetadata(key, 1);
        assertEquals(getCorrectOutput(m, key, true), PathUtils
                .replaceEnvVariables(pathString, m, true));
        assertEquals(getCorrectOutput(m, key, false), PathUtils
                .replaceEnvVariables(pathString, m, false));
        assertEquals(PathUtils.replaceEnvVariables(pathString, m), PathUtils
                .replaceEnvVariables(pathString, m, false));
        assertEquals(PathUtils.replaceEnvVariables(pathString, m), PathUtils
                .replaceEnvVariables(pathString, m, true));
        assertEquals(PathUtils.replaceEnvVariables(pathString, m, false),
                PathUtils.replaceEnvVariables(pathString, m, true));
    }

    private void multiValueTest() {
        String key = "KEY";
        String pathString = "[" + key + "]";
        Metadata m = generateTestMetadata(key, 5);
        assertEquals(getCorrectOutput(m, key, true), PathUtils
                .replaceEnvVariables(pathString, m, true));
        assertEquals(getCorrectOutput(m, key, false), PathUtils
                .replaceEnvVariables(pathString, m, false));
        assertEquals(PathUtils.replaceEnvVariables(pathString, m), PathUtils
                .replaceEnvVariables(pathString, m, false));
        assertFalse(PathUtils.replaceEnvVariables(pathString, m).equals(
                PathUtils.replaceEnvVariables(pathString, m, true)));
        assertFalse(PathUtils.replaceEnvVariables(pathString, m, false).equals(
                PathUtils.replaceEnvVariables(pathString, m, true)));
    }

    private static Metadata generateTestMetadata(String key,
            int numOfValueFields) {
        Metadata m = new Metadata();
        StringBuffer outputValue = new StringBuffer("");
        Vector valList = new Vector();
        for (int i = 0; i < numOfValueFields; i++)
            valList.add("val" + i);
        m.addMetadata(key, valList);
        return m;
    }

    private static String getCorrectOutput(Metadata m, String key,
            boolean expand) {
        List valList = m.getAllMetadata(key);
        String var = (String) valList.get(0);
        if (expand)
            for (int j = 1; j < valList.size(); j++)
                var += PathUtils.DELIMITER + (String) valList.get(j);
        return var;
    }
   
}
