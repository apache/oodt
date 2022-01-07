/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.cas.wmservices.resources;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;

/**
 * A JAX-RS resource representing a {@link WorkflowInstance}.
 *
 * @author ngimhana (Nadeeshan Gimhana)
 */
@XmlRootElement(name = "workflowInstance")
@XmlType(
    propOrder = {"workflowInstanceId", "currentTaskId", "startDate", "endDate", "timesBlocked","sharedContext","workflowState"})
@XmlAccessorType(XmlAccessType.NONE)
public class WorkflowInstanceResource {

  private String workflowInstanceId;
  private String currentTaskId;
  private String startDate;
  private String endDate;
  private int timesBlocked;
  private MetadataResource sharedContext;
  private WorkflowStateResource workflowState;

  /** Default constructor required by JAXB. */
  public WorkflowInstanceResource() {}

  /**
   * Constructor that sets the workflowInstance to JAXRS resource.
   *
   * @param workflowInstance the workflowInstance associated with the resource
   */
  public WorkflowInstanceResource(WorkflowInstance workflowInstance) {
    this.workflowInstanceId = workflowInstance.getId();
    this.currentTaskId = workflowInstance.getCurrentTaskId();
    this.startDate = workflowInstance.getStartDate().toString();
    this.timesBlocked = workflowInstance.getTimesBlocked();
    this.sharedContext = new MetadataResource(workflowInstance.getSharedContext());
    this.workflowState = new WorkflowStateResource(workflowInstance.getState());
    if (workflowInstance.getEndDate() != null){
      this.endDate = workflowInstance.getEndDate().toString();
    }
  }

  @XmlElement(name = "workflowInstanceId")
  public String getWorkflowInstanceId() {
    return workflowInstanceId;
  }

  @XmlElement(name = "currentTaskId")
  public String getCurrentTaskId() {
    return currentTaskId;
  }

  @XmlElement(name = "startDate")
  public String getStartDate() {
    return startDate;
  }

  @XmlElement(name = "endDate")
  public String getEndDate() {
    return endDate;
  }

  @XmlElement(name = "timesBlocked")
  public int getTimesBlocked() {
    return timesBlocked;
  }

  @XmlElement(name = "sharedContext")
  public MetadataResource getSharedContext(){
    return sharedContext;
  }

  @XmlElement(name = "workflowState")
  public WorkflowStateResource getWorkflowState() {
    return workflowState;
  }
}
