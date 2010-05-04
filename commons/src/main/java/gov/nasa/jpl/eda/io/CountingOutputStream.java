// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: CountingOutputStream.java,v 1.1.1.1 2004-02-28 13:09:13 kelly Exp $

package jpl.eda.io;

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
