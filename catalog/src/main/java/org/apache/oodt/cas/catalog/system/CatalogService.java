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
package org.apache.oodt.cas.catalog.system;

//JDK imports
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.catalog.exception.CatalogServiceException;
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
import org.apache.oodt.cas.catalog.page.CatalogReceipt;
import org.apache.oodt.cas.catalog.page.Page;
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.catalog.page.QueryPager;
import org.apache.oodt.cas.catalog.page.TransactionReceipt;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.struct.Dictionary;
import org.apache.oodt.cas.catalog.struct.Index;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.util.PluginURL;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * An interface for a Metadata Catalog Manager Service
 * <p>
 */
public interface CatalogService {
		
	public static final String CATALOG_SERVICE_TRANSACTION_ID_MET_KEY = "urn:CatalogService:TransactionId";
	public static final String CATALOG_IDS_MET_KEY = "urn:CatalogService:CatalogIds";
	public static final String ENABLE_UPDATE_MET_KEY = "urn:CatalogService:EnableUpdate";
	public static final String CATALOG_TRANSACTION_ID_MET_KEY = "urn:Catalog:TransactionId";
	public static final String CATALOG_ID_MET_KEY = "urn:Catalog:Id";

	public void shutdown() throws CatalogServiceException;
	
	public boolean isRestrictQueryPermissions() throws CatalogServiceException;

	public boolean isRestrictIngestPermissions() throws CatalogServiceException;

	public void addCatalog(Catalog catalog) throws CatalogServiceException;
	
	public void replaceCatalog(Catalog catalog) throws CatalogServiceException;
	
	public void addCatalog(String catalogId, Index index) throws CatalogServiceException;
	
	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries) throws CatalogServiceException;

	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries, boolean restrictQueryPermission, boolean restrictIngestPermission) throws CatalogServiceException;

	public void addDictionary(String catalogId, Dictionary dictionary) throws CatalogServiceException;

	public void replaceDictionaries(String catalogId, List<Dictionary> dictionaries) throws CatalogServiceException;
	
	public void replaceIndex(String catalogId, Index index) throws CatalogServiceException;

	public void modifyIngestPermission(String catalogId, boolean restrictIngestPermission) throws CatalogServiceException;
	
	public void modifyQueryPermission(String catalogId, boolean restrictQueryPermission) throws CatalogServiceException;
		
	public void removeCatalog(String catalogId) throws CatalogServiceException;

	public URL getPluginStorageDir() throws CatalogServiceException;
	
	public List<PluginURL> getPluginUrls() throws CatalogServiceException;

	public void addPluginUrls(List<PluginURL> pluginURLs) throws CatalogServiceException;

	public Set<String> getCurrentCatalogIds() throws CatalogServiceException;
		
	public TransactionReceipt ingest(Metadata metadata) throws CatalogServiceException;
	
	public void delete(Metadata metadata) throws CatalogServiceException;
	
	public List<String> getProperty(String key) throws CatalogServiceException;

	public Properties getCalalogProperties() throws CatalogServiceException;
	
	public Properties getCalalogProperties(String catalogUrn) throws CatalogServiceException;
	
	public Page getNextPage(Page page) throws CatalogServiceException;
		
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression) throws CatalogServiceException;
	
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression, Set<String> catalogIds) throws CatalogServiceException;

	public List<TransactionalMetadata> getMetadata(Page page) throws CatalogServiceException;
	
	public QueryPager query(QueryExpression queryExpression) throws CatalogServiceException;

	public QueryPager query(QueryExpression queryExpression, Set<String> catalogIds) throws CatalogServiceException;
	 
	public List<TransactionalMetadata> getNextPage(QueryPager queryPager) throws CatalogServiceException;
	
	public List<TransactionalMetadata> getAllPages(QueryPager queryPager) throws CatalogServiceException;
	
	public List<TransactionalMetadata> getMetadataFromTransactionIdStrings(List<String> catalogServiceTransactionIdStrings) throws CatalogServiceException;
	
	public List<TransactionalMetadata> getMetadataFromTransactionIds(List<TransactionId<?>> catalogServiceTransactionIds) throws CatalogServiceException;
	
	public List<TransactionId<?>> getCatalogServiceTransactionIds(List<TransactionId<?>> catalogTransactionIds, String catalogUrn) throws CatalogServiceException;
	
	public TransactionId<?> getCatalogServiceTransactionId(TransactionId<?> catalogTransactionId, String catalogUrn) throws CatalogServiceException;
	
	public TransactionId<?> getCatalogServiceTransactionId(CatalogReceipt catalogReceipt, boolean generateNew) throws CatalogServiceException;
	
}
