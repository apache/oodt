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

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import org.apache.oodt.commons.util.Base64;

/** An input stream that decodes its data from the RFC-1512 base 64 format.
 *
 * Wrap this input stream around another input stream, and all the bytes will be converted
 * from their base-64 format when you read from it.
 *
 * @author Kelly
 */
public class Base64DecodingInputStream extends FilterInputStream {
	/** Construct a base-64 decoding input stream.
	 *
	 * @param inputStream The input stream to decode.
	 */
	public Base64DecodingInputStream(InputStream inputStream) {
		super(inputStream);
	}

	/** Read the next byte.
	 *
	 * Decode more base-64 data and return the next decoded byte.
	 *
	 * @return The byte, or -1 on end of stream.
	 * @throws IOException If an I/O error occurs.
	 */
	public int read() throws IOException {
		if (in == null) {
		  throw new IOException("Can't read from a closed stream");
		}

		// If we've used up the decoded data buffer, read 4 more bytes and decode 'em.
		if (buffer == null || index == buffer.length) {
			byte[] streamBuf = new byte[4];
			int toRead = 4;
			int atIndex = 0;
			int actuallyGot;
			boolean firstRead = true;
			while (toRead > 0) {
				actuallyGot = in.read(streamBuf, atIndex, toRead);
				if (actuallyGot == -1) {
					if (firstRead) {
					  return -1;
					} else {
					  break;
					}
				}
				firstRead = false;
				atIndex += actuallyGot;
				toRead -= actuallyGot;
			}
			buffer = Base64.decode(streamBuf);
			if (buffer.length == 0) {
				buffer = null;
				return -1;
			}
			index = 0;
		}
		return buffer[index++] & 0xff;
	}

	/** Read a bunch of bytes.
	 *
	 * This decodes base-64 data from the underlying stream and puts the result into
	 * the given array.
	 *
	 * @param b The buffer to fill with decoded base-64 data.
	 * @param offset Where in the buffer to start filling.
	 * @param length How many bytes to fill.
	 * @return The actual number of decoded bytes.
	 * @throws IOException If an I/O error occurs.
	 */
	public int read(byte[] b, int offset, int length) throws IOException {
		if (b == null) {
		  throw new IllegalArgumentException("Can't read data into a null array");
		}
		if (offset < 0 || offset >= b.length) {
		  throw new IndexOutOfBoundsException("Can't read data into an array with indexes 0.." + (b.length - 1)
											  + " at index " + offset);
		}
		if (length < 0) {
		  throw new IllegalArgumentException("Can't read a negative amount of data");
		}
		if (offset + length > b.length) {
		  throw new IndexOutOfBoundsException("Can't read data past the right edge of an array");
		}
		if (in == null) {
		  throw new IOException("Can't read from a closed stream");
		}

		int c = read();
		if (c == -1) {
		  return -1;
		}
		b[offset] = (byte) c;
		int i = 1;
		try {
			for (; i < length; ++i) {
				c = read();
				if (c == -1) {
				  break;
				}
				b[offset + i] = (byte) c;
			}
		} catch (IOException ignore) {}
		return i;
	}

	/** Skip bytes.
	 *
	 * This method skips and discards <var>n</var> decoded bytes on the input stream.
	 *
	 * @param n Number of bytes to skip.
	 * @return Actual number of bytes skipped.
	 * @throws IOException If an I/O error occurs.
	 */
	public long skip(long n) throws IOException {
		if (in == null) {
		  throw new IOException("Can't skip past data on a closed stream");
		}
		int actuallySkipped = 0;
		while (n > 0) {
			if (read() == -1) {
			  return actuallySkipped;
			}
			--n;
			++actuallySkipped;
		}
		return actuallySkipped;
	}

	/** Return bytes available for reading or skipping without blocking.
	 *
	 * @return The number of bytes that can be read from this stream or skipped over
	 * on the stream without blocking.
	 * @throws IOException If an I/O error occurs.
	 */
	public int available() throws IOException {
		if (in == null) {
		  throw new IOException("Can't see how many bytes are available on a closed stream");
		}
		if (buffer != null && index < buffer.length) {
		  return buffer.length - index;
		}
		return in.available() >= 4? 1 : 0;
	}

	/** Close this stream.
	 *
	 * @throws IOException If an I/O error occurs.
	 */
	public void close() throws IOException {
		if (in == null) {
		  throw new IOException("Can't close a closed stream");
		}
		in.close();
		in = null;
		buffer = null;
	}

	/** Buffer for decoded data.
	 */
	private byte[] buffer;

	/** Where we'll next read out of the buffer.
	 *
	 * Since we always read 4 bytes at a time (a base-64 block), we can decode that
	 * into as many as 3 bytes, so start out the index in an invalid location.
	 */
	private int index = 3;
}
