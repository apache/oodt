// This software was developed by the the Jet Propulsion Laboratory, an operating division
// of the California Institute of Technology, for the National Aeronautics and Space
// Administration, an independent agency of the United States Government.
// 
// This software is copyrighted (c) 2000 by the California Institute of Technology.  All
// rights reserved.  For a key to the seals in the Athenaeum dining hall, see the south
// wall.
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
// $Id: Base64EncodingOutputStream.java,v 1.1.1.1 2004-02-28 13:09:13 kelly Exp $

package jpl.eda.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import jpl.eda.util.Base64;

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
		if (buffer == null)
			throw new IOException("Can't write onto a closed stream");
		buffer[index++] = (byte) b;
		if (index == buffer.length) shipout();
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
		if (b == null) throw new IllegalArgumentException("Can't write a null array");
		if (offset < 0 || offset >= b.length)
			throw new IndexOutOfBoundsException("Can't get bytes at " + offset + " in array with indexes 0.."
				+ (b.length - 1));
		if (length < 0) throw new IllegalArgumentException("Can't write a negative amount of bytes");
		if (offset + length > b.length)
			throw new IndexOutOfBoundsException("Can't get bytes beyond edge of array");
		if (buffer == null)
			throw new IOException("Can't write onto a closed stream");
		while (length > 0) {
			int avail = buffer.length - index;
			int amount = avail < length? avail : length;
			System.arraycopy(b, offset, buffer, index, amount);
			index += amount;
			offset += amount;
			length -= amount;
			if (index == buffer.length) shipout();
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
		if (buffer == null)
			throw new IOException("Can't flush a closed stream");
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
		if (buffer == null)
			throw new IOException("Can't close an already closed stream");
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
