// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: EnumeratedProfileElement.java,v 1.4 2006/06/16 17:13:42 kelly Exp $

package jpl.eda.profile;

import java.util.ArrayList;
import java.util.List;
import jpl.eda.util.XML;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import com.hp.hpl.mesa.rdf.jena.model.Model;
import com.hp.hpl.mesa.rdf.jena.model.Resource;
import com.hp.hpl.mesa.rdf.jena.model.RDFException;
import java.net.URI;
import java.util.Iterator;
import org.w3c.dom.Element;

/**
 * Enumerated profile element.
 *
 * Objects of this class are elements of a profile that have several enumerated values.
 *
 * @author Kelly
 */
public class EnumeratedProfileElement extends ProfileElement {
	/**
	 * Create blank profile element belonging to the given profile.
	 */
	public EnumeratedProfileElement(Profile profile) {
		super(profile);
  values = new ArrayList();
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
	 * @param comment Any comments about this element.
	 * @param values Valid values.
	 */
	public EnumeratedProfileElement(Profile profile, String name, String id, String desc, String type, String unit,
		List synonyms, boolean obligation, int maxOccurrence, String comment, List values) {
		super(profile, name, id, desc, type, unit, synonyms, obligation, maxOccurrence, comment);
		if (values.contains(null))
			throw new IllegalArgumentException("Null item in 'values' not allowed for enumerated profile elements");
		this.values = values;
	}

	protected boolean isEnumerated() {
		return true;
	}

	protected void addValues(Node node) throws DOMException {
		if (values == null) return;
		for (Iterator i = values.iterator(); i.hasNext();) {
			Element e = node.getOwnerDocument().createElement("elemValue");
			e.appendChild(node.getOwnerDocument().createCDATASection((String) i.next()));
			node.appendChild(e);
		}
	}

	public String getMinValue() {
		return "";
	}

	public String getMaxValue() {
		return "";
	}

	public List getValues() {
		return values;
	}

	protected void addElementSpecificProperties(Model model, Resource element, ProfileAttributes profAttr, URI uri)
		throws RDFException {
		Utility.addProperty(model, element, Utility.edmValue, values, profAttr, uri);
	}

	/** Valid values. */
	protected List values;
}
