// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Union.java,v 1.1.1.1 2004/03/02 20:53:28 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import java.util.Set;
import jpl.eda.profile.ProfileElement;

/**
 * A union of matching elements.
 *
 * @author Kelly
 */
class Union implements Result {
	/**
	 * Construct a union.
	 *
	 * @param lhs Left-hand side
	 * @param rhs Right-hand side.
	 */
	public Union(Result lhs, Result rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public Set matchingElements() {
		Set union = ProfileElement.profiles(lhs.matchingElements());
		union.addAll(ProfileElement.profiles(rhs.matchingElements()));
		Set rc = ProfileElement.elements(union, lhs.matchingElements());
		rc.addAll(ProfileElement.elements(union, rhs.matchingElements()));
		return rc;
	}

	public String toString() {
		return "union[" + lhs + "," + rhs + "]";
	}

	/** Left-hand side of the result. */
	private Result lhs;

	/** Right-hand side of the result. */
	private Result rhs;
}
