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
package org.apache.oodt.cas.workflow.server.action;

//JDK imports
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;
import org.apache.oodt.cas.workflow.model.repo.XmlWorkflowModelRepositoryFactory;
import org.apache.oodt.cas.workflow.priority.Priority;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Action for starting a workflow
 * <p>
 */
public class StartWorkflow extends WorkflowEngineServerAction {

	private static final Logger LOG = Logger.getLogger(StartWorkflow.class.getName());
	
	private String modelId;
	private Priority priority;
	private String modelXmlDefinition;
	private Metadata inputMetadata;
	
	public StartWorkflow() {
		this.inputMetadata = new Metadata();
	}
	
	@Override
	public void performAction(WorkflowEngineClient weClient) throws Exception {
		String instanceId = null;
		if (this.modelXmlDefinition != null) {
			XmlWorkflowModelRepositoryFactory factory = new XmlWorkflowModelRepositoryFactory();
			factory.setModelFiles(Collections.singletonList(this.modelXmlDefinition));
			instanceId = weClient.startWorkflow(factory.createModelRepository().loadGraphs(weClient.getSupportedProcessorIds()).get(this.modelId), this.inputMetadata, this.priority);
		}else if (this.modelId != null) {
			instanceId = weClient.startWorkflow(modelId, inputMetadata, this.priority);
		}else {
			LOG.log(Level.WARNING, "Failed to start workflow [ModelId = '" + this.modelId + "']");	
		}
		LOG.log(Level.INFO, "Started workflow [ModelId = '" + this.modelId + "',InstanceId = '" + instanceId + "']");	
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}
	
	public void setPriority(double priority) {
		this.priority = Priority.getPriority(priority);
	}
	
	public void setModelXmlDefinition(String modelXmlDefinition) {
		this.modelXmlDefinition = modelXmlDefinition;
	}
	
	public void replaceInputMetadata(List<String> keyValues) {
		this.inputMetadata.replaceMetadata(keyValues.get(0), keyValues.subList(1, keyValues.size()));
	}
	
}
