/**
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

package org.apache.oodt.cas.catalog.server.action;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

//OODT imports
import org.apache.oodt.cas.catalog.system.impl.CatalogServiceClient;
import org.apache.oodt.cas.commons.spring.SpringSetIdInjectionType;

/**
 * 
 * Describe your class here.
 * 
 */
public abstract class CatalogServiceServerAction implements SpringSetIdInjectionType {
	
	protected String id;
	protected String description;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@Required
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public abstract void performAction(CatalogServiceClient csClient) throws Exception;
	
}
