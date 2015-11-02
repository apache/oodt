// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.
//
// Portions of this code come from the Apache XML Project's Xerces 1.0.3 XML Parser
// (specifically, the functions escape and getEntityRef).  Apache license applies:
//
// The Apache Software License, Version 1.1
//
// Copyright (c) 1999 The Apache Software Foundation.  All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this list of
//     conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice, this list
//    of conditions and the following disclaimer in the documentation and/or other materials
//    provided with the distribution.
//
// 3. The end-user documentation included with the redistribution, if any, must include
//    the following acknowledgment:
//
//    "This product includes software developed by the Apache Software Foundation (http://www.apache.org/)."
//
//    Alternately, this acknowledgment may appear in the software itself,
//    if and wherever such third-party acknowledgments normally appear.
//
// 4. The names "Xerces" and "Apache Software Foundation" must not be used to endorse or
//    promote products derived from this software without prior written permission. For
//    written permission, please contact apache@apache.org.
//
// 5. Products derived from this software may not be called "Apache", nor may "Apache"
//    appear in their name, without prior written permission of the Apache Software
//    Foundation.
//
// THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
// BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
// PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
// ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
// AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
// EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// $Id: XML.java,v 1.2 2005-05-01 22:49:55 cmattmann Exp $

package org.apache.oodt.commons.util;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/** XML services.
 *
 * This class provides several XML convenience services and encapsulates the underlying
 * XML implementation, allowing it to vary without impacting developers.
 *
 * @author Kelly
 */
