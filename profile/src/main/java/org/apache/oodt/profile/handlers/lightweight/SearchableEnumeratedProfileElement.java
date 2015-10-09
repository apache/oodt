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

import org.apache.oodt.profile.EnumeratedProfileElement;
import java.util.List;

/**
 * Searchable, enumerated, profile element.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public class SearchableEnumeratedProfileElement extends EnumeratedProfileElement implements SearchableProfileElement {
	public SearchableEnumeratedProfileElement(SearchableProfile profile, String name, String id, String desc, String type,
		String unit, List synonyms, boolean obligation, int maxOccurrence, String comment, List values) {
		super(profile, name, id, desc, type, unit, synonyms, obligation, maxOccurrence, comment, values);
	}

	public Result result(String value, String operator) {
		Result rc = FalseResult.INSTANCE;
		if (operator.equals("EQ") || operator.equals("LE") || operator.equals("GE") ||
			operator.equals("LIKE")) {
			if (values.contains(value))
				rc = new MatchingResult(this);
		} else if (operator.equals("NE") || operator.equals("NOTLIKE")) {
			if (!values.contains(value))
				rc = new MatchingResult(this);
		}
		return rc;
	}
}
