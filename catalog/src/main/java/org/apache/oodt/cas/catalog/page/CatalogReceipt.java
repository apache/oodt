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

import java.util.Date;

import org.apache.oodt.cas.catalog.struct.TransactionId;

public class CatalogReceipt {

	protected TransactionId<?> transactionId;
	protected Date transactionDate;
	protected String catalogId;
	
	public CatalogReceipt(IngestReceipt ingestReceipt, String catalogId) {
		this.transactionId = ingestReceipt.getCatalogTransactionId();
		this.transactionDate = ingestReceipt.getTransactionDate();
		this.catalogId = catalogId;
	}
	
	public TransactionId<?> getTransactionId() {
		return this.transactionId;
	}

	public Date getTransactionDate() {
		return this.transactionDate;
	}

	public String getCatalogId() {
		return this.catalogId;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof CatalogReceipt) {
			CatalogReceipt compareTo = (CatalogReceipt) obj;
			return this.transactionId.equals(compareTo.transactionId) && this.transactionDate.equals(compareTo.transactionDate) && this.catalogId.equals(compareTo.catalogId);
		}else {
			return false;
		}
	}
	
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	public String toString() {
		return ("{CatalogReceipt(tID=" + this.transactionId + ",tDate=" + this.transactionDate + ",catID=" + this.catalogId + ")}");
	}
	
}
