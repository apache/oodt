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

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.HttpTransceiver;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.util.AvroTypeFactory;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;
import java.util.Vector;

/**
 * @author radu
 *
 * <p>
 * The Avro RPC based workflow manager client.
 * </p>
 */
public class AvroRpcWorkflowManagerClient implements WorkflowManagerClient {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AvroRpcWorkflowManagerClient.class);

    private transient Transceiver client;
    private transient org.apache.oodt.cas.workflow.struct.avrotypes.WorkflowManager proxy;
    private URL workflowManagerUrl;

    public AvroRpcWorkflowManagerClient(URL url){
        workflowManagerUrl = url;
        try {
            client = new HttpTransceiver(url);
            proxy = SpecificRequestor.getClient(org.apache.oodt.cas.workflow.struct.avrotypes.WorkflowManager.class, client);
        } catch (IOException e) {
            logger.error("Error occurred when creating client for: {}", url, e);
        }
        logger.info("Client created successfully for workflow manager URL: {}", url);
    }

    @Override
    public boolean refreshRepository() throws Exception {
        return proxy.refreshRepository();
    }

    @Override
    public String executeDynamicWorkflow(List<String> taskIds, Metadata metadata) throws Exception {
        logger.debug("Executing dynamic workflow for taskIds: {}", taskIds);
        return proxy.executeDynamicWorkflow(taskIds, AvroTypeFactory.getAvroMetadata(metadata));
    }

    @Override
    public List getRegisteredEvents() throws Exception {
        return proxy.getRegisteredEvents();
    }

    @Override
    public WorkflowInstancePage getFirstPage() throws Exception {
        return AvroTypeFactory.getWorkflowInstancePage(proxy.getFirstPage());
    }

    @Override
    public WorkflowInstancePage getNextPage(WorkflowInstancePage currentPage) throws Exception {
        return AvroTypeFactory.getWorkflowInstancePage(proxy.getNextPage(AvroTypeFactory.getAvroWorkflowInstancePage(currentPage)));
    }

    @Override
    public WorkflowInstancePage getPrevPage(WorkflowInstancePage currentPage) throws Exception {
        return AvroTypeFactory.getWorkflowInstancePage(proxy.getPrevPage(AvroTypeFactory.getAvroWorkflowInstancePage(currentPage)));
    }

    @Override
    public WorkflowInstancePage getLastPage() throws Exception {
        return AvroTypeFactory.getWorkflowInstancePage(proxy.getLastPage());
    }

    @Override
    public WorkflowInstancePage paginateWorkflowInstances(int pageNum, String status) throws Exception {
        return AvroTypeFactory.getWorkflowInstancePage(proxy.paginateWorkflowInstancesOfStatus(pageNum, status));
    }

    @Override
    public WorkflowInstancePage paginateWorkflowInstances(int pageNum) throws Exception {
        return AvroTypeFactory.getWorkflowInstancePage(proxy.paginateWorkflowInstances(pageNum));
    }

    @Override
    public List getWorkflowsByEvent(String eventName) throws Exception {
        return AvroTypeFactory.getWorkflows(proxy.getWorkflowsByEvent(eventName));
    }

    @Override
    public Metadata getWorkflowInstanceMetadata(String wInstId) throws Exception {
        return AvroTypeFactory.getMetadata(proxy.getWorkflowInstanceMetadata(wInstId));
    }

    @Override
    public boolean setWorkflowInstanceCurrentTaskStartDateTime(String wInstId, String startDateTimeIsoStr) throws Exception {
        return proxy.setWorkflowInstanceCurrentTaskStartDateTime(wInstId, startDateTimeIsoStr);
    }

    @Override
    public double getWorkflowCurrentTaskWallClockMinutes(String workflowInstId) throws Exception {
        return proxy.getWorkflowCurrentTaskWallClockMinutes(workflowInstId);
    }

    @Override
    public double getWorkflowWallClockMinutes(String workflowInstId) throws Exception {
        return proxy.getWorkflowWallClockMinutes(workflowInstId);
    }

    @Override
    public boolean stopWorkflowInstance(String workflowInstId) throws Exception {
        return proxy.stopWorkflowInstance(workflowInstId);
    }

    @Override
    public boolean pauseWorkflowInstance(String workflowInstId) throws Exception {
        return proxy.pauseWorkflowInstance(workflowInstId);
    }

    @Override
    public boolean resumeWorkflowInstance(String workflowInstId) throws Exception {
        return proxy.resumeWorkflowInstance(workflowInstId);
    }

    @Override
    public boolean setWorkflowInstanceCurrentTaskEndDateTime(String wInstId, String endDateTimeIsoStr) throws Exception {
        return proxy.setWorkflowInstanceCurrentTaskEndDateTime(wInstId, endDateTimeIsoStr);
    }

    @Override
    public boolean updateWorkflowInstanceStatus(String workflowInstId, String status) throws Exception {
        logger.debug("Updating workflow instance status for instance ID: {}, status: {}", workflowInstId, status);
        return proxy.updateWorkflowInstanceStatus(workflowInstId, status);
    }

    @Override
    public boolean updateWorkflowInstance(WorkflowInstance instance) throws Exception {
        return proxy.updateWorkflowInstance(AvroTypeFactory.getAvroWorkflowInstance(instance));
    }

    @Override
    public boolean updateMetadataForWorkflow(String workflowInstId, Metadata metadata) throws Exception {
        return proxy.updateMetadataForWorkflow(workflowInstId, AvroTypeFactory.getAvroMetadata(metadata));
    }

    @Override
    public boolean sendEvent(String eventName, Metadata metadata) throws Exception {
        return proxy.handleEvent(eventName, AvroTypeFactory.getAvroMetadata(metadata));
    }

    @Override
    public WorkflowTask getTaskById(String taskId) throws Exception {
        return AvroTypeFactory.getWorkflowTask(proxy.getTaskById(taskId));
    }

    @Override
    public WorkflowCondition getConditionById(String conditionId) throws Exception {
        return AvroTypeFactory.getWorkflowCondition(proxy.getConditionById(conditionId));
    }

    @Override
    public WorkflowInstance getWorkflowInstanceById(String wInstId) throws Exception {
        return AvroTypeFactory.getWorkflowInstance(proxy.getWorkflowInstanceById(wInstId));
    }

    @Override
    public Workflow getWorkflowById(String workflowId) throws Exception {
        return AvroTypeFactory.getWorkflow(proxy.getWorkflowById(workflowId));
    }

    @Override
    public Vector getWorkflows() throws Exception {
        Vector works = new Vector();

        List<Workflow> worksList = AvroTypeFactory.getWorkflows(proxy.getWorkflows());
        for (Workflow w : worksList){
            works.add(w);
        }
        return works;
    }

    @Override
    public int getNumWorkflowInstancesByStatus(String status) throws Exception {
        return proxy.getNumWorkflowInstancesByStatus(status);
    }

    @Override
    public int getNumWorkflowInstances() throws Exception {
        return proxy.getNumWorkflowInstances();
    }

    @Override
    public Vector getWorkflowInstancesByStatus(String status) throws Exception {
        return (Vector) AvroTypeFactory.getWorkflowInstances(proxy.getWorkflowInstancesByStatus(status));

    }

    @Override
    public Vector getWorkflowInstances() throws Exception {
        List workflowInstances =  AvroTypeFactory.getWorkflowInstances(proxy.getWorkflowInstances());
        Vector vector = new Vector();
        for (Object o : workflowInstances){
            vector.add(o);
        }
        return vector;
    }

    @Override
    public URL getWorkflowManagerUrl() {
        return this.workflowManagerUrl;
    }

    @Override
    public void setWorkflowManagerUrl(URL workflowManagerUrl) {
        this.workflowManagerUrl = workflowManagerUrl;
        try {
            client = new NettyTransceiver(new InetSocketAddress(workflowManagerUrl.getHost(), workflowManagerUrl.getPort()));
            proxy = SpecificRequestor.getClient(org.apache.oodt.cas.workflow.struct.avrotypes.WorkflowManager.class, client);
        } catch (IOException e) {
            logger.error("Error occurred when setting workflow manager url: {}", workflowManagerUrl, e);
        }

    }

    @Override
    public boolean isAlive() {
        try {
            return proxy.isAlive();
        } catch (AvroRemoteException e) {
            logger.error("Error occurred when checking if WM is alive", e);
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        if (client != null) {
            client.close();
            client = null;
            logger.info("Closed workflow manager client: {}", workflowManagerUrl.toString());
        }
    }

    @Override
    public void finalize() throws IOException {
        close();
        logger.info("Finalized client");
    }
}
