// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: SearchableRangedProfileElement.java,v 1.1.1.1 2004/03/02 20:53:28 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import java.util.List;
import jpl.eda.profile.RangedProfileElement;

/**
 * Searchable profile element with a range of valid values.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public class SearchableRangedProfileElement extends RangedProfileElement implements SearchableProfileElement {
	public SearchableRangedProfileElement(SearchableProfile profile, String name, String id, String desc, String type,
		String unit, List synonyms, boolean obligation, int maxOccurrence, String comment, String min, String max) {
		super(profile, name, id, desc, type, unit, synonyms, obligation, maxOccurrence, comment, min, max);
	}

	public Result result(String value, String operator) {
		Result rc = FalseResult.INSTANCE;
		double numeric = Double.parseDouble(value);
		if (operator.equals("EQ") || operator.equals("LIKE")) {
			if (Double.parseDouble(min) <= numeric && numeric <= Double.parseDouble(max)) rc = new MatchingResult(this);
		} else if (operator.equals("NE") || operator.equals("NOTLIKE")) {
			if (numeric < Double.parseDouble(min) || numeric > Double.parseDouble(max)) rc = new MatchingResult(this);
		} else if (operator.equals("LT")) {
			if (numeric > Double.parseDouble(min)) rc = new MatchingResult(this);
		} else if (operator.equals("GT")) {
			if (numeric < Double.parseDouble(max)) rc = new MatchingResult(this);
		} else if (operator.equals("LE")) {
			if (numeric >= Double.parseDouble(min)) rc = new MatchingResult(this);
		} else {
			if (numeric <= Double.parseDouble(max)) rc = new MatchingResult(this);
		}
		return rc;
	}
}
