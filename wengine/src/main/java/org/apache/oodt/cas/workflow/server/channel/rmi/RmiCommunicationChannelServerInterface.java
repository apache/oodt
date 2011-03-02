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
package org.apache.oodt.cas.workflow.server.channel.rmi;

//JDK imports
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 * @author bfoster
 *
 */
public interface RmiCommunicationChannelServerInterface extends Remote {

	public void rmi_shutdown() throws RemoteException;
	
	public void rmi_pauseRunner() throws RemoteException;
	
	public void rmi_resumeRunner() throws RemoteException;
	
	public String rmi_getLaunchDate() throws RemoteException;
	
    public String rmi_startWorkflow_WithModel(String workflow, String metadata) throws RemoteException;

    public String rmi_startWorkflow_WithModel(String workflow, String metadata, String priority) throws RemoteException;

    public String rmi_startWorkflow_WithModelId(String modelId, String inputMetadata) throws RemoteException;

    public String rmi_startWorkflow_WithModelId(String modelId, String inputMetadata, String priority) throws RemoteException;

    public void rmi_deleteWorkflow(String instanceId) throws RemoteException;
    
    public void rmi_stopWorkflow(String instanceId) throws RemoteException;

    public void rmi_pauseWorkflow(String instanceId) throws RemoteException;

    public void rmi_resumeWorkflow(String instanceId) throws RemoteException;
    
    public String rmi_getModel(String modelId) throws RemoteException;

    public String rmi_getWorkflowGraph(String modelId) throws RemoteException;
    
    public String rmi_getSupportedProcessorIds() throws RemoteException;
    
    public String rmi_getModels() throws RemoteException;
    
    public String rmi_getWorkflowGraphs() throws RemoteException;

    public String rmi_getProcessorInfo(String instanceId) throws RemoteException;

    public String rmi_getProcessorInfo(String instanceId, String modelId) throws RemoteException;

    public String rmi_getInstanceRepository() throws RemoteException;

    public void rmi_updateWorkflowMetadata(String instanceId, String modelId, String metadata) throws RemoteException;
    
    public void rmi_updateInstanceMetadata(String jobId, String metadata) throws RemoteException;

    public void rmi_updateWorkflowAndInstance(String instanceId, String modelId, String state, String metadata, String jobId, String instanceMetadata) throws RemoteException;

    public void rmi_setWorkflowState(String instanceId, String modelId, String state) throws RemoteException;

    public void rmi_setWorkflowPriority(String instanceId, String modelId, String priority) throws RemoteException;

    public String rmi_getWorkflowMetadata(String instanceId) throws RemoteException;

    public String rmi_getWorkflowMetadata(String instanceId, String modelId) throws RemoteException;
    
    public String rmi_getInstanceMetadata(String jobId) throws RemoteException;
            
    public void rmi_registerEvent(String event) throws RemoteException;
    
    public void rmi_triggerEvent(String eventId, String inputMetadata) throws RemoteException;
    
    public String rmi_getRegisteredEvents() throws RemoteException;
    
    public String rmi_getSupportedStates() throws RemoteException;
    
    public String rmi_getNumOfLoadedProcessors() throws RemoteException;
    
    public String rmi_getNumOfWorkflows() throws RemoteException;
    
    public String rmi_getExecutingPage(String pageInfo) throws RemoteException;

    public String rmi_getRunnablesPage(String pageInfo) throws RemoteException;
    
    public String rmi_getPage(String pageInfo) throws RemoteException;

    public String rmi_getPage_WithFilter(String pageInfo, String filter) throws RemoteException;
    
    public String rmi_getPage_WithComparator(String pageInfo, String comparator) throws RemoteException;

    public String rmi_getPage_WithFilterAndComparator(String pageInfo, String filter, String comparator) throws RemoteException;

    public String rmi_getPage_WithState(String pageInfo, String state) throws RemoteException ;

    public String rmi_getPage_WithCategory(String pageInfo, String category) throws RemoteException;

    public String rmi_getPage_WithModelId(String pageInfo, String modelId) throws RemoteException;

    public String rmi_getPage_WithMap(String pageInfo, String keyValPairs) throws RemoteException;
    
    public String rmi_getNextQueuePage(String page) throws RemoteException;
    
    public String rmi_getWorkflow(String instanceId) throws RemoteException;
    
	public String rmi_getNextQueryPage(String page) throws RemoteException;
	
	public String rmi_getQueryPage(String pageInfo, String queryExpression) throws RemoteException;
	
	public String rmi_getMetadata(String page) throws RemoteException;
	
    public String rmi_getWorkflowStub(String instanceId) throws RemoteException;
    
    public String rmi_getWorkflowStub(String instanceId, String modelId) throws RemoteException;

	public String rmi_getWorkflowState(String instanceId) throws RemoteException;
	
}
