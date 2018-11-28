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
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
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

//APACHE imports
import org.apache.xmlrpc.WebServer;

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
	
	public void startup() throws Exception {
		this.webServer = new WebServer(this.port);
		this.webServer.addHandler(this.getClass().getSimpleName(), this);
		this.webServer.start();
	}
	
	public boolean xmlrpc_shutdown() throws Exception {
		this.shutdown();
		this.webServer.shutdown();
		this.webServer = null;
		return true;
	}
	
	public boolean xmlrpc_addCatalog(String catalogObject) throws Exception {
		this.addCatalog(this.serializer.deserializeObject(Catalog.class, catalogObject));
		return true;
	}
	
	public boolean xmlrpc_replaceCatalog(String catalogObject) throws Exception {
		this.replaceCatalog(this.serializer.deserializeObject(Catalog.class, catalogObject));
		return true;
	}
	
	public boolean xmlrpc_addCatalog(String catalogId, String indexObject) throws Exception {
		this.addCatalog(catalogId, this.serializer.deserializeObject(Index.class, indexObject));
		return true;
	}
	
	public boolean xmlrpc_addCatalog(String catalogId, String indexObject, String dictionariesObject) throws Exception {
		this.addCatalog(catalogId, this.serializer.deserializeObject(Index.class, indexObject), this.serializer.deserializeObject(List.class, dictionariesObject));
		return true;
	}

	public boolean xmlrpc_addCatalog(String catalogId, String indexObject, String dictionariesObject, String restrictQueryPermissionObject, String restrictIngestPermissionObject) throws Exception {
		this.addCatalog(catalogId, this.serializer.deserializeObject(Index.class, indexObject), this.serializer.deserializeObject(List.class, dictionariesObject), this.serializer.deserializeObject(Boolean.class, restrictQueryPermissionObject), this.serializer.deserializeObject(Boolean.class, restrictIngestPermissionObject));
		return true;
	}

	public boolean xmlrpc_addDictionary(String catalogId, String dictionaryObject) throws Exception {
		this.addDictionary(catalogId, this.serializer.deserializeObject(Dictionary.class, dictionaryObject));
		return true;
	}
	
	public boolean xmlrpc_replaceDictionaries(String catalogId, String dictionariesObject) throws Exception {
		this.replaceDictionaries(catalogId, this.serializer.deserializeObject(List.class, dictionariesObject));
		return true;
	}

	public boolean xmlrpc_replaceIndex(String catalogId, String indexObject) throws Exception {
		this.replaceIndex(catalogId, this.serializer.deserializeObject(Index.class, indexObject));
		return true;
	}

	public boolean xmlrpc_modifyIngestPermission(String catalogId, String restrictIngestPermissionObject) throws Exception {
		this.modifyIngestPermission(catalogId, this.serializer.deserializeObject(Boolean.class, restrictIngestPermissionObject));
		return true;
	}
	
	public boolean xmlrpc_modifyQueryPermission(String catalogId, String restrictQueryPermissionObject) throws Exception {
		this.modifyIngestPermission(catalogId, this.serializer.deserializeObject(Boolean.class, restrictQueryPermissionObject));
		return true;
	}
	
	public boolean xmlrpc_delete(String metadataObject) throws Exception {
		this.delete(this.serializer.deserializeObject(Metadata.class, metadataObject));	
		return true;
	}
	
	public String xmlrpc_getPluginUrls() throws Exception {
		return this.serializer.serializeObject(this.getPluginUrls());
	}
	
	public boolean xmlrpc_addPluginUrls(String pluginUrlsObject) throws Exception {
		this.addPluginUrls(this.serializer.deserializeObject(List.class, pluginUrlsObject));
		return true;
	}
	
	public String xmlrpc_getPluginStorageDir() throws Exception {
		return this.serializer.serializeObject(this.getPluginStorageDir());
	}
	
    public boolean xmlrpc_transferFile(String filePath, byte[] fileData, int offset, int numBytes) throws Exception {
        FileOutputStream fOut = null;
        try {
            File outFile = new File(filePath);
	        if (outFile.exists()) 
	        	fOut = new FileOutputStream(outFile, true);
	        else 
	        	fOut = new FileOutputStream(outFile, false);
	
	        fOut.write(fileData, (int) offset, (int) numBytes);
        }catch (Exception e) {
        	throw e;
        }finally {
        	try {
        		fOut.close();
        	}catch(Exception e) {}
        }
        return true;
    }

	public String xmlrpc_getAllPages(String queryPagerObject) throws Exception {
		return this.serializer.serializeObject(this.getAllPages(this.serializer.deserializeObject(QueryPager.class, queryPagerObject)));	
	}

	public String xmlrpc_getCalalogProperties() throws Exception {
		return this.serializer.serializeObject(this.getCalalogProperties());	
	}

	public String xmlrpc_getCalalogProperties(String catalogUrn)
			throws Exception {
		return this.serializer.serializeObject(this.getCalalogProperties(catalogUrn));	
	}

