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
package org.apache.oodt.cas.workflow.instance;

//JDK imports
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Calendar;

//OODT imports
import org.apache.oodt.commons.date.DateUtils;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;
import org.apache.oodt.cas.workflow.exceptions.EngineException;
import org.apache.oodt.cas.workflow.metadata.ControlMetadata;
import org.apache.oodt.cas.workflow.metadata.WorkflowMetKeys;
import org.apache.oodt.cas.workflow.processor.ProcessorInfo;
import org.apache.oodt.cas.workflow.state.WorkflowState;
import org.apache.oodt.cas.workflow.state.done.FailureState;
import org.apache.oodt.cas.workflow.state.holding.UnknownState;
import org.apache.oodt.cas.workflow.state.results.ResultsBailState;
import org.apache.oodt.cas.workflow.state.results.ResultsFailureState;
import org.apache.oodt.cas.workflow.state.results.ResultsState;
import org.apache.oodt.cas.workflow.state.results.ResultsSuccessState;
import org.apache.oodt.cas.workflow.state.running.ExecutingState;
import org.apache.oodt.cas.workflow.state.transition.ExecutionCompleteState;
import org.apache.oodt.cas.workflow.state.waiting.BlockedState;
import org.apache.oodt.cas.workflow.util.WorkflowUtils;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 *	Base TaskInstance which auto handles communicating metadata and state 
 *	back to the WorkflowEngine from which it came.
 *
 */
public abstract class TaskInstance {
	
	private static final Logger LOG = Logger.getLogger(TaskInstance.class.getName());
	
	private String jobId;
	private String instanceId;
	private String modelId;
	private WorkflowEngineClient weClient;
	private Metadata dynamicMetadata;
	private Metadata staticMetadata;
	private WorkflowState state;
	
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	
	public String getJobId() {
		return this.jobId;
	}
	
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	public String getInstanceId() {
		return this.instanceId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}
	
	public String getModelId() {
		return this.modelId;
	}
	
	public ProcessorInfo getProcessorInfo() throws EngineException {
		return this.weClient.getProcessorInfo(this.instanceId, this.modelId);
	}
	
	public WorkflowState getState() {
		return this.state;
	}
	
	public void setNotifyEngine(WorkflowEngineClient weClient) {
		this.weClient = weClient;
	}
	
	public void setDynamicMetadata(Metadata dynamicMetadata) {
		this.dynamicMetadata = new Metadata(dynamicMetadata);
	}
	
	public void setStaticMetadata(Metadata staticMetadata) {
		this.staticMetadata = staticMetadata;
	}
	
	public Metadata getMetadata() {
		return new ControlMetadata(this.staticMetadata, this.dynamicMetadata).asMetadata();
	}
    
    private void syncWorkflowMetadata(ControlMetadata ctrlMetadata) {
    	try {
			this.weClient.updateWorkflowMetadata(this.instanceId, this.modelId, ctrlMetadata.asMetadata(ControlMetadata.DYN));
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to update workflow metadata for jobId = " + this.jobId + " ; modelId = " + this.modelId + " : " + e.getMessage(), e);
		}
    }
	
    private void syncInstanceMetadata(ControlMetadata ctrlMetadata) {
    	try {
    		this.weClient.updateInstanceMetadata(this.jobId, ctrlMetadata.asMetadata());
    	}catch (Exception e) {
    		LOG.log(Level.SEVERE, "Failed to update instance metadata for jobId = " + this.jobId + " ; modelId = " + this.modelId + " : " + e.getMessage(), e);
    	}
    }

    private void syncWorkflowAndInstanceMetadata(Metadata metadata, Metadata instanceMetadata) {
    	try {
			this.weClient.updateWorkflowAndInstance(this.instanceId, this.modelId, this.state, metadata, this.jobId, instanceMetadata);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to update workflow and instance metadata for jobId = " + this.jobId + " ; modelId = " + this.modelId + " : " + e.getMessage(), e);
		}
    }
    
    private void syncState(WorkflowState workflowState) throws EngineException {
		this.weClient.setWorkflowState(this.instanceId, this.modelId, workflowState);
    }
    
