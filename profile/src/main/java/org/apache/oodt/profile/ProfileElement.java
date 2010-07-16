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


package org.apache.oodt.profile;

import com.hp.hpl.mesa.rdf.jena.model.Model;
import com.hp.hpl.mesa.rdf.jena.model.RDFException;
import com.hp.hpl.mesa.rdf.jena.model.Resource;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.oodt.commons.util.Documentable;
import org.apache.oodt.commons.util.XML;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Profile element.
 *
 * Objects of this class are elements that describe the composition of a resource.
 *
 * @author Kelly
 */
public abstract class ProfileElement implements Serializable, Cloneable, Comparable, Documentable {
	/**
	 * Create a profile element from the given XML node.
	 *
	 * @param root The &lt;profElement&gt; node.
	 * @param profile To what profile the element belongs.
	 * @return A profile element.
	 */
	public static ProfileElement createProfileElement(Element root, Profile profile, ObjectFactory factory) {
		String name = null;
		String id = null;
		String desc = null;
		String type = null;
		String unit = null;
		List synonyms = new ArrayList();
		boolean obligation = false;
		int maxOccurrence = 0;
		String comments = null;
		boolean ranged = false;
		NodeList children = root.getChildNodes();
		String min = "0.0", max = "0.0";
		boolean gotMin = false;
		boolean gotMax = false;
		List values = new ArrayList();
		for (int i = 0; i < children.getLength(); ++i) {
			Node node = children.item(i);
			if ("elemId".equals(node.getNodeName()))
				id = XML.unwrappedText(node);
			else if ("elemName".equals(node.getNodeName()))
				name = XML.unwrappedText(node);
			else if ("elemDesc".equals(node.getNodeName()))
				desc = XML.unwrappedText(node);
			else if ("elemType".equals(node.getNodeName()))
				type = XML.unwrappedText(node);
			else if ("elemUnit".equals(node.getNodeName()))
				unit = XML.unwrappedText(node);
			else if ("elemEnumFlag".equals(node.getNodeName()))
				ranged = "F".equals(XML.unwrappedText(node));
			else if ("elemSynonym".equals(node.getNodeName()))
				synonyms.add(XML.unwrappedText(node));
			else if ("elemObligation".equals(node.getNodeName())) {
				String value = XML.unwrappedText(node);
				obligation = "Required".equals(value) || "T".equals(value);
			} else if ("elemMaxOccurrence".equals(node.getNodeName()))
				try {
					maxOccurrence = Integer.parseInt(XML.unwrappedText(node));
				} catch (NumberFormatException ignore) {}
			else if ("elemComment".equals(node.getNodeName()))
				comments = XML.unwrappedText(node);
			else if ("elemValue".equals(node.getNodeName())) {
				values.add(text(node));
			} else if ("elemMinValue".equals(node.getNodeName()))
				try {
					min = XML.unwrappedText(node);
					gotMin = true;
				} catch (NumberFormatException ignore) {}
			else if ("elemMaxValue".equals(node.getNodeName()))
				try {
					max = XML.unwrappedText(node);
					gotMax = true;
				} catch (NumberFormatException ignore) {}
		}
		if (ranged) {
			if (gotMin && gotMax) {
				return factory.createRangedProfileElement(profile, name, id, desc, type, unit, synonyms, obligation,
					maxOccurrence, comments, min, max);
			}
			return factory.createUnspecifiedProfileElement(profile, name, id, desc, type, unit, synonyms, obligation,
				maxOccurrence, comments);
		} else
			return factory.createEnumeratedProfileElement(profile, name, id, desc, type, unit, synonyms, obligation,
				maxOccurrence, comments, values);
	}

	/**
	 * Create blank profile attributes belonging to the given profile.
	 */
	protected ProfileElement(Profile profile) {
		this.profile = profile;
		synonyms = new ArrayList();
	}

	/**
	 * Create a profile element from constituent attributes.
	 *
	 * @param profile Profile to which this element belongs.
	 * @param name Required name of the element.
	 * @param id ID of the element.
	 * @param desc Long description of the element.
	 * @param type Data type of the element.
	 * @param unit Units.
	 * @param synonyms Collection of element IDs ({@link String}) that are synonyms for this element.
	 * @param obligation True if this is a required element
	 * @param maxOccurrence Maximum number of occurrences of this element.
	 * @param comments Any comments about this element.
	 */
	protected ProfileElement(Profile profile, String name, String id, String desc, String type, String unit, List synonyms,
		boolean obligation, int maxOccurrence, String comments) {
		this.profile = profile;
		this.name = name;
		this.id = id;
		this.desc = desc;
		this.type = type;
		this.unit = unit;
		this.synonyms = synonyms;
		this.obligation = obligation;
		this.maxOccurrence = maxOccurrence;
		this.comments = comments;
	}

	public int hashCode() {
		return name.hashCode();
	}

