// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: SearchableEnumeratedProfileElement.java,v 1.1.1.1 2004/03/02 20:53:27 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import java.rmi.RemoteException;
import jpl.eda.xmlquery.XMLQuery;
import jpl.eda.profile.EnumeratedProfileElement;
import java.util.List;

/**
 * Searchable, enumerated, profile element.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public class SearchableEnumeratedProfileElement extends EnumeratedProfileElement implements SearchableProfileElement {
	public SearchableEnumeratedProfileElement(SearchableProfile profile, String name, String id, String desc, String type,
		String unit, List synonyms, boolean obligation, int maxOccurrence, String comment, List values) {
		super(profile, name, id, desc, type, unit, synonyms, obligation, maxOccurrence, comment, values);
	}

	public Result result(String value, String operator) {
		Result rc = FalseResult.INSTANCE;
		if (operator.equals("EQ") || operator.equals("LE") || operator.equals("GE") ||
			operator.equals("LIKE")) {
			if (values.contains(value))
				rc = new MatchingResult(this);
		} else if (operator.equals("NE") || operator.equals("NOTLIKE")) {
			if (!values.contains(value))
				rc = new MatchingResult(this);
		}
		return rc;
	}
}
