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
package org.apache.oodt.cas.catalog.server.channel.rmi;

//JDK imports
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.catalog.mapping.IngestMapper;
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
import org.apache.oodt.cas.catalog.page.QueryPager;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.repository.CatalogRepository;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.system.Catalog;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 */
public interface RmiCommunicationChannelClientInterface extends Remote {
	
	void setCatalogRepository(CatalogRepository catalogRepository);
	
	CatalogRepository getCatalogRepository() throws Exception;
	
	IngestMapper getIngestMapper() throws RemoteException;

	void setIngestMapper(IngestMapper ingestMapper) throws RemoteException;

	boolean isRestrictQueryPermissions() throws RemoteException;

	void setRestrictQueryPermissions(boolean restrictQueryPermissions) throws RemoteException;

	boolean isHasIngestPermissions() throws RemoteException;

	void setHasIngestPermissions(boolean restrictIngestPermissions) throws RemoteException;

	Class<? extends TransactionId<?>> getTransactionIdClass() throws RemoteException;

	void setTransactionIdClass(Class<? extends TransactionId<?>> transactionIdClass) throws RemoteException;

	void addCatalog(Catalog catalog) throws RemoteException;
	
	void addCatalog(Catalog catalog, boolean allowOverride) throws RemoteException;
	
	void removeCatalog(String catalogUrn) throws RemoteException;

	void removeCatalog(String catalogUrn, boolean preserveMapping) throws RemoteException;

	Set<Catalog> getCurrentCatalogList() throws RemoteException;
	
	Catalog getCatalog(String catalogUrn) throws RemoteException;

	Set<String> getCurrentCatalogIds() throws RemoteException;
		
	TransactionId<?> ingest(Metadata metadata) throws RemoteException;
	
	void delete(Metadata metadata) throws RemoteException;
	
	List<String> getProperty(String key) throws RemoteException;

	Properties getCalalogProperties() throws RemoteException;
	
	Properties getCalalogProperties(String catalogUrn) throws RemoteException;
	
	QueryPager query(QueryExpression queryExpression) throws RemoteException;
	
	QueryPager query(QueryExpression queryExpression, boolean sortResults) throws RemoteException;
 
	Set<TransactionalMetadata> getNextPage(QueryPager queryPager) throws RemoteException;

	Set<TransactionId<?>> getTransactionIdsForAllPages(QueryPager queryPager) throws RemoteException;
	
	Set<TransactionalMetadata> getAllPages(QueryPager queryPager) throws RemoteException;
	
	Set<TransactionalMetadata> getMetadataFromTransactionIdStrings(List<String> catalogServiceTransactionIdStrings) throws RemoteException;
	
	Set<TransactionalMetadata> getMetadataFromTransactionIds(List<TransactionId<?>> catalogServiceTransactionIds) throws RemoteException;
	
	Set<TransactionId<?>> getCatalogServiceTransactionIds(List<TransactionId<?>> catalogTransactionIds,
														  String catalogUrn) throws RemoteException;
	
	TransactionId<?> getCatalogServiceTransactionId(TransactionId<?> catalogTransactionId, String catalogUrn) throws RemoteException;
	
	TransactionId<?> getCatalogServiceTransactionId(TransactionId<?> catalogTransactionId, String catalogUrn,
													boolean generateNew) throws RemoteException;
}
