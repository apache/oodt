// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Result.java,v 1.1.1.1 2004/03/02 20:53:27 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import java.util.Set;

/**
 * A result of a profile match.
 *
 * @author Kelly
 */
interface Result {
	/**
	 * Get the set of matching elements.
	 *
	 * If the set is empty, it means no elements matched.
	 */
	Set matchingElements();
}
