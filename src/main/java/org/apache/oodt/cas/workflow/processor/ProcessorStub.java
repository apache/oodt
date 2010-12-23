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
package org.apache.oodt.cas.workflow.processor;

//OODT imports
import org.apache.oodt.cas.workflow.priority.Priority;
import org.apache.oodt.cas.workflow.state.WorkflowState;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A cache of only the necessary variables of a workflow processor 
 * </p>.
 */
public class ProcessorStub {
	
	private String instanceId;
	private String modelId;
	private String modelName;
	private String executionType;
	private Priority priority;
	private WorkflowState state;
	private ProcessorInfo processorInfo;
	private int timesBlocked;
	
	public ProcessorStub(String instanceId, String modelId, String modelName, String executionType, Priority priority, WorkflowState state, ProcessorInfo processorInfo, int timesBlocked) {
		this.instanceId = instanceId;
		this.modelId = modelId;
		this.modelName = modelName;
		this.executionType = executionType;
		this.priority = priority;
		this.state = state;
		this.processorInfo = processorInfo;
		this.timesBlocked = timesBlocked;
	}
	
	public String getInstanceId() {
		return this.instanceId;
	}
	
	public String getModelId() {
		return this.modelId;
	}
	
	public String getModelName() {
		return this.modelName;
	}
	
	public String getExecutionType() {
		return this.executionType;
	}
	
	public Priority getPriority() {
		return this.priority;
	}
	
	public WorkflowState getState() {
		return this.state;
	}
	
	public ProcessorInfo getProcessorInfo() {
		return this.processorInfo;
	}
	
	public int getTimesBlocked() {
		return this.timesBlocked;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof ProcessorStub)
			return this.getInstanceId().equals(((ProcessorStub) obj).getInstanceId()) && this.getModelId().equals(((ProcessorStub) obj).getModelId());
		else
			return false;
	}
	
	public int hashCode() {
		return (this.getInstanceId() + ":" + this.getModelId()).hashCode();
	}
	
}
