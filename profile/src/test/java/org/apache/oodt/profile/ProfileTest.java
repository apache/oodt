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


package org.apache.oodt.profile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;
import org.apache.oodt.commons.util.XML;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Unit test the Profile class.
 *
 * @author Kelly
 */ 
public class ProfileTest extends TestCase {
	/** Construct the test case for the Profile class. */
	public ProfileTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		oldProfNS = System.getProperty("jpl.rdf.ns");
		System.setProperty("jpl.rdf.ns", "http://enterprise.jpl.nasa.gov/rdfs/prof.rdf#");

		StringBuffer buffer = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		buffer.append("<!DOCTYPE profile PUBLIC \"").append(Profile.PROFILES_DTD_FPI).append("\" \"")
			.append(Profile.PROFILES_DTD_URL).append("\">\n");
		BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("test.xml")));
		String line;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
			buffer.append('\n');
		}
		reader.close();
		Document doc = XML.parse(buffer.toString());
		profile1 = new Profile(buffer.toString());
		profile2 = new Profile(doc.getDocumentElement());
	}

	protected void tearDown() throws Exception {
		if (oldProfNS != null)
			System.setProperty("jpl.rdf.ns", oldProfNS);
		else
			System.getProperties().remove("jpl.rdf.ns");
	}

	public void testQueries() {
		// We test both profile1 (built from a string) and profile2 (from an XML
		// document node); they should yield the same results because they
		// represent the same document.

		// Test the getResourceAttribute method.
		assertEquals("PDS_PROFILE_SERVER", profile1.getResourceAttributes().getIdentifier());
		assertEquals("PDS_PROFILE_SERVER", profile2.getResourceAttributes().getIdentifier());
		assertEquals("text/html", profile1.getResourceAttributes().getFormats().get(0));
		assertEquals("text/html", profile2.getResourceAttributes().getFormats().get(0));

		// Test the getProfileID method.
		assertEquals("OODT_PDS_PROFILE_SERVER", profile1.getProfileAttributes().getID());
		assertEquals("OODT_PDS_PROFILE_SERVER", profile2.getProfileAttributes().getID());

		// Test the getProfileAttribute method
		assertEquals("profile", profile1.getProfileAttributes().getType());
		assertEquals("profile", profile2.getProfileAttributes().getType());
		assertEquals("NULL", profile1.getProfileAttributes().getSecurityType());
		assertEquals("NULL", profile2.getProfileAttributes().getSecurityType());

		// Test the getProfileElementItem method.
		Map elements1 = profile1.getProfileElements();
		Map elements2 = profile2.getProfileElements();
		assertEquals(3, elements1.size());
		assertEquals(3, elements2.size());
		assertTrue(elements1.containsKey("TEST"));
		assertTrue(elements2.containsKey("TEST"));
		assertTrue(!elements1.containsKey("does-not-exist"));
		assertTrue(!elements2.containsKey("does-not-exist"));
		ProfileElement element1 = (ProfileElement) elements1.get("TEST2");
		ProfileElement element2 = (ProfileElement) elements2.get("TEST2");
		assertEquals("Testing", element1.getType());
		assertEquals("Testing", element2.getType());

		// Test the toString and getProfileString methods ... NB: this test should
		// actually check value, not just see if they're equal.
		assertEquals(profile1.toString(), profile2.toString());

		// Test some miscellaneous query methods
		assertEquals("Planetary Data System (PDS) - Profile Server V1.0", profile1.getResourceAttributes().getTitle());
		assertEquals("iiop://oodt.jpl.nasa.gov:10000/JPL.PDS.PROFILE", profile1.getResourceAttributes().getResLocations()
			.get(0));
		assertEquals("system.profileServer", profile1.getResourceAttributes().getResClass());
		assertEquals("Planetary Data System (PDS) - Profile Server V1.0", profile2.getResourceAttributes().getTitle());
		assertEquals("iiop://oodt.jpl.nasa.gov:10000/JPL.PDS.PROFILE", profile2.getResourceAttributes().getResLocations()
			.get(0));
		assertEquals("system.profileServer", profile2.getResourceAttributes().getResClass());
	}

	/** A test profile, built from a string. */
	private Profile profile1;

	/** Another test profile, built from an XML document node. */
	private Profile profile2;

	/** Previous value of the JPL RDF namespace in the system properties. */
	private String oldProfNS;

	/** Another (static) test profile, for use by other test cases. */
	static Profile TEST_PROFILE = new Profile(ProfileAttributesTest.TEST_PROFILE_ATTRIBUTES,
		ResourceAttributesTest.TEST_RESOURCE_ATTRIBUTES);
}
