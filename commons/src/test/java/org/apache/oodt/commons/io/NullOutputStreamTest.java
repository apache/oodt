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

package org.apache.oodt.commons.io;

import java.io.*;

import junit.framework.*;

/** Unit test the {@link NullOutputStream} class.
 *
 * @author Kelly
 */ 
public class NullOutputStreamTest extends TestCase {
	/** Construct the test case for the {@link NullOutputStream} class. */
	public NullOutputStreamTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		out = new NullOutputStream();
	}

	public void testClosing() {
		try {
			out.close();
		} catch (IOException ex) {
			fail("Should not throw an exception on close: " + ex.getMessage());
		}
		try {
			out.write(1);
			fail("Should not be able to write to a closed stream");
		} catch (IOException ignore) {}
		try {
			out.flush();
			fail("Should not be able to flush a closed stream");
		} catch (IOException ignore) {}
		try {
			out.close();
			fail("Should not be able to close a closed stream");
		} catch (IOException ignore) {}
	}

	public void testFlush() {
		try {
			out.flush();
		} catch (IOException ex) {
			fail("Should not throw an exception on flush: " + ex.getMessage());
		}
	}

	public void testWriting() {
		try {
			byte[] array = new byte[]{(byte)0, (byte)1, (byte)2};
			out.write(1);
			out.write(array);
			out.write(array, 1, 1);
		} catch (IOException ex) {
			fail("Should not throw an exception on writing: " + ex.getMessage());
		}
	}

	/** The {@link NullOutputStream} we're testing. */
	private NullOutputStream out;

}

