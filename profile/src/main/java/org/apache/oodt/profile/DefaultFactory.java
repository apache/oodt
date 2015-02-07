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

import java.util.List;
import org.w3c.dom.Element;

/**
 * Default factory for profile objects.
 *
 * This factory creates the typical profile objects {@link RangedProfileElement}, {@link
 * UnspecifiedProfileElement}, {@link EnumeratedProfileElement}, {@link Profile}, {@link
 * ProfileAttributes}, and {@link ResourceAttributes}.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
class DefaultFactory implements ObjectFactory {
	public RangedProfileElement createRangedProfileElement(Profile profile, String name, String id, String desc, String type,
		String unit, List synonyms, boolean obligation, int maxOccurrence, String comments, String min, String max) {
		return new RangedProfileElement(profile, name, id, desc, type, unit, synonyms, obligation, maxOccurrence,
			comments, min, max);
	}
	public UnspecifiedProfileElement createUnspecifiedProfileElement(Profile profile, String name, String id, String desc,
		String type, String unit, List synonyms, boolean obligation, int maxOccurrence, String comments) {
		return new UnspecifiedProfileElement(profile, name, id, desc, type, unit, synonyms, obligation, maxOccurrence,
			comments);
	}
	public EnumeratedProfileElement createEnumeratedProfileElement(Profile profile, String name, String id, String desc,
		String type, String unit, List synonyms, boolean obligation, int maxOccurrence, String comments, List values) {
		return new EnumeratedProfileElement(profile, name, id, desc, type, unit, synonyms, obligation, maxOccurrence,
			comments, values);
	}
	public Profile createProfile(Element node) {
		return new Profile(node, this);
	}
	public ProfileAttributes createProfileAttributes(Element node) {
		return new ProfileAttributes(node);
	}
	public ResourceAttributes createResourceAttributes(Profile profile, Element node) {
		return new ResourceAttributes(profile, node);
	}
}
