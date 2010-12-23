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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.workflow.instance.TaskInstance;
import org.apache.oodt.cas.workflow.priority.Priority;
import org.apache.oodt.cas.workflow.state.WorkflowState;
import org.apache.oodt.cas.workflow.state.transition.PreConditionSuccessState;
import org.apache.oodt.cas.workflow.state.waiting.BlockedState;
import org.apache.oodt.cas.workflow.state.waiting.QueuedState;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * WorkflowProcessor which handles running task workflows
 * </p>.
 */
public class TaskProcessor extends WorkflowProcessor {

	private Class<? extends TaskInstance> instanceClass;
	private String jobId;
	
	public TaskProcessor() {
		super();
	}
	
	public Class<? extends TaskInstance> getInstanceClass() {
		return this.instanceClass;
	}
	
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	
	public String getJobId() {
		return this.jobId;
	}

	public void setInstanceClass(Class<? extends TaskInstance> instanceClass) {
		this.instanceClass = instanceClass;
	}
	
	@Override
	public void setPriority(Priority priority) {
		super.setPriority(Priority.getPriority(priority.getValue() + 0.1));
	}
	
	@Override
    public List<TaskProcessor> getRunnableWorkflowProcessors() {
		List<TaskProcessor> tps = super.getRunnableWorkflowProcessors();
		if (tps.size() == 0) {
			if (this.getState() instanceof BlockedState) {
				String requiredBlockTimeElapseString = this.getStaticMetadata().getMetadata("BlockTimeElapse");
				int requiredBlockTimeElapse = 2;
				if (requiredBlockTimeElapseString != null) {
					try {
						requiredBlockTimeElapse = Integer.parseInt(requiredBlockTimeElapseString);
					}catch (Exception e) {}
				}
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(this.getState().getStartTime());
				long elapsedTime = ((System.currentTimeMillis() - calendar.getTimeInMillis()) / 1000) / 60;
				if (elapsedTime >= requiredBlockTimeElapse)
					tps.add(this);
			}else if (this.getState() instanceof QueuedState && this.passedPreConditions() || this.getState() instanceof PreConditionSuccessState) {
				tps.add(this);
			}
		}
		return tps;
    }
	
    protected boolean hasSubProcessors() {
    	return true;
    }
	
	@Override
	public List<WorkflowProcessor> getRunnableSubProcessors() {
		return new Vector<WorkflowProcessor>();
	}
	
	@Override
	public void setSubProcessors(List<WorkflowProcessor> subProcessors) {
		//not allowed
	}

	@Override
	public void handleSubProcessorMetadata(WorkflowProcessor workflowProcessor) {
		//do nothing
	}
	
}
