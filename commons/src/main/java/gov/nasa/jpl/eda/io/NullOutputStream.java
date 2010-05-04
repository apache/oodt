// This software was developed by the the Jet Propulsion Laboratory, an operating division
// of the California Institute of Technology, for the National Aeronautics and Space
// Administration, an independent agency of the United States Government.
// 
// This software is copyrighted (c) 2000 by the California Institute of Technology.  All
// rights reserved.
// 
// Redistribution and use in source and binary forms, with or without modification, is not
// permitted under any circumstance without prior written permission from the California
// Institute of Technology.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
// THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// $Id: NullOutputStream.java,v 1.1.1.1 2004-02-28 13:09:15 kelly Exp $

package jpl.eda.io;

import java.io.*;
import java.util.*;

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
		if (a == null)
			throw new NullPointerException("Can't write a null array");
		else if ((offset < 0) || (offset > a.length) || (length < 0) || ((offset + length) > a.length)
			|| ((offset + length) < 0))
			throw new IndexOutOfBoundsException("Offset " + offset + " and length " + length
				+ " not in array of length " + a.length);
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
		if (!open) throw new IOException("Stream closed");
	}

	/** Is the output stream open? */
	private boolean open;
}

