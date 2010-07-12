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

package org.apache.oodt.cas.catalog.struct;

//OODT imports
import java.util.List;

//OODT imports
import org.apache.oodt.cas.catalog.exception.IngestServiceException;
import org.apache.oodt.cas.catalog.page.IngestReceipt;
import org.apache.oodt.cas.catalog.term.TermBucket;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Interface for performing ingests to an Index
 * <p>
 */
public interface IngestService {
	
	/**
	 * Indexes the given TermBucket to a TransactionId, and returns a IngestReceipt
	 * @param termBuckets The List of TermBucket to be ingested
	 * @return IngestReceipt Receipt of ingest
	 * @throws IngestServiceException Any error 
	 */
	public IngestReceipt ingest(List<TermBucket> termBuckets) throws IngestServiceException;
	
	/**
	 * TermBucket updates to given TransactionId.  A new TransactionId can be returned in IngestReceipt
	 * if so desired and it will automatically get remapped by CatalogService.  Existing metadata 
	 * for given TransactionId should not be deleted, just the terms in the given term buckets should
	 * be modified.  For a complete re-ingest, one should instead delete() then ingest().
	 * @param transactionId
	 * @param termBuckets
	 * @throws IngestServiceException
	 */
	public IngestReceipt update(TransactionId<?> transactionId, List<TermBucket> termBuckets) throws IngestServiceException;
	
	/**
	 * Deletes all TermBuckets attached to given TransactionId -- there should be no trace of 
	 * given transaction after this method is called.
	 * @param transactionId The ID for given transaction which should be erased
	 * @throws IngestServiceException Any error 
	 */
	public boolean delete(TransactionId<?> transactionId) throws IngestServiceException;
	
	/**
	 * Deletes only the Terms in the given TermBuckets from the given TransactionId
	 * @param transactionId The TransactionId for which Terms will be deleted
	 * @param termBuckets The reduction set of Terms for each TermBucket
	 * @throws IngestServiceException Any error
	 */
	public boolean reduce(TransactionId<?> transactionId, List<TermBucket> termBuckets) throws IngestServiceException;
		
}
