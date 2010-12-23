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
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.workflow.engine.WorkflowEngine;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineFactory;

/**
 * 
 * @author bfoster
 *
 */
public class MultiCommunicationChannelServerFactory implements
		CommunicationChannelServerFactory {

	private WorkflowEngineFactory workflowEngineFactory;
	private int port;
	private List<CommunicationChannelServerFactory> serverFactories;
	
	public CommunicationChannelServer createCommunicationChannelServer() {
		try {
			if (serverFactories != null) {
				WorkflowEngine engine = workflowEngineFactory.createEngine();
				Vector<CommunicationChannelServer> servers = new Vector<CommunicationChannelServer>();
				int portCounter = port;
				for (CommunicationChannelServerFactory factory : this.serverFactories) {
					factory.setWorkflowEngineFactory(new WorkflowEngineFactory() {
						public WorkflowEngine createEngine() {
							return null;
						}
					});
					CommunicationChannelServer server = factory.createCommunicationChannelServer();
					server.setWorkflowEngine(engine);
					server.setPort(portCounter++);
					servers.add(server);
				}
				return new MultiCommunicationChannelServer(servers);
			}else {
				throw new Exception("Must set server factories!");
			}
		}catch (Exception e) {
			return null;
		}
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setWorkflowEngineFactory(
			WorkflowEngineFactory workflowEngineFactory) {
		this.workflowEngineFactory = workflowEngineFactory;
	}
	
	public void setServerFactories(List<CommunicationChannelServerFactory> serverFactories) {
		this.serverFactories = serverFactories;
	}
	
	public List<CommunicationChannelServerFactory> getServerFactories() {
		return this.serverFactories;
	}

}
