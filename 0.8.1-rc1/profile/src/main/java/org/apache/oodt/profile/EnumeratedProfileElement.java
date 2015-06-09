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

import java.util.ArrayList;
import java.util.List;
import org.apache.oodt.commons.util.XML;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
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

	protected void addElementSpecificProperties(Model model, Resource element, ProfileAttributes profAttr, URI uri) {
		Utility.addProperty(model, element, Utility.edmValue, values, profAttr, uri);
	}

	/** Valid values. */
	protected List values;
}
