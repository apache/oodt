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
import org.apache.oodt.cas.catalog.system.Catalog;
import org.apache.oodt.cas.catalog.system.CatalogService;
import org.apache.oodt.cas.catalog.util.PluginURL;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Communication Channel Server
 * <p>
 */
public interface CommunicationChannelServer {

	public void setCatalogService(CatalogService catalogService) throws Exception;
	
	public void setPort(int port) throws Exception;

	public int getPort();
	
	public void startup() throws Exception;

	public void shutdown() throws Exception;

	public boolean isRestrictQueryPermissions() throws Exception;
//
//	public void setRestrictQueryPermissions(boolean restrictQueryPermissions) throws Exception;
//
	public boolean isRestrictIngestPermissions() throws Exception;
//
//	public void setHasIngestPermissions(boolean restrictIngestPermissions) throws Exception;

//	public Class<? extends TransactionId<?>> getTransactionIdClass() throws Exception;
//
//	public void setTransactionIdClass(Class<? extends TransactionId<?>> transactionIdClass) throws Exception;
//	
	public void addCatalog(Catalog catalog) throws Exception;

	public void replaceCatalog(Catalog catalog) throws Exception;
	
	public void addCatalog(String catalogId, Index index) throws Exception;
	
	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries) throws Exception;

	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries, boolean restrictQueryPermission, boolean restrictIngestPermission) throws Exception;

	public void addDictionary(String catalogId, Dictionary dictionary) throws Exception;

	public void replaceDictionaries(String catalogId, List<Dictionary> dictionaries) throws Exception;
	
	public void replaceIndex(String catalogId, Index index) throws Exception;

	public void modifyIngestPermission(String catalogId, boolean restrictIngestPermission) throws Exception;
	
	public void modifyQueryPermission(String catalogId, boolean restrictQueryPermission) throws Exception;
		
	public void removeCatalog(String catalogUrn) throws Exception;
	
//	public void removeCatalog(String catalogUrn, boolean preserveMapping) throws Exception;

	public List<PluginURL> getPluginUrls() throws Exception;
	
	public void addPluginUrls(List<PluginURL> pluginURLs) throws Exception;
	
	public URL getPluginStorageDir() throws Exception;
	
//	public Set<Catalog> getCurrentCatalogList() throws Exception;
//	
//	public Catalog getCatalog(String catalogUrn) throws Exception;

	public Set<String> getCurrentCatalogIds() throws Exception;
		
	public TransactionReceipt ingest(Metadata metadata) throws Exception;
	
	public void delete(Metadata metadata) throws Exception;
	
	public List<String> getProperty(String key) throws Exception;

	public Properties getCalalogProperties() throws Exception;
	
	public Properties getCalalogProperties(String catalogUrn) throws Exception;
	
//	public Page getFirstPage(QueryExpression queryExpression) throws Exception;
//
//	public Page getFirstPage(QueryExpression queryExpression, Set<String> catalogIds) throws Exception;
	
	public Page getNextPage(Page page) throws Exception;
	
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression) throws Exception;
	
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression, Set<String> catalogIds) throws Exception;
	
//	public Page getLastPage(QueryExpression queryExpression) throws Exception;
//
//	public Page getLastPage(QueryExpression queryExpression, Set<String> catalogIds) throws Exception;
	
	public List<TransactionalMetadata> getMetadata(Page page) throws Exception;
	
	public QueryPager query(QueryExpression queryExpression) throws Exception;

	public QueryPager query(QueryExpression queryExpression, Set<String> catalogIds) throws Exception;

//	public QueryPager query(QueryExpression queryExpression, boolean sortResults) throws Exception;
 
	public List<TransactionalMetadata> getNextPage(QueryPager queryPager) throws Exception;

//	public List<TransactionId<?>> getTransactionIdsForAllPages(QueryPager queryPager) throws Exception;
	
	public List<TransactionalMetadata> getAllPages(QueryPager queryPager) throws Exception;
	
	public List<TransactionalMetadata> getMetadataFromTransactionIdStrings(List<String> catalogServiceTransactionIdStrings) throws Exception;
	
	public List<TransactionalMetadata> getMetadataFromTransactionIds(List<TransactionId<?>> catalogServiceTransactionIds) throws Exception;
	
	public List<TransactionId<?>> getCatalogServiceTransactionIds(List<TransactionId<?>> catalogTransactionIds, String catalogUrn) throws Exception;
	
	public TransactionId<?> getCatalogServiceTransactionId(TransactionId<?> catalogTransactionId, String catalogUrn) throws Exception;
	
	public TransactionId<?> getCatalogServiceTransactionId(CatalogReceipt catalogReceipt, boolean generateNew) throws Exception;
	
}
