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
