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

//OODT imports
import java.text.ParseException;
import java.util.Date;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.commons.util.DateConvert;

/**
 * A WorkflowInstance is an instantiation of the abstract description of a
 * Workflow provided by the {@link Workflow} class. WorkflowInstances have
 * status, and in general are data structures intended to be used as a means for
 * monitoring the status of an executing {@link Workflow}.
 * 
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 */
public class WorkflowInstance {

  private Workflow workflow;

  private String id;

  private String status;

  private String currentTaskId;

  private Date startDate;

  private Date endDate;

  private Date taskStartDate;

  private Date taskEndDate;

  private Metadata sharedContext;

  private Priority priority;

  /**
   * Default Constructor.
   * 
   */
  public WorkflowInstance() {
    this(null, null, null, null, new Date(), null, null, null, new Metadata(),
        Priority.getDefault());
  }

  public WorkflowInstance(Workflow workflow, String id, String status,
      String currentTaskId, Date startDate, Date endDate, Date taskStartDate,
      Date taskEndDate, Metadata sharedContext, Priority priority) {
    this.workflow = workflow;
    this.id = id;
    this.status = status;
    this.currentTaskId = currentTaskId;
    this.startDate = startDate;
    this.endDate = endDate;
    this.taskStartDate = taskStartDate;
    this.taskEndDate = taskEndDate;
    this.sharedContext = sharedContext;
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
  public String getStatus() {
    return status;
  }

  /**
   * @param status
   *          the status to set
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * @return the workflow
   */
  public Workflow getWorkflow() {
    return workflow;
  }

  /**
   * @param workflow
   *          the workflow to set
   */
  public void setWorkflow(Workflow workflow) {
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
   * {@link #currentTaskStartDateTimeIsoStr} as a {@link Date}.
   * 
   * @return {@link Date} representation of
   *         {@link #getCurrentTaskStartDateTimeIsoStr()}.
   */
  public Date getCreationDate() {
    return this.startDate;
  }

  /**
   * Convenience method to format and return the
   * {@link #currentTaskEndDateTimeIsoStr} as a {@link Date}.
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
   * @param startDate the startDate to set
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
   * @param endDate the endDate to set
   */
  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  /**
   * @return the taskStartDate
   */
  public Date getTaskStartDate() {
    return taskStartDate;
  }

  /**
   * @param taskStartDate the taskStartDate to set
   */
  public void setTaskStartDate(Date taskStartDate) {
    this.taskStartDate = taskStartDate;
  }

  /**
   * @return the taskEndDate
   */
  public Date getTaskEndDate() {
    return taskEndDate;
  }

  /**
   * @param taskEndDate the taskEndDate to set
   */
  public void setTaskEndDate(Date taskEndDate) {
    this.taskEndDate = taskEndDate;
  }

  /**
   * @return the endDateTimeIsoStr
   */
  @Deprecated
  public String getEndDateTimeIsoStr() {
    return this.endDate != null ? 
        DateConvert.isoFormat(this.endDate):null;
  }

  /**
   * @param endDateTimeIsoStr
   *          the endDateTimeIsoStr to set
   */
  @Deprecated
  public void setEndDateTimeIsoStr(String endDateTimeIsoStr) {
    try {
      this.endDate = DateConvert.isoParse(endDateTimeIsoStr);
    } catch (ParseException e) {
      e.printStackTrace();
      // fail silently besides this: it's just a setter
    }
  }

  /**
   * @return the startDateTimeIsoStr
   */
  @Deprecated
  public String getStartDateTimeIsoStr() {
    return this.startDate != null ? 
        DateConvert.isoFormat(this.startDate):null;
  }

  /**
   * @param startDateTimeIsoStr
   *          the startDateTimeIsoStr to set
   */
  @Deprecated
  public void setStartDateTimeIsoStr(String startDateTimeIsoStr) {
    try {
      this.startDate = DateConvert.isoParse(startDateTimeIsoStr);
    } catch (ParseException e) {
      e.printStackTrace();
      // fail silently besides this: it's just a setter
    }
  }

  /**
   * @return the currentTaskEndDateTimeIsoStr
   */
  @Deprecated
  public String getCurrentTaskEndDateTimeIsoStr() {
    return this.taskEndDate != null ? 
        DateConvert.isoFormat(this.taskEndDate):null;
  }

  /**
   * @param currentTaskEndDateTimeIsoStr
   *          the currentTaskEndDateTimeIsoStr to set
   */
  @Deprecated
  public void setCurrentTaskEndDateTimeIsoStr(
      String currentTaskEndDateTimeIsoStr) {
    try {
      this.taskEndDate = DateConvert.isoParse(currentTaskEndDateTimeIsoStr);
    } catch (ParseException e) {
      e.printStackTrace();
      // fail silently besides this: it's just a setter
    }
  }

  /**
   * @return the currentTaskStartDateTimeIsoStr
   */
  @Deprecated
  public String getCurrentTaskStartDateTimeIsoStr() {
    return this.taskStartDate != null ? 
        DateConvert.isoFormat(this.taskStartDate):null;
  }

  /**
   * @param currentTaskStartDateTimeIsoStr
   *          the currentTaskStartDateTimeIsoStr to set
   */
  @Deprecated
  public void setCurrentTaskStartDateTimeIsoStr(
      String currentTaskStartDateTimeIsoStr) {
    try {
      this.taskStartDate = DateConvert.isoParse(currentTaskStartDateTimeIsoStr);
    } catch (ParseException e) {
      e.printStackTrace();
      // fail silently besides this: it's just a setter
    }
  }

}
