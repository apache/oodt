// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: UnspecifiedProfileElement.java,v 1.2 2004/11/29 23:04:09 pramirez Exp $

package jpl.eda.profile;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.Node;
import com.hp.hpl.mesa.rdf.jena.model.Model;
import com.hp.hpl.mesa.rdf.jena.model.Resource;
import java.net.URI;

/**
 * A profile element with unspecified values.

 * This is a profile element that is not enumerated (it doesn't have listed values, and so
 * <code>&lt;elemEnumFlag&gt;</code> is false), nor does it have minimum or maximum
 * values.  As such, it always matches any query in which it's involved in an expression.
 *
 * @author Kelly
 */
public class UnspecifiedProfileElement extends ProfileElement implements Serializable, Cloneable {
	/**
	 * Creates a profile element.
	 *
	 * @param profile a <code>Profile</code> value.
	 */
	public UnspecifiedProfileElement(Profile profile) {
		super(profile);
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
	 */
	public UnspecifiedProfileElement(Profile profile, String name, String id, String desc, String type, String unit,
		List synonyms, boolean obligation, int maxOccurrence, String comment) {
		super(profile, name, id, desc, type, unit, synonyms, obligation, maxOccurrence, comment);
	}

	public List getValues() {
		return Collections.EMPTY_LIST;
	}

	public String getMinValue() {
		return "";
	}

	public String getMaxValue() {
		return "";
	}

	protected boolean isEnumerated() {
		return false;
	}

	protected void addValues(Node node) {}

	protected void addElementSpecificProperties(Model model, Resource element, ProfileAttributes profAttr, URI uri) {}

        /** Serial version unique ID. */
        static final long serialVersionUID = -3717582969125927629L;
}
