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
package org.apache.oodt.cas.workflow.engine;

//JDK imports
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
import org.apache.oodt.cas.workflow.exceptions.EngineException;
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
import org.apache.oodt.cas.workflow.server.channel.CommunicationChannelClient;
import org.apache.oodt.cas.workflow.server.channel.xmlrpc.XmlRpcCommunicationChannelClientFactory;
import org.apache.oodt.cas.workflow.state.WorkflowState;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * WorkflowEngine client for interfacing with server WorkflowEngine
 *
 */
public class WorkflowEngineClient implements WorkflowEngine {

	protected CommunicationChannelClient client;
	
	public WorkflowEngineClient() {
		this.client = new XmlRpcCommunicationChannelClientFactory().createCommunicationChannelClient();
	}
	
	public void setCommunicationChannelClient(CommunicationChannelClient client) {
		this.client = client;
	}

	public void shutdown() throws EngineException {
		try {
			this.client.shutdown();
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
	public void pauseRunner() throws EngineException {
		try {
			this.client.pauseRunner();
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
	public void resumeRunner() throws EngineException {
		try {
			this.client.resumeRunner();
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
	public Date getLaunchDate() throws EngineException {
		try {
			return this.client.getLaunchDate();
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
	public void deleteWorkflow(String instanceId)
			throws EngineException {
		try {
			this.client.deleteWorkflow(instanceId);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}

	public Metadata getInstanceMetadata(String jobId)
			throws EngineException {
		try {
			return this.client.getInstanceMetadata(jobId);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}

	public WorkflowInstanceRepository getInstanceRepository()
			throws EngineException {
		try {
			return this.client.getInstanceRepository();
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}

	public WorkflowModel getModel(String modelId) throws EngineException {
		try {
			return this.client.getModel(modelId);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
    public Set<String> getSupportedProcessorIds() throws EngineException {
		try {
			return this.client.getSupportedProcessorIds();
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }

	public List<WorkflowModel> getModels() throws EngineException {
		try {
			return this.client.getModels();
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}

	public WorkflowGraph getWorkflowGraph(String modelId) throws EngineException {
		try {
			return this.client.getWorkflowGraph(modelId);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
    public List<WorkflowGraph> getWorkflowGraphs() throws EngineException {
		try {
			return this.client.getWorkflowGraphs();
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }
    
    public ProcessorInfo getProcessorInfo(String instanceId, String modelId) throws EngineException {
		try {
			return this.client.getProcessorInfo(instanceId, modelId);
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
	
	public Metadata getWorkflowMetadata(String instanceId,
			String modelId) throws EngineException {
		try {
			return this.client.getWorkflowMetadata(instanceId, modelId);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}

	public void pauseWorkflow(String instanceId)
			throws EngineException {
		try {
			this.client.pauseWorkflow(instanceId);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}

	public void resumeWorkflow(String instanceId)
			throws EngineException {
		try {
			this.client.resumeWorkflow(instanceId);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}

	public void setWorkflowState(String instanceId,
			String modelId, WorkflowState state) throws EngineException {
		try {
			this.client.setWorkflowState(instanceId, modelId, state);
		}catch (Exception e) {
			throw new EngineException("Failed to update workflow state : " + e.getMessage(), e);
		}
	}

    public void setWorkflowPriority(String instanceId, String modelId, Priority priority) throws EngineException {
		try {
			this.client.setWorkflowPriority(instanceId, modelId, priority);
		}catch (Exception e) {
			throw new EngineException("Failed to update workflow priority : " + e.getMessage(), e);
		}
    }
	
	public String startWorkflow(WorkflowGraph workflow, Metadata inputMetadata)
			throws EngineException {
		try {
			return this.client.startWorkflow(workflow, inputMetadata);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
    public String startWorkflow(WorkflowGraph workflow, Metadata inputMetadata, Priority priority) throws EngineException {
		try {
			return this.client.startWorkflow(workflow, inputMetadata, priority);
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }

	public String startWorkflow(String modelId, Metadata inputMetadata)
			throws EngineException {
		try {
			return this.client.startWorkflow(modelId, inputMetadata);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
    public String startWorkflow(String modelId, Metadata inputMetadata, Priority priority) throws EngineException {
		try {
			return this.client.startWorkflow(modelId, inputMetadata, priority);
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }


	public void stopWorkflow(String instanceId)	throws EngineException {
		try {
			this.client.stopWorkflow(instanceId);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}

	public void updateInstanceMetadata(String jobId, Metadata metadata)
			throws EngineException {
		try {
			this.client.updateInstanceMetadata(jobId, metadata);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}

	public void updateWorkflowMetadata(String instanceId,
			String modelId, Metadata metadata) throws EngineException {
		try {
			this.client.updateWorkflowMetadata(instanceId, modelId, metadata);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
    public void updateWorkflowAndInstance(String instanceId, String modelId, WorkflowState state, Metadata metadata, String jobId, Metadata instanceMetadata) throws EngineException {
		try {
			this.client.updateWorkflowAndInstance(instanceId, modelId, state, metadata, jobId, instanceMetadata);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
    public void registerEvent(WorkflowEngineEvent event) throws EngineException {
		try {
			this.client.registerEvent(event);
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }
    
    public void triggerEvent(String eventId, Metadata inputMetadata) throws EngineException {
		try {
			this.client.triggerEvent(eventId, inputMetadata);
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }
    
    public List<WorkflowEngineEvent> getRegisteredEvents() throws EngineException {
		try {
			return this.client.getRegisteredEvents();
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }

    public List<WorkflowState> getSupportedStates() throws EngineException {
		try {
			return this.client.getSupportedStates();
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }
    
    public int getNumOfLoadedProcessors() throws EngineException {
		try {
			return this.client.getNumOfLoadedProcessors();
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }
    
    public int getNumOfWorkflows() throws EngineException {
		try {
			return this.client.getNumOfWorkflows();
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }

	public ProcessorSkeleton getWorkflow(String instanceId) throws EngineException {
		try {
			return this.client.getWorkflow(instanceId);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
    public RunnablesPage getExecutingPage(PageInfo pageInfo) throws EngineException {
		try {
			return this.client.getExecutingPage(pageInfo);
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }
	
    public RunnablesPage getRunnablesPage(PageInfo pageInfo) throws EngineException {
		try {
			return this.client.getRunnablesPage(pageInfo);
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }
	
    public QueuePage getPage(PageInfo pageInfo) throws EngineException {
		try {
			return this.client.getPage(pageInfo);
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }
    
    public QueuePage getPage(PageInfo pageInfo, PageFilter filter) throws EngineException {
		try {
			return this.client.getPage(pageInfo, filter);
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }

    public QueuePage getPage(PageInfo pageInfo, Comparator<ProcessorStub> comparator) throws EngineException {
		try {
			return this.client.getPage(pageInfo, comparator);
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }

    public QueuePage getPage(PageInfo pageInfo, PageFilter filter, Comparator<ProcessorStub> comparator) throws EngineException {
		try {
			return this.client.getPage(pageInfo, filter, comparator);
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }
    
    public QueuePage getPage(PageInfo pageInfo, WorkflowState state) throws EngineException {
		try {
			return this.client.getPage(pageInfo, state);
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }

    public QueuePage getPage(PageInfo pageInfo, WorkflowState.Category category) throws EngineException {
		try {
			return this.client.getPage(pageInfo, category);
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }

    public QueuePage getPage(PageInfo pageInfo, String modelId) throws EngineException {
		try {
			return this.client.getPage(pageInfo, modelId);
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }
    
    public QueuePage getPage(PageInfo pageInfo, Map<String, List<String>> keyValPairs) throws EngineException {
		try {
			return this.client.getPage(pageInfo, keyValPairs);
		}catch (Exception e) {
			throw new EngineException(e);
		}
    }
   
    public QueuePage getNextPage(QueuePage page) throws EngineException {
		try {
			return this.client.getNextPage(page);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
	public QueryPage getNextPage(QueryPage page) throws EngineException {
		try {
			return this.client.getNextPage(page);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
	public QueryPage getPage(PageInfo pageInfo, QueryExpression queryExpression) throws EngineException {
		try {
			return this.client.getPage(pageInfo, queryExpression);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
	public List<Metadata> getMetadata(QueryPage page) throws EngineException {
		try {
			return this.client.getMetadata(page);
		}catch (Exception e) {
			throw new EngineException(e);
		}
	}
}
