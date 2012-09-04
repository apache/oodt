/**
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

package org.apache.oodt.cas.workflow.engine.processor;

//JDK imports
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.workflow.engine.TaskQuerier;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycle;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowState;
import org.apache.oodt.cas.workflow.repository.WorkflowRepository;
import org.apache.oodt.cas.workflow.structs.Graph;
import org.apache.oodt.cas.workflow.structs.ParentChildWorkflow;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;

/**
 * 
 * The queue of available {@link WorkflowTask}s, that will be fed into the
 * {@link TaskQuerier}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowProcessorQueue {

  private static final Logger LOG = Logger
      .getLogger(WorkflowProcessorQueue.class.getName());

  private WorkflowInstanceRepository repo;
  
  private WorkflowRepository modelRepo;

  private WorkflowLifecycleManager lifecycle;

  public WorkflowProcessorQueue(WorkflowInstanceRepository repo,
      WorkflowLifecycleManager lifecycle, WorkflowRepository modelRepo) {
    this.repo = repo;
    this.lifecycle = lifecycle;
    this.modelRepo = modelRepo;
  }

  /**
   * Should return the list of available, Queued, {@link WorkflowProcessor}s.
   * 
   * @return the list of available, Queued, {@link WorkflowProcessor}s.
   */
  public synchronized List<WorkflowProcessor> getProcessors() {
    WorkflowInstancePage page = null;
    try {
      page = repo.getPagedWorkflows(1, "Queued");
    } catch (Exception e) {
      e.printStackTrace();
      LOG.log(Level.WARNING, "Unable to load workflow processors: Message: "
          + e.getMessage());
      return null;
    }

    List<WorkflowProcessor> processors = new Vector<WorkflowProcessor>(
        page.getPageSize());
    for (WorkflowInstance inst : (List<WorkflowInstance>) (List<?>) page
        .getPageWorkflows()) {
      if ((inst.getState() == null)
          || (inst.getState() != null && inst.getState().getCategory() == null)) {
        WorkflowLifecycle cycle = getLifecycle(inst.getWorkflow());
        WorkflowState state = cycle.getStateByName(inst.getStatus());
        state.setMessage("Queued by WorkflowProcessorQueue.");
        inst.setState(state);
        try {
          this.repo.updateWorkflowInstance(inst);
        } catch (Exception e) {
          e.printStackTrace();
          LOG.log(
              Level.WARNING,
              "Unable to update workflow instance: [" + inst.getId()
                  + "] with status: [" + inst.getStatus() + "]: Message: "
                  + e.getMessage());
        }

      }
      processors.add(fromWorkflowInstance(inst));
    }

    return processors;
  }

  private WorkflowProcessor fromWorkflowInstance(WorkflowInstance inst) {
    WorkflowProcessor processor = null;
    if (inst.getParentChildWorkflow().getTasks() != null
        && inst.getParentChildWorkflow().getTasks().size() > 1) {
      processor = new SequentialProcessor();
      processor.setWorkflowInstance(inst); 
      
      for (WorkflowTask task : inst.getParentChildWorkflow().getTasks()) {
        WorkflowInstance instance = new WorkflowInstance();
        instance.setState(inst.getState());
        instance.setCurrentTaskId(task.getTaskId());
        ParentChildWorkflow workflow = new ParentChildWorkflow(new Graph());
        workflow.setId("task-workflow-"+UUID.randomUUID().toString());
        workflow.setName("Task Workflow-"+task.getTaskName());
        workflow.getTasks().add(task);
        workflow.getGraph().setTask(task);
        instance.setId(UUID.randomUUID().toString());
        instance.setParentChildWorkflow(workflow);
        if(modelRepo != null){
          try {
            modelRepo.addWorkflow(workflow);
          } catch (RepositoryException e) {
            e.printStackTrace();
          }
        }
        
        WorkflowProcessor subProcessor = fromWorkflowInstance(instance);
        processor.getSubProcessors().add(subProcessor);        
      }      
      processor.getWorkflowInstance().setState(inst.getState());
    }
    else{
      processor = new TaskProcessor();
      processor.setWorkflowInstance(inst);
      processor.getWorkflowInstance().setState(inst.getState());
    }

    processor.setLifecycleManager(lifecycle);
    
    return processor;
  }

  private WorkflowLifecycle getLifecycle(Workflow workflow) {
    return lifecycle.getLifecycleForWorkflow(workflow) != null ? lifecycle
        .getLifecycleForWorkflow(workflow) : lifecycle.getDefaultLifecycle();
  }

}
