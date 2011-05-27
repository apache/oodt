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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.auth.BasicAuthentication;
import org.apache.oodt.cas.protocol.auth.NoAuthentication;
import org.apache.oodt.cas.protocol.system.ProtocolManager;
import org.apache.oodt.commons.spring.SpringSetIdInjectionType;

/**
 * Action used to perform some task via a {@link ProtocolManager}
 *
 * @author bfoster
 */
public abstract class ProtocolAction implements SpringSetIdInjectionType {
	
	protected static final Logger LOG = Logger.getLogger(ProtocolAction.class.getName());
	
	private String id;
	private String user;
	private String pass;
	private String site;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
		
	public void setUser(String user) {
		this.user = user;
	}
	
	public void setPass(String pass) {
		this.pass = pass;
	}
	
	public void setSite(String site) {
		this.site = site;
	}

	public URI getSite() throws URISyntaxException {
		return site != null ? new URI(site) : null;
	}
	
	public Authentication getAuthentication() {
		if (user == null || pass == null) {
			return new NoAuthentication();
		} else {
			return new BasicAuthentication(user, pass);
		}
	}
	
	public abstract void performAction(ProtocolManager protocolManager) throws Exception;
	
}
