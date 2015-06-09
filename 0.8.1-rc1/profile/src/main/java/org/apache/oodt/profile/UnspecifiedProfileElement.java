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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
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
