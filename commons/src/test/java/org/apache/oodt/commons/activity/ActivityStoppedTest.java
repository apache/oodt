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

package org.apache.oodt.commons.activity;

import junit.framework.TestCase;

/**
 * Test the <code>ActivityStopped</code> class.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class ActivityStoppedTest extends TestCase {
	/**
	 * Creates a new {@link ActivityStoppedTest} instance.
	 *
	 * @param name Case name.
	 */
	public ActivityStoppedTest(String name) {
		super(name);
	}

	/**
	 * Test basic <code>ActivityStopped</code> methods.
	 */
	public void testActivityStopped() {
		ActivityStopped a = new ActivityStopped();
		try {
			Thread.sleep(100);
		} catch (InterruptedException ignore) {}
		ActivityStopped b = new ActivityStopped();
		assertTrue(!a.equals(b));
		assertTrue(!b.equals(a));
		assertTrue(a.getTime().compareTo(b.getTime()) < 0);
		// Really not much else we can test...
	}
}
