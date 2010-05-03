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

package gov.nasa.jpl.oodt.cas.catalog.system.impl;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

//OODT imports
import gov.nasa.jpl.oodt.cas.catalog.server.channel.CommunicationChannelClientFactory;
import gov.nasa.jpl.oodt.cas.catalog.system.CatalogServiceFactory;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Factory for CatalogServiceClient
 * <p>
 */
public class CatalogServiceClientFactory implements CatalogServiceFactory {
	
	protected CommunicationChannelClientFactory communicationChannelClientFactory;
	protected int autoPagerSize;
	
	public CatalogServiceClientFactory() {
		this.autoPagerSize = 500;
	}
	
	public CatalogServiceClient createCatalogService() {
		return new CatalogServiceClient(this.communicationChannelClientFactory.createCommunicationChannelClient(), this.autoPagerSize);
	}
	
	@Required
	public void setCommunicationChannelClientFactory(CommunicationChannelClientFactory communicationChannelClientFactory) {
		this.communicationChannelClientFactory = communicationChannelClientFactory;
	}
	
	@Required
	public void setAutoPagerSize(int autoPagerSize) {
		if (autoPagerSize > 0)
			this.autoPagerSize = autoPagerSize;
	}
	
	public String getServerUrl() {
		return this.communicationChannelClientFactory.getServerUrl();
	}

}
