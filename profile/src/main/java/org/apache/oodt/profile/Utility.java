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

package org.apache.oodt.profile;

import org.apache.oodt.commons.Configuration;

import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;

import java.util.Collection;

import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Seq;

import java.util.List;
import java.net.URI;

/**
 * Profile utilities.
 *
 * Utility methods for profiles.
 *
 * @author Kelly
 */
class Utility {
	/**
	 * Don't call because this is a utiliy class.
	 */
	private Utility() {
		throw new IllegalStateException("Utility class");
	}

	static void addProperty(Model model, Resource resource, Property property, Object value, ProfileAttributes profAttr,
		URI uri) {

		if (value == null || value.toString().length() == 0) return;

		Object obj;
		if (value instanceof Collection) {
			Collection<?> collection = (Collection<?>) value;
			if (collection.isEmpty()) return;
			Bag bag = model.createBag(uri + "_" + property.getLocalName() + "_bag");
		  for (Object aCollection : collection) {
			bag.add(aCollection);
		  }
			resource.addProperty(property, bag);
			obj = bag;
		} else {
			resource.addProperty(property, value.toString());
			obj = value;
		}

		Resource reification = model.createResource(uri + "_" + property.getLocalName() + "_reification");

		reification.addProperty(rdfSubject, resource);
		reification.addProperty(rdfPredicate, property);
		reification.addProperty(rdfObject, obj.toString());
		reification.addProperty(rdfType, rdfStatement);

		addPotentiallyNullReifiedStatement(reification, edmID, profAttr.getVersion());
		addPotentiallyNullReifiedStatement(reification, edmVersion, profAttr.getVersion());
		addPotentiallyNullReifiedStatement(reification, edmType, profAttr.getType());
		addPotentiallyNullReifiedStatement(reification, edmStatus, profAttr.getStatusID());
		addPotentiallyNullReifiedStatement(reification, edmSecurity, profAttr.getSecurityType());
		addPotentiallyNullReifiedStatement(reification, edmParent, profAttr.getParent());
		addPotentiallyNullReifiedStatement(reification, edmRegAuth, profAttr.getRegAuthority());

		List<?> children = profAttr.getChildren();
		if (!children.isEmpty()) {
			Bag bag = model.createBag(uri + "_" + property.getLocalName() + "_childrenBag");
		  for (Object aChildren : children) {
			bag.add(aChildren);
		  }
			reification.addProperty(edmChild, bag);
		}

		List<?> revNotes = profAttr.getRevisionNotes();
		if (!revNotes.isEmpty()) {
			Seq seq = model.createSeq(uri + "_" + property.getLocalName() + "_revNotesSeq");
		  for (Object revNote : revNotes) {
			seq.add(revNote);
		  }
			reification.addProperty(edmRevNote, seq);
		}
	}

	private static void addPotentiallyNullReifiedStatement(Resource reification, Property property, Object value) {
		if (value == null || value.toString().length() == 0) return;
		reification.addProperty(property, value.toString());
	}


	/** Dublin core's title */
	static Property dcTitle;
	static Property dcSubject;
	static Property dcDescription;
	static Property dcPublisher;
	static Property dcContributor;
	static Property dcCreator;
	static Property dcDate;
	static Property dcType;
	static Property dcFormat;
	static Property dcSource;
	static Property dcLanguage;
	static Property dcRelation;
	static Property dcCoverage;
	static Property dcRights;

	/** A resource that is an RDF statement. */
	static Resource rdfStatement;

	/** A property that is an RDF subject of a statement. */
	static Property rdfSubject;

	/** A property that is an RDF predicate of a statement. */
	static Property rdfPredicate;

	/** A property that is an object of a statement. */
	static Property rdfObject;

	/** A property that names the type of an RDF resource. */
	static Property rdfType;

	private static final String RDF_SYNTAX_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	private static final String DC_NS = "http://purl.org/dc/elements/1.1/";

	private static Property edmID;
	private static Property edmVersion;
	private static Property edmType;
	private static Property edmStatus;
	private static Property edmSecurity;
	private static Property edmParent;
	private static Property edmChild;
	private static Property edmRegAuth;
	private static Property edmRevNote;

