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
import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.config.ProtocolConfig;
import org.apache.oodt.cas.protocol.verify.ProtocolVerifier;
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFactory;

//JDK imports
import java.net.URI;
import java.util.HashMap;
import java.util.List;
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
    	Validate.notNull(protocolConfig, "protocolConfig must not be NULL");
    	this.protocolConfig = protocolConfig;
    	verifiedMap = new HashMap<URI, ProtocolFactory>();
    }
    
    public ProtocolConfig getConfig() {
    	return protocolConfig;
    }
    
    public List<ProtocolFactory> getFactories() {
    	return protocolConfig.getAllFactories();
    }
    
    /**
     * Determines/creates the appropriate {@link Protocol} for the given site and {@link Authentication}.
     * {@link ProtocolVerifier} is run and the first {@link Protocol} to pass its verification, will be
     * returned already connected to the given site.  If a {@link Protocol} is returned once for a given
     * site/Authentication combination, then it will be remember next time this method is called an
     * {@link ProtocolVerifier} will not be run (assumed pass).
     * 
     * @param site The URI for which a {@link Protocol} will be created
     * @param auth The connection {@link Authentication} to be used to connect to the given site
     * @param verifier The {@link ProtocolVerifier} which any {@link Protocol} must pass to be returned 
     * 			as the approved {@link Protocol} for the given site and {@link Authentication}; may be null,
     * 			in which case as long as the {@link Protocol} can connect to the site it is considered a pass
     * 			as will be returned
     * @return A verified {@link Protocol} for the given site and {@link Authentication}, otherwise null if
     * 	no {@link Protocol} could be determined.
     */
    public Protocol getProtocolBySite(URI site, Authentication auth, ProtocolVerifier verifier) {
    	if (verifiedMap.containsKey(site)) {
    		return verifiedMap.get(site).newInstance();
    	} else {
    		for (ProtocolFactory factory : protocolConfig.getFactoriesBySite(site)) {
    			Protocol protocol = null;
    			try {
    				protocol = factory.newInstance();
    				if (verifier == null || verifier.verify(protocol, site, auth)) {
    					verifiedMap.put(site, factory);
    					if (protocol.connected()) {
    						protocol.close();
    					}
    					protocol.connect(site.getHost(), auth);
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
    
    /**
     * 
     * @param site
     * @param auth
     * @param factory
     * @throws IllegalArgumentException if any of the arguments are null
     */
    public void setProtocol(URI site, Authentication auth, ProtocolFactory factory) {
    	Validate.notNull(site, "site must not be NULL");
    	Validate.notNull(auth, "auth must not be NULL");
    	Validate.notNull(factory, "factory must not be NULL");
    	verifiedMap.put(site, factory);
    }
}
