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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.oodt.commons.util.Documentable;
import org.apache.oodt.commons.util.XML;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStreamWriter;

/**
 * A profile.
 *
 * Objects of this class are profiles.  Profiles are metadata descriptions of resources.
 *
 * @author Kelly
 */
public class Profile implements Serializable, Cloneable, Comparable<Object>, Documentable {
        /** Serial version unique ID. */
        static final long serialVersionUID = -3936851809184360591L;

	/** The formal public identifier of the profiles DTD. */
	public static final String PROFILES_DTD_FPI = "-//JPL//DTD Profile 1.1//EN";

	/** The system identifier of the profiles DTD. */
	public static String PROFILES_DTD_URL = "http://oodt.jpl.nasa.gov/grid-profile/dtd/prof.dtd";

	/**
	 * Create a list of profiles by deserializing data from the given XML element.
	 *
	 * @param root Either a &lt;profiles&gt; or a &lt;profile&gt; element.
	 * @param factory Factory for creation of profile-related objects.
	 * @return A list of profiles.
	 */
	public static List<Profile> createProfiles(Element root, ObjectFactory factory) {
		List<Profile> profiles = new ArrayList<Profile>();
		if ("profile".equals(root.getNodeName()))
			// The root is a <profile>, so add the single profile to the list.
			profiles.add(factory.createProfile((Element) root));
		else if ("profiles".equals(root.getNodeName())) {
			// The root is a <profiles>, so add each <profile> to the list.
			NodeList children = root.getChildNodes();
			for (int i = 0; i < children.getLength(); ++i) {
				Node node = children.item(i);
				if ("profile".equals(node.getNodeName()))
					profiles.add(factory.createProfile((Element) node));
			}
		} else throw new IllegalArgumentException("Expected a <profiles> or <profile> top level element but got "
			+ root.getNodeName());
		return profiles;
	}


	/**
	 * Create a list of profiles by deserializing data from the given XML element.
	 *
	 * This method uses the default factory that yields objects of this package,
	 * namely {@link Profile}, {@link ProfileAttributes}, {@link ResourceAttributes},
	 * and {@link ProfileElement} and its subclasses.
	 *
	 * @param root Either a &lt;profiles&gt; or a &lt;profile&gt; element.
	 * @return A list of profiles.
	 */
	public static List<Profile> createProfiles(Element root) {
		return createProfiles(root, new DefaultFactory());
	}

	/**
	 * Create a blank profile.
	 */
	public Profile() {
		profAttr = new ProfileAttributes();
		resAttr = new ResourceAttributes(this);
	}

	/**
	 * Create a profile from an XML document.
	 *
	 * @param string The XML document (as a string).
	 * @throws SAXException If the <var>string</var> can't be parsed.
	 */
	public Profile(String string) throws SAXException {
		this(XML.parse(string).getDocumentElement(), new DefaultFactory());
	}

	/**
	 * Create a profile from an XML document.
	 *
	 * @param string The XML document (as a string).
	 * @param factory Object factory to use.
	 * @throws SAXException If the <var>string</var> can't be parsed.
	 */
	public Profile(String string, ObjectFactory factory) throws SAXException {
		this(XML.parse(string).getDocumentElement(), factory);
	}

	/**
	 * Creates a new <code>Profile</code> instance.
	 *
	 * @param root a <code>Node</code> value.
	 */
	public Profile(Node root) {
		this(root, new DefaultFactory());
	}

