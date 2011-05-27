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
package org.apache.oodt.cas.protocol.system;

//JDK imports
import java.net.URI;
import java.net.URISyntaxException;

//OODT imports
import org.apache.oodt.cas.protocol.MockProtocol;
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.auth.NoAuthentication;
import org.apache.oodt.cas.protocol.config.MockSpringProtocolConfig;
import org.apache.oodt.cas.protocol.verify.ProtocolVerifier;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link ProtocolManager}.
 * 
 * @author bfoster
 */
public class TestProtocolManager extends TestCase {
	
	private ProtocolManager protocolManager;
	
	@Override
	public void setUp() {
		protocolManager = new ProtocolManager(new MockSpringProtocolConfig());
	}
	
	public void testInitialState() {
		assertNotNull(protocolManager.getConfig());
	}
	
	public void testProtocolFactoryMapping() throws URISyntaxException {
		Protocol protocol = protocolManager.getProtocolBySite(new URI("ftp://localhost"), new NoAuthentication(), null);
		assertNotNull(protocol);
		assertTrue(protocol instanceof MockProtocol);
		MockProtocol mockProtocol = (MockProtocol) protocol;
		assertEquals("ftp1", mockProtocol.getFactoryId());
		
		//test that ftp1 was memorized and is returned again even though a Verifier was supplied this time that would return ftp3
		protocol = protocolManager.getProtocolBySite(new URI("ftp://localhost"), new NoAuthentication(), new ProtocolVerifier() {
			public boolean verify(Protocol protocol, URI site,
					Authentication auth) {
				if (protocol instanceof MockProtocol) {
					MockProtocol mockProtocol = (MockProtocol) protocol;
					return mockProtocol.getFactoryId().equals("ftp3");
				} else {
					return false;
				}
			}
		});
		assertNotNull(protocol);
		assertTrue(protocol instanceof MockProtocol);
		mockProtocol = (MockProtocol) protocol;
		assertEquals("ftp1", mockProtocol.getFactoryId());

	}
	
	public void testVerifier() throws URISyntaxException {
		Protocol protocol = protocolManager.getProtocolBySite(new URI("ftp://localhost"), new NoAuthentication(), new ProtocolVerifier() {
			public boolean verify(Protocol protocol, URI site,
					Authentication auth) {
				if (protocol instanceof MockProtocol) {
					MockProtocol mockProtocol = (MockProtocol) protocol;
					return mockProtocol.getFactoryId().equals("ftp3");
				} else {
					return false;
				}
			}
		});
		assertTrue(protocol instanceof MockProtocol);
		MockProtocol mockProtocol = (MockProtocol) protocol;
		assertEquals("ftp3", mockProtocol.getFactoryId());
	}
	
}
