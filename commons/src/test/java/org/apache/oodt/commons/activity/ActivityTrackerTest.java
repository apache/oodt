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
 * Test the {@link ActivityTracker} class.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class ActivityTrackerTest extends TestCase {
	/**
	 * Creates a new {@link ActivityTrackerTest} instance.
	 *
	 * @param name Case name.
	 */
	public ActivityTrackerTest(String name) {
		super(name);
	}

	/**
	 * Save old values of the <code>org.apache.oodt.commons.activity.factories</code> and
	 * <code>activity.factories</code> properties.
	 *
	 * @throws Exception if an error occurs.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		longName = System.getProperty("org.apache.oodt.commons.activity.factories");
		shortName = System.getProperty("activity.factories");
	}

	/**
	 * Test initialization of the activity tracker.
	 *
	 * @throws Exception if an error occurs.
	 */
	public void testInitialization() throws Exception {
		System.getProperties().remove("org.apache.oodt.commons.activity.factories");           // Clear the long property
		System.getProperties().remove("activity.factories");		       // And the short one
		ActivityTracker.initializeFactories();				       // And initalize
		Activity activity = ActivityTracker.createActivity();		       // Have it make an activity
		assertTrue(activity instanceof NullActivity);			       // With neither set, it must be NullActivity

		System.setProperty("org.apache.oodt.commons.activity.factories",		       // Now set the long name
			"org.apache.oodt.commons.activity.ActivityTrackerTest$TestFactory");	       // to point to our test factory
		System.setProperty("activity.factories", "non-existent-class");	       // And set the short name to nonsense
		ActivityTracker.initializeFactories();				       // And initialize
		activity = ActivityTracker.createActivity();			       // Have it make an activity
		assertTrue(activity instanceof TestActivity);			       // With just one, it must be our TestActivity

		System.getProperties().remove("org.apache.oodt.commons.activity.factories");           // Clear the long so it must look at short
		System.setProperty("activity.factories",			       // And set the short name to...
			"org.apache.oodt.commons.activity.ActivityTrackerTest$TestFactory"	       // ...not one, but...
			+ ",org.apache.oodt.commons.activity.ActivityTrackerTest$TestFactory");	       // ...two factories
		ActivityTracker.initializeFactories();				       // And initialize
		activity = ActivityTracker.createActivity();			       // Now create an activity
		assertTrue(activity instanceof CompositeActivity);		       // With > 1, it must be a CompositeActivity
	}

	/**
	 * Restore old values of the <code>org.apache.oodt.commons.activity.factories</code> and
	 * <code>activity.factories</code> properties.
	 *
	 * @throws Exception if an error occurs.
	 */
	protected void tearDown() throws Exception {
		if (longName == null)
			System.getProperties().remove("org.apache.oodt.commons.activity.factories");
		else
			System.setProperty("org.apache.oodt.commons.activity.factories", longName);
		if (shortName == null)
			System.getProperties().remove("activity.factories");
		else
			System.setProperty("activity.factories", shortName);
		super.tearDown();
	}

	/**
	 * A test activity that does nothing.
	 */
	static class TestActivity extends Activity {
		protected void recordIncident(Incident incident) {}
	}

	/**
	 * A test activity factory that generates test activities.
	 */
	static class TestFactory implements ActivityFactory {
		public Activity createActivity() {
			return new TestActivity();
		}
	}

	/** Old value of the <code>org.apache.oodt.commons.activity.factories</code> property. */
	private String longName;

	/** Old value of the <code>activity.factories</code> property. */
	private String shortName;
}
