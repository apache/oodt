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

package org.apache.oodt.cas.crawl.action;

// JDK imports
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.logging.Level;

//OODT imports
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.filemgr.tools.SolrIndexer;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.solr.client.solrj.SolrServerException;

/**
 * Crawler action that ingests the product metadata into the configured Solr index.
 * 
 */
public class SolrIndexingAction extends CrawlerAction {
	
	// URL of Solr instance with default value.
	private String solrUrl = "http://localhost:8983/solr";
	
	// URL of File Manager instance with default value
	private String fileManagerUrl = "http://localhost:9000/";
	
	// environment variables (containing location of indexer.properties)
	private Map<String, String> env = new ConcurrentHashMap<String,String>();
	
	// service responsible for metadata migration
	private SolrIndexer solrIndexer = null;

	@Override
	public boolean performAction(File product, Metadata productMetadata) throws CrawlerActionException {
		
		//try {
			
			String productName = productMetadata.getMetadata("ProductName");
			LOG.log(Level.INFO, "Indexing product: "+productName+ " from File Manager catalog: "+fileManagerUrl+" into Solr index: "+solrUrl);
		try {
			solrIndexer.indexProductByName(productName, true); // delete=true
		} catch (SolrServerException e) {
			throw new CrawlerActionException(e);
		}
		try {
			solrIndexer.commit(); // must commit:w
		} catch (SolrServerException | IOException e) {
			throw new CrawlerActionException(e);
		}
		return true; // success
			
		/*} catch(Exception e) {
			throw new CrawlerActionException(e);
		}*/
		
	}
	
	/**
	 * Initialization method configures the SolrIndexer.
	 */
	public void init() throws InstantiationException {
		
		// set environment from bean configuration
		// (including indexer.properties)
		for (Map.Entry<String, String> s : env.entrySet()) {
			System.setProperty(s.getKey(), env.get(s.getValue()));
		}
		
		// instantiate indexing service
		solrIndexer = new SolrIndexer(solrUrl, fileManagerUrl);
		
	}
	
	public void setSolrUrl(String solrUrl) {
		this.solrUrl = solrUrl;
	}
	
	public void setFileManagerUrl(String fileManagerUrl) {
		this.fileManagerUrl = fileManagerUrl;
	}

	public void setEnv(Map<String, String> env) {
		this.env = env;
	}

}
