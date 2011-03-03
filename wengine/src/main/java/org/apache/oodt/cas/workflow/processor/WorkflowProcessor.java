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

//JDK imports
import java.util.Collections;
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.listener.ChangeType;
import org.apache.oodt.cas.workflow.listener.WorkflowProcessorListener;
import org.apache.oodt.cas.workflow.priority.Priority;
import org.apache.oodt.cas.workflow.state.RevertableWorkflowState;
import org.apache.oodt.cas.workflow.state.StateUtils;
import org.apache.oodt.cas.workflow.state.WorkflowState;
import org.apache.oodt.cas.workflow.state.done.FailureState;
import org.apache.oodt.cas.workflow.state.done.SuccessState;
import org.apache.oodt.cas.workflow.state.holding.UnknownState;
import org.apache.oodt.cas.workflow.state.initial.NullState;
import org.apache.oodt.cas.workflow.state.results.ResultsBailState;
import org.apache.oodt.cas.workflow.state.results.ResultsFailureState;
import org.apache.oodt.cas.workflow.state.results.ResultsState;
import org.apache.oodt.cas.workflow.state.results.ResultsSuccessState;
import org.apache.oodt.cas.workflow.state.running.ExecutingState;
import org.apache.oodt.cas.workflow.state.running.PostConditionEvalState;
import org.apache.oodt.cas.workflow.state.running.PreConditionEvalState;
import org.apache.oodt.cas.workflow.state.transition.ExecutionCompleteState;
import org.apache.oodt.cas.workflow.state.transition.PreConditionSuccessState;
import org.apache.oodt.cas.workflow.state.waiting.BlockedState;
import org.apache.oodt.cas.workflow.state.waiting.QueuedState;
import org.apache.oodt.cas.workflow.state.waiting.WaitingOnResourcesState;
	
/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Abstract WorkflowProcessor
 * </p>.
 */
public abstract class WorkflowProcessor implements WorkflowProcessorListener, Comparable<WorkflowProcessor> {

	public static final String LOCAL_KEYS_GROUP = "WorkflowProcessor/Local";
	
	private String instanceId;
	private String modelId;
	private String modelName;
	private String executionType;
	private List<String> excusedSubProcessorIds;
	private WorkflowState state;
	private Vector<WorkflowProcessor> subProcessors;
	private Vector<WorkflowProcessorListener> listeners;
	private WorkflowProcessor preConditions;
	private WorkflowProcessor postConditions;
	private ProcessorInfo processorInfo;
	private Priority priority;
	private int minReqSuccessfulSubProcessors;
	private Metadata staticMetadata;    
	private Metadata dynamicMetadata;    
    private boolean isConditionProcessor;
	private int timesBlocked;
	
	public WorkflowProcessor() {
		this.state = new NullState();
		this.listeners = new Vector<WorkflowProcessorListener>();
		this.processorInfo = new ProcessorInfo();
		this.staticMetadata = new Metadata();
		this.dynamicMetadata = new Metadata();
		this.excusedSubProcessorIds = new Vector<String>();
		this.minReqSuccessfulSubProcessors = -1;
		this.isConditionProcessor = false;
		this.timesBlocked = 0;
	}
	
	public void setIsConditionProcessor(boolean isConditionProcessor) {
		this.isConditionProcessor = isConditionProcessor;
	}
	
	public boolean isConditionProcessor() {
		return this.isConditionProcessor;
	}
	
	public ProcessorStub getStub() {
		return new ProcessorStub(this.instanceId, this.modelId, this.modelName, this.executionType, this.priority, this.state, this.processorInfo, this.timesBlocked);
	}
	
	public void setInstanceId(String instanceId) {
		if (this.instanceId == null)
			this.instanceId = instanceId;
	}
	
	public String getInstanceId() {
		return this.instanceId;
	}
 
	public void setModelId(String modelId) {
		if (this.modelId == null) {
			this.modelId = modelId;
			if (this.modelName == null)
				this.modelName = this.modelId;
		}
	}
	
	public String getModelId() {
		return this.modelId;
	}
	
	public void setModelName(String modelName) {
		if (modelName != null && (this.modelName == null || this.modelId.equals(this.modelName)))
			this.modelName = modelName;
	}
	
	public String getModelName() {
		return this.modelName;
	}
	
	public int getTimesBlocked() {
		return this.timesBlocked;
	}
	
	public void setExecutionType(String executionType) {
		if (this.executionType == null)
			this.executionType = executionType;
	}
	
	public String getExecutionType() {
		return this.executionType;
	}
	
	public List<String> getExcusedSubProcessorIds() {
		return excusedSubProcessorIds;
	}

	public void setExcusedSubProcessorIds(List<String> excusedSubProcessorIds) {
		if (excusedSubProcessorIds != null) {
			this.excusedSubProcessorIds = new Vector<String>(excusedSubProcessorIds);
			this.notifyListenersOfChange(ChangeType.EXCUSED_WPS);
		}
	}

	public void setPriority(Priority priority) {
		if (priority != null) {
			this.priority = priority;
			this.notifyListenersOfChange(ChangeType.PRIORITY);
		}
	}
	
	public void setPriorityRecur(Priority priority) {
		this.setPriority(priority);
    	for (WorkflowProcessor wp : this.getSubProcessors())
    		wp.setPriorityRecur(priority);
    	if (this.preConditions != null)
    		this.preConditions.setPriorityRecur(priority);
    	if (this.postConditions != null)
    		this.postConditions.setPriorityRecur(priority);
	}
	
