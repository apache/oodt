// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: SearchableProfileElement.java,v 1.1.1.1 2004/03/02 20:53:27 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import jpl.eda.profile.ProfileElement;

/**
 * A profile element that can be searched.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
interface SearchableProfileElement {
	/** Produce a search result.
	 *
	 * @param value The desired value.
	 * @param operator What operator to use for comparison.
	 * @return A search result.
	 */
	Result result(String value, String operator);
}
