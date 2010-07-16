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
