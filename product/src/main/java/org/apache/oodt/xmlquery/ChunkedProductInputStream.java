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

import org.apache.oodt.product.ProductException;
import org.apache.oodt.product.Retriever;

import java.io.IOException;
import java.io.InputStream;

/**
 * Streamer for chunked products.
 *
 * This streamer starts a background thread to repeatedly read chunks of a product into a
 * bounded buffer.  Reads read from chunks in the buffer.
 *
 * @author Kelly
 * @version $Revision: 1.5 $
 */
final class ChunkedProductInputStream extends InputStream {
	/**
	 * Creates a new <code>ChunkedProductInputStream</code> instance.
	 *
	 * @param id Product ID.
	 * @param retriever Retriever to use.
	 * @param size How big the product is.
	 */
	ChunkedProductInputStream(String id, Retriever retriever, long size) {
		this.id = id;							       // Save product ID
		this.size = size;						       // Save size
		this.retriever = retriever;					       // And the retriever used to get chunks
		open = true;							       // Start out open
		eof = false;                                                           // And not yet reached EOF, even if size=0
	}


	/**
	 * Read a single byte.
	 *
	 * This method reads from a chunk stored in the bounded buffer.  It may block if
	 * there are no more blocks in the buffer.
	 *
	 * @return Byte, or -1 if at end of file.
	 * @throws IOException if an error occurs.
	 */
	public int read() throws IOException {
		checkOpen();                                                           // Make sure the stream's open
		if (eof) {
		  throw new IOException("End of file");                   // Already reached EOF?  You lose.
		}
		fetchBlock();							       // Get a block.
		if (eof) {
		  return -1;                               // No more blocks?  Signal EOF.
		}
		return block[blockIndex++];					       // Yield next byte (promoted) from block.
	}

	/**
	 * Read into an array.
	 *
	 * This method reads from a chunk stored in the bounded buffer.  It may block if
	 * there are no more blocks in the buffer.
	 *
	 * @param b a <code>byte[]</code> value.
	 * @param offset Where in <var>b</var> to save read bytes.
	 * @param length How many bytes to try to read.
	 * @return Number of bytes actually read, or -1 at end of file.
	 * @throws IOException if an error occurs.
	 */
	public int read(byte[] b, int offset, int length) throws IOException {
		checkOpen();							       // Check if open
		if (offset < 0 || offset > b.length || length < 0 || (offset + length) > b.length || (offset + length) < 0) {
		  throw new IllegalArgumentException("Illegal offset=" + offset + "/length=" + length
											 + " for byte array of length " + b.length);
		} else if (length == 0)						       // Want zero?
		{
		  return 0;                               // Then you get zero
		}
		if (eof) {
		  throw new IOException("End of file");                   // Already reached EOF?  You lose.
		}
		fetchBlock();							       // Get a block.
		if (eof) {
		  return -1;                               // No more blocks?  Signal EOF.
		}
		int amount = Math.min(length, block.length - blockIndex);	       // Return requested amount or whatever's left
		System.arraycopy(block, blockIndex, b, offset, amount);		       // Transfer
		blockIndex += amount;						       // Advance
		return amount;							       // Done
	}

	/**
	 * Fetch another block.
	 *
	 * @throws IOException if an error occurs.
	 */
	private void fetchBlock() throws IOException {
		if (block == null || blockIndex == block.length) {
		  try {               // No block, or current block exhausted?
			if (productIndex == size) {                       // No more blocks left to get?
			  block = null;                           // Drop current block
			  eof = true;                           // Signal EOF
			} else {                               // Otherwise there are more blocks
			  int x = (int) Math.min(BLOCK_SIZE, size - productIndex);  // Can only fetch so much
			  block = retriever.retrieveChunk(id, productIndex, x);  // Get x's worth of data
			  blockIndex = 0;                           // Start at block's beginning
			  productIndex += block.length;                   // Advance product index by block size
			}
		  } catch (ProductException ex) {
			throw new IOException(ex.getMessage());
		  }
		}
	}

	/**
	 * Return number of bytes currently available.
	 *
	 * If we have a block, the amount of available bytes is whatever's in the block.
	 * Otherwise we don't know how many bytes, and we could block, so say zero are available.
	 *
	 * @return an <code>int</code> value.
	 * @throws IOException if an error occurs.
	 */
	public int available() throws IOException {
		checkOpen();							       // Open?
		return block == null? 0 : block.length - blockIndex;		       // If no current block, you can only get 0
	}

	public void close() throws IOException {
		checkOpen();							       // Open?
	  retriever.close(id);					       // Tell retriever we're done
	  open = false;						       // Flag it
	}

	/**
	 * Mark is not supported.
	 *
	 * @param limit Unused parameter.
	 */
	public void mark(int limit) {
		throw new UnsupportedOperationException("Mark not supported");
	}

	/**
	 * Reset is not supported.
	 */
	public void reset() {
		throw new UnsupportedOperationException("Reset not supported");
	}

	/**
	 * Mark/reset operations are not supported.
	 *
	 * @return False.
	 */
	public boolean markSuppoted() {
		return false;
	}
	
	/**
	 * Throw an exception if the stream's closed.
	 *
	 * @throws IOException if the stream's closed.
	 */
	private void checkOpen() throws IOException {
		if (open) {
		  return;
		}
		throw new IOException("Stream closed");
	}

	/** Product ID. */
	private String id;

	/** What can retrieve the product. */
	private Retriever retriever;

	/** How big the product is. */
	private long size;

	/** Current block or null if there's no current block. */
	private byte[] block;

	/** From where in <code>block</code> to read. */
	private int blockIndex;

	/** From where in the product to read the next block. */
	private long productIndex = 0L;

	/** True if we got to the end of file. */
	private boolean eof;
	
	/** Is the stream open? */
	private boolean open;

  public static final int VAL = 4096;
  /** Size of chunks. */
	private static final int BLOCK_SIZE = Integer.getInteger("org.apache.oodt.xmlquery.blockSize", VAL);
}
