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
package org.apache.oodt.cas.catalog.system.impl;

//OODT imports
import org.apache.oodt.cas.catalog.exception.CatalogException;
import org.apache.oodt.cas.catalog.exception.CatalogServiceException;
import org.apache.oodt.cas.catalog.mapping.IngestMapper;
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
import org.apache.oodt.cas.catalog.page.*;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.query.QueryLogicalGroup;
import org.apache.oodt.cas.catalog.query.WrapperQueryExpression;
import org.apache.oodt.cas.catalog.repository.CatalogRepository;
import org.apache.oodt.cas.catalog.struct.Dictionary;
import org.apache.oodt.cas.catalog.struct.Index;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.struct.TransactionIdFactory;
import org.apache.oodt.cas.catalog.system.Catalog;
import org.apache.oodt.cas.catalog.system.CatalogService;
import org.apache.oodt.cas.catalog.util.PluginURL;
import org.apache.oodt.cas.catalog.util.QueryUtils;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Calatog Service that manages Metadata via one or more underlying Catalogs
 * <p>
 */
public class CatalogServiceLocal implements CatalogService {
	
	private static Logger LOG = Logger.getLogger(CatalogServiceLocal.class.getName());
	
	protected Set<Catalog> catalogs;
	protected ReadWriteLock catalogsLock;
	protected CatalogRepository catalogRepository;
	protected IngestMapper ingestMapper;
	protected ReadWriteLock ingestMapperLock;
	protected boolean restrictQueryPermissions;
	protected boolean restrictIngestPermissions;
	protected TransactionIdFactory transactionIdFactory;
	protected File pluginStorageDir;
	protected boolean oneCatalogFailsAllFail;
	protected boolean simplifyQueries;
	protected boolean disableIntersectingCrossCatalogQueries;
	protected int crossCatalogResultSortingThreshold;
	
	public CatalogServiceLocal(CatalogRepository catalogRepository, IngestMapper ingestMapper, File pluginStorageDir, TransactionIdFactory transactionIdFactory, boolean restrictQueryPermissions, boolean restrictIngestPermissions, boolean oneCatalogFailsAllFail, boolean simplifyQueries, boolean disableIntersectingCrossCatalogQueries, int crossCatalogResultSortingThreshold) throws InstantiationException {
		try {
			this.catalogs = new HashSet<Catalog>();
			this.catalogsLock = new ReentrantReadWriteLock();
			this.ingestMapperLock = new ReentrantReadWriteLock();
			this.setPluginStorageDir(pluginStorageDir);
			this.setRestrictQueryPermissions(restrictQueryPermissions);
			this.setRestrictIngestPermissions(restrictIngestPermissions);
			this.setTransactionIdFactory(transactionIdFactory);
			this.setIngestMapper(ingestMapper);
			this.setCatalogRepository(catalogRepository);	
			this.oneCatalogFailsAllFail = oneCatalogFailsAllFail;
			this.simplifyQueries = simplifyQueries;
			this.disableIntersectingCrossCatalogQueries = disableIntersectingCrossCatalogQueries;
			this.crossCatalogResultSortingThreshold = crossCatalogResultSortingThreshold;
		}catch (Exception e) {
			e.printStackTrace();
			throw new InstantiationException(e.getMessage());
		}
	}
	
	/**
	 * Set the CatalogRepository for this CatalogService, with replace existing CatalogRepository
	 * and immediately load all Catalogs from it.
	 * @throws CatalogServiceException On Error loading given CatalogRepository
	 */
	protected void setCatalogRepository(CatalogRepository catalogRepository) throws CatalogServiceException {
		if (catalogRepository != null) {
			this.catalogsLock.writeLock().lock();
			CatalogRepository backupRepository = null;
			Set<Catalog> backupCatalogs = null;
			try {
				LOG.log(Level.INFO, "Using CatalogRepository '" + catalogRepository.getClass().getName() + "'");
				backupRepository = this.catalogRepository;
				backupCatalogs = new HashSet<Catalog>(this.catalogs);
				LOG.log(Level.INFO, "Loading Catalogs from CatalogRepository . . .");
				this.catalogs = catalogRepository.deserializeAllCatalogs();
				LOG.log(Level.INFO, "Loaded Catalogs: '" + this.catalogs + "'");
				this.catalogRepository = catalogRepository;
			}catch (Exception e) {
				this.catalogs = backupCatalogs;
				this.catalogRepository = backupRepository;
				throw new CatalogServiceException("Failed to set CatalogRepository '" + catalogRepository + "', reverting back to original settings : " + e.getMessage(), e);
			}finally {
				this.catalogsLock.writeLock().unlock();
			}
		}else {
			throw new CatalogServiceException("Cannot add NULL CatalogRepository to CatalogService, reverting back to original settings");
		}
	}

	protected void setIngestMapper(IngestMapper ingestMapper) {
		this.ingestMapperLock.writeLock().lock();
		try {
			LOG.log(Level.INFO, "Using IngestMapper '" + ingestMapper.getClass().getName() + "'");
			this.ingestMapper = ingestMapper;
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to set ingest mapper : " + e.getMessage(), e);
		}finally {
			this.ingestMapperLock.writeLock().unlock();
		}
	}

	public void shutdown() throws CatalogServiceException {
		this.catalogsLock.writeLock().lock();
		this.ingestMapperLock.writeLock().lock();
	}
	
	/**
	 * Returns true if this CatalogService is restricting any queries
	 * from being made to the Catalogs it is managing
	 * @return True is restricting queries, false if restriction is
	 * on a per Catalog bases.
	 */
	public boolean isRestrictQueryPermissions() {
		return this.restrictIngestPermissions;
	}

	/**
	 * Modify this CatalogServices query restriction, default is false.
	 * @param restrictQueryPermissions True to block all querys to managing
	 * Catalogs or false to leave it at a per Catalog bases.
	 */
	protected void setRestrictQueryPermissions(boolean restrictQueryPermissions) {
		this.restrictQueryPermissions = restrictQueryPermissions;
	}

	/**
	 * Returns true if this CatalogService is restricting any ingestions
	 * from being made to the Catalogs it is managing
	 * @return True is restricting ingestions, false if restriction is
	 * on a per Catalog bases.
	 */
	public boolean isRestrictIngestPermissions() {
		return this.restrictIngestPermissions;
	}

	/**
	 * Modify this CatalogServices ingest restriction, default is false.
	 * @param restrictIngestPermissions True to block all ingestions to managing
	 * Catalogs or false to leave it at a per Catalog bases.
	 */
	protected void setRestrictIngestPermissions(boolean restrictIngestPermissions) {
		this.restrictIngestPermissions = restrictIngestPermissions;
	}

	/**
	 *
	 */
	protected void setTransactionIdFactory(
			TransactionIdFactory transactionIdFactory) {
		this.transactionIdFactory = transactionIdFactory;
	}
	
