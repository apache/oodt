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

import java.util.Collections;
import junit.framework.TestCase;

/**
 * Test the {@link CompositeActivity} class.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class CompositeActivityTest extends TestCase {
	/**
	 * Creates a new {@link CompositeActivityTest} instance.
	 *
	 * @param name Case name.
	 */
	public CompositeActivityTest(String name) {
		super(name);
	}

	public void testIt() {
		try {
			new CompositeActivity(null);
			fail("Can contruct CompositeActivity with null collection");
		} catch (IllegalArgumentException good) {}
		try {
			new CompositeActivity(Collections.singleton("hello"));
			fail("Can construct CompositeActivity with non-Activity in collection");
		} catch (IllegalArgumentException good) {}
		CompositeActivity ca = new CompositeActivity(Collections.singleton(new TestActivity()));
		Incident i = new Incident();
		ca.log(i);
		assertEquals(i, incident);
	}

	/** Last received incident. */
	private Incident incident;

	/**
	 * Test activity that records last incidents received into the test case.
	 */
	private class TestActivity extends Activity {
		protected void recordIncident(Incident i) {
			incident = i;
		}
	}
}
