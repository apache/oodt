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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.ChangeType;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowState;
import org.apache.oodt.cas.workflow.structs.Priority;
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

  public static final String LOCAL_KEYS = "WorkflowProcessor/Local/Keys";

  private static final Logger LOG = Logger.getLogger(WorkflowProcessor.class
      .getName());

  private WorkflowInstance workflowInstance;
  private String executionType;
  private List<String> excusedSubProcessorIds;
  private WorkflowState state;
  private List<WorkflowProcessor> subProcessors;
  private List<WorkflowProcessorListener> listeners;
  private WorkflowProcessor preConditions;
  private WorkflowProcessor postConditions;
  private ProcessorDateTimeInfo ProcessorDateTimeInfo;
  private Priority priority;
  private int minReqSuccessfulSubProcessors;
  private Metadata staticMetadata;
  private Metadata dynamicMetadata;
  private boolean isConditionProcessor;
  private int timesBlocked;
  protected WorkflowLifecycleManager lifecycleManager;

  public WorkflowProcessor(WorkflowLifecycleManager lifecycleManager) {
    this.subProcessors = new Vector<WorkflowProcessor>();
    this.listeners = new Vector<WorkflowProcessorListener>();
    this.ProcessorDateTimeInfo = new ProcessorDateTimeInfo();
    this.staticMetadata = new Metadata();
    this.dynamicMetadata = new Metadata();
    this.excusedSubProcessorIds = new Vector<String>();
    this.minReqSuccessfulSubProcessors = -1;
    this.isConditionProcessor = false;
    this.timesBlocked = 0;
    this.workflowInstance = new WorkflowInstance();
    this.lifecycleManager = lifecycleManager;
    this.priority = Priority.getDefault();
  }

  /**
   * Cleans the dynamic and static Metadata for this WorkflowProcessor as
   * defined by the {@link #LOCAL_KEYS} parameter. All keys and values belonging
   * to that group will be removed from the processor met.
   * 
   * @return A cleansed version of the static and dynamic metadata, merged, and
   *         with {@link #LOCAL_KEYS} removed.
   */
  public Metadata getPassThroughDynamicMetadata() {
    Metadata passThroughMet = new Metadata(this.dynamicMetadata);
    passThroughMet.removeMetadata(LOCAL_KEYS);
    if (this.dynamicMetadata.getAllMetadata(LOCAL_KEYS) != null)
      for (String key : this.dynamicMetadata.getAllMetadata(LOCAL_KEYS))
        passThroughMet.removeMetadata(key);
    if (this.staticMetadata.getAllMetadata(LOCAL_KEYS) != null)
      for (String key : this.staticMetadata.getAllMetadata(LOCAL_KEYS))
        passThroughMet.removeMetadata(key);
    return passThroughMet;
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
   * @return the executionType
   */
  public String getExecutionType() {
    return executionType;
  }

  /**
   * @param executionType
   *          the executionType to set
   */
  public void setExecutionType(String executionType) {
    this.executionType = executionType;
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
   * @return the state
   */
  public WorkflowState getState() {
    return state;
  }

  /**
   * @param state
   *          the state to set
   */
  public void setState(WorkflowState state) {
    this.state = state;
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

  /**
   * @return the processorDateTimeInfo
   */
  public ProcessorDateTimeInfo getProcessorDateTimeInfo() {
    return ProcessorDateTimeInfo;
  }

  /**
   * @param processorDateTimeInfo
   *          the processorDateTimeInfo to set
   */
  public void setProcessorDateTimeInfo(
      ProcessorDateTimeInfo processorDateTimeInfo) {
    ProcessorDateTimeInfo = processorDateTimeInfo;
  }

  /**
   * @return the priority
   */
  public Priority getPriority() {
    return priority;
  }

  /**
   * @param priority
   *          the priority to set
   */
  public void setPriority(Priority priority) {
    this.priority = priority;
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
   * @return the staticMetadata
   */
  public Metadata getStaticMetadata() {
    return staticMetadata;
  }

  /**
   * @param staticMetadata
   *          the staticMetadata to set
   */
  public void setStaticMetadata(Metadata staticMetadata) {
    this.staticMetadata = staticMetadata;
  }

  /**
   * @return the dynamicMetadata
   */
  public Metadata getDynamicMetadata() {
    return dynamicMetadata;
  }

  /**
   * @param dynamicMetadata
   *          the dynamicMetadata to set
   */
  public void setDynamicMetadata(Metadata dynamicMetadata) {
    this.dynamicMetadata = dynamicMetadata;
  }

  /**
   * @return the isConditionProcessor
   */
  public boolean isConditionProcessor() {
    return isConditionProcessor;
  }

  /**
   * @param isConditionProcessor
   *          the isConditionProcessor to set
   */
  public void setConditionProcessor(boolean isConditionProcessor) {
    this.isConditionProcessor = isConditionProcessor;
  }

  /**
   * @return the timesBlocked
   */
  public int getTimesBlocked() {
    return timesBlocked;
  }

  /**
   * @param timesBlocked
   *          the timesBlocked to set
   */
  public void setTimesBlocked(int timesBlocked) {
    this.timesBlocked = timesBlocked;
  }
  
  /**
   * @return the lifecycleManager
   */
  public WorkflowLifecycleManager getLifecycleManager() {
    return lifecycleManager;
  }

  /**
   * @param lifecycleManager the lifecycleManager to set
   */
  public void setLifecycleManager(WorkflowLifecycleManager lifecycleManager) {
    this.lifecycleManager = lifecycleManager;
  }  



  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(WorkflowProcessor workflowProcessor) {
    return this.priority.compareTo(workflowProcessor.priority);
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

  protected boolean passedPreConditions() {
    if (this.getPreConditions() != null) {
      return this.getPreConditions().getState().getName().equals("Success");
    } else {
      return true;
    }
  }

  protected boolean passedPostConditions() {
    if (this.getPostConditions() != null) {
      return this.getPostConditions().getState().getName().equals("Success");
    } else {
      return true;
    }
  }

  protected boolean containsCategory(
      List<WorkflowProcessor> workflowProcessors, String categoryName) {
    for (WorkflowProcessor workflowProcessor : workflowProcessors)
      if (workflowProcessor.getState().getCategory().getName()
          .equals(categoryName))
        return true;
    return false;
  }
  
  protected Metadata mergeMetadata(Metadata m1, Metadata m2) {
    HashMap<String, LinkedHashSet<String>> merge = new HashMap<String, LinkedHashSet<String>>();
    List<Metadata> metadatas = Arrays.asList(m1, m2);
    for (Metadata m : metadatas) {
      for (String key : m.getAllKeys()) {
        LinkedHashSet<String> values = merge.get(key);
        if (values == null)
          values = new LinkedHashSet<String>();
        values.addAll(m.getAllMetadata(key));
        merge.put(key, values);
      }
    }
    Metadata m = new Metadata();
    for (Entry<String, LinkedHashSet<String>> entry : merge.entrySet())
      m.addMetadata(entry.getKey(), new Vector<String>(entry.getValue()));
    return m;
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
    if (containsCategory(this.getSubProcessors(), "done")) {
      List<WorkflowProcessor> failedSubProcessors = getWorkflowProcessorsByState(
          this.getSubProcessors(), "Failure");
      if (this.minReqSuccessfulSubProcessors != -1
          && failedSubProcessors.size() > (this.getSubProcessors().size() - this.minReqSuccessfulSubProcessors))
        return lifecycleManager.getDefaultLifecycle().createState("ResultsFailure",
            "results", "More than the allowed number of sub-processors failed");
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
      if (allProcessorsSameCategory(this.getSubProcessors(), "done"))
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
   * Recursively set a WorkflowProcessor and its sub-processor's dynamic
   * Metadata, along with the associated metadata of the processor (and
   * sub-processor)'s pre- and post- conditions.
   * 
   * @param dynamicMetadata
   *          The dynamic {@link Metadata} to propogate.
   */
  protected synchronized void setDynamicMetadataRecur(Metadata dynamicMetadata) {
    if (dynamicMetadata != null) {
      for (WorkflowProcessor subProcessor : this.getSubProcessors())
        subProcessor.setDynamicMetadataRecur(dynamicMetadata);
      if (this.getPreConditions() != null)
        this.getPreConditions().setDynamicMetadataRecur(dynamicMetadata);
      if (this.getPostConditions() != null)
        this.getPostConditions().setDynamicMetadataRecur(dynamicMetadata);
      this.setDynamicMetadata(dynamicMetadata);
    } else {
      LOG.log(Level.WARNING,
          "Attempt to set null dynamic metadata for workflow instance: id: ["
              + workflowInstance.getId() + "]");
    }
  }

  /**
   * Verifies that all provided WorkflowProcessors are in a state belonging to
   * the given categoryName.
   * 
   * @param workflowProcessors
   *          The {@link List} of WorkflowProcessors to inspect.
   * @param categoryName
   *          The name of the WorkflowState's category to check against.
   * @return True if they are all in the same category, false otherwise.
   */
  protected boolean allProcessorsSameCategory(
      List<WorkflowProcessor> workflowProcessors, String categoryName) {
    for (WorkflowProcessor workflowProcessor : workflowProcessors)
      if (!workflowProcessor.getState().getCategory().getName()
          .equals(categoryName))
        return false;
    return true;
  }

  /**
   * Sub-selects all WorkflowProcessors provided by the provided state
   * identified by stateName.
   * 
   * @param workflowProcessors
   *          The {@link List} of WorkflowProcessors to subset.
   * 
   * @param stateName
   *          The name of the state to subset by.
   * @return A subset version of the provided {@link List} of
   *         WorkflowProcessors.
   */
  protected List<WorkflowProcessor> getWorkflowProcessorsByState(
      List<WorkflowProcessor> workflowProcessors, String stateName) {
    List<WorkflowProcessor> returnProcessors = new Vector<WorkflowProcessor>();
    for (WorkflowProcessor workflowProcessor : workflowProcessors) {
      if (workflowProcessor.getState().equals(stateName)) {
        returnProcessors.add(workflowProcessor);
      }
    }
    return returnProcessors;
  }

  /**
   * Sub-selects all WorkflowProcessors provided by the provided category
   * identified by categoryName.
   * 
   * @param workflowProcessors
   *          The {@link List} of WorkflowProcessors to subset.
   * 
   * @param categoryName
   *          The name of the category to subset by.
   * @return A subset version of the provided {@link List} of
   *         WorkflowProcessors.
   */
  protected List<WorkflowProcessor> getWorkflowProcessorsByCategory(
      List<WorkflowProcessor> workflowProcessors, String categoryName) {
    List<WorkflowProcessor> returnProcessors = new Vector<WorkflowProcessor>();
    for (WorkflowProcessor workflowProcessor : workflowProcessors) {
      if (workflowProcessor.getState().getCategory().getName()
          .equals(categoryName)) {
        returnProcessors.add(workflowProcessor);
      }
    }
    return returnProcessors;
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

  // FIXME: grab the rest of this class from the wengine-branch
  // and drop it in and start working through the errors.
}