//	public String xmlrpc_getCatalog(String catalogUrn) throws Exception {
//		return this.serializer.serializeObject(this.getCatalog(catalogUrn));	
//	}

	public String xmlrpc_getCatalogServiceTransactionId(
			String catalogTransactionIdObject, String catalogUrn)
			throws Exception {
		return this.serializer.serializeObject(this.getCatalogServiceTransactionId(this.serializer.deserializeObject(TransactionId.class, catalogTransactionIdObject), catalogUrn));	
	}

	public String xmlrpc_getCatalogServiceTransactionId2(
			String catalogReceiptObject,
			String generateNewObject) throws Exception {
		return this.serializer.serializeObject(this.getCatalogServiceTransactionId(this.serializer.deserializeObject(CatalogReceipt.class, catalogReceiptObject), this.serializer.deserializeObject(Boolean.class, generateNewObject)));	
	}

	public String xmlrpc_getCatalogServiceTransactionIds(
			String catalogTransactionIdsObject, String catalogUrn)
			throws Exception {
		return this.serializer.serializeObject(this.getCatalogServiceTransactionIds(this.serializer.deserializeObject(List.class, catalogTransactionIdsObject), catalogUrn));	
	}

	public String xmlrpc_getCurrentCatalogIds() throws Exception {
		return this.serializer.serializeObject(this.getCurrentCatalogIds());	
	}

//	public String xmlrpc_getCurrentCatalogList() throws Exception {
//		return this.serializer.serializeObject(this.getCurrentCatalogList());	
//	}

	public String xmlrpc_getMetadataFromTransactionIdStrings(
			String catalogServiceTransactionIdStringsObject)
			throws Exception {
		return this.serializer.serializeObject(this.getMetadataFromTransactionIdStrings(this.serializer.deserializeObject(List.class, catalogServiceTransactionIdStringsObject)));	
	}

	public String xmlrpc_getMetadataFromTransactionIds(
			String catalogServiceTransactionIdsObject)
			throws Exception {
		return this.serializer.serializeObject(this.getMetadataFromTransactionIds(this.serializer.deserializeObject(List.class, catalogServiceTransactionIdsObject)));	
	}

	public String xmlrpc_getNextPage(String queryPagerObject)
			throws Exception {
		return this.serializer.serializeObject(this.getNextPage(this.serializer.deserializeObject(QueryPager.class, queryPagerObject)));	
	}

	public String xmlrpc_getProperty(String key) throws Exception {
		return this.serializer.serializeObject(this.getProperty(key));	
	}
//
//	public String xmlrpc_getTransactionIdClass() throws Exception {
//		return this.serializer.serializeObject(this.getTransactionIdClass());	
//	}

