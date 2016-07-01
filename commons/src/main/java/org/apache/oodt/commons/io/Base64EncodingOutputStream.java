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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.oodt.commons.util.Base64;

/** An output stream that encodes its data into RFC-1512 base 64 format.
 *
 * Wrap this input stream around another output stream, and all the bytes will be
 * converted into their base-64 format when you write to it.
 *
 * @author Kelly
 */
public class Base64EncodingOutputStream extends FilterOutputStream {
	/** Construct a base-64 encoding output stream.
	 *
	 * @param outputStream The output stream to which to write.
	 */
	public Base64EncodingOutputStream(OutputStream outputStream) {
		super(outputStream);
	}

	/** Write a byte of data.
	 *
	 * The byte will be encoded into base-64 format on the output.
	 *
	 * @param b The byte.
	 * @throws IOException If an I/O error occurs.
	 */
	public void write(int b) throws IOException {
		if (buffer == null) {
		  throw new IOException("Can't write onto a closed stream");
		}
		buffer[index++] = (byte) b;
		if (index == buffer.length) {
		  shipout();
		}
	}

	/** Write a bunch of bytes.
	 *
	 * The given array of bytes will be encoded into base-64 on the output.
	 *
	 * @param b The array to write.
	 * @param offset Where in the data to start writing.
	 * @param length How many bytes to write.
	 * @throws IOException If an I/O error occurs.
	 */
	public void write(byte[] b, int offset, int length) throws IOException {
		if (b == null) {
		  throw new IllegalArgumentException("Can't write a null array");
		}
		if (offset < 0 || offset >= b.length) {
		  throw new IndexOutOfBoundsException("Can't get bytes at " + offset + " in array with indexes 0.."
											  + (b.length - 1));
		}
		if (length < 0) {
		  throw new IllegalArgumentException("Can't write a negative amount of bytes");
		}
		if (offset + length > b.length) {
		  throw new IndexOutOfBoundsException("Can't get bytes beyond edge of array");
		}
		if (buffer == null) {
		  throw new IOException("Can't write onto a closed stream");
		}
		while (length > 0) {
			int avail = buffer.length - index;
			int amount = avail < length? avail : length;
			System.arraycopy(b, offset, buffer, index, amount);
			index += amount;
			offset += amount;
			length -= amount;
			if (index == buffer.length) {
			  shipout();
			}
		}
	}

	/** Flush the stream.
	 *
	 * This causes any buffered bytes to be encoded and shipped out to the underlying
	 * stream, which is also flushed.
	 *
	 * @throws IOException If an I/O error occurs.
	 */
	public void flush() throws IOException {
		if (buffer == null) {
		  throw new IOException("Can't flush a closed stream");
		}
		shipout();
		out.flush();
	}

	/** Close the stream.
	 *
	 * This writes out any unflushed data in base-64 format and closes the underlying
	 * stream.
	 *
	 * @throws IOException If an I/O error occurs.
	 */
	public void close() throws IOException {
		if (buffer == null) {
		  throw new IOException("Can't close an already closed stream");
		}
		flush();
		out.close();
		out = null;
		buffer = null;
	}

	/** Ship out a bunch of buffered data in base-64 format.
	 *
	 * This resets the index of the next byte to insert back to zero.
	 *
	 * @throws IOException If an I/O error occurs.
	 */
	private void shipout() throws IOException {
		byte[] encoded = Base64.encode(buffer, 0, index);
		out.write(encoded);
		index = 0;
	}

	/** Size of the output data buffer.  Must be a multiple of 3.
	 */
	private static final int BUFFER_SIZE = 300;

	/** Buffer for output data.
	 */
	private byte[] buffer = new byte[BUFFER_SIZE];

	/** Where we are in the buffer.
	 */
	private int index = 0;
}
