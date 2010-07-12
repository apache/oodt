// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: CompositeActivityTest.java,v 1.1 2004-03-02 19:29:00 kelly Exp $

package jpl.eda.activity;

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