	public boolean equals(Object rhs) {
		if (rhs == this) return true;
		if (rhs == null || !(rhs instanceof ProfileElement)) return false;
		ProfileElement obj = (ProfileElement) rhs;
		return profile.equals(obj.profile) && name.equals(obj.name);
	}

	public String toString() {
		return getClass().getName() + "[profile=" + profile.toString() + ",name=" + name + "]";
	}

	public Object clone() {
		Object obj = null;
		try {
			obj = super.clone();
		} catch (CloneNotSupportedException cantHappen) {}
		return obj;
	}

	public int compareTo(Object rhs) {
		ProfileElement obj = (ProfileElement) rhs;
		if (profile.compareTo(obj.profile) < 0)
			return -1;
		if (profile.compareTo(obj.profile) == 0)
			return name.compareTo(obj.name);
		return 1;
	}

	/**
	 * Get the profile to whom I belong.
	 *
	 * @return The owning profile.
	 */
	public Profile getProfile() {
		return profile;
	}

	/**
	 * Get my name.
	 *
	 * @return My name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get my element ID.
	 *
	 * @return My ID.
	 */
	public String getID() {
		return id;
	}

	/**
	 * Get my long description.
	 *
	 * @return My long description.
	 */
	public String getDescription() {
		return desc;
	}

	/**
	 * Get my type.
	 *
	 * @return My type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Get my unit.
	 *
	 * @return My unit.
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * Get my synonyms.
	 *
	 * @return A list of synonyms.
	 */
	public List getSynonyms() {
		return synonyms;
	}

	/**
	 * Am I obligatory?
	 *
	 * @return True if I am, false otherwise.
	 */
	public boolean isObligatory() {
		return obligation;
	}

	/**
	 * Get how many times I can occur.
	 *
	 * @return How many times I can occur.
	 */
	public int getMaxOccurrence() {
		return maxOccurrence;
	}

	/**
	 * Get any comments about me.
	 *
	 * @return Any comments.
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * Set the profile to whom I belong.
	 *
	 * @param profile The owning profile.
	 */
	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	/**
	 * Set my name.
	 *
	 * @param name My name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set my element ID.
	 *
	 * @param id My ID.
	 */
	public void setID(String id) {
		this.id = id;
	}

	/**
	 * Set my long description.
	 *
	 * @param desc My long description.
	 */
	public void setDescription(String desc) {
		this.desc = desc;
	}

	/**
	 * Set my type.
	 *
	 * @param type My type.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Set my unit.
	 *
	 * @param unit My unit.
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * Set whether I'm obligatory.
	 *
	 * @param obligatory True if I am, false otherwise.
	 */
	public void setObligation(boolean obligatory) {
		this.obligation = obligatory;
	}

	/**
	 * Set how many times I can occur.
	 *
	 * @param occurrence How many times I can occur.
	 */
	public void setMaxOccurrence(int occurrence) {
		this.maxOccurrence = occurrence;
	}

	/**
	 * Set any comments about me.
	 *
	 * @param comments Any comments.
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * Get my minimum value.
	 *
	 * @return My minimum value.
	 */
	public abstract String getMinValue();

	/**
	 * Get my maximum value.
	 *
	 * @return My maximum value.
	 */
	public abstract String getMaxValue();

	/**
	 * Get legal values.
	 *
	 * @return List of legal values (as {@link String}s).
	 */
	public abstract List getValues();

	/**
	 * Serialize this element as an XML node.
	 *
	 * @param doc The document that will own this node.
	 * @return The XML element &lt;resAttributes&gt; representing these attributes.
	 * @throws DOMException If an error occurs creating the XML nodes.
	 */
	public Node toXML(Document doc) throws DOMException {
		return toXML(doc, /*withValues*/true);
	}

	/**
	 * Serialize this element as an XML node without any values.
	 *
	 * @param doc The document that will own this node.
	 * @return The XML element &lt;resAttributes&gt; representing these attributes.
	 * @throws DOMException If an error occurs creating the XML nodes.
	 */
	public Node toXMLWithoutValues(Document doc) throws DOMException {
		return toXML(doc, /*withValues*/false);
	}

	/**
	 * Serialize this element.
	 *
	 * @param doc The document that will own this node.
	 * @param withValues If true, serialize the values as well.
	 * @return The XML element &lt;profElement&gt; representing this element.
	 * @throws DOMException If an error occurs creating the XML nodes.
	 */
	private Node toXML(Document doc, boolean withValues) throws DOMException {
		Element profElement = doc.createElement("profElement");
		XML.addNonNull(profElement, "elemId", id);
		XML.addNonNull(profElement, "elemName", name);
		XML.addNonNull(profElement, "elemDesc", desc);
		XML.addNonNull(profElement, "elemType", type);
		XML.addNonNull(profElement, "elemUnit", unit);
		XML.add(profElement, "elemEnumFlag", isEnumerated()? "T" : "F");
		if (withValues) addValues(profElement);
		XML.add(profElement, "elemSynonym", synonyms);
		if (isObligatory())
			XML.add(profElement, "elemObligation","Required");
		if (getMaxOccurrence() >= 0)
			XML.add(profElement, "elemMaxOccurrence", String.valueOf(getMaxOccurrence()));
		XML.add(profElement, "elemComment", comments);
		return profElement;
	}

