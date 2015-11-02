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
package org.apache.oodt.cas.catalog.repository;


import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.catalog.exception.CatalogRepositoryException;
import org.apache.oodt.cas.catalog.system.Catalog;
import org.apache.oodt.cas.catalog.util.PluginURL;
import org.apache.oodt.cas.catalog.util.Serializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Catalog Repository that serializes Catalogs via XStream utility
 * <p>
 */
public class SerializedCatalogRepository implements CatalogRepository {

	private static Logger LOG = Logger.getLogger(SerializedCatalogRepository.class.getName());
	protected String storageDir;
	
	public SerializedCatalogRepository(String storageDir) throws InstantiationException {
		try {
			this.storageDir = storageDir;
			new File(this.storageDir + "/catalogs").mkdirs();
			new File(this.storageDir + "/classloaders").mkdirs();
		}catch(Exception e) {
			LOG.log(Level.SEVERE, e.getMessage());
		  	LOG.log(Level.SEVERE, e.getMessage());
			throw new InstantiationException("Failed to instantiate SerializedCatalogRepository : " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.catalog.repository.MemoryBasedCatalogRepository#deleteSerializedCatalog(java.lang.String)
	 */
	public void deleteSerializedCatalog(String catalogUrn)
			throws CatalogRepositoryException {
		LOG.log(Level.INFO, "Deleting Catalog: '" + catalogUrn + "' . . . ");
		boolean catalogFileDelete = this.getCatalogFile(catalogUrn).delete();
		if (!catalogFileDelete) {
		  throw new CatalogRepositoryException(
			  "Failed to deserialize catalog '" + catalogUrn + "', delete files returned false");
		} else {
		  LOG.log(Level.INFO, "Successfully deleting Catalog: '" + catalogUrn + "'");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.catalog.repository.MemoryBasedCatalogRepository#deserializeCatalog(java.lang.String)
	 */
	public Catalog deserializeCatalog(String catalogUrn)
			throws CatalogRepositoryException {
		LOG.log(Level.INFO, "Deserializing Catalog: " + catalogUrn);
		FileInputStream catalogIn = null;
		try {
			return new Serializer().deserializeObject(Catalog.class, catalogIn = new FileInputStream(this.getCatalogFile(catalogUrn)));
		}catch (Exception e) {
			throw new CatalogRepositoryException("Failed to Deserialized Catalogs from '" + this.storageDir + "' : " + e.getMessage(), e);
		}finally {
			try {
				catalogIn.close();
			}catch (Exception ignored) {}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.catalog.repository.MemoryBasedCatalogRepository#deserializeAllCatalogs()
	 */
	public Set<Catalog> deserializeAllCatalogs()
			throws CatalogRepositoryException {
		HashSet<Catalog> catalogs = new HashSet<Catalog>();
		for (String catalogFile : new File(this.storageDir + "/catalogs").list()) {
			Catalog catalog = this.deserializeCatalog(catalogFile.split("\\.ser")[0]);
			catalogs.add(catalog);
		}
		return catalogs;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.catalog.repository.MemoryBasedCatalogRepository#isCatalogSerialized(java.lang.String)
	 */
	public boolean isCatalogSerialized(String catalogUrn) {
		return this.getCatalogFile(catalogUrn).exists();
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.catalog.repository.MemoryBasedCatalogRepository#serializeCatalog(org.apache.oodt.cas.catalog.Catalog)
	 */
	public void serializeCatalog(Catalog catalog)
			throws CatalogRepositoryException {
		LOG.log(Level.INFO, "Serializing Catalog: " + catalog.getId());
		FileOutputStream catalogOut = null;
		try {
			//serialize Catalog
			new Serializer().serializeObject(catalog, (catalogOut = new FileOutputStream(this.getCatalogFileWorker(catalog.getId()))));
			if (this.getCatalogFile(catalog.getId()).exists()) {
			  FileUtils.copyFile(this.getCatalogFile(catalog.getId()), this.getCatalogFileBkup(catalog.getId()), true);
			}
			FileUtils.copyFile(this.getCatalogFileWorker(catalog.getId()), this.getCatalogFile(catalog.getId()), true);
			this.getCatalogFileWorker(catalog.getId()).delete();
			this.getCatalogFileBkup(catalog.getId()).delete();
		}catch (Exception e) {
			throw new CatalogRepositoryException("Failed to Serialized Catalogs to '" + this.storageDir + "' : " + e.getMessage(), e);
		}finally {
			try {
				catalogOut.close();
			}catch (Exception ignored) {}
		}	
	}
	
	public void serializePluginURLs(List<PluginURL> urls) 
			throws CatalogRepositoryException {
		FileOutputStream urlsOut = null;
		try {
			//serialize URLs
			new Serializer().serializeObject(urls, (urlsOut = new FileOutputStream(this.getClassLoaderUrlsFileWorker())));
			if (this.getClassLoaderUrlsFile().exists()) {
			  FileUtils.copyFile(this.getClassLoaderUrlsFile(), this.getClassLoaderUrlsFileBkup(), true);
			}
			FileUtils.copyFile(this.getClassLoaderUrlsFileWorker(), this.getClassLoaderUrlsFile(), true);
			this.getClassLoaderUrlsFileWorker().delete();
			this.getClassLoaderUrlsFileBkup().delete();
		}catch (Exception e) {
			throw new CatalogRepositoryException("Failed to Serialized ClassLoader URLs to '" + this.storageDir + "' : " + e.getMessage(), e);
		}finally {
			try {
				urlsOut.close();
			}catch (Exception ignored) {}
		}	
	}

	public List<PluginURL> deserializePluginURLs() throws CatalogRepositoryException {
		FileInputStream urlsIn = null;
		try {
			if (this.getClassLoaderUrlsFile().exists()) {
			  return new Serializer()
				  .deserializeObject(List.class, (urlsIn = new FileInputStream(this.getClassLoaderUrlsFile())));
			} else {
			  return Collections.emptyList();
			}
		}catch (Exception e) {
			throw new CatalogRepositoryException("Failed to Deserialized All ClassLoader URLs from '" + this.storageDir + "' : " + e.getMessage(), e);
		}finally {
			try {
				urlsIn.close();
			}catch (Exception ignored) {}
		}
	}

	public boolean isModifiable() {
		return true;
	}

	protected File getCatalogFile(String catalogUrn) {
		return new File(this.storageDir + "/catalogs/" + catalogUrn + ".ser");
	}

	protected File getCatalogFileBkup(String catalogUrn) {
		return new File(this.storageDir + "/catalogs/" + catalogUrn + ".ser-bkup");
	}
	
	protected File getCatalogFileWorker(String catalogUrn) {
		return new File(this.storageDir + "/catalogs/" + catalogUrn + ".ser-worker");
	}
	
	protected File getClassLoaderUrlsFile() {
		return new File(this.storageDir + "/classloaders/urls.ser");
	}
	
	protected File getClassLoaderUrlsFileBkup() {
		return new File(this.storageDir + "/classloaders/urls.ser-bkup");
	}
	
	protected File getClassLoaderUrlsFileWorker() {
		return new File(this.storageDir + "/classloaders/urls.ser-worker");
	}
	
}
