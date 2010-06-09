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


package jpl.eda.product;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jpl.eda.Configuration;
import jpl.eda.EDAException;
import jpl.eda.util.SAXParser;
import jpl.eda.util.XML;
import jpl.eda.xmlquery.Result;
import jpl.eda.xmlquery.XMLQuery;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/** Servlet that returns products in response to OODT queries.
 *
 * This servlet takes a <code>keywordQuery</code> parameter which is the DIS-style keyword
 * query string, and an <code>object</code> parameter which is the CORBA object name of
 * the object to receive the query.
 *
 * <p>It also takes the following optional parameters:
 * <ul>
 *   <li><code>ns</code>: name server to use, defaulting to the system nameserver in the
 *   <code>.edarc.xml</code> file.</li>
 *
 *   <li><code>id</code>: product ID to return in the case of multiple products.</li>
 *
 *   <li><code>mimeType</code>: desired MIME type of product.  This can parameter may be
 *   specified multiple times to show an in-order preference for MIME types.</li>
 * </ul>
 *
 * <p>It responds with a single product in the MIME type of the product.
 *
 * @author Kelly
 */
public class ProductServlet extends HttpServlet {
	/** Default name server URL. */
	private static String defaultNameServerURL;

	/** Initialize by finding the default name server URL. */
	static {
		defaultNameServerURL = "corbaloc::localhost/" + jpl.eda.util.CORBAMgr.NS_OBJECT_KEY;
		try {
			Configuration configuration = Configuration.getConfiguration();
			configuration.mergeProperties(System.getProperties());
			defaultNameServerURL = configuration.getNameServerURL();
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			System.err.println("Exception " + ex.getClass().getName() + " while reading configuration: "
				+ ex.getMessage());
		}

	}

	/** Construct the product servlet.
	 */
	public ProductServlet() {}

