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

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Vector;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import jpl.eda.xmlquery.XMLQuery;

/**
 * Statistics recorder to an HTTP form-data acceptor.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
class HTTPFormRecorder implements Recorder {
	/**
	 * Creates a new <code>HTTPFormRecorder</code> instance.
	 *
	 * @param servletConfig a <code>ServletConfig</code> value.
	 * @throws ServletException if an error occurs.
	 */
	public HTTPFormRecorder(ServletConfig servletConfig) throws ServletException {
		try {
			String urlStr = servletConfig.getInitParameter("recorder.url");
			url = new URL(urlStr == null? "http://oodt.jpl.nasa.gov/data-stats/recorder" : urlStr);

			groupID = servletConfig.getInitParameter("recorder.id");
			if (groupID == null)
				groupID = "oodt";
		} catch (MalformedURLException ex) {
			throw new ServletException(ex);
		}
	}

	public void record(final Transaction transaction) {
		Thread t = new Thread() {
			public void run() {
				HttpURLConnection connection = null;
				try {
					String kwdQuery = transaction.getQuery().getKwdQueryString();
					StringBuffer req = new StringBuffer("groupID=").append(URLEncoder.encode(groupID, "UTF-8"))
						.append("&serverID=").append(URLEncoder.encode(transaction.getServerID(), "UTF-8"))
						.append("&keywordQuery=").append(URLEncoder.encode(kwdQuery, "UTF-8"))
						.append("&startTime=").append(transaction.getStartTime().getTime())
						.append("&numMatches=").append(transaction.getNumMatches())
						.append("&numBytes=").append(transaction.getNumBytes())
						.append("&ttc=").append(transaction.getTimeToComplete());

					connection = (HttpURLConnection) url.openConnection();
					connection.setDoInput(true);
					connection.setDoOutput(true);
					connection.setRequestProperty("Content-length", String.valueOf(req.length()));
					connection.setRequestMethod("POST");

					connection.connect();
					OutputStream out = connection.getOutputStream();
					out.write(req.toString().getBytes());
					out.close();

					int rc = connection.getResponseCode();
					if (rc != HttpURLConnection.HTTP_OK)
						throw new IOException("HTTP server response code " + rc);
				} catch (IOException ex) {
					throw new IllegalStateException("Unexpected IOException: " + ex.getMessage());
				} finally {
					if (connection != null)
						connection.disconnect();
				}
			}
		};
		try {
			t.start();
			t.join(MAX_TIME);
			if (t.isAlive()) t.interrupt();
		} catch (InterruptedException ex) {
			if (t.isAlive()) t.interrupt();
		}
	}

	public String toString() {
		return "HTTPFormRecorder[url=" + url + ",groupID=" + groupID + "]";
	}

	/** URL to which to send statistics. */
	protected URL url;

	/** Group ID under which to log statistics. */
	protected String groupID;

	/** How long to try to record a transaction before giving up, in milliseconds. */
	private static final long MAX_TIME = Long.getLong("jpl.eda.product.HTTPFormRecorder.maxTime", 500).longValue();
}
