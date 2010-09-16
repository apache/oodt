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

package org.apache.oodt.xmlps.mapping;

//APACHE imports
import org.apache.oodt.xmlps.mapping.funcs.MappingFunc;
import org.apache.oodt.xmlps.structs.CDEValue;

//JDK imports
import java.io.InputStream;

//Junit imports
import junit.framework.TestCase;

/**
 * Test suite for XMLPS xml map file reader.
 */
public class TestMappingReader extends TestCase {

    private static final String expectedName = "Test Query Handler";

    private static final String expectedId = "urn:oodt:xmlps:testps";

    private static final int expectedFields = 43;

    private static final String SPECIMEN_CONTACT_EMAIL_TEXT = "SPECIMEN_CONTACT-EMAIL_TEXT";

    private static final String SPECIMEN_COLLECTED_CODE = "SPECIMEN_COLLECTED_CODE";

    private static final String SPECIMEN_TISSUE_ORGAN_SITE_CODE = "SPECIMEN_TISSUE_ORGAN-SITE_CODE";

    private static final String CONTACT_PERSON_EMAIL = "Brendan.Phalan@med.nyu.edu";

    private static final String expectedDefaultTable = "baseline";

    private static final String expectedJoinFld = "patient_id";

    public TestMappingReader() {

    }

    public void testReadTables() {
        Mapping mapping = getMappingOrFail();
        assertNotNull(mapping);

        assertEquals(1, mapping.getNumTables());
        assertNull(mapping.getTableByName("baseline"));
        assertNotNull(mapping.getTableByName("specimen"));
        assertNotNull(mapping.getDefaultTable());
        assertEquals(expectedDefaultTable, mapping.getDefaultTable());
        assertEquals(expectedJoinFld, mapping.getTableByName("specimen")
                .getJoinFieldName());
    }

    public void testReadBasicInfo() {
        Mapping mapping = getMappingOrFail();

        assertNotNull(mapping);
        assertEquals(expectedName, mapping.getName());
        assertEquals(expectedId, mapping.getId());
    }

    public void testReadFields() {
        Mapping mapping = getMappingOrFail();
        assertNotNull(mapping);
        assertEquals(expectedFields, mapping.getNumFields());
        containsSpecimenContactEmailTextOrFail(mapping);
        containsSpecimenCollectedCodeOrFail(mapping);

    }

    public void testReadFuncs() {
        Mapping mapping = getMappingOrFail();
        assertNotNull(mapping);
        assertTrue(mapping.getNumFields() > 0);

        MappingField funcField = mapping
                .getFieldByName(SPECIMEN_TISSUE_ORGAN_SITE_CODE);
        assertNotNull(funcField);

        assertNotNull(funcField.getFuncs());
        assertEquals(funcField.getFuncs().size(), 1);

        MappingFunc func = funcField.getFuncs().get(0);
        CDEValue val = new CDEValue("test", "16");
        CDEValue result = func.translate(val);
        assertNotNull(result);
        assertEquals(result.getVal(), "1");
        val.setVal("235");
        result = func.translate(val);
        assertEquals(result.getVal(), "235");
    }

    private void containsSpecimenCollectedCodeOrFail(Mapping mapping) {

        MappingField fld = mapping.getFieldByName(SPECIMEN_COLLECTED_CODE);
        assertNotNull(fld);
        assertTrue(fld.getType().equals(FieldType.DYNAMIC));
        assertFalse(fld.getType().equals(FieldType.CONSTANT));
        assertEquals("specimen_collected", fld.getDbName());
        assertEquals("specimen", fld.getTableName());

    }

    private void containsSpecimenContactEmailTextOrFail(Mapping mapping) {
        MappingField fld = mapping.getFieldByName(SPECIMEN_CONTACT_EMAIL_TEXT);
        assertNotNull(fld);
        assertEquals(fld.getConstantValue(), CONTACT_PERSON_EMAIL);
        assertTrue(fld.getType().equals(FieldType.CONSTANT));
        assertFalse(fld.getType().equals(FieldType.DYNAMIC));
        assertTrue(fld.getScope().equals(FieldScope.RETURN));
        assertFalse(fld.getScope().equals(FieldScope.QUERY));

    }

    private Mapping getMappingOrFail() {
        Mapping mapping = null;

        InputStream configFileIs = TestMappingReader.class
                .getResourceAsStream("test-ps.xml");

        try {
            mapping = MappingReader.getMapping(configFileIs);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        return mapping;
    }

}
