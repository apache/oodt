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
import org.apache.oodt.cas.workflow.engine.WorkflowEngine;
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
import org.apache.oodt.cas.workflow.util.Serializer;

//JDK imports
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Communication Channel between for server to respond to client calls
 * <p>
 */
public abstract class AbstractCommunicationChannelServer implements CommunicationChannelServer {
	
	protected static Logger LOG = Logger.getLogger(AbstractCommunicationChannelServer.class.getName());
	
	protected WorkflowEngine workflowEngine;
	protected int port;
	protected Serializer serializer;
	
	public AbstractCommunicationChannelServer() {
		this.serializer = new Serializer();
	}
	
	public void shutdown() throws Exception {
		try {
			this.workflowEngine.shutdown();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to shutdown engine : " + e.getMessage(), e);
			throw new Exception("Failed to shutdown engine : " + e.getMessage(), e);
		}
	}
	
	public void pauseRunner() throws Exception {
		try {
			this.workflowEngine.pauseRunner();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to pause engine runner : " + e.getMessage(), e);
			throw new Exception("Failed to pause engine runner : " + e.getMessage(), e);
		}
	}
	
	public void resumeRunner() throws Exception {
		try {
			this.workflowEngine.resumeRunner();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to resume engine runner : " + e.getMessage(), e);
			throw new Exception("Failed to resume engine runner : " + e.getMessage(), e);
		}
	}
	
