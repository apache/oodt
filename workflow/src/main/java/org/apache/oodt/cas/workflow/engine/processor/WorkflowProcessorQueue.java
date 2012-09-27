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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
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

  private Map<String, WorkflowProcessor> processorCache;

  public WorkflowProcessorQueue(WorkflowInstanceRepository repo,
      WorkflowLifecycleManager lifecycle, WorkflowRepository modelRepo) {
    this.repo = repo;
    this.lifecycle = lifecycle;
    this.modelRepo = modelRepo;
    this.processorCache = new HashMap<String, WorkflowProcessor>();
  }

  /**
   * Should return the list of available, Queued, {@link WorkflowProcessor}s.
   * 
   * @return the list of available, Queued, {@link WorkflowProcessor}s.
   */
  public synchronized List<WorkflowProcessor> getProcessors() {
    WorkflowInstancePage page = null;
    try {
      page = repo.getPagedWorkflows(1);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.log(Level.WARNING, "Unable to load workflow processors: Message: "
          + e.getMessage());
      return null;
    }

    List<WorkflowProcessor> processors = new Vector<WorkflowProcessor>(
        page.getPageWorkflows() != null ? page.getPageWorkflows().size() : 0);
    for (WorkflowInstance inst : (List<WorkflowInstance>) (List<?>) page
        .getPageWorkflows()) {
      if (!inst.getState().getCategory().getName().equals("done")) {
        WorkflowProcessor processor = fromWorkflowInstance(inst);
        if(processor != null) processors.add(processor);
      }
    }

    return processors;
  }

  private WorkflowProcessor fromWorkflowInstance(WorkflowInstance inst) {
    WorkflowProcessor processor = null;
    if (processorCache.containsKey(inst.getId())) {
      return processorCache.get(inst.getId());
    } else {
      if (inst.getParentChildWorkflow().getGraph() == null) {
        LOG.log(Level.SEVERE,
            "Unable to process Graph for workflow instance: [" + inst.getId()
                + "]");
        return processor;        
      }
      
      if (isCompositeProcessor(inst)){
        processor = getProcessorFromInstanceGraph(inst, lifecycle);
        WorkflowState processorState = getLifecycle(
            inst.getParentChildWorkflow()).createState(
            "Loaded",
            "initial",
            "Sequential Workflow instance with id: [" + inst.getId()
                + "] loaded by processor queue.");
        inst.setState(processorState);
        persist(inst);

        for (WorkflowCondition cond : inst.getParentChildWorkflow()
            .getPreConditions()) {

        }

        for (WorkflowTask task : inst.getParentChildWorkflow().getTasks()) {
          WorkflowInstance instance = new WorkflowInstance();
          WorkflowState taskWorkflowState = lifecycle.getDefaultLifecycle()
              .createState(
                  "Null",
                  "initial",
                  "Sub Task Workflow created by Workflow Processor Queue for workflow instance: "
                      + "[" + inst.getId() + "]");
          instance.setState(taskWorkflowState);
          instance.setPriority(inst.getPriority());
          instance.setCurrentTaskId(task.getTaskId());
          Graph taskGraph = new Graph();
          taskGraph.setExecutionType("task");
          taskGraph.setTask(task);
          ParentChildWorkflow workflow = new ParentChildWorkflow(taskGraph);
          String taskWorkflowId = UUID.randomUUID().toString();
          workflow.setId("task-workflow-" + taskWorkflowId);
          workflow.setName("Task Workflow-" + task.getTaskName());
          workflow.getTasks().add(task);
          workflow.getGraph().setTask(task);
          instance.setId(taskWorkflowId);
          instance.setParentChildWorkflow(workflow);
          this.addToModelRepo(workflow);
          persist(inst);
          WorkflowProcessor subProcessor = fromWorkflowInstance(instance);
          processor.getSubProcessors().add(subProcessor);
        }
      } else {
        processor = new TaskProcessor(lifecycle, inst);
        WorkflowState taskProcessorState = getLifecycle(
            inst.getParentChildWorkflow()).createState(
            "Loaded",
            "initial",
            "Task Workflow instance with id: [" + inst.getId()
                + "] loaded by processor queue.");
        inst.setState(taskProcessorState);
        persist(inst);
      }

      synchronized (processorCache) {
        processorCache.put(inst.getId(), processor);
      }
      return processor;
    }

  }

  private void addToModelRepo(Workflow workflow) {
    if (modelRepo != null) {
      try {
        modelRepo.addWorkflow(workflow);
      } catch (RepositoryException e) {
        e.printStackTrace();
      }
    }
  }

  private void persist(WorkflowInstance instance) {
    try {
      this.repo.updateWorkflowInstance(instance);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.log(Level.WARNING,
          "Unable to update workflow instance: [" + instance.getId()
              + "] with status: [" + instance.getState().getName()
              + "]: Message: " + e.getMessage());
    }
  }

  private WorkflowLifecycle getLifecycle(Workflow workflow) {
    return lifecycle.getLifecycleForWorkflow(workflow) != null ? lifecycle
        .getLifecycleForWorkflow(workflow) : lifecycle.getDefaultLifecycle();
  }
  
  private boolean isCompositeProcessor(WorkflowInstance instance){
    return instance.getParentChildWorkflow().getGraph() != null && 
    instance.getParentChildWorkflow().getGraph().getExecutionType().equals("parallel") || 
    instance.getParentChildWorkflow().getGraph().getExecutionType().equals("sequential");
  }

  private WorkflowProcessor getProcessorFromInstanceGraph(
      WorkflowInstance instance, WorkflowLifecycleManager lifecycle) {
    Graph graph = instance.getParentChildWorkflow().getGraph();
    if (graph != null && graph.getExecutionType() != null
        && graph.getExecutionType().equals("sequential")) {
      return new SequentialProcessor(lifecycle, instance);
    } else {
      return new ParallelProcessor(lifecycle, instance);
    }
  }

}
