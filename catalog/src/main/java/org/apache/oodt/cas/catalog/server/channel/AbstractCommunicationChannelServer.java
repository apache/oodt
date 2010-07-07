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
import org.apache.oodt.cas.catalog.util.Serializer;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		Proxy.newProxyInstance(CommunicationChannelServer.class.getClassLoader(),
	            new Class[] { CommunicationChannelServer.class },
	            new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args)
							throws Throwable {
						try {
							return method.invoke(AbstractCommunicationChannelServer.this, args);
						}catch(Exception e) {
							LOG.log(Level.SEVERE, "Error invoking CatalogService on server : " + e.getMessage(), e);
							throw new CatalogServiceException("Error invoking CatalogService on server : " + e.getMessage(), e);
						}
					} 
		     	}
			);
	}
	
	public void setCatalogService(CatalogService catalogService) throws CatalogServiceException {
		this.catalogService = catalogService;
//		this.addCustomUrlsToClassLoader(this.catalogService.getCustomClassLoaderUrls());
	}
//	
//	protected synchronized void addCustomUrlsToClassLoader(List<URL> urls) {
//		this.serializer.addCustomUrls(urls);
//	}
	
	public void setPort(int port) { 
		this.port = port;
	}
	
	public int getPort() {
		return this.port;
	}

	public void shutdown() throws CatalogServiceException {
		this.catalogService.shutdown();
		this.catalogService = null;
		System.gc(); // used to speed up shutdown process (gives java a boost-start at cleaning up everything so server will die)
	}
	
	public boolean isRestrictQueryPermissions() throws CatalogServiceException {
		return this.catalogService.isRestrictQueryPermissions();
	}
//
//	public void setRestrictQueryPermissions(boolean restrictQueryPermissions) throws CatalogServiceException {
//		this.catalogService.setRestrictQueryPermissions(restrictQueryPermissions);
//	}
//
	public boolean isRestrictIngestPermissions() throws CatalogServiceException {
		return this.catalogService.isRestrictIngestPermissions();
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
	public void addCatalog(Catalog catalog) throws CatalogServiceException {
		this.catalogService.addCatalog(catalog);
	}
	
	public void replaceCatalog(Catalog catalog) throws CatalogServiceException {
		this.catalogService.replaceCatalog(catalog);
	}
	
	public void addCatalog(String catalogId, Index index) throws CatalogServiceException {
		this.catalogService.addCatalog(catalogId, index);
	}
	
	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries) throws CatalogServiceException {
		this.catalogService.addCatalog(catalogId, index, dictionaries);
	}

	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries, boolean restrictQueryPermission, boolean restrictIngestPermission) throws CatalogServiceException {
		this.catalogService.addCatalog(catalogId, index, dictionaries, restrictQueryPermission, restrictIngestPermission);
	}

	public void addDictionary(String catalogId, Dictionary dictionary) throws CatalogServiceException {
		this.catalogService.addDictionary(catalogId, dictionary);
	}
	
	public void replaceDictionaries(String catalogId, List<Dictionary> dictionaries) throws CatalogServiceException {
		this.catalogService.replaceDictionaries(catalogId, dictionaries);
	}

	public void replaceIndex(String catalogId, Index index) throws CatalogServiceException {
		this.catalogService.replaceIndex(catalogId, index);
	}

	public void modifyIngestPermission(String catalogId, boolean restrictIngestPermission) throws CatalogServiceException {
		this.catalogService.modifyIngestPermission(catalogId, restrictIngestPermission);
	}
	
	public void modifyQueryPermission(String catalogId, boolean restrictQueryPermission) throws CatalogServiceException {
		this.catalogService.modifyQueryPermission(catalogId, restrictQueryPermission);
	}
	
	public void removeCatalog(String catalogUrn) throws CatalogServiceException {
		this.catalogService.removeCatalog(catalogUrn);
	}

//	public void removeCatalog(String catalogUrn, boolean preserveMapping) throws CatalogServiceException {
//		this.catalogService.removeCatalog(catalogUrn, preserveMapping);
//	}

	public List<PluginURL> getPluginUrls() throws CatalogServiceException {
		return this.catalogService.getPluginUrls();
	}
	
	public void addPluginUrls(List<PluginURL> pluginURLs) throws CatalogServiceException {
		this.catalogService.addPluginUrls(pluginURLs);
		this.serializer.refreshClassLoader();
//		this.addCustomUrlsToClassLoader(urls);
	}
	
	public URL getPluginStorageDir() throws CatalogServiceException {
		return this.catalogService.getPluginStorageDir();
	}
	
