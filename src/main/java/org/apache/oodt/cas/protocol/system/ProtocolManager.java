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

//OODT imports
import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.config.ProtocolConfig;
import org.apache.oodt.cas.protocol.verify.ProtocolVerifier;
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFactory;

//JDK imports
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A Manager responsible for managing site to {@link ProtocolFactory} mapping
 *
 * @author bfoster
 */
public class ProtocolManager {

	private static final Logger LOG = Logger.getLogger(ProtocolManager.class.getName());
	
	private ProtocolConfig protocolConfig;
	private Map<URI, ProtocolFactory> verifiedMap;
	
    public ProtocolManager(ProtocolConfig protocolConfig) {
    	this.protocolConfig = protocolConfig;
    	verifiedMap = new HashMap<URI, ProtocolFactory>();
    }
    
    public ProtocolConfig getConfig() {
    	return protocolConfig;
    }
    
    public Protocol getProtocolBySite(URI site, Authentication auth, ProtocolVerifier verifier) {
    	if (verifiedMap.containsKey(site)) {
    		return verifiedMap.get(site).newInstance();
    	} else {
    		for (ProtocolFactory factory : protocolConfig.getFactoriesBySite(site)) {
    			try {
    				Protocol protocol = factory.newInstance();
    				if (verifier.verify(protocol, site, auth)) {
    					verifiedMap.put(site, factory);
    					return protocol;
    				}
    			} catch (Exception e) {
					LOG.warning("Failed to create/verify protocol from factory '"
							+ factory.getClass().getCanonicalName()
							+ "' : "
							+ e.getMessage());
    			}
    		}
        	return null;
    	}
    }
    
    public void setProtocol(URI site, ProtocolFactory factory) {
    	verifiedMap.put(site, factory);
    }
}
