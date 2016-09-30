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

package org.apache.oodt.commons.util;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

/** XML-RPC utilities.
 *
 * This class provides several XML-RPC utilities.
 *
 * @author Kelly
 * @deprecated soon be replaced by avro-rpc
 */
@Deprecated
public class XMLRPC {
	/** Build an XML-RPC method call.
	 *
	 * This may throw {@link IllegalArgumentException} if the <var>params</var>
	 * contains an object that's incompatible with XML-RPC.
	 *
	 * @param name Name of the method to call.
	 * @param params Parameters to pass to the call.
	 * @return An XML string encapsulationg the call.
	 * @throws DOMException If we can't construct the call.
	 */
	public static byte[] createMethodCall(String name, Collection params) throws DOMException {
		Document doc = XML.createDocument();
		Element methodCallElement = doc.createElement("methodCall");
		doc.appendChild(methodCallElement);
		XML.add(methodCallElement, "methodName", name);
		if (params != null && !params.isEmpty()) {
			Element paramsElement = doc.createElement("params");
			methodCallElement.appendChild(paramsElement);
		  for (Object param : params) {
			paramsElement.appendChild(createValueElement(doc, param));
		  }
		}
		return XML.serialize(doc).getBytes();
	}

	/** Create a &lt;value&gt; element for an XML-RPC method.
	 *
	 * This may throw {@link IllegalArgumentException} if the <var>value</var> is
	 * incompatible with XML-RPC.
	 *
	 * @param doc Owning document.
	 * @param value The value.
	 * @throws DOMException If we can't construct the &lt;value&gt;.
	 */
	private static Element createValueElement(Document doc, Object value) throws DOMException {
		if (value == null) {
		  throw new IllegalArgumentException("Nulls not supported in XML-RPC");
		}
		Element valueElement = doc.createElement("value");
		if (value instanceof Integer || value instanceof Short) {
			XML.add(valueElement, "int", value.toString());
		} else if (value instanceof Boolean) {
			XML.add(valueElement, "boolean", (Boolean) value ? "1" : "0");
		} else if (value instanceof String) {
			Element stringElement = doc.createElement("string");
			valueElement.appendChild(stringElement);
			stringElement.appendChild(doc.createCDATASection(value.toString()));
		} else if (value instanceof Float || value instanceof Double) {
			XML.add(valueElement, "double", value.toString());
		} else if (value instanceof Date) {
			XML.add(valueElement, "dateTime.iso8601", ISO8601_FORMAT.format((Date) value));
		} else if (value instanceof byte[]) {
			Element base64Element = doc.createElement("base64");
			valueElement.appendChild(base64Element);
			base64Element.appendChild(doc.createCDATASection(new String(Base64.encode((byte[])value))));
		} else if (value instanceof Map) {
			Element structElement = doc.createElement("struct");
			valueElement.appendChild(structElement);
			Map map = (Map) value;
		  for (Object o : map.entrySet()) {
			Element memberElement = doc.createElement("member");
			valueElement.appendChild(memberElement);
			Map.Entry entry = (Map.Entry) o;
			if (!(entry.getKey() instanceof String)) {
			  throw new IllegalArgumentException("Keys in maps for XML-RPC structs must be Strings");
			}
			XML.add(memberElement, "name", entry.getKey().toString());
			memberElement.appendChild(createValueElement(doc, entry.getValue()));
		  }
		} else if (value instanceof Collection) {
			Element arrayElement = doc.createElement("array");
			valueElement.appendChild(arrayElement);
			Element dataElement = doc.createElement("data");
			arrayElement.appendChild(dataElement);
			Collection collection = (Collection) value;
		  for (Object aCollection : collection) {
			dataElement.appendChild(createValueElement(doc, aCollection));
		  }
		} else {
		  throw new IllegalArgumentException(value.getClass().getName() + " not supported in XML-RPC");
		}
		return valueElement;
	}

