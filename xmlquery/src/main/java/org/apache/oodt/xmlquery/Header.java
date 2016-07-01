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

import org.apache.oodt.commons.util.Documentable;
import org.apache.oodt.commons.util.XML;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** A single header.
 *
 * An object of this class is a header element of a query.
 *
 * @author Kelly
 */
public class Header implements Serializable, Cloneable, Documentable {
	/** Create a list of headers from an XML document.
	 *
	 * @param root A &lt;resultHeader&gt; element.
	 * @return A list of <code>Header</code>s.
	 */
	public static List createHeaders(Node root) {
		if (!"resultHeader".equals(root.getNodeName())) {
		  throw new IllegalArgumentException("Expected <resultHeader> but got <" + root.getNodeName() + ">");
		}
		NodeList children = root.getChildNodes();
		List rc = new ArrayList();
		for (int i = 0; i < children.getLength(); ++i){
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Header header = new Header(node);
				rc.add(header);
			}
		}
		return rc;
	}

	/** Create a new, blank header.
	 *
	 * This initializes the result with default values for various properties.
	 */
	public Header() {
		this(/*name*/"UNKNOWN");
	}

	/** Create a header.
	 *
	 * Here, you specify the header's name only.
	 *
	 * @param name Name of this header.
	 */
	public Header(String name) {
		this(name, /*type*/null, /*unit*/null);
	}

	/** Create a fully specified header.
	 *
	 * @param name Name of this header.
	 * @param type Data type.
	 * @param unit Units.
	 */
	public Header(String name, String type, String unit) {
		this.name = name;
		this.type = type;
		this.unit = unit;
	}

	/** Create a header from a DOM node.
	 *
	 * @param node The DOM node, which must be a &lt;headerElement&gt; element.
	 */
	public Header(Node node) {
		if (!"headerElement".equals(node.getNodeName())) {
		  throw new IllegalArgumentException("Header must be constructed from <headerElement> node, not <"
											 + node.getNodeName() + ">");
		}
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if ("elemName".equals(child.getNodeName())) {
			  name = XML.unwrappedText(child);
			} else if ("elemType".equals(child.getNodeName())) {
			  type = XML.unwrappedText(child);
			} else if ("elemUnit".equals(child.getNodeName())) {
			  unit = XML.unwrappedText(child);
			}
		}
	}

	/** Get the name.
	 *
	 * @return The name of the header, suitable for printing in a column heading.
	 */
	public String getName() {
		return name;
	}

	/** Get the type of this result.
	 *
	 * @return The type, as in data type.
	 */
	public String getType() {
		return type;
	}

	/** Get the unit.
	 *
	 * @return The units in which the header is presented.
	 */
	public String getUnit() {
		return unit;
	}

	/** Set the name.
	 *
	 * @param name The name of the header, suitable for printing in a column heading.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/** Set the type of this result.
	 *
	 * @param type The type, as in data type.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/** Set the unit.
	 *
	 * @param unit The units in which the header is presented.
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Node toXML(Document doc) throws DOMException {
		Element root = doc.createElement("headerElement");
		XML.add(root, "elemName", getName());
		XML.addNonNull(root, "elemType", getType());
		XML.addNonNull(root, "elemUnit", getUnit());
		return root;
	}

	public int hashCode() {
		return name.hashCode();
	}

	public boolean equals(Object rhs) {
		if (rhs == this) {
		  return true;
		}
		if (rhs == null || !(rhs instanceof Header)) {
		  return false;
		}
		Header obj = (Header) rhs;
		return name.equals(obj.name) && ((type == null && obj.type == null) || type.equals(obj.type))
			&& ((unit == null && obj.unit == null) || unit.equals(obj.unit));
	}

	public Object clone() {
		Object rc;
		try {
			rc = super.clone();
		} catch (CloneNotSupportedException cantHappen) {
			throw new RuntimeException("CloneNotSupportedException thrown for class that implements Cloneable: "
				+ cantHappen.getMessage());
		}
		return rc;
	}

	public String toString() {
		return getClass().getName() + "[name=" + getName() + ",type=" + getType() + ",unit=" + getUnit() + "]";
	}

	/** Name of the header. */
	private String name;

	/** Type, as in type of the data. */
	private String type;

	/** Unit, as in the units of the data type. */
	private String unit;

        /** Serial version unique ID. */
        static final long serialVersionUID = -4596588383046581840L;
}
