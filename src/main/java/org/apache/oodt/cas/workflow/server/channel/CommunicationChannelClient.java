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

//JDK imports
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Communication Channel for client to talk to server
 * <p>
 */
public interface CommunicationChannelClient {
	
	public void shutdown() throws Exception;
	
	public void pauseRunner() throws Exception;
	
	public void resumeRunner() throws Exception;
	
	public Date getLaunchDate() throws Exception;
	
    public String startWorkflow(WorkflowGraph workflow, Metadata metadata) throws Exception;

    public String startWorkflow(WorkflowGraph workflow, Metadata metadata, Priority priority) throws Exception;

    public String startWorkflow(String modelId, Metadata inputMetadata) throws Exception;

    public String startWorkflow(String modelId, Metadata inputMetadata, Priority priority) throws Exception;

    public void deleteWorkflow(String instanceId) throws Exception;
    
    public void stopWorkflow(String instanceId) throws Exception;

    public void pauseWorkflow(String instanceId) throws Exception;

    public void resumeWorkflow(String instanceId) throws Exception;
    
    public WorkflowModel getModel(String modelId) throws Exception;

    public WorkflowGraph getWorkflowGraph(String modelId) throws Exception;
    
    public Set<String> getSupportedProcessorIds() throws Exception;
    
    public List<WorkflowModel> getModels() throws Exception;
    
    public List<WorkflowGraph> getWorkflowGraphs() throws Exception;
    
    public ProcessorInfo getProcessorInfo(String instanceId, String modelId) throws Exception;

    public WorkflowInstanceRepository getInstanceRepository() throws Exception;

    public void updateWorkflowMetadata(String instanceId, String modelId, Metadata metadata) throws Exception;
    
    public void updateInstanceMetadata(String jobId, Metadata metadata) throws Exception;

    public void updateWorkflowAndInstance(String instanceId, String modelId, WorkflowState state, Metadata metadata, String jobId, Metadata instanceMetadata) throws Exception;
    
    public void setWorkflowState(String instanceId, String modelId, WorkflowState state) throws Exception;

    public void setWorkflowPriority(String instanceId, String modelId, Priority priority) throws Exception;
    
    public Metadata getWorkflowMetadata(String instanceId, String modelId) throws Exception;
    
    public Metadata getInstanceMetadata(String jobId) throws Exception;
            
    public void registerEvent(WorkflowEngineEvent event) throws Exception;
    
    public void triggerEvent(String eventId, Metadata inputMetadata) throws Exception;
    
    public List<WorkflowEngineEvent> getRegisteredEvents() throws Exception;
    
    public List<WorkflowState> getSupportedStates() throws Exception;
    
    public int getNumOfLoadedProcessors() throws Exception; 
    
    public int getNumOfWorkflows() throws Exception;
    
    public RunnablesPage getExecutingPage(PageInfo pageInfo) throws Exception;

    public RunnablesPage getRunnablesPage(PageInfo pageInfo) throws Exception;
    
    public QueuePage getPage(PageInfo pageInfo) throws Exception;

    public QueuePage getPage(PageInfo pageInfo, PageFilter filter) throws Exception;
    
    public QueuePage getPage(PageInfo pageInfo, Comparator<ProcessorStub> comparator) throws Exception;
     
    public QueuePage getPage(PageInfo pageInfo, WorkflowState state) throws Exception;

    public QueuePage getPage(PageInfo pageInfo, WorkflowState.Category category) throws Exception;

    public QueuePage getPage(PageInfo pageInfo, String modelId) throws Exception;

    public QueuePage getPage(PageInfo pageInfo, Map<String, List<String>> keyValPairs) throws Exception;
    
    public QueuePage getNextPage(QueuePage page) throws Exception;
    
    public ProcessorSkeleton getWorkflow(String instanceId) throws Exception;
    
	public QueryPage getNextPage(QueryPage page) throws Exception;
	
	public QueryPage getPage(PageInfo pageInfo, QueryExpression queryExpression) throws Exception;
	
	public List<Metadata> getMetadata(QueryPage page) throws Exception;
	
}
