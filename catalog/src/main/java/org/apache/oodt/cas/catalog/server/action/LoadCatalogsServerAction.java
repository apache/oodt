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

import java.util.Map;

import org.apache.oodt.cas.catalog.system.Catalog;
import org.apache.oodt.cas.catalog.system.impl.CatalogServiceClient;
import org.apache.oodt.cas.catalog.util.Serializer;

import org.springframework.context.support.FileSystemXmlApplicationContext;

public class LoadCatalogsServerAction extends CatalogServiceServerAction {

	protected String beanRepo;
	
	@Override
	public void performAction(CatalogServiceClient csClient) throws Exception {
		FileSystemXmlApplicationContext repoAppContext = new FileSystemXmlApplicationContext(new String[] { this.beanRepo }, false);
		repoAppContext.setClassLoader(new Serializer().getClassLoader());
		repoAppContext.refresh();
		Map<String, Catalog> catalogs = repoAppContext.getBeansOfType(Catalog.class);
		for (Catalog catalog : catalogs.values())
			csClient.addCatalog(catalog);
	}
	
	public void setBeanRepo(String beanRepo) {
		this.beanRepo = beanRepo;
	}

}