	public Date getLaunchDate() throws Exception {
		try {
			return this.workflowEngine.getLaunchDate();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed get launch date from engine : " + e.getMessage(), e);
			throw new Exception("Failed get launch date from engine : " + e.getMessage(), e);
		}
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public void setWorkflowEngine(WorkflowEngine workflowEngine) {
		this.workflowEngine = workflowEngine;
	}
    
	public void deleteWorkflow(String instanceId)
			throws Exception {
		try {
			this.workflowEngine.deleteWorkflow(instanceId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to delete workflow '" + instanceId + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to delete workflow '" + instanceId + "' from engine : " + e.getMessage(), e);
		}
	}

	public Metadata getInstanceMetadata(String jobId) throws Exception {
		try {
			return this.workflowEngine.getInstanceMetadata(jobId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get instance metadata for job '" + jobId + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get instance metadata for job '" + jobId + "' from engine : " + e.getMessage(), e);
		}
	}

	public WorkflowInstanceRepository getInstanceRepository() throws Exception {
		try {
			return this.workflowEngine.getInstanceRepository();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get instance repo from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get instance repo from engine : " + e.getMessage(), e);
		}
	}

	public WorkflowModel getModel(String modelId) throws Exception {
		try {
			return this.workflowEngine.getModel(modelId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get model '" + modelId + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get model '" + modelId + "' from engine : " + e.getMessage(), e);
		}
	}

	public List<WorkflowModel> getModels() throws Exception {
		try {
			return this.workflowEngine.getModels();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get models from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get models from engine : " + e.getMessage(), e);
		}
	}
	
    public WorkflowGraph getWorkflowGraph(String modelId) throws Exception {
    	try {
    		return this.workflowEngine.getWorkflowGraph(modelId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get graph '" + modelId + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get graph '" + modelId + "' from engine : " + e.getMessage(), e);
		}
    }
    
    public List<WorkflowGraph> getWorkflowGraphs() throws Exception {
    	try {
    		return this.workflowEngine.getWorkflowGraphs();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get graphs from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get graphs from engine : " + e.getMessage(), e);
		}
    }

    public Set<String> getSupportedProcessorIds() throws Exception {
    	try {
    		return this.workflowEngine.getSupportedProcessorIds();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get supported processor ids from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get supported processor ids from engine : " + e.getMessage(), e);
		}
    }
    
    public ProcessorInfo getProcessorInfo(String instanceId, String modelId) throws Exception {
    	try {
    		return this.workflowEngine.getProcessorInfo(instanceId, modelId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get processor info for workflow [instanceid='" + instanceId  + "',modelId='" + modelId + "'] from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get processor info for workflow [instanceid='" + instanceId  + "',modelId='" + modelId + "'] from engine : " + e.getMessage(), e);
		}
    }
    
	public Metadata getWorkflowMetadata(String instanceId,
			String modelId) throws Exception {
		try {
			return this.workflowEngine.getWorkflowMetadata(instanceId, modelId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get workflow metadata [instanceid='" + instanceId  + "',modelId='" + modelId + "'] from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get workflow metadata [instanceid='" + instanceId  + "',modelId='" + modelId + "'] from engine : " + e.getMessage(), e);
		}
	}

	public void pauseWorkflow(String instanceId)
			throws Exception {
		try {
			this.workflowEngine.pauseWorkflow(instanceId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to pause workflow '" + instanceId + "' : " + e.getMessage(), e);
			throw new Exception("Failed to pause workflow '" + instanceId + "' : " + e.getMessage(), e);
		}
	}

	public void resumeWorkflow(String instanceId)
			throws Exception {
		try {
			this.workflowEngine.resumeWorkflow(instanceId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to resume workflow '" + instanceId + "' : " + e.getMessage(), e);
			throw new Exception("Failed to resume workflow '" + instanceId + "' : " + e.getMessage(), e);
		}
	}

	public void setWorkflowState(String instanceId,
			String modelId, WorkflowState state) throws Exception {
		try {
			this.workflowEngine.setWorkflowState(instanceId, modelId, state);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to set state '" + state + "' for workflow [instanceid='" + instanceId  + "',modelId='" + modelId + "'] in engine : " + e.getMessage(), e);
			throw new Exception("Failed to set state '" + state + "' for workflow [instanceid='" + instanceId  + "',modelId='" + modelId + "'] in engine : " + e.getMessage(), e);
		}
	}

    public void setWorkflowPriority(String instanceId, String modelId, Priority priority) throws Exception {
    	try {
    		this.workflowEngine.setWorkflowPriority(instanceId, modelId, priority);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to set priority '" + priority + "' for workflow [instanceid='" + instanceId  + "',modelId='" + modelId + "'] in engine : " + e.getMessage(), e);
			throw new Exception("Failed to set priority '" + priority + "' for workflow [instanceid='" + instanceId  + "',modelId='" + modelId + "'] in engine : " + e.getMessage(), e);
		}
    }
	
	public String startWorkflow(WorkflowGraph workflow, Metadata inputMetadata)
			throws Exception {
		try {
			return this.workflowEngine.startWorkflow(workflow, inputMetadata);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to start workflow '" + workflow + "' with input metadata '" + (inputMetadata != null ? inputMetadata.getHashtable() : inputMetadata) + "' in engine : " + e.getMessage(), e);
			throw new Exception("Failed to start workflow '" + workflow + "' with input metadata '" + (inputMetadata != null ? inputMetadata.getHashtable() : inputMetadata) + "' in engine : " + e.getMessage(), e);
		}
	}
	
	public String startWorkflow(WorkflowGraph workflow, Metadata inputMetadata, Priority priority)
			throws Exception {
		try {
			return this.workflowEngine.startWorkflow(workflow, inputMetadata, priority);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to start workflow '" + workflow + "' with input metadata '" + (inputMetadata != null ? inputMetadata.getHashtable() : inputMetadata) + "' and priority '" + priority + "' in engine : " + e.getMessage(), e);
			throw new Exception("Failed to start workflow '" + workflow + "' with input metadata '" + (inputMetadata != null ? inputMetadata.getHashtable() : inputMetadata) + "' and priority '" + priority + "' in engine : " + e.getMessage(), e);
		}
	}

	public String startWorkflow(String modelId, Metadata inputMetadata)
			throws Exception {
		try {
			return this.workflowEngine.startWorkflow(modelId, inputMetadata);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to start workflow '" + modelId + "' with input metadata '" + (inputMetadata != null ? inputMetadata.getHashtable() : inputMetadata) + "' in engine : " + e.getMessage(), e);
			throw new Exception("Failed to start workflow '" + modelId + "' with input metadata '" + (inputMetadata != null ? inputMetadata.getHashtable() : inputMetadata) + "' in engine : " + e.getMessage(), e);
		}
	}
	
	public String startWorkflow(String modelId, Metadata inputMetadata, Priority priority)
			throws Exception {
		try {
			return this.workflowEngine.startWorkflow(modelId, inputMetadata, priority);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to start workflow '" + modelId + "' with input metadata '" + (inputMetadata != null ? inputMetadata.getHashtable() : inputMetadata) + "' and priority '" + priority + "' in engine : " + e.getMessage(), e);
			throw new Exception("Failed to start workflow '" + modelId + "' with input metadata '" + (inputMetadata != null ? inputMetadata.getHashtable() : inputMetadata) + "' and priority '" + priority + "' in engine : " + e.getMessage(), e);
		}
	}

	public void stopWorkflow(String instanceId)
			throws Exception {
		try {
			this.workflowEngine.stopWorkflow(instanceId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to stop workflow '" + instanceId + "' in engine : " + e.getMessage(), e);
			throw new Exception("Failed to stop workflow '" + instanceId + "' in engine : " + e.getMessage(), e);
		}
	}

	public void updateInstanceMetadata(String jobId, Metadata metadata) throws Exception {
		try {
			this.workflowEngine.updateInstanceMetadata(jobId, metadata);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to update instance metadata for job '" + jobId + "' in engine : " + e.getMessage(), e);
			throw new Exception("Failed to update instance metadata for job '" + jobId + "' in engine : " + e.getMessage(), e);
		}
	}

	public void updateWorkflowMetadata(String instanceId,
			String modelId, Metadata metadata) throws Exception {
		try {
			this.workflowEngine.updateWorkflowMetadata(instanceId, modelId, metadata);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to update workflow metadata [InstanceId='" + instanceId + "',ModelId='" + modelId + "'] in engine : " + e.getMessage(), e);
			throw new Exception("Failed to update workflow metadata [InstanceId='" + instanceId + "',ModelId='" + modelId + "'] in engine : " + e.getMessage(), e);
		}
	}
	
    public void updateWorkflowAndInstance(String instanceId, String modelId, WorkflowState state, Metadata metadata, String jobId, Metadata instanceMetadata) throws Exception {
		try {
			this.workflowEngine.updateWorkflowAndInstance(instanceId, modelId, state, metadata, jobId, instanceMetadata);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to update workflow and instance metadata [InstanceId='" + instanceId + "',ModelId='" + modelId + "',JobId='" + jobId + "'] in engine : " + e.getMessage(), e);
			throw new Exception("Failed to update workflow metadata [InstanceId='" + instanceId + "',ModelId='" + modelId + "',JobId='" + jobId + "'] in engine : " + e.getMessage(), e);
		}
    }

    public void registerEvent(WorkflowEngineEvent event) throws Exception {
    	try {
    		this.workflowEngine.registerEvent(event);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to register event '" + event + "' in engine : " + e.getMessage(), e);
			throw new Exception("Failed to register event '" + event + "' in engine : " + e.getMessage(), e);
		}
    }
    
    public void triggerEvent(String eventId, Metadata inputMetadata) throws Exception {
    	try {
    		this.workflowEngine.triggerEvent(eventId, inputMetadata);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to trigger event '" + eventId + "' with metadata '" + (inputMetadata != null ? inputMetadata.getHashtable() : inputMetadata) + "' in engine : " + e.getMessage(), e);
			throw new Exception("Failed to trigger event '" + eventId + "' with metadata '" + (inputMetadata != null ? inputMetadata.getHashtable() : inputMetadata) + "' in engine : " + e.getMessage(), e);
		}
    }

    public List<WorkflowEngineEvent> getRegisteredEvents() throws Exception {
    	try {
    		return this.workflowEngine.getRegisteredEvents();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get registered events from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get registered events from engine : " + e.getMessage(), e);
		}
    }
    
    public List<WorkflowState> getSupportedStates() throws Exception {
    	try {
    		return this.workflowEngine.getSupportedStates();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get supported states from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get supported states from engine : " + e.getMessage(), e);
		}
    }
    
    public int getNumOfLoadedProcessors() throws Exception {
    	try {
    		return this.workflowEngine.getNumOfLoadedProcessors();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get number of loaded processors from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get number of loaded processors from engine : " + e.getMessage(), e);
		}
    }
    
    public int getNumOfWorkflows() throws Exception {
    	try {
    		return this.workflowEngine.getNumOfWorkflows();
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get number of workflows from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get number of workflows from engine : " + e.getMessage(), e);
		}
    }
    
    public RunnablesPage getExecutingPage(PageInfo pageInfo) throws Exception {
    	try {
    		return this.workflowEngine.getExecutingPage(pageInfo);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get executing page '" + pageInfo + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get executing page '" + pageInfo + "' from engine : " + e.getMessage(), e);
		}
    }
    
    public RunnablesPage getRunnablesPage(PageInfo pageInfo) throws Exception {
    	try {
    		return this.workflowEngine.getRunnablesPage(pageInfo);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get runnbles page '" + pageInfo + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get runnbles page '" + pageInfo + "' from engine : " + e.getMessage(), e);
		}
    }
    
    public QueuePage getPage(PageInfo pageInfo) throws Exception {
    	try {
    		return this.workflowEngine.getPage(pageInfo);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get page '" + pageInfo + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get page '" + pageInfo + "' from engine : " + e.getMessage(), e);
		}
    }
    
    public QueuePage getPage(PageInfo pageInfo, PageFilter filter) throws Exception {
    	try {
    		return this.workflowEngine.getPage(pageInfo, filter);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get page '" + pageInfo + "' from engine with filter '" + filter + "' : " + e.getMessage(), e);
			throw new Exception("Failed to get page '" + pageInfo + "' from engine with filter '" + filter + "' : " + e.getMessage(), e);
		}
    }

    public QueuePage getPage(PageInfo pageInfo, Comparator<ProcessorStub> comparator) throws Exception {
    	try {
    		return this.workflowEngine.getPage(pageInfo, comparator);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get page '" + pageInfo + "' with comparator '" + comparator + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get page '" + pageInfo + "' with comparator '" + comparator + "' from engine : " + e.getMessage(), e);
		}
    }
    
    public QueuePage getPage(PageInfo pageInfo, PageFilter filter, Comparator<ProcessorStub> comparator) throws Exception {
    	try {
    		return this.workflowEngine.getPage(pageInfo, filter, comparator);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get page '" + pageInfo + "' with filter '" + filter + "' and comparator '" + comparator + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get page '" + pageInfo + "' with filter '" + filter + "' and comparator '" + comparator + "' from engine : " + e.getMessage(), e);
		}	
    }
     
    public QueuePage getPage(PageInfo pageInfo, WorkflowState state) throws Exception {
    	try {
    		return this.workflowEngine.getPage(pageInfo, state);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get page '" + pageInfo + "' of workflows in state '" + state + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get page '" + pageInfo + "' of workflows in state '" + state + "' from engine : " + e.getMessage(), e);
		}
    }

    public QueuePage getPage(PageInfo pageInfo, WorkflowState.Category category) throws Exception {
    	try {
    		return this.workflowEngine.getPage(pageInfo, category);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get page '" + pageInfo + "' of workflows in category '" + category + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get page '" + pageInfo + "' of workflows in category '" + category + "' from engine : " + e.getMessage(), e);
		}	
    }

    public QueuePage getPage(PageInfo pageInfo, String modelId) throws Exception {
    	try {
    		return this.workflowEngine.getPage(pageInfo, modelId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get page '" + pageInfo + "' of workflows of modelId '" + modelId + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get page '" + pageInfo + "' of workflows of modelId '" + modelId + "' from engine : " + e.getMessage(), e);
		}
    }
    
    public QueuePage getPage(PageInfo pageInfo, Map<String, List<String>> keyValPairs) throws Exception {
    	try {
    		return this.workflowEngine.getPage(pageInfo, keyValPairs);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get page '" + pageInfo + "' of workflows with metadata '" + keyValPairs + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get page '" + pageInfo + "' of workflows with metadata '" + keyValPairs + "' from engine : " + e.getMessage(), e);
		}
    }
    
	public QueuePage getNextPage(QueuePage page) throws Exception {
		try {
			return this.workflowEngine.getNextPage(page);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get next queue page '" + page + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get next queue page '" + page + "' from engine : " + e.getMessage(), e);
		}
	}
    
    public ProcessorSkeleton getWorkflow(String instanceId) throws Exception {
    	try {
    		return this.workflowEngine.getWorkflow(instanceId);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get next skeleton for workflow '" + instanceId + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get next skeleton for workflow '" + instanceId + "' from engine : " + e.getMessage(), e);
		}
    }
    
	public QueryPage getNextPage(QueryPage page) throws Exception {
		try {
			return this.workflowEngine.getNextPage(page);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get next query page '" + page + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get next query page '" + page + "' from engine : " + e.getMessage(), e);
		}
	}
	
	public QueryPage getPage(PageInfo pageInfo, QueryExpression queryExpression) throws Exception {
		try {
			return this.workflowEngine.getPage(pageInfo, queryExpression);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get query page '" + pageInfo + "' for query '" + queryExpression + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get query page '" + pageInfo + "' for query '" + queryExpression + "' from engine : " + e.getMessage(), e);
		}
	}
	
	public List<Metadata> getMetadata(QueryPage page) throws Exception {
		try {
			return this.workflowEngine.getMetadata(page);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get metadata for query page '" + page + "' from engine : " + e.getMessage(), e);
			throw new Exception("Failed to get metadata for query page '" + page + "' from engine : " + e.getMessage(), e);
		}
	}
	
}
