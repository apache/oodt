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

//JDK imports
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
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
import org.apache.oodt.cas.catalog.util.Serializer;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * An Abstract Communication Channel Server Interface that automatically handles throw exceptions
 * <p>
 */
public abstract class AbstractCommunicationChannelServer implements CommunicationChannelServer {
	
	private static Logger LOG = Logger.getLogger(AbstractCommunicationChannelServer.class.getName());
	
	protected CatalogService catalogService;
	protected int port;
	protected Serializer serializer;
	
	public AbstractCommunicationChannelServer() {
		this.serializer = new Serializer();
	}
	
	public void setCatalogService(CatalogService catalogService) {
		this.catalogService = catalogService;
	}
	
	public void setPort(int port) { 
		this.port = port;
	}
	
	public int getPort() {
		return this.port;
	}

	public void shutdown() throws Exception {
		try {
			this.catalogService.shutdown();
			this.catalogService = null;
			System.gc(); // used to speed up shutdown process (gives java a boost-start at cleaning up everything so server will die)
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to shutdown server : " + e.getMessage(), e);
			throw new Exception("Failed to shutdown server : " + e.getMessage(), e);
		}
	}
	
	public boolean isRestrictQueryPermissions() throws Exception {
		try {
			return this.catalogService.isRestrictQueryPermissions();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while checking server query permissions : " + e.getMessage(), e);
			throw new Exception("Failed while checking server query permissions : " + e.getMessage(), e);
		}
	}
//
//	public void setRestrictQueryPermissions(boolean restrictQueryPermissions) throws CatalogServiceException {
//		this.catalogService.setRestrictQueryPermissions(restrictQueryPermissions);
//	}
//
	public boolean isRestrictIngestPermissions() throws Exception {
		try {
			return this.catalogService.isRestrictIngestPermissions();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while checking server ingest permissions : " + e.getMessage(), e);
			throw new Exception("Failed while checking server ingest permissions : " + e.getMessage(), e);
		}
	}
//
//	public void setHasIngestPermissions(boolean restrictIngestPermissions) throws CatalogServiceException {
//		this.catalogService.setHasIngestPermissions(restrictIngestPermissions);
//	}

//	public Class<? extends TransactionId<?>> getTransactionIdClass() throws CatalogServiceException {
//		return this.catalogService.getTransactionIdClass();
//	}
//
//	public void setTransactionIdClass(Class<? extends TransactionId<?>> transactionIdClass) throws CatalogServiceException {
//		this.catalogService.setTransactionIdClass(transactionIdClass);
//	}
//
	public void addCatalog(Catalog catalog) throws Exception {
		try {
			this.catalogService.addCatalog(catalog);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while adding catalog '" + catalog + "' to server : " + e.getMessage(), e);
			throw new Exception("Failed while adding catalog '" + catalog + "' to server : " + e.getMessage(), e);
		}
	}
	
	public void replaceCatalog(Catalog catalog) throws Exception {
		try {
			this.catalogService.replaceCatalog(catalog);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while replacing catalog '" + catalog + "' to server : " + e.getMessage(), e);
			throw new Exception("Failed while replacing catalog '" + catalog + "' to server : " + e.getMessage(), e);
		}
	}
	
