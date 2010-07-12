// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: DatagramLoggingActivityFactoryTest.java,v 1.1 2004-03-02 19:29:01 kelly Exp $

package jpl.eda.activity;

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
		oldLongHost = System.getProperty("jpl.eda.activity.DatagramLoggingActivityFactory.host");
		oldShortHost = System.getProperty("activity.host");
		oldLongPort = System.getProperty("jpl.eda.activity.DatagramLoggingActivityFactory.port");
		oldShortPort = System.getProperty("activity.port");
	}

	protected void tearDown() throws Exception {
		restore("jpl.eda.activity.DatagramLoggingActivityFactory.host", oldLongHost);
		restore("activity.host", oldShortHost);
		restore("jpl.eda.activity.DatagramLoggingActivityFactory.port", oldLongPort);
		restore("activity.port", oldShortPort);
		super.tearDown();
	}

	/**
	 * Test use of the system properties.
	 *
	 * @throws SocketException if an error occurs.
	 */
	public void testPropertyPriority() throws SocketException {
		System.getProperties().remove("jpl.eda.activity.DatagramLoggingActivityFactory.host");
		System.getProperties().remove("jpl.eda.activity.DatagramLoggingActivityFactory.port");
		System.getProperties().remove("activity.host");
		System.getProperties().remove("activity.port");
		try {
			new DatagramLoggingActivityFactory();
			fail("Can make a DatagramLoggingActivityFactory without host property set");
		} catch (IllegalStateException ex) {}

		System.setProperty("jpl.eda.activity.DatagramLoggingActivityFactory.host", "localhost");
		System.setProperty("activity.host", "non-existent-host");
		DatagramLoggingActivityFactory fac = new DatagramLoggingActivityFactory();
		assertEquals(jpl.eda.net.Net.getLoopbackAddress(), fac.host);
		assertEquals(4556, fac.port);
		fac.socket.close();

		System.getProperties().remove("jpl.eda.activity.DatagramLoggingActivityFactory.host");
		System.setProperty("activity.host", "localhost");
		fac = new DatagramLoggingActivityFactory();
		assertEquals(jpl.eda.net.Net.getLoopbackAddress(), fac.host);
		assertEquals(4556, fac.port);
		fac.socket.close();

		DatagramSocket soc = new DatagramSocket();
		int portNum = soc.getLocalPort();
		System.setProperty("jpl.eda.activity.DatagramLoggingActivityFactory.port", String.valueOf(portNum));
		System.setProperty("activity.port", "illegal-port-value");
		fac = new DatagramLoggingActivityFactory();
		assertEquals(portNum, fac.port);
		fac.socket.close();

		System.getProperties().remove("jpl.eda.activity.DatagramLoggingActivityFactory.port");
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
	public void testActivityReceipt() throws SocketException, IOException, ClassNotFoundException {
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

	/** Old value of <code>jpl.eda.activity.DatagramLoggingActivityFactory.host</code>. */
	private String oldLongHost;

	/** Old value of <code>activity.host</code>. */
	private String oldShortHost;

	/** Old value of <code>jpl.eda.activity.DatagramLoggingActivityFactory.port</code>. */
	private String oldLongPort;

	/** Old value of <code>activity.port</code>. */
	private String oldShortPort;
}
