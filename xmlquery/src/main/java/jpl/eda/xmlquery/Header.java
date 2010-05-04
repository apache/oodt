// This software was developed by the Object Oriented Data Technology task of the Science
// Data Engineering group of the Engineering and Space Science Directorate of the Jet
// Propulsion Laboratory of the National Aeronautics and Space Administration, an
// independent agency of the United States Government.
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
// $Id: Header.java,v 1.3 2005-08-03 16:04:07 kelly Exp $

package jpl.eda.xmlquery;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import jpl.eda.util.*;
import org.w3c.dom.*;

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
		if (!"resultHeader".equals(root.getNodeName()))
			throw new IllegalArgumentException("Expected <resultHeader> but got <" + root.getNodeName() + ">");
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
		if (!"headerElement".equals(node.getNodeName()))
			throw new IllegalArgumentException("Header must be constructed from <headerElement> node, not <"
				+ node.getNodeName() + ">");
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if ("elemName".equals(child.getNodeName()))
				name = XML.unwrappedText(child);
			else if ("elemType".equals(child.getNodeName()))
				type = XML.unwrappedText(child);
			else if ("elemUnit".equals(child.getNodeName()))
				unit = XML.unwrappedText(child);
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
		if (rhs == this) return true;
		if (rhs == null || !(rhs instanceof Header)) return false;
		Header obj = (Header) rhs;
		return name.equals(obj.name) && ((type == null && obj.type == null) || type.equals(obj.type))
			&& ((unit == null && obj.unit == null) || unit.equals(obj.unit));
	}

	public Object clone() {
		Object rc = null;
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
