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


package org.apache.oodt.profile.handlers.lightweight;

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
