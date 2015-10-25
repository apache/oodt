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

//JDK imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.catalog.exception.CatalogRepositoryException;
import org.apache.oodt.cas.catalog.system.Catalog;
import org.apache.oodt.cas.catalog.util.PluginURL;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 *          <p>
 *          Memory based Repository where the CatalogService stores its
 *          TransactionId Mapping and serializes its Catalogs
 *          <p>
 */
public class MemoryBasedCatalogRepository implements CatalogRepository {
	
	protected HashMap<String, Catalog> catalogMap;
	protected List<PluginURL> classLoaderUrls;

	public MemoryBasedCatalogRepository() {
		this.catalogMap = new HashMap<String, Catalog>();
		this.classLoaderUrls = new Vector<PluginURL>();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.apache.oodt.cas.catalog.repository.CatalogRepository#
	 * deleteSerializedCatalog(java.lang.String)
	 */
	public void deleteSerializedCatalog(String catalogUrn)
			throws CatalogRepositoryException {
		this.catalogMap.remove(catalogUrn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.apache.oodt.cas.catalog.repository.CatalogRepository#
	 * deserializeAllCatalogs()
	 */
	public Set<Catalog> deserializeAllCatalogs()
			throws CatalogRepositoryException {
		return new HashSet<Catalog>(this.catalogMap.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.oodt.cas.catalog.repository.CatalogRepository#deserializeCatalog
	 * (java.lang.String)
	 */
	public Catalog deserializeCatalog(String catalogUrn) {
		return this.catalogMap.get(catalogUrn);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.apache.oodt.cas.catalog.repository.CatalogRepository#
	 * isCatalogSerialized(java.lang.String)
	 */
	public boolean isCatalogSerialized(String catalogUrn) {
		return this.catalogMap.containsKey(catalogUrn);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.oodt.cas.catalog.repository.CatalogRepository#serializeCatalog
	 * (org.apache.oodt.cas.catalog.Catalog)
	 */
	public void serializeCatalog(Catalog catalog)
			throws CatalogRepositoryException {
		this.catalogMap.put(catalog.getId(), catalog);
	}
	
	public void serializePluginURLs(List<PluginURL> urls) 
			throws CatalogRepositoryException {
		this.classLoaderUrls.addAll(urls);
	}

	public List<PluginURL> deserializePluginURLs() 
			throws CatalogRepositoryException {
		return new Vector<PluginURL>(this.classLoaderUrls);
	}
	
	public boolean isModifiable() {
		return true;
	}

}
