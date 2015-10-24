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
package org.apache.oodt.cas.catalog.server.channel.xmlrpc;

import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
import org.apache.oodt.cas.catalog.page.CatalogReceipt;
import org.apache.oodt.cas.catalog.page.Page;
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.catalog.page.QueryPager;
import org.apache.oodt.cas.catalog.page.TransactionReceipt;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.server.channel.AbstractCommunicationChannelClient;
import org.apache.oodt.cas.catalog.struct.Dictionary;
import org.apache.oodt.cas.catalog.struct.Index;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.system.Catalog;
import org.apache.oodt.cas.catalog.util.PluginURL;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.xmlrpc.CommonsXmlRpcTransportFactory;
import org.apache.xmlrpc.XmlRpcClient;

import java.io.File;
import java.io.FileInputStream;
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
 * A Communication Channel Client over XML-RPC
 * <p>
 */
public class XmlRpcCommunicationChannelClient extends AbstractCommunicationChannelClient {

	protected XmlRpcClient client;
	protected int chunkSize;
	
	public XmlRpcCommunicationChannelClient(URL serverUrl, int connectionTimeout, int requestTimeout, int chunkSize) {
		super();
        CommonsXmlRpcTransportFactory transportFactory = new CommonsXmlRpcTransportFactory(serverUrl);
        transportFactory.setConnectionTimeout(connectionTimeout * 60 * 1000);
        transportFactory.setTimeout(requestTimeout * 60 * 1000);        
		this.client = new XmlRpcClient(serverUrl, transportFactory);
		this.chunkSize = chunkSize;
	}
	
	public void shutdown() throws Exception {
		Vector<Object> args = new Vector<Object>();
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_shutdown", args);
	}

	public void addCatalog(Catalog catalog) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(catalog));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_addCatalog", args);
	}
	
	public void replaceCatalog(Catalog catalog) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(catalog));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_replaceCatalog", args);	
	}
	
	public void addCatalog(String catalogId, Index index) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(index));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_addCatalog", args);
	}
	
	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(index));
		args.add(this.serializer.serializeObject(dictionaries));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_addCatalog", args);
	}

	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries, boolean restrictQueryPermission, boolean restrictIngestPermission) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(index));
		args.add(this.serializer.serializeObject(dictionaries));
		args.add(restrictQueryPermission);
		args.add(restrictIngestPermission);
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_addCatalog", args);
	}

	public void addDictionary(String catalogId, Dictionary dictionary) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(dictionary));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_addDictionary", args);
	}
	
	public void replaceDictionaries(String catalogId, List<Dictionary> dictionaries) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(dictionaries));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_addDictionary", args);
	}

	public void replaceIndex(String catalogId, Index index) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(index));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_replaceIndex", args);
	}

	public void modifyIngestPermission(String catalogId, boolean restrictIngestPermission) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(restrictIngestPermission));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_modifyIngestPermission", args);
	}
	
	public void modifyQueryPermission(String catalogId, boolean restrictQueryPermission) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(restrictQueryPermission));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_modifyQueryPermission", args);
	}
	
	public List<PluginURL> getPluginUrls() throws Exception {
		Vector<Object> args = new Vector<Object>();
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPluginUrls", args));
	}
	
	public void addPluginUrls(List<PluginURL> pluginURLs) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pluginURLs));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_addPluginUrls", args);
	}
	
	public URL getPluginStorageDir() throws Exception {
		Vector<Object> args = new Vector<Object>();
		return this.serializer.deserializeObject(URL.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPluginStorageDir", args));	
	}
	
	public void transferUrl(URL fromUrl, URL toURL) throws Exception {
		System.out.println("Transfering '" + fromUrl + "' to '" + toURL + "'");
        FileInputStream is = null;
        try {
            byte[] buf = new byte[this.chunkSize];
	        is = new FileInputStream(new File(fromUrl.getPath()));
            int offset = 0;
            int numBytes = 0;
	        while ((numBytes = is.read(buf, offset, chunkSize)) != -1)
	            this.transferFile(new File(toURL.getPath()).getAbsolutePath(), buf, offset, numBytes);
        }catch (Exception e) {
        	throw e;
        }finally {
        	try {
        		is.close();
        	}catch(Exception ignored) {}
        }
	}
	
    protected void transferFile(String filePath, byte[] fileData, int offset,
            int numBytes) throws Exception {
        Vector<Object> argList = new Vector<Object>();
        argList.add(filePath);
        argList.add(fileData);
        argList.add(offset);
        argList.add(numBytes);
        client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_transferFile", argList);
    }

	public void delete(Metadata metadata) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(metadata));
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_delete", args);		
	}

	public List<TransactionalMetadata> getAllPages(QueryPager queryPager) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryPager));
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getAllPages", args));
	}

	public Properties getCalalogProperties() throws Exception {
		return this.serializer.deserializeObject(Properties.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCalalogProperties", new Vector<Object>()));
	}

	public Properties getCalalogProperties(String catalogUrn)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogUrn);
		return this.serializer.deserializeObject(Properties.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCalalogProperties", args));
	}

