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
package org.apache.oodt.cas.filemgr.catalog.solr;

import java.util.logging.Logger;

import org.apache.oodt.cas.filemgr.catalog.Catalog;
import org.apache.oodt.cas.filemgr.catalog.CatalogFactory;
import org.apache.oodt.cas.metadata.util.PathUtils;

/**
 * Factory class that creates a {@link SolrCatalog} instance
 * configured with the properties specified in filemgr.properties.
 * 
 * @author Luca Cinquini
 *
 */
public class SolrCatalogFactory implements CatalogFactory {
	
	private String solrUrl;
	private ProductIdGenerator productIdGenerator;
	private ProductSerializer productSerializer;
	
	private static final Logger LOG = Logger.getLogger(SolrCatalogFactory.class.getName());
	
	public SolrCatalogFactory() throws IllegalArgumentException {
		
		// base Solr URL
		String solrUrl = System.getProperty("org.apache.oodt.cas.filemgr.catalog.solr.url");
		if (solrUrl==null) {
			throw new IllegalArgumentException("Invalid Solr URL specified from property 'org.apache.oodt.cas.filemgr.catalog.solr.url'");
		}
		
		this.solrUrl = PathUtils.replaceEnvVariables( solrUrl );
		this.configure();
		
	}
	
	/**
	 * Method to configure the specific flavor of the Solr catalog implementation
	 * before the catalog is instantiated.
	 */
	private void configure() {
		
		// product serializer
		String productSerializerClass = System.getProperty("org.apache.oodt.cas.filemgr.catalog.solr.productSerializer");
		if (productSerializerClass!=null) {
			try {
				productSerializer = (ProductSerializer)Class.forName( PathUtils.replaceEnvVariables(productSerializerClass) ).newInstance();
			} catch(Exception e) {
				LOG.severe(e.getMessage());
				System.exit(-1);
			}
		} else {
			productSerializer = new DefaultProductSerializer();
		}
		
		// product id generator
		String productIdGeneratorClass = System.getProperty("org.apache.oodt.cas.filemgr.catalog.solr.productIdGenerator");
		if (productIdGeneratorClass!=null) {
			try {
				productIdGenerator = (ProductIdGenerator)Class.forName( PathUtils.replaceEnvVariables(productIdGeneratorClass) ).newInstance();
			} catch(Exception e) {
				LOG.severe(e.getMessage());
				System.exit(-1);
			}
		} else {
			productIdGenerator = new UUIDProductIdGenerator();
		}
		
	}

	@Override
	public Catalog createCatalog() {
		LOG.info("Creating Solr Catalog for URL="+this.solrUrl);
		return new SolrCatalog(solrUrl, productIdGenerator, productSerializer);
	}

}
