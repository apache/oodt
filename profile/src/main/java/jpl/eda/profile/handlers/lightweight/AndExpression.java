// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: AndExpression.java,v 1.1.1.1 2004/03/02 20:53:24 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import java.util.Map;

/**
 * An "and" component of a where-expression.
 *
 * This component evaluates a result that's the intersection of the left and right
 * expressions.
 *
 * @author Kelly
 */
class AndExpression implements WhereExpression {
	/**
	 * Construct an "and" where-expression.
	 *
	 * @param lhs Left-hand side
	 * @param rhs Right-hand side.
	 */
	public AndExpression(WhereExpression lhs, WhereExpression rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public Result result(SearchableResourceAttributes resAttr, Map elements) {
		return new Intersection(lhs.result(resAttr, elements), rhs.result(resAttr, elements));
	}

	public WhereExpression simplify() {
		// Simplify the left and right sides, and keep this node.
		lhs = lhs.simplify();
		rhs = rhs.simplify();
		return this;
	}

	public WhereExpression negate() {
		return new OrExpression(lhs.negate(), rhs.negate());
	}

	public String toString() {
		return "and[" + lhs + "," + rhs + "]";
	}

	/** Left-hand side of the expression. */
	private WhereExpression lhs;

	/** Right-hand side of the expression. */
	private WhereExpression rhs;
}
