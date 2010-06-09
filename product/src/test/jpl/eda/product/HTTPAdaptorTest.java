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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import jpl.eda.io.NullInputStream;
import jpl.eda.io.NullOutputStream;
import jpl.eda.xmlquery.Result;
import jpl.eda.xmlquery.XMLQuery;
import junit.framework.TestCase;

/**
 * Unit test for the HTTPAdaptor class.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class HTTPAdaptorTest extends TestCase {
	/**
	 * Creates a new {@link HTTPAdaptorTest} instance.
	 *
	 * @param name Case name.
	 */
	public HTTPAdaptorTest(String name) {
		super(name);
	}

	/**
	 * Test if the HTTPAdaptor handles the content-disposition header correctly.
	 */
	public void testContentDisposition() throws Throwable {
		HTTPAdaptor a = new HTTPAdaptor(new URL(null, "httptest://testhttp", new Handler()));
		Server s = a.createServer();
		XMLQuery q = new XMLQuery("fish = 1", null, null, null, null, null, null, null, 99);
		q = s.query(q);
		assertEquals("HTTPAdaptor is not parsing content-disposition and setting resource ID from it.",
			"bite.me", ((Result) q.getResults().get(0)).getResourceID());
	}
	
	/** A stream handler for the unit test. */
	public static class Handler extends URLStreamHandler {
		/** Make a new handler instance. */
		public Handler() {}

		/** {@inheritDoc} */
		protected URLConnection openConnection(URL url) {
			return new TestConnection(url);
		}

		/** Pseudo-HTTP connection that sets the content-disposition header and otherwise does nothing else. */
		private static class TestConnection extends HttpURLConnection {
			/** Make the pseudo-HTTP connection. */
			public TestConnection(URL url) {
				super (url);
			}

			/** Connection succeeds. */
			public void connect() {}

			/** No data. */
			public int getContentLength() {
				return 0;
			}

			/** It's binary data. */
			public String getContentType() {
				return "application/octet-stream";
			}

			/** Return only a few headers. */
			public String getHeaderField(String name) {
				name = name.toLowerCase();
				if ("content-disposition".equals(name))
					return "attachment; filename=\"bite.me\"";
				else if ("content-type".equals(name))
					return "application/octet-stream";
				else if ("content-length".equals(name))
					return "0";
				return null;
			}

			/** No data to read. */
			public InputStream getInputStream() {
				return new NullInputStream();
			}

			/** No data to write. */
			public OutputStream getOutputStream() {
				return new NullOutputStream();
			}

			/** Disconnecting works fine. */
			public void disconnect() {}

			/** No proxy involved. */
			public boolean usingProxy() {
				return false;
			}

			/** It's always 200 OK. */
			public int getResponseCode() {
				return HttpURLConnection.HTTP_OK;
			}
		}
	} 
}
