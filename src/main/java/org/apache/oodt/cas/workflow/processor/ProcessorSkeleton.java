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
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.listener.WorkflowProcessorListener;
import org.apache.oodt.cas.workflow.priority.Priority;
import org.apache.oodt.cas.workflow.state.WorkflowState;

//JDK imports
import java.util.List;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Stripped down WorkflowProcessor mapping -- used to give state of WorkflowProcessor to 
 * client components without actually giving access to the WorkflowProcessor itself
 * </p>.
 */
public class ProcessorSkeleton {

	private String instanceId;
	private String modelId;
	private String modelName;
	private String executionType;
	private ProcessorInfo processorInfo;
	private int timesBlocked;
	
	private ProcessorSkeleton preConditions;
	private ProcessorSkeleton postConditions;
	private List<ProcessorSkeleton> subProcessors;
	private List<String> excusedSubProcessorIds;
	private WorkflowState state;
	private List<WorkflowProcessorListener> listeners;
	private Priority priority;
	private Metadata staticMetadata;    
	private Metadata dynamicMetadata;    

	public ProcessorSkeleton(ProcessorStub stub, Metadata staticMetadata, Metadata dynamicMetadata, List<String> excusedSubProcessorIds, List<WorkflowProcessorListener> listeners) {
		this.instanceId = stub.getInstanceId();
		this.modelId = stub.getModelId();
		this.modelName = stub.getModelName();
		this.executionType = stub.getExecutionType();
		this.processorInfo = stub.getProcessorInfo();
		this.timesBlocked = stub.getTimesBlocked();
		this.priority = stub.getPriority();
		this.state = stub.getState();
		this.staticMetadata = staticMetadata;
		this.dynamicMetadata = dynamicMetadata;
		this.excusedSubProcessorIds = excusedSubProcessorIds;
		this.listeners = listeners;
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

	public ProcessorInfo getProcessorInfo() {
		return this.processorInfo;
	}
	
	/* MODIFIABLE PROCESSOR PROPERTIES */
	
	public ProcessorSkeleton getPreConditions() {
		return preConditions;
	}

	public void setPreConditions(ProcessorSkeleton preConditions) {
		this.preConditions = preConditions;
	}

	public ProcessorSkeleton getPostConditions() {
		return postConditions;
	}

	public void setPostConditions(ProcessorSkeleton postConditions) {
		this.postConditions = postConditions;
	}

	public List<ProcessorSkeleton> getSubProcessors() {
		return subProcessors;
	}

	public void setSubProcessors(List<ProcessorSkeleton> subProcessors) {
		this.subProcessors = subProcessors;
	}

	public List<String> getExcusedSubProcessorIds() {
		return excusedSubProcessorIds;
	}

	public void setExcusedSubProcessorIds(List<String> excusedSubProcessorIds) {
		this.excusedSubProcessorIds = excusedSubProcessorIds;
	}

	public WorkflowState getState() {
		return state;
	}

	public void setState(WorkflowState state) {
		this.state = state;
	}

	public List<WorkflowProcessorListener> getListeners() {
		return listeners;
	}

	public void setListeners(List<WorkflowProcessorListener> listeners) {
		this.listeners = listeners;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Metadata getStaticMetadata() {
		return staticMetadata;
	}

	public void setStaticMetadata(Metadata staticMetadata) {
		this.staticMetadata = staticMetadata;
	}

	public Metadata getDynamicMetadata() {
		return dynamicMetadata;
	}

	public void setDynamicMetadata(Metadata dynamicMetadata) {
		this.dynamicMetadata = dynamicMetadata;
	}
	
	public int getTimesBlocked() {
		return this.timesBlocked;
	}

}
