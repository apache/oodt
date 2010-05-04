// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: DatagramLoggingActivityFactory.java,v 1.1 2004-03-02 19:28:57 kelly Exp $

package jpl.eda.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This activity factory produces activities that log their incidents using datagrams.  It
 * examines the system properties to determine where to send the incidents.  The property
 * <code>jpl.eda.activity.DatagramLoggingActivityFactory.host</code> (or
 * <code>activity.host</code> if not defined) teslls to what host to send the datagrams.
 * There's no default value, and it's an runtime exception if neither property's defined.
 *
 * <p>The property <code>jpl.eda.activity.DatagramLoggingActivityFactory.port</code> (or
 * <code>activity.port</code> if not defined) tells to what port to send the datagrams.
 * It defaults to 4556.
 *
 * <p>Once created, it records incidents by serializing them into a byte buffer and
 * sending that buffer as a datagram to the activity host/port.  This does limit the size
 * of incidents that can go over, but most incidents are pretty small.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class DatagramLoggingActivityFactory implements ActivityFactory {
	/**
	 * Creates a new {@link DatagramLoggingActivityFactory} instance.
	 */
	public DatagramLoggingActivityFactory() {
		String hostname = System.getProperty("jpl.eda.activity.DatagramLoggingActivityFactory.host",
			System.getProperty("activity.host", ""));
		port = Integer.getInteger("jpl.eda.activity.DatagramLoggingActivityFactory.port",
			Integer.getInteger("activity.port", 4556)).intValue();
		if (hostname.length() == 0)
			throw new IllegalStateException("System property `jpl.eda.activity.DatagramLoggingActivityFactory.host'"
				+ " (or simply `activity.host') not defined or is empty");
		try {
			host = InetAddress.getByName(hostname);
			socket = new DatagramSocket();
		} catch (UnknownHostException ex) {
			throw new IllegalStateException("Activity host `" + host + "' unknown");
		} catch (SocketException ex) {
			throw new IllegalStateException("Cannot create anonymous datagram socket");
		}
	}

	/**
	 * Create an activity that records incidents in a UDP datagram.
	 *
	 * @return an {@link Activity} value.
	 */
	public Activity createActivity() {
		return new Activity() {
			public void recordIncident(Incident incident) {
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(baos);
					oos.writeObject(incident);
					oos.close();
					baos.close();
					byte[] buf = baos.toByteArray();
					DatagramPacket packet = new DatagramPacket(buf, buf.length, host, port);
					socket.send(packet);
				} catch (IOException ignore) {}
			}
		};
	}

	/** To what host to send datagrams. */
	InetAddress host;

	/** To what port on {@link #host}. */
	int port;

	/** Transmission socket for datagrams. */
	DatagramSocket socket;
}
