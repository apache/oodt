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

import java.util.List;
import org.apache.oodt.profile.RangedProfileElement;

/**
 * Searchable profile element with a range of valid values.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public class SearchableRangedProfileElement extends RangedProfileElement implements SearchableProfileElement {
	public SearchableRangedProfileElement(SearchableProfile profile, String name, String id, String desc, String type,
		String unit, List synonyms, boolean obligation, int maxOccurrence, String comment, String min, String max) {
		super(profile, name, id, desc, type, unit, synonyms, obligation, maxOccurrence, comment, min, max);
	}

	public Result result(String value, String operator) {
		Result rc = FalseResult.INSTANCE;
		double numeric = Double.parseDouble(value);
		if (operator.equals("EQ") || operator.equals("LIKE")) {
			if (Double.parseDouble(min) <= numeric && numeric <= Double.parseDouble(max)) {
			  rc = new MatchingResult(this);
			}
		} else if (operator.equals("NE") || operator.equals("NOTLIKE")) {
			if (numeric < Double.parseDouble(min) || numeric > Double.parseDouble(max)) {
			  rc = new MatchingResult(this);
			}
		} else if (operator.equals("LT")) {
			if (numeric > Double.parseDouble(min)) {
			  rc = new MatchingResult(this);
			}
		} else if (operator.equals("GT")) {
			if (numeric < Double.parseDouble(max)) {
			  rc = new MatchingResult(this);
			}
		} else if (operator.equals("LE")) {
			if (numeric >= Double.parseDouble(min)) {
			  rc = new MatchingResult(this);
			}
		} else {
			if (numeric <= Double.parseDouble(max)) {
			  rc = new MatchingResult(this);
			}
		}
		return rc;
	}
}
