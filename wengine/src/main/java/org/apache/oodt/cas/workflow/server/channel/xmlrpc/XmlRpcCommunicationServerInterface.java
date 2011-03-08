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
package org.apache.oodt.cas.workflow.server.channel.xmlrpc;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * XML-RPC communication channel server interface
 * <p>
 */
public interface XmlRpcCommunicationServerInterface {

	public boolean xmlrpc_shutdown() throws Exception;
	
	public boolean xmlrpc_pauseRunner() throws Exception;
	
	public boolean xmlrpc_resumeRunner() throws Exception;
	
	public String xmlrpc_getLaunchDate() throws Exception;

	public String xmlrpc_deleteWorkflow(String instanceId) throws Exception;

	public String xmlrpc_getInstanceMetadata(String jobId) throws Exception;

	public String xmlrpc_getInstanceRepository() throws Exception;

	public String xmlrpc_getModel(String modelId) throws Exception;

	public String xmlrpc_getModels() throws Exception;
	
	public String xmlrpc_getWorkflowGraph(String modelId) throws Exception;

	public String xmlrpc_getWorkflowGraphs() throws Exception;

	public String xmlrpc_getWorkflowStub(String instanceId) throws Exception;

    public String xmlrpc_getWorkflowStub(String instanceId, String modelId) throws Exception;
    
	public String xmlrpc_getProcessorInfo(String instanceId) throws Exception;

    public String xmlrpc_getProcessorInfo(String instanceId, String modelId) throws Exception;
	
	public String xmlrpc_getWorkflowState(String instanceId) throws Exception;
	
	public String xmlrpc_getWorkflowMetadata(String instanceId) throws Exception;
	
	public String xmlrpc_getWorkflowMetadata(String instanceId, String modelId) throws Exception;

	public String xmlrpc_pauseWorkflow(String instanceId) throws Exception;

	public String xmlrpc_resumeWorkflow(String instanceId) throws Exception;

	public String xmlrpc_setWorkflowState(String instanceId, String state) throws Exception;

	public String xmlrpc_setWorkflowState(String instanceId, String modelId, String state) throws Exception;

    public String xmlrpc_setWorkflowPriority(String instanceId, String priority) throws Exception;

    public String xmlrpc_setWorkflowPriority(String instanceId, String modelId, String priority) throws Exception;
	
	public String xmlrpc_startWorkflow(String workflow, String metadata) throws Exception;
	
	public String xmlrpc_startWorkflow(String workflow, String metadata, String priority) throws Exception;

	public String xmlrpc_startWorkflow2(String modelId, String inputMetadata) throws Exception;

	public String xmlrpc_startWorkflow2(String modelId, String inputMetadata, String priority) throws Exception;

	public String xmlrpc_stopWorkflow(String instanceId) throws Exception;

	public String xmlrpc_updateInstanceMetadata(String jobId, String metadata) throws Exception;
	
	public String xmlrpc_updateWorkflowMetadata(String instanceId, String metadata)  throws Exception;

	public String xmlrpc_updateWorkflowMetadata(String instanceId, String modelId, String metadata)  throws Exception;

    public String xmlrpc_registerEvent(String event) throws Exception;
    
    public String xmlrpc_triggerEvent(String eventId, String inputMetadata) throws Exception;
	
    public String xmlrpc_getRegisteredEvents() throws Exception;

    public String xmlrpc_getSupportedStates() throws Exception;
    
    public String xmlrpc_getNumOfLoadedProcessors() throws Exception;
    
    public String xmlrpc_getNumOfWorkflows() throws Exception;
    
    public String xmlrpc_getExecutingPage(String pageInfo) throws Exception;

    public String xmlrpc_getRunnablesPage(String pageInfo) throws Exception;

    public String xmlrpc_getPage(String pageInfo) throws Exception;

    public String xmlrpc_getPage_filtered(String pageInfo, String pageFilter) throws Exception;

    public String xmlrpc_getPageWithFilterAndComparator(String pageInfo, String pageFilter, String comparator) throws Exception;

    public String xmlrpc_getPage2(String pageInfo, String comparator) throws Exception;
         
    public String xmlrpc_getPage3(String pageInfo, String state) throws Exception;

    public String xmlrpc_getPage4(String pageInfo, String category) throws Exception;

    public String xmlrpc_getPage5(String pageInfo, String modelId) throws Exception;
    
    public String xmlrpc_getPage7(String pageInfo, String keyValPairs) throws Exception;
    
    public String xmlrpc_getNextPage(String page) throws Exception;
    
    public String xmlrpc_getWorkflow(String instanceId) throws Exception;
    
	public String xmlrpc_getNextPage2(String page) throws Exception;
	
	public String xmlrpc_getPage6(String pageInfo, String queryExpression) throws Exception;
	
	public String xmlrpc_getMetadata(String page) throws Exception;
	
}
