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
package org.apache.oodt.cas.filemgr.catalog.catalogservice;

//JDK imports
import java.io.FileInputStream;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

//OODT imports
import org.apache.oodt.cas.catalog.struct.Dictionary;
import org.apache.oodt.cas.filemgr.catalog.CatalogFactory;
import org.apache.oodt.cas.filemgr.repository.RepositoryManager;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;

/**
 * 
 * @author bfoster
 *
 */
public class CatalogBuilder {

	private static final Logger LOG = Logger.getLogger(CatalogBuilder.class.getName());
	
	protected String catalogId;
	protected CatalogFactory fmCatalogFactory;
	protected List<String> supportedProductTypeIds;
	protected String transactionIdFactory;
	protected boolean restrictIngestPermissions;
	protected boolean restrictQueryPermissions;
	protected String filemgrPropertiesFile;
	
	public CatalogBuilder() {}
	
	public String getCatalogId() {
		return this.catalogId;
	}

	public void setCatalogId(String catalogId) {
		this.catalogId = catalogId;
	}

	public String getTransactionIdFactory() {
		return transactionIdFactory;
	}

	@Required
	public void setTransactionIdFactory(
			String transactionIdFactory) {
		this.transactionIdFactory = transactionIdFactory;
	}
	
	public String getFilemgrPropertiesFile() {
		return this.filemgrPropertiesFile;
	}
	
	public void setFilemgrPropertiesFile(String filemgrPropertiesFile) {
		this.filemgrPropertiesFile = filemgrPropertiesFile;
	}
	
	public CatalogFactory getFmCatalogFactory() {
		return this.fmCatalogFactory;
	}

	@Required
	public void setFmCatalogFactory(CatalogFactory fmCatalogFactory) {
		this.fmCatalogFactory = fmCatalogFactory;
	}

	public List<String> getSupportedProductTypeIds() {
		return this.supportedProductTypeIds;
	}

	@Required
	public void setSupportedProductTypeIds(List<String> supportedProductTypeIds) {
		this.supportedProductTypeIds = supportedProductTypeIds;
	}

	public boolean isRestrictIngestPermissions() {
		return restrictIngestPermissions;
	}

	@Required
	public void setRestrictIngestPermissions(boolean restrictIngestPermissions) {
		this.restrictIngestPermissions = restrictIngestPermissions;
	}

	public boolean isRestrictQueryPermissions() {
		return restrictQueryPermissions;
	}

	@Required
	public void setRestrictQueryPermissions(boolean restrictQueryPermissions) {
		this.restrictQueryPermissions = restrictQueryPermissions;
	}

	public org.apache.oodt.cas.catalog.system.Catalog createCatalog() {
		try {
			System.getProperties().load(new FileInputStream(this.filemgrPropertiesFile));
			ValidationLayer validationLayer = GenericFileManagerObjectFactory.getValidationLayerFromFactory(System.getProperty("filemgr.validationLayer.factory"));
			RepositoryManager repositoryManager = GenericFileManagerObjectFactory.getRepositoryManagerServiceFromFactory(System.getProperty("filemgr.repository.factory"));
			List<Dictionary> dictionaries = new Vector<Dictionary>();
			for (String productTypeId : this.supportedProductTypeIds) {
				ProductDictionary productDictionary = new ProductDictionary();
				ProductType type = repositoryManager.getProductTypeById(productTypeId);
				if (type == null)
					throw new RepositoryManagerException("ProductType '" + productTypeId + "' does not exist");
				productDictionary.setProductType(type, validationLayer.getElements(type));
				dictionaries.add(productDictionary);
			}
			FilemgrCatalogIndex index = new FilemgrCatalogIndex();
			this.fmCatalogFactory.setValidationLayer(validationLayer);
			index.setFmCatalogFactory(this.fmCatalogFactory);
			List<ProductType> supportedProductTypes = new Vector<ProductType>();
			for (String productTypeId : this.supportedProductTypeIds)
				supportedProductTypes.add(repositoryManager.getProductTypeById(productTypeId));
			index.setSupportedProductTypes(supportedProductTypes);
			index.setTransactionIdFactory(this.transactionIdFactory);
			return new org.apache.oodt.cas.catalog.system.Catalog(this.catalogId, index, dictionaries, this.restrictQueryPermissions, this.restrictIngestPermissions);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to create CatalogService Catalog : " + e.getMessage(), e);
			return null;
		}
	}
	
}
