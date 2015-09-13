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

