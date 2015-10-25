/**
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

package org.apache.oodt.grid;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oodt.product.HttpRedirectException;
import org.apache.oodt.product.LargeProductQueryHandler;
import org.apache.oodt.product.ProductException;
import org.apache.oodt.product.QueryHandler;
import org.apache.oodt.product.Retriever;
import org.apache.oodt.xmlquery.LargeResult;
import org.apache.oodt.xmlquery.Result;
import org.apache.oodt.xmlquery.XMLQuery;


/**
 * Product query servlet handles product queries.  It always returns the first matching
 * product, if any.  If no handler can provide a product, it returns 404 Not Found.  If
 * there are no query handlers, it returns 404 Not Found.
 *
 */
public class ProductQueryServlet extends QueryServlet {
	/** {@inheritDoc} */
	protected List getServers(Configuration config) {
		return config.getProductServers();
	}

	/** {@inheritDoc} */
	protected void handleQuery(XMLQuery query, List handlers, HttpServletRequest req, HttpServletResponse res)
		throws IOException {
		if (handlers.isEmpty()) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND, "no query handlers available to handle query");
			return;
		}

		try {								       // OK, let's try
		  for (Object handler1 : handlers) {           // Try each query handler
			QueryHandler handler = (QueryHandler) handler1;           // Get the query handler
			query = handler.query(query);                   // Give it the query
			if (!query.getResults().isEmpty()) {               // Did it give any result?
			  Result result = (Result) query.getResults().get(0); // Yes, get the result
			  deliverResult(handler, result, res);           // And deliver it
			  return;                           // Done!
			}
		  }
		} catch (ProductException ex) {
          if (ex instanceof HttpRedirectException) {
            HttpRedirectException hre = (HttpRedirectException) ex;
            res.sendRedirect(hre.getLocation());
          } else {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
          }
          return;
		}

		res.sendError(HttpServletResponse.SC_NOT_FOUND, "no matching products from any query handler");
	}

	/**
	 * Deliver a result.  This streams the product data.
	 *
	 * @param handler Which handler produced the result.
	 * @param result The result.
	 * @param res The HTTP response to which to send the result data.
	 * @throws IOException if an error occurs.
	 */
	private void deliverResult(QueryHandler handler, Result result, HttpServletResponse res) throws IOException {
		characterize(handler, result, res);					       // First, describe it using HTTP headers
		if (result instanceof LargeResult) {				       // Is it a large result?
			LargeResult lr = (LargeResult) result;			       // Yes, this is gonna take some special work
			LargeProductQueryHandler lpqh = (LargeProductQueryHandler) handler; // First treat 'em as large
			ProductRetriever retriever = new ProductRetriever(lpqh);       // Large ones need a retriever
			lr.setRetriever(retriever);				       // Set the retriever
		}
		BufferedInputStream in = null;					       // Start with no input stream
		try {								       // Then try ...
			in = new BufferedInputStream(result.getInputStream());	       // To open the input stream
			byte[] buf = new byte[512];				       // And a byte buffer for data
			int num;						       // And a place to count data
			while ((num = in.read(buf)) != -1)			       // While we read
				res.getOutputStream().write(buf, 0, num);	       // We write
			res.getOutputStream().flush();				       // Flush to commit response
		} finally {							       // And finally
			if (in != null) try {					       // If we opened it
				in.close();					       // Close it
			} catch (IOException ignore) {}				       // Ignoring any error during closing
		}								       // Because come on, it's just closing!
	}									       // For fsck's sake!

	/**
	 * Characterize a result by using HTTP headers.
	 *
	 * @param result Result to characterize.
	 * @param res HTTP response to set headers in.
	 */
	private void characterize(QueryHandler handler, Result result, HttpServletResponse res) {
		String contentType = result.getMimeType();			       // Grab the content type
		res.setContentType(contentType);				       // Set it
		long size = result.getSize();					       // Grab the size
		if (size >= 0)
			res.addHeader("Content-Length", String.valueOf(size));	       // Don't use setContentLength(int)
		if (!displayable(contentType))					       // Finally, if a browser can't show it
			this.suggestFilename(handler, result, res);		       // Then suggest a save-as filename
	}

	/**
	 * Tell if a result is displayable.  This compares its MIME type to a list of MIME
	 * types commonly displayble by browsers.
	 *
	 * @param contentType MIME type.
	 * @return a <code>boolean</code> value.
	 */
	protected static boolean displayable(String contentType) {
	  for (String DISPLAYABLE_TYPE : DISPLAYABLE_TYPES) {
		if (DISPLAYABLE_TYPE.equals(contentType)) {
		  return true;     // Does it match?
		}
	  }
		return false;							       // None of 'em do, it's not displayable
	}

	/**
	 * We can suggest a filename (if the client happens to be a browser) using a
	 * content-disposition header.
	 *
	 * @param res a <code>HttpServletResponse</code> value.
	 */
	protected void suggestFilename(QueryHandler handler, Result result, HttpServletResponse res) {
		
		String resource = result.getResourceID();
		if (resource == null || resource.length() == 0) resource = "product.dat";
		
		// suggest some names based on resource mime type
		String contentType = res.getContentType();
		
		// "zip" mime types
		if (contentType!=null) {
			if (contentType.equals("application/x-compressed") || 
				contentType.equals("application/x-zip-compressed") || 
				contentType.equals("application/zip") || 
				contentType.equals("multipart/x-zip") ) {
				
				// resource = resource.replaceAll("\\..+",".zip"); // replace extension with .zip 
				resource = "products_" + resource + ".zip"; 				
			}
		}
		
		// set "Content-disposition" header
		res.addHeader("Content-disposition", "attachment; filename=\"" + resource + "\"");
		
	}

	/**
	 * MIME types commonly displayable by browsers.
	 */
	private static final String[] DISPLAYABLE_TYPES = {
		"text/plain", "text/richtext", "text/enriched", "text/tab-separated-values", "text/html", "text/xml", "text/rtf",
		"message/rfc822", "message/partial", "message/external-body", "message/news", "message/http",
		"message/delivery-status", "message/disposition-notification", "message/s-http", "application/rtf",
		"application/pdf", "image/jpeg", "image/gif", "image/tiff", "image/png", "audio/basic", "audio/32kadpcm",
		"audio/mpeg", "video/mpeg", "video/quicktime"
	};

	/**
	 * Retriever that retrieves product data over a method call boundary to a large
	 * product query handler.
	 */
	private static class ProductRetriever implements Retriever {
		/**
		 * Creates a new <code>ProductRetriever</code> instance.
		 *
		 * @param handler a <code>LargeProductQueryHandler</code> value.
		 */
		public ProductRetriever(LargeProductQueryHandler handler) {
			this.handler = handler;
		}

		/** {@inheritDoc} */
		public byte[] retrieveChunk(String id, long offset, int len) throws ProductException {
			return handler.retrieveChunk(id, offset, len);
		}

		/** {@inheritDoc} */
		public void close(String id) throws ProductException {
			handler.close(id);
		}

		/** Handler to use. */
		private LargeProductQueryHandler handler;
	}
}

