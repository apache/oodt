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

package org.apache.oodt.cas.catalog.page;

//JDK imports
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.catalog.struct.TransactionId;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Reciept created by performing a transaction with a CatalogService
 * <p>
 */
public class TransactionReceipt {

	protected TransactionId<?> transactionId;
	protected Set<String> catalogIds;
	protected Date transactionDate;
	protected Vector<CatalogReceipt> catalogReceipts;
	
	public TransactionReceipt(TransactionId<?> transactionId, List<CatalogReceipt> catalogReceipts) {
		this.transactionId = transactionId;
		this.catalogIds = new HashSet<String>();
		this.catalogReceipts = new Vector<CatalogReceipt>(catalogReceipts);
		for (CatalogReceipt catalogReceipt : catalogReceipts) {
			this.catalogIds.add(catalogReceipt.getCatalogId());
			if (this.transactionDate == null)
				this.transactionDate = catalogReceipt.getTransactionDate();
			else if (this.transactionDate.before(catalogReceipt.getTransactionDate()))
				this.transactionDate = catalogReceipt.getTransactionDate();
		}
	}

	public TransactionId<?> getTransactionId() {
		return this.transactionId;
	}
	
	public Set<String> getCatalogIds() {
		return this.catalogIds;
	}
	
	public Date getTransactionDate() {
		return this.transactionDate;
	}
	
	public List<CatalogReceipt> getCatalogReceipts() {
		return Collections.unmodifiableList(this.catalogReceipts);
	}
	
	public int hashCode() {
		return this.transactionId.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof TransactionReceipt) {
			return this.transactionId.equals(((TransactionReceipt) obj).transactionId);
		}else {
			return false;
		}
	}
	
	public String toString() {
		return this.transactionId + ":" + this.catalogIds;
	}
	
}