	public void addCatalog(String catalogId, Index index) throws Exception {
		try {
			this.catalogService.addCatalog(catalogId, index);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while adding catalog '" + catalogId + "' with index '" + index + "' to server : " + e.getMessage(), e);
			throw new Exception("Failed while adding catalog '" + catalogId + "' with index '" + index + "' to server : " + e.getMessage(), e);
		}
	}
	
	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries) throws Exception {
		try {
			this.catalogService.addCatalog(catalogId, index, dictionaries);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while adding catalog '" + catalogId + "' with index '" + index + "' and dictionaries '" + dictionaries + "' to server : " + e.getMessage(), e);
			throw new Exception("Failed while adding catalog '" + catalogId + "' with index '" + index + "' and dictionaries '" + dictionaries + "' to server : " + e.getMessage(), e);
		}
	}

	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries, boolean restrictQueryPermission, boolean restrictIngestPermission) throws Exception {
		try {
			this.catalogService.addCatalog(catalogId, index, dictionaries, restrictQueryPermission, restrictIngestPermission);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while adding catalog '" + catalogId + "' with index '" + index + "' and dictionaries '" + dictionaries + "' and restrictQueryPermission '" + restrictQueryPermission + "' and restrictIngestPermission '" + restrictIngestPermission + "' to server : " + e.getMessage(), e);
			throw new Exception("Failed while adding catalog '" + catalogId + "' with index '" + index + "' and dictionaries '" + dictionaries + "' and restrictQueryPermission '" + restrictQueryPermission + "' and restrictIngestPermission '" + restrictIngestPermission + "' to server : " + e.getMessage(), e);
		}
	}

	public void addDictionary(String catalogId, Dictionary dictionary) throws Exception {
		try {
			this.catalogService.addDictionary(catalogId, dictionary);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while adding dictionary '" + dictionary + "' to catalog '" + catalogId + "' : " + e.getMessage(), e);
			throw new Exception("Failed while adding dictionary '" + dictionary + "' to catalog '" + catalogId + "' : " + e.getMessage(), e);
		}
	}
	
	public void replaceDictionaries(String catalogId, List<Dictionary> dictionaries) throws Exception {
		try {
			this.catalogService.replaceDictionaries(catalogId, dictionaries);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while replacing dictionaries '" + dictionaries + "' in catalog '" + catalogId + "' : " + e.getMessage(), e);
			throw new Exception("Failed while replacing dictionaries '" + dictionaries + "' in catalog '" + catalogId + "' : " + e.getMessage(), e);
		}
	}

	public void replaceIndex(String catalogId, Index index) throws Exception {
		try {
			this.catalogService.replaceIndex(catalogId, index);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while replacing index '" + index + "' in catalog '" + catalogId + "' : " + e.getMessage(), e);
			throw new Exception("Failed while replacing index '" + index + "' in catalog '" + catalogId + "' : " + e.getMessage(), e);
		}
	}

	public void modifyIngestPermission(String catalogId, boolean restrictIngestPermission) throws Exception {
		try {
			this.catalogService.modifyIngestPermission(catalogId, restrictIngestPermission);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while changing ingest permissions for catalog '" + catalogId + "' to '" + restrictIngestPermission + "' : " + e.getMessage(), e);
			throw new Exception("Failed while changing ingest permissions for catalog '" + catalogId + "' to '" + restrictIngestPermission + "' : " + e.getMessage(), e);
		}
	}
	
	public void modifyQueryPermission(String catalogId, boolean restrictQueryPermission) throws Exception {
		try {
			this.catalogService.modifyQueryPermission(catalogId, restrictQueryPermission);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while changing query permissions for catalog '" + catalogId + "' to '" + restrictQueryPermission + "' : " + e.getMessage(), e);
			throw new Exception("Failed while changing query permissions for catalog '" + catalogId + "' to '" + restrictQueryPermission + "' : " + e.getMessage(), e);
		}
	}
	
	public void removeCatalog(String catalogId) throws Exception {
		try {
			this.catalogService.removeCatalog(catalogId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while removing catalog '" + catalogId + "' : " + e.getMessage(), e);
			throw new Exception("Failed while removing catalog '" + catalogId + "' : " + e.getMessage(), e);
		}
	}

//	public void removeCatalog(String catalogUrn, boolean preserveMapping) throws CatalogServiceException {
//		this.catalogService.removeCatalog(catalogUrn, preserveMapping);
//	}

	public List<PluginURL> getPluginUrls() throws Exception {
		try {
			return this.catalogService.getPluginUrls();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while getting plugin URLs : " + e.getMessage(), e);
			throw new Exception("Failed while getting plugin URLs : " + e.getMessage(), e);
		}
	}
	
	public void addPluginUrls(List<PluginURL> pluginURLs) throws Exception {
		try {
			this.catalogService.addPluginUrls(pluginURLs);
			this.serializer.refreshClassLoader();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while adding plugin URLs '" + pluginURLs + "' : " + e.getMessage(), e);
			throw new Exception("Failed while adding plugin URLs '" + pluginURLs + "' : " + e.getMessage(), e);
		}
	}
	
	public URL getPluginStorageDir() throws Exception {
		try {
			return this.catalogService.getPluginStorageDir();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while getting plugin storage directory : " + e.getMessage(), e);
			throw new Exception("Failed while getting plugin storage directory : " + e.getMessage(), e);
		}
	}
	
//	public Set<Catalog> getCurrentCatalogList() throws CatalogServiceException {
//		return this.catalogService.getCurrentCatalogList();
//	}
//	
//	public Catalog getCatalog(String catalogUrn) throws CatalogServiceException {
//		return this.catalogService.getCatalog(catalogUrn);
//	}

	public Set<String> getCurrentCatalogIds() throws Exception {
		try {
			return this.catalogService.getCurrentCatalogIds();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while getting current catalog ids : " + e.getMessage(), e);
			throw new Exception("Failed while getting current catalog ids : " + e.getMessage(), e);
		}	
	}
		
	public TransactionReceipt ingest(Metadata metadata) throws Exception {
		try {
			return this.catalogService.ingest(metadata);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while performing ingest : " + e.getMessage(), e);
			throw new Exception("Failed while performing ingest : " + e.getMessage(), e);
		}
	}
	
	public void delete(Metadata metadata) throws Exception {
		try {
			this.catalogService.delete(metadata);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while performing deletion : " + e.getMessage(), e);
			throw new Exception("Failed while performing deletion : " + e.getMessage(), e);
		}	
	}
	
	public List<String> getProperty(String key) throws Exception {
		try {
			return this.catalogService.getProperty(key);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while getting property '" + key + "' : " + e.getMessage(), e);
			throw new Exception("Failed while getting property '" + key + "' : " + e.getMessage(), e);
		}	
	}

	public Properties getCalalogProperties() throws Exception {
		try {
			return this.catalogService.getCalalogProperties();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while getting catalog properties : " + e.getMessage(), e);
			throw new Exception("Failed while getting catalog properties : " + e.getMessage(), e);
		}		
	}
	
	public Properties getCalalogProperties(String catalogId) throws Exception {
		try {
			return this.catalogService.getCalalogProperties(catalogId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while getting catalog properties for catalog '" + catalogId + "' : " + e.getMessage(), e);
			throw new Exception("Failed while getting catalog properties for catalog '" + catalogId + "' : " + e.getMessage(), e);
		}	
	}

//	public Page getFirstPage(QueryExpression queryExpression) throws Exception {
//		return this.catalogService.getFirstPage(queryExpression);
//	}
//
//	public Page getFirstPage(QueryExpression queryExpression, Set<String> catalogIds) throws Exception {
//		return this.catalogService.getFirstPage(queryExpression, catalogIds);
//	}
	
	public Page getNextPage(Page page) throws Exception {
		try {
			return this.catalogService.getNextPage(page);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while getting next page : " + e.getMessage(), e);
			throw new Exception("Failed while getting next page : " + e.getMessage(), e);
		}	
	}
	
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression) throws Exception {
		try {
			return this.catalogService.getPage(pageInfo, queryExpression);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while getting next page [pageInfo='" + pageInfo + "',query='" + queryExpression + "'] : " + e.getMessage(), e);
			throw new Exception("Failed while getting next page [pageInfo='" + pageInfo + "',query='" + queryExpression + "'] : " + e.getMessage(), e);
		}
	}
	
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression, Set<String> catalogIds) throws Exception {
		try {
			return this.catalogService.getPage(pageInfo, queryExpression, catalogIds);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while getting next page [pageInfo='" + pageInfo + "',query='" + queryExpression + "',catalogIds='" + catalogIds + "'] : " + e.getMessage(), e);
			throw new Exception("Failed while getting next page [pageInfo='" + pageInfo + "',query='" + queryExpression + "',catalogIds='" + catalogIds + "'] : " + e.getMessage(), e);
		}
	}
	