	public Priority getPriority() {
		return this.priority;
	}
	
	public void setMinReqSuccessfulSubProcessors(int minReqSuccessfulSubProcessors) {
		this.minReqSuccessfulSubProcessors = minReqSuccessfulSubProcessors;
	}
	
	public int getMinReqSuccessfulSubProcessors() {
		return this.minReqSuccessfulSubProcessors;
	}
	
	public void setStaticMetadata(Metadata staticMetadata) {
		if (staticMetadata != null) {
			this.staticMetadata = new Metadata(staticMetadata);
			this.notifyListenersOfChange(ChangeType.STATIC_MET);
		}
	}
	
	public void setStaticMetadataRecur(Metadata staticMetadata) {
		if (staticMetadata != null) {
			for (WorkflowProcessor subProcessor : this.getSubProcessors())
				subProcessor.setStaticMetadataRecur(staticMetadata);
			if (this.getPreConditions() != null)
				this.getPreConditions().setStaticMetadataRecur(staticMetadata);
			if (this.getPostConditions() != null)
				this.getPostConditions().setStaticMetadataRecur(staticMetadata);
			this.setStaticMetadata(staticMetadata);
		}
	}
	
	public Metadata getStaticMetadata() {
		return this.staticMetadata;
	}
	
	public synchronized void setDynamicMetadata(Metadata dynamicMetadata) {
		if (dynamicMetadata != null) {
			this.dynamicMetadata = new Metadata(dynamicMetadata);
			this.notifyListenersOfChange(ChangeType.DYN_MET);
		}
	}
	
	public synchronized void setDynamicMetadataRecur(Metadata dynamicMetadata) {
		if (dynamicMetadata != null) {
			for (WorkflowProcessor subProcessor : this.getSubProcessors())
				subProcessor.setDynamicMetadataRecur(dynamicMetadata);
			if (this.getPreConditions() != null)
				this.getPreConditions().setDynamicMetadataRecur(dynamicMetadata);
			if (this.getPostConditions() != null)
				this.getPostConditions().setDynamicMetadataRecur(dynamicMetadata);
			this.setDynamicMetadata(dynamicMetadata);
		}
	}
	
	protected void setDynamicMetadataRecurOnlySubProcessors(Metadata dynamicMetadata) {
		if (dynamicMetadata != null) {
			for (WorkflowProcessor subProcessor : this.getSubProcessors())
				subProcessor.setDynamicMetadataRecur(dynamicMetadata);
			this.setDynamicMetadata(dynamicMetadata);
		}
	}
	
	public synchronized Metadata getDynamicMetadata() {
		return this.dynamicMetadata;
	}
	
	public synchronized Metadata getPassThroughDynamicMetadata() {
		Metadata passThroughMet = new Metadata(this.dynamicMetadata);
		passThroughMet.removeMetadataGroup(LOCAL_KEYS_GROUP);
		return passThroughMet;
	}
	
	public void setProcessorInfo(ProcessorInfo processorInfo) {
		this.processorInfo = processorInfo;
	}
	
	public ProcessorInfo getProcessorInfo() {
		return this.processorInfo;
	}
	
    public WorkflowProcessor getPreConditions() {
		return this.preConditions;
	}

	public void setPreConditions(WorkflowProcessor preConditions) {
		if (this.preConditions == null) {
			this.preConditions = preConditions;
			this.preConditions.registerListener(this);
		}
	}

	public WorkflowProcessor getPostConditions() {
		return this.postConditions;
	}

	public void setPostConditions(WorkflowProcessor postConditions) {
		if (this.postConditions == null) {
			this.postConditions = postConditions;
			this.postConditions.registerListener(this);
		}
	}
    
    public List<WorkflowProcessor> getSubProcessors() {
    	return Collections.unmodifiableList(this.subProcessors == null ? new Vector<WorkflowProcessor>() : this.subProcessors);
    }
    
	public void setSubProcessors(List<WorkflowProcessor> subProcessors) {
		if (this.subProcessors == null) {
			if (subProcessors == null) {
				this.subProcessors = new Vector<WorkflowProcessor>();
			}else {
				this.subProcessors = new Vector<WorkflowProcessor>(subProcessors);
				for (WorkflowProcessor subProcessor : this.subProcessors)
					subProcessor.registerListener(this);
			}
		}
    }
	
	public void revertState() {
		if (this.getState() instanceof RevertableWorkflowState) {
			this.state = ((RevertableWorkflowState) this.state).getPrevState();
		}
	}
    
    public synchronized WorkflowState getState() {
    	return this.state;
    }
    
    public synchronized void setState(WorkflowState state) {
    	if (state != null) {
        	if (!(this.getState() instanceof BlockedState) && state instanceof BlockedState) { 
        		timesBlocked++;
        	}else if (state instanceof ExecutionCompleteState) {
        		if (this.passedPostConditions())
    				state = new SuccessState("No PostConditions; successfully completed : " + state.getMessage());
        		else if (this.getPostConditions() != null)
            		this.getPostConditions().setDynamicMetadataRecur(this.getDynamicMetadata());
        	}
    		
	    	if (state instanceof WaitingOnResourcesState && ((WaitingOnResourcesState) state).getPrevState() instanceof ExecutingState) {
	    		this.processorInfo.markReadyDate();
	    	}else if (state instanceof ExecutingState) {
	    		this.processorInfo.markExecutionDate();
	    	}else if (state.getCategory().equals(WorkflowState.Category.DONE)) {
	    		this.processorInfo.markCompletionDate();
	    	}
    	
    		WorkflowState currentState = this.getState();
    		this.state = state;
    		if (!this.getState().equals(currentState))
    			this.notifyListenersOfChange(ChangeType.STATE);
    	}
    }
    
