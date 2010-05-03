// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ConstantExpression.java,v 1.1.1.1 2004/03/02 20:53:24 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import java.util.HashSet;
import java.util.Map;

/**
 * A constant expression.
 *
 * This expression yields either all the elements (if it's a constant true) or none (if
 * it's a constant false).
 *
 * @author Kelly
 */
class ConstantExpression implements WhereExpression {
	/**
	 * Construct a constant where-expression.
	 *
	 * @param value Truth value of this expression.
	 */
	public ConstantExpression(boolean value) {
		this.value = value;
	}

	public Result result(SearchableResourceAttributes resAttr, Map elements) {
		if (value)
			return new MatchingResult(new HashSet(elements.values()));
		else
			return FalseResult.INSTANCE;
	}

	public WhereExpression simplify() {
		// Can't get simpler than this.
		return this;
	}

	public WhereExpression negate() {
		return new ConstantExpression(!value);
	}

	public String toString() {
		return "constant[" + value + "]";
	}

	/** Truth value. */
	private boolean value;
}
