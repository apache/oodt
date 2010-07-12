// Copyright 2001-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: NetTest.java,v 1.1.1.1 2004-02-28 13:09:21 kelly Exp $

package jpl.eda.net;

import java.net.InetAddress;
import junit.framework.*;

/** Unit test the {@link Net} class.
 *
 * @author Kelly
 */ 
public class NetTest extends TestCase {
	/** Construct the test case for the {@link Net} class.
	 */
	public NetTest(String name) {
		super(name);
	}

	/** Test the {@link Net#getLoopbackAddress} method.
	 */
	public void testGetLoopbackAddress() {
		InetAddress addr = Net.getLoopbackAddress();
		assertNotNull(addr);
		byte[] bytes = addr.getAddress();
		assertNotNull(bytes);
		assertEquals(4, bytes.length);
		assertEquals(127, bytes[0]);
		assertEquals(0, bytes[1]);
		assertEquals(0, bytes[2]);
		assertEquals(1, bytes[3]);
	}

	public void testGetLocalHost() {
		InetAddress addr = Net.getLocalHost();
		assertNotNull(addr);
	}
}

