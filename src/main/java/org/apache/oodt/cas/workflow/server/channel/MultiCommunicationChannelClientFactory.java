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
package org.apache.oodt.cas.workflow.server.channel;

//JDK imports
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author bfoster
 *
 */
public class MultiCommunicationChannelClientFactory implements
		CommunicationChannelClientFactory {

	private static final Logger LOG = Logger.getLogger(MultiCommunicationChannelClientFactory.class.getName());
	
	private String serverUrl;
	private CommunicationChannelClientFactory useClientFactory;
	private List<CommunicationChannelClientFactory> clientFactories;
	
	public MultiCommunicationChannelClient createCommunicationChannelClient() {
		try {
			CommunicationChannelClient useClient = this.useClientFactory.createCommunicationChannelClient();
			Vector<CommunicationChannelClient> clients = new Vector<CommunicationChannelClient>();
			URL url = new URL(this.serverUrl);
			int portCounter = url.getPort();
			for (CommunicationChannelClientFactory factory : this.clientFactories) {
				factory.setServerUrl(url.getProtocol() + "://" + url.getHost() + ":" + portCounter++);
				clients.add(factory.createCommunicationChannelClient());
			}
			return new MultiCommunicationChannelClient(useClient, clients);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to create '" + MultiCommunicationChannelClientFactory.class.getName() + " : " + e.getMessage(), e);
			return null;
		}
	}

	public String getServerUrl() {
		return this.serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	
	public void setUseClientFactory(CommunicationChannelClientFactory useClientFactory) {
		this.useClientFactory = useClientFactory;
	}
	
	public void setClientFactories(List<CommunicationChannelClientFactory> clientFactories) {
		this.clientFactories = clientFactories;
	}

}
