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

//JDK imports

import org.apache.oodt.cas.catalog.exception.CatalogServiceException;
import org.apache.oodt.cas.catalog.page.CatalogReceipt;
import org.apache.oodt.cas.catalog.page.Page;
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.catalog.page.QueryPager;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.server.channel.AbstractCommunicationChannelServer;
import org.apache.oodt.cas.catalog.struct.Dictionary;
import org.apache.oodt.cas.catalog.struct.Index;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.system.Catalog;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.xmlrpc.WebServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;


/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Communication Channel Server over XML-RPC
 * <p>
 */
public class XmlRpcCommunicationChannelServer extends
		AbstractCommunicationChannelServer {

	private WebServer webServer;
	
	public XmlRpcCommunicationChannelServer() {
		super();
	}
	
	public void startup() {
		this.webServer = new WebServer(this.port);
		this.webServer.addHandler(this.getClass().getSimpleName(), this);
		this.webServer.start();
	}
	
	public boolean xmlrpc_shutdown() throws CatalogServiceException {
		this.shutdown();
		this.webServer.shutdown();
		this.webServer = null;
		return true;
	}
	
	public boolean xmlrpc_addCatalog(String catalogObject) throws CatalogServiceException {
		this.addCatalog(this.serializer.deserializeObject(Catalog.class, catalogObject));
		return true;
	}
	
	public boolean xmlrpc_replaceCatalog(String catalogObject) throws CatalogServiceException {
		this.replaceCatalog(this.serializer.deserializeObject(Catalog.class, catalogObject));
		return true;
	}
	
	public boolean xmlrpc_addCatalog(String catalogId, String indexObject) throws CatalogServiceException {
		this.addCatalog(catalogId, this.serializer.deserializeObject(Index.class, indexObject));
		return true;
	}
	
	public boolean xmlrpc_addCatalog(String catalogId, String indexObject, String dictionariesObject) throws CatalogServiceException {
		this.addCatalog(catalogId, this.serializer.deserializeObject(Index.class, indexObject), this.serializer.deserializeObject(List.class, dictionariesObject));
		return true;
	}

	public boolean xmlrpc_addCatalog(String catalogId, String indexObject, String dictionariesObject, String restrictQueryPermissionObject, String restrictIngestPermissionObject) throws CatalogServiceException {
		this.addCatalog(catalogId, this.serializer.deserializeObject(Index.class, indexObject), this.serializer.deserializeObject(List.class, dictionariesObject), this.serializer.deserializeObject(Boolean.class, restrictQueryPermissionObject), this.serializer.deserializeObject(Boolean.class, restrictIngestPermissionObject));
		return true;
	}

	public boolean xmlrpc_addDictionary(String catalogId, String dictionaryObject) throws CatalogServiceException {
		this.addDictionary(catalogId, this.serializer.deserializeObject(Dictionary.class, dictionaryObject));
		return true;
	}
	
	public boolean xmlrpc_replaceDictionaries(String catalogId, String dictionariesObject) throws CatalogServiceException {
		this.replaceDictionaries(catalogId, this.serializer.deserializeObject(List.class, dictionariesObject));
		return true;
	}

	public boolean xmlrpc_replaceIndex(String catalogId, String indexObject) throws CatalogServiceException {
		this.replaceIndex(catalogId, this.serializer.deserializeObject(Index.class, indexObject));
		return true;
	}

	public boolean xmlrpc_modifyIngestPermission(String catalogId, String restrictIngestPermissionObject) throws CatalogServiceException {
		this.modifyIngestPermission(catalogId, this.serializer.deserializeObject(Boolean.class, restrictIngestPermissionObject));
		return true;
	}
	
	public boolean xmlrpc_modifyQueryPermission(String catalogId, String restrictQueryPermissionObject) throws CatalogServiceException {
		this.modifyIngestPermission(catalogId, this.serializer.deserializeObject(Boolean.class, restrictQueryPermissionObject));
		return true;
	}
	
	public boolean xmlrpc_delete(String metadataObject) throws CatalogServiceException {
		this.delete(this.serializer.deserializeObject(Metadata.class, metadataObject));	
		return true;
	}
	
	public String xmlrpc_getPluginUrls() throws CatalogServiceException {
		return this.serializer.serializeObject(this.getPluginUrls());
	}
	
	public boolean xmlrpc_addPluginUrls(String pluginUrlsObject) throws CatalogServiceException {
		this.addPluginUrls(this.serializer.deserializeObject(List.class, pluginUrlsObject));
		return true;
	}
	
	public String xmlrpc_getPluginStorageDir() throws CatalogServiceException {
		return this.serializer.serializeObject(this.getPluginStorageDir());
	}
	
    public boolean xmlrpc_transferFile(String filePath, byte[] fileData, int offset, int numBytes)
		throws CatalogServiceException, IOException {
        FileOutputStream fOut = null;
        try {
            File outFile = new File(filePath);
	        if (outFile.exists()) 
	        	fOut = new FileOutputStream(outFile, true);
	        else 
	        	fOut = new FileOutputStream(outFile, false);
	
	        fOut.write(fileData, (int) offset, (int) numBytes);
        } finally {
        	try {
        		fOut.close();
        	}catch(Exception ignored) {}
        }
        return true;
    }

	public String xmlrpc_getAllPages(String queryPagerObject) throws CatalogServiceException {
		return this.serializer.serializeObject(this.getAllPages(this.serializer.deserializeObject(QueryPager.class, queryPagerObject)));	
	}

	public String xmlrpc_getCalalogProperties() throws CatalogServiceException {
		return this.serializer.serializeObject(this.getCalalogProperties());	
	}

	public String xmlrpc_getCalalogProperties(String catalogUrn)
			throws CatalogServiceException {
		return this.serializer.serializeObject(this.getCalalogProperties(catalogUrn));	
	}

//	public String xmlrpc_getCatalog(String catalogUrn) throws CatalogServiceException {
//		return this.serializer.serializeObject(this.getCatalog(catalogUrn));	
//	}

	public String xmlrpc_getCatalogServiceTransactionId(
			String catalogTransactionIdObject, String catalogUrn)
			throws CatalogServiceException {
		return this.serializer.serializeObject(this.getCatalogServiceTransactionId(this.serializer.deserializeObject(TransactionId.class, catalogTransactionIdObject), catalogUrn));	
	}

	public String xmlrpc_getCatalogServiceTransactionId2(
			String catalogReceiptObject,
			String generateNewObject) throws CatalogServiceException {
		return this.serializer.serializeObject(this.getCatalogServiceTransactionId(this.serializer.deserializeObject(CatalogReceipt.class, catalogReceiptObject), this.serializer.deserializeObject(Boolean.class, generateNewObject)));	
	}

	public String xmlrpc_getCatalogServiceTransactionIds(
			String catalogTransactionIdsObject, String catalogUrn)
			throws CatalogServiceException {
		return this.serializer.serializeObject(this.getCatalogServiceTransactionIds(this.serializer.deserializeObject(List.class, catalogTransactionIdsObject), catalogUrn));	
	}

	public String xmlrpc_getCurrentCatalogIds() throws CatalogServiceException {
		return this.serializer.serializeObject(this.getCurrentCatalogIds());	
	}

//	public String xmlrpc_getCurrentCatalogList() throws CatalogServiceException {
//		return this.serializer.serializeObject(this.getCurrentCatalogList());	
//	}

	public String xmlrpc_getMetadataFromTransactionIdStrings(
			String catalogServiceTransactionIdStringsObject)
			throws CatalogServiceException {
		return this.serializer.serializeObject(this.getMetadataFromTransactionIdStrings(this.serializer.deserializeObject(List.class, catalogServiceTransactionIdStringsObject)));	
	}

	public String xmlrpc_getMetadataFromTransactionIds(
			String catalogServiceTransactionIdsObject)
			throws CatalogServiceException {
		return this.serializer.serializeObject(this.getMetadataFromTransactionIds(this.serializer.deserializeObject(List.class, catalogServiceTransactionIdsObject)));	
	}

	public String xmlrpc_getNextPage(String queryPagerObject)
			throws CatalogServiceException {
		return this.serializer.serializeObject(this.getNextPage(this.serializer.deserializeObject(QueryPager.class, queryPagerObject)));	
	}

	public String xmlrpc_getProperty(String key) throws CatalogServiceException {
		return this.serializer.serializeObject(this.getProperty(key));	
	}
//
//	public String xmlrpc_getTransactionIdClass() throws CatalogServiceException {
//		return this.serializer.serializeObject(this.getTransactionIdClass());	
//	}

//	public String xmlrpc_getTransactionIdsForAllPages(
//			String queryPagerObject) throws CatalogServiceException {
//		return this.serializer.serializeObject(this.getTransactionIdsForAllPages(this.serializer.deserializeObject(QueryPager.class, queryPagerObject)));	
//	}

	public String xmlrpc_ingest(String metadataObject)
			throws CatalogServiceException {
		return this.serializer.serializeObject(this.ingest(this.serializer.deserializeObject(Metadata.class, metadataObject)));	
	}

	public String xmlrpc_isRestrictIngestPermissions() throws CatalogServiceException {
		return this.serializer.serializeObject(this.isRestrictIngestPermissions());
	}

	public String xmlrpc_isRestrictQueryPermissions() throws CatalogServiceException {
		return this.serializer.serializeObject(this.isRestrictQueryPermissions());
	}

	public String xmlrpc_query(String queryExpressionObject) throws CatalogServiceException {
		return this.serializer.serializeObject(this.query(this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject)));	
	}

	public String xmlrpc_query(String queryExpressionObject, String catalogIdsObject) throws CatalogServiceException {
		return this.serializer.serializeObject(this.query(this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject), this.serializer.deserializeObject(Set.class, catalogIdsObject)));	
	}
	
