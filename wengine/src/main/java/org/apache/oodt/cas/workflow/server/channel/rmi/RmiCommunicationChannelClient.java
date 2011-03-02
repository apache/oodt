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
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.apache.oodt.cas.workflow.server.channel.AbstractCommunicationChannelClient;
import org.apache.oodt.cas.workflow.state.WorkflowState;

/**
 * 
 * @author bfoster
 *
 */
public class RmiCommunicationChannelClient extends
		AbstractCommunicationChannelClient {

	private URL url;
	private String name;
	
	public RmiCommunicationChannelClient(URL url, String name) throws MalformedURLException, RemoteException, NotBoundException {
		this.url = url;
		this.name = name;
	}
	
	public void shutdown() throws Exception {
		this.getRmiServer().rmi_shutdown();
	}
	
	public void pauseRunner() throws Exception {
		this.getRmiServer().rmi_pauseRunner();
	}
	
	public void resumeRunner() throws Exception {
		this.getRmiServer().rmi_resumeRunner();
	}
	
	public Date getLaunchDate() throws Exception {
		return this.serializer.deserializeObject(Date.class, this.getRmiServer().rmi_getLaunchDate());
	}
	
    public String startWorkflow(WorkflowGraph workflow, Metadata metadata) throws Exception {
		return this.getRmiServer().rmi_startWorkflow_WithModel(this.serializer.serializeObject(workflow), this.serializer.serializeObject(metadata));
    }

    public String startWorkflow(WorkflowGraph workflow, Metadata metadata, Priority priority) throws Exception {
		return this.getRmiServer().rmi_startWorkflow_WithModel(this.serializer.serializeObject(workflow), this.serializer.serializeObject(metadata), this.serializer.serializeObject(priority));
    }

    public String startWorkflow(String modelId, Metadata inputMetadata) throws Exception {
		return this.getRmiServer().rmi_startWorkflow_WithModelId(modelId, this.serializer.serializeObject(inputMetadata));
    }

    public String startWorkflow(String modelId, Metadata inputMetadata, Priority priority) throws Exception {
		return this.getRmiServer().rmi_startWorkflow_WithModelId(modelId, this.serializer.serializeObject(inputMetadata), this.serializer.serializeObject(priority));
    }

    public void deleteWorkflow(String instanceId) throws Exception {
		this.getRmiServer().rmi_deleteWorkflow(instanceId);
    }
    
    public void stopWorkflow(String instanceId) throws Exception {
		this.getRmiServer().rmi_stopWorkflow(instanceId);
    }

    public void pauseWorkflow(String instanceId) throws Exception {
		this.getRmiServer().rmi_pauseWorkflow(instanceId);
    }

    public void resumeWorkflow(String instanceId) throws Exception {
		this.getRmiServer().rmi_resumeWorkflow(instanceId);
    }
    
    public WorkflowModel getModel(String modelId) throws Exception {
		return this.serializer.deserializeObject(WorkflowModel.class, this.getRmiServer().rmi_getModel(modelId));
    }

    public WorkflowGraph getWorkflowGraph(String modelId) throws Exception {
		return this.serializer.deserializeObject(WorkflowGraph.class, this.getRmiServer().rmi_getWorkflowGraph(modelId));
    }
    
    public Set<String> getSupportedProcessorIds() throws Exception {
		return (Set<String>) this.serializer.deserializeObject(Set.class, this.getRmiServer().rmi_getSupportedProcessorIds());
    }
    
    public List<WorkflowModel> getModels() throws Exception {
		return (List<WorkflowModel>) this.serializer.deserializeObject(List.class, this.getRmiServer().rmi_getModels());
    }
    
    public List<WorkflowGraph> getWorkflowGraphs() throws Exception {
		return (List<WorkflowGraph>) this.serializer.deserializeObject(List.class, this.getRmiServer().rmi_getWorkflowGraphs());
    }
    
    public ProcessorStub getWorkflowStub(String instanceId) throws Exception {
		return this.serializer.deserializeObject(ProcessorStub.class, this.getRmiServer().rmi_getWorkflowStub(instanceId));
    }

    public ProcessorStub getWorkflowStub(String instanceId, String modelId) throws Exception {
		return this.serializer.deserializeObject(ProcessorStub.class, this.getRmiServer().rmi_getWorkflowStub(instanceId, modelId));
    }
    
    public ProcessorInfo getProcessorInfo(String instanceId) throws Exception {
		return this.serializer.deserializeObject(ProcessorInfo.class, this.getRmiServer().rmi_getProcessorInfo(instanceId));
    }
    
    public ProcessorInfo getProcessorInfo(String instanceId, String modelId) throws Exception {
		return this.serializer.deserializeObject(ProcessorInfo.class, this.getRmiServer().rmi_getProcessorInfo(instanceId, modelId));
    }

    public WorkflowInstanceRepository getInstanceRepository() throws Exception {
		return this.serializer.deserializeObject(WorkflowInstanceRepository.class, this.getRmiServer().rmi_getInstanceRepository());
    }

    public void updateWorkflowMetadata(String instanceId, String modelId, Metadata metadata) throws Exception {
		this.getRmiServer().rmi_updateWorkflowMetadata(instanceId, modelId, this.serializer.serializeObject(metadata));
    }
    
    public void updateInstanceMetadata(String jobId, Metadata metadata) throws Exception {
		this.getRmiServer().rmi_updateInstanceMetadata(jobId, this.serializer.serializeObject(metadata));
    }

    public void updateWorkflowAndInstance(String instanceId, String modelId, WorkflowState state, Metadata metadata, String jobId, Metadata instanceMetadata) throws Exception {
		this.getRmiServer().rmi_updateWorkflowAndInstance(instanceId, modelId, this.serializer.serializeObject(state), this.serializer.serializeObject(metadata), jobId, this.serializer.serializeObject(instanceMetadata));
    }
    
    public void setWorkflowState(String instanceId, String modelId, WorkflowState state) throws Exception {
		this.getRmiServer().rmi_setWorkflowState(instanceId, modelId, this.serializer.serializeObject(state));
    }

    public void setWorkflowPriority(String instanceId, String modelId, Priority priority) throws Exception {
		this.getRmiServer().rmi_setWorkflowPriority(instanceId, modelId, this.serializer.serializeObject(priority));
    }
    
    public Metadata getWorkflowMetadata(String instanceId) throws Exception {
		return this.serializer.deserializeObject(Metadata.class, this.getRmiServer().rmi_getWorkflowMetadata(instanceId));
    }
    
    public Metadata getWorkflowMetadata(String instanceId, String modelId) throws Exception {
		return this.serializer.deserializeObject(Metadata.class, this.getRmiServer().rmi_getWorkflowMetadata(instanceId, modelId));
    }
    
    public WorkflowState getWorkflowState(String instanceId) throws Exception {
		return this.serializer.deserializeObject(WorkflowState.class, this.getRmiServer().rmi_getWorkflowState(instanceId));
    }
    
    public Metadata getInstanceMetadata(String jobId) throws Exception {
		return this.serializer.deserializeObject(Metadata.class, this.getRmiServer().rmi_getInstanceMetadata(jobId));
    }
            
    public void registerEvent(WorkflowEngineEvent event) throws Exception {
		this.getRmiServer().rmi_registerEvent(this.serializer.serializeObject(event));
    }
    
    public void triggerEvent(String eventId, Metadata inputMetadata) throws Exception {
		this.getRmiServer().rmi_triggerEvent(eventId, this.serializer.serializeObject(inputMetadata));
    }
    
    public List<WorkflowEngineEvent> getRegisteredEvents() throws Exception {
		return (List<WorkflowEngineEvent>) this.serializer.deserializeObject(List.class, this.getRmiServer().rmi_getRegisteredEvents());
    }
    
    public List<WorkflowState> getSupportedStates() throws Exception {
		return (List<WorkflowState>) this.serializer.deserializeObject(List.class, this.getRmiServer().rmi_getSupportedStates());
    }
    
    public int getNumOfLoadedProcessors() throws Exception {
		return this.serializer.deserializeObject(Integer.class, this.getRmiServer().rmi_getNumOfLoadedProcessors());
    }
    
    public int getNumOfWorkflows() throws Exception {
		return this.serializer.deserializeObject(Integer.class, this.getRmiServer().rmi_getNumOfWorkflows());
    }
    
    public RunnablesPage getExecutingPage(PageInfo pageInfo) throws Exception {
		return this.serializer.deserializeObject(RunnablesPage.class, this.getRmiServer().rmi_getExecutingPage(this.serializer.serializeObject(pageInfo)));
    }

    public RunnablesPage getRunnablesPage(PageInfo pageInfo) throws Exception {
		return this.serializer.deserializeObject(RunnablesPage.class, this.getRmiServer().rmi_getRunnablesPage(this.serializer.serializeObject(pageInfo)));
    }
    
    public QueuePage getPage(PageInfo pageInfo) throws Exception {
		return this.serializer.deserializeObject(QueuePage.class, this.getRmiServer().rmi_getPage(this.serializer.serializeObject(pageInfo)));
    }

    public QueuePage getPage(PageInfo pageInfo, PageFilter filter) throws Exception {
		return this.serializer.deserializeObject(QueuePage.class, this.getRmiServer().rmi_getPage_WithFilter(this.serializer.serializeObject(pageInfo), this.serializer.serializeObject(filter)));
    }
    
    public QueuePage getPage(PageInfo pageInfo, Comparator<ProcessorStub> comparator) throws Exception {
		return this.serializer.deserializeObject(QueuePage.class, this.getRmiServer().rmi_getPage_WithComparator(this.serializer.serializeObject(pageInfo), this.serializer.serializeObject(comparator)));
    }
    
    public QueuePage getPage(PageInfo pageInfo, PageFilter filter, Comparator<ProcessorStub> comparator) throws Exception {
		return this.serializer.deserializeObject(QueuePage.class, this.getRmiServer().rmi_getPage_WithFilterAndComparator(this.serializer.serializeObject(pageInfo), this.serializer.serializeObject(filter), this.serializer.serializeObject(comparator)));
    }
     
    public QueuePage getPage(PageInfo pageInfo, WorkflowState state) throws Exception {
		return this.serializer.deserializeObject(QueuePage.class, this.getRmiServer().rmi_getPage_WithState(this.serializer.serializeObject(pageInfo), this.serializer.serializeObject(state)));
    }

    public QueuePage getPage(PageInfo pageInfo, WorkflowState.Category category) throws Exception {
		return this.serializer.deserializeObject(QueuePage.class, this.getRmiServer().rmi_getPage_WithCategory(this.serializer.serializeObject(pageInfo), this.serializer.serializeObject(category)));
    }

    public QueuePage getPage(PageInfo pageInfo, String modelId) throws Exception {
		return this.serializer.deserializeObject(QueuePage.class, this.getRmiServer().rmi_getPage_WithModelId(this.serializer.serializeObject(pageInfo), this.serializer.serializeObject(modelId)));
    }

    public QueuePage getPage(PageInfo pageInfo, Map<String, List<String>> keyValPairs) throws Exception {
		return this.serializer.deserializeObject(QueuePage.class, this.getRmiServer().rmi_getPage_WithMap(this.serializer.serializeObject(pageInfo), this.serializer.serializeObject(keyValPairs)));
    }
    
    public QueuePage getNextPage(QueuePage page) throws Exception {
		return this.serializer.deserializeObject(QueuePage.class, this.getRmiServer().rmi_getNextQueuePage(this.serializer.serializeObject(page)));
    }
    
    public ProcessorSkeleton getWorkflow(String instanceId) throws Exception {
		return this.serializer.deserializeObject(ProcessorSkeleton.class, this.getRmiServer().rmi_getWorkflow(instanceId));
    }
    
	public QueryPage getNextPage(QueryPage page) throws Exception {
		return this.serializer.deserializeObject(QueryPage.class, this.getRmiServer().rmi_getNextQueryPage(this.serializer.serializeObject(page)));
	}
	
	public QueryPage getPage(PageInfo pageInfo, QueryExpression queryExpression) throws Exception {
		return this.serializer.deserializeObject(QueryPage.class, this.getRmiServer().rmi_getQueryPage(this.serializer.serializeObject(pageInfo), this.serializer.serializeObject(queryExpression)));
	}
	
	public List<Metadata> getMetadata(QueryPage page) throws Exception {
		return (List<Metadata>) this.serializer.deserializeObject(List.class, this.getRmiServer().rmi_getMetadata(this.serializer.serializeObject(page)));
	}

	protected RmiCommunicationChannelServerInterface getRmiServer() throws Exception {
		return (RmiCommunicationChannelServerInterface) Naming.lookup("rmi://" + url.getHost() + ":" + url.getPort() + "/" + this.name);
	}
	
}
