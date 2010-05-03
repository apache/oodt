// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: SearchableObjectFactory.java,v 1.1.1.1 2004/03/02 20:53:27 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import java.util.List;
import jpl.eda.profile.EnumeratedProfileElement;
import jpl.eda.profile.ObjectFactory;
import jpl.eda.profile.Profile;
import jpl.eda.profile.ProfileAttributes;
import jpl.eda.profile.RangedProfileElement;
import jpl.eda.profile.ResourceAttributes;
import jpl.eda.profile.UnspecifiedProfileElement;
import org.w3c.dom.Element;

/**
 * Factory that yields searchable profile objects.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
class SearchableObjectFactory implements ObjectFactory {
	public RangedProfileElement createRangedProfileElement(Profile profile, String name, String id, String desc, String type,
		String unit, List synonyms, boolean obligation, int maxOccurrence, String comments, String min, String max) {
		return new SearchableRangedProfileElement((SearchableProfile) profile, name, id, desc, type, unit, synonyms,
			obligation, maxOccurrence, comments, min, max);
	}
	public UnspecifiedProfileElement createUnspecifiedProfileElement(Profile profile, String name, String id, String desc,
		String type, String unit, List synonyms, boolean obligation, int maxOccurrence, String comments) {
		return new SearchableUnspecifiedProfileElement((SearchableProfile) profile, name, id, desc, type, unit, synonyms,
			obligation, maxOccurrence, comments);
	}
	public EnumeratedProfileElement createEnumeratedProfileElement(Profile profile, String name, String id, String desc,
		String type, String unit, List synonyms, boolean obligation, int maxOccurrence, String comments, List values) {
		return new SearchableEnumeratedProfileElement((SearchableProfile) profile, name, id, desc, type, unit, synonyms,
			obligation, maxOccurrence, comments, values);
	}
	public Profile createProfile(Element node) {
		return new SearchableProfile(node, this);
	}
	public ProfileAttributes createProfileAttributes(Element node) {
		return new ProfileAttributes(node);
	}
	public ResourceAttributes createResourceAttributes(Profile profile, Element node) {
		return new SearchableResourceAttributes((SearchableProfile) profile, node);
	}
}
