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
 * Test the {@link Activity} class.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class ActivityTest extends TestCase {
	/**
	 * Creates a new {@link ActivityTest} instance.
	 *
	 * @param name Case name.
	 */
	public ActivityTest(String name) {
		super(name);
	}

	/**
	 * Test basic Activity methods.
	 */
	public void testActivities() {
		TestActivity ta = new TestActivity();				       // Create test activity
		assertTrue(!"new-id".equals(ta.getID()));			       // It should generate a unique ID
		ta.setID("new-id");						       // Change its ID
		assertEquals("new-id", ta.getID());				       // Make sure it got the change
		Incident i = new Incident();					       // Now create an incident and...
		ta.log(i);							       // ...log it with the activity
		assertEquals(incident, i);					       // See if it recorded the incident
		ta.stop();							       // Now stop the activity
		assertEquals(ActivityStopped.class, incident.getClass());	       // And see if it recorded a "stop" incident
	}

	/** Last received incident from the test activity. */
	private Incident incident;
	
	/**
	 * Testing activity that records incidents to the test case.
	 */
	private class TestActivity extends Activity {
		/**
		 * Creates a new {@link TestActivity} instance.
		 */
		public TestActivity() {
			super();
		}

		/**
		 * Record the last incident to the test case.
		 *
		 * @param incident an {@link Incident} value.
		 */
		protected void recordIncident(Incident incident) {
			ActivityTest.this.incident = incident;
		}
	}
}
