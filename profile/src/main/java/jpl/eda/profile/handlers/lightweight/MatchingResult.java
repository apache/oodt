// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: MatchingResult.java,v 1.1.1.1 2004/03/02 20:53:24 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import java.util.Collections;
import java.util.Set;
import jpl.eda.profile.ProfileElement;

/**
 * A matching result.
 *
 * This result matches profile elements.
 *
 * @author Kelly
 */
class MatchingResult implements Result {
	/**
	 * Construct a matching result.
	 *
	 * @param element The profile element that matches.
	 */
	public MatchingResult(ProfileElement element) {
		this.elements = Collections.singleton(element);
	}

	/**
	 * Construct a matching result.
	 *
	 * @param elements The profile elements that all match.
	 */
	public MatchingResult(Set elements) {
		this.elements = elements;
	}

	public Set matchingElements() {
		return elements;
	}

	public String toString() {
		return "match[" + elements + "]";
	}

	/** The matching elements. */
	private Set elements;
}
