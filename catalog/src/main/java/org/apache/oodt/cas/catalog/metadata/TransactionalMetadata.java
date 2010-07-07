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

package org.apache.oodt.cas.catalog.metadata;

//JDK imports
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

//OODT imports
import org.apache.oodt.cas.catalog.page.TransactionReceipt;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.system.CatalogService;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Metadata tied to a Transaction
 * <p>
 */
public class TransactionalMetadata {

	protected TransactionReceipt receipt;
	protected Metadata metadata;
	
	public TransactionalMetadata(TransactionReceipt receipt, Metadata metadata) {
		this.receipt = receipt;
		this.metadata = metadata;
		this.metadata.replaceMetadata(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY, this.receipt.getTransactionId().toString());
		this.metadata.replaceMetadata(CatalogService.CATALOG_IDS_MET_KEY, StringUtils.join(this.receipt.getCatalogIds().iterator(), ","));
	}

	public TransactionId<?> getTransactionId() {
		return receipt.getTransactionId();
	}

	public Set<String> getCatalogIds() {
		return receipt.getCatalogIds();
	}
	
	public Date getTransactionDate() {
		return receipt.getTransactionDate();
	}
	
	public Metadata getMetadata() {
		return metadata;
	}
	
	@Override
	public int hashCode() {
		return this.getTransactionId().hashCode();
	}
	
}