	public void addCatalog(String catalogId, Index index) throws CatalogServiceException {
		if (!this.containsCatalog(catalogId)) {
			try {
				this.replaceCatalog(new Catalog(catalogId, index, null, false, false));
			}catch (Exception ignored) {
				
			}
		} else {
			LOG.log(Level.WARNING, "Attempt to override an existing catalog '" + catalogId + "' already used in CatalogService, remedy and retry add -- no changes took place!");
		}
	}
	
	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries) throws CatalogServiceException {
		if (!this.containsCatalog(catalogId)) {
			try {
				this.replaceCatalog(new Catalog(catalogId, index, dictionaries, false, false));
			}catch (Exception ignored) {
				
			}
		} else {
			LOG.log(Level.WARNING, "Attempt to override an existing catalog '" + catalogId + "' already used in CatalogService, remedy and retry add -- no changes took place!");
		}
	}

	public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries, boolean restrictQueryPermission, boolean restrictIngestPermission) throws CatalogServiceException {
		if (!this.containsCatalog(catalogId)) {
			try {
				this.replaceCatalog(new Catalog(catalogId, index, dictionaries, restrictQueryPermission, restrictIngestPermission));
			}catch (Exception ignored) {
				
			}
		} else {
			LOG.log(Level.WARNING, "Attempt to override an existing catalog '" + catalogId + "' already used in CatalogService, remedy and retry add -- no changes took place!");
		}
	}

	public void addDictionary(String catalogId, Dictionary dictionary) throws CatalogServiceException {
		if (this.containsCatalog(catalogId)) {
			Set<Catalog> backupCatalogs = null;
			this.catalogsLock.writeLock().lock();
			try {
				backupCatalogs = new HashSet<Catalog>(this.catalogs);
				for (Catalog catalog : this.catalogs) {
					if (catalog.getId().equals(catalogId)) {
						catalog.addDictionary(dictionary);
						this.catalogRepository.serializeCatalog(catalog);
						break;
					}
				}
			}catch (Exception e) {
				this.catalogs = backupCatalogs;
				throw new CatalogServiceException("Failed to serialize Catalog '" + catalogId + "' -- if CatalogService goes down, Catalog will have to be readded : " + e.getMessage(), e);
			}finally {
				this.catalogsLock.writeLock().unlock();
			}
		} else {
			LOG.log(Level.WARNING, "Attempt to change an existing catalog '" + catalogId + "' already used in CatalogService, remedy and retry add -- no changes took place!");
		}
	}
	
	public void replaceDictionaries(String catalogId, List<Dictionary> dictionaries) throws CatalogServiceException {
		this.modifyCatalog(catalogId, dictionaries, null, null, null);
	}

	public void replaceIndex(String catalogId, Index index) throws CatalogServiceException {
		this.modifyCatalog(catalogId, null, index, null, null);
	}

	public void modifyIngestPermission(String catalogId, boolean restrictIngestPermission) throws CatalogServiceException {
		this.modifyCatalog(catalogId, null, null, null, restrictIngestPermission);
	}
	
	public void modifyQueryPermission(String catalogId, boolean restrictQueryPermission) throws CatalogServiceException {
		this.modifyCatalog(catalogId, null, null, restrictQueryPermission, null);
	}
	
	protected void modifyCatalog(String catalogId, List<Dictionary> dictionaries, Index index, Boolean restrictQueryPermission, Boolean restrictIngestPermission) throws CatalogServiceException {
		if (this.containsCatalog(catalogId)) {
			Set<Catalog> backupCatalogs = null;
			this.catalogsLock.writeLock().lock();
			try {
				backupCatalogs = new HashSet<Catalog>(this.catalogs);
				for (Catalog catalog : this.catalogs) {
					if (catalog.getId().equals(catalogId)) {
						if (dictionaries != null)
							catalog.setDictionaries(dictionaries);
						if (index != null)
							catalog.setIndex(index);
						if (restrictQueryPermission != null)
							catalog.setRestrictQueryPermissions(restrictQueryPermissions);
						if (restrictIngestPermission != null)
							catalog.setRestrictIngestPermissions(restrictIngestPermissions);
						this.catalogRepository.serializeCatalog(catalog);
						break;
					}
				}
			}catch (Exception e) {
				this.catalogs = backupCatalogs;
				throw new CatalogServiceException("Failed to serialize Catalog '" + catalogId + "' -- if CatalogService goes down, Catalog will have to be readded : " + e.getMessage(), e);
			}finally {
				this.catalogsLock.writeLock().unlock();
			}
		}
	}

	protected boolean containsCatalog(String catalogId) throws CatalogServiceException {
		this.catalogsLock.readLock().lock();
		try {
			return this.catalogs.contains(catalogId);
		}catch (Exception e) {
			throw new CatalogServiceException("Failed to check if catalog '" + catalogId + "' has already been added to this CatalogService : " + e.getMessage(), e);
		}finally {
			this.catalogsLock.readLock().unlock();
		}
	}
	
	/**
	 * Ability to dynamically add a Catalog to this CatalogService for managing
	 * @param catalog Catalog for this CatalogService to manage
	 * @return True if catalogs where added to list
	 * @throws CatalogServiceException If one of the adding Catalog
	 * URNs equals that of an existing Catalog. 
	 */	
	public void addCatalog(Catalog catalog) throws CatalogServiceException {
		if (!this.containsCatalog(catalog.getId()))
			this.replaceCatalog(catalog);
		else
			LOG.log(Level.WARNING, "Attempt to override an existing catalog '" + catalog + "' already used in CatalogService, remedy and retry add -- no changes took place!");
	}
	
	/**
	 * Ability to dynamically add a Catalog to this CatalogService for managing
	 * @param catalog Catalog for this CatalogService to manage
	 * @throws CatalogServiceException When allowOverride=false and one of the adding Catalog
	 * URNs equals that of an existing Catalog. 
	 */
	public void replaceCatalog(Catalog catalog) throws CatalogServiceException {
		Set<Catalog> backupCatalogs = null;
		this.catalogsLock.writeLock().lock();
		try {
			backupCatalogs = new HashSet<Catalog>(this.catalogs);
			this.catalogs.remove(catalog);
			this.catalogs.add(catalog);
			this.catalogRepository.serializeCatalog(catalog);
		}catch (Exception e) {
			this.catalogs = backupCatalogs;
			throw new CatalogServiceException("Failed to serialize Catalog '" + catalog + "' -- if CatalogService goes down, Catalog will have to be readded : " + e.getMessage(), e);
		}finally {
			this.catalogsLock.writeLock().unlock();
		}
	}
	
	public void removeCatalog(String catalogUrn) throws CatalogServiceException {
		this.removeCatalog(catalogUrn, false);
	}
	
	/**
	 * 
	 * @throws CatalogServiceException
	 */
	public void removeCatalog(String catalogId, boolean preserveMapping) throws CatalogServiceException {
			this.catalogsLock.readLock().lock();
			Catalog rmCatalog = null;
			try {
				for (Catalog catalog : this.catalogs) {
					if (catalog.getId().equals(catalogId)) {
						rmCatalog = catalog;
						break;
					}
				}
			}catch (Exception e) {
				throw new CatalogServiceException("Failed to find catalog object for catalog URN '" + catalogId + "' : " + e.getMessage(), e);
			}finally {
				this.catalogsLock.readLock().unlock();
			}
			
			if (rmCatalog != null) {
				this.catalogsLock.writeLock().lock();
				try {
					LOG.log(Level.INFO, "Removing catalog '" + rmCatalog + "'");
					this.catalogs.remove(rmCatalog);
					this.catalogRepository.deleteSerializedCatalog(catalogId);
					if (!preserveMapping) {
						this.ingestMapperLock.writeLock().lock();
						try {
							LOG.log(Level.INFO, "Deleting all index mappings for catalog '" + rmCatalog + "'");
							this.ingestMapper.deleteAllMappingsForCatalog(catalogId);
						} finally {
							this.ingestMapperLock.writeLock().unlock();
						}
					}
				}catch (Exception e) {
					throw new CatalogServiceException("Failed to remove Catalog '" + catalogId + "' from this CatalogService");
				}finally {
					this.catalogsLock.writeLock().unlock();
				}
			}else {
				LOG.log(Level.WARNING, "Catalog '" + catalogId + "' is not currently managed by this CatalogService");
			}
	}
	
	public void setPluginStorageDir(File pluginStorageDir) {
		this.pluginStorageDir = pluginStorageDir;
		this.pluginStorageDir.mkdirs();
	}
	
	public URL getPluginStorageDir() throws CatalogServiceException {
		try {
			return new URL("file://" + this.pluginStorageDir.getAbsolutePath());
		}catch (Exception e) {
			throw new CatalogServiceException("Failed to get plugin storage dir directory : " + e.getMessage(), e);
		}
	}
	
	public List<PluginURL> getPluginUrls() throws CatalogServiceException {
		try {
			return this.catalogRepository.deserializePluginURLs();
		}catch (Exception e) {
			throw new CatalogServiceException(e.getMessage(), e);
		}
	}
	
	public void addPluginUrls(List<PluginURL> urls) throws CatalogServiceException {
		try {
			List<PluginURL> currentUrls = new Vector<PluginURL>(this.catalogRepository.deserializePluginURLs());
			currentUrls.addAll(urls);
			this.catalogRepository.serializePluginURLs(currentUrls);
		}catch (Exception e) {
			throw new CatalogServiceException(e.getMessage(), e);
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws CatalogServiceException
	 */
	public Set<Catalog> getCurrentCatalogList() throws CatalogServiceException {
		this.catalogsLock.readLock().lock();
		try {
			return new HashSet<Catalog>(this.catalogs);
		}catch (Exception e) {
			throw new CatalogServiceException("Failed to get current catalog list : " + e.getMessage(), e);
		}finally {
			this.catalogsLock.readLock().unlock();
		}
	}
	
	protected Catalog getCatalog(String catalogUrn) throws CatalogServiceException {
		this.catalogsLock.readLock().lock();
		try {
			for (Catalog catalog : this.catalogs)
				if (catalog.getId().equals(catalogUrn))
					return catalog;
			return null;
		}catch (Exception e) {
			throw new CatalogServiceException("Failed to get catalog catalog '" +  catalogUrn + "' : " + e.getMessage(), e);
		}finally {
			this.catalogsLock.readLock().unlock();
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws CatalogServiceException
	 */
	public Set<String> getCurrentCatalogIds() throws CatalogServiceException {
		this.catalogsLock.readLock().lock();
		try {
			Set<String> catalogIds = new HashSet<String>();
			for (Catalog catalog : this.catalogs) 
				catalogIds.add(catalog.getId());
			return catalogIds;
		}catch (Exception e) {
			throw new CatalogServiceException("Failed to get current catalog ids list : " + e.getMessage(), e);
		}finally {
			this.catalogsLock.readLock().unlock();
		}
	}
		
	public TransactionReceipt ingest(Metadata metadata) throws CatalogServiceException {
		if (this.restrictIngestPermissions) 
			throw new CatalogServiceException("Ingest permissions are restricted for this CatalogService -- request denied");
		try {	
			boolean performUpdate;
			TransactionId<?> catalogServiceTransactionId = this.getCatalogServiceTransactionId(metadata);
			if (performUpdate = this.ingestMapper.hasCatalogServiceTransactionId(catalogServiceTransactionId)) 
				LOG.log(Level.INFO, "TransactionId '" + catalogServiceTransactionId + "' is an existing TransactionId, switching to update mode");
			List<CatalogReceipt> catalogReceipts = new Vector<CatalogReceipt>();
			for (Catalog catalog : this.getFilteredCatalogList(metadata)) {			
				if (catalog.isIngestable()) {
					this.ingestMapperLock.writeLock().lock();
					try {
						// perform update
						if (performUpdate) {
							if (!Boolean.parseBoolean(metadata.getMetadata(ENABLE_UPDATE_MET_KEY)))
								throw new CatalogServiceException("TransactionId '" + catalogServiceTransactionId + "' already exists -- enable update by setting metadata key '" + ENABLE_UPDATE_MET_KEY + "'=true");
							TransactionId<?> catalogTransactionId = this.ingestMapper.getCatalogTransactionId(catalogServiceTransactionId, catalog.getId());
							if (catalogTransactionId != null) {
								CatalogReceipt catalogReceipt = catalog.update(catalogTransactionId, metadata);
								if (catalogReceipt != null) {
									if (!catalogReceipt.getTransactionId().equals(catalogTransactionId)) {
										this.ingestMapper.deleteTransactionIdMapping(catalogTransactionId, catalog.getId());
										this.ingestMapper.storeTransactionIdMapping(catalogServiceTransactionId, this.transactionIdFactory, catalogReceipt, catalog.getTransactionIdFactory());
									}
									catalogReceipts.add(catalogReceipt);
									LOG.log(Level.INFO, "Successfully updated metadata to catalog '" + catalog + "' for TransactionId '" + catalogServiceTransactionId + "'");
								}else {
									LOG.log(Level.SEVERE, "Update attempt to catalog '" + catalog + "' failed for TransactionId '" + catalogServiceTransactionId + "' -- update returned false");
								}
							}else {
								LOG.log(Level.INFO, "Catalog '" + catalog + "' was not on ingest list for TransactionId '" + catalogServiceTransactionId + "' -- skipping");
							}
						// perform ingest	
						}else {
							LOG.log(Level.INFO, "Performing ingest for TransactionId '" + catalogServiceTransactionId + "' to catalog '" + catalog + "'");
							CatalogReceipt catalogReceipt = catalog.ingest(metadata);
							if (catalogReceipt != null) {
								LOG.log(Level.INFO, "Successfully ingested metadata -- Indexing TransactionId information for ingest (CatalogService TransactionId = '" + catalogServiceTransactionId + "', Catalog TransactionId = '" + catalogReceipt.getTransactionId() + "', catalog = '" + catalogReceipt.getCatalogId() + "')");
								this.ingestMapper.storeTransactionIdMapping(catalogServiceTransactionId, this.transactionIdFactory, catalogReceipt, catalog.getTransactionIdFactory());
								catalogReceipts.add(catalogReceipt);
							}else {
								LOG.log(Level.WARNING, "Catalog '" + catalog + "' not interested in any Metadata for TransactionId '" + catalogServiceTransactionId + "'");
							}
						}
					}catch (Exception e) {
						LOG.log(Level.WARNING, "Failed to add metadata to catalog '" + catalog.getId() + "' : " + e.getMessage(), e);
						if (this.oneCatalogFailsAllFail)
							throw new CatalogServiceException("Failed to add metadata to catalog '" + catalog.getId() + "' : " + e.getMessage(), e);
					}finally {
						this.ingestMapperLock.writeLock().unlock();
					}
				}else {
					LOG.log(Level.WARNING, "Ingest not permitted to catalog '" + catalog + "' -- skipping over catalog");
				}
			}
			return (catalogReceipts.size() > 0) ? new TransactionReceipt(catalogServiceTransactionId, catalogReceipts) : null;
		}catch (Exception e) {
			throw new CatalogServiceException("Error occured during Metadata ingest attempt : " + e.getMessage(), e);
		}
	}
	
	/**
	 * 
	 * @param metadata
	 * @throws CatalogServiceException
	 */
	public void delete(Metadata metadata) throws CatalogServiceException {
		if (this.restrictIngestPermissions)
			throw new CatalogServiceException("Delete permissions are restricted for this CatalogService -- request denied");
		TransactionId<?> catalogServiceTransactionId = this.getCatalogServiceTransactionId(metadata, false);
		if (catalogServiceTransactionId != null) {
			for (Catalog catalog : this.getFilteredCatalogList(metadata)) {
				if (catalog.isIngestable()) {
					this.ingestMapperLock.writeLock().lock();
					try {
						TransactionId<?> catalogTransactionId = this.ingestMapper.getCatalogTransactionId(catalogServiceTransactionId, catalog.getId());
						if (catalogTransactionId != null) {
							if (this.doReduce(metadata)) {
								LOG.log(Level.INFO, "Deleting metadata from TransactionId '" + catalogServiceTransactionId + "' for catalog '" + catalog + "'");
								if (catalog.reduce(catalogTransactionId, metadata)) {
									LOG.log(Level.INFO, "Successfully deleted metadata from catalog '" + catalog + "' for TransactionId [id = " + catalogServiceTransactionId + "]");
								}else {
									LOG.log(Level.INFO, "Failed to deleted metadata from catalog '" + catalog + "' for TransactionId [id = " + catalogServiceTransactionId + "] -- delete returned false");
								}
							}else {
								LOG.log(Level.INFO, "Deleting all records of TransactionId from catalog '" + catalog + "'");
								if (catalog.delete(catalogTransactionId)) {
									this.ingestMapper.deleteTransactionIdMapping(catalogTransactionId, catalog.getId());
									LOG.log(Level.INFO, "Successfully deleted metadata from catalog '" + catalog + "' for TransactionId [id = " + catalogServiceTransactionId + "]");
								}else {
									LOG.log(Level.INFO, "Failed to deleted metadata from catalog '" + catalog + "' for TransactionId [id = " + catalogServiceTransactionId + "] -- delete returned false");
								}
							}
						}else {
							LOG.log(Level.INFO, "Catalog '" + catalog + "' was not on delete list for TransactionId '" + catalogServiceTransactionId + "' -- skipping");
						}
					}catch (Exception e) {
						LOG.log(Level.WARNING, "Error occured while deleting metadata for TransactionId [id = " + catalogServiceTransactionId + "] : " + e.getMessage(), e);
						if (this.oneCatalogFailsAllFail)
							throw new CatalogServiceException("Error occured while deleting metadata for TransactionId [id = " + catalogServiceTransactionId + "] : " + e.getMessage(), e);
					}finally {
						this.ingestMapperLock.writeLock().unlock();
					}
				}else {
					LOG.log(Level.WARNING, "Deletion is not permitted to catalog '" + catalog + "' -- skipping over catalog");
				}	
			}
		}else {
			throw new CatalogServiceException("Must specify a TransactionId to delete");
		}
	}
	
	protected boolean doReduce(Metadata metadata) {
		for (String key : metadata.getAllKeys())
			if (!(key.equals(CATALOG_SERVICE_TRANSACTION_ID_MET_KEY) || key.equals(CATALOG_IDS_MET_KEY) || key.equals(CATALOG_TRANSACTION_ID_MET_KEY) || key.equals(CATALOG_ID_MET_KEY)))
				return true;
		return false;
	}
	
	public List<String> getProperty(String key) throws CatalogServiceException {
		List<String> vals = new Vector<String>();
		for (Catalog catalog : this.getCurrentCatalogList()) {
			try {
				String val = catalog.getProperty(key);
				if (val != null)
					vals.add(val);
			}catch (Exception e) {
				if (this.oneCatalogFailsAllFail)
					throw new CatalogServiceException("Failed to get catalog property '" + key + "' from catalog '" + catalog.getId() + "' : " + e.getMessage(), e);
				else
					LOG.log(Level.WARNING, "Failed to get catalog property '" + key + "' from catalog '" + catalog.getId() + "' : " + e.getMessage(), e);
			}
		}
		return vals;
	}

	public Properties getCalalogProperties() throws CatalogServiceException {
		Properties properties = new Properties();
		for (Catalog catalog : this.getCurrentCatalogList()) {
			try {
				Properties catalogProperties = catalog.getProperties();
				for (Object key : catalogProperties.keySet()) {
					String value = properties.getProperty((String) key);
					if (value != null)
						value += "," + catalogProperties.getProperty((String) key);
					else 
						value = catalogProperties.getProperty((String) key);
					properties.setProperty((String) key, value);
				}
			}catch (Exception e) {
				if (this.oneCatalogFailsAllFail)
					throw new CatalogServiceException("Failed to get catalog properties from catalog '" + catalog.getId() + "' : " + e.getMessage(), e);
				else
					LOG.log(Level.WARNING, "Failed to get catalog properties from catalog '" + catalog.getId() + "' : " + e.getMessage(), e);
			}
		}
		return properties;
	}
	
	public Properties getCalalogProperties(String catalogUrn) throws CatalogServiceException {
		try {
			Catalog catalog = this.getCatalog(catalogUrn);
			if (catalog != null)
				return catalog.getProperties();
			else 
				return null;
		}catch (Exception e) {
			throw new CatalogServiceException("Failed to get catalog properties from catalog '" + catalogUrn + "' : " + e.getMessage(), e);
		}
	}
	
	public Page getNextPage(Page page) throws CatalogServiceException {
		QueryPager queryPager = new QueryPager(this._query(page.getQueryExpression(), page.getRestrictToCatalogIds()));
		queryPager.setPageInfo(new PageInfo(page.getPageSize(), page.getPageNum() + 1));
		return this.getPage(page.getQueryExpression(), page.getRestrictToCatalogIds(), queryPager);
	}
	
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression) throws CatalogServiceException {
		return this.getPage(pageInfo, queryExpression, this.getCurrentCatalogIds());
	}
	
	public Page getPage(PageInfo pageInfo, QueryExpression queryExpression, Set<String> catalogIds) throws CatalogServiceException {
		if (this.disableIntersectingCrossCatalogQueries) {
			try {
				int totalResults = 0;
				LinkedHashMap<String, Integer> catalogToSizeOfMap = new LinkedHashMap<String, Integer>();
				for (String catalogId : catalogIds) {
					Catalog catalog = this.getCatalog(catalogId);
					QueryExpression qe = this.reduceToUnderstoodExpressions(catalog, queryExpression);
					if (qe != null) {
						int catalogResultSize = catalog.sizeOf(qe);
						totalResults += catalogResultSize;
						catalogToSizeOfMap.put(catalogId, catalogResultSize);
					}
				}
				
				LOG.log(Level.INFO, "Routing query to catalogs as non-cross catalog intersecting queries . . .");
				if (totalResults <= this.crossCatalogResultSortingThreshold) {
					List<CatalogReceipt> catalogReceipts = new Vector<CatalogReceipt>();
					for (String catalogId : catalogToSizeOfMap.keySet()) {
						Catalog catalog = this.getCatalog(catalogId);
						QueryExpression qe = this.reduceToUnderstoodExpressions(catalog, queryExpression);
						if (qe != null)
							catalogReceipts.addAll(catalog.query(qe));
					}
					List<TransactionReceipt> transactionReceipts = this.getPossiblyUnindexedTransactionReceipts(catalogReceipts);
					LOG.log(Level.INFO, "Sorting Query Results . . . ");
					Collections.sort(transactionReceipts, new Comparator<TransactionReceipt>() {
						public int compare(TransactionReceipt o1,
								TransactionReceipt o2) {
							return o2.getTransactionDate().compareTo(o1.getTransactionDate());
						}
					});
					QueryPager queryPager = new QueryPager(transactionReceipts);
					queryPager.setPageInfo(pageInfo);
					return this.getPage(queryExpression, catalogIds, queryPager);
				}else {
					int currentIndex = 0;
					int desiredStartingIndex = pageInfo.getPageNum() * pageInfo.getPageSize();
					List<CatalogReceipt> pageOfReceipts = new Vector<CatalogReceipt>();
					for (Entry<String, Integer> entry : catalogToSizeOfMap.entrySet()) {
						if (desiredStartingIndex - currentIndex <= entry.getValue()) {
							Catalog catalog = this.getCatalog(entry.getKey());
							QueryExpression qe = this.reduceToUnderstoodExpressions(catalog, queryExpression);
							if (qe != null) {
								List<CatalogReceipt> receipts = catalog.query(qe, desiredStartingIndex - currentIndex, Math.min((desiredStartingIndex - currentIndex) + pageInfo.getPageSize(), entry.getValue()));
								pageOfReceipts.addAll(receipts);
								if (pageOfReceipts.size() >= pageInfo.getPageSize())
									break;
							}
						}else {
							currentIndex += entry.getValue();
						}
					}
					return new Page(new ProcessedPageInfo(pageInfo.getPageSize(), pageInfo.getPageNum(), totalResults), queryExpression, catalogIds, this.indexReceipts(this.getPossiblyUnindexedTransactionReceipts(pageOfReceipts)));
				}
			}catch (Exception e) {
				throw new CatalogServiceException(e.getMessage(), e);
			}
		}else {
			QueryPager queryPager = new QueryPager(this._query(queryExpression, catalogIds)); 
			queryPager.setPageInfo(pageInfo);
			return this.getPage(queryExpression, catalogIds, queryPager);
		}
	}
	
	public QueryPager query(QueryExpression queryExpression) throws CatalogServiceException {
		return this.query(queryExpression, this.getCurrentCatalogIds());
	}

	public List<TransactionalMetadata> getMetadata(Page page) throws CatalogServiceException {
		return this.getMetadata(page.getReceipts());
	}
	
	protected Page getPage(QueryExpression queryExpression, Set<String> restrictToCatalogIds, QueryPager queryPager) throws CatalogServiceException {
		return new Page(new ProcessedPageInfo(queryPager.getPageSize(), queryPager.getPageNum(), queryPager.getNumOfHits()), queryExpression, restrictToCatalogIds, this.indexReceipts(queryPager.getCurrentPage()));
	}
	
	public QueryPager query(QueryExpression queryExpression, Set<String> catalogIds) throws CatalogServiceException {
		return new QueryPager(this.indexReceipts(this._query(queryExpression, catalogIds)));
	}
	
	/**
	 * 
	 * @param queryExpression
	 * @return
	 * @throws CatalogServiceException
	 */
	public List<TransactionReceipt> _query(QueryExpression queryExpression, Set<String> catalogIds) throws CatalogServiceException {
		if (this.restrictQueryPermissions)
			throw new CatalogServiceException("Query permissions are restricted for this CatalogService -- request denied");
		try {
			LOG.log(Level.INFO, "Recieved query '" + queryExpression + "'");
			if (this.simplifyQueries) {
				queryExpression = QueryUtils.simplifyQuery(queryExpression);
				LOG.log(Level.INFO, "Simplified query to '" + queryExpression + "' -- routing query to catalogs");
			}
			QueryResult queryResult = this.queryRecur(queryExpression, catalogIds);
			List<CatalogReceipt> catalogReceipts = new Vector<CatalogReceipt>();
			if (queryResult.getCatalogReceipts() == null && queryResult.getInterestedCatalogs() != null) {
				for (Catalog catalog : this.getCurrentCatalogList()) {
					try {
						if (queryResult.getInterestedCatalogs().contains(catalog.getId())) {
							LOG.log(Level.INFO, "Restricting query to understood terms for Catalog '" + catalog + "'");
							QueryExpression reducedExpression = this.reduceToUnderstoodExpressions(catalog, queryExpression);
							LOG.log(Level.INFO, "Querying Catalog '" + catalog + "' with query '" + reducedExpression + "'");
							catalogReceipts.addAll(catalog.query(reducedExpression));
						}
					}catch (Exception e) {
						if (this.oneCatalogFailsAllFail)
							throw new CatalogServiceException("Failed to query catalog '" + catalog.getId() + "' for query '" + queryExpression + "' : " + e.getMessage(), e);
						else
							LOG.log(Level.WARNING, "Failed to query catalog '" + catalog.getId() + "' for query '" + queryExpression + "' : " + e.getMessage(), e);
					}	
				}
			}
			List<TransactionReceipt> transactionReceipts = this.getPossiblyUnindexedTransactionReceipts(catalogReceipts);
			LOG.log(Level.INFO, "Sorting Query Results . . . ");
			Collections.sort(transactionReceipts, new Comparator<TransactionReceipt>() {
				public int compare(TransactionReceipt o1,
						TransactionReceipt o2) {
					return o2.getTransactionDate().compareTo(o1.getTransactionDate());
				}
			});

			LOG.log(Level.INFO, "Query returned " + transactionReceipts.size() + " results");
			return transactionReceipts;
		}catch (Exception e) {
			e.printStackTrace();
			throw new CatalogServiceException("Failed to get TransactionId to Metadata map for query '" + queryExpression + "' : " + e.getMessage(), e);
		}
	}
	
	protected List<TransactionReceipt> getPossiblyUnindexedTransactionReceipts(List<CatalogReceipt> catalogReceipts) throws CatalogServiceException {
		try {
			List<TransactionReceipt> returnList = new Vector<TransactionReceipt>();
			LinkedHashMap<TransactionId<?>, List<CatalogReceipt>> existing = new LinkedHashMap<TransactionId<?>, List<CatalogReceipt>>();
 			for (CatalogReceipt catalogReceipt : catalogReceipts) {
 				TransactionId<?> catalogServiceTransactionId = this.getCatalogServiceTransactionId(catalogReceipt.getTransactionId(), catalogReceipt.getCatalogId());
 				if (catalogServiceTransactionId != null) {
 					List<CatalogReceipt> found = existing.get(catalogServiceTransactionId);
 					if (found == null) 
 						found = new Vector<CatalogReceipt>();
 					found.add(catalogReceipt);	
 					existing.put(catalogServiceTransactionId, found);
 				}else {
 					returnList.add(new TransactionReceipt(null, Collections.singletonList(catalogReceipt)));
 				}
 			}
 			for (TransactionId<?> transactionId : existing.keySet())
 				returnList.add(new TransactionReceipt(transactionId, existing.get(transactionId)));
 			return returnList;
		}catch (Exception e) {
			throw new CatalogServiceException(e.getMessage(), e);
		}
	}
	
	protected List<TransactionReceipt> indexReceipts(List<TransactionReceipt> transactionReceipts) throws CatalogServiceException {
		List<TransactionReceipt> indexedReceipts = new Vector<TransactionReceipt>();
		for (TransactionReceipt transactionReceipt : transactionReceipts) {
			try {
//				for (CatalogReceipt catalogReceipt : transactionReceipt.getCatalogReceipts()) {
					if (transactionReceipt.getTransactionId() == null)
						transactionReceipt = new TransactionReceipt(this.getCatalogServiceTransactionId(transactionReceipt.getCatalogReceipts().get(0), true), transactionReceipt.getCatalogReceipts());
//				}
				indexedReceipts.add(transactionReceipt);
			}catch(Exception e) {
				throw new CatalogServiceException(e.getMessage(), e);
			}
		}
		return indexedReceipts;
	}
 
	public List<TransactionalMetadata> getNextPage(QueryPager queryPager) throws CatalogServiceException {
		try {
			return this.getMetadata(queryPager.getCurrentPage());
		}catch (Exception e) {
			throw new CatalogServiceException("Failed to get next page of Metadata : " + e.getMessage(), e);
		}
	}
	
	public List<TransactionalMetadata> getAllPages(QueryPager queryPager) throws CatalogServiceException {
		try {
			return this.getMetadata(queryPager.getTransactionReceipts());
		}catch (Exception e) {
			throw new CatalogServiceException("Failed to get all page of Metadata : " + e.getMessage(), e);
		}	
	}
	
	public List<TransactionalMetadata> getMetadataFromTransactionIdStrings(List<String> catalogServiceTransactionIdStrings) throws CatalogServiceException {
		List<TransactionId<?>> catalogServiceTransactionIds = new Vector<TransactionId<?>>();
		for (String catalogServiceTransactionIdString : catalogServiceTransactionIdStrings) 
			catalogServiceTransactionIds.add(this.generateTransactionId(catalogServiceTransactionIdString));
		return this.getMetadataFromTransactionIds(catalogServiceTransactionIds);
	}
	
	public List<TransactionalMetadata> getMetadata(List<TransactionReceipt> transactionReceipts) throws CatalogServiceException {
		LinkedHashSet<TransactionalMetadata> metadataSet = new LinkedHashSet<TransactionalMetadata>();
		for (TransactionReceipt transactionReceipt : transactionReceipts) {
			Metadata metadata = new Metadata();
			Vector<CatalogReceipt> successfulCatalogReceipts = new Vector<CatalogReceipt>();
			for (CatalogReceipt catalogReceipt : transactionReceipt.getCatalogReceipts()) {
				try {
					Catalog catalog = this.getCatalog(catalogReceipt.getCatalogId());
					metadata.addMetadata(catalog.getMetadata(catalogReceipt.getTransactionId()));
					successfulCatalogReceipts.add(catalogReceipt);
				}catch (Exception e) {
					if (this.oneCatalogFailsAllFail)
						throw new CatalogServiceException("Failed to get metadata for transaction ids for catalog '" + catalogReceipt.getCatalogId() + "' : " + e.getMessage(), e);
					else
						LOG.log(Level.WARNING, "Failed to get metadata for transaction ids for catalog '" + catalogReceipt.getCatalogId() + "' : " + e.getMessage(), e);
				}
			}
			if (metadata.getHashtable().keySet().size() > 0)
				metadataSet.add(new TransactionalMetadata(new TransactionReceipt(transactionReceipt.getTransactionId(), successfulCatalogReceipts), metadata));
		}
		return new Vector<TransactionalMetadata>(metadataSet);
	}
	
	public List<TransactionalMetadata> getMetadataFromTransactionIds(List<TransactionId<?>> catalogServiceTransactionIds) throws CatalogServiceException {
		LinkedHashSet<TransactionalMetadata> metadataSet = new LinkedHashSet<TransactionalMetadata>();
		for (TransactionId<?> catalogServiceTransactionId : catalogServiceTransactionIds) {
			Metadata metadata = new Metadata();
			Vector<CatalogReceipt> catalogReceipts = new Vector<CatalogReceipt>();
			for (Catalog catalog : this.getCurrentCatalogList()) {
				try {
					CatalogReceipt catalogReceipt = this.ingestMapper.getCatalogReceipt(catalogServiceTransactionId, catalog.getId());
					if (catalogReceipt != null) {
						metadata.addMetadata(catalog.getMetadata(catalogReceipt.getTransactionId()).getHashtable());
						catalogReceipts.add(catalogReceipt);
					}
				}catch (Exception e) {
					if (this.oneCatalogFailsAllFail)
						throw new CatalogServiceException("Failed to get metadata for transaction ids for catalog '" + catalog.getId() + "' : " + e.getMessage(), e);
					else
						LOG.log(Level.WARNING, "Failed to get metadata for transaction ids for catalog '" + catalog.getId() + "' : " + e.getMessage(), e);
				}
			}
			if (metadata.getHashtable().keySet().size() > 0)
				metadataSet.add(new TransactionalMetadata(new TransactionReceipt(catalogServiceTransactionId, catalogReceipts), metadata));
		}
		return new Vector<TransactionalMetadata>(metadataSet);
	}
	
	public List<TransactionId<?>> getCatalogServiceTransactionIds(List<TransactionId<?>> catalogTransactionIds, String catalogUrn) throws CatalogServiceException {
		LinkedHashSet<TransactionId<?>> catalogServiceTransactionIds = new LinkedHashSet<TransactionId<?>>();
		for (TransactionId<?> catalogTransactionId : catalogTransactionIds) {
			TransactionId<?> catalogServiceTransactionId = this.getCatalogServiceTransactionId(catalogTransactionId, catalogUrn);
			catalogServiceTransactionIds.add(catalogServiceTransactionId);
		}
		return new Vector<TransactionId<?>>(catalogServiceTransactionIds); 
	}
	
	public TransactionId<?> getCatalogServiceTransactionId(TransactionId<?> catalogTransactionId, String catalogUrn) throws CatalogServiceException {
		this.ingestMapperLock.readLock().lock();
		try {
			return this.ingestMapper.getCatalogServiceTransactionId(catalogTransactionId, catalogUrn);
		}catch (Exception e) {
			throw new CatalogServiceException(e.getMessage(), e);
		}finally {
			this.ingestMapperLock.readLock().unlock();
		}
	}
	
	public TransactionId<?> getCatalogServiceTransactionId(CatalogReceipt catalogReceipt, boolean generateNew) throws CatalogServiceException {
		try {
			TransactionId<?> catalogServiceTransactionId = this.getCatalogServiceTransactionId(catalogReceipt.getTransactionId(), catalogReceipt.getCatalogId());
			if (catalogServiceTransactionId == null && generateNew) {
				catalogServiceTransactionId = this.generateNewUniqueTransactionId();
				LOG.log(Level.INFO, "CatalogServer mapping transaction: " + catalogServiceTransactionId + "," + catalogReceipt.getTransactionId() + "," + catalogReceipt.getCatalogId());
				this.ingestMapperLock.writeLock().lock();
				try {
					this.ingestMapper.storeTransactionIdMapping(catalogServiceTransactionId, this.transactionIdFactory, catalogReceipt, this.getCatalog(catalogReceipt.getCatalogId()).getTransactionIdFactory());
				}catch (Exception e) {
					throw new CatalogServiceException("Failed to write TransactionId '" + catalogServiceTransactionId + "' : " + e.getMessage(), e);
				}finally {
					this.ingestMapperLock.writeLock().unlock();
				}
			}
			return catalogServiceTransactionId;
		}catch (Exception e) {
			throw new CatalogServiceException("Failed to get CatalogServiceTransactionId : " + e.getMessage(), e);
		}
	}

	protected TransactionId<?> generateNewUniqueTransactionId() {
		try {
			return this.transactionIdFactory.createNewTransactionId();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to generate a new TransactionId from factory '" + this.transactionIdFactory.getClass().getCanonicalName() + "' : " + e.getMessage(), e);
			return null;
		}
	}
	
	protected TransactionId<?> generateTransactionId(String stringTransactionId) {
		try {
			return this.transactionIdFactory.createTransactionId(stringTransactionId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to generate a new TransactionId from factory '" + this.transactionIdFactory.getClass().getCanonicalName() + "' for string value '" + stringTransactionId + ": " + e.getMessage(), e);
			return null;
		}
	}
	
	// check if transaction id was specified by user, otherwise generate a new one
	protected TransactionId<?> getCatalogServiceTransactionId(Metadata metadata) throws Exception {
		return this.getCatalogServiceTransactionId(metadata, true);
	}
	
	protected TransactionId<?> getCatalogServiceTransactionId(Metadata metadata, boolean generateNew) throws CatalogServiceException {
		try {
			if (metadata.getMetadata(CatalogServiceLocal.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY) != null) {
				return this.generateTransactionId(metadata.getMetadata(CatalogServiceLocal.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY));
			}else if (metadata.getMetadata(CatalogServiceLocal.CATALOG_TRANSACTION_ID_MET_KEY) != null && metadata.getMetadata(CatalogServiceLocal.CATALOG_ID_MET_KEY) != null) {
				String catalogId = metadata.getMetadata(CatalogServiceLocal.CATALOG_ID_MET_KEY);
				Catalog catalog = this.getCatalog(catalogId);
				if (catalog != null) {
					TransactionId<?> catalogTransactionId = catalog.getTransactionIdFromString(metadata.getMetadata(CatalogServiceLocal.CATALOG_TRANSACTION_ID_MET_KEY));
					TransactionId<?> catalogServiceTransactionId = this.ingestMapper.getCatalogServiceTransactionId(catalogTransactionId, catalogId);
					if (catalogServiceTransactionId == null)
						throw new CatalogServiceException("CatalogService's Catalog '" + catalog.getId() + "' is not aware of TransactionId '" + catalogTransactionId + "'s");
					return catalogServiceTransactionId;
				}else {
					throw new CatalogServiceException("This CatalogService has no Catalog with ID = '" + catalogId + "'");
				}
			}else if (generateNew) {
				return this.generateNewUniqueTransactionId();
			}else {
				throw new CatalogServiceException("Metadata fields not present to determine a TransactionId");
			}
		}catch (Exception e) {
			throw new CatalogServiceException("Failed determine TransactionId : " + e.getMessage(), e);
		}
	}
	
	protected Set<Catalog> getFilteredCatalogList(Metadata metadata) throws CatalogServiceException {
		try {
			if (metadata.containsKey(CATALOG_ID_MET_KEY)) {
				Catalog catalog = this.getCatalog(metadata.getMetadata(CATALOG_ID_MET_KEY));
				if (catalog == null)
					throw new CatalogServiceException("Catalog '" + metadata.getMetadata(CATALOG_ID_MET_KEY) + "' is not managed by this CatalogService");
				else
					return Collections.singleton(catalog);
			}else if (metadata.containsKey(CATALOG_IDS_MET_KEY)) {
				HashSet<Catalog> filteredCatalogList = new HashSet<Catalog>();
				for (Object catalogUrn : metadata.getAllMetadata(CATALOG_IDS_MET_KEY)) {
					Catalog catalog = this.getCatalog((String) catalogUrn);
					if (catalog == null)
						throw new CatalogServiceException("Catalog '" + metadata.getMetadata(CATALOG_ID_MET_KEY) + "' is not managed by this CatalogService");
					else
						filteredCatalogList.add(catalog);
				}
				return filteredCatalogList;
			}else {
				return new HashSet<Catalog>(this.catalogs);
			}
		}catch (Exception e) {
			throw new CatalogServiceException("Failed to get filtered catalog list : " + e.getMessage(), e);
		}
	}
	
	protected QueryResult queryRecur(QueryExpression queryExpression, Set<String> restrictToCatalogIds) throws CatalogServiceException, CatalogException {
		// get QueryResults for sub queries
		if (queryExpression instanceof QueryLogicalGroup) {
			
			// get children query results
			List<QueryResult> childrenQueryResults = new Vector<QueryResult>();
			for (QueryExpression subQueryExpression : ((QueryLogicalGroup) queryExpression).getExpressions()) 
				childrenQueryResults.add(queryRecur(subQueryExpression, restrictToCatalogIds));
			
			// if (QueryLogicalGroup's operator is AND and is unbalanced or a child contains query results)
			if ((((QueryLogicalGroup) queryExpression).getOperator().equals(QueryLogicalGroup.Operator.AND) && containsUnbalancedCatalogInterest(childrenQueryResults)) || containsTranactionReceipts(childrenQueryResults)) {

			  for (QueryResult childQueryResult : childrenQueryResults) {
				// if childQueryResult has not been used, use it
				if (childQueryResult.getCatalogReceipts() == null) {
				  List<CatalogReceipt> catalogReceipts = new Vector<CatalogReceipt>();
				  for (Catalog catalog : this.getCurrentCatalogList()) {
					try {
					  if (childQueryResult.getInterestedCatalogs().contains(catalog.getId())) {
						catalogReceipts.addAll(catalog
							.query(this.reduceToUnderstoodExpressions(catalog, childQueryResult.getQueryExpression())));
					  }
					} catch (Exception e) {
					  if (this.oneCatalogFailsAllFail) {
						throw new CatalogServiceException(
							"Failed to query catalog '" + catalog.getId() + "' for query '" + queryExpression + "' : "
							+ e.getMessage(), e);
					  } else {
						LOG.log(Level.WARNING,
							"Failed to query catalog '" + catalog.getId() + "' for query '" + queryExpression + "' : "
							+ e.getMessage(), e);
					  }
					}
				  }
				  childQueryResult.setCatalogReceipts(catalogReceipts);
				}

			  }
				
				// get intersection of results
	   			QueryResult queryResult = new QueryResult(queryExpression);
	   			queryResult.setCatalogReceipts(this.getIntersection(childrenQueryResults));
				return queryResult;
			}else {
				// get merge of results
				QueryResult queryResult = new QueryResult(queryExpression);
				HashSet<String> interestedCatalogs = new HashSet<String>();
				for (QueryResult childQueryResult : childrenQueryResults)
					interestedCatalogs.addAll(childQueryResult.getInterestedCatalogs());
				queryResult.setInterestedCatalogs(interestedCatalogs);
				return queryResult;
			}
		}else if (queryExpression instanceof WrapperQueryExpression) {

			// check for catalogs interested in wrapper query expression
			restrictToCatalogIds.retainAll(getInterestedCatalogs(queryExpression, restrictToCatalogIds));
			
			// check for catalogs interested in wrapped query expression
			QueryResult wrapperExprQueryResult;
			QueryExpression wrapperQE = ((WrapperQueryExpression) queryExpression).getQueryExpression();
			if (wrapperQE instanceof QueryLogicalGroup) {
				wrapperExprQueryResult = this.queryRecur((QueryLogicalGroup) wrapperQE, restrictToCatalogIds);
			}else {
				wrapperExprQueryResult = new QueryResult(wrapperQE);
				wrapperExprQueryResult.interestedCatalogs = getInterestedCatalogs(wrapperQE, restrictToCatalogIds);
				wrapperExprQueryResult.interestedCatalogs.retainAll(restrictToCatalogIds);
			}				
			return wrapperExprQueryResult;
		}else {
			// determine catalogs interested in this query expression
			QueryResult queryResult = new QueryResult(queryExpression);
			Set<String> interestedCatalogs = getInterestedCatalogs(queryExpression, restrictToCatalogIds);
			interestedCatalogs.retainAll(restrictToCatalogIds);
			queryResult.setInterestedCatalogs(interestedCatalogs);
			return queryResult;
		}
	}
	
	protected List<CatalogReceipt> getIntersection(List<QueryResult> queryResults) {
		List<CatalogReceipt> catalogReceipts = new Vector<CatalogReceipt>();
		if (queryResults.size() > 0) {
			catalogReceipts.addAll(queryResults.get(0).getCatalogReceipts());
			for (int i = 1; i < queryResults.size(); i++) {
				QueryResult qr = queryResults.get(i);
TR:				for (CatalogReceipt catalogReceipt : qr.getCatalogReceipts()) {
					for (CatalogReceipt compCatalogReceipt : catalogReceipts) {
						if (catalogReceipt.getTransactionId().equals(compCatalogReceipt.getTransactionId()))
							continue TR;
					}
					catalogReceipts.remove(catalogReceipt);
				}
			}
		}
		return catalogReceipts;
	}

	protected QueryExpression reduceToUnderstoodExpressions(Catalog catalog, QueryExpression queryExpression) throws
		CatalogException {
		if (queryExpression instanceof QueryLogicalGroup) {
        	QueryLogicalGroup queryLogicalGroup = (QueryLogicalGroup) queryExpression;
        	List<QueryExpression> restrictedExpressions = new Vector<QueryExpression>();
        	for (QueryExpression qe : queryLogicalGroup.getExpressions()) {
        		QueryExpression restrictedQE = this.reduceToUnderstoodExpressions(catalog, qe);
        		if (restrictedQE == null && queryLogicalGroup.getOperator().equals(QueryLogicalGroup.Operator.AND) && this.disableIntersectingCrossCatalogQueries) {
        			restrictedExpressions.clear();
        			break;
        		}
        		if (restrictedQE != null)
        			restrictedExpressions.add(restrictedQE);
        	}
        	if (restrictedExpressions.size() > 0) {
        		if (restrictedExpressions.size() == 1) {
        			return restrictedExpressions.get(0);
        		}else {
		        	QueryLogicalGroup restrictedQueryLogicalGroup = queryLogicalGroup.clone();
		        	restrictedQueryLogicalGroup.setExpressions(restrictedExpressions);
		        	return restrictedQueryLogicalGroup;
        		}
        	}else {
        		return null;
        	}
		}else if (queryExpression instanceof WrapperQueryExpression) {
			WrapperQueryExpression wrapperQueryExpresssion = (WrapperQueryExpression) queryExpression;
        	if (catalog.isInterested(queryExpression)) {
	        	QueryExpression qe = this.reduceToUnderstoodExpressions(catalog, wrapperQueryExpresssion.getQueryExpression());
				if (qe != null) {
					WrapperQueryExpression wqe = wrapperQueryExpresssion.clone();
					wqe.setQueryExpression(qe);
					return wqe;
				}else if (wrapperQueryExpresssion.isValidWithNoSubExpression()){
					WrapperQueryExpression wqe = wrapperQueryExpresssion.clone();
					wqe.setQueryExpression(null);
					return wqe;
				}else {
					return null;
				}
        	}else {
        		return null;
        	}
        	
        }else if (catalog.isInterested(queryExpression)) {
        	return queryExpression;
    	}else {
    		return null;
    	}
	}
	
	protected boolean containsTranactionReceipts(List<QueryResult> queryResults) {
		for (QueryResult queryResult : queryResults)
			if (queryResult.getCatalogReceipts() != null)
				return true;
		return false;
	}

	protected boolean containsUnbalancedCatalogInterest(List<QueryResult> queryResults) {
		if (queryResults.size() > 0) {
			QueryResult firstQueryResult = queryResults.get(0);
			for (int i = 1; i < queryResults.size(); i++) {
				QueryResult queryResult = queryResults.get(i);
				if (!(queryResult.interestedCatalogs.containsAll(firstQueryResult.interestedCatalogs) && firstQueryResult.interestedCatalogs.containsAll(queryResult.interestedCatalogs)))
					return true;
			}
			return false;
		}else {
			return false;
		}
	}

	protected HashSet<String> getInterestedCatalogs(QueryExpression queryExpression, Set<String> restrictToCatalogIds) throws CatalogException, CatalogServiceException {
		HashSet<String> interestedCatalogs = new HashSet<String>();
		for (Catalog catalog : this.getCurrentCatalogList()) {
			try {
				if (restrictToCatalogIds.contains(catalog.getId())) {
					if (catalog.isInterested(queryExpression))
						interestedCatalogs.add(catalog.getId());
				}
			}catch (Exception e) {
				if (this.oneCatalogFailsAllFail)
					throw new CatalogException("Failed to determine if Catalog '" + catalog.getId() + "' is interested in query expression '" + queryExpression + "' : " + e.getMessage(), e);
				else
					LOG.log(Level.WARNING, "Failed to determine if Catalog '" + catalog.getId() + "' is interested in query expression '" + queryExpression + "' : " + e.getMessage(), e);
			}
		}
		return interestedCatalogs;
	}

	protected class QueryResult {
		
		private QueryExpression queryExpression;
		private List<CatalogReceipt> catalogReceipts;
		private Set<String> interestedCatalogs;
		
		public QueryResult(QueryExpression queryExpression) {
			this.queryExpression = queryExpression;
		}

		public QueryExpression getQueryExpression() {
			return queryExpression;
		}

		public void setQueryExpression(QueryExpression queryExpression) {
			this.queryExpression = queryExpression;
		}

		public List<CatalogReceipt> getCatalogReceipts() {
			return catalogReceipts;
		}

		public void setCatalogReceipts(
				List<CatalogReceipt> catalogReceipts) {
			this.catalogReceipts = catalogReceipts;
		}

		public Set<String> getInterestedCatalogs() {
			return interestedCatalogs;
		}

		public void setInterestedCatalogs(Set<String> interestedCatalogs) {
			this.interestedCatalogs = interestedCatalogs;
		}
	
	}
	
	protected class QueryResultGroup {
		HashSet<TransactionReceipt> transactionReceipts;
		String id;
		
		public QueryResultGroup(String id) {
			this.id = id;
			transactionReceipts = new HashSet<TransactionReceipt>();
		}
		
		public HashSet<TransactionReceipt> getResults() {
			return this.transactionReceipts;
		}
		
		public void addTransactionReceipt(TransactionReceipt transactionReceipt) {
			this.transactionReceipts.add(transactionReceipt);
		}
	}
		
}
