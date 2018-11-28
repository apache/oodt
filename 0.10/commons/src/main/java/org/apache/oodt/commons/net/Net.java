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

package org.apache.oodt.commons.net;

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