    public void setStateRecur(WorkflowState state) {
    	this.setState(state);
    	for (WorkflowProcessor wp : this.getSubProcessors())
    		wp.setStateRecur(state);
    	if (this.preConditions != null)
    		this.preConditions.setStateRecur(state);
    	if (this.postConditions != null)
    		this.postConditions.setStateRecur(state);
    }

    public List<WorkflowProcessorListener> getListeners() {
    	return Collections.unmodifiableList(this.listeners);
    }
    
    public void registerListenerRecur(WorkflowProcessorListener listener) {
    	for (WorkflowProcessor wp : this.getSubProcessors())
    		wp.registerListenerRecur(listener);
    	if (this.preConditions != null)
    		this.preConditions.registerListenerRecur(listener);
    	if (this.postConditions != null)
    		this.postConditions.registerListenerRecur(listener);
    	this.registerListener(listener);
    }
    
    public void registerListener(WorkflowProcessorListener listener) {
    	this.listeners.add(listener);
    	this.notifyListenersOfChange(ChangeType.LISTENERS);
    }
    
    public void deregisterListenerRecur(WorkflowProcessorListener listener) {
    	for (WorkflowProcessor wp : this.getSubProcessors())
    		wp.deregisterListenerRecur(listener);
    	if (this.preConditions != null)
    		this.preConditions.deregisterListenerRecur(listener);
    	if (this.postConditions != null)
    		this.postConditions.deregisterListenerRecur(listener);
    	this.deregisterListener(listener);
    }
    
    public void deregisterListener(WorkflowProcessorListener listener) {
    	this.listeners.remove(listener);
    	this.notifyListenersOfChange(ChangeType.LISTENERS);
    }
         
    public synchronized ProcessorSkeleton getSkeleton() {
    	ProcessorSkeleton ps = new ProcessorSkeleton(this.getStub(), new Metadata(this.staticMetadata), new Metadata(this.dynamicMetadata), new Vector<String>(this.excusedSubProcessorIds), new Vector<WorkflowProcessorListener>(this.listeners));
    	
    	Vector<ProcessorSkeleton> subSkeletons = new Vector<ProcessorSkeleton>();
    	for (WorkflowProcessor wp : this.getSubProcessors())
    		subSkeletons.add(wp.getSkeleton());
    	ps.setSubProcessors(subSkeletons);
    	if (this.getPreConditions() != null)
    		ps.setPreConditions(this.getPreConditions().getSkeleton());
    	if (this.getPostConditions() != null)
    		ps.setPostConditions(this.getPostConditions().getSkeleton());

    	return ps;
    }
    
    public synchronized List<TaskProcessor> getRunnableWorkflowProcessors() {
    	Vector<TaskProcessor> runnableTasks = new Vector<TaskProcessor>();

    	// evaluate pre-conditions
    	if (!this.passedPreConditions()) { //this.getPreConditions() != null && (this.getState() instanceof QueuedState || this.getState() instanceof PreConditionEvalState || (this.getState() instanceof BlockedState && this.getPreConditions().getState() instanceof BlockedState))) {
    		for (WorkflowProcessor subProcessor : this.getPreConditions().getRunnableSubProcessors()) {
				for (TaskProcessor tp : subProcessor.getRunnableWorkflowProcessors()) {
					runnableTasks.add(tp);
				}
    		}	
    	
    	}else if (this.isDone() instanceof ResultsFailureState) {
    		//do nothing -- this workflow failed!!!
    		
        // if not done, ask all sub-processors for their runnable sub-processors
    	}else if (this.isDone() instanceof ResultsBailState) {//!this.getState().getCategory().equals(WorkflowState.Category.DONE)) {
			for (WorkflowProcessor subProcessor : this.getRunnableSubProcessors()) 
				runnableTasks.addAll(subProcessor.getRunnableWorkflowProcessors());

			// evaluate post-conditions
    	}else if (!this.passedPostConditions()) { //this.getPostConditions() != null && (this.getState() instanceof ExecutionCompleteState || this.getState() instanceof PostConditionEvalState || (this.getState() instanceof BlockedState && this.getPostConditions().getState() instanceof BlockedState))) {
    		for (WorkflowProcessor subProcessor : this.getPostConditions().getRunnableSubProcessors()) {
				for (TaskProcessor tp : subProcessor.getRunnableWorkflowProcessors()) {
					runnableTasks.add(tp);
				}
    		}
    	
    	}
    	
	    return runnableTasks;
    }

