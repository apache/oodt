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

package org.apache.oodt.cas.catalog.repository;

//OODT imports
import org.apache.oodt.cas.catalog.exception.CatalogRepositoryException;
import org.apache.oodt.cas.catalog.system.Catalog;
import org.apache.oodt.cas.catalog.util.PluginURL;
import org.apache.oodt.cas.catalog.util.Serializer;

//JDK imports
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Spring imports
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Spring Framework based CatalogRepository
 * <p>
 */
public class SpringCatalogRepository implements CatalogRepository {

	protected String beanRepo;
	
	public SpringCatalogRepository(String beanRepo) {
		this.beanRepo = beanRepo;
	}
	
	public Set<Catalog> deserializeAllCatalogs()
			throws CatalogRepositoryException {
		try {
	        FileSystemXmlApplicationContext appContext = new FileSystemXmlApplicationContext(new String[] { this.beanRepo }, false);
	        appContext.setClassLoader(new Serializer().getClassLoader());
	        appContext.refresh();
	        return new HashSet<Catalog>(appContext.getBeansOfType(Catalog.class).values());
		} catch (Exception e) {
			throw new CatalogRepositoryException("", e);
		}
	}

	public void deleteSerializedCatalog(String catalogUrn)
			throws CatalogRepositoryException {
		throw new CatalogRepositoryException("Modification not allowed during runtime");
	}

	public boolean isModifiable() throws CatalogRepositoryException {
		return false;
	}

	public void serializeCatalog(Catalog catalog)
			throws CatalogRepositoryException {
		throw new CatalogRepositoryException("Modification not allowed during runtime");		
	}

	public List<PluginURL> deserializePluginURLs()
			throws CatalogRepositoryException {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	public void serializePluginURLs(List<PluginURL> urls)
			throws CatalogRepositoryException {
		throw new CatalogRepositoryException("Modification not allowed during runtime");		
	}
	
}
