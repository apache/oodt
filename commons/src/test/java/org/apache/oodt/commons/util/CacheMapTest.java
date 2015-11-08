// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.commons.util;

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

