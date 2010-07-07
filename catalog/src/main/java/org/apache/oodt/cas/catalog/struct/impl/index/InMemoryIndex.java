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

package org.apache.oodt.cas.catalog.struct.impl.index;

//JDK imports
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.catalog.exception.CatalogIndexException;
import org.apache.oodt.cas.catalog.exception.IngestServiceException;
import org.apache.oodt.cas.catalog.page.IndexPager;
import org.apache.oodt.cas.catalog.page.IngestReceipt;
import org.apache.oodt.cas.catalog.struct.Index;
import org.apache.oodt.cas.catalog.struct.IngestService;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.struct.TransactionIdFactory;
import org.apache.oodt.cas.catalog.struct.impl.transaction.UuidTransactionIdFactory;
import org.apache.oodt.cas.catalog.term.TermBucket;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A in memory Index which is ingestable
 * <p>
 */
public class InMemoryIndex implements Index, IngestService {

	private static final long serialVersionUID = -3978455064365343116L;
	
	public HashMap<TransactionId<?>, List<TermBucket>> transactionIdToBucketsMap;
	public HashMap<TransactionId<?>, Date> transactionIdToTransactionDate;
	
	public InMemoryIndex() {
		this.transactionIdToBucketsMap = new HashMap<TransactionId<?>, List<TermBucket>>();
		this.transactionIdToTransactionDate = new HashMap<TransactionId<?>, Date>();
	}
	
	public String getProperty(String key) throws CatalogIndexException {
		return null;
	}
	
	public Properties getProperties() throws CatalogIndexException {
		return new Properties();
	}
	
	public List<TransactionId<?>> getPage(IndexPager indexPage) {
		List<TransactionId<?>> returnList = new Vector<TransactionId<?>>();
		int skipToLocation = (int) ((int) indexPage.getPageSize() * indexPage.getPageNum());
		List<TransactionId<?>> transactionIds = new Vector<TransactionId<?>>(this.transactionIdToBucketsMap.keySet());
		for (int i = skipToLocation; i < transactionIds.size() && i < (skipToLocation + indexPage.getPageSize()); i++)
			returnList.add(transactionIds.get(i));
		if (returnList.size() > 0)
			return returnList;
		else 
			return Collections.emptyList();
	}


	public TransactionIdFactory getTransactionIdFactory() throws CatalogIndexException {
		return new UuidTransactionIdFactory();
	}

	public boolean delete(TransactionId<?> transactionId)
			throws IngestServiceException {
		return this.transactionIdToBucketsMap.remove(transactionId) != null;
	}

	public IngestReceipt ingest(List<TermBucket> termBuckets) throws IngestServiceException {
		TransactionId<?> transactionId = null;
		try {
			transactionId = this.getTransactionIdFactory().createNewTransactionId();
			this.transactionIdToBucketsMap.put(transactionId, termBuckets);
			Date transactionDate = new Date();
			this.transactionIdToTransactionDate.put(transactionId, transactionDate);
			return new IngestReceipt(transactionId, transactionDate);
		}catch (Exception e) {
			throw new IngestServiceException("Failed to ingest '" + transactionId + "' : " + e.getMessage());
		}
	}

	public boolean reduce(TransactionId<?> transactionId,
			List<TermBucket> termBuckets) throws IngestServiceException {
		// TODO Auto-generated method stub
		return false;
	}

	public IngestReceipt update(TransactionId<?> transactionId,
			List<TermBucket> termBuckets) throws IngestServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasTransactionId(TransactionId<?> catalogTransactionid)
			throws CatalogIndexException {
		return this.transactionIdToBucketsMap.containsKey(catalogTransactionid);
	}

}
