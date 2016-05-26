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

//JDK imports
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.catalog.exception.CatalogRepositoryException;
import org.apache.oodt.cas.catalog.page.CatalogReceipt;
import org.apache.oodt.cas.catalog.page.IndexPager;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.struct.TransactionIdFactory;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Ingest Mapper that indexes to a Lucene index
 * <p>
 */
public class LuceneIngestMapper implements IngestMapper {

  private static final String UNSUPPORTED = "This operation is currently unsupported. Please report to dev@oodt.apache.org";
  @Override
	public void deleteAllMappingsForCatalog(String catalogId)
			throws CatalogRepositoryException {
		throw new UnsupportedOperationException(UNSUPPORTED);
	}

  @Override
	public void deleteAllMappingsForCatalogServiceTransactionId(
			TransactionId<?> catalogServiceTransactionId)
			throws CatalogRepositoryException {
    throw new UnsupportedOperationException(UNSUPPORTED);
	}

  @Override
	public void deleteTransactionIdMapping(
			TransactionId<?> catalogTransactionId, String catalogId)
			throws CatalogRepositoryException {
    throw new UnsupportedOperationException(UNSUPPORTED);
	}

  @Override
	public CatalogReceipt getCatalogReceipt(
			TransactionId<?> catalogServiceTransactionId, String catalogId)
			throws CatalogRepositoryException {
    throw new UnsupportedOperationException(UNSUPPORTED);
	}

  @Override
	public TransactionId<?> getCatalogServiceTransactionId(
			TransactionId<?> catalogTransactionId, String catalogId)
			throws CatalogRepositoryException {
    throw new UnsupportedOperationException(UNSUPPORTED);
	}

  @Override
	public TransactionId<?> getCatalogTransactionId(
			TransactionId<?> catalogServiceTransactionId, String catalogId)
			throws CatalogRepositoryException {
    throw new UnsupportedOperationException(UNSUPPORTED);
	}

  @Override
	public Set<String> getCatalogIds(
			TransactionId<?> catalogServiceTransactionId)
			throws CatalogRepositoryException {
    throw new UnsupportedOperationException(UNSUPPORTED);
	}

  @Override
	public Set<TransactionId<?>> getPageOfCatalogTransactionIds(
			IndexPager indexPager, String catalogId)
			throws CatalogRepositoryException {
    throw new UnsupportedOperationException(UNSUPPORTED);
	}

  @Override
	public boolean hasCatalogServiceTransactionId(
			TransactionId<?> catalogServiceTransactionId)
			throws CatalogRepositoryException {
    throw new UnsupportedOperationException(UNSUPPORTED);
	}

  @Override
	public void storeTransactionIdMapping(
			TransactionId<?> catalogServiceTransactionId,
			TransactionIdFactory catalogServiceTransactionIdFactory,
			CatalogReceipt catalogReceipt,
			TransactionIdFactory catalogTransactionIdFactory)
			throws CatalogRepositoryException {
    throw new UnsupportedOperationException(UNSUPPORTED);
	}



}
