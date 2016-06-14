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
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import org.apache.oodt.commons.io.NullOutputStream;
import org.apache.oodt.commons.util.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Unit test the {@link RangedProfileElement} class.
 *
 * @author Kelly
 */ 
public class RangedProfileElementTest extends ProfileElementTestCase {
	/** Construct the test case for the {@link RangedProfileElement} class. */
	public RangedProfileElementTest(String name) {
		super(name);
	}

	protected ProfileElement createProfileElement() {
		return new RangedProfileElement(ProfileTest.TEST_PROFILE, "name", "id", "desc", "type", "unit",
			/*synonyms*/ new ArrayList(), /*obligation*/false, /*maxOccurrence*/1, "comment", /*min*/"-100.0",
			/*max*/"100.0");
	}

	public void testIt() {
		ProfileElement element = createProfileElement();
		assertEquals("100.0", element.getMaxValue());
		assertEquals("-100.0", element.getMinValue());
		assertEquals(0, element.getValues().size());
	}
	
	public void testXMLSerialization() throws Exception {
		Profile p = new Profile();
		RangedProfileElement e = new RangedProfileElement(p);
		Document doc = XML.createDocument();
		Node root = e.toXML(doc);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();
		DOMSource s = new DOMSource(root);
		StreamResult r = new StreamResult(new NullOutputStream());
		t.transform(s, r);
	}

	protected void checkEnumFlag(String text) {
		assertEquals("F", text);
	}

	protected void checkValue(String text) {
		fail("Ranged profile element shouldn't have an enumerated value");
	}

	protected void checkMaxValue(String text) {
		assertEquals("100.0", text);
	}

	protected void checkMinValue(String text) {
		assertEquals("-100.0", text);
	}
}
