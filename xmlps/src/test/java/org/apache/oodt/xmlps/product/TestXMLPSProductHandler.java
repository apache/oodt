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

package org.apache.oodt.xmlps.product;

//JDK imports
import java.util.Iterator;
import java.util.List;

//APACHE imports
import org.apache.oodt.xmlps.util.XMLQueryHelper;
import org.apache.oodt.xmlquery.QueryElement;
import org.apache.oodt.xmlquery.XMLQuery;

//Junit imports
import junit.framework.TestCase;

/**
 * Tests the XMLPS product handler.
 */
public class TestXMLPSProductHandler extends TestCase {

    private static final String expectedSpecimenFldName = "specimen.specimen_collected";

    private static XMLPSProductHandler handler;

    private static final String specimenCollectedCodeField = "SPECIMEN_COLLECTED_CODE";

    private static final String studyProtocolIdField = "STUDY_PROTOCOL_ID";
    
    private static final String unconstrainedQuery = "RETURN = STUDY_PARTICIPANT_ID";

    private static final String queryStr = specimenCollectedCodeField
            + " = 3 AND " + studyProtocolIdField + " = 71 AND RETURN = "
            + specimenCollectedCodeField;

    public TestXMLPSProductHandler() {
        System.setProperty("org.apache.oodt.xmlps.xml.mapFilePath",
                "./src/test/resources/test-ps.xml");

        try {
            handler = new XMLPSProductHandler();
        } catch (InstantiationException e) {
            fail("Can't construct test suite: exception building test handler");
        }
    }
    
    public void testAllowUnConstrainedQuery(){
        XMLQuery query = XMLQueryHelper
             .getDefaultQueryFromQueryString(unconstrainedQuery);
        
        
        assertNotNull(query);
        assertNotNull(query.getWhereElementSet());
        assertTrue(query.getWhereElementSet().size() == 0);
        assertNotNull(query.getSelectElementSet());
        assertTrue(query.getSelectElementSet().size() == 1);
        
        try{
            handler.translateToDomain(query.getSelectElementSet(), true);
        }
        catch(Exception e){
            fail(e.getMessage());
        }
        
        try{
            handler.queryAndPackageResults(query);
        }
        catch(Exception e){
            fail(e.getMessage());
        }
        
    }

    public void testDomainTranslationWhereSet() {
        XMLQuery query = XMLQueryHelper
                .getDefaultQueryFromQueryString(queryStr);

        assertNotNull(query);
        assertNotNull(query.getWhereElementSet());
        assertEquals(7, query.getWhereElementSet().size());

        try {
            handler.translateToDomain(query.getWhereElementSet(), false);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        List<QueryElement> elemNames = handler
                .getElemNamesFromQueryElemSet(query.getWhereElementSet());
        assertNotNull(elemNames);
        assertEquals(1, elemNames.size()); // only 1 b/c one field is constant

        boolean gotSpecCollected = false;
        for (Iterator<QueryElement> i = elemNames.iterator(); i.hasNext();) {
            QueryElement elem = i.next();
            if (elem.getValue().equals("specimen.specimen_collected")) {
                gotSpecCollected = true;
            }

        }

        assertTrue(gotSpecCollected);

    }

    public void testDomainTranslationSelectSet() {

        XMLQuery query = XMLQueryHelper
                .getDefaultQueryFromQueryString(queryStr);

        assertNotNull(query);
        assertNotNull(query.getSelectElementSet());
        assertEquals(1, query.getSelectElementSet().size());
        try {
            handler.translateToDomain(query.getSelectElementSet(), true);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertNotNull(query.getSelectElementSet());
        assertEquals(1, query.getSelectElementSet().size());
        assertNotNull(query.getSelectElementSet().get(0));
        QueryElement elem = (QueryElement) query.getSelectElementSet().get(0);
        assertNotNull(elem.getValue());
        assertEquals("Expected: [" + expectedSpecimenFldName + "]: got: ["
                + elem.getValue() + "]", elem.getValue(),
                expectedSpecimenFldName);

    }
}
