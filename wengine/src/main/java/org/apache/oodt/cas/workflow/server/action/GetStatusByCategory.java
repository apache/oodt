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
import java.util.Arrays;

//OODT imports
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Action for printing out status of workflows by state
 * <p>
 */
public class GetStatusByCategory extends WorkflowEngineServerAction {
	
	private int pageSize = 5;
	private boolean showMessage = false;

	@Override
	public void performAction(WorkflowEngineClient weClient) throws Exception {
		System.out.println("\nTotal Number of Workflows: " + weClient.getNumOfWorkflows() + "\n");
		for (String category : Arrays.asList("RUNNING", "WAITING", "TRANSITION", "DONE", "HOLDING")) {
			GetPage action = new GetPage();
			action.setPageNum(1);
			action.setPageSize(pageSize);
			action.setCategory(category);
			action.showMessage(this.showMessage);
			action.performAction(weClient);
		}
	}
	
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void showMessage(boolean showMessage) {
		this.showMessage = showMessage;
	}
	
}
