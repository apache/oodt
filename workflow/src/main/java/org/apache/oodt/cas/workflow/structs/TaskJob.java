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
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;
import org.apache.oodt.cas.workflow.system.rpc.RpcCommunicationFactory;
import org.apache.oodt.commons.util.DateConvert;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.JobInstance;
import org.apache.oodt.cas.resource.structs.exceptions.JobInputException;
import org.apache.oodt.cas.workflow.metadata.CoreMetKeys;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Resource Manager {@link Job} that runs an underlying {@link WorkflowTask}.
 * </p>.
 */
public class TaskJob implements JobInstance, WorkflowStatus, CoreMetKeys{

    /* our log stream */
    private static Logger LOG = Logger.getLogger(TaskJob.class.getName());

    /**
     * Default Constructor.
     */
    public TaskJob() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.resource.structs.JobInstance#execute(org.apache.oodt.cas.resource.structs.JobInput)
     */
    public boolean execute(JobInput in) throws JobInputException {
        if (!(in instanceof TaskJobInput)) {
            throw new JobInputException("Task Job: unknown JobInput class: ["
                    + in.getClass().getName() + "]");
        }

        // otherwise, we're all good
        TaskJobInput taskInput = (TaskJobInput) in;

        // Instantiate the Workflow Task Instance
        WorkflowTaskInstance inst = GenericWorkflowObjectFactory
                .getTaskObjectFromClassName(taskInput
                        .getWorkflowTaskInstanceClassName());

        // override what ProcessingNode we are running on
        // the workflow manager by default inserts its own host
        // as the ProcessingNode
        taskInput.getDynMetadata().replaceMetadata(PROCESSING_NODE,
                getHostname());
        updateMetadata(taskInput.getDynMetadata());

        // update status to started again (now that the job is distributed and
        // about to run
        updateStatus(STARTED, taskInput.getDynMetadata());

        // go ahead and persist the workflow instance, after we
        // save the current task start date time
        String currentTaskIsoStartDateTimeStr = DateConvert
                .isoFormat(new Date());
        setWorkflowInstanceCurrentTaskStartDateTime(
                currentTaskIsoStartDateTimeStr, taskInput.getDynMetadata());
        setWorkflowInstanceCurrentTaskEndDateTime("", taskInput
                .getDynMetadata()); /* clear this out */

        // now we just call inst with the given task cfg and metadata
        try {
            inst.run(taskInput.getDynMetadata(), taskInput.getTaskConfig());
        } catch (WorkflowTaskInstanceException e) {
            throw new JobInputException("Failed to run task", e);
        }

        String currentTaskIsoEndDateTimeStr = DateConvert.isoFormat(new Date());
        setWorkflowInstanceCurrentTaskEndDateTime(currentTaskIsoEndDateTimeStr,
                taskInput.getDynMetadata());

        // now we have to update the workflow manager with the metadata
        // that may have been updated
        updateMetadata(taskInput.getDynMetadata());

        return true;
    }

    private void updateStatus(String status, Metadata met) {
        String workflowInstId = met.getMetadata(WORKFLOW_INST_ID);
        try (WorkflowManagerClient wClient = getWmClientFromMetadata(met)) {
            if (!wClient.updateWorkflowInstanceStatus(workflowInstId, status)) {
                LOG.log(Level.WARNING,
                        "Unable to update status for workflow instance: ["
                                + workflowInstId + "] to : [" + status + "]");
            }
        } catch (Exception ignore) {
        }
    }

    private void updateMetadata(Metadata met) {
        String workflowInstId = met.getMetadata(WORKFLOW_INST_ID);

        try (WorkflowManagerClient wClient = getWmClientFromMetadata(met)) {
            if (!wClient.updateMetadataForWorkflow(workflowInstId, met)) {
                LOG.log(Level.WARNING,
                        "Unable to update Metadata for workflow instance: ["
                                + workflowInstId + "]");
            }
        } catch (Exception ignore) {
        }
    }

    private void setWorkflowInstanceCurrentTaskStartDateTime(
            String startDateTime, Metadata met) {
        String workflowInstId = met.getMetadata(WORKFLOW_INST_ID);

        try (WorkflowManagerClient wClient = getWmClientFromMetadata(met)) {
            if (!wClient.setWorkflowInstanceCurrentTaskStartDateTime(
                    workflowInstId, startDateTime)) {
                LOG.log(Level.WARNING,
                        "Unable to update start date time for workflow instance: ["
                                + workflowInstId + "]");
            }
        } catch (Exception ignore) {
        }
    }

    private void setWorkflowInstanceCurrentTaskEndDateTime(String endDateTime,
            Metadata met) {
        String workflowInstId = met.getMetadata(WORKFLOW_INST_ID);

        try (WorkflowManagerClient wClient = getWmClientFromMetadata(met)) {
            if (!wClient.setWorkflowInstanceCurrentTaskEndDateTime(
                    workflowInstId, endDateTime)) {
                LOG.log(Level.WARNING,
                        "Unable to update end date time for workflow instance: ["
                                + workflowInstId + "]");
            }
        } catch (Exception ignore) {
        }
    }

    private WorkflowManagerClient getWmClientFromMetadata(Metadata met) {
        String workflowMgrUrlStr = met.getMetadata(WORKFLOW_MANAGER_URL);
        if (workflowMgrUrlStr == null || (workflowMgrUrlStr.equals(""))) {
            // try to default to a workflow mgr on localhost
            // most likely won't work, but worth trying
            workflowMgrUrlStr = "http://localhost:9001";
        }

        return RpcCommunicationFactory.createClient(safeGetUrlFromString(workflowMgrUrlStr));
    }

    private String getHostname() {
        try {
            // Get hostname by textual representation of IP address
            InetAddress addr = InetAddress.getLocalHost();
            // Get the host name
            return addr.getHostName();
        } catch (UnknownHostException ignored) {
        }
        return null;
    }

    private URL safeGetUrlFromString(String urlStr) {
        try {
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            return null;
        }
    }

}
