// This software was developed by the Object Oriented Data Technology task of the Science
// Data Engineering group of the Engineering and Space Science Directorate of the Jet
// Propulsion Laboratory of the National Aeronautics and Space Administration, an
// independent agency of the United States Government.
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
// $Id: QueryElement.java,v 1.4 2005-08-03 16:03:59 kelly Exp $

package jpl.eda.xmlquery;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import jpl.eda.util.*;
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
		if (!"queryElement".equals(node.getNodeName()))
			throw new IllegalArgumentException("Query element must be constructed from <queryElement> node, not <"
				+ node.getNodeName() + ">");
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if ("tokenRole".equals(child.getNodeName()))
				role = XML.unwrappedText(child);
			else if ("tokenValue".equals(child.getNodeName()))
				value = XML.unwrappedText(child);
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
		if (role == null) role = "UNKNOWN";
		this.role = role;
	}

	/** Set my value.
	 *
	 * @param value The new value of this element.
	 */
	public void setValue(String value) {
		if (value == null) value = "UNKNOWN";
		this.value = value;
	}

	public Node toXML(Document doc) throws DOMException {
		Element root = doc.createElement("queryElement");
		XML.add(root, "tokenRole", getRole());
		XML.add(root, "tokenValue", getValue());
		return root;
	}

	public boolean equals(Object rhs) {
		if (rhs == this) return true;
		if (rhs == null || !(rhs instanceof QueryElement)) return false;
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
		} catch (CloneNotSupportedException cantHappen) {}
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
