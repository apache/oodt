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

package org.apache.oodt.commons;

import junit.framework.TestCase;

/**
 * Unit test the ConfiguredTestCase class.
 *
 * @author Kelly
 */ 
public class ConfiguredTestCaseTest extends TestCase {
	/** Construct the test case for the ConfiguredTestCaseTest class.
	 *
	 * @param name Case name
	 */
	public ConfiguredTestCaseTest(String name) {
		super(name);
	}

	public void testIt() throws Exception {
		Case c = new Case();
		try {
 			c.setUp();
			assertNotNull(Configuration.configuration);
		} finally {
			c.tearDown();
		}			
	}

	public void testItAgain() throws Exception {
		Case c = new Case();
		try {
			c.setUp();
			assertNotNull(Configuration.configuration);
		} finally {
			c.tearDown();
		}			
	}

	private static class Case extends ConfiguredTestCase {
		Case() {
			super("test");
		}
		public void setUp() throws Exception {
			super.setUp();
		}
		public void tearDown() throws Exception {
			super.tearDown();
		}
	}		
}
