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

package org.apache.oodt.cas.workflow.engine;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Vector;

import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowStatus;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.exceptions.EngineException;
import org.apache.oodt.cas.workflow.metadata.CoreMetKeys;
import org.apache.oodt.cas.metadata.Metadata;

public class NonBlockingShepardThread implements WorkflowStatus, CoreMetKeys, Runnable {

	private NonBlockingThreadPoolWorkflowEngine engine;
	
	private ConditionEvaluator evalutor;
	
	/* our log stream */
	 private static final Logger LOG = Logger
     .getLogger(ThreadPoolWorkflowEngine.class.getName());
	
	public NonBlockingShepardThread(NonBlockingThreadPoolWorkflowEngine engine){
		this.engine = engine;
		this.evalutor = new ConditionEvaluator();
	}
	
	public void run() {
		while(true){
			
			Vector workflowQueue = engine.getWorkflowQueue();
			
			for(int i=0;i<workflowQueue.size();i++){
				WorkflowInstance wInst = (WorkflowInstance)workflowQueue.get(i);	
				WorkflowTask task = getTaskById(wInst.getWorkflow(),wInst.getCurrentTaskId());
			
				//check required metadata on current task
				if (!checkTaskRequiredMetadata(task, wInst.getSharedContext())) {
	                wInst.setStatus(METADATA_MISSING);
	                try{
	                  engine.persistWorkflowInstance(wInst);
	                } catch (EngineException e){
	                }
	                break;
				}
				
				//check preconditions on current task
				if (task.getConditions() != null) {
	                if(!this.evalutor.satisfied(task.getConditions(), task.getTaskId(), wInst.getSharedContext())) {

	                    /*LOG.log(Level.FINEST, "Pre-conditions for task: "
	                            + task.getTaskName() + " unsatisfied.");*/
	                
	                } else {
	                	//create thread, remove from queue, then if there is another task, increment and read to the queue
	                	
	                	engine.submitWorkflowInstancetoPool(wInst);

	                    workflowQueue.remove(wInst);
	                    i--;
	                    
	                    List tasks = wInst.getWorkflow().getTasks();
	                    for(int j=0;j<tasks.size();j++){
	                    	if( ((WorkflowTask)tasks.get(j)).getTaskId().equals(wInst.getCurrentTaskId()) && j<(tasks.size()-1)){
	                    		wInst.setCurrentTaskId( ((WorkflowTask)tasks.get(j+1)).getTaskId() );
	                    		workflowQueue.add(wInst);
	                    	}
	                    }
	                }
	            }		
			}
			
			
		}
	}

	
	private boolean checkTaskRequiredMetadata(WorkflowTask task,
            Metadata dynMetadata) {
        if (task.getRequiredMetFields() == null
                || (task.getRequiredMetFields() != null && task
                        .getRequiredMetFields().size() == 0)) {
            LOG.log(Level.INFO, "Task: [" + task.getTaskName()
                    + "] has no required metadata fields");
            return true; /* no required metadata, so we're fine */
        }

        for (Iterator i = task.getRequiredMetFields().iterator(); i.hasNext();) {
            String reqField = (String) i.next();
            if (!dynMetadata.containsKey(reqField)) {
                LOG.log(Level.SEVERE, "Checking metadata key: [" + reqField
                        + "] for task: [" + task.getTaskName()
                        + "]: failed: aborting workflow");
                return false;
            }
        }

        LOG.log(Level.INFO, "All required metadata fields present for task: ["
                + task.getTaskName() + "]");

        return true;
    }
	
	
	  private WorkflowTask getTaskById(Workflow w, String Id){
	    	List tasks = w.getTasks();
	    	for(int i=0;i<tasks.size();i++){
	    		if(((WorkflowTask)tasks.get(i)).getTaskId().equals(Id)){
	    			return (WorkflowTask)tasks.get(i);
	    		}
	    	}
	    	
	    	return null;
	    }
	
}
