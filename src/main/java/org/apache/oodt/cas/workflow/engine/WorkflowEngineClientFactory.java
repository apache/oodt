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
package org.apache.oodt.cas.workflow.engine;

//OODT imports
import org.apache.oodt.cas.workflow.server.channel.CommunicationChannelClientFactory;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * Factory for creating client WorkflowEngine
 * 
 */
public class WorkflowEngineClientFactory implements WorkflowEngineFactory {

	private CommunicationChannelClientFactory communicationChannelClientFactory;
	private int autoPagerSize;
	
	public WorkflowEngineClient createEngine() {
		WorkflowEngineClient client = new WorkflowEngineClient();
		client.setCommunicationChannelClient(this.communicationChannelClientFactory.createCommunicationChannelClient());
		return client;
	}

	public CommunicationChannelClientFactory getCommunicationChannelClientFactory() {
		return this.communicationChannelClientFactory;
	}

	public void setCommunicationChannelClientFactory(CommunicationChannelClientFactory communicationChannelClientFactory) {
		this.communicationChannelClientFactory = communicationChannelClientFactory;
	}

	public int getAutoPagerSize() {
		return autoPagerSize;
	}

	public void setAutoPagerSize(int autoPagerSize) {
		this.autoPagerSize = autoPagerSize;
	}
	
	public String getServerUrl() {
		return this.communicationChannelClientFactory.getServerUrl();
	}
	
}
