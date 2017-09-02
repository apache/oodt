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


package org.apache.oodt.xmlquery;

import java.util.*;
import org.apache.oodt.commons.util.*;
import junit.framework.*;
import org.w3c.dom.*;

/** Unit test the {@link Result} class.
 *
 * @author Kelly
 */ 
public class ResultTest extends TestCase {
	/** Construct the test case for the {@link Result} class. */
	public ResultTest(String name) {
		super(name);
	}

	public void testNoArgsCtor() {
		Result blank = new Result();
		assertEquals("UNKNOWN", blank.getID());
		assertEquals("UNKNOWN", blank.getMimeType());
		assertEquals("UNKNOWN", blank.getProfileID());
		assertEquals("UNKNOWN", blank.getResourceID());
		assertEquals(0, blank.getHeaders().size());
		assertEquals("", blank.getValue());
		assertTrue(!blank.isClassified());
		assertEquals(Result.INFINITE, blank.getValidity());
	}

	public void testSimpleCtor() {
		Result simple = new Result("1", "The Value");
		assertEquals("1", simple.getID());
		assertEquals("UNKNOWN", simple.getMimeType());
		assertEquals("UNKNOWN", simple.getProfileID());
		assertEquals("UNKNOWN", simple.getResourceID());
		assertEquals(0, simple.getHeaders().size());
		assertEquals("The Value", simple.getValue());
		assertTrue(!simple.isClassified());
		assertEquals(Result.INFINITE, simple.getValidity());
	}

	public void testFullCtor() {
		List headers = new ArrayList();
		headers.add(new Header("header"));
		Result full = new Result("1", "text/xml", "edaDataSetInv1", "geeba1", headers,TEST_VALUE, /*classified*/true,
			/*validity*/12345L);
		assertEquals("1", full.getID());
		assertEquals("text/xml", full.getMimeType());
		assertEquals("edaDataSetInv1", full.getProfileID());
		assertEquals("geeba1", full.getResourceID());
		assertEquals(1, full.getHeaders().size());
		assertEquals(TEST_VALUE, full.getValue());
		assertEquals(true, full.isClassified());
		assertEquals(12345L, full.getValidity());
	}		

	public void testSetters() {
		Result result = new Result("1", "text/xml", "edaDataSetInv1", "geeba1", new ArrayList(), TEST_VALUE);

		assertEquals("1", result.getID());
		result.setID("2");
		assertEquals("2", result.getID());

		assertEquals("text/xml", result.getMimeType());
		result.setMimeType("text/sgml");
		assertEquals("text/sgml", result.getMimeType());

		assertEquals("edaDataSetInv1", result.getProfileID());
		result.setProfileID("ptiDataSet");
		assertEquals("ptiDataSet", result.getProfileID());

		assertEquals("geeba1", result.getResourceID());
		result.setResourceID("fish2");
		assertEquals("fish2", result.getResourceID());

		assertEquals(TEST_VALUE, result.getValue());
		result.setValue("<hello>world</hello>");
		assertEquals("<hello>world</hello>", result.getValue());

		assertEquals(false, result.isClassified());
		result.setClassified(true);
		assertEquals(true, result.isClassified());

		assertEquals(Result.INFINITE, result.getValidity());
		result.setValidity(54321L);
		assertEquals(54321L, result.getValidity());
	}

	public void testObjectMethods() {
		Result r1 = new Result("1", "text/xml", "edaDataSetInv1", "geeba1", new ArrayList(), TEST_VALUE);
		Result r2 = new Result("1", "text/xml", "edaDataSetInv1", "geeba1", new ArrayList(), TEST_VALUE);
		Result r3 = new Result("2", "text/xml", "edaDataSetInv1", "geeba1", new ArrayList(), TEST_VALUE);
		assertEquals(r1, r1);
		assertEquals(r1, r2);
		assertTrue(!r1.equals(r3));
		Result r4 = (Result) r3.clone();
		assertEquals(r3, r4);
		assertTrue(r3 != r4);
	}

	public void testXML() throws Exception {
		Document doc = XML.createDocument();
		Element bogus = doc.createElement("bogus");
		try {
			Result r0 = new Result(bogus);
			fail("Result constructor failed to throw exception when given invalid XML node");
		} catch (IllegalArgumentException ignored) {}

		Result r1 = new Result("1", "text/xml", "edaDataSetInv1", "geeba1", new ArrayList(), TEST_VALUE,
			/*classified*/true, /*validity*/3456789);
		Node root = r1.toXML(doc);
		assertEquals("resultElement", root.getNodeName());
		assertEquals("true", ((Element) root).getAttribute("classified"));
		assertEquals("3456789", ((Element) root).getAttribute("validity"));
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if ("resultId".equals(child.getNodeName())) {
				assertEquals("1", XML.text(child));
			} else if ("resultMimeType".equals(child.getNodeName())) {
				assertEquals("text/xml", XML.text(child));
			} else if ("profId".equals(child.getNodeName())) {
				assertEquals("edaDataSetInv1", XML.text(child));
			} else if ("identifier".equals(child.getNodeName())) {
				assertEquals("geeba1", XML.text(child));
			} else if ("resultHeader".equals(child.getNodeName())) {
				// ignore, use HeaderTest
			} else if ("resultValue".equals(child.getNodeName())) {
				assertEquals(TEST_VALUE, child.getFirstChild().getNodeValue());
			} else fail("Unknown node \"" + child.getNodeName() + "\" in XML result");
		}
		Result r2 = new Result(root);
		assertEquals(r1, r2);
	}

	public void testMimeTypes() {
		try {
			Result r = new Result("1", "invalid/mime.type", "", "", new ArrayList(), "");
		} catch (IllegalArgumentException ex) {
			// Good.
			return;
		}
		fail("Result constructor failed to throw IllegalArgumentException for invalid mime type");
	}

	private static final String TEST_VALUE = "<?xml version='1.0' encoding='UTF-8'?>\n<test>value</test>";
}

