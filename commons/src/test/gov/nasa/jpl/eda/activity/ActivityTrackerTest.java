// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ActivityTrackerTest.java,v 1.1 2004-03-02 19:29:00 kelly Exp $

package jpl.eda.activity;

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
	 * Save old values of the <code>jpl.eda.activity.factories</code> and
	 * <code>activity.factories</code> properties.
	 *
	 * @throws Exception if an error occurs.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		longName = System.getProperty("jpl.eda.activity.factories");
		shortName = System.getProperty("activity.factories");
	}

	/**
	 * Test initialization of the activity tracker.
	 *
	 * @throws Exception if an error occurs.
	 */
	public void testInitialization() throws Exception {
		System.getProperties().remove("jpl.eda.activity.factories");           // Clear the long property
		System.getProperties().remove("activity.factories");		       // And the short one
		ActivityTracker.initializeFactories();				       // And initalize
		Activity activity = ActivityTracker.createActivity();		       // Have it make an activity
		assertTrue(activity instanceof NullActivity);			       // With neither set, it must be NullActivity

		System.setProperty("jpl.eda.activity.factories",		       // Now set the long name
			"jpl.eda.activity.ActivityTrackerTest$TestFactory");	       // to point to our test factory
		System.setProperty("activity.factories", "non-existent-class");	       // And set the short name to nonsense
		ActivityTracker.initializeFactories();				       // And initialize
		activity = ActivityTracker.createActivity();			       // Have it make an activity
		assertTrue(activity instanceof TestActivity);			       // With just one, it must be our TestActivity

		System.getProperties().remove("jpl.eda.activity.factories");           // Clear the long so it must look at short
		System.setProperty("activity.factories",			       // And set the short name to...
			"jpl.eda.activity.ActivityTrackerTest$TestFactory"	       // ...not one, but...
			+ ",jpl.eda.activity.ActivityTrackerTest$TestFactory");	       // ...two factories
		ActivityTracker.initializeFactories();				       // And initialize
		activity = ActivityTracker.createActivity();			       // Now create an activity
		assertTrue(activity instanceof CompositeActivity);		       // With > 1, it must be a CompositeActivity
	}

	/**
	 * Restore old values of the <code>jpl.eda.activity.factories</code> and
	 * <code>activity.factories</code> properties.
	 *
	 * @throws Exception if an error occurs.
	 */
	protected void tearDown() throws Exception {
		if (longName == null)
			System.getProperties().remove("jpl.eda.activity.factories");
		else
			System.setProperty("jpl.eda.activity.factories", longName);
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

	/** Old value of the <code>jpl.eda.activity.factories</code> property. */
	private String longName;

	/** Old value of the <code>activity.factories</code> property. */
	private String shortName;
}
