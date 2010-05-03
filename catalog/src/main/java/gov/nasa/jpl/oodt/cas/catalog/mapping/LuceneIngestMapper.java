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

package gov.nasa.jpl.oodt.cas.catalog.mapping;

//OODT imports
import gov.nasa.jpl.oodt.cas.catalog.exception.CatalogRepositoryException;
import gov.nasa.jpl.oodt.cas.catalog.page.CatalogReceipt;
import gov.nasa.jpl.oodt.cas.catalog.page.IndexPager;
import gov.nasa.jpl.oodt.cas.catalog.struct.TransactionId;
import gov.nasa.jpl.oodt.cas.catalog.struct.TransactionIdFactory;

//JDK imports
import java.util.Set;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Ingest Mapper that indexes to a Lucene index
 * <p>
 */
public class LuceneIngestMapper implements IngestMapper {

	public void deleteAllMappingsForCatalog(String catalogId)
			throws CatalogRepositoryException {
		// TODO Auto-generated method stub
		
	}

	public void deleteAllMappingsForCatalogServiceTransactionId(
			TransactionId<?> catalogServiceTransactionId)
			throws CatalogRepositoryException {
		// TODO Auto-generated method stub
		
	}

	public void deleteTransactionIdMapping(
			TransactionId<?> catalogTransactionId, String catalogId)
			throws CatalogRepositoryException {
		// TODO Auto-generated method stub
		
	}

	public CatalogReceipt getCatalogReceipt(
			TransactionId<?> catalogServiceTransactionId, String catalogId)
			throws CatalogRepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public TransactionId<?> getCatalogServiceTransactionId(
			TransactionId<?> catalogTransactionId, String catalogId)
			throws CatalogRepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public TransactionId<?> getCatalogTransactionId(
			TransactionId<?> catalogServiceTransactionId, String catalogId)
			throws CatalogRepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getCatalogIds(
			TransactionId<?> catalogServiceTransactionId)
			throws CatalogRepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<TransactionId<?>> getPageOfCatalogTransactionIds(
			IndexPager indexPager, String catalogId)
			throws CatalogRepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasCatalogServiceTransactionId(
			TransactionId<?> catalogServiceTransactionId)
			throws CatalogRepositoryException {
		// TODO Auto-generated method stub
		return false;
	}

	public void storeTransactionIdMapping(
			TransactionId<?> catalogServiceTransactionId,
			TransactionIdFactory catalogServiceTransactionIdFactory,
			CatalogReceipt catalogReceipt,
			TransactionIdFactory catalogTransactionIdFactory)
			throws CatalogRepositoryException {
		// TODO Auto-generated method stub
		
	}



}
