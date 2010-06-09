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


package jpl.eda.profile.handlers.lightweight;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jpl.eda.util.SAXParser;
import jpl.eda.util.XML;
import jpl.eda.profile.ProfileElement;
import jpl.eda.profile.ProfileException;
import jpl.eda.xmlquery.XMLQuery;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Unit test the LightweightProfileServer class.
 *
 * @author Kelly
 */ 
public class LightweightProfileServerTest extends TestCase {
	/** Construct the test case for the LightweightProfileServer class. */
	public LightweightProfileServerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		server = new LightweightProfileServer(getClass().getResource("lightweightTest.xml"), "testing");
	}
	
	/**
	 * Test the getID method.
	 */
	public void testGetID() {
		assertEquals("testing", server.getID());
	}

	/**
	 * Test the searching methods.
	 */
	public void testSearching() {
		try {
			List profiles;

			// Try a positive search.
			profiles = doSearch("TEST > 2 AND TEST < 30");
			assertEquals(1, profiles.size());
			SearchableProfile profile = (SearchableProfile) profiles.get(0);
			assertEquals("PROFILE1", profile.getProfileAttributes().getID());
			Map elements = profile.getProfileElements();
			assertEquals(3, elements.size());
			ProfileElement element = (ProfileElement) elements.get("TEST");
			assertNotNull(element);
			assertEquals("3.14159", element.getID());

			// Now a negative one.
			profiles = doSearch("NONEXISTENT = 452712917824812123125100884");
			assertEquals(0, profiles.size());

			// Now one that has multiple elements from one profile
			profiles = doSearch("TEST <= 14 AND TEST2 >= 10");
			assertEquals(1, profiles.size());
			profile = (SearchableProfile) profiles.get(0);
			assertEquals("PROFILE1", profile.getProfileAttributes().getID());
			elements = profile.getProfileElements();
			assertEquals(3, elements.size());
			assertTrue(elements.containsKey("TEST"));
			assertTrue(elements.containsKey("TEST2"));

			// And again, but with OR instead of AND
			profiles = doSearch("TEST <= 14 OR TEST2 >= 10");
			assertEquals(1, profiles.size());
			profile = (SearchableProfile) profiles.get(0);
			assertEquals("PROFILE1", profile.getProfileAttributes().getID());
			elements = profile.getProfileElements();
			assertEquals(3, elements.size());
			assertTrue(elements.containsKey("TEST"));
			assertTrue(elements.containsKey("TEST2"));

			// And again, but with one of the elements being not found
			profiles = doSearch("NONEXISTENT = 123456789 OR TEST2 <= 1000000");
			assertEquals(1, profiles.size());
			profile = (SearchableProfile) profiles.get(0);
			assertEquals("PROFILE1", profile.getProfileAttributes().getID());
			elements = profile.getProfileElements();
			assertEquals(3, elements.size());
			assertTrue(elements.containsKey("TEST2"));

			// And again, but spanning profiles
			profiles = doSearch("TEST2 = 48 OR TEST4 = 192");
			assertEquals(2, profiles.size());
			for (Iterator i = profiles.iterator(); i.hasNext();) {
				profile = (SearchableProfile) i.next();
				elements = profile.getProfileElements();
				assertEquals(3, elements.size());
				if (profile.getProfileAttributes().getID().equals("PROFILE1")) {
					assertTrue(elements.containsKey("TEST2"));
				} else if (profile.getProfileAttributes().getID().equals("PROFILE2")) {
					assertTrue(elements.containsKey("TEST4"));
				} else fail("Profile \"" + profile.getProfileAttributes().getID() + "\" matched, but shouldn't");
			}

			// And again, but with a query on the "from" part.
			profiles = doSearch("( TEST2 = 48 OR TEST4 = 192 ) AND Creator = Alice");
			assertEquals(1, profiles.size());
			profile = (SearchableProfile) profiles.get(0);
			assertEquals("PROFILE1", profile.getProfileAttributes().getID());

			// And again, but on a nonenumerated element with no min/max values.
			profiles = doSearch("TEST5 = GEEBA");
			assertEquals(1, profiles.size());
			profile = (SearchableProfile) profiles.get(0);
			assertEquals("PROFILE2", profile.getProfileAttributes().getID());

			// And again, but with "RETURN =" parts.
			//fail("Not yet implemented");

		} catch (ProfileException ex) {
			fail("Profile server failed with excepton: " + ex.getMessage());
		}
	}

	/**
	 * Execute the given search.
	 *
	 * @param expr The search experssion.
	 * @return List of matching profiles.
	 * @throws ProfileException If the profile server fails.
	 */
	private List doSearch(String expr) throws ProfileException {
		XMLQuery query = new XMLQuery(expr, "test1", "LightweightProfileServerTest",
			"This query is to test the LightweightProfileServer", /*ddId*/null, /*resultModeId*/null,
			/*propType*/null, /*propLevels*/null, XMLQuery.DEFAULT_MAX_RESULTS);
		return server.findProfiles(query);
	}
		

	/** The lightweight profile server being tested. */
	private LightweightProfileServer server;
}
