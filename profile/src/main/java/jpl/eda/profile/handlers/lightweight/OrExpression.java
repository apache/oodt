// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: OrExpression.java,v 1.1.1.1 2004/03/02 20:53:27 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import java.util.Map;

/**
 * An "or" component of a where-expression.
 *
 * This component yields a result that's the union of the results of the left and right
 * sides.
 *
 * @author Kelly
 */
class OrExpression implements WhereExpression {
	/**
	 * Construct an "or" where-expression.
	 *
	 * @param lhs Left-hand side
	 * @param rhs Right-hand side.
	 */
	public OrExpression(WhereExpression lhs, WhereExpression rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public Result result(SearchableResourceAttributes resAttr, Map elements) {
		return new Union(lhs.result(resAttr, elements), rhs.result(resAttr, elements));
	}

	public WhereExpression simplify() {
		// Simplify the left and right sides, and keep this node.
		lhs = lhs.simplify();
		rhs = rhs.simplify();
		return this;
	}

	public WhereExpression negate() {
		return new AndExpression(lhs.negate(), rhs.negate());
	}

	public String toString() {
		return "or[" + lhs + "," + rhs + "]";
	}

	/** Left-hand side of the expression. */
	private WhereExpression lhs;

	/** Right-hand side of the expression. */
	private WhereExpression rhs;
}
