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

import java.util.Set;
import java.util.Collections;

/**
 * A constant false result.
 *
 * This result never contains any matching elements.
 *
 * <p>Don't create elements of this object.  Just use the {@link #INSTANCE} field, since
 * only one is any program will ever need.
 *
 * @author Kelly
 */
class FalseResult implements Result {
	/**
	 * Construct a false result.
	 */
	private FalseResult() {}

	/** The single false result any program will ever need. */
	public static final FalseResult INSTANCE = new FalseResult();

	public String toString() {
		return "falseResult";
	}

	public Set matchingElements() {
		return Collections.EMPTY_SET;
	}
}
