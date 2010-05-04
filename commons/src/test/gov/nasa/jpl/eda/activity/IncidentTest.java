// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: IncidentTest.java,v 1.1 2004-03-02 19:29:01 kelly Exp $

package jpl.eda.activity;

import java.util.Date;
import junit.framework.TestCase;

/**
 * Test the {@link Incident} class.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class IncidentTest extends TestCase {
	/**
	 * Creates a new {@link IncidentTest} instance.
	 *
	 * @param name Case name.
	 */
	public IncidentTest(String name) {
		super(name);
	}

	/**
	 * Make sure incidents get timestamped.
	 */
	public void testTimestamping() {
		Incident i = new Incident();
		rest();
		assertTrue(new Date().compareTo(i.getTime()) > 0);
	}

	/**
	 * See if incidents are "well-behaved" objects.
	 */
	public void testObjectMethods() {
		Incident a = new Incident();
		rest();
		Incident b = new Incident();

		assertEquals(a, a);
		assertTrue(!a.equals(b));
		assertTrue(a.compareTo(b) < 0);
		assertTrue(b.compareTo(a) > 0);
		int ignore = a.hashCode();

		b.setActivityID("test");
		assertTrue(!b.equals(a));
		assertTrue(a.compareTo(b) < 0);
		assertTrue(b.compareTo(a) > 0);
		ignore = b.hashCode();
	}

	/**
	 * Pause current thread for 10 whole milliseconds.
	 */
	private static void rest() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException ignore) {}
	}
}
