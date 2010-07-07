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

package org.apache.oodt.cas.catalog.mapping;

//OODT imports
import org.apache.oodt.cas.catalog.exception.CatalogRepositoryException;
import org.apache.oodt.cas.catalog.page.CatalogReceipt;
import org.apache.oodt.cas.catalog.page.IndexPager;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.struct.TransactionIdFactory;

//JDK imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Ingest Mapper that indexes to local memory
 * <p>
 */
public class MemoryBasedIngestMapper implements IngestMapper {

	private static Logger LOG = Logger.getLogger(MemoryBasedIngestMapper.class.getName());
	
	protected HashMap<String, TransactionIdMapping> catalogServiceTransactionIdKeyMapping;
	protected HashMap<String, TransactionIdMapping> catalogInfoKeyMapping;
	protected HashMap<String, List<CatalogReceipt>> catalogIdToCatalogReceiptMapping;
	
	public MemoryBasedIngestMapper() {
		this.catalogServiceTransactionIdKeyMapping = new HashMap<String, TransactionIdMapping>();
		this.catalogInfoKeyMapping = new HashMap<String, TransactionIdMapping>();
		this.catalogIdToCatalogReceiptMapping = new HashMap<String, List<CatalogReceipt>>();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.apache.oodt.cas.catalog.repository.CatalogRepository#
	 * getCatalogServiceTransactionId
	 * (org.apache.oodt.cas.catalog.struct.TransactionId, java.lang.String)
	 */
	public synchronized TransactionId<?> getCatalogServiceTransactionId(
			TransactionId<?> catalogTransactionId, String catalogId)
			throws CatalogRepositoryException {
		LOG.log(Level.INFO, "Looking up CatalogService TransactionId for Catalog TransactionId '" + catalogTransactionId + "' and catalog '" + catalogId + "'");
		String key = generateKey(catalogTransactionId.toString(), catalogId);
		System.out.println("LOOKING UP: " + key);
		TransactionIdMapping mapping = this.catalogInfoKeyMapping.get(key);
		if (mapping != null) {
			return mapping.catalogServiceTransactionId;
		}else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.apache.oodt.cas.catalog.repository.CatalogRepository#
	 * getCatalogTransactionId
	 * (org.apache.oodt.cas.catalog.struct.TransactionId, java.lang.String)
	 */
	public synchronized TransactionId<?> getCatalogTransactionId(
			TransactionId<?> catalogServiceTransactionId, String catalogId)
			throws CatalogRepositoryException {
		TransactionIdMapping mapping = this.catalogServiceTransactionIdKeyMapping
				.get(catalogServiceTransactionId.toString());
		if (mapping != null)
			for (CatalogReceipt receipt : mapping.getCatalogReceipts())
				if (receipt.getCatalogId().equals(catalogId))
					return receipt.getTransactionId();
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.oodt.cas.catalog.mapping.IngestMapper#getPage(org.apache
	 * .oodt.cas.catalog.page.IndexPager, java.lang.String)
	 */
	public synchronized Set<TransactionId<?>> getPageOfCatalogTransactionIds(IndexPager indexPager,
			String catalogId) throws CatalogRepositoryException {
		Set<TransactionId<?>> catalogTransactionIds = new HashSet<TransactionId<?>>();
		List<CatalogReceipt> catalogReceipts = this.catalogIdToCatalogReceiptMapping.get(catalogId);
		if (catalogReceipts != null) 
			for (int i = indexPager.getPageNum() * indexPager.getPageSize(); i < catalogReceipts.size() && i < (indexPager.getPageNum() + 1) * indexPager.getPageSize(); i++) 
				catalogTransactionIds.add(catalogReceipts.get(i).getTransactionId());
		return catalogTransactionIds;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.catalog.mapping.IngestMapper#deleteAllMappingsForCatalog(java.lang.String)
	 */
	public synchronized void deleteAllMappingsForCatalog(String catalogId)
			throws CatalogRepositoryException {
		List<CatalogReceipt> catalogReceipts = this.catalogIdToCatalogReceiptMapping.remove(catalogId);
		if (catalogReceipts != null) {
			for (CatalogReceipt catalogReceipt : catalogReceipts) {
				TransactionIdMapping mapping = this.catalogInfoKeyMapping.remove(generateKey(catalogReceipt.getTransactionId().toString(), catalogReceipt.getCatalogId()));
				if (mapping != null)
					mapping.getCatalogReceipts().remove(catalogReceipt);
			}
		}		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.catalog.repository.CatalogRepository#deleteAllMappingsForCatalogServiceTransactionId(org.apache.oodt.cas.catalog.struct.TransactionId)
	 */
	public synchronized void deleteAllMappingsForCatalogServiceTransactionId(
			TransactionId<?> catalogServiceTransactionId)
			throws CatalogRepositoryException {
		TransactionIdMapping mapping = this.catalogServiceTransactionIdKeyMapping.remove(catalogServiceTransactionId.toString());
		if (mapping != null)
			for (CatalogReceipt catalogReceipt : mapping.getCatalogReceipts()) {
				this.catalogIdToCatalogReceiptMapping.get(catalogReceipt.getCatalogId()).remove(catalogReceipt);
				this.catalogInfoKeyMapping.remove(generateKey(catalogReceipt.getTransactionId().toString(), catalogReceipt.getCatalogId()));
			}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.catalog.repository.CatalogRepository#deleteTransactionIdMapping(org.apache.oodt.cas.catalog.struct.TransactionId, org.apache.oodt.cas.catalog.struct.TransactionId, java.lang.String)
	 */
	public synchronized void deleteTransactionIdMapping(
			TransactionId<?> catalogTransactionId, String catalogId)
			throws CatalogRepositoryException {
		List<CatalogReceipt> catalogReceipts = this.catalogIdToCatalogReceiptMapping.get(catalogId);
		for (int i = 0; i < catalogReceipts.size(); i++) {
			if (catalogReceipts.get(i).getCatalogId().equals(catalogId) && catalogReceipts.get(i).getTransactionId().equals(catalogTransactionId)) {
				catalogReceipts.remove(i);
				break;
			}
		}
		TransactionIdMapping mapping = this.catalogInfoKeyMapping.remove(generateKey(catalogTransactionId.toString(), catalogId));
		this.catalogServiceTransactionIdKeyMapping.remove(mapping.getCatalogServiceTransactionId().toString());
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.cas.catalog.repository.CatalogRepository#hasCatalogServiceTransactionId(org.apache.oodt.cas.catalog.struct.TransactionId)
	 */
	public synchronized boolean hasCatalogServiceTransactionId(
			TransactionId<?> catalogServiceTransactionId)
			throws CatalogRepositoryException {
		return this.catalogServiceTransactionIdKeyMapping.containsKey(catalogServiceTransactionId.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.apache.oodt.cas.catalog.repository.CatalogRepository#
	 * storeTransactionIdMapping(java.lang.String,
	 * org.apache.oodt.cas.catalog.struct.TransactionId,
	 * org.apache.oodt.cas.catalog.struct.TransactionId)
	 */
	public synchronized void storeTransactionIdMapping(
			TransactionId<?> catalogServiceTransactionId,
			TransactionIdFactory catalogServiceTransactionIdFactory,
			CatalogReceipt catalogReceipt,
			TransactionIdFactory catalogTransactionIdFactory)
			throws CatalogRepositoryException { 
		TransactionIdMapping mapping = this.catalogServiceTransactionIdKeyMapping
				.get(catalogServiceTransactionId.toString());
		if (mapping == null)
			mapping = new TransactionIdMapping(catalogServiceTransactionId);
		mapping.addCatalogReceipt(catalogReceipt);
		this.catalogServiceTransactionIdKeyMapping.put(
				catalogServiceTransactionId.toString(), mapping);
		this.catalogInfoKeyMapping.put(generateKey(catalogReceipt.getTransactionId().toString(), catalogReceipt.getCatalogId()), mapping);
		List<CatalogReceipt> catalogReceipts = this.catalogIdToCatalogReceiptMapping.get(catalogReceipt.getCatalogId());
		if (catalogReceipts == null)
			catalogReceipts = new Vector<CatalogReceipt>();
		catalogReceipts.add(catalogReceipt);
		this.catalogIdToCatalogReceiptMapping.put(catalogReceipt.getCatalogId(), catalogReceipts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.oodt.cas.catalog.repository.CatalogRepository#getCatalogs
	 * (org.apache.oodt.cas.catalog.struct.TransactionId)
	 */
	public synchronized Set<String> getCatalogIds(
			TransactionId<?> catalogServiceTransactionId)
			throws CatalogRepositoryException {
		HashSet<String> catalogs = new HashSet<String>();
		TransactionIdMapping mapping = this.catalogServiceTransactionIdKeyMapping
				.get(catalogServiceTransactionId.toString());
		for (CatalogReceipt catalogReceipt : mapping.getCatalogReceipts())
			catalogs.add(catalogReceipt.getCatalogId());
		return catalogs;
	}

	public CatalogReceipt getCatalogReceipt(
			TransactionId<?> catalogServiceTransactionId, String catalogId)
			throws CatalogRepositoryException {
		TransactionIdMapping mapping = this.catalogServiceTransactionIdKeyMapping.get(catalogServiceTransactionId);
		for (CatalogReceipt catalogReceipt : mapping.getCatalogReceipts())
			if (catalogReceipt.getCatalogId().equals(catalogId))
				return catalogReceipt;
		return null;
	}

	private static String generateKey(String catalogTransactionId, String catalogId) {
		return catalogTransactionId + ":" + catalogId;
	}
	
	private class TransactionIdMapping {

		private TransactionId<?> catalogServiceTransactionId;
		List<CatalogReceipt> catalogReceipts;

		public TransactionIdMapping(TransactionId<?> catalogServiceTransactionId) {
			this.catalogServiceTransactionId = catalogServiceTransactionId;
			this.catalogReceipts = new Vector<CatalogReceipt>();
		}

		public void addCatalogReceipt(CatalogReceipt catalogReceipt) {
			this.catalogReceipts.add(catalogReceipt);
		}

		public List<CatalogReceipt> getCatalogReceipts() {
			return this.catalogReceipts;
		}

		public TransactionId<?> getCatalogServiceTransactionId() {
			return catalogServiceTransactionId;
		}

	}

//	private class CatalogInfo {
//
//		private String catalogId;
//		private TransactionId<?> catalogTransactionId;
//
//		public CatalogInfo(String catalogId,
//				TransactionId<?> catalogTransactionId) {
//			this.catalogId = catalogId;
//			this.catalogTransactionId = catalogTransactionId;
//		}
//
//		public String getCatalogUrn() {
//			return this.catalogId;
//		}
//
//		public TransactionId<?> getCatalogTransactionId() {
//			return this.catalogTransactionId;
//		}
//		
//		public boolean equals(Object obj) {
//			if (obj instanceof CatalogInfo) {
//				return this.toString().equals(obj.toString());
//			}else {
//				return false;
//			}
//		}
//		
//		public String toString() {
//			return this.catalogId + ":" + this.catalogTransactionId;
//		}
//
//	}
	
}