	static Property edmElement;
	static Property edmContext;
	static Property edmAggregation;
	static Property edmClass;
	static Property edmLocation;
	static Property edmElemID;
	static Property edmDescription;
	static Property edmElemType;
	static Property edmUnit;
	static Property edmSynonym;
	static Property edmObligation;
	static Property edmMaxOccurrence;
	static Property edmComment;
	static Property edmMinValue;
	static Property edmMaxValue;
	static Property edmValue;

	/**
	 * Initialize this class.
	 */
	static {
		try {
			@SuppressWarnings("unused")
      Configuration config = Configuration.getConfiguration();
			String profNS = System.getProperty("jpl.rdf.ns", "http://oodt.jpl.nasa.gov/grid-profile/rdfs/prof.rdf");
			Model model = ModelFactory.createDefaultModel();

			rdfStatement     = model.createResource(RDF_SYNTAX_NS + "Statement");

			rdfSubject       = model.createProperty(RDF_SYNTAX_NS, "subject");
			rdfPredicate     = model.createProperty(RDF_SYNTAX_NS, "predicate");
			rdfObject        = model.createProperty(RDF_SYNTAX_NS, "object");
			rdfType          = model.createProperty(RDF_SYNTAX_NS, "type");

			dcTitle          = model.createProperty(DC_NS, "title");
			dcCreator        = model.createProperty(DC_NS, "creator");
			dcSubject        = model.createProperty(DC_NS, "subject");
			dcDescription    = model.createProperty(DC_NS, "description");
			dcPublisher      = model.createProperty(DC_NS, "publisher");
			dcContributor    = model.createProperty(DC_NS, "contributor");
			dcDate           = model.createProperty(DC_NS, "date");
			dcType           = model.createProperty(DC_NS, "type");
			dcFormat         = model.createProperty(DC_NS, "format");
			dcSource         = model.createProperty(DC_NS, "source");
			dcLanguage       = model.createProperty(DC_NS, "language");
			dcRelation       = model.createProperty(DC_NS, "relation");
			dcCoverage       = model.createProperty(DC_NS, "coverage");
			dcRights         = model.createProperty(DC_NS, "rights");

			edmID            = model.createProperty(profNS, "id");
			edmVersion       = model.createProperty(profNS, "version");
			edmType          = model.createProperty(profNS, "type");
			edmStatus        = model.createProperty(profNS, "status");
			edmSecurity      = model.createProperty(profNS, "security");
			edmParent        = model.createProperty(profNS, "parent");
			edmChild         = model.createProperty(profNS, "child");
			edmRegAuth       = model.createProperty(profNS, "regAuth");
			edmRevNote       = model.createProperty(profNS, "revNote");
			edmElement       = model.createProperty(profNS, "element");
			edmContext       = model.createProperty(profNS, "context");
			edmClass         = model.createProperty(profNS, "class");
			edmAggregation   = model.createProperty(profNS, "aggregation");
			edmLocation      = model.createProperty(profNS, "location");

			edmElemID        = model.createProperty(profNS, "edmElemID");
			edmDescription   = model.createProperty(profNS, "edmDescription");
			edmElemType      = model.createProperty(profNS, "edmElemType");
			edmUnit          = model.createProperty(profNS, "edmUnit");
			edmSynonym       = model.createProperty(profNS, "edmSynonym");
			edmObligation    = model.createProperty(profNS, "edmObligation");
			edmMaxOccurrence = model.createProperty(profNS, "edmMaxOccurrence");
			edmComment       = model.createProperty(profNS, "edmComment");
			edmMinValue      = model.createProperty(profNS, "edmMinValue");
			edmMaxValue      = model.createProperty(profNS, "edmMaxValue");
			edmValue         = model.createProperty(profNS, "edmValue");

		} catch (IOException ex) {
			System.err.println("Fatal I/O error prevents reading of configuration: " + ex.getMessage());
			System.exit(1);
		} catch (SAXParseException ex) {
			System.err.println("Fatal error parsing file (public ID \"" + ex.getPublicId() + "\", system ID \""
				+ ex.getSystemId() + "\"), line " + ex.getLineNumber() + " column " + ex.getColumnNumber()
				+ ": " + ex.getMessage());
			System.exit(1);
		} catch (SAXException ex) {
			System.err.println("Fatal SAX exception: " + ex.getMessage() + (ex.getException() == null? ""
				: " (embedded exception " + ex.getException().getClass().getName() + ": "
				+ ex.getException().getMessage() + ")"));
			System.exit(1);
		}
	}
}
