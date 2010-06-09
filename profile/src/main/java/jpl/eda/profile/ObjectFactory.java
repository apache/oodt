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

import java.util.List;
import org.w3c.dom.Element;

/**
 * Factory to create profile-related objects.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public interface ObjectFactory {
	/**
	 * Create a ranged profile element.
	 *
	 * @param profile a <code>Profile</code> value.
	 * @param name a <code>String</code> value.
	 * @param id a <code>String</code> value.
	 * @param desc a <code>String</code> value.
	 * @param type a <code>String</code> value.
	 * @param unit a <code>String</code> value.
	 * @param synonyms a <code>List</code> value.
	 * @param obligation a <code>boolean</code> value.
	 * @param maxOccurrence an <code>int</code> value.
	 * @param comments a <code>String</code> value.
	 * @param min a <code>double</code> value.
	 * @param max a <code>double</code> value.
	 * @return a <code>RangedProfileElement</code> value.
	 */
	RangedProfileElement createRangedProfileElement(Profile profile, String name, String id, String desc, String type,
		String unit, List synonyms, boolean obligation, int maxOccurrence, String comments, String min, String max);

	/**
	 * Create a profile element with unspecified values.
	 *
	 * @param profile a <code>Profile</code> value.
	 * @param name a <code>String</code> value.
	 * @param id a <code>String</code> value.
	 * @param desc a <code>String</code> value.
	 * @param type a <code>String</code> value.
	 * @param unit a <code>String</code> value.
	 * @param synonyms a <code>List</code> value.
	 * @param obligation a <code>boolean</code> value.
	 * @param maxOccurrence an <code>int</code> value.
	 * @param comments a <code>String</code> value.
	 * @return an <code>UnspecifiedProfileElement</code> value.
	 */
	UnspecifiedProfileElement createUnspecifiedProfileElement(Profile profile, String name, String id, String desc, String type,
		String unit, List synonyms, boolean obligation, int maxOccurrence, String comments);

	/**
	 * Create a profile element with enumerated values.
	 *
	 * @param profile a <code>Profile</code> value.
	 * @param name a <code>String</code> value.
	 * @param id a <code>String</code> value.
	 * @param desc a <code>String</code> value.
	 * @param type a <code>String</code> value.
	 * @param unit a <code>String</code> value.
	 * @param synonyms a <code>List</code> value.
	 * @param obligation a <code>boolean</code> value.
	 * @param maxOccurrence an <code>int</code> value.
	 * @param comments a <code>String</code> value.
	 * @param values a <code>List</code> value.
	 * @return an <code>Enumerated</code> value.
	 */
	EnumeratedProfileElement createEnumeratedProfileElement(Profile profile, String name, String id, String desc, String type,
		String unit, List synonyms, boolean obligation, int maxOccurrence, String comments, List values);

	/**
	 * Create a profile from a DOM node.
	 *
	 * @param node an <code>Element</code> value.
	 * @return a <code>Profile</code> value.
	 */
	Profile createProfile(Element node);

	/**
	 * Create profile attributes from a DOM node.
	 *
	 * @param node an <code>Element</code> value.
	 * @return a <code>ProfileAttributes</code> value.
	 */
	ProfileAttributes createProfileAttributes(Element node);

	/**
	 * Create resource attributes from a DOM node.
	 *
	 * @param profile Owning profile.
	 * @param node an <code>Element</code> value.
	 * @return a <code>ResourceAttributes</code> value.
	 */
	ResourceAttributes createResourceAttributes(Profile profile, Element node);
}
