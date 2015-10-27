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

import org.apache.oodt.cas.catalog.exception.CatalogException;
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
import org.apache.xmlrpc.XmlRpcException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	
	public void shutdown() throws CatalogException {
		Vector<Object> args = new Vector<Object>();
	  try {
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_shutdown", args);
	  } catch (XmlRpcException e) {
		throw new CatalogException("Shutdown Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Shutdown Failed: "+ e.getMessage(), e);
	  }
	}

	public void addCatalog(Catalog catalog) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(catalog));
	  try {
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_addCatalog", args);
	  } catch (XmlRpcException e) {
		throw new CatalogException("Add Catalog Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Add Catalog Failed: "+ e.getMessage(), e);
	  }
	}
	
	public void replaceCatalog(Catalog catalog) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(catalog));
	  try {
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_replaceCatalog", args);
	  } catch (XmlRpcException e) {
		throw new CatalogException("Replace Catalog Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Replace Catalog Failed: "+ e.getMessage(), e);
	  }
	}
	
	public void addCatalog(String catalogId, Index index) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(index));
	  try {
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_addCatalog", args);
	  } catch (XmlRpcException e) {
		throw new CatalogException("Add Catalog Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Add Catalog Failed: "+ e.getMessage(), e);
	  }
	}
	
	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(index));
		args.add(this.serializer.serializeObject(dictionaries));
	  try {
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_addCatalog", args);
	  } catch (XmlRpcException e) {
		throw new CatalogException("Add Catalog Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Add Catalog Failed: "+ e.getMessage(), e);
	  }
	}

	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries, boolean restrictQueryPermission, boolean restrictIngestPermission) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(index));
		args.add(this.serializer.serializeObject(dictionaries));
		args.add(restrictQueryPermission);
		args.add(restrictIngestPermission);
	  try {
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_addCatalog", args);
	  } catch (XmlRpcException e) {
		throw new CatalogException("Add Catalog Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Add Catalog Failed: "+ e.getMessage(), e);
	  }
	}

	public void addDictionary(String catalogId, Dictionary dictionary) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(dictionary));
	  try {
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_addDictionary", args);
	  } catch (XmlRpcException e) {
		throw new CatalogException("Add Dictionary: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Add Dictionary: "+ e.getMessage(), e);
	  }
	}
	
	public void replaceDictionaries(String catalogId, List<Dictionary> dictionaries) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(dictionaries));
	  try {
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_addDictionary", args);
	  } catch (XmlRpcException e) {
		throw new CatalogException("Replace Dictionaries Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Replace Dictionaries Failed: "+ e.getMessage(), e);
	  }
	}

	public void replaceIndex(String catalogId, Index index) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(index));
	  try {
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_replaceIndex", args);
	  } catch (XmlRpcException e) {
		throw new CatalogException("Replace Index Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Replace Index Failed: "+ e.getMessage(), e);
	  }
	}

	public void modifyIngestPermission(String catalogId, boolean restrictIngestPermission) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(restrictIngestPermission));
	  try {
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_modifyIngestPermission", args);
	  } catch (XmlRpcException e) {
		throw new CatalogException("Modify Ingest Permission Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Modify Ingest Permission Failed: "+ e.getMessage(), e);
	  }
	}
	
	public void modifyQueryPermission(String catalogId, boolean restrictQueryPermission) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogId);
		args.add(this.serializer.serializeObject(restrictQueryPermission));
	  try {
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_modifyQueryPermission", args);
	  } catch (XmlRpcException e) {
		throw new CatalogException("Modify Query Permission Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Modify Query Permission Failed: "+ e.getMessage(), e);
	  }
	}
	
	public List<PluginURL> getPluginUrls() throws CatalogException {
		Vector<Object> args = new Vector<Object>();
	  try {
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPluginUrls", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Plugins Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Plugins Failed: "+ e.getMessage(), e);
	  }
	}
	
	public void addPluginUrls(List<PluginURL> pluginURLs) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pluginURLs));
	  try {
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_addPluginUrls", args);
	  } catch (XmlRpcException e) {
		throw new CatalogException("Add Plugins Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Add Plugins Failed: "+ e.getMessage(), e);
	  }
	}
	
	public URL getPluginStorageDir() throws CatalogException {
		Vector<Object> args = new Vector<Object>();
	  try {
		return this.serializer.deserializeObject(URL.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPluginStorageDir", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Plugin Storage Dir Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Plugin Storage Dir Failed: "+ e.getMessage(), e);
	  }
	}
	
	public void transferUrl(URL fromUrl, URL toURL) throws CatalogException {
		System.out.println("Transfering '" + fromUrl + "' to '" + toURL + "'");
        FileInputStream is = null;
        try {
            byte[] buf = new byte[this.chunkSize];
	        is = new FileInputStream(new File(fromUrl.getPath()));
            int offset = 0;
            int numBytes;
	        while ((numBytes = is.read(buf, offset, chunkSize)) != -1)
	            this.transferFile(new File(toURL.getPath()).getAbsolutePath(), buf, offset, numBytes);
        } catch (FileNotFoundException e) {
		  throw new CatalogException("Transfer URL Failed: "+ e.getMessage(), e);
		} catch (IOException e) {
		  throw new CatalogException("Transfer URL Failed: "+ e.getMessage(), e);
		} finally {
        	try {
        		is.close();
        	}catch(Exception ignored) {}
        }
	}
	
    protected void transferFile(String filePath, byte[] fileData, int offset,
            int numBytes) throws CatalogException {
        Vector<Object> argList = new Vector<Object>();
        argList.add(filePath);
        argList.add(fileData);
        argList.add(offset);
        argList.add(numBytes);
	  try {
		client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_transferFile", argList);
	  } catch (XmlRpcException e) {
		throw new CatalogException("Transfer File Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Transfer File Failed: "+ e.getMessage(), e);
	  }
	}

	public void delete(Metadata metadata) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(metadata));
	  try {
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_delete", args);
	  } catch (XmlRpcException e) {
		throw new CatalogException("Delete Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Delete Failed: "+ e.getMessage(), e);
	  }
	}

	public List<TransactionalMetadata> getAllPages(QueryPager queryPager) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryPager));
	  try {
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getAllPages", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get All Pages Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get All Pages Failed: "+ e.getMessage(), e);
	  }
	}

	public Properties getCalalogProperties() throws CatalogException {
	  try {
		return this.serializer.deserializeObject(Properties.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCalalogProperties", new Vector<Object>()));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Catalog Properties Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Catalog Properties Failed: "+ e.getMessage(), e);
	  }
	}

	public Properties getCalalogProperties(String catalogUrn)
			throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogUrn);
	  try {
		return this.serializer.deserializeObject(Properties.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCalalogProperties", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Catalog Properties Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Catalog Properties Failed: "+ e.getMessage(), e);
	  }
	}

