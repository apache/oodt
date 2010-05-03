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

package gov.nasa.jpl.oodt.cas.catalog.system.impl;

//OODT imports
import gov.nasa.jpl.oodt.cas.catalog.exception.CatalogServiceException;
import gov.nasa.jpl.oodt.cas.catalog.metadata.TransactionalMetadata;
import gov.nasa.jpl.oodt.cas.catalog.page.CatalogReceipt;
import gov.nasa.jpl.oodt.cas.catalog.page.Page;
import gov.nasa.jpl.oodt.cas.catalog.page.PageInfo;
import gov.nasa.jpl.oodt.cas.catalog.page.QueryPager;
import gov.nasa.jpl.oodt.cas.catalog.page.TransactionReceipt;
import gov.nasa.jpl.oodt.cas.catalog.query.QueryExpression;
import gov.nasa.jpl.oodt.cas.catalog.server.channel.CommunicationChannelClient;
import gov.nasa.jpl.oodt.cas.catalog.struct.Dictionary;
import gov.nasa.jpl.oodt.cas.catalog.struct.Index;
import gov.nasa.jpl.oodt.cas.catalog.struct.TransactionId;
import gov.nasa.jpl.oodt.cas.catalog.system.Catalog;
import gov.nasa.jpl.oodt.cas.catalog.system.CatalogService;
import gov.nasa.jpl.oodt.cas.catalog.util.PluginURL;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

//JDK imports
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Client that appears to be a CatalogService, but communicates with a CatalogService Server
 * <p>
 */
public class CatalogServiceClient implements CatalogService {

	protected CommunicationChannelClient communicationChannelClient;
	protected int autoPagerSize;
	
	public CatalogServiceClient(CommunicationChannelClient communicationChannelClient, int autoPagerSize) {
		this.communicationChannelClient = communicationChannelClient;
		this.autoPagerSize = autoPagerSize;
	}
	
	public void shutdown() throws CatalogServiceException  {
		try {
			this.communicationChannelClient.shutdown();
		}catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}
	
