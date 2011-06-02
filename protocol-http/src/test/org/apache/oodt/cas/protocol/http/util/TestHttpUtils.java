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
package org.apache.oodt.cas.protocol.http.util;

//JDK imports
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

//JUnits imports
import junit.framework.TestCase;

/**
 * Test class for {@link HttpUtils}.
 *
 * @author bfoster
 */
public class TestHttpUtils extends TestCase {
	
	private static final String URL_OF_THIS_TEST = "http://svn.apache.org/repos/asf/oodt/branches/protocol/protocol-http/src/main/java/org/apache/oodt/cas/protocol/http/util/TestHttpUtils.java";
	
	public void testResolveUri() throws URISyntaxException {
		URI baseUri = new URI("http://localhost/base/directory/");
		
		// Test absolute resolve.
		URI resolvedAbsoluteUri = HttpUtils.resolveUri(baseUri, "/path/to/file");
		assertEquals("http://localhost/path/to/file", resolvedAbsoluteUri.toString());
		
		// Test relative resolve.
		URI resolvedRelativeUri = HttpUtils.resolveUri(baseUri, "path/to/file");
		assertEquals("http://localhost/base/directory/path/to/file", resolvedRelativeUri.toString());

		// Test relative with base not ending in /
		baseUri = new URI("http://localhost/base/directory");
		assertEquals("http://localhost/base/directory/path/to/file", resolvedRelativeUri.toString());
	}
	
	public void testConnectUrl() throws MalformedURLException, IOException {
		HttpURLConnection conn = HttpUtils.connect(new URL(URL_OF_THIS_TEST));
		assertNotSame(0, conn.getDate());
		String urlText = HttpUtils.readUrl(conn);
		assertTrue(urlText.contains("public class TestHttpUtils extends TestCase {"));
		conn.disconnect();
	}

}
