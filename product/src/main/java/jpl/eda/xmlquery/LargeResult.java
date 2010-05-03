// Copyright 2002-2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: LargeResult.java,v 1.2 2005-08-03 17:02:50 kelly Exp $

package jpl.eda.xmlquery;

import java.util.List;
import java.util.StringTokenizer;
import jpl.eda.product.Retriever;
import java.io.InputStream;
import java.io.IOException;

/**
 * A <em>large</em> result is a result for <em>large</em> products.
 *
 * What is large?  Some might say large is something that exceeds most other things of
 * like kind in bulk, capacity, quantity, superficial dimensions, or number of constituent
 * units.  Large might be big, great, capacious, extensive.  Large might be opposed to
 * small; as, a large horse; a large house or room; a large lake or pool; a large jug or
 * spoon; a large vineyard; a large army; a large city.  Some might say that particularly
 * if they're named Webster.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public class LargeResult extends Result {
	/**
	 * Creates a new <code>LargeResult</code> instance.
	 *
	 * @param result a <code>Result</code> value.
	 */
	public LargeResult(Result result) {
		super(result.getID(), "application/vnd.jpl.large-product", result.getProfileID(), result.getResourceID(),
			result.getHeaders(), transformMimeType(result));
		StringTokenizer st = new StringTokenizer((String) value);
		st.nextToken();
		this.size = Long.parseLong(st.nextToken());
	}

	/**
	 * Creates a new <code>LargeResult</code> instance.
	 *
	 * @param id Result ID.
	 * @param mimeType MIME type.
	 * @param profileID Profile ID.
	 * @param resourceID Resource ID.
	 * @param headers Headers.
	 * @param size Size of the product.
	 */
	public LargeResult(String id, String mimeType, String profileID, String resourceID, List headers, long size) {
		super(id, "application/vnd.jpl.large-product", profileID, resourceID, headers, mimeType + " " + size);
		this.size = size;
	}

	/**
	 * Get the size of the product.
	 *
	 * @return Its size.
	 */
	public final long getSize() {
		return size;
	}

	public final String getMimeType() {
		return new StringTokenizer((String) value).nextToken();
	}

	/**
	 * Return the result's value.
	 *
	 * @return a String.
	 * @deprecated This method always treats its value as if it were a String.  Worse,
	 * for very large results, it cannot contain the entire result in memory.  Use
	 * {@link #getInputStream} instead to perform stream processing on result data.
	 */
	public final Object getValue() {
		Object value = null;
		InputStream in = null;
		try {
			if (size > Integer.MAX_VALUE)
				throw new IllegalStateException("Cannot use getValue() for this product, result is too large; "
					+ "use LargeResult.getInputStream instead");
			int sizeToRead = (int) size;
			byte[] buf = new byte[sizeToRead];
			int index = 0;
			int num;
			in = getInputStream();
			while ((num = in.read(buf, index, sizeToRead)) != -1) {
				index += num;
				sizeToRead -= num;
				if (sizeToRead == 0) break;
			}

			// OK, this sucks.  Sucks sucks sucks.  Look, getValue is not to
			// be used anyway.  It sucks.  But dammit, there's some annoying
			// code over in EDRN which is using it when they should be using
			// getInputStream.  Basically, if you call this, you're a hoser.
			// And if you call it and you're not expecting a String, you're a
			// loser/hoser.  DEPRECATED!
			value = new String(buf);

		} catch (IOException ex) {
			throw new IllegalStateException("Unexpected IOException: " + ex.getMessage());
		} finally {
			if (in != null) try {
				in.close();
			} catch (IOException ignore) {}
		}
		return value;
	}

	/** Size of the product. */
	private long size;

	/**
	 * Get an input stream that streams the result from the product server.
	 *
	 * @return an <code>InputStream</code> value.
	 * @throws IOException if an error occurs.
	 */
	public InputStream getInputStream() throws IOException {
		return new ChunkedProductInputStream(id, retriever, size);
	}

	/**
	 * Given an existing <code>Result</code> yield its MIME type.
	 *
	 * The existing <code>Result</code> might be a <code>LargeResult</code>, in which
	 * case the real MIME type is hidden in the value.  Otherwise, it's directly in
	 * the object.
	 *
	 * @param result a <code>Result</code> value.
	 * @return The MIME type.
	 */
	private static String transformMimeType(Result result) {
		if ("application/vnd.jpl.large-product".equals(result.mimeType))
			return (String) result.value;
		else
			return result.mimeType + " 0";
	}

	/** Serial version unique ID. */
	static final long serialVersionUID = -969838775595705444L;
}
