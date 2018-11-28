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
import java.io.File;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;

//Lucene imports
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Factory for creating {@link Lucene}-based {@link Catalog}s.
 * </p>
 * 
 */
public class LuceneCatalogFactory implements CatalogFactory {

	/* path to the index directory for lucene catalogs */
	private String indexFilePath = null;

	/* our validation layer */
	private ValidationLayer validationLayer = null;
	
	/* the page size for pagination */
	private int pageSize = -1;
	
	/* the write lock timeout */
	private long writeLockTimeOut = -1L;
	
	/* the commit lock timeout */
	private long commitLockTimeOut = -1L;
	
	/* the merge factor */
	private int mergeFactor = -1;
	
	/* Whether or not to enforce strict definition of metadata fields:
	 * 'lenient=false' means that all metadata fields need to be explicitly defined in the XML configuration file */
	private boolean lenientFields = false;
	
	/* our log stream */
    private static final Logger LOG = Logger.getLogger(LuceneCatalogFactory.class.getName());
    
	/**
	 * 
	 */
	public LuceneCatalogFactory() throws IllegalArgumentException {
		indexFilePath = System
				.getProperty("org.apache.oodt.cas.filemgr.catalog.lucene.idxPath");
		if (indexFilePath == null) {
			throw new IllegalArgumentException(
					"error initializing lucene catalog: "
							+ "[org.apache.oodt.cas.filemgr.catalog.lucene.idxPath="
							+ indexFilePath);
		}

		//do env var replacement
		indexFilePath = PathUtils.replaceEnvVariables(indexFilePath);
		
		// instantiate validation layer, unless catalog is explicitly configured for lenient fields
		lenientFields = Boolean.parseBoolean( System.getProperty("org.apache.oodt.cas.filemgr.catalog.lucene.lenientFields", "false") );
		if (!lenientFields) {
			String validationLayerFactoryClass = System
				.getProperty("filemgr.validationLayer.factory",
						"org.apache.oodt.cas.filemgr.validation.XMLValidationLayerFactory");
			validationLayer = GenericFileManagerObjectFactory
				.getValidationLayerFromFactory(validationLayerFactoryClass);
		}
		
		pageSize = Integer.getInteger("org.apache.oodt.cas.filemgr.catalog.lucene.pageSize", 20).intValue();
		
		commitLockTimeOut = Long
				.getLong(
						"org.apache.oodt.cas.filemgr.catalog.lucene.commitLockTimeout.seconds",
						60).longValue();
		writeLockTimeOut = Long
				.getLong(
						"org.apache.oodt.cas.filemgr.catalog.lucene.writeLockTimeout.seconds",
						60).longValue();
		mergeFactor = Integer.getInteger(
				"org.apache.oodt.cas.filemgr.catalog.lucene.mergeFactor", 20)
				.intValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.oodt.cas.filemgr.catalog.CatalogFactory#createCatalog()
	 */
	public Catalog createCatalog() {
	    File indexDir = new File(indexFilePath);
	    // Create the index if it does not already exist
	    IndexWriter writer = null;
	    if (!indexDir.exists()) {
	        try { 
	            writer = new IndexWriter(indexDir, new StandardAnalyzer(), true);
	        } catch (Exception e) {
	            LOG.severe("Unable to create index: " + e.getMessage());
	        } finally {
	            if (writer != null) {
	                try {
	                    writer.close();
	                } catch (Exception e) {
	                    LOG.severe("Unable to close index: " + e.getMessage());
	                }
	            }
	        }
	    }
		return new LuceneCatalog(indexFilePath, validationLayer, pageSize,
				commitLockTimeOut, writeLockTimeOut, mergeFactor);
	}

}
