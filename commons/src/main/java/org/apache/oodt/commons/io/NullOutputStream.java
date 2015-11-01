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

/** A null output stream.
 *
 * This output stream throws away all data it gets.
 *
 * @author Kelly
 */
public class NullOutputStream extends OutputStream {
	/** Construct a null output stream.
	 */
	public NullOutputStream() {
		open = true;
	}

	/** Write a byte to the output stream, which is thrown away.
	 *
	 * @param b The byte to toss.
	 * @throws IOException If the stream is closed.
	 */
	public void write(int b) throws IOException {
		checkOpen();
	}

	/** Write a byte array to the output stream, which is thrown away.
	 *
	 * @param a The array to write.
	 * @param offset Where in the array to ignore bytes to write.
	 * @param length How many bytes to ignore.
	 * @throws IOException If the stream is closed.
	 */
	public void write(byte[] a, int offset, int length) throws IOException {
		if (a == null) {
		  throw new NullPointerException("Can't write a null array");
		} else if ((offset < 0) || (offset > a.length) || (length < 0) || ((offset + length) > a.length)
			|| ((offset + length) < 0)) {
		  throw new IndexOutOfBoundsException("Offset " + offset + " and length " + length
											  + " not in array of length " + a.length);
		}
		checkOpen();
	}

	/** Flush an output stream, which does nothing.
	 *
	 * @throws IOException If the stream is closed.
	 */
	public void flush() throws IOException {
		checkOpen();
	}

	/** Close an output stream.
	 *
	 * @throws IOException If the stream is already closed.
	 */
	public void close() throws IOException {
		checkOpen();
		open = false;
	}

	/** Check if we're open.
	 *
	 * @throws IOException If we're not open.
	 */
	private void checkOpen() throws IOException {
		if (!open) {
		  throw new IOException("Stream closed");
		}
	}

	/** Is the output stream open? */
	private boolean open;
}

