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

package gov.nasa.jpl.oodt.cas.catalog.server.channel.rmi;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import gov.nasa.jpl.oodt.cas.catalog.metadata.TransactionalMetadata;
import gov.nasa.jpl.oodt.cas.catalog.page.CatalogReceipt;
import gov.nasa.jpl.oodt.cas.catalog.page.Page;
import gov.nasa.jpl.oodt.cas.catalog.page.PageInfo;
import gov.nasa.jpl.oodt.cas.catalog.page.QueryPager;
import gov.nasa.jpl.oodt.cas.catalog.page.TransactionReceipt;
import gov.nasa.jpl.oodt.cas.catalog.query.QueryExpression;
import gov.nasa.jpl.oodt.cas.catalog.server.channel.CommunicationChannelServer;
import gov.nasa.jpl.oodt.cas.catalog.struct.Dictionary;
import gov.nasa.jpl.oodt.cas.catalog.struct.Index;
import gov.nasa.jpl.oodt.cas.catalog.struct.TransactionId;
import gov.nasa.jpl.oodt.cas.catalog.system.Catalog;
import gov.nasa.jpl.oodt.cas.catalog.system.CatalogService;
import gov.nasa.jpl.oodt.cas.catalog.util.PluginURL;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

public class RmiCommunicationChannelServer implements
		CommunicationChannelServer {

	public void shutdown() throws IOException {
		// TODO Auto-generated method stub

	}

	public void startup()
			throws IOException {
		// TODO Auto-generated method stub

	}

	public void addCatalog(Catalog catalog) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void addCatalog(String catalogId, Index index) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void addCatalog(String catalogId, Index index,
			List<Dictionary> dictionaries) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void addCatalog(String catalogId, Index index,
			List<Dictionary> dictionaries, boolean restrictQueryPermission,
			boolean restrictIngestPermission) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void addDictionary(String catalogId, Dictionary dictionary)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void addPluginUrls(List<PluginURL> pluginURLs) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void delete(Metadata metadata) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public List<TransactionalMetadata> getAllPages(QueryPager queryPager)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Properties getCalalogProperties() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Properties getCalalogProperties(String catalogUrn) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public TransactionId<?> getCatalogServiceTransactionId(
			TransactionId<?> catalogTransactionId, String catalogUrn)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public TransactionId<?> getCatalogServiceTransactionId(
			CatalogReceipt catalogReceipt, boolean generateNew)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<TransactionId<?>> getCatalogServiceTransactionIds(
			List<TransactionId<?>> catalogTransactionIds, String catalogUrn)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getCurrentCatalogIds() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<TransactionalMetadata> getMetadata(Page page) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<TransactionalMetadata> getMetadataFromTransactionIdStrings(
			List<String> catalogServiceTransactionIdStrings) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<TransactionalMetadata> getMetadataFromTransactionIds(
			List<TransactionId<?>> catalogServiceTransactionIds)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Page getNextPage(Page page) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<TransactionalMetadata> getNextPage(QueryPager queryPager)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression,
			Set<String> catalogIds) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public URL getPluginStorageDir() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PluginURL> getPluginUrls() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<String> getProperty(String key) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public TransactionReceipt ingest(Metadata metadata) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isRestrictIngestPermissions() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRestrictQueryPermissions() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public void modifyIngestPermission(String catalogId,
			boolean restrictIngestPermission) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void modifyQueryPermission(String catalogId,
			boolean restrictQueryPermission) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public QueryPager query(QueryExpression queryExpression) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public QueryPager query(QueryExpression queryExpression,
			Set<String> catalogIds) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeCatalog(String catalogUrn) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void replaceCatalog(Catalog catalog) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void replaceDictionaries(String catalogId,
			List<Dictionary> dictionaries) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void replaceIndex(String catalogId, Index index) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void setCatalogService(CatalogService catalogService)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void setPort(int port) throws Exception {
		// TODO Auto-generated method stub
		
	}



}
