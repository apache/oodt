// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: FalseResult.java,v 1.1.1.1 2004/03/02 20:53:24 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import java.util.Set;
import java.util.Collections;

/**
 * A constant false result.
 *
 * This result never contains any matching elements.
 *
 * <p>Don't create elements of this object.  Just use the {@link #INSTANCE} field, since
 * only one is any program will ever need.
 *
 * @author Kelly
 */
class FalseResult implements Result {
	/**
	 * Construct a false result.
	 */
	private FalseResult() {}

	/** The single false result any program will ever need. */
	public static final FalseResult INSTANCE = new FalseResult();

	public String toString() {
		return "falseResult";
	}

	public Set matchingElements() {
		return Collections.EMPTY_SET;
	}
}
