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
import java.util.List;

import org.apache.oodt.cas.protocol.ProtocolFile;
import org.apache.oodt.cas.protocol.auth.NoAuthentication;
import org.apache.oodt.cas.protocol.exceptions.ProtocolException;

import junit.framework.TestCase;

/**
 * Test class for {@link HttpProtocol}.
 * 
 * @author bfoster
 */
public class TestHttpProtocol extends TestCase {

	public void testConnection() throws InstantiationException, ProtocolException {
		HttpProtocol httpProtocol = new HttpProtocol();
		assertFalse(httpProtocol.connected());
		httpProtocol.connect("svn.apache.org", new NoAuthentication());
		assertTrue(httpProtocol.connected());
	}
	
	public void testLS() throws ProtocolException, InstantiationException {
		HttpProtocol httpProtocol = new HttpProtocol();
		httpProtocol.connect("svn.apache.org", new NoAuthentication());
		assertTrue(httpProtocol.connected());
		httpProtocol.cd(new ProtocolFile("repos/asf/oodt/branches/protocol/protocol-http/src/main/java/org/apache/oodt/cas/protocol/http", true));
		List<ProtocolFile> files = httpProtocol.ls();
		boolean foundFile = false;
		for (ProtocolFile file : files) {
			if (file.getName().equals("HttpProtocol.java")) {
				foundFile = true;
				break;
			}
		}
		assertTrue(foundFile);
	}
}
