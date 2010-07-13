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

import java.util.Set;
import jpl.eda.profile.ProfileElement;

/**
 * Intersection of the two sets is the result.
 *
 * This result takes the intersection of two other results.
 *
 * @author Kelly
 */
class Intersection implements Result {
	/**
	 * Construct an intersection.
	 *
	 * @param lhs Left-hand side
	 * @param rhs Right-hand side.
	 */
	public Intersection(Result lhs, Result rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public Set matchingElements() {
		Set intersection = ProfileElement.profiles(lhs.matchingElements());
		intersection.retainAll(ProfileElement.profiles(rhs.matchingElements()));
		Set rc = ProfileElement.elements(intersection, lhs.matchingElements());
		rc.addAll(ProfileElement.elements(intersection, rhs.matchingElements()));
		return rc;
	}

	public String toString() {
		return "intersection[" + lhs + "," + rhs + "]";
	}

	/** Left-hand side of the result. */
	private Result lhs;

	/** Right-hand side of the result. */
	private Result rhs;
}
