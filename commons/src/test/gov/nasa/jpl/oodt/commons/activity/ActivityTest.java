// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ActivityTest.java,v 1.1 2004-03-02 19:29:00 kelly Exp $

package jpl.eda.activity;

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
