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

import java.io.IOException;
import java.util.Arrays;
import junit.framework.TestCase;

/** Unit test for the {@link FixedBufferOutputStream} class.
 *
 * @author Kelly
 */
public class FixedBufferOutputStreamTest extends TestCase {
	/** Construct the test case for the {@link FixedBufferOutputStream} class.
	 *
	 * @param name Case name.
	 */
	public FixedBufferOutputStreamTest(String name) {
		super(name);
	}

	/** Test the fixed buffer output stream's methods with various illegal arguments.
	 */
	public void testIllegalArgs() throws IOException {
		try {
			new FixedBufferOutputStream(-1);
			fail("Illegal size failed to throw exception");
		} catch (IllegalArgumentException ignored) {}
		FixedBufferOutputStream out = new FixedBufferOutputStream(100);
		try {
			out.write(null);
			fail("Writing a null byte array failed to throw exception");
		} catch (NullPointerException ignored) {}
		try {
			out.write(null, 0, 10);
			fail("Writing a null byte array failed to throw exception");
		} catch (NullPointerException ignored) {}
		try {
			out.write(TEST_DATA, -1, 10);
			fail("Writing with negative offset failed to throw exception");
		} catch (IndexOutOfBoundsException ignored) {}
		try {
			out.write(TEST_DATA, TEST_DATA.length + 1, 10);
			fail("Writing with offset past end of array failed to throw exception");
		} catch (IndexOutOfBoundsException ignored) {}
		try {
			out.write(TEST_DATA, 0, -1);
			fail("Writing with negative length array failed to throw exception");
		} catch (IndexOutOfBoundsException ignored) {}
		try {
			out.write(TEST_DATA, 2, 5);
			fail("Writing with offset and length exceeding end of array failed to throw exception");
		} catch (IndexOutOfBoundsException ignored) {}
	}

	/** Test a stream with space for no bytes at all.
	 */
	public void testZeroSizeStream() throws IOException {
		FixedBufferOutputStream out = new FixedBufferOutputStream(0);			    // Space for no byte at all
		out.write(1);									    // Write a 1 
		out.write(2);									    // ...and a 2
		out.write(3);									    // ...and a 3
		out.write(TEST_DATA);								    // ...and a byte array
		byte[] buffer = out.getBuffer();						    // Get the buffer
		assertNotNull(buffer);								    // It should never be null
		assertEquals(0, buffer.length);							    // But it should be empty
	}