//	public Catalog getCatalog(String catalogUrn) throws Exception {
//		Vector<Object> args = new Vector<Object>();
//		args.add(catalogUrn);
//		return this.serializer.deserializeObject(Catalog.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCatalog", args));
//	}

//	public CatalogRepository getCatalogRepository()
//			throws Exception {
//		return this.serializer.deserializeObject(CatalogRepository.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCatalogRepository", new Vector<Object>()));
//	}

	public TransactionId<?> getCatalogServiceTransactionId(
			TransactionId<?> catalogTransactionId, String catalogUrn)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(catalogTransactionId));
		args.add(catalogUrn);
		return this.serializer.deserializeObject(TransactionId.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCatalogServiceTransactionId", args));
	}

	public TransactionId<?> getCatalogServiceTransactionId(
			CatalogReceipt catalogReceipt,
			boolean generateNew) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(catalogReceipt));
		args.add(this.serializer.serializeObject(generateNew));
		return this.serializer.deserializeObject(TransactionId.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCatalogServiceTransactionId2", args));
	}

	public List<TransactionId<?>> getCatalogServiceTransactionIds(
			List<TransactionId<?>> catalogTransactionIds, String catalogUrn)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(catalogTransactionIds));
		args.add(catalogUrn);
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCatalogServiceTransactionId", args));
	}

	public Set<String> getCurrentCatalogIds() throws Exception {
		return this.serializer.deserializeObject(Set.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCurrentCatalogIds", new Vector<Object>()));
	}

//	public Set<Catalog> getCurrentCatalogList() throws Exception {
//		return this.serializer.deserializeObject(Set.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCurrentCatalogList", new Vector<Object>()));
//	}
//
//	public IngestMapper getIngestMapper() throws Exception { 
//		return this.serializer.deserializeObject(IngestMapper.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getIngestMapper", new Vector<Object>()));
//	}

	public List<TransactionalMetadata> getMetadataFromTransactionIdStrings(
			List<String> catalogServiceTransactionIdStrings)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(catalogServiceTransactionIdStrings));
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getMetadataFromTransactionIdStrings", args));
	}

	public List<TransactionalMetadata> getMetadataFromTransactionIds(
			List<TransactionId<?>> catalogServiceTransactionIds)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(catalogServiceTransactionIds));
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getMetadataFromTransactionIds", args));
	}

	public List<TransactionalMetadata> getNextPage(QueryPager queryPager)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryPager));
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getNextPage", args));
	}

	public List<String> getProperty(String key) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(key);
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getProperty", args));
	}

	public Class<? extends TransactionId<?>> getTransactionIdClass() throws Exception {
		return this.serializer.deserializeObject(Class.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getTransactionIdClass", new Vector<Object>()));
	}

	public List<TransactionId<?>> getTransactionIdsForAllPages(
			QueryPager queryPager) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryPager));
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getTransactionIdsForAllPages", args));
	}

	public TransactionReceipt ingest(Metadata metadata)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(metadata));
		return this.serializer.deserializeObject(TransactionReceipt.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_ingest", args));
	}

	public boolean isRestrictIngestPermissions() throws Exception {
		return this.serializer.deserializeObject(Boolean.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_isRestrictIngestPermissions", new Vector<Object>()));
	}

	public boolean isRestrictQueryPermissions() throws Exception {
		return this.serializer.deserializeObject(Boolean.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_isRestrictQueryPermissions", new Vector<Object>()));
	}

	public Page getFirstPage(QueryExpression queryExpression) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryExpression));
		return this.serializer.deserializeObject(Page.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getFirstPage", args));
	}

	public Page getFirstPage(QueryExpression queryExpression, Set<String> catalogIds) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryExpression));
		args.add(this.serializer.serializeObject(catalogIds));
		return this.serializer.deserializeObject(Page.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getFirstPage", args));
	}
	
	public Page getNextPage(Page page) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(page));
		return this.serializer.deserializeObject(Page.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getNextPage2", args));
	}
	
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pageInfo));
		args.add(this.serializer.serializeObject(queryExpression));
		return this.serializer.deserializeObject(Page.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPage", args));
	}
	
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression, Set<String> catalogIds) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pageInfo));
		args.add(this.serializer.serializeObject(queryExpression));
		args.add(this.serializer.serializeObject(catalogIds));
		return this.serializer.deserializeObject(Page.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPage", args));
	}
	
	public Page getLastPage(QueryExpression queryExpression) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryExpression));
		return this.serializer.deserializeObject(Page.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getLastPage", args));
	}

	public Page getLastPage(QueryExpression queryExpression, Set<String> catalogIds) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryExpression));
		args.add(this.serializer.serializeObject(catalogIds));
		return this.serializer.deserializeObject(Page.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getLastPage", args));
	}
	
	public List<TransactionalMetadata> getMetadata(Page page) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(page));
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getMetadata", args));
	}
	
	public QueryPager query(QueryExpression queryExpression)
			throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryExpression));
		return this.serializer.deserializeObject(QueryPager.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_query", args));
	}

	public QueryPager query(QueryExpression queryExpression, Set<String> catalogIds) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryExpression));
		args.add(this.serializer.serializeObject(catalogIds));
		return this.serializer.deserializeObject(QueryPager.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_query", args));
	}
	
