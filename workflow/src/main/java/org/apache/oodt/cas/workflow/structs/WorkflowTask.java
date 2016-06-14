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

package org.apache.oodt.cas.workflow.structs;

//JDK imports
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * 
 * A Workflow task, or job, or process.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowTask {

  /* a unique ID for the task */
  protected String taskId = null;

  /* the name of the task */
  protected String taskName = null;

  /* the static configuration parameters for the task */
  protected WorkflowTaskConfiguration taskConfig = null;

  /* pre conditions for this task */
  protected List<WorkflowCondition> preConditions = null;

  /* post conditions for this task */
  protected List<WorkflowCondition> postConditions = null;

  /* the actual work performing portion of this WorkflowTask */
  protected String taskInstanceClassName = null;

  /* the order for this task */
  protected int order = -1;

  /* the list of required metadata fields that need to be passed into this task */
  protected List requiredMetFields = null;

  private Date startDate;

  private Date endDate;

  /**
   * <p>
   * Default Constructor.
   * </p>
   * 
   */
  public WorkflowTask() {
    this(null, null, new WorkflowTaskConfiguration(),
        new Vector<WorkflowCondition>(), new Vector<WorkflowCondition>(),
        new Vector<String>(), null, null, null, -1);
  }

  /**
   * 
   * This constructor is now deprecated in Apache OODT 0.4, in favor of
   *
   * that explicitly specifies pre- and post- {@link WorkflowCondition}s. As
   * used, this method will set the pre-conditions via the passed in
   * {@link List} of {@link WorkflowCondition}s only.
   * 
   * @param taskId
   *          The unique ID for this WorkflowTask.
   * @param taskName
   *          The display name for this WorkflowTask.
   * @param taskConfig
   *          The static configuration parameters for this WorkflowTask.
   * @param conditions
   *          The List of conditions attached to this WorkflowTask (if any).
   * @param taskInstanceClassName
   *          The instance class name for this WorkflowTask.
   * @param order
   *          The order in which this WorkflowTask is executed within a
   *          Workflow.
   */
  @Deprecated
  public WorkflowTask(String taskId, String taskName,
      WorkflowTaskConfiguration taskConfig, List conditions,
      String taskInstanceClassName, int order) {
    this(taskId, taskName, taskConfig, conditions,
        new Vector<WorkflowCondition>(), new Vector<String>(), null,
        null, null, -1);
  }

  /**
   * Constructs a new WorkflowTask.
   * 
   * @param taskId
   *          The identifier for this task.
   * @param taskName
   *          The name for this task.
   * @param taskConfig
   *          The associated {@link WorkflowTaskConfiguration}.
   * @param requiredMetFields
   *          A {@link List} of String met field names that this task requires.
   * @param preConditions
   *          The {@link List} of pre-{@link WorkflowCondition}s.
   * @param postConditions
   *          The {@link List} of post-{@link WorkflowCondition}s.
   * @param taskInstanceClassName
   *          The implementing class name of this WorkflowTask.
   * @param startDate
   *          The time that this task started executing.
   * @param endDate
   *          The time that this task stopped executing.
   * @param order
   *          The order in which this task should be run.
   */
  public WorkflowTask(String taskId, String taskName,
      WorkflowTaskConfiguration taskConfig,
      List<WorkflowCondition> preConditions,
      List<WorkflowCondition> postConditions, List<String> requiredMetFields,
      String taskInstanceClassName, Date startDate, Date endDate, int order) {
    this.taskId = taskId;
    this.taskName = taskName;
    this.taskConfig = taskConfig;
    this.requiredMetFields = requiredMetFields;
    this.preConditions = preConditions;
    this.postConditions = postConditions;
    this.taskInstanceClassName = taskInstanceClassName;
    this.startDate = startDate;
    this.endDate = endDate;
    this.order = order;

  }

  /**
   * @return Returns the taskConfig.
   */
  public WorkflowTaskConfiguration getTaskConfig() {
    return taskConfig;
  }

  /**
   * @return the preConditions
   */
  public List<WorkflowCondition> getPreConditions() {
    return preConditions;
  }

  /**
   * @param preConditions
   *          the preConditions to set
   */
  public void setPreConditions(List<WorkflowCondition> preConditions) {
    this.preConditions = preConditions;
  }

  /**
   * @return the postConditions
   */
  public List<WorkflowCondition> getPostConditions() {
    return postConditions;
  }

  /**
   * @param postConditions
   *          the postConditions to set
   */
  public void setPostConditions(List<WorkflowCondition> postConditions) {
    this.postConditions = postConditions;
  }

  /**
   * @param taskConfig
   *          The taskConfig to set.
   */
  public void setTaskConfig(WorkflowTaskConfiguration taskConfig) {
    this.taskConfig = taskConfig;
  }

  /**
   * @return Returns the taskId.
   */
  public String getTaskId() {
    return taskId;
  }

  /**
   * @param taskId
   *          The taskId to set.
   */
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  /**
   * @return Returns the taskInstanceClassName.
   */
  public String getTaskInstanceClassName() {
    return taskInstanceClassName;
  }

  /**
   * @param taskInstanceClassName
   *          The taskInstanceClassName to set.
   */
  public void setTaskInstanceClassName(String taskInstanceClassName) {
    this.taskInstanceClassName = taskInstanceClassName;
  }

  /**
   * @return Returns the taskName.
   */
  public String getTaskName() {
    return taskName;
  }

  /**
   * @param taskName
   *          The taskName to set.
   */
  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  /**
   * This method is deprecated in favor of using {@link #getPreConditions()} or
   * {@link #getPostConditions()}. As called, will return a union of the Tasks's
   * pre- and post- {@link WorkflowCondition}s.
   * 
   * @return A {@link List} of {@link WorkflowCondition}s associated with this
   *         task.
   */
  @Deprecated
  public List getConditions() {
    List conditions = new Vector();
    conditions.addAll(this.preConditions);
    conditions.addAll(this.postConditions);
    return conditions;
  }

  /**
   * 
   * This method is depcreated in favor of {@link #setPostConditions(List)} and
   * {@link #setPreConditions(List)}, for explicitly setting the pre or post
   * conditions of this WorkflowTask.
   * 
   * To keep back compat, this method in its deprecated form will set the
   * WorkflowTask pre-conditions, as was the case before.
   * 
   * Sets the {@link List} of {@link WorkflowCondition}s associated with this
   * task.
   * 
   * @param conditions
   *          The condition {@link List}.
   */
  @Deprecated
  public void setConditions(List conditions) {
    this.preConditions = conditions;
  }

  /**
   * @return Returns the order. Don't use this method anymore -- order is not
   *         relevant to the control flow which is acutally controlled now by
   *         the surrounding workflow model.
   */
  @Deprecated
  public int getOrder() {
    return order;
  }

  /**
   * Don't use this method anymore -- order is not relevant to the control flow
   * which is acutally controlled now by the surrounding workflow model.
   * 
   * @param order
   *          The order to set.
   */
  @Deprecated
  public void setOrder(int order) {
    this.order = order;
  }

  /**
   * @return the requiredMetFields
   */
  public List getRequiredMetFields() {
    return requiredMetFields;
  }

  /**
   * @param requiredMetFields
   *          the requiredMetFields to set
   */
  public void setRequiredMetFields(List requiredMetFields) {
    this.requiredMetFields = requiredMetFields;
  }

  /**
   * @return the startDate
   */
  public Date getStartDate() {
    return startDate;
  }

  /**
   * @param startDate
   *          the startDate to set
   */
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  /**
   * @return the endDate
   */
  public Date getEndDate() {
    return endDate;
  }

  /**
   * @param endDate
   *          the endDate to set
   */
  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

}
