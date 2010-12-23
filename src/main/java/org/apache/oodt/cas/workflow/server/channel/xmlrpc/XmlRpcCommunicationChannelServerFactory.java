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

//OODT imports
import org.apache.oodt.cas.workflow.engine.WorkflowEngineFactory;
import org.apache.oodt.cas.workflow.server.channel.CommunicationChannelServerFactory;


/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Factory for creating XML-RPC communication channel server
 * <p>
 */
public class XmlRpcCommunicationChannelServerFactory implements
		CommunicationChannelServerFactory {

	private int port;
	private WorkflowEngineFactory workflowEngineFactory;
	
	public XmlRpcCommunicationChannelServer createCommunicationChannelServer() {
		XmlRpcCommunicationChannelServer xmlrpcServer = new XmlRpcCommunicationChannelServer();
		xmlrpcServer.setPort(this.port);
		xmlrpcServer.setWorkflowEngine(this.workflowEngineFactory.createEngine());
		return xmlrpcServer;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setWorkflowEngineFactory(WorkflowEngineFactory workflowEngineFactory) {
		this.workflowEngineFactory = workflowEngineFactory;
	}

}