	public void init(ServletConfig servletConfig) throws ServletException {
		displayableTypes = new HashSet();
		try {
			SAXParser p = XML.createSAXParser();
			p.setContentHandler(new ContentHandler() {
				private StringBuffer token = new StringBuffer();
				private String pi;
				public void characters(char[] ch, int start, int length) {
					token.append(ch, start, length);
				}
				public void endDocument() {}
				public void endElement(String ns, String name, String qual) {
					displayableTypes.add(token.toString().trim());
					token.delete(0, token.length());
				}
				public void endPrefixMapping(String prefix) {}
				public void ignorableWhitespace(char[] ch, int start, int length) {}
				public void processingInstruction(String target, String data) {}
				public void setDocumentLocator(Locator locator) {}
				public void skippedEntity(String name) {}
				public void startDocument() {}
				public void startElement(String ns, String name, String qual, Attributes atts) {}
				public void startPrefixMapping(String prefix, String uri) {}
			});
			p.parse(new InputSource(getClass().getResourceAsStream("displayableTypes.xml")));
		} catch (SAXException ex) {
			throw new ServletException(ex);
		} catch (IOException ex) {
			throw new ServletException(ex);
		}

		try {
			jpl.eda.ExecServer.runInitializers();
		} catch (EDAException ex) {
			throw new ServletException(ex);
		}

		preferredNamespace = System.getProperty("jpl.eda.servlets.ProductServlet.preferredNamespace", "urn:eda:rmi:");

		String recorderList = servletConfig.getInitParameter("recorders");
		if (recorderList == null) recorderList = "";
		recorders = new ArrayList();
		for (Iterator i = jpl.eda.util.Utility.parseCommaList(recorderList); i.hasNext();) try {
			String recorderClassName = (String) i.next();
			Class recorderClass = Class.forName(recorderClassName);
			Constructor ctor = recorderClass.getConstructor(new Class[]{ ServletConfig.class });
			Recorder recorder = (Recorder) ctor.newInstance(new Object[]{ servletConfig });
			recorders.add(recorder);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ServletException(ex);
		}

		super.init(servletConfig);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doIt(req, res);
	}
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doIt(req, res);
	}

	/** Process the request for the query parameters, query, and deliver a product.
	 *
	 * @param req Servlet request.
	 * @param res Servlet response.
	 * @throws ServletException If there's something wrong with the request or response.
	 * @throws IOException If there's an I/O error writing the image.
	 */
	private void doIt(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		Date startTime = new Date();
		String ns = null;
		String object = null;
		String host = req.getRemoteHost();
		try {
			object = req.getParameter("object");
			if (object == null)
				throw new IllegalArgumentException("required object parameter not specified");
			if (!object.startsWith("urn:"))
				object = preferredNamespace + object;
			String keywordQuery = req.getParameter("keywordQuery");
			if (keywordQuery == null)
				throw new IllegalArgumentException("required keywordQuery parameter not specified");
			String id = req.getParameter("id");
			String[] mimeAccept = req.getParameterValues("mimeType");
			if (mimeAccept == null) mimeAccept = new String[]{"*/*"};
			List mimeAcceptList = Arrays.asList(mimeAccept);

			// Construct the query from the parameters and send it in.
			XMLQuery xmlQuery = new XMLQuery(keywordQuery, "queryServlet", "Query from QueryServlet",
				"This query comes from the query servlet currently handling a client at "
				+ req.getRemoteHost(), /*ddID*/null, /*resultModeID*/null, /*propType*/null, /*propLevels*/null,
				XMLQuery.DEFAULT_MAX_RESULTS, mimeAcceptList);
			ProductClient pc = new ProductClient(object);
			xmlQuery = pc.query(xmlQuery);

			// Get the results of the query, if any.
			List results = xmlQuery.getResults();
			boolean found;
			long resultSize = 0L;
			if (results.isEmpty()) {
				res.sendError(HttpServletResponse.SC_NOT_FOUND, "No matching results to query \"" + keywordQuery
					+ "\" for object \"" + object + "\"");
				found = false;
			} else {
				// Default to the first result.  Then look for a specific result, if specified.
				Result result = (Result) results.get(0);
				if (id != null) {
					for (Iterator i = results.iterator(); i.hasNext();) {
						Result r = (Result) i.next();
						if (id.equals(r.getID())) {
							result = r;
							break;
						}
					}
					throw new IllegalArgumentException("Result with ID " + id + " not in results list");
				}

				// Characterize.
				res.setContentType(result.getMimeType());
				if (!displayableByBrowser(result.getMimeType()))
					suggestFilename(result.getResourceID(), res);
				resultSize = result.getSize();
				if (resultSize <= Integer.MAX_VALUE)
					res.setContentLength((int) resultSize);

				// Deliver.
				BufferedInputStream productInputStream = null;
				try {
					productInputStream = new BufferedInputStream(result.getInputStream());
					byte[] buf = new byte[512];
					int numRead;
					while ((numRead = productInputStream.read(buf)) != -1)
						res.getOutputStream().write(buf, 0, numRead);
					productInputStream.close();
					found = true;
				} finally {
					if (productInputStream != null) try {
						productInputStream.close();
					} catch (IOException ignore) {}
				}
			}

			// Record the transaction for posterity (oh, and statistical analysis of our software, too).
			record(new Transaction(object, xmlQuery, startTime, found? 1 : 0, resultSize,
				       System.currentTimeMillis() - startTime.getTime()));
		} catch (IllegalArgumentException ex) {
			System.err.println(host + ": illegal argument: " + ex.getMessage());
			res.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
		} catch (ProductException ex) {
			System.err.println("Product exception: " + ex.getMessage());
			ex.printStackTrace();
			System.err.println(host + ": product exception: " + ex.getMessage());
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Query error: " + ex.getMessage());
		} catch (org.omg.CORBA.SystemException ex) {
			System.err.println("I hate CORBA: " + ex.getMessage());
			ex.printStackTrace();
			System.err.println(host + ": CORBA system exception: " + ex.getClass().getName() + ": " + ex.getMessage());
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CORBA system exception \""
				+ ex.getClass().getName() + "\" communicating with object \"" + object
				+ "\" via name server \"" + ns + "\": " + ex);
		}
	}

	/**
	 * Record the transaction for stastistical purposes.
	 *
	 * @param transaction a <code>Transaction</code> value.
	 */
	private void record(Transaction transaction) {
		if (recorders.isEmpty()) return;
		new RecordingThread(transaction).start();
	}

	/**
	 * Tell if the given MIME type is generally displayable by the majority of web browsers.
	 *
	 * @param mimeType a <code>String</code> value
	 * @return True if the type named by <var>mimeType</var> is generally displayable
	 */
	private boolean displayableByBrowser(String mimeType) {
		return displayableTypes.contains(mimeType);
	}

	/**
	 * Add a header to suggest a filename to the browser.
	 *
	 * @param resourceID The name of the resource to suggest.
	 * @param res The response in which to suggest the filename.
	 */
	private void suggestFilename(String resourceID, HttpServletResponse res) {
		if (resourceID == null || resourceID.length() <= 0)
			resourceID = "product.dat";
		res.addHeader("Content-disposition", "attachment; filename=\"" + resourceID + "\"");
	}

	/** Set of MIME types ({@link String}s) generally displayable by browsers. */
	private Set displayableTypes;

	/** Preferred namespace prefixed to unqualified URNs. */
	private String preferredNamespace;

	/** List of statistical {@link Recorder}s. */
	private List recorders;

	/** Size of chunks to retrieve from product servers. */
	private static final int CHUNK_SIZE = Integer.getInteger("jpl.eda.servlets.ProductServlet.chunkSize", 32768).intValue();

	/**
	 * Background thread to record statistics.
	 */
	private class RecordingThread extends Thread {
		/**
		 * Creates a new <code>RecordingThread</code> instance.
		 *
		 * @param transaction Transaction to record.
		 */
		public RecordingThread(Transaction transaction) {
			super("Recording " + transaction);
			this.transaction = transaction;
		}

		public void run() {
			Recorder recorder = null;
			for (Iterator i = recorders.iterator(); i.hasNext();) try {
				recorder = (Recorder) i.next();
				recorder.record(transaction);
			} catch (StatsException ex) {
				System.err.println("Warning: " + recorder + " unable to record transaction " + transaction);
			}
		}

		/** Transaction to record. */
		private Transaction transaction;
	}
}
