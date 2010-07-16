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
import java.util.Collections;
import java.util.List;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import org.apache.oodt.commons.io.NullOutputStream;
import org.apache.oodt.commons.util.XML;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Unit test the {@link ProfileAttributes} class.
 *
 * @author Kelly
 */ 
public class ProfileAttributesTest extends TestCase {
	/** Construct the test case for the {@link ProfileAttributes} class. */
	public ProfileAttributesTest(String name) {
		super(name);
	}

	public void testNoArgsCtor() {
		ProfileAttributes blank = new ProfileAttributes();
		assertEquals("UNKNOWN", blank.getID());
		assertEquals(0, blank.getChildren().size());
	}

	public void testCtor() {
		assertEquals("id", TEST_PROFILE_ATTRIBUTES.getID());
		assertEquals("version", TEST_PROFILE_ATTRIBUTES.getVersion());
		assertEquals("type", TEST_PROFILE_ATTRIBUTES.getType());
		assertEquals("statusID", TEST_PROFILE_ATTRIBUTES.getStatusID());
		assertEquals("securityType", TEST_PROFILE_ATTRIBUTES.getSecurityType());
		assertEquals("parent", TEST_PROFILE_ATTRIBUTES.getParent());
		assertEquals(2, TEST_PROFILE_ATTRIBUTES.getChildren().size());
		assertEquals("child1", TEST_PROFILE_ATTRIBUTES.getChildren().get(0));
		assertEquals("child2", TEST_PROFILE_ATTRIBUTES.getChildren().get(1));
		assertEquals("regAuthority", TEST_PROFILE_ATTRIBUTES.getRegAuthority());
		assertEquals(2, TEST_PROFILE_ATTRIBUTES.getRevisionNotes().size());
		assertEquals("note1", TEST_PROFILE_ATTRIBUTES.getRevisionNotes().get(0));
		assertEquals("note2", TEST_PROFILE_ATTRIBUTES.getRevisionNotes().get(1));
	}		

	public void testObjectMethods() {
		ProfileAttributes q1 = new ProfileAttributes("1", "2", "3", "4", "5", "6", Collections.EMPTY_LIST, "7",
			Collections.EMPTY_LIST);
		ProfileAttributes q2 = new ProfileAttributes("1", "2", "3", "4", "5", "6", Collections.EMPTY_LIST, "7",
			Collections.EMPTY_LIST);
		ProfileAttributes q3 = new ProfileAttributes("2", "3", "4", "5", "6", "7", Collections.EMPTY_LIST, "8",
			Collections.EMPTY_LIST);
		assertEquals(q1, q1);
		assertEquals(q1, q2);
		assertTrue(!q1.equals(q3));
		ProfileAttributes q4 = (ProfileAttributes) q3.clone();
		assertEquals(q3, q4);
		assertTrue(q3 != q4);
	}

	public void testSetters() {
		ProfileAttributes q = (ProfileAttributes) TEST_PROFILE_ATTRIBUTES.clone();

		assertEquals("id", q.getID());
		q.setID("newId");
		assertEquals("newId", q.getID());

		assertEquals("version", q.getVersion());
		q.setVersion("newVersion");
		assertEquals("newVersion", q.getVersion());

		assertEquals("type", q.getType());
		q.setType("newType");
		assertEquals("newType", q.getType());

		assertEquals("statusID", q.getStatusID());
		q.setStatusID("newStatusid");
		assertEquals("newStatusid", q.getStatusID());

		assertEquals("securityType", q.getSecurityType());
		q.setSecurityType("newSecuritytype");
		assertEquals("newSecuritytype", q.getSecurityType());

		assertEquals("regAuthority", q.getRegAuthority());
		q.setRegAuthority("newRegAuthority");
		assertEquals("newRegAuthority", q.getRegAuthority());
	}

	public void testXML() throws Exception {
		Document doc = XML.createDocument();
		Node root = TEST_PROFILE_ATTRIBUTES.toXML(doc);
		assertEquals("profAttributes", root.getNodeName());
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			String name = child.getNodeName();
			if ("profId".equals(name)) {
				assertEquals("id", XML.text(child));
			} else if ("profVersion".equals(name)) {
				assertEquals("version", XML.text(child));
			} else if ("profType".equals(name)) {
				assertEquals("type", XML.text(child));
			} else if ("profStatusId".equals(name)) {
				assertEquals("statusID", XML.text(child));
			} else if ("profSecurityType".equals(name)) {
				assertEquals("securityType", XML.text(child));
			} else if ("profParentId".equals(name)) {
				assertEquals("parent", XML.text(child));
			} else if ("profChildId".equals(name)) {
				; // ignore, list serialization tested in XMLTest
			} else if ("profRegAuthority".equals(name)) {
				assertEquals("regAuthority", XML.text(child));
			} else if ("profRevisionNote".equals(name)) {
				; // ignore, list serialization tested in XMLTest
			} else fail("Unknown node \"" + name + "\" in XML result");
		}
		ProfileAttributes p = new ProfileAttributes(root);
		assertEquals(TEST_PROFILE_ATTRIBUTES, p);
	}
	
	public void testXMLSerialization() throws Exception {
		ProfileAttributes p = new ProfileAttributes();
		Document doc = XML.createDocument();
		Node root = p.toXML(doc);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();
		DOMSource s = new DOMSource(root);
		StreamResult r = new StreamResult(new NullOutputStream());
		t.transform(s, r);
	}

	static final ProfileAttributes TEST_PROFILE_ATTRIBUTES;

	static {
		List parents = Collections.singletonList("parent");
		List children = new ArrayList();
		children.add("child1");
		children.add("child2");
		List revisionNotes = new ArrayList();
		revisionNotes.add("note1");
		revisionNotes.add("note2");
		TEST_PROFILE_ATTRIBUTES = new ProfileAttributes("id", "version", "type", "statusID",
			"securityType", "parent", children, "regAuthority", revisionNotes);
	}
}
