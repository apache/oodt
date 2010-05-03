// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: LargeResultTest.java,v 1.2 2005-08-03 17:03:21 kelly Exp $

package jpl.eda.xmlquery;

import junit.framework.TestCase;
import java.util.Collections;

/**
 * Unit test for {@link LargeResult}.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public class LargeResultTest extends TestCase {
	public LargeResultTest(String id) {
		super(id);
	}

	public void testLargeResults() {
		LargeResult lr1 = new LargeResult("1.2.3", "text/plain", "JPL.Profile", "JPL.Resource", Collections.EMPTY_LIST, 1);
		assertEquals("1.2.3", lr1.getID());
		assertEquals("text/plain", lr1.getMimeType());
		assertEquals("JPL.Profile", lr1.getProfileID());
		assertEquals("JPL.Resource", lr1.getResourceID());
		assertEquals(1, lr1.getSize());
		assertTrue(lr1.getHeaders().isEmpty());

		Result r = new Result("2.3.4", "application/vnd.jpl.large-product", "JPL.Profile", "JPL.Resource",
			Collections.EMPTY_LIST, "text/plain 2");
		LargeResult lr2 = new LargeResult(r);
		assertEquals("text/plain", lr2.getMimeType());
		assertEquals(2, lr2.getSize());

		LargeResult lr3 = new LargeResult(lr2);
		assertEquals("text/plain", lr2.getMimeType());
		assertEquals(2, lr3.getSize());
	}
}
