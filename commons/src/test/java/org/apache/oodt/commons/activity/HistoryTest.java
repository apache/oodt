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

import java.util.List;
import junit.framework.TestCase;

/**
 * Test the {@link History} class.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class HistoryTest extends TestCase implements Storage {
	/**
	 * Creates a new {@link HistoryTest} instance.
	 *
	 * @param name Case name.
	 */
	public HistoryTest(String name) {
		super(name);
	}

	/**
	 * Save the old idle and close times.
	 *
	 * @throws Exception if an error occurs.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		oldIdle = History.idleTime;
		oldClose = History.closeTime;
	}

	/**
	 * Restore the old idle and close times.
	 *
	 * @throws Exception if an error occurs.
	 */
	protected void tearDown() throws Exception {
		History.idleTime = oldIdle;
		History.closeTime = oldClose;
		super.tearDown();
	}

	public void testIllegalIncidents() {
		History.closeTime = 50;
		Incident one = new Incident();
		one.setActivityID("a");				       // Want unique objects
		Incident two = new Incident();
		two.setActivityID("a");				       // Want unique objects
		Incident three = new Incident();
		three.setActivityID("b");
		History history = new History(one, this);
		history.addIncident(two);
		try {
			history.addIncident(three);
			fail("Able to add Incidents with non-matching activity IDs");
		} catch (IllegalArgumentException ignored) {}
	}

	/**
	 * Test a normal, well-behaved lifecylce of (incident, incident, stop) with no
	 * idle times.
	 */
	public void testLifecycle() {
		History.closeTime = 50;
		Incident one = new Incident();
		one.setActivityID("test");
		History history = new History(one, this);
		rest();
		Incident two = new Incident();
		two.setActivityID("test");
		history.addIncident(two);
		rest();
		ActivityStopped three = new ActivityStopped();
		three.setActivityID("test");
		history.addIncident(three);
		rest();
		assertNotNull("History never called store", id);
		assertEquals("test", id);
		assertNotNull(incidents);
		assertEquals(3, incidents.size());
		assertEquals(one, incidents.get(0));
		assertEquals(two, incidents.get(1));
		assertEquals(three, incidents.get(2));
	}

	/**
	 * Test arrival and recording of incidents <i>after</i> the stop incident.
	 */
	public void testPostCloseIncidents() {
		History.closeTime = 300;
		Incident one = new Incident();
		one.setActivityID("test");
		rest();
		ActivityStopped two = new ActivityStopped();
		two.setActivityID("test");
		rest();
		Incident three = new Incident();
		three.setActivityID("test");

		History history = new History(one, this);
		history.addIncident(two);
		rest();
		history.addIncident(three);
		rest(1200);

		assertNotNull("History never called store", id);
		assertEquals("test", id);
		assertNotNull(incidents);
		assertEquals(3, incidents.size());
		assertEquals(one, incidents.get(0));
		assertEquals(two, incidents.get(1));
		assertEquals(three, incidents.get(2));
	}

	/**
	 * Test if an idle history is committed when a stop incident is late.
	 */
	public void testIdleHistory() {
		History.idleTime = 300;
		History.closeTime = 100;
		Incident one = new Incident();
		one.setActivityID("test");
		rest();
		Incident two = new Incident();
		two.setActivityID("test");
		rest();
		ActivityStopped three = new ActivityStopped();
		three.setActivityID("test");

		History history = new History(one, this);
		rest();
		history.addIncident(two);
		rest(500);
		history.addIncident(three);

		assertNotNull("History never called store", id);
		assertEquals("test", id);
		assertNotNull(incidents);
		assertEquals(2, incidents.size());
		assertEquals(one, incidents.get(0));
		assertEquals(two, incidents.get(1));
	}

	/**
	 * Test if an idle history is committed and the stop incident arrives in time to
	 * be recorded.
	 */
	public void testPostCommitActivityStop() {
		History.idleTime = 300;
		History.closeTime = 500;
		Incident one = new Incident();
		one.setActivityID("test");
		rest();
		Incident two = new Incident();
		two.setActivityID("test");
		rest();
		ActivityStopped three = new ActivityStopped();
		three.setActivityID("test");

		History history = new History(one, this);
		rest();
		history.addIncident(two);
		rest(500);
		history.addIncident(three);
		rest(400);

		assertNotNull("History never called store", id);
		assertEquals("test", id);
		assertNotNull(incidents);
		assertEquals(3, incidents.size());
		assertEquals(one, incidents.get(0));
		assertEquals(two, incidents.get(1));
		assertEquals(three, incidents.get(2));
	}

	/**
	 * Our test storage just saves the last received ID and list of incidents.
	 *
	 * @param id ID, which should always be <code>test</code>.
	 * @param incidents {@link List} of received {@link Incident}s.
	 */
	public void store(String id, List incidents) {
		this.id = id;
		this.incidents = incidents;
	}

	/**
	 * Pause the current thread for the given time.
	 *
	 * @param time Milliseconds to sleep.
	 */
	private static void rest(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException ignore) {}
	}

	/**
	 * Pause the current thread for 100 milliseconds.
	 */
	private static void rest() {
		rest(100);
	}

	/** Last stored ID. */
	private String id;

	/** Last stored list of {@link Incident}s. */
	private List incidents;

	/** Idle time to restore. */
	private long oldIdle;

	/** Close time to restore. */
	private long oldClose;
}
