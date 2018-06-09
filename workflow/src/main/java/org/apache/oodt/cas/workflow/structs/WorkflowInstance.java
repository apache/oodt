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

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowState;
import org.apache.oodt.commons.util.DateConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;

/**
 * A WorkflowInstance is an instantiation of the abstract description of a
 * Workflow provided by the {@link Workflow} class. WorkflowInstances have
 * status, and in general are data structures intended to be used as a means for
 * monitoring the status of an executing {@link Workflow}.
 * 
 * As of Apache OODT 0.4, the internal {@link Workflow} implementation uses
 * {@link ParentChildWorkflow}, introduced as part of OODT-70, and the
 * PackagedWorkflowRepository. {@link Workflow} instances given to the class
 * will automatically convert to {@link ParentChildWorkflow} implementations
 * internally, and the existing {@link #getWorkflow()} and
 * {@link #setWorkflow(Workflow)} methods have been deprecated in favor of
 * {@link #getParentChildWorkflow()} and
 * {@link #setParentChildWorkflow(ParentChildWorkflow)} which will supersede
 * those methods, and eventually turn into their concrete implementations.
 * 
 * In addition, as of Apache OODT 0.4 the internal {@link #state} member
 * variable now uses {@link WorkflowState} for representation. This requires the
 * use of {@link org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycle} which has now moved from being simply a UI
 * utility class for the Worklow Monitor web application to actually being fully
 * integrated with the Workflow Manager. For backwards compatibility the
 * {@link #setStatus(String)} and {@link #getStatus()} methods are still
 * supported, but are deprecated. Developers using this class should move
 * towards using {@link #setState(WorkflowState)} and {@link #getState()}.
 * 
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 */
public class WorkflowInstance {

  private static final Logger logger = LoggerFactory.getLogger(WorkflowInstance.class);

  private ParentChildWorkflow workflow;

  private String id;

  private WorkflowState state;

  private String currentTaskId;

  private Date startDate;

  private Date endDate;

  private Metadata sharedContext;

  private Priority priority;

  private int timesBlocked;

  /**
   * Default Constructor.
   * 
   */
  public WorkflowInstance() {
    this(null, null, null, null, new Date(), null, new Metadata(),
        0, Priority.getDefault());
  }

