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
package org.apache.oodt.cas.workflow.server.action;

//JDK imports
import java.util.List;

//APACHE imports
import org.apache.commons.lang.StringUtils;

//OODT imports
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.query.parser.QueryParser;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;
import org.apache.oodt.cas.workflow.page.QueryPage;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Action for print out a page of reduced Workflow Instance Metadata
 * <p>
 */
public class PagedQuery extends WorkflowEngineServerAction {

	protected int pageNum;
	protected int pageSize;
	protected String query;
	protected List<String> termNames;
	
	public void performAction(WorkflowEngineClient weClient) throws Exception {
		QueryExpression queryExpression = QueryParser.parseQueryExpression(query);
		QueryPage page = weClient.getPage(new PageInfo(pageSize, pageNum), queryExpression);
		List<Metadata> metadata = weClient.getMetadata(page);
		System.out.println("Task Instance Metadata (Page: " + page.getPageNum() + "/" + page.getTotalPages() + "; Total: " + page.getNumOfHits() + ")");
		if (termNames != null) {
			for (Metadata met : metadata) {
				System.out.print("Metadata: (");
				StringBuffer sb = new StringBuffer("");
				for (String termName : this.termNames) {
					if (termName.startsWith("+")) {
						List<String> keys = met.getAllKeysWithName(termName.substring(1));
						if (keys.size() > 0) 
							for (String key : keys)
								sb.append(key + " = '" + StringUtils.join(met.getAllMetadata(key).iterator(), ",") + "', ");
					}else {
						sb.append(termName + " = '" + (!met.containsKey(termName) ? "null" : StringUtils.join(met.getAllMetadata(termName).iterator(), ",")) + "', ");
					}
				}
				if (sb.length() > 0)
					System.out.print(sb.substring(0, sb.length() - 2));
				System.out.println(")");
			}
		}else {
			for (Metadata met : metadata) {
				System.out.print("Metadata: (");
				StringBuffer sb = new StringBuffer("");
				for (String metKey : met.getAllKeys()) 
					sb.append(metKey + "=" + met.getAllMetadata(metKey).toString().replaceAll("[\\[\\]]", "'") + ", ");
				System.out.println(sb.substring(0, sb.length() - 2) + ")");
			}
		}
	}
	
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}
	 
	public void setQuery(String query) {
		this.query = query;
	}
	
	public void setReducedTerms(List<String> termNames) {
		this.termNames = termNames;
	}

}