	public synchronized void notifyChange(WorkflowProcessor processor, ChangeType changeType) {
		if (changeType.equals(ChangeType.STATE)) {
			
			// IF NOT A CONDTION PROCESSOR
			if (!this.isConditionProcessor()) {

				// Queued
				if (this.getState() instanceof QueuedState) {
					
					// IF NOTIFICATION CAME FROM PRE-CONDITION
					if (this.getPreConditions() != null && processor.isConditionProcessor() && this.getPreConditions().getModelId().equals(processor.getModelId())) {
						
						if (processor.getState() instanceof ExecutingState)
							this.setState(new PreConditionEvalState("Executing Preconditions"));
							
						else if (processor.getState() instanceof WaitingOnResourcesState) 
							this.setState(new WaitingOnResourcesState("", new PreConditionEvalState("Executing Preconditions")));

						else if (processor.getState() instanceof FailureState)  
								this.setState(new FailureState("Failed to pass Preconditions : " + processor.getState().getMessage()));
						
						else if (processor.getState() instanceof BlockedState)  
							this.setState(new BlockedState("Preconditions are blocked : " + processor.getState().getMessage()));
					
					// IF NOTIFICATION CAME FROM SUB-PROCESSOR
					}else if (this.passedPreConditions() && !processor.isConditionProcessor()) {
													
						if (processor.getState() instanceof PreConditionEvalState || processor.getState() instanceof ExecutingState)
							this.setState(new ExecutingState("Executing Sub-Processors"));
					
						else if (processor.getState() instanceof PreConditionSuccessState || processor.getState() instanceof WaitingOnResourcesState) 
							this.setState(new WaitingOnResourcesState("", new ExecutingState("Executing Sub-Processors")));

						else if (processor.getState() instanceof BlockedState && this.isBlocked()) 
							this.setState(new BlockedState("Sub-processors are blocked : " + processor.getState().getMessage()));
						
					// IF NOTIFICATION CAME FROM POST-CONDITION
					}else if (this.getPostConditions() != null && processor.isConditionProcessor() && this.getPostConditions().getModelId().equals(processor.getModelId())) {
						
						if (processor.getState() instanceof ExecutingState)  
							this.setState(new PostConditionEvalState("Executing Postconditions"));
						
						else if (processor.getState() instanceof WaitingOnResourcesState) 
							this.setState(new WaitingOnResourcesState("", new PostConditionEvalState("Executing Postconditions")));
							
						else if (processor.getState() instanceof FailureState)  
								this.setState(new FailureState("Failed to pass Postconditions : " + processor.getState().getMessage()));
						
						else if (processor.getState() instanceof BlockedState)  
							this.setState(new BlockedState("Postconditions are blocked : " + processor.getState().getMessage()));
						
					}

				// PreConditionEval
				}else if (this.getState() instanceof PreConditionEvalState) {
				
					if (this.getPreConditions() != null && processor.isConditionProcessor() && this.getPreConditions().getModelId().equals(processor.getModelId())) {
	
						if (processor.getState().getCategory().equals(WorkflowState.Category.DONE)) {
							if (processor.getState() instanceof FailureState) {
								this.setState(new FailureState("Failed to pass Preconditions : " + processor.getState().getMessage()));
							}else if (processor.getState() instanceof SuccessState) {
								this.setDynamicMetadataRecurOnlySubProcessors(processor.getDynamicMetadata());
								if (this.hasSubProcessors() || !this.passedPostConditions())									
									this.setState(new PreConditionSuccessState("All PreConditions passed successfully, so Ready to GO!"));
								else
									this.setState(new SuccessState("Successfully completed"));
							}else {
								this.setState(new UnknownState("Preconditions reported unknown state in DONE category : " + processor.getState().getMessage()));
							}
							
						}else if (processor.getState() instanceof BlockedState) {  
							this.setState(new BlockedState("Preconditions are blocked : " + processor.getState().getMessage()));
						
						}else if (processor.getState() instanceof WaitingOnResourcesState) {
							this.setState(new WaitingOnResourcesState("", new PreConditionEvalState("Executing Preconditions")));
							
						}
						
					}
	
				// PreConditionSuccess
				}else if (this.getState() instanceof PreConditionSuccessState) {
					
					if (!processor.isConditionProcessor()) {
						
						if (processor.getState() instanceof PreConditionEvalState || processor.getState() instanceof ExecutingState)
							this.setState(new ExecutingState("Executing Sub-Processors"));					
							
						else if (processor.getState() instanceof PreConditionSuccessState || processor.getState() instanceof WaitingOnResourcesState) 
							this.setState(new WaitingOnResourcesState("", new ExecutingState("Executing Sub-Processors")));
						
						else if (processor.getState() instanceof BlockedState && this.isBlocked()) 
							this.setState(new BlockedState("Sub-processors are blocked : " + processor.getState().getMessage()));
						
					}else if (this.getPostConditions() != null && this.getPostConditions().getModelId().equals(processor.getModelId())) {
						
						if (processor.getState() instanceof ExecutingState)  
							this.setState(new PostConditionEvalState("Executing Postconditions"));
							
						else if (processor.getState() instanceof WaitingOnResourcesState) 
							this.setState(new WaitingOnResourcesState("", new PostConditionEvalState("Executing Postconditions")));

						else if (processor.getState() instanceof FailureState)  
								this.setState(new FailureState("Failed to pass Postconditions : " + processor.getState().getMessage()));
						
						else if (processor.getState() instanceof BlockedState)  
							this.setState(new BlockedState("Postconditions are blocked : " + processor.getState().getMessage()));
						
					}
				
				// Ready
//				}else if (this.getState() instanceof ReadyState) {
//	
//					if (!processor.isConditionProcessor()) {
//						
//						if (processor.getState() instanceof ExecutingState)
//							this.setState(new ExecutingState("Executing Sub-Processors"));					
//							
//						else if (processor.getState().getCategory().equals(WorkflowState.Category.DONE)) {
//							this.handleSubProcessorMetadata(processor);
//							ResultsState result = this.isDone();
//							if (result instanceof ResultsFailureState) {
//								this.setState(new FailureState("Failed to complete : " + result.getMessage()));
//							}else if (result instanceof ResultsSuccessState) {
//								if (!this.passedPostConditions())									
//									this.setState(new ExecutionCompleteState("Successfully completed : " + result.getMessage()));
//								else
//									this.setState(new SuccessState("Successfully completed : " + result.getMessage()));
//							}else {
//								this.setState(new ExecutingState("Executing Sub-Processors"));
//							}
//						
//						}else if (processor.getState() instanceof BlockedState && this.isBlocked()) 
//							this.setState(new BlockedState("Sub-processors are blocked : " + processor.getState().getMessage()));
//						
//					}else if (this.getPostConditions() != null && this.getPostConditions().getModelId().equals(processor.getModelId())) {
//						
//						if (processor.getState() instanceof ExecutingState)  
//							this.setState(new PostConditionEvalState("Executing Postconditions"));
//							
//						else if (processor.getState() instanceof ReadyState || processor.getState() instanceof WaitingOnResourcesState) 
//							this.setState(new WaitingOnResourcesState("", new PostConditionEvalState("Executing Postconditions")));
//						
//						else if (processor.getState() instanceof FailureState)  
//								this.setState(new FailureState("Failed to pass Postconditions : " + processor.getState().getMessage()));
//						
//						else if (processor.getState() instanceof BlockedState)  
//							this.setState(new BlockedState("Postconditions are blocked : " + processor.getState().getMessage()));
//						
//					}
					
				// Executing
				}else if (this.getState() instanceof ExecutingState) {
					
					if (!processor.isConditionProcessor()) {
						
						if (processor.getState().getCategory().equals(WorkflowState.Category.DONE)) {
							this.handleSubProcessorMetadata(processor);
							ResultsState result = this.isDone();
							if (result instanceof ResultsFailureState) {
								this.setState(new FailureState("Failed to complete : " + processor.getState().getMessage()));
							}else if (result instanceof ResultsSuccessState) {
								if (!this.passedPostConditions())									
									this.setState(new ExecutionCompleteState("Execution is a Success!"));
								else
									this.setState(new SuccessState("Successfully completed"));
							}else if (!StateUtils.constainsGivenCategory(this.getSubProcessors(), WorkflowState.Category.RUNNING)) {
								this.setState(new WaitingOnResourcesState("", new ExecutingState("Executing Sub-Processors")));
							}
							
						}else if ((processor.getState() instanceof PreConditionSuccessState || processor.getState() instanceof ExecutionCompleteState || processor.getState() instanceof WaitingOnResourcesState) && !StateUtils.constainsGivenCategory(this.getSubProcessors(), WorkflowState.Category.RUNNING)) { 
							this.setState(new WaitingOnResourcesState("", new ExecutingState("Executing Sub-Processors")));

						}else if (processor.getState() instanceof BlockedState && this.isBlocked()) 
							this.setState(new BlockedState("Sub-processors are blocked : " + processor.getState().getMessage()));
							
					}else if (this.getPostConditions() != null && this.getPostConditions().getModelId().equals(processor.getModelId())) {
						
						if (processor.getState() instanceof ExecutingState)  
							this.setState(new PostConditionEvalState("Executing Postconditions"));
						
						else if (processor.getState() instanceof WaitingOnResourcesState)  
							this.setState(new WaitingOnResourcesState("", new PostConditionEvalState("Executing Postconditions")));

						else if (processor.getState() instanceof FailureState)  
								this.setState(new FailureState("Failed to pass Postconditions : " + processor.getState().getMessage()));
						
						else if (processor.getState() instanceof BlockedState)  
							this.setState(new BlockedState("Postconditions are blocked : " + processor.getState().getMessage()));
						
					}
	
				// ExecutionComplete	
				}else if (this.getState() instanceof ExecutionCompleteState) {
					
					if (this.passedPostConditions()) {
						this.setState(new SuccessState("No PostConditions, Execution is a Success!"));
						
					}else if (this.getPostConditions() != null && this.getPostConditions().getModelId().equals(processor.getModelId())) {
						
						if (processor.getState() instanceof ExecutingState)  
							this.setState(new PostConditionEvalState("Executing Postconditions"));
							
						else if (processor.getState() instanceof WaitingOnResourcesState)  
							this.setState(new WaitingOnResourcesState("", new PostConditionEvalState("Executing Postconditions")));
						
						else if (processor.getState() instanceof FailureState)  
								this.setState(new FailureState("Failed to pass Postconditions : " + processor.getState().getMessage()));
						
						else if (processor.getState() instanceof BlockedState)  
							this.setState(new BlockedState("Postconditions are blocked : " + processor.getState().getMessage()));
						
					}
					
				// PostConditionEval	
				}else if (this.getState() instanceof PostConditionEvalState) {
						
					if (this.getPostConditions() != null && this.getPostConditions().getModelId().equals(processor.getModelId())) {
						if (processor.getState().getCategory().equals(WorkflowState.Category.DONE)) {
							if (processor.getState() instanceof FailureState) { 
								this.setState(new FailureState("Failed to pass Postconditions : " + processor.getState().getMessage()));
							}else if (processor.getState() instanceof SuccessState) {
								this.setDynamicMetadata(processor.getDynamicMetadata());
								this.setState(new SuccessState("All PostConditions passed successfully, Execution is a Success!"));
							}
							
						}else if (processor.getState() instanceof WaitingOnResourcesState)  
							this.setState(new WaitingOnResourcesState("", new PostConditionEvalState("Executing Postconditions")));
					}
				
				// DONE Category
				}else if (this.getState().getCategory().equals(WorkflowState.Category.DONE)) {
					
					// IF NOTIFICATION CAME FROM PRE-CONDITION
					if (this.getPreConditions() != null && processor.isConditionProcessor() && this.getPreConditions().getModelId().equals(processor.getModelId())) {
						
						if (processor.getState() instanceof ExecutingState)  
							this.setState(new PreConditionEvalState("Executing Preconditions"));
							
						else if (processor.getState() instanceof WaitingOnResourcesState)  
							this.setState(new WaitingOnResourcesState("", new PreConditionEvalState("Executing Preconditions")));
						
						else if (processor.getState() instanceof FailureState)  
								this.setState(new FailureState("Failed to pass Preconditions : " + processor.getState().getMessage()));
						
						else if (processor.getState() instanceof BlockedState)  
							this.setState(new BlockedState("Preconditions are blocked : " + processor.getState().getMessage()));
					
					// IF NOTIFICATION CAME FROM SUB-PROCESSOR
					}else if (this.passedPreConditions() && !processor.isConditionProcessor()) {
						
						ResultsState result = this.isDone();
						if (!(result instanceof ResultsFailureState) && !(result instanceof ResultsSuccessState)) {
							if (processor.getState() instanceof BlockedState && this.isBlocked()) 
								this.setState(new BlockedState("Sub-processors are blocked : " + processor.getState().getMessage()));
							else if (processor.getState() instanceof WaitingOnResourcesState)  
								this.setState(new WaitingOnResourcesState("", new ExecutingState("Executing Sub-Processors")));
							else 
								this.setState(new ExecutingState("Executing Sub-Processors"));
						}	
						
					// IF NOTIFICATION CAME FROM POST-CONDITION
					}else if (this.getPostConditions() != null && processor.isConditionProcessor() && this.getPostConditions().getModelId().equals(processor.getModelId())) {
						
						if (processor.getState() instanceof ExecutingState)  
							this.setState(new PostConditionEvalState("Executing Postconditions"));
							
						else if (processor.getState() instanceof WaitingOnResourcesState)  
							this.setState(new WaitingOnResourcesState("", new PostConditionEvalState("Executing Postconditions")));
						
						else if (processor.getState() instanceof FailureState)  
								this.setState(new FailureState("Failed to pass Postconditions : " + processor.getState().getMessage()));
						
						else if (processor.getState() instanceof BlockedState)  
							this.setState(new BlockedState("Postconditions are blocked : " + processor.getState().getMessage()));
						
					}
					
				// Blocked
				}else if (this.getState() instanceof BlockedState) {
				
					// IF NOTIFICATION CAME FROM PRE-CONDITION
					if (this.getPreConditions() != null && processor.isConditionProcessor() && this.getPreConditions().getModelId().equals(processor.getModelId())) {
						
						if (processor.getState() instanceof ExecutingState) {
							this.setState(new PreConditionEvalState("Executing Preconditions"));
						
						}else if (processor.getState() instanceof WaitingOnResourcesState) {  
								this.setState(new WaitingOnResourcesState("", new PreConditionEvalState("Executing Preconditions")));
							
						}else if (processor.getState() instanceof BlockedState) { 
							this.setState(new BlockedState("Preconditions are blocked : " + processor.getState().getMessage()));
					
						}else if (processor.getState().getCategory().equals(WorkflowState.Category.DONE)) {
							
							if (processor.getState() instanceof FailureState) { 
								this.setState(new FailureState("Failed to pass Postconditions : " + processor.getState().getMessage()));
								
							}else if (processor.getState() instanceof SuccessState) {
								this.setDynamicMetadata(processor.getDynamicMetadata());
								
								if (this.hasSubProcessors() && !this.passedPostConditions())
									this.setState(new PreConditionSuccessState("Preconditions passed! : " + processor.getState().getMessage()));
								
								else
									this.setState(new SuccessState("Execution is a Success!"));
							}
							
						}
						
					// IF NOTIFICATION CAME FROM SUB-PROCESSOR
					}else if (this.passedPreConditions() && !processor.isConditionProcessor()) {
													
						if (processor.getState() instanceof PreConditionEvalState || processor.getState() instanceof ExecutingState) {
							this.setState(new ExecutingState("Executing Sub-Processors"));
							
						}else if (processor.getState() instanceof PreConditionSuccessState || processor.getState() instanceof WaitingOnResourcesState) {  
							this.setState(new WaitingOnResourcesState("", new ExecutingState("Executing Sub-Processors")));
							
						}else if (processor.getState().getCategory().equals(WorkflowState.Category.DONE)) {
							
							if (processor.getState().getCategory().equals(WorkflowState.Category.DONE)) {
								this.handleSubProcessorMetadata(processor);
								ResultsState result = this.isDone();
								if (result instanceof ResultsFailureState) {
									this.setState(new FailureState("Failed to complete : " + processor.getState().getMessage()));
								}else if (result instanceof ResultsSuccessState) {
									if (!this.passedPostConditions())									
										this.setState(new ExecutionCompleteState("Successfully completed"));
									else
										this.setState(new SuccessState("Successfully completed"));
								}else if (!StateUtils.constainsGivenCategory(this.getSubProcessors(), WorkflowState.Category.RUNNING)) {
									this.setState(new WaitingOnResourcesState("", new ExecutingState("Executing Sub-Processors")));
								}	
							}
							
						}

					// IF NOTIFICATION CAME FROM POST-CONDITION
					}else if (this.getPostConditions() != null && processor.isConditionProcessor() && this.getPostConditions().getModelId().equals(processor.getModelId())) {
						
						if (processor.getState() instanceof ExecutingState) {
							this.setState(new PostConditionEvalState("Executing Postconditions"));
						
						}else if (processor.getState() instanceof WaitingOnResourcesState) {  
							this.setState(new WaitingOnResourcesState("", new PostConditionEvalState("Executing Postconditions")));
							
						}else if (processor.getState() instanceof BlockedState)  {
							this.setState(new BlockedState("Postconditions are blocked : " + processor.getState().getMessage()));
						
						}else if (processor.getState().getCategory().equals(WorkflowState.Category.DONE)) {
							
							if (processor.getState() instanceof FailureState) { 
								this.setState(new FailureState("Failed to pass Postconditions : " + processor.getState().getMessage()));
								
							}else if (processor.getState() instanceof SuccessState) {
								this.setDynamicMetadata(processor.getDynamicMetadata());
								this.setState(new SuccessState("Execution is a Success!"));
							}
							
						}
						
					}

				// WaitingOnResourcesState
				}else if (this.getState() instanceof WaitingOnResourcesState) {

					if (processor.getState().getCategory().equals(WorkflowState.Category.RUNNING)) { 
						this.setState(((WaitingOnResourcesState) this.getState()).getPrevState());
					
					}else if (processor.getState().getCategory().equals(WorkflowState.Category.DONE)) {
						if (((WaitingOnResourcesState) this.getState()).getPrevState() instanceof PreConditionEvalState) {
							this.setDynamicMetadataRecurOnlySubProcessors(processor.getDynamicMetadata());
							if (processor.getState() instanceof FailureState)
								this.setState(new FailureState("Failed to completed preconditions : " + processor.getState().getMessage()));
							else
								this.setState(new PreConditionSuccessState("Successfully completed preconditions : " + processor.getState().getMessage()));

						}else if (((WaitingOnResourcesState) this.getState()).getPrevState() instanceof ExecutingState) {
							this.handleSubProcessorMetadata(processor);
							ResultsState result = this.isDone();
							if (result instanceof ResultsFailureState) {
								this.setState(new FailureState("Failed to complete : " + processor.getState().getMessage()));
							}else if (result instanceof ResultsSuccessState) {
								this.setState(new SuccessState("Successfully completed"));
							}
							
						}else if (((WaitingOnResourcesState) this.getState()).getPrevState() instanceof PostConditionEvalState) {
							this.setDynamicMetadata(processor.getDynamicMetadata());
							this.setState(new SuccessState("All PostConditions passed successfully, Execution is a Success!"));

						}
						
					}
					
				}
				
			}else {
				
				// Queued
				if (this.getState() instanceof QueuedState) {
																		
					if (processor.getState() instanceof ExecutingState)
						this.setState(new ExecutingState("Executing Sub-Processors"));
					
					else if (processor.getState() instanceof WaitingOnResourcesState)   
						this.setState(new WaitingOnResourcesState("", new ExecutingState("Executing Sub-Processors")));
					
				// Ready
//				}else if (this.getState() instanceof ReadyState) {
//							
//					if (processor.getState() instanceof ExecutingState || processor.getState() instanceof ReadyState)  
//						this.setState(new ExecutingState("Executing Sub-Processors"));					
//						
//					else if (processor.getState().getCategory().equals(WorkflowState.Category.DONE)) {
//						this.handleSubProcessorMetadata(processor);
//						ResultsState result = this.isDone();
//						if (result instanceof ResultsFailureState) {
//							this.setState(new FailureState("Failed to complete : " + result.getMessage()));
//						}else if (result instanceof ResultsSuccessState) {
//			    			this.setState(new SuccessState("Successfully completed : " + result.getMessage()));
//						}else {
//							this.setState(new ExecutingState("Executing Sub-Processors"));
//						}
//					}

				// Executing
				}else if (this.getState() instanceof ExecutingState) {
					
					if (processor.getState().getCategory().equals(WorkflowState.Category.DONE)) {
						this.handleSubProcessorMetadata(processor);
						ResultsState result = this.isDone();
						if (result instanceof ResultsFailureState) {
							this.setState(new FailureState("Failed to complete : " + processor.getState().getMessage()));
						}else if (result instanceof ResultsSuccessState) {
							this.setState(new SuccessState("Successfully completed"));
						}else if (!StateUtils.constainsGivenCategory(this.getSubProcessors(), WorkflowState.Category.RUNNING)) {
							this.setState(new WaitingOnResourcesState("", new ExecutingState("Executing Sub-Processors")));
						}	
						
					}else if (processor.getState() instanceof WaitingOnResourcesState && !StateUtils.constainsGivenCategory(this.getSubProcessors(), WorkflowState.Category.RUNNING)) { 
						this.setState(new WaitingOnResourcesState("", new ExecutingState("Executing Sub-Processors")));
						
					}else if (processor.getState() instanceof BlockedState && this.isBlocked()) {
						this.setState(new BlockedState("Sub-Processors are Blocked"));
						
					}
	
				// ExecutionComplete	
				}else if (this.getState() instanceof ExecutionCompleteState) {
					this.setState(new SuccessState("Execution is a Success!"));
			
				// DONE Category
				}else if (this.getState().getCategory().equals(WorkflowState.Category.DONE)) {
											
					ResultsState result = this.isDone();
					if (!(result instanceof ResultsFailureState) && !(result instanceof ResultsSuccessState)) {
						if (processor.getState() instanceof BlockedState && this.isBlocked()) 
							this.setState(new BlockedState("Sub-processors are blocked : " + processor.getState().getMessage()));
						else if (processor.getState() instanceof WaitingOnResourcesState)  
							this.setState(new WaitingOnResourcesState("", new ExecutingState("Executing Sub-Processors")));
						else 
							this.setState(new ExecutingState("Executing Sub-Processors"));
					}

				// Blocked
				}else if (this.getState() instanceof BlockedState) {
					
					if (processor.getState() instanceof ExecutingState) {
						this.setState(new ExecutingState("Executing Sub-Processors"));					
					
					}else if (processor.getState() instanceof WaitingOnResourcesState) {  
						this.setState(new WaitingOnResourcesState("", new ExecutingState("Executing Sub-Processors")));
						
					}else if (processor.getState().getCategory().equals(WorkflowState.Category.DONE)) {
						this.handleSubProcessorMetadata(processor);
						ResultsState result = this.isDone();
						if (result instanceof ResultsFailureState) {
							this.setState(new FailureState("Failed to complete : " + processor.getState().getMessage()));
						}else if (result instanceof ResultsSuccessState) {
							this.setState(new SuccessState("Successfully completed"));
						}	
					}

				// WaitingOnResourcesState
				}else if (this.getState() instanceof WaitingOnResourcesState) {
				
					if (processor.getState() instanceof ExecutingState) {
						this.setState(new ExecutingState("Executing Sub-Processors"));					
					
					}else if (processor.getState().getCategory().equals(WorkflowState.Category.DONE)) {
						this.handleSubProcessorMetadata(processor);
						ResultsState result = this.isDone();
						if (result instanceof ResultsFailureState) {
							this.setState(new FailureState("Failed to complete : " + processor.getState().getMessage()));
						}else if (result instanceof ResultsSuccessState) {
							this.setState(new SuccessState("Successfully completed"));
						}
						
					}

				}
			}
			
		}
		
	}
    
