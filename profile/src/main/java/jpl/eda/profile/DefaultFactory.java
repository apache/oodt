// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: DefaultFactory.java,v 1.1.1.1 2004/03/02 20:53:06 kelly Exp $

package jpl.eda.profile;

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