	/**
	 * Create a profile from an XML document.
	 *
	 * @param root The &lt;profile&gt; element.
	 */
	public Profile(Node root, ObjectFactory factory) {
		if (!root.getNodeName().equals("profile"))
			throw new IllegalArgumentException("Construct a Profile from a <profile> element, not a <"
				+ root.getNodeName() + ">");
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node node = children.item(i);
			if ("profAttributes".equals(node.getNodeName()))
				profAttr = factory.createProfileAttributes((Element) node);
			else if ("resAttributes".equals(node.getNodeName()))
				resAttr = factory.createResourceAttributes(this, (Element) node);
			else if ("profElement".equals(node.getNodeName())) {
				ProfileElement element = ProfileElement.createProfileElement((Element) node, this, factory);
				elements.put(element.getName(), element);
			}
		}
	}

	/**
	 * Create a profile from its attributes.
	 *
	 * @param profAttr Profile attributes.
	 * @param resAttr Resource attributes.
	 */
	public Profile(ProfileAttributes profAttr, ResourceAttributes resAttr) {
		this.profAttr = profAttr;
		this.resAttr = resAttr;
		if (this.resAttr != null) this.resAttr.profile = this;
	}

	public int hashCode() {
		return profAttr.hashCode();
	}

	public boolean equals(Object rhs) {
		if (rhs == this) return true;
		if (rhs == null || !(rhs instanceof Profile)) return false;
		Profile obj = (Profile) rhs;
		return profAttr.equals(obj.profAttr);
	}

	public int compareTo(Object rhs) {
		Profile obj = (Profile) rhs;
		return profAttr.compareTo(obj.profAttr);
	}

	public String toString() {
		Document doc = createProfileDocument();
		doc.removeChild(doc.getDocumentElement());
		doc.appendChild(toXML(doc));
		return XML.serialize(doc);
	}

	public Object clone() {
		Object clone = null;
		try {
			clone = super.clone();
		} catch (CloneNotSupportedException cantHappen) {}
		return clone;
	}

	/**
	 * Get the profile attributes.
	 *
	 * @return The profile attributes.
	 */
	public ProfileAttributes getProfileAttributes() {
		return profAttr;
	}

	/**
	 * Get the resource attributes.
	 *
	 * @return The resource attributes.
	 */
	public ResourceAttributes getResourceAttributes() {
		return resAttr;
	}

	/**
	 * Set this profile's profile attributes.
	 *
	 * @param profAttr a <code>ProfileAttributes</code> value.
	 */
	public void setProfileAttributes(ProfileAttributes profAttr) {
		this.profAttr = profAttr;
	}

	/**
	 * Set this profile's resource attributes.
	 *
	 * @param resAttr a <code>ResourceAttributes</code> value.
	 */
	public void setResourceAttributes(ResourceAttributes resAttr) {
		this.resAttr = resAttr;
	}

	/**
	 * Get the profile elements.
	 *
	 * The profile elements are a mapping from the element name ({@link
	 * java.lang.String}) to {@link ProfileElement}.
	 *
	 * @return The profile elements.
	 */
	public Map<String, ProfileElement> getProfileElements() {
		return elements;
	}

	public URI getURI() {
		return resAttr.getURI();
	}

	/**
	 * Add this profile as an RDF resource description to an RDF model.
	 *
	 * This creates a description of the resource that this profile describes, as well
	 * as additional resources for the reified statements about the profile itself,
	 * and adds them all to the given model.
	 *
	 * @param model Model to which to add the profile and its related descriptions.
	 */
	public void addToModel(Model model) {
		Resource resource = model.createResource(getURI().toString());
		resAttr.addToModel(model, resource, profAttr);
	  for (ProfileElement e : elements.values()) {
		e.addToModel(model, resource, profAttr);
	  }
	}

	/**
	 * Serialize this profile as an XML node.
	 *
	 * @param doc The document that will own this node.
	 * @return The XML element &lt;profile&gt; representing these attributes.
	 * @throws DOMException If an error occurs creating the XML nodes.
	 */
	public Node toXML(Document doc) throws DOMException {
		return toXML(doc, /*withElements*/true);
	}

	/**
	 * Serialize this profile as an XML node but without any elements.
	 *
	 * @param doc The document that will own this node.
	 * @return The XML element &lt;profile&gt; representing these attributes.
	 * @throws DOMException If an error occurs creating the XML nodes.
	 */
	public Node toXMLWithoutElements(Document doc) throws DOMException {
		return toXML(doc, /*withElements*/false);
	}

	/**
	 * Serialize this profile as an XML node.
	 *
	 * @param doc The document that will own this node.
	 * @param withElements Include the profile elements?
	 * @return The XML element &lt;profile&gt; representing these attributes.
	 * @throws DOMException If an error occurs creating the XML nodes.
	 */
	private Node toXML(Document doc, boolean withElements) throws DOMException {
		Element profile = doc.createElement("profile");
		profile.appendChild(profAttr.toXML(doc));
		profile.appendChild(resAttr.toXML(doc));
		if (withElements)
		  for (ProfileElement profileElement : elements.values()) {
			profile.appendChild((profileElement).toXML(doc));
		  }
		return profile;
	}

	/**
	 * Create a &lt;profiles&gt; document using the profiles DTD.
	 *
	 * @return A &lt;profiles&gt; document with the profiles DTD.
	 */
	public static Document createProfilesDocument() {
		return createDocument("profiles");
	}

	/**
	 * Create a &lt;profile&gt; document using the profiles DTD.
	 *
	 * @return A &lt;profile&gt; document with the profiles DTD.
	 */
	public static Document createProfileDocument() {
		return createDocument("profile");
	}

	/**
	 * Create a document using the profiles DTD with the given root element.
	 *
	 * @param root Name of the root element.
	 * @return The document with the appropriate document type declaration.
	 */
	static Document createDocument(String root) {
		DocumentType docType = XML.getDOMImplementation().createDocumentType(root, PROFILES_DTD_FPI, PROFILES_DTD_URL);
	  return XML.getDOMImplementation().createDocument(/*namespaceURI*/null, root, docType);
	}

	/** My profile attributes. */
	protected ProfileAttributes profAttr;

	/** My resource attributes. */
	protected ResourceAttributes resAttr;

	/** My elements.
	 *
	 * This mapping is from element name (a {@link String}) to {@link ProfileElement}.
	 */
	protected Map<String, ProfileElement> elements = new HashMap<String, ProfileElement>();

	/**
	 * Try to parse an XML profile in a file in its XML vocabulary.  If successful,
	 * you get the profile as RDF document (in XML format) to the standard output
	 * after it's been digested by the profile class.  If not, then you get an
	 * exception.
	 *
	 * @param argv Command-line arguments, of which there should be one, the name of XML file containing the profile to parse
	 * @throws Throwable if an error occurs.
	 */
	public static void main(String[] argv) throws Throwable {
		if (argv.length != 1) {
			System.err.println("Usage: <profile.xml>");
			System.exit(1);
		}
		StringBuilder b = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(argv[0]));
		char[] buf = new char[512];
		int num;
		while ((num = reader.read(buf)) != -1)
			b.append(buf, 0, num);
		reader.close();
		Profile p = new Profile(b.toString());

		Model model = ModelFactory.createDefaultModel();
		p.addToModel(model);
		OutputStreamWriter writer = new OutputStreamWriter(System.out);
		model.write(writer);
		writer.close();
		System.exit(0);
	}
}
