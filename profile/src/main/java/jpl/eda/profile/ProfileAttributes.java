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


package jpl.eda.profile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jpl.eda.util.Documentable;
import jpl.eda.util.XML;
import jpl.eda.Configuration;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.IOException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

/**
 * Profile attributes.
 *
 * Objects of this class are attributes of profiles.
 *
 * @author Kelly
 */
public class ProfileAttributes implements Serializable, Cloneable, Comparable, Documentable {
	/**
	 * Create blank profile attributes.
	 */
	public ProfileAttributes() {
		id = "UNKNOWN";
		type = "UNKNOWN";
		statusID = "UNKNOWN";
		children = new ArrayList();
		revisionNotes = new ArrayList();
	}

	/**
	 * Create profile attributes from an XML document.
	 *
	 * @param root The &lt;profAttributes&gt; element.
	 */
	public ProfileAttributes(Node root) {
		children = new ArrayList();
		revisionNotes = new ArrayList();
		NodeList childNodes = root.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); ++i) {
			Node node = childNodes.item(i);
			if ("profId".equals(node.getNodeName()))
				id = XML.unwrappedText(node);
			else if ("profVersion".equals(node.getNodeName()))
				version = XML.unwrappedText(node);
			else if ("profType".equals(node.getNodeName()))
				type = XML.unwrappedText(node);
			else if ("profStatusId".equals(node.getNodeName()))
				statusID = XML.unwrappedText(node);
			else if ("profSecurityType".equals(node.getNodeName()))
				securityType = XML.unwrappedText(node);
			else if ("profParentId".equals(node.getNodeName()))
				parent = XML.unwrappedText(node);
			else if ("profChildId".equals(node.getNodeName()))
				children.add(XML.unwrappedText(node));
			else if ("profRegAuthority".equals(node.getNodeName()))
				regAuthority = XML.unwrappedText(node);
			else if ("profRevisionNote".equals(node.getNodeName()))
				revisionNotes.add(XML.unwrappedText(node));
		}
	}

	/**
	 * Create profile attributes from constituent attributes.
	 *
	 * @param id The profile ID.
	 * @param version Version.
	 * @param type Type of the profile.
	 * @param statusID Status ID.
	 * @param securityType Security type.
	 * @param parent Parent profile IDs.
	 * @param children Zero or more {@link String} child profile IDs.
	 * @param regAuthority Registration authority.
	 * @param revisionNotes Zero or more {@link String} revision notes.
	 */
	public ProfileAttributes(String id, String version, String type, String statusID, String securityType, String parent,
		List children, String regAuthority, List revisionNotes) {
		this.id = id;
		this.version = version;
		this.type = type;
		this.statusID = statusID;
		this.securityType = securityType;
		this.parent = parent;
		this.children = children;
		this.regAuthority = regAuthority;
		this.revisionNotes = revisionNotes;
	}

	public int hashCode() {
		return id.hashCode();
	}

	public boolean equals(Object rhs) {
		if (rhs == this) return true;
		if (rhs == null || !(rhs instanceof ProfileAttributes)) return false;
		return ((ProfileAttributes) rhs).id.equals(id);
	}

	public int compareTo(Object rhs) {
		ProfileAttributes obj = (ProfileAttributes) rhs;
		return id.compareTo(obj.id);
	}

	public String toString() {
		return getClass().getName() + "[id=" + id + "]";
	}

	public Object clone() {
		Object clone = null;
		try {
			clone = super.clone();
		} catch (CloneNotSupportedException cantHappen) {}
		return clone;
	}

	/**
	 * Get the ID.
	 *
	 * @return The ID.
	 */
	public String getID() {
		return id;
	}

	/**
	 * Get the version.
	 *
	 * @return The version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Get the type.
	 *
	 * @return The type of the profile.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Get the status ID.
	 *
	 * @return The status ID.
	 */
	public String getStatusID() {
		return statusID;
	}

	/**
	 * Get the security type.
	 *
	 * @return The security type.
	 */
	public String getSecurityType() {
		return securityType;
	}

	/**
	 * Get the parent of this profile.
	 *
	 * @return The parent profile's ID.
	 */
	public String getParent() {
		return parent;
	}

	/**
	 * Get the children of this profile.
	 *
	 * @return A list of {@link String} IDs of its children.
	 */
	public List getChildren() {
		return children;
	}

	/**
	 * Get the registration authority of this profile.
	 *
	 * @return Its registration authority.
	 */
	public String getRegAuthority() {
		return regAuthority;
	}

	/**
	 * Get the revision notes of this profile.
	 *
	 * @return A list of {@link String} revision notes.
	 */
	public List getRevisionNotes() {
		return revisionNotes;
	}

	/**
	 * 
	 * Set the Parent
	 * 
	 * @param theParent The Parent String.
	 */
	public void setParent(String theParent){
		parent = theParent;
	}

	/**
	 * Set the ID.
	 *
	 * @param id The ID.
	 */
	public void setID(String id) {
		this.id = id;
	}

	/**
	 * Set the version.
	 *
	 * @param version The version.
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Set the type.
	 *
	 * @param type The type of the profile.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Set the status ID.
	 *
	 * @param statusID The status ID.
	 */
	public void setStatusID(String statusID) {
		this.statusID = statusID;
	}

	/**
	 * Set the security type.
	 *
	 * @param securityType The security type.
	 */
	public void setSecurityType(String securityType) {
		this.securityType = securityType;
	}

	/**
	 * Set the registration authority of this profile.
	 *
	 * @param regAuthority Its registration authority.
	 */
	public void setRegAuthority(String regAuthority) {
		this.regAuthority = regAuthority;
	}

	/**
	 * Serialize this attributes as an XML node.
	 *
	 * @param doc The document that will own this node.
	 * @return The XML element &lt;profAttributes&gt; representing these attributes.
	 * @throws DOMException If an error occurs creating the XML nodes.
	 */
	public Node toXML(Document doc) throws DOMException {
		Element root = doc.createElement("profAttributes");
		XML.add(root, "profId", id);
		XML.addNonNull(root, "profVersion", version);
		XML.addNonNull(root, "profType", type);
		XML.addNonNull(root, "profStatusId", statusID);
		XML.addNonNull(root, "profSecurityType", securityType);
		XML.addNonNull(root, "profParentId", parent);
		XML.add(root, "profChildId", children);
		XML.addNonNull(root, "profRegAuthority", regAuthority);
		XML.add(root, "profRevisionNote", revisionNotes);
		return root;
	}

	/** Unique identifier, required. */
	protected String id;

	/** Version, optional. */
	protected String version;

	/** Type, required. */
	protected String type;

	/** Status ID, required. */
	protected String statusID;

	/** Type of security to apply, optional. */
	protected String securityType;

	/** Parent profile ID, optional. */
	protected String parent;

	/** List of zero or more children profile IDs ({@link String}s). */
	protected List children;

	/** Registration authority, optional. */
	protected String regAuthority;

	/** Revision notes, zero or more {@link String}s. */
	protected List revisionNotes;

        /** Serial version unique ID. */
        static final long serialVersionUID = 6140264312462080058L;

	/**
	 * Create a &lt;profAttributes&gt; document using the profiles DTD.
	 *
	 * @return A &lt;profAttributs&gt; document with the profiles DTD.
	 */
	public static Document createProfAttributesDocument() {
		return Profile.createDocument("profAttributes");
	}


}
