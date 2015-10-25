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
package org.apache.oodt.cas.catalog.system;

//OODT imports
import org.apache.oodt.cas.catalog.exception.CatalogDictionaryException;
import org.apache.oodt.cas.catalog.exception.CatalogException;
import org.apache.oodt.cas.catalog.exception.CatalogIndexException;
import org.apache.oodt.cas.catalog.page.CatalogReceipt;
import org.apache.oodt.cas.catalog.page.IndexPager;
import org.apache.oodt.cas.catalog.page.IngestReceipt;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.struct.Dictionary;
import org.apache.oodt.cas.catalog.struct.*;
import org.apache.oodt.cas.catalog.term.Term;
import org.apache.oodt.cas.catalog.term.TermBucket;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Calatog is a communication interface between the CatalogService and an underlying
 * database or index service
 * <p>
 */
public class Catalog {

	private static Logger LOG = Logger.getLogger(Catalog.class.getName());
		
	protected Vector<Dictionary> dictionaries;
	protected Index index;
	protected String id;
	protected boolean restrictQueryPermissions = true;
	protected boolean restrictIngestPermissions = true;
	
	public Catalog(String id, Index index, List<Dictionary> dictionaries, boolean restrictQueryPermissions, boolean restrictIngestPermissions) {
		this.id = id;
		this.index = index;
		if (dictionaries != null)
			this.dictionaries = new Vector<Dictionary>(dictionaries);
		this.restrictQueryPermissions = restrictQueryPermissions;
		this.restrictIngestPermissions = restrictIngestPermissions;
	}
	
	public String getId() {
		return this.id;
	}

	public TransactionIdFactory getTransactionIdFactory() throws CatalogIndexException {
		return this.index.getTransactionIdFactory();
	}
	
	public void setIndex(Index index) {
		this.index = index;
	}
	
	public void setDictionaries(List<Dictionary> dictionaries) {
		this.dictionaries = new Vector<Dictionary>(dictionaries);
	}
	
	public List<Dictionary> getDictionaries() {
		return Collections.unmodifiableList(this.dictionaries);
	}
	
	public void addDictionary(Dictionary dictionary) {
		if (this.dictionaries == null)
			this.dictionaries = new Vector<Dictionary>();
		this.dictionaries.add(dictionary);
	}

	public void setRestrictQueryPermissions(boolean restrictQueryPermissions) {
		this.restrictQueryPermissions = restrictQueryPermissions;
	}

	public void setRestrictIngestPermissions(boolean restrictIngestPermissions) {
		this.restrictIngestPermissions = restrictIngestPermissions;
	}

	public boolean isQueriable() {
		return this.index instanceof QueryService && !this.restrictQueryPermissions;
	}
	
	public boolean isIngestable() {
		return this.index instanceof IngestService && !this.restrictIngestPermissions;
	}
	
	public List<TransactionId<?>> getPage(IndexPager indexPage) throws CatalogIndexException {
		return this.index.getPage(indexPage);
	}
	
	public TransactionId<?> getTransactionIdFromString(String catalogTransactionId) throws IllegalArgumentException, SecurityException,
		CatalogIndexException {
		return this.getTransactionIdFactory().createTransactionId(catalogTransactionId);
	}
	
	public boolean hasTransactionId(TransactionId<?> catalogTransactionid)  throws CatalogIndexException {
		return this.index.hasTransactionId(catalogTransactionid);
	}
	
	public String getProperty(String key)  throws CatalogException {
		try {
			return this.index.getProperty(key);
		}catch (Exception e) {
			throw new CatalogException("Failed to get property '" + key + "' : " + e.getMessage(), e);
		}
	}
	
	public Properties getProperties() throws CatalogException {
		try {
			return this.index.getProperties();
		}catch (Exception e) {
			throw new CatalogException("Failed to get properties : " + e.getMessage(), e);
		}
	}
	
