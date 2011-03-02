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
package org.apache.oodt.cas.workflow.server.channel;

//OODT imports
import org.apache.oodt.cas.catalog.page.PageInfo;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.event.WorkflowEngineEvent;
import org.apache.oodt.cas.workflow.instance.repo.WorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.model.WorkflowGraph;
import org.apache.oodt.cas.workflow.model.WorkflowModel;
import org.apache.oodt.cas.workflow.page.PageFilter;
import org.apache.oodt.cas.workflow.page.QueryPage;
import org.apache.oodt.cas.workflow.page.QueuePage;
import org.apache.oodt.cas.workflow.page.RunnablesPage;
import org.apache.oodt.cas.workflow.priority.Priority;
import org.apache.oodt.cas.workflow.processor.ProcessorInfo;
import org.apache.oodt.cas.workflow.processor.ProcessorSkeleton;
import org.apache.oodt.cas.workflow.processor.ProcessorStub;
import org.apache.oodt.cas.workflow.state.WorkflowState;
import org.apache.oodt.cas.workflow.state.WorkflowState.Category;

//JDK imports
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author bfoster
 *
 */
public class MultiCommunicationChannelClient extends
		AbstractCommunicationChannelClient {

	private CommunicationChannelClient useClient;
	private List<CommunicationChannelClient> clients;
	
	public MultiCommunicationChannelClient(CommunicationChannelClient useClient, List<CommunicationChannelClient> clients) {
		this.useClient = useClient;
		this.clients = clients;
	}
	
	public void shutdown() throws Exception {
		for (CommunicationChannelClient client : this.clients)
			client.shutdown();
	}
	
	public void pauseRunner() throws Exception {
		this.useClient.pauseRunner();
	}
	
	public void resumeRunner() throws Exception {
		this.useClient.resumeRunner();
	}
	
	public void deleteWorkflow(String instanceId) throws Exception {
		useClient.deleteWorkflow(instanceId);
	}

	public RunnablesPage getExecutingPage(PageInfo pageInfo) throws Exception {
		return useClient.getExecutingPage(pageInfo);
	}

	public Metadata getInstanceMetadata(String jobId) throws Exception {
		return useClient.getInstanceMetadata(jobId);
	}

	public WorkflowInstanceRepository getInstanceRepository() throws Exception {
		return useClient.getInstanceRepository();
	}

	public Date getLaunchDate() throws Exception {
		return useClient.getLaunchDate();
	}
	
    public ProcessorStub getWorkflowStub(String instanceId) throws Exception {
    	return useClient.getWorkflowStub(instanceId);
    }
    
    public ProcessorStub getWorkflowStub(String instanceId, String modelId) throws Exception {
    	return useClient.getWorkflowStub(instanceId, modelId);
    }

	public List<Metadata> getMetadata(QueryPage page) throws Exception {
		return useClient.getMetadata(page);
	}

	public WorkflowModel getModel(String modelId) throws Exception {
		return useClient.getModel(modelId);
	}

	public List<WorkflowModel> getModels() throws Exception {
		return useClient.getModels();
	}

	public QueuePage getNextPage(QueuePage page) throws Exception {
		return useClient.getNextPage(page);
	}

	public QueryPage getNextPage(QueryPage page) throws Exception {
		return useClient.getNextPage(page);
	}

	public int getNumOfLoadedProcessors() throws Exception {
		return useClient.getNumOfLoadedProcessors();
	}

	public int getNumOfWorkflows() throws Exception {
		return useClient.getNumOfWorkflows();
	}

	public QueuePage getPage(PageInfo pageInfo) throws Exception {
		return useClient.getPage(pageInfo);
	}

	public QueuePage getPage(PageInfo pageInfo, PageFilter filter)
			throws Exception {
		return useClient.getPage(pageInfo, filter);
	}

	public QueuePage getPage(PageInfo pageInfo,
			Comparator<ProcessorStub> comparator) throws Exception {
		return useClient.getPage(pageInfo, comparator);
	}
	
	public QueuePage getPage(PageInfo pageInfo, PageFilter filter,
			Comparator<ProcessorStub> comparator) throws Exception {
		return useClient.getPage(pageInfo, filter, comparator);
	}

	public QueuePage getPage(PageInfo pageInfo, WorkflowState state)
			throws Exception {
		return useClient.getPage(pageInfo, state);
	}

	public QueuePage getPage(PageInfo pageInfo, Category category)
			throws Exception {
		return useClient.getPage(pageInfo, category);
	}

	public QueuePage getPage(PageInfo pageInfo, String modelId)
			throws Exception {
		return useClient.getPage(pageInfo, modelId);
	}

	public QueuePage getPage(PageInfo pageInfo,
			Map<String, List<String>> keyValPairs) throws Exception {
		return useClient.getPage(pageInfo, keyValPairs);
	}

	public QueryPage getPage(PageInfo pageInfo, QueryExpression queryExpression)
			throws Exception {
		return useClient.getPage(pageInfo, queryExpression);
	}

	public ProcessorInfo getProcessorInfo(String instanceId) throws Exception {
		return useClient.getProcessorInfo(instanceId);
	}
	
	public ProcessorInfo getProcessorInfo(String instanceId, String modelId)
			throws Exception {
		return useClient.getProcessorInfo(instanceId, modelId);
	}

	public List<WorkflowEngineEvent> getRegisteredEvents() throws Exception {
		return useClient.getRegisteredEvents();
	}

	public RunnablesPage getRunnablesPage(PageInfo pageInfo) throws Exception {
		return useClient.getRunnablesPage(pageInfo);
	}

	public Set<String> getSupportedProcessorIds() throws Exception {
		return useClient.getSupportedProcessorIds();
	}

	public List<WorkflowState> getSupportedStates() throws Exception {
		return useClient.getSupportedStates();
	}

	public ProcessorSkeleton getWorkflow(String instanceId) throws Exception {
		return useClient.getWorkflow(instanceId);
	}

	public WorkflowGraph getWorkflowGraph(String modelId) throws Exception {
		return useClient.getWorkflowGraph(modelId);
	}

    public WorkflowState getWorkflowState(String instanceId) throws Exception {
    	return useClient.getWorkflowState(instanceId);
	}
	
	public List<WorkflowGraph> getWorkflowGraphs() throws Exception {
		return useClient.getWorkflowGraphs();
	}

	public Metadata getWorkflowMetadata(String instanceId) throws Exception {
		return useClient.getWorkflowMetadata(instanceId);
	}
	
	public Metadata getWorkflowMetadata(String instanceId, String modelId)
			throws Exception {
		return useClient.getWorkflowMetadata(instanceId, modelId);
	}

	public void pauseWorkflow(String instanceId) throws Exception {
		useClient.pauseWorkflow(instanceId);
	}

	public void registerEvent(WorkflowEngineEvent event) throws Exception {
		useClient.registerEvent(event);
	}

	public void resumeWorkflow(String instanceId) throws Exception {
		useClient.resumeWorkflow(instanceId);
	}

	public void setWorkflowPriority(String instanceId, String modelId,
			Priority priority) throws Exception {
		useClient.setWorkflowPriority(instanceId, modelId, priority);
	}

	public void setWorkflowState(String instanceId, String modelId,
			WorkflowState state) throws Exception {
		useClient.setWorkflowState(instanceId, modelId, state);
	}

	public String startWorkflow(WorkflowGraph workflow, Metadata metadata)
			throws Exception {
		return useClient.startWorkflow(workflow, metadata);
	}

	public String startWorkflow(WorkflowGraph workflow, Metadata metadata,
			Priority priority) throws Exception {
		return useClient.startWorkflow(workflow, metadata, priority);
	}

	public String startWorkflow(String modelId, Metadata inputMetadata)
			throws Exception {
		return useClient.startWorkflow(modelId, inputMetadata);
	}

	public String startWorkflow(String modelId, Metadata inputMetadata,
			Priority priority) throws Exception {
		return useClient.startWorkflow(modelId, inputMetadata, priority);
	}

	public void stopWorkflow(String instanceId) throws Exception {
		useClient.stopWorkflow(instanceId);
	}

	public void triggerEvent(String eventId, Metadata inputMetadata)
			throws Exception {
		useClient.triggerEvent(eventId, inputMetadata);
	}

	public void updateInstanceMetadata(String jobId, Metadata metadata)
			throws Exception {
		useClient.updateInstanceMetadata(jobId, metadata);
	}

	public void updateWorkflowMetadata(String instanceId, String modelId,
			Metadata metadata) throws Exception {
		useClient.updateWorkflowMetadata(instanceId, modelId, metadata);
	}

    public void updateWorkflowAndInstance(String instanceId, String modelId, WorkflowState state, Metadata metadata, String jobId, Metadata instanceMetadata) throws Exception {
		useClient.updateWorkflowAndInstance(instanceId, modelId, state, metadata, jobId, instanceMetadata);
    }

}
