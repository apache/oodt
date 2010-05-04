// This software was developed by the the Jet Propulsion Laboratory, an operating division
// of the California Institute of Technology, for the National Aeronautics and Space
// Administration, an independent agency of the United States Government.
// 
// This software is copyrighted (c) 2001 by the California Institute of Technology.  All
// rights reserved.
// 
// Redistribution and use in source and binary forms, with or without modification, is not
// permitted under any circumstance without prior written permission from the California
// Institute of Technology.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
// THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// $Id: HeaderTest.java,v 1.1.1.1 2004-03-02 19:37:17 kelly Exp $

package jpl.eda.xmlquery;

import java.io.*;
import java.util.*;
import jpl.eda.util.*;
import junit.framework.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/** Unit test the {@link Header} class.
 *
 * @author Kelly
 */ 
public class HeaderTest extends TestCase {
	/** Construct the test case for the {@link Header} class. */
	public HeaderTest(String name) {
		super(name);
	}

	public void testNoArgsCtor() {
		Header blank = new Header();
		assertEquals("UNKNOWN", blank.getName());
		assertNull(blank.getType());
		assertNull(blank.getUnit());
	}

	public void testSimpleCtor() {
		Header simple = new Header("name");
		assertEquals("name", simple.getName());
		assertNull(simple.getType());
		assertNull(simple.getUnit());
	}

	public void testFullCtor() {
		Header full = new Header("name", "type", "unit");
		assertEquals("name", full.getName());
		assertEquals("type", full.getType());
		assertEquals("unit", full.getUnit());
	}		

	public void testSetters() {
		Header h = new Header("name", "type", "unit");

		assertEquals("name", h.getName());
		h.setName("newName");
		assertEquals("newName", h.getName());

		assertEquals("type", h.getType());
		h.setType("newType");
		assertEquals("newType", h.getType());

		assertEquals("unit", h.getUnit());
		h.setUnit("newUnit");
		assertEquals("newUnit", h.getUnit());
	}

	public void testObjectMethods() {
		Header h1 = new Header("name1", "type1", "unit1");
		Header h2 = new Header("name1", "type1", "unit1");
		Header h3 = new Header("name2", "type2", "unit2");
		assertEquals(h1, h1);
		assertEquals(h1, h2);
		assertTrue(!h1.equals(h3));
		Header h4 = (Header) h3.clone();
		assertEquals(h3, h4);
		assertTrue(h3 != h4);
	}

	public void testXML() throws Exception {
		Document doc = XML.createDocument();
		Element bogus = doc.createElement("bogus");
		try {
			Header h0 = new Header(bogus);
			fail("Header constructor failed to throw exception when given invalid XML node");
		} catch (IllegalArgumentException good) {}

		Header h1 = new Header("name1", "type1", "unit1");
		Node root = h1.toXML(doc);
		assertEquals("headerElement", root.getNodeName());
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if ("elemName".equals(child.getNodeName())) {
				assertEquals("name1", XML.text(child));
			} else if ("elemType".equals(child.getNodeName())) {
				assertEquals("type1", XML.text(child));
			} else if ("elemUnit".equals(child.getNodeName())) {
				assertEquals("unit1", XML.text(child));
			} else fail("Unknown node \"" + child.getNodeName() + "\" in XML result");
		}
		Header h2 = new Header(root);
		assertEquals(h1, h2);
	}

	public void testMultipleHeaders() throws Exception {
		Document doc = XML.createDocument();
		Element resultHeader = doc.createElement("resultHeader");
		Element headerElement = doc.createElement("headerElement");
		resultHeader.appendChild(headerElement);
		XML.add(headerElement, "elemName", "name1");
		headerElement = doc.createElement("headerElement");
		resultHeader.appendChild(headerElement);
		XML.add(headerElement, "elemName", "name2");
		XML.add(headerElement, "elemType", "type2");
		headerElement = doc.createElement("headerElement");
		resultHeader.appendChild(headerElement);
		XML.add(headerElement, "elemName", "name3");
		XML.add(headerElement, "elemUnit", "unit3");
		headerElement = doc.createElement("headerElement");
		resultHeader.appendChild(headerElement);
		XML.add(headerElement, "elemName", "name4");
		XML.add(headerElement, "elemType", "type4");
		XML.add(headerElement, "elemUnit", "unit4");

		List headers = Header.createHeaders(resultHeader);
		Header h1 = new Header("name1");
		Header h2 = new Header("name2", "type2", null);
		Header h3 = new Header("name3", null, "unit3");
		Header h4 = new Header("name4", "type4", "unit4");
		assertEquals(h1, headers.get(0));
		assertEquals(h2, headers.get(1));
		assertEquals(h3, headers.get(2));
		assertEquals(h4, headers.get(3));
	}
}

