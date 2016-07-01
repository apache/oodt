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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import junit.framework.TestCase;

/**
 * Test the {@link DatagramLoggingActivityFactory} class.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class DatagramLoggingActivityFactoryTest extends TestCase {
	/**
	 * Creates a new {@link DatagramLoggingActivityFactoryTest} instance.
	 *
	 * @param name Case name.
	 */
	public DatagramLoggingActivityFactoryTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		oldLongHost = System.getProperty("org.apache.oodt.commons.activity.DatagramLoggingActivityFactory.host");
		oldShortHost = System.getProperty("activity.host");
		oldLongPort = System.getProperty("org.apache.oodt.commons.activity.DatagramLoggingActivityFactory.port");
		oldShortPort = System.getProperty("activity.port");
	}

	protected void tearDown() throws Exception {
		restore("org.apache.oodt.commons.activity.DatagramLoggingActivityFactory.host", oldLongHost);
		restore("activity.host", oldShortHost);
		restore("org.apache.oodt.commons.activity.DatagramLoggingActivityFactory.port", oldLongPort);
		restore("activity.port", oldShortPort);
		super.tearDown();
	}

	/**
	 * Test use of the system properties.
	 *
	 * @throws SocketException if an error occurs.
	 */
	public void testPropertyPriority() throws SocketException {
		System.getProperties().remove("org.apache.oodt.commons.activity.DatagramLoggingActivityFactory.host");
		System.getProperties().remove("org.apache.oodt.commons.activity.DatagramLoggingActivityFactory.port");
		System.getProperties().remove("activity.host");
		System.getProperties().remove("activity.port");
		try {
			new DatagramLoggingActivityFactory();
			fail("Can make a DatagramLoggingActivityFactory without host property set");
		} catch (IllegalStateException ignored) {}

		System.setProperty("org.apache.oodt.commons.activity.DatagramLoggingActivityFactory.host", "localhost");
		System.setProperty("activity.host", "non-existent-host");
		DatagramLoggingActivityFactory fac = new DatagramLoggingActivityFactory();
		assertEquals(org.apache.oodt.commons.net.Net.getLoopbackAddress(), fac.host);
		assertEquals(4556, fac.port);
		fac.socket.close();

		System.getProperties().remove("org.apache.oodt.commons.activity.DatagramLoggingActivityFactory.host");
		System.setProperty("activity.host", "localhost");
		fac = new DatagramLoggingActivityFactory();
		assertEquals(org.apache.oodt.commons.net.Net.getLoopbackAddress(), fac.host);
		assertEquals(4556, fac.port);
		fac.socket.close();

		DatagramSocket soc = new DatagramSocket();
		int portNum = soc.getLocalPort();
		System.setProperty("org.apache.oodt.commons.activity.DatagramLoggingActivityFactory.port", String.valueOf(portNum));
		System.setProperty("activity.port", "illegal-port-value");
		fac = new DatagramLoggingActivityFactory();
		assertEquals(portNum, fac.port);
		fac.socket.close();

		System.getProperties().remove("org.apache.oodt.commons.activity.DatagramLoggingActivityFactory.port");
		System.setProperty("activity.port", String.valueOf(portNum));
		fac = new DatagramLoggingActivityFactory();
		assertEquals(portNum, fac.port);
		fac.socket.close();
		soc.close();
	}

	/**
	 * Test construction and transmission of datagrams.
	 *
	 * @throws SocketException if an error occurs.
	 * @throws IOException if an error occurs.
	 * @throws ClassNotFoundException if an error occurs.
	 */
	public void testActivityReceipt() throws IOException, ClassNotFoundException {
		DatagramSocket socket = null;
		try {
			byte[] buf = new byte[512];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket = new DatagramSocket();
			System.setProperty("activity.host", "localhost");
			System.setProperty("activity.port", String.valueOf(socket.getLocalPort()));
			DatagramLoggingActivityFactory fac = new DatagramLoggingActivityFactory();
			Activity a = fac.createActivity();
			Incident i = new Incident();
			a.log(i);
			socket.receive(packet);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), packet.getOffset(),
				packet.getLength()));
			Incident j = (Incident) ois.readObject();
			assertEquals(i, j);
		} finally {
			if (socket != null)
				socket.close();
		}
	}

	/**
	 * Restore a system property.
	 *
	 * @param propName Name of the property to restore.
	 * @param value Old value of the system property, or null if the property wasn't previously set.
	 */
	private static void restore(String propName, String value) {
		if (value == null)
			System.getProperties().remove(propName);
		else
			System.setProperty(propName, value);
	}

	/** Old value of <code>org.apache.oodt.commons.activity.DatagramLoggingActivityFactory.host</code>. */
	private String oldLongHost;

	/** Old value of <code>activity.host</code>. */
	private String oldShortHost;

	/** Old value of <code>org.apache.oodt.commons.activity.DatagramLoggingActivityFactory.port</code>. */
	private String oldLongPort;

	/** Old value of <code>activity.port</code>. */
	private String oldShortPort;
}
