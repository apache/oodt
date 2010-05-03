// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: SearchableUnspecifiedProfileElement.java,v 1.1.1.1 2004/03/02 20:53:28 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import java.util.List;
import jpl.eda.profile.UnspecifiedProfileElement;

/**
 * Searchable profile element with unspecified values.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public class SearchableUnspecifiedProfileElement extends UnspecifiedProfileElement implements SearchableProfileElement {
	public SearchableUnspecifiedProfileElement(SearchableProfile profile, String name, String id, String desc, String type,
		String unit, List synonyms, boolean obligation, int maxOccurrence, String comment) {
		super(profile, name, id, desc, type, unit, synonyms, obligation, maxOccurrence, comment);
	}

	public Result result(String value, String operator) {
		return new MatchingResult(this);
	}
}
