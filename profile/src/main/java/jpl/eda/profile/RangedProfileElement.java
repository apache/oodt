// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: RangedProfileElement.java,v 1.2 2004/11/29 23:04:08 pramirez Exp $

package jpl.eda.profile;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import jpl.eda.util.XML;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import com.hp.hpl.mesa.rdf.jena.model.RDFException;
import com.hp.hpl.mesa.rdf.jena.model.Model;
import com.hp.hpl.mesa.rdf.jena.model.Resource;
import java.net.URI;

/**
 * Ranged profile element.
 *
 * Objects of this class are elements of a profile that represent a range of values.
 *
 * @author Kelly
 */
public class RangedProfileElement extends ProfileElement {
	/**
	 * Create blank profile attributes belonging to the given profile.
	 */
	public RangedProfileElement(Profile profile) {
		super(profile);
		min = "";
		max = "";
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
	 * @param min Minimum value.
	 * @param max Maximum value.
	 */
	public RangedProfileElement(Profile profile, String name, String id, String desc, String type, String unit, List synonyms,
		boolean obligation, int maxOccurrence, String comment, String min, String max) {
		super(profile, name, id, desc, type, unit, synonyms, obligation, maxOccurrence, comment);
		this.min = min;
		this.max = max;
	}

	protected boolean isEnumerated() {
		return false;
	}
	
	public void setMinValue(String min){
		this.min = min;
		
	}
	
	public void setMaxValue(String max){
		this.max = max;
		
	}

	protected void addValues(Node node) throws DOMException {
		XML.add(node, "elemMinValue", min);
		XML.add(node, "elemMaxValue", max);
	}

	public String getMinValue() {
		return String.valueOf(min);
	}

	public String getMaxValue() {
		return String.valueOf(max);
	}

	public List getValues() {
		return Collections.EMPTY_LIST;
	}

	protected void addElementSpecificProperties(Model model, Resource element, ProfileAttributes profAttr, URI uri)
		throws RDFException {
		Utility.addProperty(model, element, Utility.edmMinValue, min, profAttr, uri);
		Utility.addProperty(model, element, Utility.edmMaxValue, max, profAttr, uri);
	}

	/** Minimum value. */
	protected String min;

	/** Maximum value. */
	protected String max;

        /** Serial version unique ID. */
        static final long serialVersionUID = -5697102597443089753L;
}