	/** Test a stream under various conditions.
	 */
	public void testStream() throws IOException {
		FixedBufferOutputStream out = new FixedBufferOutputStream(10);			    // Space for 10 bytes
		byte[] buffer = out.getBuffer();						    // Get the buffer
		assertNotNull(buffer);								    // It should never be null
		assertEquals(0, buffer.length);							    // Nothing written => 0 length
		out.write(1);									    // [1]
		buffer = out.getBuffer();							    // Get the buffer
		assertNotNull(buffer);								    // It should never be null
		assertEquals(1, buffer.length);							    // Only 1 byte so far
		assertEquals(1, buffer[0]);							    // And it should be a 1
		out.write(2);									    // [1,2]
		buffer = out.getBuffer();							    // Get the buffer
		assertNotNull(buffer);								    // It should never be null
		assertEquals(2, buffer.length);							    // Two bytes so far
		assertEquals(1, buffer[0]);							    // And it should be a 1...
		assertEquals(2, buffer[1]);							    // ...and a 2
		out.write(3);									    // [1,2,3]
		buffer = out.getBuffer();							    // Get the buffer
		assertNotNull(buffer);								    // It should never be null
		assertEquals(3, buffer.length);							    // 3 bytes so far
		assertEquals(1, buffer[0]);							    // They are 1
		assertEquals(2, buffer[1]);							    // ...and 2
		assertEquals(3, buffer[2]);							    // ...and 3

		out.write(TEST_DATA);								    // [1,2,3,8,9,10,11]
		buffer = out.getBuffer();							    // Get the buffer
		assertNotNull(buffer);								    // It should never be null
		assertEquals(7, buffer.length);							    // 7 bytes so far, right?
		assertEquals(1, buffer[0]);							    // They are 1
		assertEquals(2, buffer[1]);							    // ...and 2
		assertEquals(3, buffer[2]);							    // ...and 3
		assertEquals(8, buffer[3]);							    // ...and 8
		assertEquals(9, buffer[4]);							    // ...and 9
		assertEquals(10, buffer[5]);							    // ...and 10
		assertEquals(11, buffer[6]);							    // ...and 11

		out.write(TEST_DATA);								    // [2,3,8,9,10,11,8,9,10,11]
		out.write(TEST_DATA, 1, 2);							    // [8,9,10,11,8,9,10,11,9,10]
		buffer = out.getBuffer();							    // Get the buffer
		assertNotNull(buffer);								    // It should never be null
		assertEquals(10, buffer.length);						    // Full buffer
		assertEquals(8, buffer[0]);							    // They are 8
		assertEquals(9, buffer[1]);							    // ...and 9
		assertEquals(10, buffer[2]);							    // ...and 10
		assertEquals(11, buffer[3]);							    // ...and 11
		assertEquals(8, buffer[4]);							    // ...and 8
		assertEquals(9, buffer[5]);							    // ...and 9
		assertEquals(10, buffer[6]);							    // ...and 10
		assertEquals(11, buffer[7]);							    // ...and 11
		assertEquals(9, buffer[8]);							    // ...and 9
		assertEquals(10, buffer[9]);							    // ...and 10

		out.write(TEST_DATA);								    // [8,9,10,11,9,10,8,9,10,11]
		out.write(TEST_DATA, 0, 1);							    // [9,10,11,9,10,8,9,10,11,8]
		out.write(42);									    // [10,11,9,10,8,9,10,11,8,42]
		out.write(99);									    // [11,9,10,8,9,10,11,8,42,99]
		out.write(TEST_DATA, 1, 3);							    // [8,9,10,11,8,42,99,9,10,11]
		buffer = out.getBuffer();							    // Get the buffer
		assertNotNull(buffer);								    // It should never be null
		assertEquals(10, buffer.length);						    // Full buffer
		assertEquals(8, buffer[0]);							    // They are 8
		assertEquals(9, buffer[1]);							    // ...and 9
		assertEquals(10, buffer[2]);							    // ...and 10
		assertEquals(11, buffer[3]);							    // ...and 11
		assertEquals(8, buffer[4]);							    // ...and 8
		assertEquals(42, buffer[5]);							    // ...and 42
		assertEquals(99, buffer[6]);							    // ...and 99
		assertEquals(9, buffer[7]);							    // ...and 9
		assertEquals(10, buffer[8]);							    // ...and 10
		assertEquals(11, buffer[9]);							    // ...and 11

		byte[] array = new byte[buffer.length];						    // New test data array that's
		System.arraycopy(buffer, 0, array, 0, buffer.length);				    // the same as the current buf
		out.write(array);								    // ...and write that
		buffer = out.getBuffer();							    // Get the buffer
		assertNotNull(buffer);								    // It should never be null
		assertTrue(Arrays.equals(array, buffer));					    // Should get same result

		byte[] big = new byte[buffer.length * 3];					    // More new test data array
		System.arraycopy(buffer, 0, big, 0, buffer.length);				    // ...and add some data
		System.arraycopy(buffer, 0, big, buffer.length, buffer.length);			    // ...and more data
		System.arraycopy(buffer, 0, big, buffer.length * 2, buffer.length);		    // ...and LOTS of data
		out.write(big, buffer.length, buffer.length * 2);				    // Write the last third of it
		buffer = out.getBuffer();							    // Get the buffer
		assertNotNull(buffer);								    // It should never be null
		assertTrue(Arrays.equals(array, buffer));					    // Should get same result!

		out.close();									    // Close it
		try {										    // ...and try...
			out.write(1);								    // ...to write to it
			fail("Writing to a closed stream failed to throw an exception");	    // Shouldn't get here
		} catch (IOException ignored) {}							    // Should get here
	}

	/** Some test data in an array. */
	private static final byte[] TEST_DATA = new byte[] {
		(byte) 8, (byte) 9, (byte) 10, (byte) 11
	};
}
