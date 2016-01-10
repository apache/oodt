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
package org.apache.oodt.cas.catalog.server.channel;

import org.apache.oodt.cas.catalog.exception.CatalogException;
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
import org.apache.oodt.cas.catalog.system.Catalog;
import org.apache.oodt.cas.catalog.util.PluginURL;
import org.apache.oodt.cas.metadata.Metadata;

import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Communication Channel Interface
 * <p>
 */
public interface CommunicationChannelClient {

	void shutdown() throws CatalogException;
	
	boolean isRestrictQueryPermissions() throws CatalogException;

	boolean isRestrictIngestPermissions() throws CatalogException;

	void addCatalog(Catalog catalog) throws CatalogException;

	void replaceCatalog(Catalog catalog) throws CatalogException;

	void addCatalog(String catalogId, Index index) throws CatalogException;
	
	void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries) throws CatalogException;

	void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries, boolean restrictQueryPermission,
					boolean restrictIngestPermission) throws CatalogException;

	void addDictionary(String catalogId, Dictionary dictionary) throws CatalogException;

	void replaceDictionaries(String catalogId, List<Dictionary> dictionaries) throws CatalogException;

	void replaceIndex(String catalogId, Index index) throws CatalogException;

	void modifyIngestPermission(String catalogId, boolean restrictIngestPermission) throws CatalogException;
	
	void modifyQueryPermission(String catalogId, boolean restrictQueryPermission) throws CatalogException;
	
	void removeCatalog(String catalogUrn) throws CatalogException;

	List<PluginURL> getPluginUrls() throws CatalogException;

	void addPluginUrls(List<PluginURL> pluginUrls) throws CatalogException;

	URL getPluginStorageDir() throws CatalogException;
	
	void transferUrl(URL fromUrl, URL toUrl) throws CatalogException;

	Set<String> getCurrentCatalogIds() throws CatalogException;
		
	TransactionReceipt ingest(Metadata metadata) throws CatalogException;
	
	void delete(Metadata metadata) throws CatalogException;
	
	List<String> getProperty(String key) throws CatalogException;

	Properties getCalalogProperties() throws CatalogException;
	
	Properties getCalalogProperties(String catalogUrn) throws CatalogException;
	
	Page getNextPage(Page page) throws CatalogException;
	
	Page getPage(PageInfo pageInfo, QueryExpression queryExpression) throws CatalogException;
	
	Page getPage(PageInfo pageInfo, QueryExpression queryExpression, Set<String> catalogIds) throws CatalogException;
	
	List<TransactionalMetadata> getMetadata(Page page) throws CatalogException;

	QueryPager query(QueryExpression queryExpression) throws CatalogException;

	QueryPager query(QueryExpression queryExpression, Set<String> catalogIds) throws CatalogException;

	List<TransactionalMetadata> getNextPage(QueryPager queryPager) throws CatalogException;

	List<TransactionId<?>> getTransactionIdsForAllPages(QueryPager queryPager) throws CatalogException;
	
	List<TransactionalMetadata> getAllPages(QueryPager queryPager) throws CatalogException;
	
	List<TransactionalMetadata> getMetadataFromTransactionIdStrings(List<String> catalogServiceTransactionIdStrings) throws CatalogException;
	
	List<TransactionalMetadata> getMetadataFromTransactionIds(List<TransactionId<?>> catalogServiceTransactionIds) throws CatalogException;
	
	List<TransactionId<?>> getCatalogServiceTransactionIds(List<TransactionId<?>> catalogTransactionIds,
														   String catalogUrn) throws CatalogException;
	
	TransactionId<?> getCatalogServiceTransactionId(TransactionId<?> catalogTransactionId, String catalogUrn) throws CatalogException;
	
	TransactionId<?> getCatalogServiceTransactionId(CatalogReceipt catalogReceipt, boolean generateNew) throws CatalogException;
	
}