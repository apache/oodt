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
package org.apache.oodt.cas.workflow.instance.repo;

//OODT imports
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.catalog.page.QueryPager;
import org.apache.oodt.cas.catalog.page.TransactionReceipt;
import org.apache.oodt.cas.catalog.query.ComparisonQueryExpression;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.system.CatalogService;
import org.apache.oodt.cas.catalog.term.Term;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.page.QueryPage;
import static org.apache.oodt.cas.workflow.metadata.WorkflowMetKeys.*;

//JDK imports
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * TaskInstance Metadata Repository which utilizes CAS-Catalog
 * </p>.
 */
public class WorkflowInstanceRepository {

	private CatalogService catalogService;
	
	public WorkflowInstanceRepository(CatalogService catalogService) {
		this.catalogService = catalogService;
	}
	
    public void storeInstanceMetadata(String jobId, Metadata metadata) throws Exception {
    	metadata.replaceMetadata(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY, jobId);
    	metadata.replaceMetadata(CatalogService.ENABLE_UPDATE_MET_KEY, "true");
    	this.catalogService.ingest(metadata);
    }

    public void removeInstanceMetadatas(String instanceId) throws Exception {
    	for (TransactionId<?> transId : this.getTransactionIdsByInstanceId(instanceId)) {
    		Metadata m = new Metadata();
    		m.replaceMetadata(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY, transId.toString());
    		this.catalogService.delete(m);
    	}
    }
    
    public void removeInstanceMetadata(String jobId) throws Exception {
		Metadata m = new Metadata();
		m.addMetadata(CatalogService.CATALOG_SERVICE_TRANSACTION_ID_MET_KEY, jobId);
		this.catalogService.delete(m);
    }
    
    public List<String> getJobIds(String instanceId) throws Exception {
    	List<String> jobIds = new Vector<String>();
    	for (TransactionId<?> transId : this.getTransactionIdsByInstanceId(instanceId))
    		jobIds.add(transId.toString());
    	return jobIds;
    }

    public Metadata getInstanceMetadata(String jobId) throws Exception {
		List<TransactionalMetadata> metadatas = this.catalogService.getMetadataFromTransactionIdStrings(Collections.singletonList(jobId));
		if (metadatas.size() > 0)
			return metadatas.get(0).getMetadata();
		else
			return new Metadata();
    }
    
	public QueryPage getNextPage(QueryPage page) throws Exception {
		return new QueryPage(this.catalogService.getNextPage(page));
	}
	
	public QueryPage getPage(PageInfo pageInfo, QueryExpression queryExpression) throws Exception {
    	return new QueryPage(this.catalogService.getPage(pageInfo, queryExpression));
	}
	
	public List<Metadata> getMetadata(QueryPage page) throws Exception {
    	List<Metadata> metadatas = new Vector<Metadata>();
    	for(TransactionalMetadata m : this.catalogService.getMetadata(page))
    		metadatas.add(m.getMetadata());
    	return metadatas;
	}
    
    protected List<TransactionId<?>> getTransactionIdsByInstanceId(String instanceId) throws Exception {
    	ComparisonQueryExpression query = new ComparisonQueryExpression();
    	query.setOperator(ComparisonQueryExpression.Operator.EQUAL_TO);
    	query.setTerm(new Term(INSTANCE_ID, Collections.singletonList(instanceId)));
    	QueryPager queryPager = this.catalogService.query(query);
    	Vector<TransactionId<?>> transIds = new Vector<TransactionId<?>>();
    	for (TransactionReceipt receipt: queryPager.getTransactionReceipts())
    		transIds.add(receipt.getTransactionId());
    	return transIds;
    }

}
