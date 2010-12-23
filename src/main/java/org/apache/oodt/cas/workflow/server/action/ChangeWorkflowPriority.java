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
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;
import org.apache.oodt.cas.workflow.priority.Priority;
import org.apache.oodt.cas.workflow.processor.ProcessorSkeleton;
import org.apache.oodt.cas.workflow.util.WorkflowUtils;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Action for change a workflow's priority
 * <p>
 */
public class ChangeWorkflowPriority extends WorkflowEngineServerAction {

	private String instanceId;
	private String modelId;
	private Priority priority;
	
	@Override
	public void performAction(WorkflowEngineClient weClient) throws Exception {
		if (this.priority == null) 
			throw new Exception("Priority '" + this.priority + "' must be specified");
		this.changePriority(weClient, WorkflowUtils.findSkeleton(weClient.getWorkflow(this.instanceId), this.modelId), this.priority);
	}
	
	private void changePriority(WorkflowEngineClient weClient, ProcessorSkeleton skeleton, Priority priority) throws Exception {
		weClient.setWorkflowPriority(skeleton.getInstanceId(), skeleton.getModelId(), priority);
		if (skeleton.getPreConditions() != null)
			this.changePriority(weClient, skeleton.getPreConditions(), priority);
		if (skeleton.getPostConditions() != null)
			this.changePriority(weClient, skeleton.getPostConditions(), priority);
		for (ProcessorSkeleton subProcessor : skeleton.getSubProcessors())
			this.changePriority(weClient, subProcessor, priority);
	}
	
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public void setPriority(double priority) {
		this.priority = Priority.getPriority(priority);
	}
	
}
