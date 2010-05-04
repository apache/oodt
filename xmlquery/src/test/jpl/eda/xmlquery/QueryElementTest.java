// This software was developed by the the Jet Propulsion Laboratory, an operating division
// of the California Institute of Technology, for the National Aeronautics and Space
// Administration, an independent agency of the United States Government.
// 
// This software is copyrighted (c) 2000 by the California Institute of Technology.  All
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
// $Id: QueryElementTest.java,v 1.1.1.1 2004-03-02 19:37:17 kelly Exp $

package jpl.eda.xmlquery;

import java.io.*;
import java.util.*;
import jpl.eda.util.*;
import junit.framework.*;
import org.w3c.dom.*;
import org.xml.sax.*;

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

