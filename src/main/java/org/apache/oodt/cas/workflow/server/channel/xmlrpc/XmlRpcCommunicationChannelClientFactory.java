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
package org.apache.oodt.cas.workflow.server.channel.xmlrpc;

//JDK imports
import java.net.URL;

//OODT imports
import org.apache.oodt.cas.workflow.server.channel.CommunicationChannelClientFactory;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Factory for creating XML-RPC communication channel client
 * <p>
 */
public class XmlRpcCommunicationChannelClientFactory implements
		CommunicationChannelClientFactory {

	private String serverUrl = "http://localhost:9000";
	private int connectionTimeout = 60;
	private int requestTimeout = 90;
	private int chunkSize = 1024;
	private int connectionRetries = 3;
	private int connectionRetryIntervalSecs = 0;
	
	public XmlRpcCommunicationChannelClient createCommunicationChannelClient() {
		try {
			return new XmlRpcCommunicationChannelClient(new URL(this.serverUrl), this.connectionTimeout, this.requestTimeout, this.chunkSize, this.connectionRetries, this.connectionRetryIntervalSecs);
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getServerUrl() {
		return this.serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public int getConnectionRetries() {
		return connectionRetries;
	}

	public void setConnectionRetries(int connectionRetries) {
		this.connectionRetries = connectionRetries;
	}

	public int getConnectionRetryIntervalSecs() {
		return connectionRetryIntervalSecs;
	}

	public void setConnectionRetryIntervalSecs(int connectionRetryIntervalSecs) {
		this.connectionRetryIntervalSecs = connectionRetryIntervalSecs;
	}

}
