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

package org.apache.oodt.xmlps.profile;

//APACHE imports
import org.apache.oodt.xmlps.util.XMLQueryHelper;
import org.apache.oodt.xmlquery.XMLQuery;

//Junit imports
import junit.framework.TestCase;

/**
 * Tests the XMLPS profile handler.
 */
public class TestXMLPSProfileHandler extends TestCase {

  private static XMLPSProfileHandler handler;

  private static final String unconstrainedQuery = "RETURN = STUDY_PARTICIPANT_ID";

  public TestXMLPSProfileHandler() {
    System.setProperty("org.apache.oodt.xmlps.profile.xml.mapFilePath",
        "./src/test/resources/test-ps.xml");

    try {
      handler = new XMLPSProfileHandler();
    } catch (InstantiationException e) {
      fail("Can't construct test suite: exception building test handler");
    }
  }

  public void testAllowUnConstrainedQuery() {
    XMLQuery query = XMLQueryHelper
        .getDefaultQueryFromQueryString(unconstrainedQuery);

    assertNotNull(query);
    assertNotNull(query.getWhereElementSet());
    assertTrue(query.getWhereElementSet().size() == 0);
    assertNotNull(query.getSelectElementSet());
    assertTrue(query.getSelectElementSet().size() == 1);

    try {
      handler.translateToDomain(query.getSelectElementSet(), true);
    } catch (Exception e) {
      fail(e.getMessage());
    }

    try {
      handler.queryAndPackageProfiles(query);
    } catch (Exception e) {
      fail(e.getMessage());
    }

  }

}