	protected List<PluginURL> transferToServerSide(List<PluginURL> clientSideUrls) throws CatalogServiceException {
		try {
			URL customUrlStorageDir = this.communicationChannelClient.getPluginStorageDir();
			System.out.println("Got Tmp dir : " + customUrlStorageDir);
			Vector<PluginURL> serverSideUrls = new Vector<PluginURL>();
			for (PluginURL pluginURL : clientSideUrls) {
				PluginURL serverSideURL = new PluginURL(pluginURL.getCatalogId(), new URL(customUrlStorageDir, new File(pluginURL.getURL().getFile()).getName()));
				System.out.println("generated server side url : " + customUrlStorageDir);
				this.communicationChannelClient.transferUrl(pluginURL.getURL(), serverSideURL.getURL());
				serverSideUrls.add(serverSideURL);
			}
			return serverSideUrls;
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}
		
	public void addCatalog(Catalog catalog) throws CatalogServiceException {
		try {
			this.communicationChannelClient.addCatalog(catalog);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public void replaceCatalog(Catalog catalog)
			throws CatalogServiceException {
		try {
			this.communicationChannelClient.replaceCatalog(catalog);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}
	
	public void addCatalog(String catalogId, Index index)
			throws CatalogServiceException {
		try {
			this.communicationChannelClient.addCatalog(catalogId, index);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries) throws CatalogServiceException {
		try {
			this.communicationChannelClient.addCatalog(catalogId, index, dictionaries);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries, boolean restrictQueryPermission,
			boolean restrictIngestPermission) throws CatalogServiceException {
		try {
			this.communicationChannelClient.addCatalog(catalogId, index, dictionaries, restrictQueryPermission, restrictIngestPermission);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public void addDictionary(String catalogId, Dictionary dictionary)
			throws CatalogServiceException {
		try {
			this.communicationChannelClient.addDictionary(catalogId, dictionary);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public void replaceDictionaries(String catalogId, List<Dictionary> dictionaries) throws CatalogServiceException {
		try {
			this.communicationChannelClient.replaceDictionaries(catalogId, dictionaries);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}
	
	public void modifyIngestPermission(String catalogId,
			boolean restrictIngestPermission) throws CatalogServiceException {
		try {
			this.communicationChannelClient.modifyIngestPermission(catalogId, restrictIngestPermission);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public void modifyQueryPermission(String catalogId,
			boolean restrictQueryPermission) throws CatalogServiceException {
		try {
			this.communicationChannelClient.modifyQueryPermission(catalogId, restrictQueryPermission);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public void replaceIndex(String catalogId, Index index)
			throws CatalogServiceException {
		try {
			this.communicationChannelClient.replaceIndex(catalogId, index);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}
	
	public void removeCatalog(String catalogId) throws CatalogServiceException {
		try {
			this.communicationChannelClient.removeCatalog(catalogId);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}
	
	public URL getPluginStorageDir() throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getPluginStorageDir();
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}
	
	public List<PluginURL> getPluginUrls() throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getPluginUrls();
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public void addPluginUrls(List<PluginURL> urls) throws CatalogServiceException {
		try {
			this.communicationChannelClient.addPluginUrls(this.transferToServerSide(urls));
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}
	
	public void delete(Metadata metadata) throws CatalogServiceException {
		try {
			this.communicationChannelClient.delete(metadata);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public List<TransactionalMetadata> getAllPages(QueryPager queryPager)
			throws CatalogServiceException {
		try {
			List<TransactionalMetadata> metadata = new Vector<TransactionalMetadata>();
			if (queryPager.getTotalPages() > 0) {
				queryPager.setPageInfo(new PageInfo(this.autoPagerSize, PageInfo.FIRST_PAGE));
				while (!queryPager.isLastPage()) {
					metadata.addAll(this.communicationChannelClient.getNextPage(queryPager));
					queryPager.incrementPageNumber();
				}
				metadata.addAll(this.communicationChannelClient.getNextPage(queryPager));
			}
			return metadata;
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public Properties getCalalogProperties() throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getCalalogProperties();
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public Properties getCalalogProperties(String catalogUrn)
			throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getCalalogProperties(catalogUrn);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public TransactionId<?> getCatalogServiceTransactionId(
			TransactionId<?> catalogTransactionId, String catalogUrn)
			throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getCatalogServiceTransactionId(catalogTransactionId, catalogUrn);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public TransactionId<?> getCatalogServiceTransactionId(
			CatalogReceipt catalogReceipt, boolean generateNew) throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getCatalogServiceTransactionId(catalogReceipt, generateNew);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public List<TransactionId<?>> getCatalogServiceTransactionIds(
			List<TransactionId<?>> catalogTransactionIds, String catalogUrn)
			throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getCatalogServiceTransactionIds(catalogTransactionIds, catalogUrn);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public Set<String> getCurrentCatalogIds() throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getCurrentCatalogIds();
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public List<TransactionalMetadata> getMetadataFromTransactionIdStrings(
			List<String> catalogServiceTransactionIdStrings)
			throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getMetadataFromTransactionIdStrings(catalogServiceTransactionIdStrings);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public List<TransactionalMetadata> getMetadataFromTransactionIds(
			List<TransactionId<?>> catalogServiceTransactionIds)
			throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getMetadataFromTransactionIds(catalogServiceTransactionIds);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public List<TransactionalMetadata> getNextPage(QueryPager queryPager)
			throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getNextPage(queryPager);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public List<String> getProperty(String key) throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getProperty(key);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public List<TransactionId<?>> getTransactionIdsForAllPages(
			QueryPager queryPager) throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getTransactionIdsForAllPages(queryPager);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public TransactionReceipt ingest(Metadata metadata)
			throws CatalogServiceException {
		try {
			return this.communicationChannelClient.ingest(metadata);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public boolean isRestrictIngestPermissions() throws CatalogServiceException {
		try {
			return this.communicationChannelClient.isRestrictIngestPermissions();
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

	public boolean isRestrictQueryPermissions() throws CatalogServiceException {
		try {
			return this.communicationChannelClient.isRestrictQueryPermissions();
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}
	
	public Page getNextPage(Page page) throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getNextPage(page);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}
	
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression) throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getPage(pageInfo, queryExpression);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}
	
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression, Set<String> catalogIds) throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getPage(pageInfo, queryExpression, catalogIds);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}
	
	public List<TransactionalMetadata> getMetadata(Page page) throws CatalogServiceException {
		try {
			return this.communicationChannelClient.getMetadata(page);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}
	
	public QueryPager query(QueryExpression queryExpression)
			throws CatalogServiceException {
		try {
			return this.communicationChannelClient.query(queryExpression);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}
	
	public QueryPager query(QueryExpression queryExpression, Set<String> catalogIds) throws CatalogServiceException {
		try {
			return this.communicationChannelClient.query(queryExpression, catalogIds);
		} catch (Exception e) {
			throw new CatalogServiceException("", e);
		}
	}

}