//	public Catalog getCatalog(String catalogUrn) throws CatalogException {
//		Vector<Object> args = new Vector<Object>();
//		args.add(catalogUrn);
//		return this.serializer.deserializeObject(Catalog.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCatalog", args));
//	}

//	public CatalogRepository getCatalogRepository()
//			throws CatalogException {
//		return this.serializer.deserializeObject(CatalogRepository.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCatalogRepository", new Vector<Object>()));
//	}

	public TransactionId<?> getCatalogServiceTransactionId(
			TransactionId<?> catalogTransactionId, String catalogUrn)
			throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(catalogTransactionId));
		args.add(catalogUrn);
	  try {
		return this.serializer.deserializeObject(TransactionId.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCatalogServiceTransactionId", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Catalog Service Transaction Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Catalog Service Transaction Failed: "+ e.getMessage(), e);
	  }
	}

	public TransactionId<?> getCatalogServiceTransactionId(
			CatalogReceipt catalogReceipt,
			boolean generateNew) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(catalogReceipt));
		args.add(this.serializer.serializeObject(generateNew));
	  try {
		return this.serializer.deserializeObject(TransactionId.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCatalogServiceTransactionId2", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Catalog Services Transaction Id Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Catalog Services Transaction Id Failed: "+ e.getMessage(), e);
	  }
	}

	public List<TransactionId<?>> getCatalogServiceTransactionIds(
			List<TransactionId<?>> catalogTransactionIds, String catalogUrn)
			throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(catalogTransactionIds));
		args.add(catalogUrn);
	  try {
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCatalogServiceTransactionId", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Catalog Service Transaction Ids Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Catalog Service Transaction Ids Failed: "+ e.getMessage(), e);
	  }
	}

	public Set<String> getCurrentCatalogIds() throws CatalogException {
	  try {
		return this.serializer.deserializeObject(Set.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCurrentCatalogIds", new Vector<Object>()));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Curent Catalog Ids Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Current Catalog Ids Failed: "+ e.getMessage(), e);
	  }
	}

