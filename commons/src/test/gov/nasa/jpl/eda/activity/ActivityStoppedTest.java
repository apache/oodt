// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ActivityStoppedTest.java,v 1.1 2004-03-02 19:29:00 kelly Exp $

package jpl.eda.activity;

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
