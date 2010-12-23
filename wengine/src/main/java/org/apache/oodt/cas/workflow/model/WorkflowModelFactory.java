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
package org.apache.oodt.cas.workflow.model;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.priority.Priority;

//JDK imports
import java.util.List;
import java.util.Vector;
import java.util.UUID;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Factory for creating WorkflowModels
 * </p>.
 */
public class WorkflowModelFactory {

    protected String modelId;
    protected String modelName;
    protected String executionType;
    protected Priority priority;
    protected int minReqSuccessfulSubProcessors;
    protected String instanceClass;
    protected List<String> excusedSubProcessorIds;
    protected Metadata staticMetadata;
	
    public WorkflowModelFactory() {
    	this.priority = Priority.getDefault();
    	this.minReqSuccessfulSubProcessors = -1;
    }
    
    public WorkflowModelFactory(WorkflowModel model) {
    	this.modelId = model.getId();
    	this.modelName = model.getName();
    	this.executionType = model.getExecutionType();
    	this.priority = model.getPriority();
    	this.minReqSuccessfulSubProcessors = model.getMinReqSuccessfulSubProcessors();
    	this.instanceClass = model.getInstanceClass();
    	this.excusedSubProcessorIds = model.getExcusedSubProcessorIds();
    	this.staticMetadata = model.getStaticMetadata();
    }
    
	public WorkflowModel createModel() {
		return new WorkflowModel(this.modelId == null ? this.modelId = UUID.randomUUID().toString() : this.modelId, this.modelName == null ? this.modelId : this.modelName, this.executionType, this.instanceClass, this.priority, this.minReqSuccessfulSubProcessors, this.excusedSubProcessorIds == null ? new Vector<String>() : this.excusedSubProcessorIds, this.staticMetadata == null ? new Metadata() : this.staticMetadata);
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}
	
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public void setExecutionType(String executionType) {
		this.executionType = executionType;
	}
	
	public void setInstanceClass(String instanceClass) {
		this.instanceClass = instanceClass;
	}

	public void setStaticMetadata(Metadata staticMetadata) {
		this.staticMetadata = staticMetadata;
	}
	
	public void setPriority(Priority priority) {
		this.priority = priority;
	}
	
	public void setMinReqSuccessfulSubProcessors(int minReqSuccessfulSubProcessors) {
		this.minReqSuccessfulSubProcessors = minReqSuccessfulSubProcessors;
	}
	
	public void setExcusedSubProcessorIds(List<String> excusedSubProcessorIds) {
		this.excusedSubProcessorIds = excusedSubProcessorIds;
	}
	
}