//	public Page getLastPage(QueryExpression queryExpression) throws Exception {
//		return this.catalogService.getLastPage(queryExpression);
//	}
//	
//	public Page getLastPage(QueryExpression queryExpression, Set<String> catalogIds) throws Exception {
//		return this.catalogService.getLastPage(queryExpression, catalogIds);
//	}
	
	public List<TransactionalMetadata> getMetadata(Page page) throws Exception {
		try {
			return this.catalogService.getMetadata(page);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while getting metadata for page : " + e.getMessage(), e);
			throw new Exception("Failed while getting metadata for page : " + e.getMessage(), e);
		}
	}
	
	public QueryPager query(QueryExpression queryExpression) throws Exception {
		try {
			return this.catalogService.query(queryExpression);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while performing query '" + queryExpression + "' : " + e.getMessage(), e);
			throw new Exception("Failed while performing query '" + queryExpression + "' : " + e.getMessage(), e);
		}
	}
	
	public QueryPager query(QueryExpression queryExpression, Set<String> catalogIds) throws Exception {
		try {
			return this.catalogService.query(queryExpression, catalogIds);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while performing query '" + queryExpression + "' to catalogs '" + catalogIds + "' : " + e.getMessage(), e);
			throw new Exception("Failed while performing query '" + queryExpression + "' to catalogs '" + catalogIds + "' : " + e.getMessage(), e);
		}
	}
	