//	public Set<Catalog> getCurrentCatalogList() throws CatalogException {
//		return this.serializer.deserializeObject(Set.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getCurrentCatalogList", new Vector<Object>()));
//	}
//
//	public IngestMapper getIngestMapper() throws CatalogException { 
//		return this.serializer.deserializeObject(IngestMapper.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getIngestMapper", new Vector<Object>()));
//	}

	public List<TransactionalMetadata> getMetadataFromTransactionIdStrings(
			List<String> catalogServiceTransactionIdStrings)
			throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(catalogServiceTransactionIdStrings));
	  try {
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getMetadataFromTransactionIdStrings", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Metadata From Transaction Id Strings Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Metadata From Transaction Id String Failed: "+ e.getMessage(), e);
	  }
	}

	public List<TransactionalMetadata> getMetadataFromTransactionIds(
			List<TransactionId<?>> catalogServiceTransactionIds)
			throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(catalogServiceTransactionIds));
	  try {
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getMetadataFromTransactionIds", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Metadata Transaction Ids Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Metadata Transaction Ids Failed: "+ e.getMessage(), e);
	  }
	}

	public List<TransactionalMetadata> getNextPage(QueryPager queryPager)
			throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryPager));
	  try {
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getNextPage", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Next Page Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Next Page Failed: "+ e.getMessage(), e);
	  }
	}

	public List<String> getProperty(String key) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(key);
	  try {
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getProperty", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Property Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Property Failed: "+ e.getMessage(), e);
	  }
	}

	public Class<? extends TransactionId<?>> getTransactionIdClass() throws CatalogException {
	  try {
		return this.serializer.deserializeObject(Class.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getTransactionIdClass", new Vector<Object>()));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Transaction Id Class Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Transaction Id Calss Failed: "+ e.getMessage(), e);
	  }
	}

	public List<TransactionId<?>> getTransactionIdsForAllPages(
			QueryPager queryPager) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryPager));
	  try {
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getTransactionIdsForAllPages", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Transaction Ids For All Pages Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Transaction Ids For All Pages Failed: "+ e.getMessage(), e);
	  }
	}

	public TransactionReceipt ingest(Metadata metadata)
			throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(metadata));
	  try {
		return this.serializer.deserializeObject(TransactionReceipt.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_ingest", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Ingest Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Ingest Failed: "+ e.getMessage(), e);
	  }
	}

	public boolean isRestrictIngestPermissions() throws CatalogException {
	  try {
		return this.serializer.deserializeObject(Boolean.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_isRestrictIngestPermissions", new Vector<Object>()));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Is Restrict Ingest Permissions Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Is Restrict Ingest Permissions Failed: "+ e.getMessage(), e);
	  }
	}

	public boolean isRestrictQueryPermissions() throws CatalogException {
	  try {
		return this.serializer.deserializeObject(Boolean.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_isRestrictQueryPermissions", new Vector<Object>()));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Is Restrict Query Permissions Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Is Restrict Query Permissions Failed: "+ e.getMessage(), e);
	  }
	}

	public Page getFirstPage(QueryExpression queryExpression) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryExpression));
	  try {
		return this.serializer.deserializeObject(Page.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getFirstPage", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get First Page Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get First Page Failed: "+ e.getMessage(), e);
	  }
	}

	public Page getFirstPage(QueryExpression queryExpression, Set<String> catalogIds) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryExpression));
		args.add(this.serializer.serializeObject(catalogIds));
	  try {
		return this.serializer.deserializeObject(Page.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getFirstPage", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get First Page Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get First Page Failed: "+ e.getMessage(), e);
	  }
	}
	
	public Page getNextPage(Page page) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(page));
	  try {
		return this.serializer.deserializeObject(Page.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getNextPage2", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Next Page Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Next Page Failed: "+ e.getMessage(), e);
	  }
	}
	
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pageInfo));
		args.add(this.serializer.serializeObject(queryExpression));
	  try {
		return this.serializer.deserializeObject(Page.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPage", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Page Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Page Failed: "+ e.getMessage(), e);
	  }
	}
	
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression, Set<String> catalogIds) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(pageInfo));
		args.add(this.serializer.serializeObject(queryExpression));
		args.add(this.serializer.serializeObject(catalogIds));
	  try {
		return this.serializer.deserializeObject(Page.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getPage", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Page Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Page Failed: "+ e.getMessage(), e);
	  }
	}
	
	public Page getLastPage(QueryExpression queryExpression) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryExpression));
	  try {
		return this.serializer.deserializeObject(Page.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getLastPage", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Last Page Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Last Page Failed: "+ e.getMessage(), e);
	  }
	}

	public Page getLastPage(QueryExpression queryExpression, Set<String> catalogIds) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryExpression));
		args.add(this.serializer.serializeObject(catalogIds));
	  try {
		return this.serializer.deserializeObject(Page.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getLastPage", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Last Page Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Last Page Failed: "+ e.getMessage(), e);
	  }
	}
	
	public List<TransactionalMetadata> getMetadata(Page page) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(page));
	  try {
		return this.serializer.deserializeObject(List.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_getMetadata", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Get Metadata Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Get Metadata Failed: "+ e.getMessage(), e);
	  }
	}
	
	public QueryPager query(QueryExpression queryExpression)
			throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryExpression));
	  try {
		return this.serializer.deserializeObject(QueryPager.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_query", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Query Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Query Failed: "+ e.getMessage(), e);
	  }
	}

	public QueryPager query(QueryExpression queryExpression, Set<String> catalogIds) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(this.serializer.serializeObject(queryExpression));
		args.add(this.serializer.serializeObject(catalogIds));
	  try {
		return this.serializer.deserializeObject(QueryPager.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_query", args));
	  } catch (XmlRpcException e) {
		throw new CatalogException("Query Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Query Failed: "+ e.getMessage(), e);
	  }
	}
	
