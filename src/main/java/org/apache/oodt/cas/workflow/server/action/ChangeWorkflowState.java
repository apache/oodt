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
import org.apache.oodt.cas.workflow.processor.ProcessorSkeleton;
import org.apache.oodt.cas.workflow.state.StateUtils;
import org.apache.oodt.cas.workflow.state.WorkflowState;
import org.apache.oodt.cas.workflow.util.WorkflowUtils;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Action for change a workflow's state
 * <p>
 */
public class ChangeWorkflowState extends WorkflowEngineServerAction {

	private String instanceId;
	private String modelId;
	private String state;
	private String message;
	private boolean recur;
	
	public ChangeWorkflowState() {
		this.message = "State Changed By User '" + System.getProperty("user.name", "unknown") + "'";
		this.recur = false;
	}
	
	@Override
	public void performAction(WorkflowEngineClient weClient) throws Exception {
		WorkflowState selectedState = StateUtils.getStateByName(weClient.getSupportedStates(), this.state);
		if (selectedState == null) 
			throw new Exception("State '" + this.state + "' is not supported by server");
		
		WorkflowState newState = selectedState.getClass().getConstructor(String.class).newInstance(this.message);
		if (this.recur) 
			this.changeState(weClient, WorkflowUtils.findSkeleton(weClient.getWorkflow(instanceId), modelId), newState);
		else
			weClient.setWorkflowState(instanceId, modelId, newState);
	}
	
	private void changeState(WorkflowEngineClient weClient, ProcessorSkeleton skeleton, WorkflowState newState) throws Exception {
		weClient.setWorkflowState(skeleton.getInstanceId(), skeleton.getModelId(), newState);
		if (skeleton.getPreConditions() != null)
			this.changeState(weClient, skeleton.getPreConditions(), newState);
		if (skeleton.getPostConditions() != null)
			this.changeState(weClient, skeleton.getPostConditions(), newState);
		for (ProcessorSkeleton subProcessor : skeleton.getSubProcessors())
			this.changeState(weClient, subProcessor, newState);
	}
	
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setRecur(boolean recur) {
		this.recur = recur;
	}
	
}
