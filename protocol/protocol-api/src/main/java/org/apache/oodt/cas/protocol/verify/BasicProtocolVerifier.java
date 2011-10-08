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
package org.apache.oodt.cas.protocol.verify;

//JDK imports
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFile;
import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.exceptions.ProtocolException;

/**
 * {@link ProtocolVerifier} which performs basic verification of a given {@link Protocol} to
 * a given site.  It check that {@link Protocol} can connect to the site, cd to a directory,
 * cd back to HOME directory, and able to perform an ls and pwd.
 * 
 * @author bfoster
 */
public class BasicProtocolVerifier implements ProtocolVerifier {

	private static final Logger LOG = Logger.getLogger(BasicProtocolVerifier.class.getName());
	
	private Map<URI, ProtocolFile> uriTestCdMap;
	
	public BasicProtocolVerifier(Map<URI, ProtocolFile> uriTestCdMap) {
		this.uriTestCdMap = uriTestCdMap;
	}

	public boolean verify(Protocol protocol, URI site, Authentication auth) {
        try {
            LOG.log(Level.INFO, "Testing protocol "
                    + protocol.getClass().getCanonicalName()
                    + " . . . this may take a few minutes . . .");
            
            // Test connectivity
            protocol.connect(site.getHost(), auth);
            
            // Test ls, cd, and pwd
            protocol.cdHome();
            ProtocolFile home = protocol.pwd();
            protocol.ls();
            if (uriTestCdMap.containsKey(site)) {
            	protocol.cd(uriTestCdMap.get(site));
            } else {
            	protocol.cdHome();
            }
            protocol.cdHome();
            
            // Verify again at home directory
            if (home == null || !home.equals(protocol.pwd()))
                throw new ProtocolException(
                        "Home directory not the same after cd");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Protocol "
                    + protocol.getClass().getCanonicalName()
                    + " failed compatibility test : " + e.getMessage(), e);
            return false;
        } finally {
        	try { protocol.close(); } catch (Exception e) {}
        }
        return true;
    }

}