//	public String xmlrpc_getFirstPage(String queryExpressionObject) throws CatalogServiceException {
//		return this.serializer.serializeObject(this.getFirstPage(this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject)));	
//	}
//
//	public String xmlrpc_getFirstPage(String queryExpressionObject, String catalogIdsObject) throws CatalogServiceException {
//		return this.serializer.serializeObject(this.getFirstPage(this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject), this.serializer.deserializeObject(Set.class, catalogIdsObject)));	
//	}
	
	public String xmlrpc_getNextPage2(String pageObject) throws CatalogServiceException {
		return this.serializer.serializeObject(this.getNextPage(this.serializer.deserializeObject(Page.class, pageObject)));	
	}
	
	public String xmlrpc_getPage(String pageInfoObject, String queryExpressionObject) throws CatalogServiceException {
		return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfoObject), this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject)));
	}
	
	public String xmlrpc_getPage(String pageInfoObject, String queryExpressionObject, String catalogIdsObject) throws CatalogServiceException {
		return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfoObject), this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject), this.serializer.deserializeObject(Set.class, catalogIdsObject)));
	}
	
//	public String xmlrpc_getLastPage(String queryExpressionObject) throws CatalogServiceException {
//		return this.serializer.serializeObject(this.getLastPage(this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject)));	
//	}
//
//	public String xmlrpc_getLastPage(String queryExpressionObject, String catalogIdsObject) throws CatalogServiceException {
//		return this.serializer.serializeObject(this.getLastPage(this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject), this.serializer.deserializeObject(Set.class, catalogIdsObject)));	
//	}
	
	public String xmlrpc_getMetadata(String pageObject) throws CatalogServiceException {
		return this.serializer.serializeObject(this.getMetadata(this.serializer.deserializeObject(Page.class, pageObject)));
	}
	
