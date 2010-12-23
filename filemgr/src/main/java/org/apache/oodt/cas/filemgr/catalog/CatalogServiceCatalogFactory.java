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
package org.apache.oodt.cas.filemgr.catalog;

//JDK imports
import java.util.logging.Level;
import java.util.logging.Logger;

//Spring imports
import org.springframework.context.support.FileSystemXmlApplicationContext;

//OODT imports
import org.apache.oodt.cas.catalog.system.CatalogService;
import org.apache.oodt.cas.catalog.system.CatalogServiceFactory;
import org.apache.oodt.cas.filemgr.repository.RepositoryManager;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;

/**
 * @author bfoster
 * @version $Revision$
 */
public class CatalogServiceCatalogFactory implements CatalogFactory {

	private static Logger LOG = Logger.getLogger(CatalogServiceCatalogFactory.class.getName());
	
	protected ValidationLayer validationLayer;
	protected String validationLayerFactoryClass;
	protected String repositoryManagerFactoryClass;
	
	protected int pageSize;
	protected boolean permitCatalogOverride;
	protected boolean preserveCatalogMapping;
	
	public CatalogServiceCatalogFactory() {
		this.validationLayerFactoryClass = System
        	.getProperty("filemgr.validationLayer.factory",
                "gov.nasa.jpl.oodt.cas.filemgr.validation.DataSourceValidationLayerFactory");
        
		this.repositoryManagerFactoryClass = System
        	.getProperty("filemgr.repository.factory",
        		"gov.nasa.jpl.oodt.cas.filemgr.repository.DataSourceRepositoryManagerFactory");
		
        this.pageSize = Integer.getInteger("gov.nasa.jpl.oodt.cas.filemgr.catalog.catalogservice.pageSize", 50);
	}
	
	/*
	 * (non-Javadoc)
	 * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.CatalogFactory#setValidationLayer(gov.nasa.jpl.oodt.cas.filemgr.validation.ValidationLayer)
	 */
	public void setValidationLayer(ValidationLayer validationLayer) {
		this.validationLayer = validationLayer;
	}
	
	/*
	 * (non-Javadoc)
	 * @see gov.nasa.jpl.oodt.cas.filemgr.catalog.CatalogFactory#createCatalog()
	 */
	public Catalog createCatalog() {
		try {
			if (validationLayer == null)
		        validationLayer = GenericFileManagerObjectFactory.getValidationLayerFromFactory(validationLayerFactoryClass);
			RepositoryManager repositoryManager = GenericFileManagerObjectFactory.getRepositoryManagerServiceFromFactory(System.getProperty("filemgr.repository.factory"));
	        String configFile = System.getProperty("gov.nasa.jpl.oodt.cas.filemgr.catalog.catalogservice.config");
			if (configFile != null) {
		        FileSystemXmlApplicationContext appContext = new FileSystemXmlApplicationContext(configFile);
		        CatalogService catalogService = ((CatalogServiceFactory) appContext.getBean(System.getProperty("gov.nasa.jpl.oodt.cas.filemgr.catalog.catalogservice.factory.bean.id"), CatalogServiceFactory.class)).createCatalogService();
//		        Map<String, CatalogBuilder> catalogBuilders = appContext.getBeansOfType(CatalogBuilder.class);
//				Set<String> hitList = catalogService.getCurrentCatalogIds();
//				for (CatalogBuilder catalogBuilder : catalogBuilders.values()) {
//					catalogBuilder.setValidationLayer(this.validationLayer);
//					catalogBuilder.setRepositoryManager(this.repositoryManager);
//					gov.nasa.jpl.oodt.cas.catalog.system.Catalog catalog = catalogBuilder.buildCatalogServiceCatalog();
//					hitList.remove(catalog.getUrnId());
//					if (catalogBuilder.getCatalogClassLoaderUrls() != null && catalogBuilder.getCatalogClassLoaderUrls().size() > 0)
//						catalogService.addCustomClassLoaderUrls(catalogBuilder.getCatalogClassLoaderUrls());
//					if (this.permitCatalogOverride)
//						catalogService.replaceCatalog(catalog);
//					else
//						catalogService.addCatalog(catalog);
//				}
//				if (this.permitCatalogOverride) {
//					System.out.println("HITLIST: " + hitList);
//					for (String catalogUrn : hitList)
//						catalogService.removeCatalog(catalogUrn, this.preserveCatalogMapping);
//				}
			
				return new CatalogServiceCatalog(catalogService, repositoryManager, this.validationLayer, this.pageSize);
			}else {
				throw new Exception("Must specify property 'gov.nasa.jpl.oodt.cas.filemgr.catalog.catalogservice.config'");
			}
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to create CatalogServiceCatalog : " + e.getMessage(), e);
			return null;
		}
	}

}
