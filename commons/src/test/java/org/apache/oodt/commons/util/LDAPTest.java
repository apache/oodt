// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.commons.util;

import junit.framework.*;

/** Unit test the {@link LDAP} class.
 *
 * @author Kelly
 */ 
public class LDAPTest extends TestCase {
	/** Construct the test case for the {@link LDAP} class. */
	public LDAPTest(String name) {
		super(name);
	}

	/** Test the {@link LDAP#toLDAPString} method. */
	public void testToLDAPString() {
		String str = "Hello (World), \\How are you*?";
		String result = LDAP.toLDAPString(str);
		assertEquals("Hello \\28World\\29, \\5cHow are you\\2a?", result);
	}
}

