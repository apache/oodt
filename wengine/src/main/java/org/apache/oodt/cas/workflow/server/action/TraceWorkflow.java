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
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;
import org.apache.oodt.cas.workflow.exceptions.EngineException;
import org.apache.oodt.cas.workflow.instance.WorkflowConnectTaskInstance;
import org.apache.oodt.cas.workflow.processor.ProcessorSkeleton;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Trace workflows connected to workflow with given InstanceId
 * <p>
 */
public class TraceWorkflow extends WorkflowEngineServerAction {

	private String instanceId;
	public enum Mode { COMPLETE, RELATIVES, CHILDREN };
	private Mode mode;
	
	@Override
	public void performAction(WorkflowEngineClient weClient) throws Exception {
		System.out.println("Workflow Trace [InstanceId='" + this.instanceId + "']");
		if (this.mode.equals(Mode.COMPLETE) || this.mode.equals(Mode.RELATIVES)) {
			Vector<String> parentInstanceIds = new Vector<String>();
			String currentInstanceId = this.instanceId;
			String parentWorkflowInstanceId = null;
			do {
				if (parentWorkflowInstanceId != null) {
					currentInstanceId = parentWorkflowInstanceId;
					parentInstanceIds.add(parentWorkflowInstanceId);
				}
				parentWorkflowInstanceId = weClient.getWorkflowMetadata(currentInstanceId).getMetadata(WorkflowConnectTaskInstance.SPAWNED_BY_WORKFLOW);
			}while(parentWorkflowInstanceId != null);
			if (this.mode.equals(Mode.RELATIVES)) {
				ProcessorSkeleton parentSkeleton = null;
				String indent = "";
				for (String parentInstanceId : parentInstanceIds) {
					ProcessorSkeleton skeleton = weClient.getWorkflow(parentInstanceId);
					System.out.println(indent + " - InstanceId = '" + instanceId + "' : ModelId = '" + skeleton.getModelId() + "' : State = '" + skeleton.getState().getName() + "'" + (parentSkeleton != null ? " : SpawnedBy = '" + this.findSpawnedBy(parentSkeleton, parentInstanceId) + "'" : ""));
					parentSkeleton = skeleton;
					indent += "  ";
				}
				this.printTree(weClient, currentInstanceId, (parentSkeleton != null ? parentSkeleton.getModelId() : null), indent);
			}else if (this.mode.equals(Mode.COMPLETE)) {
				this.printTree(weClient, currentInstanceId, null, "");
			}			
		}else if (this.mode.equals(Mode.CHILDREN)){
			this.printTree(weClient, this.instanceId, null, "");
		}
	}
	
	private void printTree(WorkflowEngineClient weClient, String instanceId, String parentModelId, String indent) throws EngineException {
		ProcessorSkeleton skeleton = weClient.getWorkflow(instanceId);
		System.out.println(indent + " - InstanceId = '" + instanceId + "' : ModelId = '" + skeleton.getModelId() + "' : State = '" + skeleton.getState().getName() + "'" + (parentModelId != null ? " : SpawnedBy = '" + parentModelId + "'" : ""));
		Metadata metadata = weClient.getWorkflowMetadata(instanceId);
		List<String> spawnedWorkflows = metadata.getAllMetadata(WorkflowConnectTaskInstance.SPAWNED_WORKFLOWS);
		if (spawnedWorkflows != null) 
			for (String child : spawnedWorkflows) 
				this.printTree(weClient, child, this.findSpawnedBy(skeleton, child).getModelId(), indent + "  ");
	}
	
	private ProcessorSkeleton findSpawnedBy(ProcessorSkeleton skeleton, String spawnedInstanceId) {
		List<String> spawnedWorkflows = skeleton.getDynamicMetadata().getAllMetadata(WorkflowConnectTaskInstance.SPAWNED_WORKFLOWS);
		if (skeleton.getSubProcessors().isEmpty() && spawnedWorkflows.contains(spawnedInstanceId)) {
			return skeleton;
		}else {
			ProcessorSkeleton result = null;
			if (skeleton.getPreConditions() != null)
				result = findSpawnedBy(skeleton.getPreConditions(), spawnedInstanceId);
			if (result == null && skeleton.getPostConditions() != null)
				result = findSpawnedBy(skeleton.getPostConditions(), spawnedInstanceId);
			if (result == null) {
				for (ProcessorSkeleton ps : skeleton.getSubProcessors()) {
					result = findSpawnedBy(ps, spawnedInstanceId);
					if (result != null)
						break;
				}
			}
			return result;
		}
	}
	
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	public void setMode(String mode) {
		this.mode = Mode.valueOf(mode.toUpperCase());
	}

}