//	public QueryPager query(QueryExpression queryExpression, boolean sortResults) throws CatalogServiceException {
//		return this.catalogService.query(queryExpression, sortResults);
//	}
 
	public List<TransactionalMetadata> getNextPage(QueryPager queryPager) throws Exception {
		try {
			return this.catalogService.getNextPage(queryPager);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while get next page from query pager : " + e.getMessage(), e);
			throw new Exception("Failed while get next page from query pager : " + e.getMessage(), e);
		}	
	}

//	public List<TransactionId<?>> getTransactionIdsForAllPages(QueryPager queryPager) throws CatalogServiceException {
//		return this.catalogService.getTransactionIdsForAllPages(queryPager);
//	}
	
	public List<TransactionalMetadata> getAllPages(QueryPager queryPager) throws Exception {
		try {
			return this.catalogService.getAllPages(queryPager);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while get all pages from query pager : " + e.getMessage(), e);
			throw new Exception("Failed while get all pages from query pager : " + e.getMessage(), e);
		}	
	}
	
	public List<TransactionalMetadata> getMetadataFromTransactionIdStrings(List<String> catalogServiceTransactionIdStrings) throws Exception {
		try {
			return this.catalogService.getMetadataFromTransactionIdStrings(catalogServiceTransactionIdStrings);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while getting metadata for catalog service transaction ids '" + catalogServiceTransactionIdStrings + "' : " + e.getMessage(), e);
			throw new Exception("Failed while getting metadata for catalog service transaction ids '" + catalogServiceTransactionIdStrings + "' : " + e.getMessage(), e);
		}	
	}
	
	public List<TransactionalMetadata> getMetadataFromTransactionIds(List<TransactionId<?>> catalogServiceTransactionIds) throws Exception {
		try {
			return this.catalogService.getMetadataFromTransactionIds(catalogServiceTransactionIds);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while getting metadata for catalog service transaction ids '" + catalogServiceTransactionIds + "' : " + e.getMessage(), e);
			throw new Exception("Failed while getting metadata for catalog service transaction ids '" + catalogServiceTransactionIds + "' : " + e.getMessage(), e);
		}	
	}
	
	public List<TransactionId<?>> getCatalogServiceTransactionIds(List<TransactionId<?>> catalogTransactionIds, String catalogId) throws Exception {
		try {
			return this.catalogService.getCatalogServiceTransactionIds(catalogTransactionIds, catalogId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while getting catalog service transaction ids for catalog transaction ids '" + catalogTransactionIds + "' from catalog '" + catalogId + "' : " + e.getMessage(), e);
			throw new Exception("Failed while getting catalog service transaction ids for catalog transaction ids '" + catalogTransactionIds + "' from catalog '" + catalogId + "' : " + e.getMessage(), e);
		}	
	}
	
	public TransactionId<?> getCatalogServiceTransactionId(TransactionId<?> catalogTransactionId, String catalogId) throws Exception {
		try {
			return this.catalogService.getCatalogServiceTransactionId(catalogTransactionId, catalogId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while getting catalog service transaction id for catalog transaction id '" + catalogTransactionId + "' from catalog '" + catalogId + "' : " + e.getMessage(), e);
			throw new Exception("Failed while getting catalog service transaction id for catalog transaction id '" + catalogTransactionId + "' from catalog '" + catalogId + "' : " + e.getMessage(), e);
		}		
	}
	
	public TransactionId<?> getCatalogServiceTransactionId(CatalogReceipt catalogReceipt, boolean generateNew) throws Exception {
		try {
			return this.catalogService.getCatalogServiceTransactionId(catalogReceipt, generateNew);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed while getting metadata for catalog service transaction id for catalog receipt '" + catalogReceipt + "' with generate new equal '" + generateNew + "' : " + e.getMessage(), e);
			throw new Exception("Failed while getting metadata for catalog service transaction id for catalog receipt '" + catalogReceipt + "' with generate new equal '" + generateNew + "' : " + e.getMessage(), e);
		}		
	}
	
}
