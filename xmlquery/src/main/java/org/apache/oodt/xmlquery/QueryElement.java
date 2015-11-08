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

import java.io.*;

import org.apache.oodt.commons.util.*;
import org.w3c.dom.*;

/** A single query element.
 *
 * An object of this class is an element of a query.
 *
 * <p>TODO: Consider computing proper mime-type based on class of object being inserted as
 * a value into the result.
 *
 * @author Kelly
 */
public class QueryElement implements Serializable, Cloneable, Documentable {
	/** Create a blank query element.
	 */
	public QueryElement() {
		this("UNKNOWN", "UNKNOWN");
	}

	/** Create a query element.
	 *
	 * @param role The role the element plays.
	 * @param value The value of the element.
	 */
	public QueryElement(String role, String value) {
		this.role = role;
		this.value = value;
	}

	public QueryElement(Node node) {
		if (!"queryElement".equals(node.getNodeName())) {
		  throw new IllegalArgumentException("Query element must be constructed from <queryElement> node, not <"
											 + node.getNodeName() + ">");
		}
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if ("tokenRole".equals(child.getNodeName())) {
			  role = XML.unwrappedText(child);
			} else if ("tokenValue".equals(child.getNodeName())) {
			  value = XML.unwrappedText(child);
			}
		}
	}

	/** Get my role.
	 *
	 * @return The role this element plays.
	 */
	public String getRole() {
		return role;
	}

	/** Get my value.
	 *
	 * @return The value of this element.
	 */
	public String getValue() {
		return value;
	}

	/** Set my role.
	 *
	 * @param role The new role this element plays.
	 */
	public void setRole(String role) {
		if (role == null) {
		  role = "UNKNOWN";
		}
		this.role = role;
	}

	/** Set my value.
	 *
	 * @param value The new value of this element.
	 */
	public void setValue(String value) {
		if (value == null) {
		  value = "UNKNOWN";
		}
		this.value = value;
	}

	public Node toXML(Document doc) throws DOMException {
		Element root = doc.createElement("queryElement");
		XML.add(root, "tokenRole", getRole());
		XML.add(root, "tokenValue", getValue());
		return root;
	}

	public boolean equals(Object rhs) {
		if (rhs == this) {
		  return true;
		}
		if (rhs == null || !(rhs instanceof QueryElement)) {
		  return false;
		}
		QueryElement obj = (QueryElement) rhs;
		return role.equals(obj.role) && value.equals(obj.value);
	}

	public int hashCode() {
		return role.hashCode() ^ value.hashCode();
	}

	public Object clone() {
		Object rc = null;
		try {
			rc = super.clone();
		} catch (CloneNotSupportedException ignored) {}
		return rc;
	}

	public String toString() {
		return getClass().getName() + "[role=" + role + ",value=" + value + "]";
	}

	/** The role I play. */
	private String role;

	/** The value my role has. */
	private String value;

        /** Serial version unique ID. */
        static final long serialVersionUID = -8401434443475540800L;
}
