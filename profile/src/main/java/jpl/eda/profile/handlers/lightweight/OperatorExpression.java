// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: OperatorExpression.java,v 1.1.1.1 2004/03/02 20:53:27 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A operator component of a where-expression.
 *
 * This component applies an operator, like &lt;, =, &ge;, etc., to a element name and a
 * literal value.
 *
 * @author Kelly
 */
class OperatorExpression implements WhereExpression {
	/**
	 * Construct an operator where-expression.
	 *
	 * @param value Value to which to compare.
	 * @param name Name of element to match.
	 * @param operator What operator to use
	 */
	public OperatorExpression(String value, String name, String operator) {
		if (Arrays.binarySearch(VALID_OPERATORS, operator) < 0)
			throw new IllegalArgumentException("Invalid operator \"" + operator + "\"");

		this.name = name;
		this.value = value;
		this.operator = operator;
	}

	public Result result(SearchableResourceAttributes resAttr, Map elements) {
		// Is it a "from" relation or a "where" relation?
		if (FROM_TOKENS.contains(name)) {
			// "From."  So let the resource attributes take care of it.
			return resAttr.result(name, value, operator);
		} else if (RESOURCE_ATTRIBUTES.contains(name)) {
			// Resource attributes in "Where"
			// let the resource attributes take care of it.
			return resAttr.result(name, value, operator);
		} else {
			// "Where."  See if our keyword is present in the given set.
			SearchableProfileElement element = (SearchableProfileElement) elements.get(name);

			// Nope.  We can only give a false result.
			if (element == null) return FalseResult.INSTANCE;

			// Yep.  Ask the element to yield the result.
			return element.result(value, operator);
		}
	}

	public WhereExpression simplify() {
		// Can't get simpler than this.
		return this;
	}

	public WhereExpression negate() {
		String negated;
		if (operator.equals("EQ"))
			negated = "NE";
		else if (operator.equals("NE"))
			negated = "EQ";
		else if (operator.equals("LT"))
			negated = "GE";
		else if (operator.equals("GT"))
			negated = "LE";
		else if (operator.equals("LE"))
			negated = "GT";
		else if (operator.equals("LIKE"))
			negated = "NE";
		else if (operator.equals("NOTLIKE"))
			negated = "EQ";
		else
			negated = "LT";
		return new OperatorExpression(value, name, negated);
	}

	public String toString() {
		return "operator[" + name + " " + operator + " " + value + "]";
	}

	/** Name of element to match. */
	private String name;

	/** Value to compare. */
	private String value;

	/** Operator to use. */
	private String operator;

	/**
	 * Valid operators.
	 *
	 * <strong>KEEP THIS IN ORDER!</strong>  We binary search on them!
	 */
	private static final String[] VALID_OPERATORS = {
		"EQ", "GE", "GT", "LE", "LIKE", "LT", "NE", "NOTLIKE"
	};

	/**
	 * Relational operations performed on the "from" part of a query instead of the
	 * "where" part.  These work off the resource attributes rather than the profile
	 * elements.
	 */
	private static final Set FROM_TOKENS = new HashSet(Arrays.asList(jpl.eda.xmlquery.XMLQuery.FROM_TOKENS));

	/**
	 * Resource attributes that are parsed into WHERE part of the xmlquery
	 */
	private static final Set RESOURCE_ATTRIBUTES = new HashSet(Arrays.asList(
		new String[]{
		"Identifier", "Title", "Format", "Description", "Creator", 
		"Subject", "Publisher", "Contributor", "Date", "Type", 
		"Source", "Language", "Relation", "Coverage", "Rights", 
		"resContext", "resClass", "resLocation" }));
}
