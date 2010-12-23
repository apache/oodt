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
import java.util.Hashtable;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;
import org.apache.oodt.cas.workflow.exceptions.EngineException;
import org.apache.oodt.cas.workflow.page.PageFilter;
import org.apache.oodt.cas.workflow.processor.ProcessorStub;
import org.apache.oodt.cas.workflow.state.StateUtils;
import org.apache.oodt.cas.workflow.state.WorkflowState;

/**
 * 
 * @author bfoster
 *
 */
public abstract class FilteredAction extends WorkflowEngineServerAction {

	protected String categoryName;
	protected String stateName;
	protected String modelId;
	protected Hashtable<String, Object> filterKeys = new Hashtable<String, Object>();
	protected Metadata metadata;
	
	
	public PageFilter createFilter(WorkflowEngineClient weClient) throws EngineException {
		final List<WorkflowState> supportedStates = weClient.getSupportedStates();
		if (this.filterKeys.size() > 0) 
			(metadata = new Metadata()).addMetadata(filterKeys);
		return new PageFilter() {
			
			private String modelId = FilteredAction.this.modelId;
			private WorkflowState state = FilteredAction.this.stateName != null ? StateUtils.getStateByName(supportedStates, FilteredAction.this.stateName) : null;
			private WorkflowState.Category category = FilteredAction.this.categoryName != null ? StateUtils.getCategoryByName(supportedStates, FilteredAction.this.categoryName) : null;
			private Metadata metadata = FilteredAction.this.metadata;
			
			public boolean accept(ProcessorStub stub,
					Metadata cachedMetadata) {
				if (modelId != null) 
					if (!stub.getModelId().equals(modelId))
						return false;
				if (state != null) 
					if (!stub.getState().equals(state))
						return false;
				if (category != null) 
					if (!stub.getState().getCategory().equals(category))
						return false;
				if (metadata != null) {
					for (String key : metadata.getAllKeys()) {
						List<String> values = cachedMetadata.getAllMetadata(key);
						if (values == null || Collections.disjoint(metadata.getAllMetadata(key), values))
							return false;
					}
				}
				return true;
			}
		};
	}
	
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public void setCategory(String category) {
		this.categoryName = category;
	}
	
	public void setState(String state) {
		this.stateName = state;
	}
	
	public void replaceFilterMetadata(List<String> keyValues) {
		this.filterKeys.put(keyValues.get(0), keyValues.subList(1, keyValues.size()));
	}
	
}
