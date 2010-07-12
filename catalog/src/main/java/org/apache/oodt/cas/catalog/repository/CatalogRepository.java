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
 * Repository where the CatalogService stores its TransactionId Mapping and
 * serializes its Catalogs
 * 
 */
public interface CatalogRepository {

  /**
   * Serializes a given Catalog to the Repository
   * 
   * @param catalog
   *          The Catalog to be serialized
   * @throws CatalogRepositoryException
   *           Any Error
   */
  public void serializeCatalog(Catalog catalog)
      throws CatalogRepositoryException;

  /**
   * Removes a Catalog from the Repository
   * 
   * @param catalogUrn
   *          The URN that unique represents the Catalog to be removed
   * @param preserveMapping
   *          If true, don't erase TransactionId mapping for this catalog
   * @throws CatalogRepositoryException
   *           Any Error
   */
  public void deleteSerializedCatalog(String catalogUrn)
      throws CatalogRepositoryException;

  /**
   * Loads all Catalogs serialized in this Repository
   * 
   * @return All the Catalogs serialized in this Repository
   * @throws CatalogRepositoryException
   *           Any Error
   */
  public Set<Catalog> deserializeAllCatalogs()
      throws CatalogRepositoryException;

  public void serializePluginURLs(List<PluginURL> urls)
      throws CatalogRepositoryException;

  public List<PluginURL> deserializePluginURLs()
      throws CatalogRepositoryException;

  public boolean isModifiable() throws CatalogRepositoryException;

}
