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
package org.apache.oodt.cas.catalog.server.action;

//JDK imports
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
import org.apache.oodt.cas.catalog.page.QueryPager;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.query.parser.QueryParser;
import org.apache.oodt.cas.catalog.system.impl.CatalogServiceClient;


/**
 * @author bfoster
 * @version $Revision$
 *
 */
public class QueryServerAction extends CatalogServiceServerAction {

	protected String query;
	protected Set<String> catalogIds;
	
	public void performAction(CatalogServiceClient csClient) throws Exception {
		QueryExpression queryExpression = QueryParser.parseQueryExpression(query);
		QueryPager queryPager = null;
		if (catalogIds == null) 
			queryPager = csClient.query(queryExpression);
		else
			queryPager = csClient.query(queryExpression, catalogIds);
		List<TransactionalMetadata> transactionMetadatas = csClient.getAllPages(queryPager);
		for (TransactionalMetadata tMet : transactionMetadatas) {
			System.out.print("ID: " + tMet.getTransactionId() + " ; CatalogIDs: " + tMet.getCatalogIds() + " ; Metadata: (");
			StringBuffer sb = new StringBuffer("");
			for (Object metKey : tMet.getMetadata().getHashtable().keySet()) {
				sb.append(metKey + "=" + tMet.getMetadata().getAllMetadata((String) metKey).toString().replaceAll("[\\[\\]]", "'") + ", ");
			}
			System.out.println(sb.substring(0, sb.length() - 2) + ")");
		}
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public void setCatalogIds(List<String> catalogIds) {
		this.catalogIds = new HashSet<String>(catalogIds);
	}

}
