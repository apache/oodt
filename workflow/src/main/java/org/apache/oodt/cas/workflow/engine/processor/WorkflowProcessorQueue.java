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

//OODT imports
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycle;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowState;
import org.apache.oodt.cas.workflow.repository.WorkflowRepository;
import org.apache.oodt.cas.workflow.structs.*;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;

//JDK imports
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * The queue of available {@link WorkflowTask}s, that will be fed into the
 * {@link org.apache.oodt.cas.workflow.engine.TaskQuerier}.
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
    this.processorCache = new ConcurrentHashMap<String, WorkflowProcessor>();
  }

  /**
   * Should return the list of available, Queued, {@link WorkflowProcessor}s.
   * 
   * @return the list of available, Queued, {@link WorkflowProcessor}s.
   */
  public synchronized List<WorkflowProcessor> getProcessors() {
    WorkflowInstancePage page;
    try {
      page = repo.getPagedWorkflows(1);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Unable to load workflow processors: Message: "
          + e.getMessage());
      return null;
    }

    List<WorkflowProcessor> processors = new Vector<WorkflowProcessor>(
        page.getPageWorkflows() != null ? page.getPageWorkflows().size() : 0);
    for (WorkflowInstance inst : (List<WorkflowInstance>) (List<?>) page
        .getPageWorkflows()) {
      if (!inst.getState().getCategory().getName().equals("done")) {
        WorkflowProcessor processor;
        try {
          processor = fromWorkflowInstance(inst);
        } catch (Exception e) {
          LOG.log(Level.SEVERE, e.getMessage());
          LOG.log(Level.WARNING,
              "Unable to convert workflow instance: [" + inst.getId()
                  + "] into WorkflowProcessor: Message: " + e.getMessage());
          continue;
        }
        if (processor != null) {
          processors.add(processor);
        }
      }
    }

    return processors;
  }
  

  public synchronized void persist(WorkflowInstance inst) {
    try {
      if (inst.getId() == null || (inst.getId().equals(""))) {
        // we have to persist it by adding it
        // rather than updating it
        repo.addWorkflowInstance(inst);
      } else {
        // persist by update
        repo.updateWorkflowInstance(inst);
      }
    } catch (InstanceRepositoryException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "Unable to update workflow instance: [" + inst.getId()
              + "] with status: [" + inst.getState().getName() + "]: Message: "
              + e.getMessage());
    }
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
        return null;
      }

      if (isCompositeProcessor(inst)) {
        processor = getProcessorFromInstanceGraph(inst, lifecycle);
        WorkflowState processorState = getLifecycle(
            inst.getParentChildWorkflow()).createState(
            "Loaded",
            "initial",
            "Sequential Workflow instance with id: [" + inst.getId()
                + "] loaded by processor queue.");
        inst.setState(processorState);
        persist(inst);

        // handle its pre-conditions
        for (WorkflowCondition cond : inst.getParentChildWorkflow()
            .getPreConditions()) {
          WorkflowInstance instance = new WorkflowInstance();
          WorkflowState condWorkflowState = lifecycle
              .getDefaultLifecycle()
              .createState(
                  "Null",
                  "initial",
                  "Sub Pre Condition Workflow created by Workflow Processor Queue for workflow instance: "
                      + "[" + inst.getId() + "]");
          instance.setState(condWorkflowState);
          instance.setPriority(inst.getPriority());
          WorkflowTask conditionTask = toConditionTask(cond);
          instance.setCurrentTaskId(conditionTask.getTaskId());
          Graph condGraph = new Graph();
          condGraph.setExecutionType("condition");
          condGraph.setCond(cond);
          condGraph.setTask(conditionTask);
          ParentChildWorkflow workflow = new ParentChildWorkflow(condGraph);
          workflow.setId("pre-cond-workflow-"
              + inst.getParentChildWorkflow().getId());
          workflow.setName("Pre Condition Workflow-" + cond.getConditionName());
          workflow.getTasks().add(conditionTask);
          instance.setParentChildWorkflow(workflow);
          this.addToModelRepo(workflow);
          persist(instance);
          WorkflowProcessor subProcessor = fromWorkflowInstance(instance);
          processor.getSubProcessors().add(subProcessor);
          synchronized (processorCache) {
            processorCache.put(instance.getId(), subProcessor);
          }
        }

        // handle its tasks
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
          workflow.setId("task-workflow-"
              + inst.getParentChildWorkflow().getId());
          workflow.setName("Task Workflow-" + task.getTaskName());
          workflow.getTasks().add(task);
          workflow.getGraph().setTask(task);
          instance.setParentChildWorkflow(workflow);
          this.addToModelRepo(workflow);
          persist(instance);
          WorkflowProcessor subProcessor = fromWorkflowInstance(instance);
          processor.getSubProcessors().add(subProcessor);
          synchronized (processorCache) {
            processorCache.put(instance.getId(), subProcessor);
          }
        }

        // handle its post conditions
        for (WorkflowCondition cond : inst.getParentChildWorkflow()
            .getPostConditions()) {
          WorkflowInstance instance = new WorkflowInstance();
          WorkflowState condWorkflowState = lifecycle
              .getDefaultLifecycle()
              .createState(
                  "Null",
                  "initial",
                  "Sub Post Condition Workflow created by Workflow Processor Queue for workflow instance: "
                      + "[" + inst.getId() + "]");
          instance.setState(condWorkflowState);
          instance.setPriority(inst.getPriority());
          WorkflowTask conditionTask = toConditionTask(cond);
          instance.setCurrentTaskId(conditionTask.getTaskId());
          Graph condGraph = new Graph();
          condGraph.setExecutionType("condition");
          condGraph.setCond(cond);
          condGraph.setTask(conditionTask);
          ParentChildWorkflow workflow = new ParentChildWorkflow(condGraph);
          workflow.setId("post-cond-workflow-"
              + inst.getParentChildWorkflow().getId());
          workflow
              .setName("Post Condition Workflow-" + cond.getConditionName());
          workflow.getTasks().add(conditionTask);
          instance.setParentChildWorkflow(workflow);
          this.addToModelRepo(workflow);
          persist(instance);
          WorkflowProcessor subProcessor = fromWorkflowInstance(instance);
          processor.getSubProcessors().add(subProcessor);
          synchronized (processorCache) {
            processorCache.put(instance.getId(), subProcessor);
          }
        }

      } else {
        // it's not a composite workflow, and it's either just a task processor
        // or a condition processor
        if (inst.getParentChildWorkflow().getGraph().getExecutionType()
            .equals("task")) {
          processor = new TaskProcessor(lifecycle, inst);
          WorkflowState taskProcessorState = getLifecycle(
              inst.getParentChildWorkflow()).createState(
              "Loaded",
              "initial",
              "Task Workflow instance with id: [" + inst.getId()
                  + "] loaded by processor queue.");
          inst.setState(taskProcessorState);

          // handle its pre-conditions
          for (WorkflowCondition cond : inst.getParentChildWorkflow()
              .getGraph().getTask().getPreConditions()) {
            WorkflowInstance instance = new WorkflowInstance();
            WorkflowState condWorkflowState = lifecycle
                .getDefaultLifecycle()
                .createState(
                    "Null",
                    "initial",
                    "Sub Pre Condition Workflow for Task created by Workflow Processor Queue for workflow instance: "
                        + "[" + inst.getId() + "]");
            instance.setState(condWorkflowState);
            instance.setPriority(inst.getPriority());
            WorkflowTask conditionTask = toConditionTask(cond);
            instance.setCurrentTaskId(conditionTask.getTaskId());
            Graph condGraph = new Graph();
            condGraph.setExecutionType("condition");
            condGraph.setCond(cond);
            condGraph.setTask(conditionTask);
            ParentChildWorkflow workflow = new ParentChildWorkflow(condGraph);
            workflow.setId("pre-cond-workflow-"
                + inst.getParentChildWorkflow().getGraph().getTask()
                    .getTaskId());
            workflow.setName("Task Pre Condition Workflow-"
                + cond.getConditionName());
            workflow.getTasks().add(conditionTask);
            instance.setParentChildWorkflow(workflow);
            this.addToModelRepo(workflow);
            persist(instance);
            WorkflowProcessor subProcessor = fromWorkflowInstance(instance);
            processor.getSubProcessors().add(subProcessor);
            synchronized (processorCache) {
              processorCache.put(instance.getId(), subProcessor);
            }
          }

          // handle its post-conditions
          for (WorkflowCondition cond : inst.getParentChildWorkflow()
              .getGraph().getTask().getPostConditions()) {
            WorkflowInstance instance = new WorkflowInstance();
            WorkflowState condWorkflowState = lifecycle
                .getDefaultLifecycle()
                .createState(
                    "Null",
                    "initial",
                    "Sub Post Condition Workflow for Task created by Workflow Processor Queue for workflow instance: "
                        + "[" + inst.getId() + "]");
            instance.setState(condWorkflowState);
            instance.setPriority(inst.getPriority());
            WorkflowTask conditionTask = toConditionTask(cond);
            instance.setCurrentTaskId(conditionTask.getTaskId());
            Graph condGraph = new Graph();
            condGraph.setExecutionType("condition");
            condGraph.setCond(cond);
            condGraph.setTask(conditionTask);
            ParentChildWorkflow workflow = new ParentChildWorkflow(condGraph);
            workflow.setId("post-cond-workflow-"
                + inst.getParentChildWorkflow().getGraph().getTask()
                    .getTaskId());
            workflow.setName("Task Post Condition Workflow-"
                + cond.getConditionName());
            workflow.getTasks().add(conditionTask);
            instance.setParentChildWorkflow(workflow);
            this.addToModelRepo(workflow);
            persist(instance);
            WorkflowProcessor subProcessor = fromWorkflowInstance(instance);
            processor.getSubProcessors().add(subProcessor);
            synchronized (processorCache) {
              processorCache.put(instance.getId(), subProcessor);
            }
          }

        } else if (inst.getParentChildWorkflow().getGraph().getExecutionType()
            .equals("condition")) {
          processor = new ConditionProcessor(lifecycle, inst);
          WorkflowState condProcessorState = getLifecycle(
              inst.getParentChildWorkflow()).createState(
              "Loaded",
              "initial",
              "Condition Workflow instance with id: [" + inst.getId()
                  + "] loaded by processor queue.");
          inst.setState(condProcessorState);
        }
        persist(inst);
      }

      synchronized (processorCache) {
        processorCache.put(inst.getId(), processor);
      }
      return processor;
    }

  }
  
  private synchronized void addTaskToModelRepo(WorkflowTask task){
    if(modelRepo != null){
      try{
        modelRepo.addTask(task);
      }
      catch(RepositoryException e){
        LOG.log(Level.SEVERE, e.getMessage());
      }
    }
  }

  private synchronized void addToModelRepo(Workflow workflow) {
    if (modelRepo != null) {
      try {
        modelRepo.addWorkflow(workflow);
      } catch (RepositoryException e) {
        LOG.log(Level.SEVERE, e.getMessage());
      }
    }
  }

  private WorkflowLifecycle getLifecycle(Workflow workflow) {
    return lifecycle.getLifecycleForWorkflow(workflow) != null ? lifecycle
        .getLifecycleForWorkflow(workflow) : lifecycle.getDefaultLifecycle();
  }

  private boolean isCompositeProcessor(WorkflowInstance instance) {
    if (instance.getParentChildWorkflow().getGraph() != null
        && instance.getParentChildWorkflow().getGraph().getExecutionType() != null
        && !instance.getParentChildWorkflow().getGraph().getExecutionType()
            .equals("")) {
      return instance.getParentChildWorkflow().getGraph().getExecutionType()
          .equals("parallel")
          || instance.getParentChildWorkflow().getGraph().getExecutionType()
              .equals("sequential");
    } else {
      // we don't have a Graph to work with, so we'll default to whether or not
      // so we'll assume this is a workflow instance delivered to us by the
      // instRep
      // which doesn't understand Graphs yet (TODO: make instRep understand
      // graphs
      // and persist them)
      // so the simple solution is to check whether or not the ID starts with
      // task-workflow or pre-cond or post-cond
      return !(instance.getParentChildWorkflow().getId()
          .startsWith("task-workflow")
          || instance.getParentChildWorkflow().getId().startsWith("pre-cond") || instance
          .getParentChildWorkflow().getId().startsWith("post-cond"));
    }
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
  
  private synchronized WorkflowTask toConditionTask(WorkflowCondition cond){    
    String taskId = cond.getConditionId()+"-task"; // TODO: this is incompat with DataSourceWorkflowRepository
    WorkflowTask condTask = safeGetTaskById(taskId);
    if(condTask != null) {
      return condTask;
    }
    condTask = new WorkflowTask();
    condTask.setTaskId(taskId);
    condTask.setTaskInstanceClassName(ConditionTaskInstance.class.getCanonicalName());
    condTask.setTaskName(cond.getConditionName()+" Task");
    WorkflowTaskConfiguration config = new WorkflowTaskConfiguration();
    config.getProperties().putAll(cond.getCondConfig().getProperties());
    // this one is a special one that will be removed by the ConditionTaskInstance class
    config.addConfigProperty("ConditionClassName", cond.getConditionInstanceClassName()); 
    condTask.setTaskConfig(config);
    this.addTaskToModelRepo(condTask);
    return condTask;
  }
  
  private WorkflowTask safeGetTaskById(String taskId){
    WorkflowTask task = null;
      try{
        if((task = this.modelRepo.getTaskById(taskId)) != null){
          return task;
        }
      }
      catch(RepositoryException e){
        LOG.log(Level.SEVERE, e.getMessage());
      }
    
    return null;
  }

}