public class XML {
  private static Logger LOG = Logger.getLogger(XML.class.getName());
	private static DocumentBuilder getStandardDocumentBuilder() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setCoalescing(false);
			factory.setExpandEntityReferences(false);
			factory.setIgnoringComments(false);
			factory.setIgnoringElementContentWhitespace(true);
			factory.setNamespaceAware(true);
			factory.setValidating(true);
			return factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new IllegalStateException("Unexpected ParserConfigurationException: " + ex.getMessage());
		}
	}

	/** Get the DOM implementation.
	 *
	 * @return The DOM implementation.
	 */
	public static DOMImplementation getDOMImplementation() {
		return getStandardDocumentBuilder().getDOMImplementation();
	}

	/** Create a DOM document.
	 *
	 * @return A new DOM document.
	 */
	public static Document createDocument() {
		return getStandardDocumentBuilder().newDocument();
	}

	/** Create a DOM parser.
	 *
	 * This method creates a new DOM parser that has validation turned on and
	 * ignorable whitespace not included, and has a default error handler that prints
	 * error messages and warnings to the standard error stream.
	 *
	 * @return A new DOM parser.
	 */
	public static DOMParser createDOMParser() {
		DocumentBuilder builder = getStandardDocumentBuilder();
		builder.setEntityResolver(ENTERPRISE_ENTITY_RESOLVER);
		builder.setErrorHandler(new ErrorHandler() {
			public void error(SAXParseException ex) {
				System.err.println("Parse error: " + ex.getMessage());
				ex.printStackTrace();
			}
			public void warning(SAXParseException ex) {
				System.err.println("Parse warning: " + ex.getMessage());
			}
			public void fatalError(SAXParseException ex) {
				System.err.println("Fatal parse error: " + ex.getMessage());
				ex.printStackTrace();
			}
		});
		return new DOMParser(builder);
	}

	/** Create a SAX parser.
	 *
	 * This method creates a new, default SAX parser.  It's set up with a default
	 * error handler that just prints messages to the standard error stream.
	 *
	 * @return A new SAX parser.
	 */
	public static SAXParser createSAXParser() {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(false);
			factory.setValidating(false);
			javax.xml.parsers.SAXParser saxParser = factory.newSAXParser();
			saxParser.getXMLReader().setEntityResolver(ENTERPRISE_ENTITY_RESOLVER);
			saxParser.getXMLReader().setErrorHandler(new ErrorHandler() {
				public void error(SAXParseException ex) {
					System.err.println("Parse error: " + ex.getMessage());
				}
				public void warning(SAXParseException ex) {
					System.err.println("Parse warning: " + ex.getMessage());
				}
				public void fatalError(SAXParseException ex) {
					System.err.println("Fatal parse error: " + ex.getMessage());
				}
			});
		  return new SAXParser(saxParser);
		} catch (ParserConfigurationException ex) {
			throw new IllegalStateException("Unexpected ParserConfigurationException: " + ex.getMessage());
		} catch (SAXException ex) {
			throw new IllegalStateException("Unexpected SAXException: " + ex.getMessage());
		}
	}

	/** Serialize an XML DOM document into a String.
	 *
	 * This method takes a DOM document and pretty-prints (or pretty-serializes, in
	 * XML parlance) it into a string.
	 *
	 * @param doc The document.
	 * @param omitXMLDeclaration True if we should omit the XML declaration, false to keep the XML declaration.
	 * @return The pretty-serialized, stringified, document.
	 */
	public static String serialize(Document doc, boolean omitXMLDeclaration) {
		StringWriter writer = new StringWriter();
	  serialize(doc, writer, omitXMLDeclaration);
	  return writer.getBuffer().toString();
	}

	/** Serialize an XML DOM document into a String.
	 *
	 * This method takes a DOM document and pretty-prints (or pretty-serializes, in
	 * XML parlance) it into a string.
	 *
	 * @param doc The document.
	 * @return The pretty-serialized, stringified, document.
	 */
	public static String serialize(Document doc) {
		return serialize(doc, /*omitXMLDeclaration*/false);
	}

	/** Serialize an XML DOM document into a writer.
	 *
	 * This method takes a DOM document and pretty-prints (or pretty-serializes, in
	 * XML parlance) it into a writer.
	 *
	 * @param doc The document.
	 * @param writer Where to write it.
	 * @param omitXMLDeclaration True if we should omit the XML declaration, false to keep the XML declaration.
	 * @throws IOException If an I/O error occurs.
	 */
	public static void serialize(Document doc, Writer writer, boolean omitXMLDeclaration) {
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			
			//update 05/01/2005
			//author: Chris Mattmann
			//set properties to pretty print
			//and also to include the DOCTYPE when serializing the XML
			
			//only set this if it's not null else we get a nice NullPointerException
			if(doc.getDoctype().getPublicId() != null){
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doc.getDoctype().getPublicId());			    
			}

			//only set this if it's not null else we get a nice NullPointerException
			if(doc.getDoctype().getSystemId() != null){
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doc.getDoctype().getSystemId());			    
			}

			transformer.setOutputProperty(OutputKeys.INDENT,"yes");
			
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);
		} catch (TransformerConfigurationException ex) {
			throw new IllegalStateException("Unexpected TransformerConfigurationException: " + ex.getMessage());
		} catch (TransformerException ex) {
			throw new IllegalStateException("Unexpected TransformerException: " + ex.getMessage());
		}
	}

	/** Serialize an XML DOM document into a writer.
	 *
	 * This method takes a DOM document and pretty-prints (or pretty-serializes, in
	 * XML parlance) it into a writer.
	 *
	 * @param doc The document.
	 * @param writer Where to write it.
	 * @throws IOException If an I/O error occurs.
	 */
	public static void serialize(Document doc, Writer writer) {
		serialize(doc, writer, /*omitXMLDeclaration*/false);
	}

	/** Parse the given XML document into a DOM tree.
	 *
	 * @param inputSource The XML document to parse.
	 * @return A DOM tree for the given XML document.
	 * @throws SAXException If a parse error occurs.
	 * @throws IOException If an I/O error occurs.
	 */
	public static Document parse(InputSource inputSource) throws SAXException, IOException {
		DOMParser parser = XML.createDOMParser();
		parser.parse(inputSource);
		return parser.getDocument();
	}

	/** Parse the given XML document into a DOM tree.
	 *
	 * @param string The XML document to parse.
	 * @return A DOM tree for the given XML document.
	 * @throws SAXException If a parse error occurs.
	 */
	public static Document parse(String string) throws SAXException {
		Document doc;
		try {
			DOMParser parser = XML.createDOMParser();
			StringReader reader = new StringReader(string);
			InputSource inputSource = new InputSource(reader);
			doc = parse(inputSource);
			reader.close();
		} catch (IOException cantHappen) {
			cantHappen.printStackTrace();
			throw new RuntimeException("I/O exception " + cantHappen.getClass().getName()
				+ " can NOT have happened, yet it did!  Message: " + cantHappen.getMessage());
		}
		return doc;
	}

	/** Parse the given XML document into a DOM tree.
	 *
	 * @param reader The XML document to parse.
	 * @return A DOM tree for the given XML document.
	 * @throws SAXException If a parse error occurs.
	 * @throws IOException If an I/O error occurs.
	 */
	public static Document parse(Reader reader) throws SAXException, IOException {
		return parse(new InputSource(reader));
	}

	/** Parse the given XML document into a DOM tree.
	 *
	 * @param inputStream The XML document to parse.
	 * @return A DOM tree for the given XML document.
	 * @throws SAXException If a parse error occurs.
	 * @throws IOException If an I/O error occurs.
	 */
	public static Document parse(InputStream inputStream) throws SAXException, IOException {
		return parse(new InputStreamReader(inputStream));
	}

	/** Add a repeating child element with text from the given collection to the given node.
	 *
	 * For example, if <var>values</var> is a {@link java.util.List} with strings items
	 * "a", "b", and "c", and <var>name</var> is "value", then the XML document will
	 * have
	 * <pre>&lt;value&gt;a&lt;/value&gt;
	 * &lt;value&gt;b&lt;/value&gt;
	 * &lt;value&gt;c&lt;/value&gt;</pre>
	 * appended to <var>node</var>.
	 *
	 * @param node Node to which to add child elements.
	 * @param name Name to give each child element.
	 * @param values Collection of values to assign to each child element, in iterator order.
	 * @throws DOMException If a DOM error occurs.
	 */
	public static void add(Node node, String name, Collection values) throws DOMException {
	  for (Object value : values) {
		add(node, name, value);
	  }
	}

	/** Add a child element with the given text to the given element.
	 *
	 * This method modifies your DOM tree so that
	 * <pre>&lt;node&gt;
	 *   ...
	 * &lt;/node&gt;</pre>
	 * becomes
	 * <pre>&lt;node&gt;
	 *   ...
	 *   &lt;name&gt;text&gt;/name&gt;
	 * &lt;/node&gt;</pre>
	 *
	 * Adding a null <var>name</var> does nothing.  Adding null <var>text</var>
	 * won't add the element.
	 *
	 * @param node Node to which to add a child element.
	 * @param name Name of the child element to add to <var>node</var>.
	 * @param text What text the text-node child the child element named <var>name</var> should have.  If null,
	 * nothing happens.
	 * @throws DOMException If a DOM error occurs.
	 */
	public static void addNonNull(Node node, String name, String text) throws DOMException {
		if (text == null) {
		  return;
		}
		add(node, name, text);
	}

	/** Add a child element with the given text to the given element.
	 *
	 * This method modifies your DOM tree so that
	 * <pre>&lt;node&gt;
	 *   ...
	 * &lt;/node&gt;</pre>
	 * becomes
	 * <pre>&lt;node&gt;
	 *   ...
	 *   &lt;name&gt;text&gt;/name&gt;
	 * &lt;/node&gt;</pre>
	 *
	 * Adding a null <var>name</var> does nothing.  Adding null <var>text</var>
	 * results in an empty <var>name</var> tag.
	 *
	 * @param node Node to which to add a child element.
	 * @param name Name of the child element to add to <var>node</var>.
	 * @param text What text the text-node child the child element named <var>name</var> should have.
	 * @throws DOMException If a DOM error occurs.
	 */
	public static void add(Node node, String name, String text) throws DOMException {
		if (name == null) {
		  return;
		}
		if (node == null) {
		  throw new IllegalArgumentException("Can't add to a null node");
		}
		Document doc = node.getOwnerDocument();
		Element element = doc.createElement(name);
		if (text != null) {
		  element.appendChild(doc.createTextNode(text));
		}
		node.appendChild(element);
	}

	/** Add a child element with the string representation of the given
	 * <var>object</var> to the given <var>node</var>.
	 *
	 * This method modifies your DOM tree so that
	 * <pre>&lt;node&gt;
	 *   ...
	 * &lt;/node&gt;</pre>
	 * becomes
	 * <pre>&lt;node&gt;
	 *   ...
	 *   &lt;name&gt;string-rep&gt;/name&gt;
	 * &lt;/node&gt;</pre>
	 *
	 * Adding a null <var>name</var> does nothing.  Adding null <var>object</var>
	 * results in an empty <var>name</var> tag.
	 *
	 * @param node Node to which to add a child element.
	 * @param name Name of the child element to add to <var>node</var>.
	 * @param object The string representation of the object to have as the text-node
	 * child the child element named <var>name</var>.
	 * @throws DOMException If a DOM error occurs.
	 */
	public static void add(Node node, String name, Object object) throws DOMException {
		add(node, name, object == null? null : object.toString());
	}

	/** Get unwrapped text from the given DOM node
	 *
	 * This method unwraps any wrapped text.  For example, if the document contains
	 * <pre>&lt;node&gt;Hello, world.  This is
	 *    my first document.
         * &lt;/node&gt;</pre>
	 * then the node's unwrapped text is
	 * <pre>Hello, world.  This is my first document.</pre>
	 * while the {@link #text} method would return the wrapped value
	 * <pre>Hello, world.  This is
	 *    my first document.</pre>
	 *
	 * <p>In other words, it collects the text nodes under the given node and replaces
	 * strings of newlines and spaces with a single space.  Unwrapping a null node
	 * returns a null string.
	 *
	 * @param node The node.
	 * @return The text in its children, unwrapped.
	 */
	public static String unwrappedText(Node node) {
		if (node == null) {
		  return null;
		}
		StringBuffer buffer = new StringBuffer();
		StringBuilder wrapped = new StringBuilder(text1(node, buffer));
		boolean newline = false;
		for (int i = 0; i < wrapped.length(); ++i) {
			if (!newline) {
				if (wrapped.charAt(i) == '\n') {
					newline = true;
					wrapped.setCharAt(i, ' ');
				}
			} else {
				if (Character.isWhitespace(wrapped.charAt(i))) {
					wrapped.deleteCharAt(i);
					--i;
				} else {
				  newline = false;
				}
			}
		}
		return wrapped.toString().trim();
	}

	/** Get the text from the given DOM node.
	 *
	 * Getting text from a null node gives you a null string.
	 *
	 * @param node The node.
	 * @return The text in its children.
	 */
	public static String text(Node node) {
		// [ return text(node) ]
		StringBuffer buffer = new StringBuffer();
		return text1(node, buffer);
	}

	/** Dump the structure of the DOM tree rooted at the given node to the given writer.
	 *
	 * This outputs the tree structure including the type of each node, its name, and
	 * its value.  Note that for many nodes, the name isn't useful (the name of text
	 * nodes, for example, is <code>#text</code>), and for many nodes, the value is
	 * null.
	 *
	 * @param writer The writer to which write the tree structure.
	 * @param node The tree to output.
	 */
	public static void dump(PrintWriter writer, Node node) {
		dump(writer, node, 0);
	}

	/** Remove all comments from the given document node's subtree.
	 *
	 * @param node Node from which to search for comments to nuke.
	 */
	public static void removeComments(Node node) {
		List commentNodes = new ArrayList();
		findCommentNodes(commentNodes, node);
	  for (Object commentNode1 : commentNodes) {
		Node commentNode = (Node) commentNode1;
		commentNode.getParentNode().removeChild(commentNode);
	  }
	}

	/** The resolver for entities for the JPL enterprise. */
	public static final EntityResolver ENTERPRISE_ENTITY_RESOLVER = new EnterpriseEntityResolver();

	/** An empty XML DOM document.  This is handy for some basic operations, and for
	 * fetching the DOM implementation.
	 */
	public static final Document EMPTY_DOCUMENT = org.apache.oodt.commons.util.XML.createDocument();

	/** Identifies the last printable character in the Unicode range that is supported
	 * by the encoding used with this serializer.  For 8-bit encodings this will be either
	 * 0x7E or 0xFF.  For 16-bit encodings this will be 0xFFFF. Characters that are not
	 * printable will be escaped using character references.
	 *
	 * <p>Taken from Xerces 1.0.3.  Apache license applies; see source code for
	 * license.
	 *
	 */
	private static int LAST_PRINTABLE = 0x7E;

	/** Escapes a string so it may be printed as text content or attribute value. Non
	 * printable characters are escaped using character references.  Where the format
	 * specifies a deault entity reference, that reference is used
	 * (e.g. <code>&amp;lt;</code>).
	 *
	 * <p>Taken from Xerces 1.0.3.  Apache license applies; see source code for
	 * license.
	 *
	 * @param source The string to escape
	 * @return The escaped string
	 */
	public static String escape(String source) {
		StringBuffer    result;
		int             i;
		char            ch;
		String          charRef;
        
		result = new StringBuffer(source.length());
		for (i = 0; i < source.length(); ++i) {
			ch = source.charAt(i);
			// If the character is not printable, print as character
			// reference.  Non printables are below ASCII space but not tab or
			// line terminator, ASCII delete, or above a certain Unicode
			// threshold.
			if ((ch < ' ' && ch != '\t' && ch != '\n' && ch != '\r') || ch > LAST_PRINTABLE || ch == 0xF7) {
			  result.append("&#").append(Integer.toString(ch)).append(';');
			} else {
				// If there is a suitable entity reference for this
				// character, print it. The list of available entity
				// references is almost but not identical between XML and
				// HTML.
				charRef = getEntityRef(ch);
				if (charRef == null) {
				  result.append(ch);
				} else {
				  result.append('&').append(charRef).append(';');
				}
			}
		}
		return result.toString();
	}

	/** Find all comment nodes under the given node and add them to the given list.
	 *
	 * @param list List to add to.
	 * @param node Node to search.
	 */
	private static void findCommentNodes(List list, Node node) {
		if (node.getNodeType() == Node.COMMENT_NODE) {
		  list.add(node);
		} else {
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); ++i) {
				findCommentNodes(list, children.item(i));
			}
		}
	}

	/** Get the entity reference for the given character.
	 *
	 * Taken from Xerces 1.0.3; see source code for license.
	 */
	private static String getEntityRef(char ch) {
		// Encode special XML characters into the equivalent character references.
		// These five are defined by default for all XML documents.
		switch (ch) {
			case '<':  return "lt";
			case '>':  return "gt";
			case '"':  return "quot";
			case '\'': return "apos";
			case '&':  return "amp";
		}
		return null;
	}

	/** Get the text from the child node using the given buffer.
	 *
	 * @param node The node.
	 * @param buffer The buffer to use.
	 * @return The text.
	 */
	private static String text1(Node node, StringBuffer buffer) {
		for (Node ch = node.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
			if (ch.getNodeType() == Node.ELEMENT_NODE || ch.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
			  buffer.append(text(ch));
			} else if (ch.getNodeType() == Node.TEXT_NODE) {
			  buffer.append(ch.getNodeValue());
			}
		}
		return buffer.toString();
	}

	/** Dump the structure of the DOM tree rooted at the given node to the given writer,
	 * indenting the contents.
	 *
	 * @param indentAmt The number of spaces to indent the output of this node;
	 * children are indented two more than this amount.
	 * @param writer The writer to which write the tree structure.
	 * @param node The tree to output.
	 */
	private static void dump(PrintWriter writer, Node node, int indentAmt) {
		for (int i = 0; i < indentAmt; ++i) {
		  writer.print(' ');
		}
		writer.println(typeOf(node) + "(" + node.getNodeName() + ", " + node.getNodeValue() + ")");
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
		  dump(writer, children.item(i), indentAmt + 2);
		}
	}

	/** Return a human-readable representation of the type of the given node.
	 *
	 * For example, an attribute node returns <code>Attribute</code>, while an element
	 * node returns <code>Element</code>.
	 *
	 * @param node The node.
	 * @return The name of the node's type.
	 */
	private static String typeOf(Node node) {
		switch (node.getNodeType()) {
			case Node.ATTRIBUTE_NODE:              return "Attribute";
			case Node.CDATA_SECTION_NODE:          return "CDATA-Section";
			case Node.COMMENT_NODE:                return "Comment";
			case Node.DOCUMENT_FRAGMENT_NODE:      return "Document-Fragment";
			case Node.DOCUMENT_NODE:               return "Document";
			case Node.DOCUMENT_TYPE_NODE:          return "Document-Type";
			case Node.ELEMENT_NODE:                return "Element";
			case Node.ENTITY_NODE:                 return "Entity";
			case Node.ENTITY_REFERENCE_NODE:       return "Entity-Ref";
			case Node.NOTATION_NODE:               return "Notation";
			case Node.PROCESSING_INSTRUCTION_NODE: return "Proc-Instr";
			case Node.TEXT_NODE:                   return "Text";
			default:                               return "Unknown!";
		}
	}
}
