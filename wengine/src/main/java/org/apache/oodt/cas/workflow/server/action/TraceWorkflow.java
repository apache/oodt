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
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;
import org.apache.oodt.cas.workflow.exceptions.EngineException;
import org.apache.oodt.cas.workflow.instance.WorkflowConnectTaskInstance;

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
			Vector<String> parents = new Vector<String>();
			String currentInstanceId = this.instanceId;
			String parentWorkflowInstanceId = null;
			do {
				if (parentWorkflowInstanceId != null) {
					currentInstanceId = parentWorkflowInstanceId;
					parents.add(parentWorkflowInstanceId);
				}
				parentWorkflowInstanceId = weClient.getWorkflowMetadata(currentInstanceId).getMetadata(WorkflowConnectTaskInstance.SPAWNED_BY_WORKFLOW);
			}while(parentWorkflowInstanceId != null);
			if (this.mode.equals(Mode.RELATIVES)) {
				String index = "";
				for (String parent : parents) {
					System.out.println(index + " - InstanceId = '" + parent + " : State = '" + weClient.getWorkflowState(parent).getName() + "'");
					index += "  ";
				}
				this.printTree(weClient, currentInstanceId, index);
			}else if (this.mode.equals(Mode.COMPLETE)) {
				this.printTree(weClient, currentInstanceId, "");
			}			
		}else if (this.mode.equals(Mode.CHILDREN)){
			this.printTree(weClient, this.instanceId, "");
		}
	}
	
	private void printTree(WorkflowEngineClient weClient, String instanceId, String indent) throws EngineException {
		System.out.println(indent + " - InstanceId = '" + instanceId + "' : State = '" + weClient.getWorkflowState(instanceId).getName() + "'");
		Metadata metadata = weClient.getWorkflowMetadata(instanceId);
		for (String child : metadata.getAllMetadata(WorkflowConnectTaskInstance.SPAWNED_WORKFLOWS))
			this.printTree(weClient, child, indent + "  ");
	}
	
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	public void setMode(String mode) {
		this.mode = Mode.valueOf(mode);
	}

}
