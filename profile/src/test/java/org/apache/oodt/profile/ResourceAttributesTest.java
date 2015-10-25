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
 * Unit test the {@link ResourceAttributes} class.
 *
 * @author Kelly
 */ 
public class ResourceAttributesTest extends TestCase {
	/** Construct the test case for the {@link ResourceAttributes} class. */
	public ResourceAttributesTest(String name) {
		super(name);
	}

	public void testNoArgsCtor() {
		ResourceAttributes blank = new ResourceAttributes();
		assertEquals("UNKNOWN", blank.getIdentifier());
		assertEquals(0, blank.getFormats().size());
		assertEquals(0, blank.getCreators().size());
		assertEquals(0, blank.getSubjects().size());
		assertEquals(0, blank.getPublishers().size());
		assertEquals(0, blank.getContributors().size());
		assertEquals(0, blank.getDates().size());
		assertEquals(0, blank.getTypes().size());
		assertEquals(0, blank.getSources().size());
		assertEquals(0, blank.getLanguages().size());
		assertEquals(0, blank.getRelations().size());
		assertEquals(0, blank.getCoverages().size());
		assertEquals(0, blank.getRights().size());
		assertEquals(0, blank.getResContexts().size());
		assertEquals(0, blank.getResLocations().size());
	}

	public void testObjectMethods() {
		List contexts = new ArrayList();
		contexts.add("context");
		ResourceAttributes r1 = new ResourceAttributes(null, "1", "title", Collections.EMPTY_LIST, "desc",
			Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
			Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
			Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
			contexts, "aggregation", "class", Collections.EMPTY_LIST);
		ResourceAttributes r2 = new ResourceAttributes(null, "1", "title", Collections.EMPTY_LIST, "desc",
			Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
			Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
			Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
			contexts, "aggregation", "class", Collections.EMPTY_LIST);
		ResourceAttributes r3 = new ResourceAttributes(null, "2", "title2", Collections.EMPTY_LIST, "desc2",
			Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
			Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
			Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
			contexts, "aggregation2", "class2", Collections.EMPTY_LIST);
		assertEquals(r1, r1);
		assertEquals(r1, r2);
		assertTrue(!r1.equals(r3));
		ResourceAttributes r4 = (ResourceAttributes) r3.clone();
		assertEquals(r3, r4);
		assertTrue(r3 != r4);
	}

	public void testSetters() {
		ResourceAttributes q = (ResourceAttributes) TEST_RESOURCE_ATTRIBUTES.clone();

		assertEquals("identifier", q.getIdentifier());
		q.setIdentifier("newIdentifier");
		assertEquals("newIdentifier", q.getIdentifier());

		assertEquals("aggregation", q.getResAggregation());
		q.setResAggregation("newAggergation");
		assertEquals("newAggergation", q.getResAggregation());

		assertEquals("class", q.getResClass());
		q.setResClass("newClass");
		assertEquals("newClass", q.getResClass());
	}

	public void testXML() throws Exception {
		Document doc = XML.createDocument();
		Node root = TEST_RESOURCE_ATTRIBUTES.toXML(doc);
		assertEquals("resAttributes", root.getNodeName());
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			String name = child.getNodeName();
			if ("Identifier".equals(name)) {
				assertEquals("identifier", XML.text(child));
			} else if ("Title".equals(name)) {
				assertEquals("title", XML.text(child));
			} else if ("Format".equals(name)) {
			  // ignore
			} else if ("Description".equals(name)) {
				assertEquals("desc", XML.text(child));
			} else if ("Creator".equals(name)) {
			  // ignore
			} else if ("Subject".equals(name)) {
			  // ignore
			} else if ("Publisher".equals(name)) {
			  // ignore
			} else if ("Contributor".equals(name)) {
			  // ignore
			} else if ("Date".equals(name)) {
			  // ignore
			} else if ("Type".equals(name)) {
			  // ignore
			} else if ("Source".equals(name)) {
			  // ignore
			} else if ("Language".equals(name)) {
			  // ignore
			} else if ("Coverage".equals(name)) {
			  // ignore
			} else if ("Rights".equals(name)) {
			  // ignore
			} else if ("resContext".equals(name)) {
				assertEquals("context", XML.text(child));
			} else if ("resAggregation".equals(name)) {
				assertEquals("aggregation", XML.text(child));
			} else if ("resClass".equals(name)) {
				assertEquals("class", XML.text(child));
			} else if ("resLocation".equals(name)) {
			  // ignore
			} else fail("Unknown node \"" + name + "\" in XML result");
		}
		ResourceAttributes q = new ResourceAttributes(null, root);
		assertEquals(TEST_RESOURCE_ATTRIBUTES, q);
	}
	
	public void testXMLSerialization() throws Exception {
		Profile p = new Profile();
		ResourceAttributes ra = new ResourceAttributes(p);
		Document doc = XML.createDocument();
		Node root = ra.toXML(doc);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();
		DOMSource s = new DOMSource(root);
		StreamResult r = new StreamResult(new NullOutputStream());
		t.transform(s, r);
	}
	
	static ResourceAttributes TEST_RESOURCE_ATTRIBUTES; {
		List contexts = Collections.singletonList("context");
		List locations = Collections.singletonList("location");
		TEST_RESOURCE_ATTRIBUTES = new ResourceAttributes(null, "identifier", "title",
			Collections.EMPTY_LIST, "desc", Collections.EMPTY_LIST, Collections.EMPTY_LIST,
			Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
			Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
			Collections.EMPTY_LIST, contexts, "aggregation", "class", locations);
	}
}
