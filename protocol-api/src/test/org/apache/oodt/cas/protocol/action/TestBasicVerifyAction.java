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
package org.apache.oodt.cas.protocol.action;

//JUnit imports
import java.net.URI;

import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.config.MockSpringProtocolConfig;
import org.apache.oodt.cas.protocol.system.ProtocolManager;
import org.apache.oodt.cas.protocol.verify.ProtocolVerifier;

import junit.framework.TestCase;

/**
 * Test class for {@link BasicVerifyAction}
 * 
 * @author bfoster
 */
public class TestBasicVerifyAction extends TestCase {

	public void testVerification() throws Exception {
		BasicVerifyAction bva = new BasicVerifyAction();
		bva.setSite("http://localhost");
		bva.setVerifier(new ProtocolVerifier() {
			public boolean verify(Protocol protocol, URI site,
					Authentication auth) {
				return auth != null && site.toString().equals("http://localhost");
			}
		});
		bva.performAction(new ProtocolManager(new MockSpringProtocolConfig()));
		assertTrue(bva.getLastVerificationResults());
	}
	
}
