// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: NotExpression.java,v 1.1.1.1 2004/03/02 20:53:25 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import java.util.Map;

/**
 * A "not" component of a where-expression.
 *
 * This expression component negates its given operand.
 *
 * @author Kelly
 */
class NotExpression implements WhereExpression {
	/**
	 * Construct a "not" where-expression.
	 *
	 * @param operand The operand.
	 */
	public NotExpression(WhereExpression operand) {
		this.operand = operand;
	}

	public Result result(SearchableResourceAttributes resAttr, Map elements) {
		throw new IllegalStateException("Not-nodes must be simplified out since they cannot yield results");
	}

	public WhereExpression simplify() {
		// Negate my operand right now, then simplify it, and drop myself out.
		return operand.negate().simplify();
	}

	public WhereExpression negate() {
		return new NotExpression(operand.negate());
	}

	public String toString() {
		return "not[" + operand + "]";
	}

	/** Expression to negate. */
	private WhereExpression operand;
}
