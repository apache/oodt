// This software was developed by the the Jet Propulsion Laboratory, an operating division
// of the California Institute of Technology, for the National Aeronautics and Space
// Administration, an independent agency of the United States Government.
// 
// This software is copyrighted (c) 2000 by the California Institute of Technology.  All
// rights reserved.
// 
// Redistribution and use in source and binary forms, with or without modification, is not
// permitted under any circumstance without prior written permission from the California
// Institute of Technology.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
// THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// $Id: CacheMapTest.java,v 1.1.1.1 2004-02-28 13:09:21 kelly Exp $

package jpl.eda.util;

import java.io.*;
import java.util.*;
import junit.framework.*;

/** Unit test the {@link CacheMap} class.
 *
 * @author Kelly
 */ 
public class CacheMapTest extends TestCase {
	/** Construct the test case for the {@link CacheMap} class. */
	public CacheMapTest(String name) {
		super(name);
	}

	/** Test the caching operation. */
	public void testCache() {
		CacheMap c = new CacheMap(3);
		c.put("alpha", "1");
		c.put("beta", "2");
		c.put("gamma", "3");
		// gamma beta alpha
		assertEquals("2", c.get("beta"));
		// beta gamma alpha
		c.put("delta", "4");
		// delta beta gamma
		assertNull(c.get("alpha"));
	}

	/** Test the {@link CacheMap#size} and {@link CacheMap#isEmpty} methods. */
	public void testSize() {
		CacheMap c = new CacheMap(3);
		assertEquals(0, c.size());
		assertTrue(c.isEmpty());
		c.put("alpha", "1");
		// alpha
		assertEquals(1, c.size());
		c.put("beta", "2");
		// beta alpha
		assertEquals(2, c.size());
		c.put("gamma", "3");
		// gamma beta alpha
		assertEquals(3, c.size());
		c.put("delta", "4");
		// delta gamma beta
		assertEquals(3, c.size());
		c.clear();
		assertTrue(c.isEmpty());
	}

	/** Test the {@link CacheMap#containsKey} and {@link CacheMap#containsValue} methods. */
	public void testContains() {
		CacheMap c = new CacheMap(3);
		c.put("alpha", "1");
		c.put("beta", "2");
		c.put("gamma", "3");

		assertTrue(c.containsKey("alpha"));
		assertTrue(!c.containsKey("hungus"));
		assertTrue(c.containsValue("2"));
		assertTrue(!c.containsValue("x"));
	}

	/** Test value replacement for the same key. */
	public void testRePut() {
		CacheMap c = new CacheMap(3);
		c.put("alpha", "1");
		c.put("beta", "2");
		c.put("gamma", "3");
		// (gamma, 3) (beta, 2) (alpha, 1)
		c.put("alpha", "x");
		// (alpha, x), (gamma, 3) (beta, 2)
		assertEquals("x", c.get("alpha"));
		// (alpha, x), (gamma, 3) (beta, 2)
		c.put("delta", "y");
		// (delta, y) (alpha, x), (gamma, 3)
		assertEquals(3, c.size());
		assertNull(c.get("beta"));
	}

	/** Test the {@link CacheMap#remove} method. */
	public void testRemove() {
		CacheMap c = new CacheMap(3);
		c.put("alpha", "1");
		c.put("beta", "2");
		c.put("gamma", "3");
		// gamma beta alpha
		c.put("delta", "4");
		// delta gamma beta
		assertEquals("3", c.remove("gamma"));
		// delta beta
		assertNull(c.remove("gamma"));
		assertNull(c.remove("alpha"));
	}
}

