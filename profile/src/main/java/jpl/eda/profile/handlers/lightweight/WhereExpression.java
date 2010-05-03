// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: WhereExpression.java,v 1.1.1.1 2004/03/02 20:53:28 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import java.util.Map;

/**
 * A where-expression.
 *
 * @author Kelly
 */
interface WhereExpression {
	/**
	 * Compute the result tree of this where expression based on the map of profile elements.
	 *
	 * @param resAttr Resource attributes to check.
	 * @param elements Map from {@link String} element name to {@link ProfileElement}.
	 * @return A result tree that when evaluated yields matching {@link ProfileElement}s.
	 */
	Result result(SearchableResourceAttributes resAttr, Map elements);

	/**
	 * Return a simplified version of this expression.
	 *
	 * Our result generation can't handle "NOT" expressions (like "not (blah < 3)")
	 * since we can't do set inversion, so we simplify the expression by removing all
	 * "NOT" nodes.
	 *
	 * @return An equivalent expression without negative nodes.
	 */
	WhereExpression simplify();

	/**
	 * Negate this expression.
	 *
	 * @return The negation of this expression.
	 */
	WhereExpression negate();
}
