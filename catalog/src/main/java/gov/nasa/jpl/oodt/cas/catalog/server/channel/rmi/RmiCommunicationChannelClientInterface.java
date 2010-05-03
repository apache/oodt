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

import gov.nasa.jpl.oodt.cas.catalog.mapping.IngestMapper;
import gov.nasa.jpl.oodt.cas.catalog.metadata.TransactionalMetadata;
import gov.nasa.jpl.oodt.cas.catalog.page.QueryPager;
import gov.nasa.jpl.oodt.cas.catalog.query.QueryExpression;
import gov.nasa.jpl.oodt.cas.catalog.repository.CatalogRepository;
import gov.nasa.jpl.oodt.cas.catalog.struct.TransactionId;
import gov.nasa.jpl.oodt.cas.catalog.system.Catalog;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public interface RmiCommunicationChannelClientInterface extends Remote {
	
	public void setCatalogRepository(CatalogRepository catalogRepository);
	
	public CatalogRepository getCatalogRepository() throws Exception;
	
	public IngestMapper getIngestMapper() throws RemoteException;

	public void setIngestMapper(IngestMapper ingestMapper) throws RemoteException;

	public boolean isRestrictQueryPermissions() throws RemoteException;

	public void setRestrictQueryPermissions(boolean restrictQueryPermissions) throws RemoteException;

	public boolean isHasIngestPermissions() throws RemoteException;

	public void setHasIngestPermissions(boolean restrictIngestPermissions) throws RemoteException;

	public Class<? extends TransactionId<?>> getTransactionIdClass() throws RemoteException;

	public void setTransactionIdClass(Class<? extends TransactionId<?>> transactionIdClass) throws RemoteException;

	public void addCatalog(Catalog catalog) throws RemoteException;
	
	public void addCatalog(Catalog catalog, boolean allowOverride) throws RemoteException;
	
	public void removeCatalog(String catalogUrn) throws RemoteException;

	public void removeCatalog(String catalogUrn, boolean preserveMapping) throws RemoteException;

	public Set<Catalog> getCurrentCatalogList() throws RemoteException;
	
	public Catalog getCatalog(String catalogUrn) throws RemoteException;

	public Set<String> getCurrentCatalogIds() throws RemoteException;
		
	public TransactionId<?> ingest(Metadata metadata) throws RemoteException;
	
	public void delete(Metadata metadata) throws RemoteException;
	
	public List<String> getProperty(String key) throws RemoteException;

	public Properties getCalalogProperties() throws RemoteException;
	
	public Properties getCalalogProperties(String catalogUrn) throws RemoteException;
	
	public QueryPager query(QueryExpression queryExpression) throws RemoteException;
	
	public QueryPager query(QueryExpression queryExpression, boolean sortResults) throws RemoteException;
 
	public Set<TransactionalMetadata> getNextPage(QueryPager queryPager) throws RemoteException;

	public Set<TransactionId<?>> getTransactionIdsForAllPages(QueryPager queryPager) throws RemoteException;
	
	public Set<TransactionalMetadata> getAllPages(QueryPager queryPager) throws RemoteException;
	
	public Set<TransactionalMetadata> getMetadataFromTransactionIdStrings(List<String> catalogServiceTransactionIdStrings) throws RemoteException;
	
	public Set<TransactionalMetadata> getMetadataFromTransactionIds(List<TransactionId<?>> catalogServiceTransactionIds) throws RemoteException;
	
	public Set<TransactionId<?>> getCatalogServiceTransactionIds(List<TransactionId<?>> catalogTransactionIds, String catalogUrn) throws RemoteException;
	
	public TransactionId<?> getCatalogServiceTransactionId(TransactionId<?> catalogTransactionId, String catalogUrn) throws RemoteException;
	
	public TransactionId<?> getCatalogServiceTransactionId(TransactionId<?> catalogTransactionId, String catalogUrn, boolean generateNew) throws RemoteException;
}
