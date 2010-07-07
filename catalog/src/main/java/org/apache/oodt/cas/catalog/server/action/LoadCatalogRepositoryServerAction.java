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

package org.apache.oodt.cas.catalog.server.action;

import java.util.Set;

import org.springframework.context.support.FileSystemXmlApplicationContext;

import org.apache.oodt.cas.catalog.repository.CatalogRepository;
import org.apache.oodt.cas.catalog.repository.CatalogRepositoryFactory;
import org.apache.oodt.cas.catalog.system.Catalog;
import org.apache.oodt.cas.catalog.system.impl.CatalogServiceClient;
import org.apache.oodt.cas.catalog.util.Serializer;

public class LoadCatalogRepositoryServerAction extends CatalogServiceServerAction {

	protected String beanId;
	protected String beanRepo;
	
	@Override
	public void performAction(CatalogServiceClient csClient) throws Exception {
		FileSystemXmlApplicationContext appContext = new FileSystemXmlApplicationContext(new String[] { this.beanRepo }, false);
		appContext.setClassLoader(new Serializer().getClassLoader());
		appContext.refresh();
		CatalogRepositoryFactory factory = (CatalogRepositoryFactory) appContext.getBean(this.beanId, CatalogRepositoryFactory.class);
		CatalogRepository catalogRepository = factory.createRepository();
		Set<Catalog> catalogs = catalogRepository.deserializeAllCatalogs();
		System.out.println("Deserialized Catalogs: " + catalogs.toString());
		for (Catalog catalog : catalogs) {
			try {
				System.out.println("Adding Catalog: " + catalog);
				csClient.addCatalog(catalog);
			}catch (Exception e) {
				e.printStackTrace();
				System.err.println("Failed to add catalog '" + catalog + "' to server : " + e.getMessage());
			}
		}		
	}
	
	public void setBeanId(String beanId) {
		this.beanId = beanId;
	}
	
	public void setBeanRepo(String beanRepo) {
		this.beanRepo = beanRepo;
	}
}
