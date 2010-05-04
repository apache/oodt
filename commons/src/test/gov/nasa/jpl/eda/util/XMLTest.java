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
// $Id: XMLTest.java,v 1.4 2005-05-04 21:52:43 kelly Exp $

package jpl.eda.util;

import java.io.*;
import java.util.*;
import junit.framework.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/** Unit test the XML class.
 *
 * @author Kelly
 */ 
public class XMLTest extends TestCase {
	/** Construct the test case for the XML class. */
	public XMLTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("test.xml")));
		inputSource = new InputSource(reader);
	}

	protected void tearDown() throws Exception {
		reader.close();
	}

	/** Test the {@link XML#createDocument} method.
	 */
	public void testDocumentCreation() {
		Document doc = XML.createDocument();
		assertTrue(doc instanceof Document);
	}

	/** Test the {@link XML#createDOMParser} and {@link XML#serialize} methods.
	 */
	public void testDOM() throws Exception {
		DOMParser p = XML.createDOMParser();
		p.parse(inputSource);
		Document doc = p.getDocument();
		doc.normalize();
		assertEquals("log", doc.getDocumentElement().getTagName());
		java.util.zip.CRC32 crc = new java.util.zip.CRC32();
		String result = XML.serialize(doc);
		crc.update(result.getBytes());
		long value = crc.getValue();
		assertTrue("Stringified DOM document CRC mismatch, got value = " + value, 3880488030L == value || 2435419114L == value || /* added by Chris Mattmann: pretty print fix */3688328384L == value || /* other newline treatment */ 750262163L == value);
	}

	/** Test the {@link XML#createSAXParser} method.
	 */
	public void testSAXParser() throws Exception {
		SAXParser p = XML.createSAXParser();
		MyHandler handler = new MyHandler();
		p.setContentHandler(handler);
		p.parse(inputSource);
		// 25 refers to the 25 elements in the test.xml document.
		assertEquals(25, handler.getElementCount());
	}

	/** Test the {@link XML#dump(PrintWriter,Node)} method. */
	public void testDump() throws Exception {
		DOMParser p = XML.createDOMParser();
		p.parse(inputSource);
		Document doc = p.getDocument();
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		XML.dump(printWriter, doc);
		printWriter.close();
		java.util.zip.CRC32 crc = new java.util.zip.CRC32();
		crc.update(stringWriter.getBuffer().toString().getBytes());
		long value = crc.getValue();
		assertTrue("Dumped DOM tree CRC mismatch; got " + value, value == 828793L || value == 2241317601L);
	}

	/** Test the {@link XML#unwrappedText} method. */
	public void testUnwrappedText() throws Exception {
		DOMParser p = XML.createDOMParser();
		p.parse(inputSource);
		Document doc = p.getDocument();
		doc.normalize();

		Node node = doc.getDocumentElement().getFirstChild().getFirstChild().getNextSibling()
			.getNextSibling().getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling();
		assertEquals("Geeba, geeba.  Geeba geeba geeba!  Geeba, geeba, blooor? Bloorien bloreinda!",
			XML.unwrappedText(node));
		assertNull(XML.unwrappedText(null));
	}

	/** Test the {@link XML#add(Node,String,String)} method. */
	public void testAddString() throws Exception {
		Document doc = XML.createDocument();
		Element root = doc.createElement("root");
		doc.appendChild(root);
		XML.add(root, "child", "child text");
		assertEquals("child", root.getFirstChild().getNodeName());
		assertEquals("child text", root.getFirstChild().getFirstChild().getNodeValue());
		NodeList children = root.getChildNodes();
		assertEquals(1, children.getLength());
		try {
			XML.add(null, "child", "child text");
		} catch (IllegalArgumentException ex) {
			return;
		}
		fail("Adding to a null node should fail by throwing IllegalArgumentException");
	}

	/** Test the {@link XML#add(Node,String,Object)} method. */
	public void testAddObject() throws Exception {
		Object obj = new Object() {
			public String toString() {
				return "child text";
			}
		};
		Document doc = XML.createDocument();
		Element root = doc.createElement("root");
		doc.appendChild(root);
		XML.add(root, "child", obj);
		assertEquals("child", root.getFirstChild().getNodeName());
		assertEquals("child text", root.getFirstChild().getFirstChild().getNodeValue());
		NodeList children = root.getChildNodes();
		assertEquals(1, children.getLength());
		try {
			XML.add(null, "child", obj);
		} catch (IllegalArgumentException ex) {
			return;
		}
		fail("Adding to a null node should fail by throwing IllegalArgumentException");
	}
	
	/** Test the {@link XML#escape} method. */
	public void testEscape() {
		assertEquals("", XML.escape(""));
		assertEquals("So I said, &quot;She said, &apos;Both 3 &amp; 2 are &lt; 5 but &gt; 0&apos; but he said &#43981;",
			XML.escape("So I said, \"She said, 'Both 3 & 2 are < 5 but > 0' but he said \uabcd"));
	}


	/** Test the {@link XML#getDOMImplementation} method. */
	public void testGetDOMImplementation() {
		DOMImplementation impl = XML.getDOMImplementation();
		assertNotNull(impl);
		DOMImplementation impl2 = XML.getDOMImplementation();
		assertSame(impl, impl2);
	}

	/** Used by {@link #testSAXParser} */
	private static class MyHandler extends org.xml.sax.helpers.DefaultHandler {
		private int elementCount = 0;
		public void endElement(String uri, String localName, String rawName) {
			++elementCount;
		}
		public int getElementCount() {
			return elementCount;
		}
	}

	/** Reader for the test data file. */
	private BufferedReader reader;

	/** Input source for the {@link #reader}. */
	private InputSource inputSource;
}

