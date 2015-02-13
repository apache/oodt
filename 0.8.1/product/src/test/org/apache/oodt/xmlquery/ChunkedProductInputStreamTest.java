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


package org.apache.oodt.xmlquery;

import java.io.IOException;
import java.util.Arrays;
import org.apache.oodt.product.ProductException;
import org.apache.oodt.product.Retriever;
import junit.framework.TestCase;
import java.io.ByteArrayOutputStream;

/**
 * Unit test for <code>ChunkedProductInputStream</code>.
 *
 * @author Kelly
 * @version $Revision: 1.5 $
 */
public class ChunkedProductInputStreamTest extends TestCase implements Retriever {
	/**
	 * Creates a new <code>ChunkedProductInputStreamTest</code> instance.
	 *
	 * @param id Case name.
	 */
	public ChunkedProductInputStreamTest(String id) {
		super(id);
	}

	public void setUp() throws Exception {
		super.setUp();
		data = new byte[4096];
		for (int i = 0; i < 4096; ++i)
			data[i] = (byte) (i % 256);
	}

	/**
	 * Test reading a single byte at a time.
	 *
	 * @throws IOException if an error occurs.
	 */
 	public void testByteReading() throws IOException {
 		ChunkedProductInputStream in = new ChunkedProductInputStream("test", this, 4096);
 		for (int i = 0; i < 4096; ++i)
 			assertEquals(toByte(i % 256), toByte(in.read() & 0xff));
 		assertEquals(-1, in.read());
 		in.close();
 	}

	public void testArrayReading() throws IOException {
		ChunkedProductInputStream in = new ChunkedProductInputStream("test", this, 4096);
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		byte[] buf = new byte[256];
		int num;
		while ((num = in.read(buf)) != -1)
			out.write(buf, 0, num);
		in.close();
		out.close();
		assertTrue(Arrays.equals(data, out.toByteArray()));
	}

	/**
	 * Test reading and skipping by various amounts.
	 *
	 * @throws IOException if an error occurs.
	 */
	public void testReadingAndSkipping() throws IOException {
		ChunkedProductInputStream in = new ChunkedProductInputStream("test", this, 4096);
		
		byte[] buf = new byte[4];							    // Byte number:
		assertEquals(0, in.read());							    // 0
		assertEquals(0, in.skip(0));							    // 0
		assertEquals(4, in.read(buf));							    // 1, 2, 3, 4
		assertEquals(toByte(1), buf[0]);
		assertEquals(toByte(2), buf[1]);
		assertEquals(toByte(3), buf[2]);
		assertEquals(toByte(4), buf[3]);
		assertEquals(toByte(5), toByte(in.read()));					    // 5
		assertEquals(4, in.skip(4));							    // 6, 7, 8, 9
		assertEquals(toByte(10), toByte(in.read()));					    // 10
		assertEquals(1000, in.skip(1000));						    // 11, 12, ..., 1010
		assertEquals(toByte(1011 % 256), toByte(in.read()));				    // 1011

		buf = new byte[1000];
		int toRead = 1000;
		int index = 0;
		while (toRead > 0) {								    // 1012, 1013, ..., 2011
			int numRead = in.read(buf, index, toRead);
			if (numRead == -1)
				fail("Premature EOF");
			toRead -= numRead;
			index += numRead;
		}
		for (int i = 0; i < buf.length; ++i)
			assertEquals(data[i + 1012], buf[i]);

		assertEquals(2, in.read(buf, 1, 2));						    // 2012, 2013
		assertEquals(toByte(1012 % 256), buf[0]);
		assertEquals(toByte(2012 % 256), buf[1]);
		assertEquals(toByte(2013 % 256), buf[2]);
		assertEquals(toByte(1015 % 256), buf[3]);

		assertEquals(2082, in.skip(2083));						    // 2014, 2015, ..., 4095
		// Shouldn't we get the -1 read first, and THEN get an IOException on subsequent reads?
		try {
			assertEquals(-1, in.read());
		} catch (IOException ignore) {}
		in.close();
	}

	/**
	 * Test reading into larger and larger arrays.
	 *
	 * @throws IOException if an error occurs.
	 */
	public void testWideningWindows() throws IOException {
		// Scary; this test hangs on Windows.  We really should investigate why as
		// it could bite us on the bum in the future.
		if (System.getProperty("os.name", "unknown").indexOf("Windows") != -1) return;

		byte[] read = new byte[4096];
		for (int size = 1; size <= 4096; size *= 2) {
			byte[] buf = new byte[size];
			ChunkedProductInputStream in = new ChunkedProductInputStream("test", this, 4096);
			ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
			int num;
			while ((num = in.read(buf)) != -1)
				out.write(buf, 0, num);
			in.close();
			out.close();
			assertTrue(Arrays.equals(data, out.toByteArray()));
		}
	}

	public byte[] retrieveChunk(String id, long offset, int length) {
		if (!id.equals("test"))
			throw new IllegalArgumentException("Unknown id " + id);
		if (offset < 0 || offset > 4096 || length < 0 ||
			(offset + length) > 4096 || (offset + length) < 0)
			throw new IllegalArgumentException("Bad offset and/or length");
		
		int index = (int) offset;
		byte[] sub = new byte[length];
		System.arraycopy(data, index, sub, 0, length);
		return sub;
	}

	private static byte toByte(int b) {
		return (byte) (b & 0xff);
	}

	public void close(String id) {}

	/** Test data. */
	private byte[] data;
}
