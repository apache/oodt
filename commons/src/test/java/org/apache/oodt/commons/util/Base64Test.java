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

package org.apache.oodt.commons.util;

import java.util.*;
import junit.framework.*;

/** Unit test the {@link Base64} class.
 *
 * @author Kelly
 */ 
public class Base64Test extends TestCase {
	/** Construct the test case for the {@link Base64} class. */
	public Base64Test(String name) {
		super(name);
	}

	/** Test encoding and decoding.
	 */
	public void testEncDec() {
		byte[] a = Base64.encode("abcde".getBytes());
		assertTrue("Base-64 encoding failed", Arrays.equals("YWJjZGU=".getBytes(), a));
		byte[] b = Base64.decode("YWJjZGU=".getBytes());
		assertTrue("Base-64 decoding failed", Arrays.equals("abcde".getBytes(), b));
	}
}