//	public QueryPager query(QueryExpression queryExpression, boolean sortResults)
//			throws Exception {
//		Vector<Object> args = new Vector<Object>();
//		args.add(this.serializer.serializeObject(queryExpression));
//		args.add(this.serializer.serializeObject(new Boolean(sortResults)));
//		return this.serializer.deserializeObject(QueryPager.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_query", args));
//	}

	public void removeCatalog(String catalogUrn) throws Exception {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogUrn);
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_removeCatalog", args);		
	}

//	public void removeCatalog(String catalogUrn, boolean preserveMapping)
//			throws Exception {
//		Vector<Object> args = new Vector<Object>();
//		args.add(catalogUrn);
//		args.add(this.serializer.serializeObject(new Boolean(preserveMapping)));
//		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_removeCatalog", args);		
//	}

//	public void setCatalogRepository(CatalogRepository catalogRepository)
//			throws Exception {
//		Vector<Object> args = new Vector<Object>();
//		args.add(this.serializer.serializeObject(catalogRepository));
//		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_setCatalogRepository", args);
//	}

//	public void setHasIngestPermissions(boolean restrictIngestPermissions) throws Exception {
//		Vector<Object> args = new Vector<Object>();
//		args.add(this.serializer.serializeObject(new Boolean(restrictIngestPermissions)));
//		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_setHasIngestPermissions", args);		
//	}

//	public void setIngestMapper(IngestMapper ingestMapper) throws Exception {
//		Vector<Object> args = new Vector<Object>();
//		args.add(this.serializer.serializeObject(ingestMapper));
//		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_setIngestMapper", args);		
//	}

//	public void setRestrictQueryPermissions(boolean restrictQueryPermissions) throws Exception {
//		Vector<Object> args = new Vector<Object>();
//		args.add(this.serializer.serializeObject(new Boolean(restrictQueryPermissions)));
//		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_setRestrictQueryPermissions", args);			
//	}

//	public void setTransactionIdClass(
//			Class<? extends TransactionId<?>> transactionIdClass) throws Exception {
//		Vector<Object> args = new Vector<Object>();
//		args.add(this.serializer.serializeObject(transactionIdClass));
//		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_setTransactionIdClass", args);		
//	}


}
