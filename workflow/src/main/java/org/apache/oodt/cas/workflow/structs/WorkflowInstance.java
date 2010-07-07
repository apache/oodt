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
import org.apache.oodt.cas.metadata.Metadata;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A WorkflowInstance is an instantiation of the abstract description of a
 * Workflow provided by the {@link Workflow} class. WorkflowInstances have
 * status, and in general are data structures intended to be used as a means for
 * monitoring the status of an executing {@link Workflow}.
 * </p>
 * 
 */
public class WorkflowInstance {

    private Workflow workflow;

    private String id;

    private String status;

    private String currentTaskId;

    private String startDateTimeIsoStr;

    private String endDateTimeIsoStr;

    private String currentTaskStartDateTimeIsoStr;

    private String currentTaskEndDateTimeIsoStr;

    private Metadata sharedContext;

    /**
     * Default Constructor.
     * 
     */
    public WorkflowInstance() {
        sharedContext = new Metadata();
    }

    /**
     * @param workflow
     * @param id
     * @param status
     * @param currentTaskId
     * @param startDateTimeIsoStr
     * @param endDateTimeIsoStr
     * @param currentTaskStartDateTimeIsoStr
     * @param currentTaskEndDateTimeIsoStr
     * @param sharedContext
     */
    public WorkflowInstance(Workflow workflow, String id, String status,
            String currentTaskId, String startDateTimeIsoStr,
            String endDateTimeIsoStr, String currentTaskStartDateTimeIsoStr,
            String currentTaskEndDateTimeIsoStr, Metadata sharedContext) {
        super();
        this.workflow = workflow;
        this.id = id;
        this.status = status;
        this.currentTaskId = currentTaskId;
        this.startDateTimeIsoStr = startDateTimeIsoStr;
        this.endDateTimeIsoStr = endDateTimeIsoStr;
        this.currentTaskStartDateTimeIsoStr = currentTaskStartDateTimeIsoStr;
        this.currentTaskEndDateTimeIsoStr = currentTaskEndDateTimeIsoStr;
        this.sharedContext = sharedContext;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
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
     *            the status to set
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
     *            the workflow to set
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
     *            the currentTaskId to set
     */
    public void setCurrentTaskId(String currentTaskId) {
        this.currentTaskId = currentTaskId;
    }

    /**
     * @return the endDateTimeIsoStr
     */
    public String getEndDateTimeIsoStr() {
        return endDateTimeIsoStr;
    }

    /**
     * @param endDateTimeIsoStr
     *            the endDateTimeIsoStr to set
     */
    public void setEndDateTimeIsoStr(String endDateTimeIsoStr) {
        this.endDateTimeIsoStr = endDateTimeIsoStr;
    }

    /**
     * @return the startDateTimeIsoStr
     */
    public String getStartDateTimeIsoStr() {
        return startDateTimeIsoStr;
    }

    /**
     * @param startDateTimeIsoStr
     *            the startDateTimeIsoStr to set
     */
    public void setStartDateTimeIsoStr(String startDateTimeIsoStr) {
        this.startDateTimeIsoStr = startDateTimeIsoStr;
    }

    /**
     * @return the currentTaskEndDateTimeIsoStr
     */
    public String getCurrentTaskEndDateTimeIsoStr() {
        return currentTaskEndDateTimeIsoStr;
    }

    /**
     * @param currentTaskEndDateTimeIsoStr
     *            the currentTaskEndDateTimeIsoStr to set
     */
    public void setCurrentTaskEndDateTimeIsoStr(
            String currentTaskEndDateTimeIsoStr) {
        this.currentTaskEndDateTimeIsoStr = currentTaskEndDateTimeIsoStr;
    }

    /**
     * @return the currentTaskStartDateTimeIsoStr
     */
    public String getCurrentTaskStartDateTimeIsoStr() {
        return currentTaskStartDateTimeIsoStr;
    }

    /**
     * @param currentTaskStartDateTimeIsoStr
     *            the currentTaskStartDateTimeIsoStr to set
     */
    public void setCurrentTaskStartDateTimeIsoStr(
            String currentTaskStartDateTimeIsoStr) {
        this.currentTaskStartDateTimeIsoStr = currentTaskStartDateTimeIsoStr;
    }

    /**
     * @return the sharedContext
     */
    public Metadata getSharedContext() {
        return sharedContext;
    }

    /**
     * @param sharedContext
     *            the sharedContext to set
     */
    public void setSharedContext(Metadata sharedContext) {
        this.sharedContext = sharedContext;
    }

}