	/** Parse an XML-RPC method response.
	 *
	 * @param response The response data.
	 * @return The value contained in the <var>response</var>.
	 * @throws XMLRPCFault If the <var>response</var> contained a fault.
	 */
	public static Object parseResponse(byte[] response) throws XMLRPCFault {
		try {
			DOMParser parser = XML.createDOMParser();
			parser.setFeature("http://xml.org/sax/features/validation", false);
			parser.parse(new InputSource(new ByteArrayInputStream(response)));
			Document doc = parser.getDocument();
			doc.normalize();
			XML.removeComments(doc);
			Element methodResponseElement = doc.getDocumentElement();
			if (!"methodResponse".equals(methodResponseElement.getNodeName())) {
			  throw new SAXException("Not a <methodResponse> document");
			}
			Node child = methodResponseElement.getFirstChild();
			if ("params".equals(child.getNodeName())) {
				return parseValue(child.getFirstChild().getFirstChild());
			} else if ("fault".equals(child.getNodeName())) {
				try {
					Map map = (Map) parseValue(child.getFirstChild());
					throw new XMLRPCFault(((Integer) map.get("faultCode")).intValue(),
						(String) map.get("faultString"));
				} catch (ClassCastException ex) {
					throw new SAXException("XML-RPC <fault> invalid");
				}
			} else {
			  throw new SAXException("XML-RPC response does not contain <params> or <fault>");
			}
		} catch (SAXException ex) {
			throw new IllegalArgumentException(ex.getMessage());
		} catch (IOException ex) {
			throw new RuntimeException("Unexpected I/O exception that shouldn't happen, but did: " + ex.getMessage());
		}
	}

	/** Parse an XML-RPC &lt;value&gt;.
	 *
	 * @param node The &lt;value&gt; node.
	 * @return The Java value of <var>node</var>.
	 */
	private static Object parseValue(Node node) {
		String n = node.getNodeName();
		if (!"value".equals(n)) {
		  throw new IllegalArgumentException("Expecting a <value>, not a <" + n + ">");
		}
		Node t = node.getFirstChild();
		n = t.getNodeName();

		// Default if there's no nested element is a String.
		if (t.getNodeType() == Node.TEXT_NODE || t.getNodeType() == Node.CDATA_SECTION_NODE) {
			return t.getNodeValue();
		}

		// Figure out what the type is from the nested element.
		String txt = XML.unwrappedText(t);
		if ("i4".equals(n) || "int".equals(n)) {
			return Integer.valueOf(txt);
		} else if ("boolean".equals(n)) {
			if ("1".equals(txt)) {
			  return true;
			} else if ("0".equals(txt)) {
			  return false;
			} else {
			  throw new IllegalArgumentException(n + " does not contain a 0 or 1");
			}
		} else if ("string".equals(n)) {
			return txt;
		} else if ("double".equals(n)) {
			return new Double(txt);
		} else if ("dateTime.iso8601".equals(n)) {
			try {
				return ISO8601_FORMAT.parse(txt);
			} catch (ParseException ex) {
				throw new IllegalArgumentException(n + " does not contain an ISO8601 format date/time");
			}
		} else if ("base64".equals(n)) {
			return Base64.decode(txt.getBytes());
		} else if ("struct".equals(n)) {
			Map m = new ConcurrentHashMap();
			NodeList memberNodes = t.getChildNodes();
			for (int i = 0; i < memberNodes.getLength(); ++i) {
				Node memberNode = memberNodes.item(i);
				if (!"member".equals(memberNode.getNodeName())) {
				  throw new IllegalArgumentException(n + " contains <" + memberNode.getNodeName()
													 + ">, not <member>");
				}
				Node nameNode = memberNode.getFirstChild();
				if (nameNode == null || !"name".equals(nameNode.getNodeName())) {
				  throw new IllegalArgumentException("<member> missing <name> element");
				}
				Node valueNode = nameNode.getNextSibling();
				if (valueNode == null || !"value".equals(valueNode.getNodeName())) {
				  throw new IllegalArgumentException("<member> missing <value> element");
				}
				m.put(XML.unwrappedText(nameNode), parseValue(valueNode));
			}
			return m;
		} else if ("array".equals(n)) {
			Node dataNode = t.getFirstChild();
			if (dataNode == null || !"data".equals(dataNode.getNodeName())) {
			  throw new IllegalArgumentException("<array> missing <data> element");
			}
			NodeList children = dataNode.getChildNodes();
			List x = new ArrayList(children.getLength());
			for (int i = 0; i < children.getLength(); ++i) {
			  x.add(parseValue(children.item(i)));
			}
			return x;
		} else {
		  throw new IllegalArgumentException("Illegal type " + n + " in <value>");
		}
	}

	/** Constructor that causes a runtime exception since this is a utility class.
	 */
	private XMLRPC() {
		throw new IllegalStateException("Do not construct XMLRPC objects");
	}

	/** ISO8601 date format for XML-RPC dates. */
	private static DateFormat ISO8601_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
}
