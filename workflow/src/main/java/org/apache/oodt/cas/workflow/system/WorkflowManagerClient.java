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

package org.apache.oodt.cas.workflow.system;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;

import java.io.Closeable;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author radu
 *
 * <p>
 * Base interface for client RPC implementation.
 * </p>
 */
public interface WorkflowManagerClient extends Closeable, Serializable {

    boolean refreshRepository()
            throws Exception;

    String executeDynamicWorkflow(List<String> taskIds, Metadata metadata)
            throws Exception;

    List getRegisteredEvents() throws Exception;

    WorkflowInstancePage getFirstPage() throws Exception;

    WorkflowInstancePage getNextPage(WorkflowInstancePage currentPage)
            throws Exception;

    WorkflowInstancePage getPrevPage(WorkflowInstancePage currentPage)
            throws Exception;

    WorkflowInstancePage getLastPage() throws Exception;

    WorkflowInstancePage paginateWorkflowInstances(int pageNum, String status) throws Exception;

    WorkflowInstancePage paginateWorkflowInstances(int pageNum)
            throws Exception;

    List getWorkflowsByEvent(String eventName)
            throws Exception;

    Metadata getWorkflowInstanceMetadata(String wInstId)
            throws Exception;

    boolean setWorkflowInstanceCurrentTaskStartDateTime(
            String wInstId, String startDateTimeIsoStr)
            throws Exception;

    double getWorkflowCurrentTaskWallClockMinutes(String workflowInstId)
            throws Exception;

    double getWorkflowWallClockMinutes(String workflowInstId)
            throws Exception;

    boolean stopWorkflowInstance(String workflowInstId)
            throws Exception;

    boolean pauseWorkflowInstance(String workflowInstId)
            throws Exception;

    boolean resumeWorkflowInstance(String workflowInstId)
            throws Exception;

    boolean setWorkflowInstanceCurrentTaskEndDateTime(
            String wInstId, String endDateTimeIsoStr) throws Exception;

    boolean updateWorkflowInstanceStatus(
            String workflowInstId, String status) throws Exception;

    boolean updateWorkflowInstance(WorkflowInstance instance)
            throws Exception;

    boolean updateMetadataForWorkflow(
            String workflowInstId, Metadata metadata) throws Exception;

    boolean sendEvent(String eventName, Metadata metadata)
            throws Exception;

    WorkflowTask getTaskById(String taskId) throws Exception;

    WorkflowCondition getConditionById(String conditionId)
            throws Exception;

    WorkflowInstance getWorkflowInstanceById(String wInstId)
            throws Exception;

    Workflow getWorkflowById(String workflowId)
            throws Exception;

    Vector getWorkflows()
            throws Exception;

    int getNumWorkflowInstancesByStatus(String status)
            throws Exception;

    int getNumWorkflowInstances()
            throws Exception;

    Vector getWorkflowInstancesByStatus(String status)
            throws Exception;

    Vector getWorkflowInstances()
            throws Exception;

    URL getWorkflowManagerUrl();

    boolean isAlive();

    void setWorkflowManagerUrl(URL workflowManagerUrl);
}
