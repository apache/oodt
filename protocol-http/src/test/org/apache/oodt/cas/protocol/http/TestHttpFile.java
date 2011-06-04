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
package org.apache.oodt.cas.protocol.http;

//JUnit imports
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

/**
 * Test class for {@link HttpFile}.
 * 
 * @author bfoster
 */
public class TestHttpFile extends TestCase {

	public void testInitialState() throws MalformedURLException {
		HttpFile parent = new HttpFile("/path/to", false, new URL("http://some-site"));
		HttpFile file = new HttpFile(parent, "/path/to/file", false, new URL("http://some-site"));
		assertNotNull(file.getLink());
		assertEquals("http://some-site", file.getLink().toString());
		assertFalse(file.isDir());
		assertFalse(file.isRelative());
		assertNotNull(file.getParent());
		assertEquals(parent, file.getParent());
	}
	
	public void testNullCase() throws MalformedURLException {
		try {
			 new HttpFile(null, false, new URL("http://some-site"));
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {}
		try {
			 new HttpFile("/path/to/file", false, null);
			fail("Should have thrown an IllegalArgumentException");
		} catch (IllegalArgumentException e) {}
	}
}