//	public String xmlrpc_query(String queryExpressionObject, String sortResultsObject) throws CatalogServiceException {
//		System.out.println(this.getClass().getClassLoader());
//		return this.serializer.serializeObject(this.query(this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject), this.serializer.deserializeObject(Boolean.class, sortResultsObject)));	
//	}

	public boolean xmlrpc_removeCatalog(String catalogUrn) throws CatalogServiceException {
		this.removeCatalog(catalogUrn);
		return true;
	}
//
//	public boolean xmlrpc_removeCatalog(String catalogUrn, String preserveMappingObject) throws CatalogServiceException {
//		this.removeCatalog(catalogUrn, this.serializer.deserializeObject(Boolean.class, preserveMappingObject));	
//		return true;
//	}
//	
//	public boolean xmlrpc_setHasIngestPermissions(String restrictIngestPermissionsObject) throws CatalogServiceException {
//		this.setHasIngestPermissions(this.serializer.deserializeObject(Boolean.class, restrictIngestPermissionsObject));	
//		return true;
//	}
//
//	public boolean xmlrpc_setRestrictQueryPermissions(String restrictQueryPermissionsObject) throws CatalogServiceException {
//		this.setRestrictQueryPermissions(this.serializer.deserializeObject(Boolean.class, restrictQueryPermissionsObject));		
//		return true;
//	}

//	public boolean setTransactionIdClass(String transactionIdClassObject) throws CatalogServiceException {
//		this.setTransactionIdClass(this.serializer.deserializeObject(Class.class, transactionIdClassObject));	
//		return true;
//	}

}
