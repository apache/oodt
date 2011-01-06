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

//OODT imports
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.metadata.util.PathUtils;

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
	protected String indexFilePath = null;

	/* our validation layer */
	protected ValidationLayer validationLayer = null;
	
	/* the page size for pagination */
	protected int pageSize = -1;
	
	/* the write lock timeout */
	protected long writeLockTimeOut = -1L;
	
	/* the commit lock timeout */
	protected long commitLockTimeOut = -1L;
	
	/* the merge factor */
	protected int mergeFactor = -1;
	
	protected String validationLayerFactoryClass;
	
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
		
		validationLayerFactoryClass = System
				.getProperty("filemgr.validationLayer.factory",
						"org.apache.oodt.cas.validation.DataSourceValidationLayerFactory");
		
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
	
	public String getIndexFilePath() {
		return indexFilePath;
	}
	
	public void setIndexFilePath(String indexFilePath) {
		this.indexFilePath = indexFilePath;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public long getWriteLockTimeOut() {
		return writeLockTimeOut;
	}

	public void setWriteLockTimeOut(long writeLockTimeOut) {
		this.writeLockTimeOut = writeLockTimeOut;
	}

	public long getCommitLockTimeOut() {
		return commitLockTimeOut;
	}

	public void setCommitLockTimeOut(long commitLockTimeOut) {
		this.commitLockTimeOut = commitLockTimeOut;
	}

	public int getMergeFactor() {
		return mergeFactor;
	}

	public void setMergeFactor(int mergeFactor) {
		this.mergeFactor = mergeFactor;
	}

	public void setValidationLayer(ValidationLayer validationLayer) {
		this.validationLayer = validationLayer;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.oodt.cas.filemgr.catalog.CatalogFactory#createCatalog()
	 */
	public Catalog createCatalog() {
		if (validationLayer == null)
	        validationLayer = GenericFileManagerObjectFactory.getValidationLayerFromFactory(validationLayerFactoryClass);
		return new LuceneCatalog(indexFilePath, validationLayer, pageSize,
				commitLockTimeOut, writeLockTimeOut, mergeFactor);
	}

}