//	public String xmlrpc_getTransactionIdsForAllPages(
//			String queryPagerObject) throws Exception {
//		return this.serializer.serializeObject(this.getTransactionIdsForAllPages(this.serializer.deserializeObject(QueryPager.class, queryPagerObject)));	
//	}

	public String xmlrpc_ingest(String metadataObject)
			throws Exception {
		return this.serializer.serializeObject(this.ingest(this.serializer.deserializeObject(Metadata.class, metadataObject)));	
	}

	public String xmlrpc_isRestrictIngestPermissions() throws Exception {
		return this.serializer.serializeObject(new Boolean(this.isRestrictIngestPermissions()));	
	}

	public String xmlrpc_isRestrictQueryPermissions() throws Exception {
		return this.serializer.serializeObject(new Boolean(this.isRestrictQueryPermissions()));
	}

	public String xmlrpc_query(String queryExpressionObject) throws Exception {
		return this.serializer.serializeObject(this.query(this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject)));	
	}

	public String xmlrpc_query(String queryExpressionObject, String catalogIdsObject) throws Exception {
		return this.serializer.serializeObject(this.query(this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject), this.serializer.deserializeObject(Set.class, catalogIdsObject)));	
	}
	
//	public String xmlrpc_getFirstPage(String queryExpressionObject) throws Exception {
//		return this.serializer.serializeObject(this.getFirstPage(this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject)));	
//	}
//
//	public String xmlrpc_getFirstPage(String queryExpressionObject, String catalogIdsObject) throws Exception {
//		return this.serializer.serializeObject(this.getFirstPage(this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject), this.serializer.deserializeObject(Set.class, catalogIdsObject)));	
//	}
	
	public String xmlrpc_getNextPage2(String pageObject) throws Exception {
		return this.serializer.serializeObject(this.getNextPage(this.serializer.deserializeObject(Page.class, pageObject)));	
	}
	
	public String xmlrpc_getPage(String pageInfoObject, String queryExpressionObject) throws Exception {
		return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfoObject), this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject)));
	}
	
	public String xmlrpc_getPage(String pageInfoObject, String queryExpressionObject, String catalogIdsObject) throws Exception {
		return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, pageInfoObject), this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject), this.serializer.deserializeObject(Set.class, catalogIdsObject)));
	}
	
//	public String xmlrpc_getLastPage(String queryExpressionObject) throws Exception {
//		return this.serializer.serializeObject(this.getLastPage(this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject)));	
//	}
//
//	public String xmlrpc_getLastPage(String queryExpressionObject, String catalogIdsObject) throws Exception {
//		return this.serializer.serializeObject(this.getLastPage(this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject), this.serializer.deserializeObject(Set.class, catalogIdsObject)));	
//	}
	
	public String xmlrpc_getMetadata(String pageObject) throws Exception {
		return this.serializer.serializeObject(this.getMetadata(this.serializer.deserializeObject(Page.class, pageObject)));
	}
	
//	public String xmlrpc_query(String queryExpressionObject, String sortResultsObject) throws Exception {
//		System.out.println(this.getClass().getClassLoader());
//		return this.serializer.serializeObject(this.query(this.serializer.deserializeObject(QueryExpression.class, queryExpressionObject), this.serializer.deserializeObject(Boolean.class, sortResultsObject)));	
//	}

	public boolean xmlrpc_removeCatalog(String catalogUrn) throws Exception {
		this.removeCatalog(catalogUrn);
		return true;
	}
//
//	public boolean xmlrpc_removeCatalog(String catalogUrn, String preserveMappingObject) throws Exception {
//		this.removeCatalog(catalogUrn, this.serializer.deserializeObject(Boolean.class, preserveMappingObject));	
//		return true;
//	}
//	
//	public boolean xmlrpc_setHasIngestPermissions(String restrictIngestPermissionsObject) throws Exception {
//		this.setHasIngestPermissions(this.serializer.deserializeObject(Boolean.class, restrictIngestPermissionsObject));	
//		return true;
//	}
//
//	public boolean xmlrpc_setRestrictQueryPermissions(String restrictQueryPermissionsObject) throws Exception {
//		this.setRestrictQueryPermissions(this.serializer.deserializeObject(Boolean.class, restrictQueryPermissionsObject));		
//		return true;
//	}

//	public boolean setTransactionIdClass(String transactionIdClassObject) throws Exception {
//		this.setTransactionIdClass(this.serializer.deserializeObject(Class.class, transactionIdClassObject));	
//		return true;
//	}

}
