// Copyright 2001-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Net.java,v 1.1.1.1 2004-02-28 13:09:15 kelly Exp $

package jpl.eda.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

/** Network utilities.
 *
 * @author Kelly.
 */
public class Net {
	/** Return the IP address of the "localhost" loopback address.
	 *
	 * @return The loopback IP address, 127.0.0.1.
	 */
	public static InetAddress getLoopbackAddress() {
		return loopback;
	}

	/**
	 * Return the IP address of the local host's primary IP interface.
	 *
	 * @return an <code>InetAddress</code> value.
	 */
	public static InetAddress getLocalHost() {
		return localHost;
	}

	/** The InetAddress of the loopback IP address, 127.0.0.1. */
	private static InetAddress loopback = null;

	/** The InetAddress of the local system's primary interface. */
	private static InetAddress localHost = null;

	/** Initialize this class. */
	static {
		try {
			loopback = InetAddress.getByName(null);
			localHost = InetAddress.getLocalHost();
		} catch (UnknownHostException ex) {
			System.err.println("FATAL ERROR: Cannot retrieve loopback or local address: " + ex.getMessage());
			System.exit(1);
		}
	}
}
