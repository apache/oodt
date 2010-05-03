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

package gov.nasa.jpl.oodt.cas.catalog.server.channel.xmlrpc;

//JDK imports
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

//OODT imports
import gov.nasa.jpl.oodt.cas.catalog.server.channel.CommunicationChannelClient;
import gov.nasa.jpl.oodt.cas.catalog.server.channel.CommunicationChannelClientFactory;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Factory for creating XmlRpcCommunicationChannelServer
 * <p>
 */
public class XmlRpcCommunicationChannelClientFactory implements
		CommunicationChannelClientFactory {
	
	private static Logger LOG = Logger.getLogger(XmlRpcCommunicationChannelClientFactory.class.getName());
	
	protected String serverUrl;
	protected int connectionTimeout;
	protected int requestTimeout;
	protected int chunkSize;
	
	public XmlRpcCommunicationChannelClientFactory() {}
	
	public CommunicationChannelClient createCommunicationChannelClient() {
		try {
			return new XmlRpcCommunicationChannelClient(new URL(this.serverUrl), this.connectionTimeout, this.requestTimeout, this.chunkSize);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to create XmlRpcCommunicationChannelClient : " + e.getMessage(), e);
			return null;
		}
	}
	
	@Required
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	
	public String getServerUrl() {
		return this.serverUrl;
	}
	
	/**
	 * @param connectionTimeout timeout for client in minutes
	 */
	@Required
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
	
	/**
	 * 
	 * @param requestTimeout timout for client in minutes
	 */
	@Required
	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}
	
	@Required
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

}
