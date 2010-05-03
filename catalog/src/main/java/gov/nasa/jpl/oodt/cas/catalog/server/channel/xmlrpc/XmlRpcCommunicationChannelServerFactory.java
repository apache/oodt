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
import java.util.logging.Level;
import java.util.logging.Logger;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

//OODT imports
import gov.nasa.jpl.oodt.cas.catalog.server.channel.CommunicationChannelServer;
import gov.nasa.jpl.oodt.cas.catalog.server.channel.CommunicationChannelServerFactory;
import gov.nasa.jpl.oodt.cas.catalog.system.CatalogServiceFactory;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Factory for creating XmlRpcCommunicationChannelServer
 * <p>
 */
public class XmlRpcCommunicationChannelServerFactory implements
		CommunicationChannelServerFactory {
	
	private static Logger LOG = Logger.getLogger(XmlRpcCommunicationChannelServerFactory.class.getName());
	
	protected int port;
	protected CatalogServiceFactory catalogServiceFactory;
	
	public XmlRpcCommunicationChannelServerFactory() {}
	
	public CommunicationChannelServer createCommunicationChannelServer() {
		try {
			XmlRpcCommunicationChannelServer server = new XmlRpcCommunicationChannelServer();
			server.setCatalogService(this.catalogServiceFactory.createCatalogService());
			server.setPort(this.port);
			return server;
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to create XML-RPC server : " + e.getMessage(), e);
			return null;
		}
	}
	
	@Required
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getPort() {
		return this.port;
	}
	
	@Required
	public void setCatalogServiceFactory(CatalogServiceFactory catalogServiceFactory) {
		this.catalogServiceFactory = catalogServiceFactory;
	}

}