    protected void notifyListenersOfChange(ChangeType changeType) {
		for (WorkflowProcessorListener listener : this.getListeners())
			listener.notifyChange(this, changeType);
    }
	
    protected boolean hasSubProcessors() {
    	return this.getSubProcessors().size() > 0;
    }
    
    protected boolean passedPreConditions() {
    	if (this.getPreConditions() != null) {
    		return this.getPreConditions().getState() instanceof SuccessState;
    	}else {
    		return true;
    	}
    }
    
    protected boolean passedPostConditions() {
    	if (this.getPostConditions() != null) {
    		return this.getPostConditions().getState() instanceof SuccessState;
    	}else {
    		return true;
    	}
    }

	public int compareTo(WorkflowProcessor workflowProcessor) {
		return this.priority.compareTo(workflowProcessor.priority);
	}

	protected ResultsState isDone() {
		if (StateUtils.constainsGivenCategory(this.getSubProcessors(), WorkflowState.Category.DONE)) {
			List<WorkflowProcessor> failedSubProcessors = StateUtils.getWorkflowProcessorsOfGivenState(this.getSubProcessors(), FailureState.class);
			if (this.minReqSuccessfulSubProcessors != -1 && failedSubProcessors.size() > (this.getSubProcessors().size() - this.minReqSuccessfulSubProcessors))
				return new ResultsFailureState("More than the allowed number of sub-processors failed");
			for (WorkflowProcessor subProcessor : failedSubProcessors)  
				if (!this.getExcusedSubProcessorIds().contains(subProcessor.modelId)) 
					return new ResultsFailureState("");
			if (StateUtils.allOfGivenCategory(this.getSubProcessors(), WorkflowState.Category.DONE))
				return new ResultsSuccessState("");
		}
		return new ResultsBailState("");
	}
	
	protected boolean isBlocked() {
		boolean hasBlocked = false;
		boolean allDone = true;
		for (WorkflowProcessor wp : this.getSubProcessors()) {
			if (wp.getState() instanceof BlockedState)
				hasBlocked = true;
			else if (!(wp.getState() instanceof QueuedState || wp.getState().getCategory().equals(WorkflowState.Category.DONE))) 
				allDone = false;
		}
		return hasBlocked && allDone;
	}
	
    protected abstract List<WorkflowProcessor> getRunnableSubProcessors();
    
    protected abstract void handleSubProcessorMetadata(WorkflowProcessor workflowProcessor);
    
}
