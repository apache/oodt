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
package org.apache.oodt.cas.workflow.event;

//OODT imports
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineLocal;
import org.apache.oodt.cas.workflow.page.QueuePage;
import org.apache.oodt.cas.workflow.processor.ProcessorStub;
import org.apache.oodt.cas.workflow.state.StateUtils;

/**
 * @author bfoster
 * @version $Revision$
 * 
 * Event for delete all workflows in a given state
 *
 */
public class DeleteWorkflowsByState extends WorkflowEngineEvent {
	
	private static final String STATE = "State";
	
	@Override
	public void performAction(WorkflowEngineLocal engine, Metadata inputMetadata)
			throws Exception {
		String state = inputMetadata.getMetadata(STATE);
		if (state == null)
			throw new Exception("Must set '" + STATE + "' metadata field!");
		try {
			QueuePage page = null; 
			do {
				page = engine.getPage(new PageInfo(50, PageInfo.FIRST_PAGE), StateUtils.getStateByName(engine.getSupportedStates(), state));
				for (ProcessorStub stub : page.getStubs()) 
					engine.deleteWorkflow(stub.getInstanceId());
			}while (!page.getPageInfo().isLastPage());		
		}catch (Exception e) {
			throw new Exception("Failed to delete workflows by state '" + state + "' : " + e.getMessage(), e);
		}
	}
	
}
