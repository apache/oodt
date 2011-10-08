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

//OODT imports
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFactory;
import org.apache.oodt.cas.protocol.system.ProtocolManager;
import org.apache.oodt.cas.protocol.verify.ProtocolVerifier;

/**
 * Action for determining whether a given {@link Protocol} or auto-determined {@link Protocol} 
 * via {@link ProtocolManager} can connect and pass verification of the given {@link Verifier}
 * for given site.
 *
 * @author bfoster
 */
public class BasicVerifyAction extends ProtocolAction {
	
	private ProtocolVerifier verifier;
	private ProtocolFactory factory;
	
	private boolean lastVerificationResult;
	
	@Override
	public void performAction(ProtocolManager protocolManager) throws Exception {
		if (factory != null) {
			Protocol protocol = factory.newInstance();
			if (lastVerificationResult = verifier.verify(protocol, getSite(), getAuthentication())) {
				LOG.info("Protocol '" + protocol.getClass().getCanonicalName() + "' PASSED verification!");
			} else {
				LOG.severe("Protocol '" + protocol.getClass().getCanonicalName() + "' FAILED verification!");
			}
		} else {
			Protocol protocol = protocolManager.getProtocolBySite(getSite(), getAuthentication(), verifier);
			if (lastVerificationResult = protocol != null) {
				LOG.info("Protocol '" + protocol.getClass().getCanonicalName() + "' PASSED verification!");
			} else {
				LOG.info("No Protocol determined, FAILED verification!");				
			}
		}
	}

	public void setVerifier(ProtocolVerifier verifier) {
		this.verifier = verifier;
	}
	
	public void setProtocolFactory(ProtocolFactory factory) {
		this.factory = factory;
	}
	
	protected boolean getLastVerificationResults() {
		return lastVerificationResult;
	}
}