    private void synchWithEngine(WorkflowState workflowState, ControlMetadata ctrlMetadata) {
    	try {
    		Metadata metadata = ctrlMetadata.asMetadata(ControlMetadata.DYN);
	    	this.addStandardInstanceMetadata(workflowState, ctrlMetadata);
	    	Metadata instanceMetadata = ctrlMetadata.asMetadata();
	    	this.syncWorkflowAndInstanceMetadata(metadata, instanceMetadata);
    	}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to updated instance state and metadata : " + e.getMessage(), e);
		}
    }
    
    private void addStandardInstanceMetadata(WorkflowState workflowState, ControlMetadata ctrlMetadata) throws Exception {
    	ctrlMetadata.replaceLocalMetadata(WorkflowMetKeys.JOB_ID, this.jobId);
    	ctrlMetadata.replaceLocalMetadata(WorkflowMetKeys.INSTANCE_ID, this.instanceId);
    	ctrlMetadata.replaceLocalMetadata(WorkflowMetKeys.MODEL_ID, this.modelId);
    	ctrlMetadata.replaceLocalMetadata(WorkflowMetKeys.STATE, workflowState.getName());
    	ctrlMetadata.replaceLocalMetadata(WorkflowMetKeys.HOST, WorkflowUtils.getHostName());
    	ProcessorInfo processorInfo = this.getProcessorInfo();
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(processorInfo.getCreationDate());
    	ctrlMetadata.replaceLocalMetadata(WorkflowMetKeys.CREATION_DATE, DateUtils.toString(DateUtils.toUtc(calendar)));
    	calendar.setTime(processorInfo.getReadyDate());
    	ctrlMetadata.replaceLocalMetadata(WorkflowMetKeys.READY_DATE, DateUtils.toString(DateUtils.toUtc(calendar)));
    	if (processorInfo.getExecutionDate() != null) {
        	calendar.setTime(processorInfo.getExecutionDate());
        	ctrlMetadata.replaceLocalMetadata(WorkflowMetKeys.EXECUTION_DATE, DateUtils.toString(DateUtils.toUtc(calendar)));
    	}
    	if (processorInfo.getCompletionDate() != null) {
    		calendar.setTime(processorInfo.getCompletionDate());
    		ctrlMetadata.replaceLocalMetadata(WorkflowMetKeys.COMPLETION_DATE, DateUtils.toString(DateUtils.toUtc(calendar)));
    	}
    }
    
    protected void update(ExecutingState workflowState, ControlMetadata ctrlMetadata) {
    	this.synchWithEngine(workflowState, ctrlMetadata);
    }
    
    public void execute() {
    	ControlMetadata ctrlMetadata = new ControlMetadata(this.staticMetadata, this.dynamicMetadata);
    	this.state = new ExecutingState("");
    	
    	try {
    		this.synchWithEngine(this.state, ctrlMetadata);
        	ResultsState resultsState = this.performExecution(ctrlMetadata);
        	if (resultsState instanceof ResultsSuccessState) {
        		this.state = new ExecutionCompleteState("Successfully Executed Task [ModelId='" + this.modelId + "',JobId='" + this.jobId + "'] : " + resultsState.getMessage());
        	}else if (resultsState instanceof ResultsFailureState) {
        		this.state = new FailureState("Failed to Execute Task [ModelId='" + this.modelId + "',JobId='" + this.jobId + "'] : " + resultsState.getMessage());
        	}else if (resultsState instanceof ResultsBailState) {
        		this.state = new BlockedState("Task Bailed : " + resultsState.getMessage());
        	}else {
        		this.state = new UnknownState("Task [ModelId='" + this.modelId + "',JobId='" + this.jobId + "'] returned unsupported ResultState '" + resultsState + "' : " + resultsState.getMessage());
        	}
    	}catch (Exception e) {
    		this.state = new FailureState("Execution of Task [ModelId='" + this.modelId + "',JobId='" + this.jobId + "'] failed : " + e.getMessage());
    		LOG.log(Level.SEVERE, "Execution of Task [InstanceId='" + this.instanceId + "',ModelId='" + this.modelId + "',JobId='" + this.jobId + "'] failed : " + e.getMessage(), e);
    	}catch (Error e) {
    		this.state = new FailureState("Execution of Task (Fatal Error!!!) [ModelId='" + this.modelId + "',JobId='" + this.jobId + "'] failed : " + e.getMessage());
    		LOG.log(Level.SEVERE, "Execution of Task (Fatal Error!!!) [InstanceId='" + this.instanceId + "',ModelId='" + this.modelId + "',JobId='" + this.jobId + "'] failed : " + e.getMessage(), e);
    	}finally {
    		ctrlMetadata.commitWorkflowMetadataKeys();
    		this.synchWithEngine(this.state, ctrlMetadata);
    	}
    }
    
    protected abstract ResultsState performExecution(ControlMetadata crtlMetadata);

}
