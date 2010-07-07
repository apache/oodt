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
import org.apache.oodt.cas.catalog.exception.CatalogIndexException;
import org.apache.oodt.cas.catalog.page.IndexPager;

//JDK imports
import java.util.List;
import java.util.Properties;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Interface for Communicating with an Term Index.  Should also implement
 * IngestService to allow Term ingest to this Index and/or implement 
 * QueryService to allow Term query on this Index.
 * <p>
 */
public interface Index {
	
	public Properties getProperties() throws CatalogIndexException;
	
	public String getProperty(String key) throws CatalogIndexException;
	
	/**
	 * Returns a list of TransactionIds associated with the 
	 * given Index page.
	 * @param indexPage The page for which TransactionIds will be returned
	 * @return A page of TransactionIds, if page does not exist,
	 * then returns null.
	 */
	public List<TransactionId<?>> getPage(IndexPager indexPage) throws CatalogIndexException;
	
	/**
	 * 
	 * @return
	 */
	public TransactionIdFactory getTransactionIdFactory() throws CatalogIndexException;
	
	/**
	 * 
	 * @param catalogTransactionid
	 * @return
	 * @throws CatalogIndexException
	 */
	public boolean hasTransactionId(TransactionId<?> transactionid)  throws CatalogIndexException;
	
}
