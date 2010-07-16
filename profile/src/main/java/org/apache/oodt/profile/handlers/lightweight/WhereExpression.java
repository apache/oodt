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
