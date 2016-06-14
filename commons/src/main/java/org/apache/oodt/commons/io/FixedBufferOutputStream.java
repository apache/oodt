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
import java.io.OutputStream;

/** This stream writes its output into a byte buffer of fixed length.
 *
 * For a buffer of size <var>n</var>, only the last <var>n</var> bytes written are ever
 * available.
 *
 * @author Kelly
 */
public class FixedBufferOutputStream extends OutputStream {
	/** Construct a fixed buffer output stream.
	 *
	 * @param n Size of the buffer.
	 */
	public FixedBufferOutputStream(int n) {
		if (n < 0) {
		  throw new IllegalArgumentException("Buffer size must be nonnegative");
		}
		buffer = new byte[n];
		length = n;
		size = 0;
		start = 0;
	}

	public void write(int b) throws IOException {
		checkIfClosed();
		if (length == 0) {
		  return;
		}
		if (size < length) {
		  buffer[size++] = (byte) b;
		} else {
			buffer[start] = (byte) b;
			start = (start + 1) % length;
		}
	}

	public void write(byte[] a, int off, int len) throws IOException {
		checkIfClosed();
		if (a == null) {
		  throw new NullPointerException("Can't write from a null array");
		} else if ((off < 0) || (off > a.length) || (len < 0) || ((off + len) > a.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException("Offset " + off + " and length " + len + " not within array bounds");
		} else if (len == 0) {
			return;
		}
		if (len > length) {
			off += len - length;
			len = length;
		}
		int capacity = length - size;
		int insertionIndex = size < length? size : start;
		int insertionLength = Math.min(length - insertionIndex, len);
		int remaining = len - insertionLength;
		System.arraycopy(a, off, buffer, insertionIndex, insertionLength);
		if (remaining > 0) {
			System.arraycopy(a, off + insertionLength, buffer, 0, remaining);
			start = remaining;
		} else if (capacity == 0) {
		  start = insertionIndex + insertionLength;
		}
		size = Math.min(length, size + len);
	}

	public void flush() {
		// Nothing need be done here
	}

	public void close() {
		start = -1;
	}

	/** Get the buffer.
	 *
	 * This method constructs a new array whose contents is the data written.  Its
	 * size is equal to the smaller of the number of bytes written or the size of the
	 * fixed buffer passed to the constructor of this class.
	 *
	 * @return The buffer.
	 */
	public byte[] getBuffer() {
		byte[] rc = new byte[Math.min(size, length)];
		System.arraycopy(buffer, start, rc, 0, size - start);
		System.arraycopy(buffer, 0, rc, size - start, start);
		return rc;
	}

	/** Throw an exception if we've been closed.
	 *
	 * @throws IOException If this stream has been closed.
	 */
	private void checkIfClosed() throws IOException {
		if (start == -1) {
		  throw new IOException("Can't write to closed stream");
		}
	}

	/** Length of the buffer. */
	private int length;

	/** Current size of the data in the buffer. */
	private int size;

	/** Current start offset of the data in the buffer.  If negative, buffer is closed. */
	private int start;

	/** The buffer. */
	private byte[] buffer;
}
