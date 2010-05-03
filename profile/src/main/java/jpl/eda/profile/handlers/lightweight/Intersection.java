// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Intersection.java,v 1.1.1.1 2004/03/02 20:53:24 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import java.util.Set;
import jpl.eda.profile.ProfileElement;

/**
 * Intersection of the two sets is the result.
 *
 * This result takes the intersection of two other results.
 *
 * @author Kelly
 */
class Intersection implements Result {
	/**
	 * Construct an intersection.
	 *
	 * @param lhs Left-hand side
	 * @param rhs Right-hand side.
	 */
	public Intersection(Result lhs, Result rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public Set matchingElements() {
		Set intersection = ProfileElement.profiles(lhs.matchingElements());
		intersection.retainAll(ProfileElement.profiles(rhs.matchingElements()));
		Set rc = ProfileElement.elements(intersection, lhs.matchingElements());
		rc.addAll(ProfileElement.elements(intersection, rhs.matchingElements()));
		return rc;
	}

	public String toString() {
		return "intersection[" + lhs + "," + rhs + "]";
	}

	/** Left-hand side of the result. */
	private Result lhs;

	/** Right-hand side of the result. */
	private Result rhs;
}
