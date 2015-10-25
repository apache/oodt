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
import java.util.Iterator;
import java.util.List;
import org.apache.oodt.profile.ResourceAttributes;
import org.w3c.dom.Element;

/**
 * Resource attributes that can be searched.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public class SearchableResourceAttributes extends ResourceAttributes {
	public SearchableResourceAttributes(SearchableProfile profile, Element node) {
		super(profile, node);
	}

	/**
	 * Produce a search result.
	 *
	 * @param value Desired value.
	 * @param operator What operator to use for comparison.
	 * @return a <code>Result</code> value.
	 */
	public Result result(String name, String value, String operator) {
		if ("Identifier".equals(name))
			return computeResult(identifier, value, operator);
		else if ("Title".equals(name))
			return computeResult(title, value, operator);
		else if ("Format".equals(name))
			return computeResult(formats, value, operator);
		else if ("Description".equals(name))
			return computeResult(description, value, operator);
		else if ("Creator".equals(name))
			return computeResult(creators, value, operator);
		else if ("Subject".equals(name))
			return computeResult(subjects, value, operator);
		else if ("Publisher".equals(name))
			return computeResult(publishers, value, operator);
		else if ("Contributor".equals(name))
			return computeResult(contributors, value, operator);
		else if ("Date".equals(name))
			return computeResult(dates, value, operator);
		else if ("Type".equals(name))
			return computeResult(types, value, operator);
		else if ("Source".equals(name))
			return computeResult(sources, value, operator);
		else if ("Language".equals(name))
			return computeResult(languages, value, operator);
		else if ("Relation".equals(name))
			return computeResult(relations, value, operator);
		else if ("Coverage".equals(name))
			return computeResult(coverages, value, operator);
		else if ("Rights".equals(name))
			return computeResult(rights, value, operator);
		else if ("resContext".equals(name))
			return computeResult(contexts, value, operator);
		else if ("resClass".equals(name))
			return computeResult(clazz, value, operator);
		else if ("resLocation".equals(name))
			return computeResult(locations, value, operator);
		else
			throw new IllegalArgumentException("Unknown attribute \"" + name + "\"");
	}

	private Result computeResult(String a, String b, String op) {
		int c = a.compareTo(b);
		boolean t;
		if ("EQ".equals(op) || "LIKE".equals(op))
			t = c == 0;
		else if ("GE".equals(op))
			t = c >= 0;
		else if ("GT".equals(op))
			t = c > 0;
		else if ("LE".equals(op))
			t = c <= 0;
		else if ("LT".equals(op))
			t = c < 0;
		else if ("NE".equals(op) || "NOTLIKE".equals(op))
			t = c != 0;
		else
			throw new IllegalArgumentException("Unknown relational operator \"" + op + "\"");
		if (t)
			return new MatchingResult(new HashSet(profile.getProfileElements().values()));
		else
			return FalseResult.INSTANCE;
	}

	private Result computeResult(List a, String b, String op) {		
		if (a == null || a.isEmpty()) return FalseResult.INSTANCE;

		Result f = FalseResult.INSTANCE;
		Result t = new MatchingResult(new HashSet(profile.getProfileElements().values()));
		Result rc = f;
		if ("EQ".equals(op) || "LIKE".equals(op))
			if (a.contains(b)) rc = t;
		else if ("NE".equals(op)  || "NOTLIKE".equals(op))
			if (!a.contains(b)) rc = t;
		else if ("LT".equals(op) || "GT".equals(op) || "LE".equals(op) || "GE".equals(op)) {
			  for (Object anA : a) {
				String value = (String) anA;
				rc = computeResult(value, b, op);
				if (rc != f) {
				  break;
				}
			  }
		} else throw new IllegalArgumentException("Unknown relational operator \"" + op + "\"");
		return rc;
	}


}
