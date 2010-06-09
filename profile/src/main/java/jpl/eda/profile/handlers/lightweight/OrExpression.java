/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