//	public Set<Catalog> getCurrentCatalogList() throws CatalogServiceException {
//		return this.catalogService.getCurrentCatalogList();
//	}
//	
//	public Catalog getCatalog(String catalogUrn) throws CatalogServiceException {
//		return this.catalogService.getCatalog(catalogUrn);
//	}

	public Set<String> getCurrentCatalogIds() throws CatalogServiceException {
		return this.catalogService.getCurrentCatalogIds();
	}
		
	public TransactionReceipt ingest(Metadata metadata) throws CatalogServiceException {
		return this.catalogService.ingest(metadata);
	}
	
	public void delete(Metadata metadata) throws CatalogServiceException {
		this.catalogService.delete(metadata);
	}
	
	public List<String> getProperty(String key) throws CatalogServiceException {
		return this.catalogService.getProperty(key);
	}

	public Properties getCalalogProperties() throws CatalogServiceException {
		return this.catalogService.getCalalogProperties();
	}
	
	public Properties getCalalogProperties(String catalogUrn) throws CatalogServiceException {
		return this.catalogService.getCalalogProperties(catalogUrn);
	}

//	public Page getFirstPage(QueryExpression queryExpression) throws Exception {
//		return this.catalogService.getFirstPage(queryExpression);
//	}
//
//	public Page getFirstPage(QueryExpression queryExpression, Set<String> catalogIds) throws Exception {
//		return this.catalogService.getFirstPage(queryExpression, catalogIds);
//	}
	
	public Page getNextPage(Page page) throws Exception {
		return this.catalogService.getNextPage(page);
	}
	
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression) throws Exception {
		return this.catalogService.getPage(pageInfo, queryExpression);
	}
	
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression, Set<String> catalogIds) throws Exception {
		return this.catalogService.getPage(pageInfo, queryExpression, catalogIds);
	}
	
//	public Page getLastPage(QueryExpression queryExpression) throws Exception {
//		return this.catalogService.getLastPage(queryExpression);
//	}
//	
//	public Page getLastPage(QueryExpression queryExpression, Set<String> catalogIds) throws Exception {
//		return this.catalogService.getLastPage(queryExpression, catalogIds);
//	}
	
	public List<TransactionalMetadata> getMetadata(Page page) throws Exception {
		return this.catalogService.getMetadata(page);
	}
	
	public QueryPager query(QueryExpression queryExpression) throws CatalogServiceException {
		return this.catalogService.query(queryExpression);
	}
	
	public QueryPager query(QueryExpression queryExpression, Set<String> catalogIds) throws CatalogServiceException {
		return this.catalogService.query(queryExpression, catalogIds);
	}
	
//	public QueryPager query(QueryExpression queryExpression, boolean sortResults) throws CatalogServiceException {
//		return this.catalogService.query(queryExpression, sortResults);
//	}
 
	public List<TransactionalMetadata> getNextPage(QueryPager queryPager) throws CatalogServiceException {
		return this.catalogService.getNextPage(queryPager);
	}

//	public List<TransactionId<?>> getTransactionIdsForAllPages(QueryPager queryPager) throws CatalogServiceException {
//		return this.catalogService.getTransactionIdsForAllPages(queryPager);
//	}
	
	public List<TransactionalMetadata> getAllPages(QueryPager queryPager) throws CatalogServiceException {
		return this.catalogService.getAllPages(queryPager);
	}
	
	public List<TransactionalMetadata> getMetadataFromTransactionIdStrings(List<String> catalogServiceTransactionIdStrings) throws CatalogServiceException {
		return this.catalogService.getMetadataFromTransactionIdStrings(catalogServiceTransactionIdStrings);
	}
	
	public List<TransactionalMetadata> getMetadataFromTransactionIds(List<TransactionId<?>> catalogServiceTransactionIds) throws CatalogServiceException {
		return this.catalogService.getMetadataFromTransactionIds(catalogServiceTransactionIds);
	}
	
	public List<TransactionId<?>> getCatalogServiceTransactionIds(List<TransactionId<?>> catalogTransactionIds, String catalogUrn) throws CatalogServiceException {
		return this.catalogService.getCatalogServiceTransactionIds(catalogTransactionIds, catalogUrn);
	}
	
	public TransactionId<?> getCatalogServiceTransactionId(TransactionId<?> catalogTransactionId, String catalogUrn) throws CatalogServiceException {
		return this.catalogService.getCatalogServiceTransactionId(catalogTransactionId, catalogUrn);
	}
	
	public TransactionId<?> getCatalogServiceTransactionId(CatalogReceipt catalogReceipt, boolean generateNew) throws CatalogServiceException {
		return this.catalogService.getCatalogServiceTransactionId(catalogReceipt, generateNew);
	}
	
}
