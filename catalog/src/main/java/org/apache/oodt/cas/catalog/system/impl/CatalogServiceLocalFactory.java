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

//JDK imports
import org.apache.oodt.cas.catalog.mapping.IngestMapperFactory;
import org.apache.oodt.cas.catalog.repository.CatalogRepositoryFactory;
import org.apache.oodt.cas.catalog.struct.TransactionIdFactory;
import org.apache.oodt.cas.catalog.system.CatalogServiceFactory;

import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
//Spring imports

//OODT imports

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Factory class for CatalogServiceLocal
 * <p>
 */
public class CatalogServiceLocalFactory implements CatalogServiceFactory {

  public static final int INT = 200;
  private static Logger LOG = Logger.getLogger(CatalogServiceLocalFactory.class.getName());
	
	protected CatalogRepositoryFactory catalogRepositoryFactory = null;
	protected IngestMapperFactory ingestMapperFactory  = null;
	protected boolean restrictQueryPermissions = false;
	protected boolean restrictIngestPermissions = false;
	protected TransactionIdFactory transactionIdFactory = null;
	protected String pluginStorageDir = null;
	protected boolean oneCatalogFailsAllFail = false;
	protected boolean simplifyQueries = false;
	protected boolean disableIntersectingCrossCatalogQueries = false;
	protected int crossCatalogResultSortingThreshold = INT;
	
	public CatalogServiceLocalFactory() {} 
	
	public CatalogServiceLocal createCatalogService() {
		try {
			return new CatalogServiceLocal(this.catalogRepositoryFactory.createRepository(), this.ingestMapperFactory.createMapper(), new File(this.pluginStorageDir), this.transactionIdFactory, this.restrictQueryPermissions, this.restrictIngestPermissions, this.oneCatalogFailsAllFail, this.simplifyQueries, this.disableIntersectingCrossCatalogQueries, this.crossCatalogResultSortingThreshold);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to create CatalogServiceLocal : " + e.getMessage(), e);
			return null;
		}
	}

	@Required
	public void setCatalogRepositoryFactory(CatalogRepositoryFactory catalogRepositoryFactory) {
		this.catalogRepositoryFactory = catalogRepositoryFactory;
	}

	@Required
	public void setIngestMapperFactory(IngestMapperFactory ingestMapperFactory) {
		this.ingestMapperFactory = ingestMapperFactory;
	}

	@Required
	public void setRestrictQueryPermissions(boolean restrictQueryPermissions) {
		this.restrictQueryPermissions = restrictQueryPermissions;
	}

	@Required
	public void setRestrictIngestPermissions(boolean restrictIngestPermissions) {
		this.restrictIngestPermissions = restrictIngestPermissions;
	}

	@Required
	public void setTransactionIdFactory(String transactionIdFactory) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		this.transactionIdFactory = (TransactionIdFactory) Class.forName(transactionIdFactory).newInstance();
	}
	
	@Required
	public void setPluginStorageDir(String pluginStorageDir) {
		this.pluginStorageDir = pluginStorageDir;
	}
	
	@Required
	public void setOneCatalogFailsAllFail(boolean oneCatalogFailsAllFail) {
		this.oneCatalogFailsAllFail = oneCatalogFailsAllFail;
	}

	@Required
	public void setSimplifyQueries(boolean simplifyQueries) {
		this.simplifyQueries = simplifyQueries;
	}
	
	@Required
	public void setDisableIntersectingCrossCatalogQueries(boolean disableIntersectingCrossCatalogQueries) {
		this.disableIntersectingCrossCatalogQueries = disableIntersectingCrossCatalogQueries;
	}

	@Required
	public void setCrossCatalogResultSortingThreshold(
			int crossCatalogResultSortingThreshold) {
		this.crossCatalogResultSortingThreshold = crossCatalogResultSortingThreshold;
	}
	
}