	/**
	 * 
	 * @param metadata
	 * @return TransactionId param if used by underlying catalog, otherwise
	 * the TransactionId generated and used by underlying catalog.  if no
	 * TermBuckets where created from the Metadata then null is returned
	 * @throws CatalogException
	 */
	public CatalogReceipt ingest(Metadata metadata) throws CatalogException {
		try {
			if (this.isIngestable()) {
				List<TermBucket> termBuckets = this.getTermBuckets(metadata);
				if (termBuckets.size() > 0) {
					LOG.log(Level.INFO, "Catalog '" + this + "' attemping ingest metadata");
					return new CatalogReceipt(((IngestService) this.index).ingest(termBuckets), this.getId());
				}else {
					LOG.log(Level.WARNING, "Catalog '" + this + "' dictionaries did not generate any TermBuckets from Metadata");
					return null;
				}
			}else {
				LOG.log(Level.WARNING, "Catalog '" + this + "' is not ingestable");
				return null;
			}
		}catch (Exception e) {
			throw new CatalogException(e.getMessage(), e);
		}
	}
	
	public CatalogReceipt update(TransactionId<?> transactionId, Metadata metadata) throws CatalogException {
		try {
			if (this.isIngestable()) {
				List<TermBucket> termBuckets = this.getTermBuckets(metadata);
				if (termBuckets.size() > 0) {
					LOG.log(Level.INFO, "Catalog '" + this + "' attemping update metadata for catalog TransactionId [id = '" + transactionId + "']");
					IngestReceipt ingestReceipt = ((IngestService) this.index).update(transactionId, termBuckets);
					if (ingestReceipt != null)
						return new CatalogReceipt(ingestReceipt, this.getId());
					else
						return null;
				}else {
					LOG.log(Level.WARNING, "Catalog '" + this + "' did not generate any TermBuckets from Metadata for catalog TransactionId [id = '" + transactionId + "']");
					return null;
				}
			}else {
				LOG.log(Level.WARNING, "Catalog '" + this + "' is not ingestable");
				return null;
			}
		}catch (Exception e) {
			throw new CatalogException(e.getMessage(), e);
		}
	}
	
	public boolean delete(TransactionId<?> transactionId) throws CatalogException {
		try {
			if (this.isIngestable()) {
				LOG.log(Level.INFO, "Catalog '" + this + "' attemping to delete all TermBuckets associated with catalog TransactionId [id = '" + transactionId + "']");
				return ((IngestService) this.index).delete(transactionId);
			}else {
				LOG.log(Level.WARNING, "Catalog '" + this + "' is not ingestable");
				return false;
			}
		}catch (Exception e) {
			throw new CatalogException(e.getMessage(), e);
		}
	}
	
	public boolean reduce(TransactionId<?> transactionId, Metadata metadata) throws CatalogException {
		try {
			if (this.isIngestable()) {
				List<TermBucket> termBuckets = this.getTermBuckets(metadata);
				if (termBuckets.size() > 0) {
					LOG.log(Level.INFO, "Catalog '" + this + "' attemping reduce metadata for catalog TransactionId [id = '" + transactionId + "']");
					return ((IngestService) this.index).reduce(transactionId, termBuckets);
				}else {
					LOG.log(Level.WARNING, "Catalog '" + this + "' did not generate any TermBuckets from Metadata for catalog TransactionId [id = '" + transactionId + "'] -- no metadata reduction took place");
					return false;
				}
			}else {
				LOG.log(Level.WARNING, "Catalog '" + this + "' is not ingestable");
				return false;
			}
		}catch(Exception e) {
			throw new CatalogException(e.getMessage(), e);
		}
	}
		
	public List<CatalogReceipt> query(QueryExpression queryExpression) throws CatalogException {
		try {
			if (this.isQueriable()) {
				QueryService queryService = (QueryService) this.index;
				List<CatalogReceipt> catalogReceipts = new Vector<CatalogReceipt>();
				for (IngestReceipt ingestReceipt : queryService.query(queryExpression)) 
					catalogReceipts.add(new CatalogReceipt(ingestReceipt, this.getId()));
				return Collections.unmodifiableList(catalogReceipts);
			}else {
				LOG.log(Level.WARNING, "Catalog '" + this + "' is not queriable");
				return Collections.emptyList();
			}
		}catch (Exception e) {
			throw new CatalogException(e.getMessage(), e);
		}
	}
	

	public List<CatalogReceipt> query(QueryExpression queryExpression, int startIndex, int endIndex) throws CatalogException {
		try {
			if (this.isQueriable()) {
				QueryService queryService = (QueryService) this.index;
				List<CatalogReceipt> catalogReceipts = new Vector<CatalogReceipt>();
				for (IngestReceipt ingestReceipt : queryService.query(queryExpression, startIndex, endIndex)) 
					catalogReceipts.add(new CatalogReceipt(ingestReceipt, this.getId()));
				return Collections.unmodifiableList(catalogReceipts);
			}else {
				LOG.log(Level.WARNING, "Catalog '" + this + "' is not queriable");
				return Collections.emptyList();
			}
		}catch (Exception e) {
			throw new CatalogException(e.getMessage(), e);
		}
	}
	
