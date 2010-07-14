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

/**
 * An output stream that counts the number bytes it passes on.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public class CountingOutputStream extends FilterOutputStream {
	/**
	 * Creates a new <code>CountingOutputStream</code> instance.
	 *
	 * @param out Where to send bytes onto.
	 */
	public CountingOutputStream(OutputStream out) {
		super(out);
	}
  
	public void write(int b) throws IOException {
		out.write(b);
		++written;
	}
  
	public void write(byte[] b) throws IOException {
		out.write(b);
		written += b.length;
	}
  
	public void write(byte[] b, int offset, int length) throws IOException {
		out.write(b, offset, length);
		written += length;
	}
  
	/**
	 * Get the number of bytes written so far.
	 *
	 * @return a <code>long</code> value.
	 */
	public long getBytesWritten() {
		return written;
	}
  
	/** Number of bytes written so far. */
	private long written = 0L;
}