//	public QueryPager query(QueryExpression queryExpression, boolean sortResults)
//			throws CatalogException {
//		Vector<Object> args = new Vector<Object>();
//		args.add(this.serializer.serializeObject(queryExpression));
//		args.add(this.serializer.serializeObject(new Boolean(sortResults)));
//		return this.serializer.deserializeObject(QueryPager.class, (String) this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_query", args));
//	}

	public void removeCatalog(String catalogUrn) throws CatalogException {
		Vector<Object> args = new Vector<Object>();
		args.add(catalogUrn);
	  try {
		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_removeCatalog", args);
	  } catch (XmlRpcException e) {
		throw new CatalogException("Remove Catalog Failed: "+ e.getMessage(), e);
	  } catch (IOException e) {
		throw new CatalogException("Remove Catalog Failed: "+ e.getMessage(), e);
	  }
	}

//	public void removeCatalog(String catalogUrn, boolean preserveMapping)
//			throws CatalogException {
//		Vector<Object> args = new Vector<Object>();
//		args.add(catalogUrn);
//		args.add(this.serializer.serializeObject(new Boolean(preserveMapping)));
//		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_removeCatalog", args);		
//	}

//	public void setCatalogRepository(CatalogRepository catalogRepository)
//			throws CatalogException {
//		Vector<Object> args = new Vector<Object>();
//		args.add(this.serializer.serializeObject(catalogRepository));
//		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_setCatalogRepository", args);
//	}

//	public void setHasIngestPermissions(boolean restrictIngestPermissions) throws CatalogException {
//		Vector<Object> args = new Vector<Object>();
//		args.add(this.serializer.serializeObject(new Boolean(restrictIngestPermissions)));
//		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_setHasIngestPermissions", args);		
//	}

//	public void setIngestMapper(IngestMapper ingestMapper) throws CatalogException {
//		Vector<Object> args = new Vector<Object>();
//		args.add(this.serializer.serializeObject(ingestMapper));
//		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_setIngestMapper", args);		
//	}

//	public void setRestrictQueryPermissions(boolean restrictQueryPermissions) throws CatalogException {
//		Vector<Object> args = new Vector<Object>();
//		args.add(this.serializer.serializeObject(new Boolean(restrictQueryPermissions)));
//		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_setRestrictQueryPermissions", args);			
//	}

//	public void setTransactionIdClass(
//			Class<? extends TransactionId<?>> transactionIdClass) throws CatalogException {
//		Vector<Object> args = new Vector<Object>();
//		args.add(this.serializer.serializeObject(transactionIdClass));
//		this.client.execute(XmlRpcCommunicationChannelServer.class.getSimpleName() + ".xmlrpc_setTransactionIdClass", args);		
//	}


}