  public WorkflowInstance(Workflow workflow, String id, WorkflowState state,
      String currentTaskId, Date startDate, Date endDate, 
      Metadata sharedContext, int timesBlocked, Priority priority) {
    this.workflow = workflow instanceof ParentChildWorkflow ?
            (ParentChildWorkflow) workflow : new ParentChildWorkflow(workflow != null ? workflow : new Workflow());
    this.id = id;
    this.state = state;
    this.currentTaskId = currentTaskId;
    this.startDate = startDate;
    this.endDate = endDate;
    this.sharedContext = sharedContext;
    this.timesBlocked = timesBlocked;
    this.priority = priority;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the status
   */
  @Deprecated
  public String getStatus() {
    return state != null ? state.getName() : "Null";
  }

  /**
   * Sets the current {@link WorkflowState} to the provided status.
   * 
   * @param status
   *          The provided status to set.
   */
  @Deprecated
  public void setStatus(String status) {
    WorkflowState state = new WorkflowState();
    state.setName(status);
    this.state = state;
    logger.debug("Workflow state updated to: {}", state.getName());
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
   * @return the workflow
   */
  @Deprecated
  public Workflow getWorkflow() {
    return (Workflow) workflow;
  }

  /**
   * @param workflow
   *          the workflow to set
   */
  @Deprecated
  public void setWorkflow(Workflow workflow) {
    if (workflow != null && workflow instanceof ParentChildWorkflow) {
      this.workflow = (ParentChildWorkflow) workflow;
    } else {
      if (workflow == null) {
        workflow = new Workflow();
      }
      this.workflow = new ParentChildWorkflow(workflow);
    }
  }

  /**
   * 
   * @return The workflow, with its parent/child relationships.
   */
  public ParentChildWorkflow getParentChildWorkflow() {
    return this.workflow;
  }

  /**
   * Sets the Parent Child workflow.
   * 
   * @param workflow
   *          The workflow to set.
   */
  public void setParentChildWorkflow(ParentChildWorkflow workflow) {
    this.workflow = workflow;
  }

  /**
   * @return the currentTaskId
   */
  public String getCurrentTaskId() {
    return currentTaskId;
  }

  /**
   * @param currentTaskId
   *          the currentTaskId to set
   */
  public void setCurrentTaskId(String currentTaskId) {
    this.currentTaskId = currentTaskId;
  }

  /**
   * @return the sharedContext
   */
  public Metadata getSharedContext() {
    return sharedContext;
  }

  /**
   * @param sharedContext
   *          the sharedContext to set
   */
  public void setSharedContext(Metadata sharedContext) {
    this.sharedContext = sharedContext;
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
   * Convenience method to format and return the
   *  as a {@link Date}.
   * 
   * @return {@link Date} representation of
   *         {@link #getCurrentTaskStartDateTimeIsoStr()}.
   */
  public Date getCreationDate() {
    return this.startDate;
  }

  /**
   * Convenience method to format and return the
   *  as a {@link Date}.
   * 
   * @return {@link Date} representation of
   *         {@link #getCurrentTaskEndDateTimeIsoStr()}.
   */
  public Date getFinishDate() {
    return this.endDate;
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

  /**
   * @return the endDateTimeIsoStr
   */
  @Deprecated
  public String getEndDateTimeIsoStr() {
    return this.endDate != null ? DateConvert.isoFormat(this.endDate) : null;
  }

  /**
   * @param endDateTimeIsoStr
   *          the endDateTimeIsoStr to set
   */
  @Deprecated
  public void setEndDateTimeIsoStr(String endDateTimeIsoStr) {
    if (endDateTimeIsoStr != null && !endDateTimeIsoStr.equals("")) {
      try {
        this.endDate = DateConvert.isoParse(endDateTimeIsoStr);
      } catch (ParseException e) {
        logger.error("Error when parsing end time: {}", e.getMessage());
        // fail silently besides this: it's just a setter
      }
    }
  }

  /**
   * @return the startDateTimeIsoStr
   */
  @Deprecated
  public String getStartDateTimeIsoStr() {
    return this.startDate != null ? DateConvert.isoFormat(this.startDate)
        : null;
  }

  /**
   * @param startDateTimeIsoStr
   *          the startDateTimeIsoStr to set
   */
  @Deprecated
  public void setStartDateTimeIsoStr(String startDateTimeIsoStr) {
    if (startDateTimeIsoStr != null && !startDateTimeIsoStr.equals("")) {
      try {
        this.startDate = DateConvert.isoParse(startDateTimeIsoStr);
      } catch (ParseException e) {
        logger.error("Error when parsing start time: {}", e.getMessage());
        // fail silently besides this: it's just a setter
      }
    }
  }

  /**
   * @return the currentTaskEndDateTimeIsoStr
   */
  @Deprecated
  public String getCurrentTaskEndDateTimeIsoStr() {
    return this.getTaskById(currentTaskId) != null ? 
        (this.getTaskById(currentTaskId).getEndDate() != null ? 
            DateConvert.isoFormat(this.getTaskById(currentTaskId).getEndDate())
        : null):null;
  }

  /**
   * @param currentTaskEndDateTimeIsoStr
   *          the currentTaskEndDateTimeIsoStr to set
   */
  @Deprecated
  public void setCurrentTaskEndDateTimeIsoStr(
      String currentTaskEndDateTimeIsoStr) {
    if (currentTaskEndDateTimeIsoStr != null
        && !currentTaskEndDateTimeIsoStr.equals("") && 
        this.getTaskById(currentTaskId) != null) {
      try {
        this.getTaskById(currentTaskId).
          setEndDate(DateConvert.isoParse(currentTaskEndDateTimeIsoStr));
      } catch (ParseException e) {
        logger.error("Error when parsing time: {}", e.getMessage());
        // fail silently besides this: it's just a setter
      }
    }
  }

  /**
   * @return the currentTaskStartDateTimeIsoStr
   */
  @Deprecated
  public String getCurrentTaskStartDateTimeIsoStr() {
    return this.getTaskById(currentTaskId) != null ? 
        (this.getTaskById(currentTaskId).getStartDate() != null ? DateConvert
        .isoFormat(this.getTaskById(currentTaskId).getStartDate()) : null):null;
  }

  /**
   * @param currentTaskStartDateTimeIsoStr
   *          the currentTaskStartDateTimeIsoStr to set
   */
  @Deprecated
  public void setCurrentTaskStartDateTimeIsoStr(
      String currentTaskStartDateTimeIsoStr) {
    if (currentTaskStartDateTimeIsoStr != null
        && !currentTaskStartDateTimeIsoStr.equals("") && 
        this.getTaskById(currentTaskId) != null) {
      try {
        this.getTaskById(currentTaskId).setStartDate(DateConvert
            .isoParse(currentTaskStartDateTimeIsoStr));
      } catch (ParseException e) {
        logger.error("Error when parsing time: {}", e.getMessage());
        // fail silently besides this: it's just a setter
      }
    }
  }
  
  /**
   * Returns the currently executing {@link WorkflowTask}
   * part of this instance.
   * 
   * @return The currently executing {@link WorkflowTask}
   * part of this instance.
   */
  public WorkflowTask getCurrentTask(){
    return getTaskById(currentTaskId);
  }
  

  /**
   * @return the timesBlocked
   */
  public int getTimesBlocked() {
    return timesBlocked;
  }

  /**
   * @param timesBlocked the timesBlocked to set
   */
  public void setTimesBlocked(int timesBlocked) {
    this.timesBlocked = timesBlocked;
  }
  
  
  private WorkflowTask getTaskById(String taskId){
    if(this.workflow.getTasks() != null && 
        this.workflow.getTasks().size() > 0){
      for(WorkflowTask task: this.workflow.getTasks()){
        if(task.getTaskId().equals(taskId)){
          return task;
        }
      }
      
      return null;
    }
    else {
      return null;
    }
  }
}
