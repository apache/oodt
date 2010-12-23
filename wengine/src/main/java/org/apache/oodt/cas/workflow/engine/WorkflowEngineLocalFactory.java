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
import java.util.List;

//OODT imports
import org.apache.oodt.cas.workflow.engine.runner.EngineRunnerFactory;
import org.apache.oodt.cas.workflow.event.repo.WorkflowEngineEventRepositoryFactory;
import org.apache.oodt.cas.workflow.instance.repo.WorkflowInstanceRepositoryFactory;
import org.apache.oodt.cas.workflow.model.repo.WorkflowModelRepositoryFactory;
import org.apache.oodt.cas.workflow.priority.PriorityManagerFactory;
import org.apache.oodt.cas.workflow.processor.map.WorkflowProcessorMapFactory;
import org.apache.oodt.cas.workflow.processor.repo.WorkflowProcessorRepositoryFactory;
import org.apache.oodt.cas.workflow.server.channel.CommunicationChannelClientFactory;


/**
 * Factory for creating local WorkflowEngine
 * 
 * @author bfoster
 * @version $Revision$
 *
 */
public class WorkflowEngineLocalFactory implements WorkflowEngineFactory {

	private WorkflowModelRepositoryFactory modelRepoFactory;
	private WorkflowProcessorRepositoryFactory processorRepoFactory;
	private WorkflowInstanceRepositoryFactory instanceRepoFactory;
	private WorkflowProcessorMapFactory processorMapFactory;
	private PriorityManagerFactory priorityManagerFactory;
	private EngineRunnerFactory runnerFactory;
	private CommunicationChannelClientFactory communicationChannelClientFactory;
	private WorkflowEngineEventRepositoryFactory eventRepoFactory;
	private List<String> metadataKeysToCache;
	private boolean debug;
	
	public WorkflowEngineLocal createEngine() {
		try {
			return new WorkflowEngineLocal(this.modelRepoFactory.createModelRepository(), this.processorRepoFactory.createRepository(), this.instanceRepoFactory.createRepo(), this.eventRepoFactory.createRepo(), this.processorMapFactory.createProcessorMap(), this.priorityManagerFactory.createPriorityManager(), this.runnerFactory.createRunner(), this.communicationChannelClientFactory.createCommunicationChannelClient(), this.metadataKeysToCache, this.debug);
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public WorkflowModelRepositoryFactory getModelRepoFactory() {
		return modelRepoFactory;
	}

	public void setModelRepoFactory(WorkflowModelRepositoryFactory modelRepoFactory) {
		this.modelRepoFactory = modelRepoFactory;
	}

	public WorkflowProcessorRepositoryFactory getProcessorRepoFactory() {
		return processorRepoFactory;
	}

	public void setProcessorRepoFactory(WorkflowProcessorRepositoryFactory processorRepoFactory) {
		this.processorRepoFactory = processorRepoFactory;
	}

	public WorkflowInstanceRepositoryFactory getInstanceRepoFactory() {
		return instanceRepoFactory;
	}

	public void setInstanceRepoFactory(WorkflowInstanceRepositoryFactory instanceRepoFactory) {
		this.instanceRepoFactory = instanceRepoFactory;
	}

	public WorkflowEngineEventRepositoryFactory getEventRepoFactory() {
		return eventRepoFactory;
	}

	public void setEventRepoFactory(
			WorkflowEngineEventRepositoryFactory eventRepoFactory) {
		this.eventRepoFactory = eventRepoFactory;
	}

	public WorkflowProcessorMapFactory getProcessorMapFactory() {
		return processorMapFactory;
	}

	public void setProcessorMapFactory(WorkflowProcessorMapFactory processorMapFactory) {
		this.processorMapFactory = processorMapFactory;
	}

	public PriorityManagerFactory getPriorityManagerFactory() {
		return priorityManagerFactory;
	}

	public void setPriorityManagerFactory(PriorityManagerFactory priorityManagerFactory) {
		this.priorityManagerFactory = priorityManagerFactory;
	}

	public EngineRunnerFactory getRunnerFactory() {
		return runnerFactory;
	}

	public void setRunnerFactory(EngineRunnerFactory runnerFactory) {
		this.runnerFactory = runnerFactory;
	}

	public CommunicationChannelClientFactory getCommunicationChannelClientFactory() {
		return communicationChannelClientFactory;
	}

	public void setCommunicationChannelClientFactory(CommunicationChannelClientFactory communicationChannelClientFactory) {
		this.communicationChannelClientFactory = communicationChannelClientFactory;
	}
	
	public List<String> getMetadataKeysToCache() {
		return metadataKeysToCache;
	}

	public void setMetadataKeysToCache(List<String> metadataKeysToCache) {
		this.metadataKeysToCache = metadataKeysToCache;
	}

	public boolean getDebug() {
		return this.debug;
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

}
