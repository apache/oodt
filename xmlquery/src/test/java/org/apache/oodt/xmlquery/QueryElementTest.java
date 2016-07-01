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

import org.apache.oodt.commons.util.*;
import junit.framework.*;
import org.w3c.dom.*;

/** Unit test the {@link QueryElement} class.
 *
 * @author Kelly
 */ 
public class QueryElementTest extends TestCase {
	/** Construct the test case for the {@link QueryElement} class. */
	public QueryElementTest(String name) {
		super(name);
	}

	public void testNoArgsCtor() {
		QueryElement blank = new QueryElement();
		assertEquals("UNKNOWN", blank.getRole());
		assertEquals("UNKNOWN", blank.getValue());
	}

	public void testCtor() {
		QueryElement full = new QueryElement("role", "value");
		assertEquals("role", full.getRole());
		assertEquals("value", full.getValue());
	}		

	public void testSetters() {
		QueryElement q = new QueryElement("role", "value");

		assertEquals("role", q.getRole());
		q.setRole("newRole");
		assertEquals("newRole", q.getRole());
		q.setRole(null);
		assertEquals("UNKNOWN", q.getRole());

		assertEquals("value", q.getValue());
		q.setValue("newValue");
		assertEquals("newValue", q.getValue());
		q.setValue(null);
		assertEquals("UNKNOWN", q.getValue());
	}

	public void testObjectMethods() {
		QueryElement q1 = new QueryElement("a", "1");
		QueryElement q2 = new QueryElement("a", "1");
		QueryElement q3 = new QueryElement("b", "2");
		assertEquals(q1, q1);
		assertEquals(q1, q2);
		assertTrue(!q1.equals(q3));
		QueryElement q4 = (QueryElement) q3.clone();
		assertEquals(q3, q4);
		assertTrue(q3 != q4);
	}

	public void testXML() throws Exception {
		QueryElement q1 = new QueryElement("a", "1");
		Document doc = XML.createDocument();
		Node root = q1.toXML(doc);
		assertEquals("queryElement", root.getNodeName());
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if ("tokenRole".equals(child.getNodeName())) {
				assertEquals("a", XML.text(child));
			} else if ("tokenValue".equals(child.getNodeName())) {
				assertEquals("1", XML.text(child));
			} else fail("Unknown node \"" + child.getNodeName() + "\" in XML result");
		}
		QueryElement q2 = new QueryElement(root);
		assertEquals(q1, q2);
	}
}

