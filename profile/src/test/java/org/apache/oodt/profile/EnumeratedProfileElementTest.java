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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.xml.sax.SAXException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import org.apache.oodt.commons.io.NullOutputStream;
import org.apache.oodt.commons.util.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Unit test the {@link EnumeratedProfileElement} class.
 *
 * @author Kelly
 */ 
public class EnumeratedProfileElementTest extends ProfileElementTestCase {
	/**
	 * Construct the test case for the {@link EnumeratedProfileElement} class.
	 */
	public EnumeratedProfileElementTest(String name) {
		super(name);
	}

	protected ProfileElement createProfileElement() {
		return new EnumeratedProfileElement(ProfileTest.TEST_PROFILE, "name", "id", "desc", "type", "unit",
			/*synonyms*/ new ArrayList(), /*obligation*/false, /*maxOccurrence*/1, "comment", VALUES);
	}

	public void testIt() {
		ProfileElement element = createProfileElement();
		List values = element.getValues();
		assertEquals(3, values.size());
		assertEquals("1", values.get(0));
		assertEquals("2", values.get(1));
		assertEquals("3", values.get(2));
	}

	public void testNulls() {
		try {
			EnumeratedProfileElement element = new EnumeratedProfileElement(createProfileElement().getProfile(),
				"test", "test", "test", "test", "test", Collections.EMPTY_LIST, /*obligation*/true, /*maxOccur*/1,
				"comment", Collections.singletonList(null));
			fail("Null values must not be allowed as values in enumerated elements.");
		} catch (IllegalArgumentException good) {}
	}

	protected void checkEnumFlag(String text) {
		assertEquals("T", text);
	}

	protected void checkValue(String text) {
		assertTrue(VALUES.contains(text));
	}

	protected void checkMaxValue(String text) {
		fail("Enumerated profile element shouldn't have a maximum value");
	}

	protected void checkMinValue(String text) {
		fail("Enumerated profile element shouldn't have a minimum value");
	}

	/**
	 * Test to see if spaces are preserved in XML generation and parsing.
	 *
	 * @throws SAXException if an error occurs.
	 */
	public void testSpacePreserving() throws SAXException {
		Profile p = new Profile();
		ProfileAttributes pa = new ProfileAttributes("1", "1", "profile", "active", "1", "1",
			/*children*/Collections.EMPTY_LIST, "1", /*revNotes*/Collections.EMPTY_LIST);
		p.setProfileAttributes(pa);
		ResourceAttributes ra = new ResourceAttributes(p, "id", "title", /*formats*/Collections.EMPTY_LIST, "description",
			/*creators*/Collections.EMPTY_LIST, /*subjects*/Collections.EMPTY_LIST, /*publishers*/Collections.EMPTY_LIST,
			/*contributors*/Collections.EMPTY_LIST, /*dates*/Collections.EMPTY_LIST, /*types*/Collections.EMPTY_LIST,
			/*sources*/Collections.EMPTY_LIST, /*languages*/Collections.EMPTY_LIST, /*relations*/Collections.EMPTY_LIST,
			/*coverages*/Collections.EMPTY_LIST, /*rights*/Collections.EMPTY_LIST, Collections.singletonList("context"),
			"granule", "grainy", Collections.singletonList("file:/dev/null"));
		p.setResourceAttributes(ra);
		EnumeratedProfileElement e = new EnumeratedProfileElement(p, "mode", "mode", "Mode", "string", "mode",
			/*synonyms*/Collections.EMPTY_LIST, /*obligation*/false, /*maxOccurrence*/1, "No comment",
			Collections.singletonList("The current\n mode setting\n  is set to indent\n\n   a\n\n"
				+ "    number of increasing\n     times."));
		p.getProfileElements().put("mode", e);

		Profile q = new Profile(p.toString());
		e = (EnumeratedProfileElement) q.getProfileElements().values().iterator().next();
		assertEquals("The current\n mode setting\n  is set to indent\n\n   a\n\n"
			+ "    number of increasing\n     times.", e.getValues().get(0));
	}

	public void testXMLSerialization() throws Exception {
		Profile p = new Profile();
		EnumeratedProfileElement e = new EnumeratedProfileElement(p);
		Document doc = XML.createDocument();
		Node root = e.toXML(doc);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();
		DOMSource s = new DOMSource(root);
		StreamResult r = new StreamResult(new NullOutputStream());
		t.transform(s, r);
	}

	/** Enumerated values for the test element. */
	private static final List VALUES = Arrays.asList(new String[]{"1", "2", "3"});
}