	/**
	 * Tell if this element is of the enumerated kind.
	 *
	 * @return a <code>boolean</code> value.
	 */
	protected abstract boolean isEnumerated();

	/**
	 * Add the values of this element to the given node.
	 *
	 * @param node The node to add to.
	 * @throws DOMException If an error occurs creating the XML nodes.
	 */
	protected abstract void addValues(Node node) throws DOMException;

	/**
	 * Add this element to an RDF model including this element's profile.
	 *
	 * @param model The model to which we're being added.
	 * @param resource The profile as an RDF resource description.
	 * @param profAttr The profile's attributes.
	 * @throws RDFException if an error occurs.
	 */
	void addToModel(Model model, Resource resource, ProfileAttributes profAttr) throws RDFException {
		URI profileURI = profile.getURI();
		URI myURI = URI.create(profileURI.toString() + "#" + name);

		Resource element = model.createResource(myURI.toString());
		Utility.addProperty(model, resource, Utility.edmElement, element, profAttr, myURI);

		String obStr = obligation? "Required" : "Optional";
		String occurStr = String.valueOf(maxOccurrence);

		Utility.addProperty(model, element, Utility.edmElemID,        id,       profAttr, myURI);
		Utility.addProperty(model, element, Utility.edmDescription,   desc,     profAttr, myURI);
		Utility.addProperty(model, element, Utility.edmElemType,      type,     profAttr, myURI);
		Utility.addProperty(model, element, Utility.edmUnit,          unit,     profAttr, myURI);
		Utility.addProperty(model, element, Utility.edmSynonym,       synonyms, profAttr, myURI);
		Utility.addProperty(model, element, Utility.edmObligation,    obStr,    profAttr, myURI);
		Utility.addProperty(model, element, Utility.edmMaxOccurrence, occurStr, profAttr, myURI);
		Utility.addProperty(model, element, Utility.edmComment,       comments, profAttr, myURI);

		addElementSpecificProperties(model, element, profAttr, myURI);
	}

	/**
	 * Add the statements specific to this kind of profile element to an RDF model.
	 *
	 * @param model The model.
	 * @param element This element, as an RDF resource.
	 * @param profAttr The attributes of the element's profile.
	 * @throws RDFException if an error occurs.
	 */
	protected abstract void addElementSpecificProperties(Model model, Resource element, ProfileAttributes profAttr, URI uri)
		throws RDFException;

	/** My profile. */
	protected Profile profile;

	/** My required name. */
	protected String name;

	/** My optional id. */
	protected String id;

	/** My optional description. */
	protected String desc;

	/** My optional type. */
	protected String type;

	/** My optional unit. */
	protected String unit;

	/** My synonyms. */
	protected List synonyms;

	/** My obligation. */
	protected boolean obligation;

	/** My maxOccurrence. */
	protected int maxOccurrence;

	/** My optional comments. */
	protected String comments;

	/**
	 * Create a &lt;profElement&gt; document using the profiles DTD.
	 *
	 * @return A &lt;profElement&gt; document with the profiles DTD.
	 */
	public static Document createProfElementDocument() {
		return Profile.createDocument("profElement");
	}

	/**
	 * Given a set of profile elements, return a set of profiles that own those elements.
	 *
	 * @param elements Profile elements.
	 * @return Profiles that own those elements.
	 */
	public static Set profiles(Set elements) {
		Set rc = new HashSet();
		for (Iterator i = elements.iterator(); i.hasNext();) {
			ProfileElement element = (ProfileElement) i.next();
			rc.add(element.getProfile());
		}
		return rc;
	}

	/**
	 * Given a set of profiles and of profile elements, return those elements that
	 * are owned by any of the given profiles.
	 *
	 * @param profiles Profiles.
	 * @param elements Profile elements.
	 * @return Members of <var>elements</var> that are owned by members of <var>profiles</var>.
	 */
	public static Set elements(Set profiles, Set elements) {
		Set rc = new HashSet();
		for (Iterator i = elements.iterator(); i.hasNext();) {
			ProfileElement element = (ProfileElement) i.next();
			if (profiles.contains(element.getProfile()))
				rc.add(element);
		}
		return rc;
	}

	static String text(Node node) {
		StringBuffer b = new StringBuffer();
	        text0(b, node);
		return b.toString();
	}

	static void text0(StringBuffer b, Node node) {
		if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
			b.append(node.getNodeValue());
			return;
		}
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i)
			text0(b, children.item(i));
	}
}

