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
import java.util.List;
import java.util.Set;

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
 *          Repository where the CatalogService stores its TransactionId Mapping
 *          and serializes its Catalogs
 *          <p>
 */
public interface CatalogRepository {
//
//	/**
//	 * Verifies if the given Catalog URN has been serialized via {@link
//	 * serializeCatalog(Catalog)}
//	 * 
//	 * @param catalogUrn
//	 *            The URN that unique represents the Catalog
//	 * @return True if the Catalog has been serialized, false otherwise
//	 * @throws CatalogRepositoryException
//	 *             Any Error
//	 */
//	public boolean isCatalogSerialized(String catalogUrn)
//			throws CatalogRepositoryException;
//
	/**
	 * Serializes a given Catalog to the Repository
	 * 
	 * @param catalog
	 *            The Catalog to be serialized
	 * @throws CatalogRepositoryException
	 *             Any Error
	 */
	void serializeCatalog(Catalog catalog)
			throws CatalogRepositoryException;
//
//	/**
//	 * Loads a Catalog from the Repository
//	 * 
//	 * @param catalogUrn
//	 *            The URN that unique represents a Catalog
//	 * @return The Catalog represented by the given URN
//	 * @throws CatalogRepositoryException
//	 *             Any Error
//	 */
//	public Catalog deserializeCatalog(String catalogUrn)
//			throws CatalogRepositoryException;
//
	/**
	 * Removes a Catalog from the Repository
	 * 
	 * @param catalogUrn
	 *            The URN that unique represents the Catalog to be removed
	 * @throws CatalogRepositoryException
	 *             Any Error
	 */
	void deleteSerializedCatalog(String catalogUrn)
			throws CatalogRepositoryException;

	/**
	 * Loads all Catalogs serialized in this Repository
	 * 
	 * @return All the Catalogs serialized in this Repository
	 * @throws CatalogRepositoryException
	 *             Any Error
	 */
	Set<Catalog> deserializeAllCatalogs()
			throws CatalogRepositoryException;

	void serializePluginURLs(List<PluginURL> urls)
			throws CatalogRepositoryException;
	
	List<PluginURL> deserializePluginURLs()
			throws CatalogRepositoryException;
	
	boolean isModifiable() throws CatalogRepositoryException;
	
}
