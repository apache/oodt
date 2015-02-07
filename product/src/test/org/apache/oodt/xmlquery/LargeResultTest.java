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


package org.apache.oodt.xmlquery;

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