	public int sizeOf(QueryExpression queryExpression) throws CatalogException {
		try {
			if (this.isQueriable()) {
				QueryService queryService = (QueryService) this.index;
				return queryService.sizeOf(queryExpression);
			}else {
				LOG.log(Level.WARNING, "Catalog '" + this + "' is not queriable");
				return 0;
			}
		}catch (Exception e) {
			throw new CatalogException(e.getMessage(), e);
		}
	}
	
	public Metadata getMetadata(TransactionId<?> transactionId) throws CatalogException {
		try {
			if (this.isQueriable()) {
				QueryService queryService = (QueryService) this.index;
				return this.getMetadataFromBuckets(queryService.getBuckets(transactionId));
			}else { 
				LOG.log(Level.WARNING, "Catalog '" + this + "' is not queriable");
				return new Metadata();
			}
		}catch(Exception e) {
			throw new CatalogException(e.getMessage(), e);
		}
	}
	
	public Map<TransactionId<?>, Metadata> getMetadata(List<TransactionId<?>> transactionIds) throws CatalogException {
		try {
			Map<TransactionId<?>, Metadata> metadataMap = new HashMap<TransactionId<?>, Metadata>();
			if (this.isQueriable()) {
				QueryService queryService = (QueryService) this.index;
				Map<TransactionId<?>, List<TermBucket>> termBucketMap = queryService.getBuckets(transactionIds);
				for (TransactionId<?> transactionId : termBucketMap.keySet())
					metadataMap.put(transactionId, this.getMetadataFromBuckets(termBucketMap.get(transactionId)));
			}else {
				LOG.log(Level.WARNING, "Catalog '" + this + "' is not queriable");
			}
			return metadataMap;
		}catch(Exception e) {
			throw new CatalogException(e.getMessage(), e);
		}
	}
	
	public boolean isInterested(QueryExpression queryExpression) throws CatalogException {
		try {
			if (this.dictionaries != null) {
				for (Dictionary dictionary : this.dictionaries)
					if (dictionary.understands(queryExpression))
						return true;
				return false;
			}else {
				return true;
			}
		}catch(Exception e) {
			throw new CatalogException(e.getMessage(), e);
		}
	}
	
	protected Metadata getMetadataFromBuckets(List<TermBucket> termBuckets) throws CatalogDictionaryException {
		Metadata metadata = new Metadata();
		for (TermBucket termBucket : termBuckets) {
			if (this.dictionaries != null) {
				for (Dictionary dictionary : this.dictionaries) 
					metadata.addMetadata(dictionary.reverseLookup(termBucket));
			}else {
				metadata.addMetadata(this.asMetadata(termBuckets));
			}
		}
		return metadata;
	}
	
	protected Metadata asMetadata(List<TermBucket> termBuckets) {
		Metadata m = new Metadata();
		for (TermBucket bucket : termBuckets)
			for (Term term : bucket.getTerms())
				m.addMetadata(term.getName(), term.getValues());
		return m;
	}
    
	protected List<TermBucket> getTermBuckets(Metadata metadata) throws CatalogDictionaryException {
		List<TermBucket> termBuckets = new Vector<TermBucket>();
		if (this.dictionaries != null) {
			for (Dictionary dictionary : this.dictionaries) {
				TermBucket termBucket = dictionary.lookup(metadata);
				if (termBucket != null)
					termBuckets.add(termBucket);
			}
		}else {
			LOG.log(Level.WARNING, "Catalog '" + this + "' has no dictionaries defined, attempting to send all Metadata in a default TermBucket");
			TermBucket bucket = new TermBucket();
			for (String key : metadata.getAllKeys())
				bucket.addTerm(new Term(key, metadata.getAllMetadata(key)));
			termBuckets.add(bucket);
		}
		return termBuckets;
	}
	
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Catalog) 
			return ((Catalog) obj).getId().equals(this.getId());
		else if (obj instanceof String) 
			return this.getId().equals((String) obj);
		else
			return false;
	}
	
    public String toString() {
    	return this.getId();
    }
		
}
