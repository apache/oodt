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
import org.apache.oodt.cas.workflow.state.WorkflowState;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * The engine that executes and monitors {@link TaskInstance}s, which are
 * the physical executing representation of the abtract {@link WorkflowModel}s
 * provided.
 * </p>
 * 
 */
public interface WorkflowEngine {
	
	public void shutdown() throws EngineException;
	
	public void pauseRunner() throws EngineException;
	
	public void resumeRunner() throws EngineException;
	
	public Date getLaunchDate() throws EngineException;
	
    public String startWorkflow(WorkflowGraph workflow, Metadata inputMetadata) throws EngineException;

    public String startWorkflow(WorkflowGraph workflow, Metadata inputMetadata, Priority priority) throws EngineException;

    public String startWorkflow(String modelId, Metadata inputMetadata) throws EngineException;

    public String startWorkflow(String modelId, Metadata inputMetadata, Priority priority) throws EngineException;
    
    public void deleteWorkflow(String instanceId) throws EngineException;
    
    public void stopWorkflow(String instanceId) throws EngineException;

    public void pauseWorkflow(String instanceId) throws EngineException;

    public void resumeWorkflow(String instanceId) throws EngineException;
    
    public WorkflowModel getModel(String modelId) throws EngineException;
    
    public Set<String> getSupportedProcessorIds() throws EngineException;
    
    public List<WorkflowModel> getModels() throws EngineException;
    
    public WorkflowGraph getWorkflowGraph(String modelId) throws EngineException;

    public List<WorkflowGraph> getWorkflowGraphs() throws EngineException;
    
    public WorkflowInstanceRepository getInstanceRepository() throws EngineException;

    public ProcessorInfo getProcessorInfo(String instanceId, String modelId) throws EngineException;
    
    public void updateWorkflowMetadata(String instanceId, String modelId, Metadata metadata) throws EngineException;
    
    public void updateInstanceMetadata(String jobId, Metadata metadata) throws EngineException;

    public void updateWorkflowAndInstance(String instanceId, String modelId, WorkflowState state, Metadata metadata, String jobId, Metadata instanceMetadata) throws EngineException;
    
    public void setWorkflowState(String instanceId, String modelId, WorkflowState state) throws EngineException;

    public void setWorkflowPriority(String instanceId, String modelId, Priority priority) throws EngineException;
    
    public Metadata getWorkflowMetadata(String instanceId, String modelId) throws EngineException;
    
    public Metadata getInstanceMetadata(String jobId) throws EngineException;
        
    public void registerEvent(WorkflowEngineEvent event) throws EngineException;
    
    public void triggerEvent(String eventId, Metadata inputMetadata) throws EngineException;
    
    public List<WorkflowEngineEvent> getRegisteredEvents() throws EngineException;
    
    public List<WorkflowState> getSupportedStates() throws EngineException;

    public int getNumOfLoadedProcessors() throws EngineException;
    
    public int getNumOfWorkflows() throws EngineException;

    public RunnablesPage getExecutingPage(PageInfo pageInfo) throws EngineException;

    public RunnablesPage getRunnablesPage(PageInfo pageInfo) throws EngineException;
    
    public QueuePage getPage(PageInfo pageInfo) throws EngineException;

    public QueuePage getPage(PageInfo pageInfo, PageFilter filter) throws EngineException;

    public QueuePage getPage(PageInfo pageInfo, Comparator<ProcessorStub> comparator) throws EngineException;

    public QueuePage getPage(PageInfo pageInfo, PageFilter filter, Comparator<ProcessorStub> comparator) throws EngineException;
     
    public QueuePage getPage(PageInfo pageInfo, WorkflowState state) throws EngineException;

    public QueuePage getPage(PageInfo pageInfo, WorkflowState.Category category) throws EngineException;

    public QueuePage getPage(PageInfo pageInfo, String modelId) throws EngineException;

    public QueuePage getPage(PageInfo pageInfo, Map<String, List<String>> keyValPairs) throws EngineException;
    
    public QueuePage getNextPage(QueuePage page) throws EngineException;

    public ProcessorSkeleton getWorkflow(String instanceId) throws EngineException;

	public QueryPage getNextPage(QueryPage page) throws EngineException;
	
	public QueryPage getPage(PageInfo pageInfo, QueryExpression queryExpression) throws EngineException;
	
	public List<Metadata> getMetadata(QueryPage page) throws EngineException;
	
}
