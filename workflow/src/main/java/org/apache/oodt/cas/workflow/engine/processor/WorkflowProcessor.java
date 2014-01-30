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
package org.apache.oodt.cas.workflow.engine.processor;

//JDK imports
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.workflow.engine.ChangeType;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleStage;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowState;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;

/**
 * 
 * The new Apache OODT workflow style of processor. These processors are
 * responsible for returning the set of underlying tasks, or conditions that can
 * run. A sequential version will return only a single sub-processor (condition
 * or task, or even workflow); a parallel version will return many sub
 * processors to run.
 * 
 * @since Apache OODT 0.4.
 * 
 * @author mattmann
 * @author bfoster
 * 
 */
public abstract class WorkflowProcessor implements WorkflowProcessorListener,
    Comparable<WorkflowProcessor> {

  private static final Logger LOG = Logger.getLogger(WorkflowProcessor.class
      .getName());

  private WorkflowInstance workflowInstance;
  private WorkflowProcessor preConditions;
  private WorkflowProcessor postConditions;
  private List<String> excusedSubProcessorIds; // FIXME: read this in
                                               // PackagedRepo: flow through
                                               // instance
  private List<WorkflowProcessor> subProcessors;
  private List<WorkflowProcessorListener> listeners;
  private int minReqSuccessfulSubProcessors; // FIXME: read this in
                                             // PackagedRepo: flow through
                                             // instance
  protected WorkflowLifecycleManager lifecycleManager;
  protected WorkflowProcessorHelper helper;

  public WorkflowProcessor(WorkflowLifecycleManager lifecycleManager,
      WorkflowInstance workflowInstance) {
    this.subProcessors = new Vector<WorkflowProcessor>();
    this.listeners = new Vector<WorkflowProcessorListener>();
    this.excusedSubProcessorIds = new Vector<String>();
    this.minReqSuccessfulSubProcessors = -1;
    this.lifecycleManager = lifecycleManager;
    this.workflowInstance = workflowInstance;
    this.helper = new WorkflowProcessorHelper(lifecycleManager);
    WorkflowState initState = helper.getLifecycleForProcessor(this)
        .createState("Null", "initial",
            "Instance created by workflow processor.");
    this.workflowInstance.setState(initState);
  }

  /**
   * @return the workflowInstance
   */
  public WorkflowInstance getWorkflowInstance() {
    return workflowInstance;
  }

  /**
   * @param workflowInstance
   *          the workflowInstance to set
   */
  public void setWorkflowInstance(WorkflowInstance workflowInstance) {
    this.workflowInstance = workflowInstance;
  }

  /**
   * @return the excusedSubProcessorIds
   */
  public List<String> getExcusedSubProcessorIds() {
    return excusedSubProcessorIds;
  }

  /**
   * @param excusedSubProcessorIds
   *          the excusedSubProcessorIds to set
   */
  public void setExcusedSubProcessorIds(List<String> excusedSubProcessorIds) {
    this.excusedSubProcessorIds = excusedSubProcessorIds;
  }

  /**
   * @return the subProcessors
   */
  public List<WorkflowProcessor> getSubProcessors() {
    return subProcessors;
  }

  /**
   * @param subProcessors
   *          the subProcessors to set
   */
  public void setSubProcessors(List<WorkflowProcessor> subProcessors) {
    this.subProcessors = subProcessors;
  }

  /**
   * @return the listeners
   */
  public List<WorkflowProcessorListener> getListeners() {
    return listeners;
  }

  /**
   * @param listeners
   *          the listeners to set
   */
  public void setListeners(List<WorkflowProcessorListener> listeners) {
    this.listeners = listeners;
  }

  /**
   * @return the minReqSuccessfulSubProcessors
   */
  public int getMinReqSuccessfulSubProcessors() {
    return minReqSuccessfulSubProcessors;
  }

  /**
   * @param minReqSuccessfulSubProcessors
   *          the minReqSuccessfulSubProcessors to set
   */
  public void setMinReqSuccessfulSubProcessors(int minReqSuccessfulSubProcessors) {
    this.minReqSuccessfulSubProcessors = minReqSuccessfulSubProcessors;
  }

  /**
   * @return the lifecycleManager
   */
  public WorkflowLifecycleManager getLifecycleManager() {
    return lifecycleManager;
  }

  /**
   * @param lifecycleManager
   *          the lifecycleManager to set
   */
  public void setLifecycleManager(WorkflowLifecycleManager lifecycleManager) {
    this.lifecycleManager = lifecycleManager;
  }

  /**
   * @return the preConditions
   */
  public WorkflowProcessor getPreConditions() {
    return preConditions;
  }

  /**
   * @param preConditions
   *          the preConditions to set
   */
  public void setPreConditions(WorkflowProcessor preConditions) {
    this.preConditions = preConditions;
  }

  /**
   * @return the postConditions
   */
  public WorkflowProcessor getPostConditions() {
    return postConditions;
  }

  /**
   * @param postConditions
   *          the postConditions to set
   */
  public void setPostConditions(WorkflowProcessor postConditions) {
    this.postConditions = postConditions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(WorkflowProcessor workflowProcessor) {
    return this.getWorkflowInstance().getPriority()
        .compareTo(workflowProcessor.getWorkflowInstance().getPriority());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.engine.WorkflowProcessorListener#notifyChange
   * (org.apache.oodt.cas.workflow.engine.WorkflowProcessor,
   * org.apache.oodt.cas.workflow.engine.ChangeType)
   */
  @Override
  public void notifyChange(WorkflowProcessor processor, ChangeType changeType) {
    for (WorkflowProcessorListener listener : this.getListeners())
      listener.notifyChange(this, changeType);
  }

  public synchronized List<TaskProcessor> getRunnableWorkflowProcessors() {
    Vector<TaskProcessor> runnableTasks = new Vector<TaskProcessor>();

    // evaluate pre-conditions
    if (!this.passedPreConditions()) {
      for (WorkflowProcessor subProcessor : this.getPreConditions()
          .getRunnableSubProcessors()) {
        for (TaskProcessor tp : subProcessor.getRunnableWorkflowProcessors()) {
          runnableTasks.add(tp);
        }
      }

    } else if (this.isDone().getName().equals("ResultsFailure")) {
      // do nothing -- this workflow failed!!!
    } else if (this.isDone().getName().equals("ResultsBail")) {
      for (WorkflowProcessor subProcessor : this.getRunnableSubProcessors())
        runnableTasks.addAll(subProcessor.getRunnableWorkflowProcessors());
    } else if (!this.passedPostConditions()) {
      for (WorkflowProcessor subProcessor : this.getPostConditions()
          .getRunnableSubProcessors()) {
        for (TaskProcessor tp : subProcessor.getRunnableWorkflowProcessors()) {
          runnableTasks.add(tp);
        }
      }

    }

    return runnableTasks;
  }

  /**
   * Advances this WorkflowProcessor to its next {@link WorkflowState}.
   */
  public void nextState() {
    if (this.workflowInstance != null
        && this.workflowInstance.getState() != null) {
      WorkflowState currState = this.workflowInstance.getState();
      WorkflowState nextState = null;
      if (currState.getName().equals("Null")) {
        nextState = this.helper.getLifecycleForProcessor(this).createState(
            "Loaded",
            "initial",
            "Workflow Processor: nextState: " + "loading workflow instance: ["
                + this.workflowInstance.getId() + "]");
      } else if (currState.getName().equals("Loaded")) {
        nextState = this.helper.getLifecycleForProcessor(this).createState(
            "Queued",
            "initial",
            "Workflow Processor: nextState: " + "queueing instance: ["
                + this.workflowInstance.getId() + "]");
      } else if (currState.getName().equals("Queued")) {
        if (!this.passedPreConditions()) {
          nextState = this.helper.getLifecycleForProcessor(this).createState(
              "PreConditionEval",
              "running",
              "Workflow Processor: nextState: "
                  + "running preconditiosn for workflow instance: ["
                  + this.workflowInstance.getId() + "]");
        } else {
          if (this.isDone().getName().equals("ResultsSuccess")) {
            nextState = this.helper.getLifecycleForProcessor(this).createState(
                "Success",
                "done",
                "Workflow Processor: nextState: " + "workflow instance: ["
                    + this.workflowInstance.getId()
                    + "] completed successfully");
          }
        }
      } else if (currState.getName().equals("Executing")) {
        if(this.isDone().getName().equals("ResultsSuccess")){
        nextState = this.helper.getLifecycleForProcessor(this).createState(
            "Success",
            "done",
            "Workflow Processor: nextState: " + "workflow instance: ["
                + this.workflowInstance.getId() + "] completed successfully");
        }
      }
      else if(currState.getName().equals("ExecutionComplete")){
        nextState = this.helper.getLifecycleForProcessor(this).createState(
            "Success",
            "done",
            "Workflow Processor: nextState: " + "workflow instance: ["
                + this.workflowInstance.getId() + "] completed successfully");        
      }

      if (nextState != null) {
        this.workflowInstance.setState(nextState);
      }

    } else {
      this.workflowInstance.setState(helper.getLifecycleForProcessor(this)
          .createState(
              "Unknown",
              "holding",
              "The Workflow Processor for instance : ["
                  + this.getWorkflowInstance().getId() + "] "
                  + "had a null state"));
    }
  }
  
  /**
   * Evaluates whether or not this processor's {@link WorkflowState}
   * is in any of the provided state names.
   * 
   * @param states The names of states to check this processor's 
   * {@link WorkflowState} against.
   * 
   * @return True, if any of the state names provided is the name of
   * this processor's internal {@link WorkflowState}, False otherwise.
   */
  public boolean isAnyState(String... states) {
    for (String state : states) {
      if (this.getWorkflowInstance().getState().getName().equals(state)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Evaluates whether or not this processor's {@link WorkflowLifecycleStage}
   * is in any of the provided category names.
   * 
   * @param categories The names of categories to check this processor's 
   * {@link WorkflowLifecycleStage} against.
   * 
   * @return True, if any of the category names provided is the name of
   * this processor's internal {@link WorkflowLifecycleStage}, False otherwise.
   */
  public boolean isAnyCategory(String... categories) {
    for (String category : categories) {
      if (this.getWorkflowInstance().getState().getCategory().getName()
          .equals(category)) {
        return true;
      }
    }

    return false;
  }  

  protected boolean passedPreConditions() {
    if (this.getPreConditions() != null) {
      return this.getPreConditions().getWorkflowInstance().getState().getName()
          .equals("Success");
    } else {
      return true;
    }
  }

  protected boolean passedPostConditions() {
    if (this.getPostConditions() != null) {
      return this.getPostConditions().getWorkflowInstance().getState()
          .getName().equals("Success");
    } else {
      return true;
    }
  }

  /**
   * First checks to see if any of this Processor's {@link #subProcessors} have
   * arrived in a state within the done category. If so the method determines if
   * any of the done {@link #subProcessors} are in Failure state. If so, the
   * method compares the number of Failed sub-processors against
   * {@link #minReqSuccessfulSubProcessors}, and if it is greater than it,
   * returns a ResultsFailure {@link WorkflowState}. Otherwise, the method scans
   * the failed sub-processors, and checks to see if all of them have been
   * excused. If they haven't, then a ResultFailure state is returned. Finally,
   * the method checks to ensure that all sub processors are in the done
   * category. If they are, a ResultsSuccess {@link WorkflowState} is returned,
   * otherwise, a ResultsBail state is returned.
   * 
   * @return A {@link WorkflowState}, according to the method description.
   */
  protected WorkflowState isDone() {
    if (this.helper.containsCategory(this.getSubProcessors(), "done")) {
      List<WorkflowProcessor> failedSubProcessors = this.helper
          .getWorkflowProcessorsByState(this.getSubProcessors(), "Failure");
      if (this.minReqSuccessfulSubProcessors != -1
          && failedSubProcessors.size() > (this.getSubProcessors().size() - this.minReqSuccessfulSubProcessors))
        return lifecycleManager.getDefaultLifecycle().createState(
            "ResultsFailure", "results",
            "More than the allowed number of sub-processors failed");
      for (WorkflowProcessor subProcessor : failedSubProcessors) {
        if (!this.getExcusedSubProcessorIds().contains(
            subProcessor.getWorkflowInstance().getId())) {
          return lifecycleManager.getDefaultLifecycle().createState(
              "ResultsFailure",
              "results",
              "Sub processor: [" + subProcessor.getWorkflowInstance().getId()
                  + "] failed.");
        }
      }
      if (this.helper
          .allProcessorsSameCategory(this.getSubProcessors(), "done"))
        return lifecycleManager.getDefaultLifecycle().createState(
            "ResultsSuccess",
            "results",
            "Workflow Processor: processing instance id: ["
                + workflowInstance.getId() + "] is Done.");
    }
    return lifecycleManager.getDefaultLifecycle().createState(
        "ResultsBail",
        "results",
        "All sub-processors for Workflow Processor handling workflow id: ["
            + workflowInstance.getId() + "] are " + "not complete");
  }

  /**
   * This is the core method of the WorkflowProcessor class in the new Wengine
   * style workflows. Instead of requiring that a processor actually walk
   * through the underlying {@link Workflow}, these style WorkflowProcessors
   * actually require their implementing sub-classes to return the current set
   * of Runnable sub-processors (which could be tasks, conditions, even
   * {@link Workflow}s themselves.
   * 
   * The Parallel sub-class returns a list of task or condition processors that
   * are able to run at a given time. The Sequential sub-class returns only a
   * single task or condition processor to run, and so forth.
   * 
   * @return The list of WorkflowProcessors able to currently run.
   */
  protected abstract List<WorkflowProcessor> getRunnableSubProcessors();

  protected abstract void handleSubProcessorMetadata(
      WorkflowProcessor workflowProcessor);

}
