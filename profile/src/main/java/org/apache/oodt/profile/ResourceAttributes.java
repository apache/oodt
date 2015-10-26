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
import org.apache.jena.rdf.model.Resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.oodt.commons.util.Documentable;
import org.apache.oodt.commons.util.XML;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URI;

/**
 * Resource attribuets.
 *
 * Objects of this class are resource attributes of profiles.
 *
 * @author Kelly
 */
public class ResourceAttributes implements Serializable, Cloneable, Comparable<Object>, Documentable {
	/**
	 * Create blank profile attributes.
	 */
	public ResourceAttributes() {
		this(/*profile*/null);
	}

	/**
	 * Create blank profile attributes belonging to a certain profile.
	 *
	 * @param profile Owning profile.
	 */
	public ResourceAttributes(Profile profile) {
		this.profile = profile;
		identifier = "UNKNOWN";

		// Other attributes are optional according to prof.dtd.
		initializeLists();
	}

	/**
	 * Create resource attributes from an XML document.
	 *
	 * @param profile Owning profile.
	 * @param root The &lt;resAttributes&gt; element.
	 */
	public ResourceAttributes(Profile profile, Node root) {
		this.profile = profile;
		initializeLists();
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node node = children.item(i);
			if ("Identifier".equals(node.getNodeName()))
				identifier = XML.unwrappedText(node);
			else if ("Title".equals(node.getNodeName()))
				title = XML.unwrappedText(node);
			else if ("Format".equals(node.getNodeName()))
				formats.add(XML.unwrappedText(node));
			else if ("Description".equals(node.getNodeName()))
				description = XML.unwrappedText(node);
			else if ("Creator".equals(node.getNodeName()))
				creators.add(XML.unwrappedText(node));
			else if ("Subject".equals(node.getNodeName()))
				subjects.add(XML.unwrappedText(node));
			else if ("Publisher".equals(node.getNodeName()))
				publishers.add(XML.unwrappedText(node));
			else if ("Contributor".equals(node.getNodeName()))
				contributors.add(XML.unwrappedText(node));
			else if ("Date".equals(node.getNodeName()))
				dates.add(XML.unwrappedText(node));
			else if ("Type".equals(node.getNodeName()))
				types.add(XML.unwrappedText(node));
			else if ("Source".equals(node.getNodeName()))
				sources.add(XML.unwrappedText(node));
			else if ("Language".equals(node.getNodeName()))
				languages.add(XML.unwrappedText(node));
			else if ("Relation".equals(node.getNodeName()))
				relations.add(XML.unwrappedText(node));
			else if ("Coverage".equals(node.getNodeName()))
				coverages.add(XML.unwrappedText(node));
			else if ("Rights".equals(node.getNodeName()))
				rights.add(XML.unwrappedText(node));
			else if ("resContext".equals(node.getNodeName()))
				contexts.add(XML.unwrappedText(node));
			else if ("resAggregation".equals(node.getNodeName()))
				aggregation = XML.unwrappedText(node);
			else if ("resClass".equals(node.getNodeName()))
				clazz = XML.unwrappedText(node);
			else if ("resLocation".equals(node.getNodeName()))
				locations.add(XML.unwrappedText(node));
		}
	}

	/**
	 * Create resource attributes from constituent attributes.
	 *
	 * According to the Dublin Core, these attributes may be multivalued, but we force
	 * the identifier, title, and description to be singly values in the DTD, so it is
	 * here, too.  Unless otherwise specified, these are all collections of {@link
	 * String}.
	 *
	 * @param profile Owning profile.
	 * @param identifier The unique identifier.
	 * @param title Title of the resource.
	 * @param formats Format of the resource.
	 * @param description Description of the resource.
	 * @param creators Who/what created the resource.
	 * @param subjects Subject matter covered by the resource.
	 * @param publishers Who/what published the resrouce.
	 * @param contributors Who/what contributed to the resource.
	 * @param dates When the resource was created (collection of {@link java.util.Date}.
	 * @param types Type of the resource.
	 * @param sources Source of the resource.
	 * @param languages Language in which the resource is written.
	 * @param relations Relationships to the resource.
	 * @param coverages Coverage of the resource.
	 * @param rights Rights of the resource.
	 * @param contexts Context of the resource.
	 * @param aggregation Aggregation of the resource.
	 * @param clazz Class of the resource.
	 * @param locations Location of the resource.
	 */
	public ResourceAttributes(Profile profile, String identifier, String title, List<String> formats, String description,
		List<String> creators, List<String> subjects, List<String> publishers, List<String> contributors, List<String> dates, List<String> types, List<String> sources,
		List<String> languages, List<String> relations, List<String> coverages, List<String> rights, List<String> contexts, String aggregation, String clazz,
		List<String> locations) {
		this.profile = profile;
		this.identifier = identifier;
		this.title = title;
		this.formats = formats;
		this.description = description;
		this.creators = creators;
		this.subjects = subjects;
		this.publishers = publishers;
		this.contributors = contributors;
		this.dates = dates;
		this.types = types;
		this.sources = sources;
		this.languages = languages;
		this.relations = relations;
		this.coverages = coverages;
		this.rights = rights;
		this.contexts = contexts;
		this.aggregation = aggregation;
		this.clazz = clazz;
		this.locations = locations;
	}

	public int hashCode() {
		return identifier.hashCode();
	}

	public boolean equals(Object rhs) {
		if (rhs == this) return true;
		if (rhs == null || !(rhs instanceof ResourceAttributes)) return false;
		return ((ResourceAttributes) rhs).identifier.equals(identifier);
	}

	public int compareTo(Object rhs) {
		ResourceAttributes obj = (ResourceAttributes) rhs;
		return identifier.compareTo(obj.identifier);
	}

	public String toString() {
		return getClass().getName() + "[identifer=" + identifier + "]";
	}

	public Object clone() {
		Object clone = null;
		try {
			clone = super.clone();
		} catch (CloneNotSupportedException ignored) {}
		return clone;
	}

	/**
	 * Add resource attribute statements to the profile description in an RDF model.
	 *
	 * @param model Model to which the profile belongs.
	 * @param resource Resource description of the profile.
	 * @param profAttr Profile's attributes.
	 */
	void addToModel(Model model, Resource resource, ProfileAttributes profAttr) {
		URI myURI = getURI();

		Utility.addProperty(model, resource, Utility.dcTitle,        title,        profAttr, myURI);
		Utility.addProperty(model, resource, Utility.dcFormat,       formats,      profAttr, myURI);
		Utility.addProperty(model, resource, Utility.dcDescription,  description,  profAttr, myURI);
		Utility.addProperty(model, resource, Utility.dcCreator,      creators,     profAttr, myURI);
		Utility.addProperty(model, resource, Utility.dcSubject,      subjects,     profAttr, myURI);
		Utility.addProperty(model, resource, Utility.dcPublisher,    publishers,   profAttr, myURI);
		Utility.addProperty(model, resource, Utility.dcContributor,  contributors, profAttr, myURI);
		Utility.addProperty(model, resource, Utility.dcDate,         dates,        profAttr, myURI);
		Utility.addProperty(model, resource, Utility.dcType,         types,        profAttr, myURI);
		Utility.addProperty(model, resource, Utility.dcSource,       sources,      profAttr, myURI);
		Utility.addProperty(model, resource, Utility.dcLanguage,     languages,    profAttr, myURI);
		Utility.addProperty(model, resource, Utility.dcRelation,     relations,    profAttr, myURI);
		Utility.addProperty(model, resource, Utility.dcRights,       rights,       profAttr, myURI);
		Utility.addProperty(model, resource, Utility.edmContext,     contexts,     profAttr, myURI);
		Utility.addProperty(model, resource, Utility.edmAggregation, aggregation,  profAttr, myURI);
		Utility.addProperty(model, resource, Utility.edmClass,       clazz,        profAttr, myURI);
		Utility.addProperty(model, resource, Utility.edmLocation,    locations,    profAttr, myURI);
	}

	public URI getURI() {
		String identification;
		if (identifier == null || identifier.length() == 0) {
			if (locations.isEmpty())
				identification = null;
			else
				identification = (String) locations.get(0);
		} else
			identification = identifier;

		return identification == null? UNKNOWN_URI : URI.create(identification);
	}

	/**
	 * Get the identifier.
	 *
	 * @return The identifier.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Set the identifier.
	 *
	 * @param identifier The identifier.
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Get the title.
	 *
	 * @return The title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set the title.
         *
         * @param title The title.
         */
        public void setTitle(String title) {
                this.title = title;
        }


	/**
	 * Get the formats.
	 *
	 * @return The formats.
	 */
	public List<String> getFormats() {
		return formats;
	}

	/**
	 * Set the description.
	 *
	 * @param description The new description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the description.
	 *
	 * @return The description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get the creators.
	 *
	 * @return The creators.
	 */
	public List<String> getCreators() {
		return creators;
	}

	/**
	 * Get the subjects.
	 *
	 * @return The subjects.
	 */
	public List<String> getSubjects() {
		return subjects;
	}

	/**
	 * Get the publishers.
	 *
	 * @return The publishers.
	 */
	public List<String> getPublishers() {
		return publishers;
	}

	/**
	 * Get the contributors.
	 *
	 * @return The contributors.
	 */
	public List<String> getContributors() {
		return contributors;
	}

	/**
	 * Get the dates.
	 *
	 * @return The dates.
	 */
	public List<String> getDates() {
		return dates;
	}

	/**
	 * Get the types.
	 *
	 * @return The types.
	 */
	public List<String> getTypes() {
		return types;
	}

	/**
	 * Get the sources.
	 *
	 * @return The sources.
	 */
	public List<String> getSources() {
		return sources;
	}

	/**
	 * Get the languages.
	 *
	 * @return The languages.
	 */
	public List<String> getLanguages() {
		return languages;
	}

	/**
	 * Get the relations.
	 *
	 * @return The relations.
	 */
	public List<String> getRelations() {
		return relations;
	}

	/**
	 * Get the coverages.
	 *
	 * @return The coverages.
	 */
	public List<String> getCoverages() {
		return coverages;
	}

	/**
	 * Get the rights.
	 *
	 * @return The rights.
	 */
	public List<String> getRights() {
		return rights;
	}

	/**
	 * Get the contexts.
	 *
	 * @return The contexts, a list of {@link String}s.
	 */
	public List<String> getResContexts() {
		return contexts;
	}


	/**
	 * Get the aggregation.
	 *
	 * @return The aggregation.
	 */
	public String getResAggregation() {
		return aggregation;
	}

	/**
	 * Set the aggregation.
	 *
	 * @param aggregation The aggregation.
	 */
	public void setResAggregation(String aggregation) {
		this.aggregation = aggregation;
	}

	/**
	 * Get the class.
	 *
	 * @return The class.
	 */
	public String getResClass() {
		return clazz;
	}

	/**
	 * Set the class.
	 *
	 * @param clazz The class.
	 */
	public void setResClass(String clazz) {
		this.clazz = clazz;
	}

	/**
	 * Get the locations.
	 *
	 * @return Locations, a list of {@link String}s.
	 */
	public List<String> getResLocations() {
		return locations;
	}


	/**
	 * Initialize all the various {@link java.util.List} fields.
	 */
	protected void initializeLists() {
		formats = new ArrayList<String>();
		creators = new ArrayList<String>();
		subjects = new ArrayList<String>();
		publishers = new ArrayList<String>();
		contributors = new ArrayList<String>();
		dates = new ArrayList<String>();
		types = new ArrayList<String>();
		sources = new ArrayList<String>();
		languages = new ArrayList<String>();
		relations = new ArrayList<String>();
		coverages = new ArrayList<String>();
		rights = new ArrayList<String>();
		contexts = new ArrayList<String>();
		locations = new ArrayList<String>();
	}

	/** Profile I describe. */
	protected Profile profile;

	/** Identifer. */
	protected String identifier;

	/** Titles. */
	protected String title;

	/** Formats. */
	protected List<String> formats;

	/** Descriptions. */
	protected String description;

	/** Creators. */
	protected List<String> creators;

	/** Subjects. */
	protected List<String> subjects;

	/** Publishers. */
	protected List<String> publishers;

	/** Contributors. */
	protected List<String> contributors;

	/** Dates. */
	protected List<String> dates;

	/** Types. */
	protected List<String> types;

	/** Sources. */
	protected List<String> sources;

	/** Languages. */
	protected List<String> languages;

	/** Relations. */
	protected List<String> relations;

	/** Coverages. */
	protected List<String> coverages;

	/** Rights. */
	protected List<String> rights;

	/** Contexts, one or more list of {@link String}s. */
	protected List<String> contexts;

	/** Aggregation. */
	protected String aggregation;

	/** Clazz. */
	protected String clazz;

	/** Locations, zero or more {@link String}s. */
	protected List<String> locations;

	/**
	 * Serialize this attributes as an XML node.
	 *
	 * @param doc The document that will own this node.
	 * @return The XML element &lt;resAttributes&gt; representing these attributes.
	 * @throws DOMException If an error occurs creating the XML nodes.
	 */
	public Node toXML(Document doc) throws DOMException {
		Element root = doc.createElement("resAttributes");
		XML.add(root, "Identifier", identifier);
		XML.addNonNull(root, "Title", title);
		XML.add(root, "Format", formats);
		XML.addNonNull(root, "Description", description);
		XML.add(root, "Creator", creators);
		XML.add(root, "Subject", subjects);
		XML.add(root, "Publisher", publishers);
		XML.add(root, "Contributor", contributors);
		XML.add(root, "Date", dates);
		XML.add(root, "Type", types);
		XML.add(root, "Source", sources);
		XML.add(root, "Language", languages);
		XML.add(root, "Relation", relations);
		XML.add(root, "Coverage", coverages);
		XML.add(root, "Rights", rights);
		List<String> contexts = new ArrayList<String>(this.contexts);
		if (contexts.isEmpty()) contexts.add("UNKNOWN");
		XML.add(root, "resContext", contexts);
		XML.addNonNull(root, "resAggregation", aggregation);
		if(clazz==null) clazz="UNKNOWN";
		XML.addNonNull(root, "resClass", clazz);
		XML.add(root, "resLocation", locations);

		return root;
	}

	/**
	 * Create a &lt;resAttributes&gt; document using the profiles DTD.
	 *
	 * @return A &lt;resAttributs&gt; document with the profiles DTD.
	 */
	public static Document createResAttributesDocument() {
		return Profile.createDocument("resAttributes");
	}

	/** URI of a profile whose URI is unknown. */
	private static final URI UNKNOWN_URI = URI.create("urn:eda:profile:UNKNOWN");

        /** Serial version unique ID. */
        static final long serialVersionUID = -4251763559607642607L;
}
