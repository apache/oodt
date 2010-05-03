// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: HTTPAdaptor.java,v 1.3 2007-06-09 15:50:50 kelly Exp $

package jpl.eda.product;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import jpl.eda.xmlquery.LargeResult;
import jpl.eda.xmlquery.XMLQuery;
import org.xml.sax.SAXException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Adapt the web-grid HTTP product service into the procedural interface expected by the
 * product client.
 *
 * @author Kelly
 * @version $Revision: 1.3 $
 */
class HTTPAdaptor implements ProductService {
	/**
	 * Creates a new <code>HTTPAdaptor</code> instance.
	 *
	 * @param url URL to web-grid HTTP product service.
	 */
	HTTPAdaptor(URL url) {
		this.url = url;
	}

	public Server createServer() {
		return new HTTPAccessor();
	}

	/** URL to web-grid HTTP product service. */
	private URL url;

	/**
	 * Access for a single query session.
	 */
	private class HTTPAccessor implements Server, Retriever {
		/**
		 * Handle a query for a product.  This method sends the XMLQuery as XML to
		 * the web-grid product service.  It interprets a 404 Not Found as
		 * returning no results.  For a 200 OK, it synthesizes a result locally
		 * and sets up a retriever to get the product data over HTTP.
		 *
		 * @param userQuery a <code>XMLQuery</code> value.
		 * @return <var>q</var>, possibly adorned with a new result.
		 * @throws ProductException if an error occurs.
		 */
		public synchronized XMLQuery query(XMLQuery userQuery) throws ProductException {
			XMLQuery q = null;
			try {
				q = new XMLQuery(userQuery.getXMLDocString()); // because clone doesn't work!
				q.getResults().clear();
				StringBuffer b = new StringBuffer("xmlq=");
				b.append(URLEncoder.encode(q.getXMLDocString(), "UTF-8"));

				c = (HttpURLConnection) url.openConnection();
				c.setDoInput(true);
				c.setDoOutput(true);
				c.setUseCaches(true);
				c.setAllowUserInteraction(false);
				c.setInstanceFollowRedirects(true);
				c.setRequestMethod("POST");
				c.setRequestProperty("Content-Length", String.valueOf(b.length()));
				c.connect();
				OutputStream out = null;
				try {
					out = c.getOutputStream();
					out.write(b.toString().getBytes());
				} finally {
					if (out != null) try {
						out.close();
					} catch (IOException ignore) {}
				}
				int rc = c.getResponseCode();
				if (rc == HttpURLConnection.HTTP_OK) {
					String mime = c.getContentType();
					long size = Long.parseLong(c.getHeaderField("Content-Length"));
					String resourceID = c.getHeaderField("Content-disposition");
					if (resourceID != null && resourceID.length() > 0) {
						Matcher m = DISPOSITION_PATTERN.matcher(resourceID);
						if (m.matches())
							resourceID = m.group(1);
					}
					LargeResult lr = new LargeResult(/*id*/"1", mime, /*profID*/null, resourceID,
						/*headers*/Collections.EMPTY_LIST, size);
					q.getResults().add(lr);
					q.setRetriever(this);
				} else if (rc == HttpURLConnection.HTTP_NOT_FOUND) {
					// no prob, just leave q unmodified
				} else {
					throw new ProductException("Received response code " + rc + " from HTTP server");
				}
			} catch (SAXException ex) {
				throw new IllegalStateException("Unexpected SAXException: " + ex.getMessage());
			} catch (IOException ex) {
				throw new ProductException(ex);
			}
			return q;
		}

		/**
		 * Retrieve the next chunk over the HTTP connection.  <strong>Major
		 * assumption</strong>: offsets are monotonically increasing.  I know they
		 * are since I wrote the ChunkedProductInputStream too, but if that
		 * changes, this could well break.
		 *
		 * @param id Ignored.
		 * @param offset Ignored.
		 * @param length How large of a block to get.
		 * @return a chunk of the product.
		 * @throws ProductException if an error occurs.
		 */
		public synchronized byte[] retrieveChunk(String id, long offset, int length) throws ProductException {
			try {
				byte[] buf = new byte[length];
				int off = 0;
				while (length > 0) {
					int num = c.getInputStream().read(buf, off, length);
					length -= num;
					off += num;
				}
				return buf;
			} catch (IOException ex) {
				throw new ProductException(ex);
			}
		}

		/** {@inheritDoc} */
		public synchronized void close(String id) throws ProductException {
			try {
				c.getInputStream().close();
				c.disconnect();
				c = null;
			} catch (IOException ex) {
				throw new ProductException(ex);
			}
		}

		/** Established connection to web-grid HTTP product service. */
		private volatile HttpURLConnection c;
		private String theQuery;
	}
	
	/** Pattern to grab the filename from a content-disposition header. */
	private static Pattern DISPOSITION_PATTERN = Pattern.compile("^attachment; *filename=\"(.+)\"$");
}
