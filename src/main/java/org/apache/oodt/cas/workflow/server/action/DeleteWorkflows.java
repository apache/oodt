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

//OODT imports
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;
import org.apache.oodt.cas.workflow.page.QueuePage;
import org.apache.oodt.cas.workflow.processor.ProcessorStub;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Action for deleting workflows in a given category
 * <p>
 */
public class DeleteWorkflows extends FilteredAction {

	@Override
	public void performAction(WorkflowEngineClient weClient) throws Exception {
		try {
			QueuePage page = null; 
			DeleteWorkflow deleteAction = new DeleteWorkflow();
			do {
				page = weClient.getPage(new PageInfo(50, PageInfo.FIRST_PAGE), this.createFilter(weClient));
				for (ProcessorStub stub : page.getStubs()) {
					deleteAction.setInstanceId(stub.getInstanceId());
					deleteAction.performAction(weClient);
				}			
			}while (!page.getPageInfo().isLastPage());
		}catch (Exception e) {
			throw new Exception("Failed to delete workflows by category '" + categoryName + "' : " + e.getMessage(), e);
		}
	}
	
}
