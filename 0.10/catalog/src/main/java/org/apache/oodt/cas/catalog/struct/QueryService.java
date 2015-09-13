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

//JDK imports
import java.util.List;
import java.util.Map;

//OODT imports
import org.apache.oodt.cas.catalog.exception.QueryServiceException;
import org.apache.oodt.cas.catalog.page.IngestReceipt;
import org.apache.oodt.cas.catalog.page.TransactionReceipt;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.term.TermBucket;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Interface for performing queries to a Catalog
 * <p>
 */
public interface QueryService {

	/**
	 * Returns all the TransactionIds that fall under the given query
	 * @param query The query for TransactionIds
	 * @return Returns a List of TransactionIds that meet the query, otherwise
	 * an empty List is no TransactionIds are found for the given query
	 */
	public List<IngestReceipt> query(QueryExpression queryExpression) throws QueryServiceException;
	
	/**
	 * Returns the results of the given query such that: [startIndex, endIndex)
	 * @param queryExpression The query for which results will be returned
	 * @param startIndex The start index of subset of results to be returned
	 * @param endIndex The end index of the subset of results to be returned
	 * @return The results of the given query such that: [startIndex, endIndex)
	 * @throws QueryServiceException on an error
	 */
	public List<IngestReceipt> query(QueryExpression queryExpression, int startIndex, int endIndex) throws QueryServiceException;

	/**
	 * Returns the number of results found for the given query
	 * @param queryExpression The query whose size in question
	 * @return The number of results found for the given query
	 * @throws QueryServiceException on an error
	 */
	public int sizeOf(QueryExpression queryExpression) throws QueryServiceException;
	
	/**
	 * Returns a List of TermBuckets ingested for a given TransactionId
	 * @param transactionId The TransactionId in question
	 * @return A List of TermBuckets for the given TransactionId or empty list
	 * if no record of the give TranactionId exists
	 */
	public List<TermBucket> getBuckets(TransactionId<?> transactionId) throws QueryServiceException;
	
	/**
	 * Does the same as getBuckets(TransactionId), except is performed over a list
	 * of TransactionIds
	 * @param transactionIds A List of TransactionIds for which TermBuckets are wanted
	 * @return A Map of TransactionIds to TermBuckets
	 */
	public Map<TransactionId<?>, List<TermBucket>> getBuckets(List<TransactionId<?>> transactionIds) throws QueryServiceException;

}
