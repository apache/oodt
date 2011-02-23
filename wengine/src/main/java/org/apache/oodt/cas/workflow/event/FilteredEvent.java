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
package org.apache.oodt.cas.workflow.event;

//JDK imports
import java.util.Collections;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineLocal;
import org.apache.oodt.cas.workflow.exceptions.EngineException;
import org.apache.oodt.cas.workflow.metadata.WorkflowMetKeys;
import org.apache.oodt.cas.workflow.page.PageFilter;
import org.apache.oodt.cas.workflow.processor.ProcessorStub;
import org.apache.oodt.cas.workflow.state.StateUtils;
import org.apache.oodt.cas.workflow.state.WorkflowState;

/**
 * @author bfoster
 * @version $Revision$
 * 
 * Abstract Event for delete workflows given by filter
 *
 */
public abstract class FilteredEvent extends WorkflowEngineEvent {
	
	public PageFilter createFilter(WorkflowEngineLocal engine, final Metadata eventMetadata) throws EngineException {
		final List<WorkflowState> supportedStates = engine.getSupportedStates();
		final Metadata filterMetadata = new Metadata(eventMetadata);
		filterMetadata.removeMetadata(WorkflowMetKeys.MODEL_ID);
		filterMetadata.removeMetadata(WorkflowMetKeys.STATE);
		filterMetadata.removeMetadata(WorkflowMetKeys.CATEGORY);
		return new PageFilter() {
			
			private String modelId = eventMetadata.getMetadata(WorkflowMetKeys.MODEL_ID);
			private WorkflowState state = eventMetadata.getMetadata(WorkflowMetKeys.STATE) != null ? StateUtils.getStateByName(supportedStates, eventMetadata.getMetadata(WorkflowMetKeys.STATE)) : null;
			private WorkflowState.Category category = eventMetadata.getMetadata(WorkflowMetKeys.CATEGORY) != null ? StateUtils.getCategoryByName(supportedStates, eventMetadata.getMetadata(WorkflowMetKeys.CATEGORY)) : null;
			private Metadata metadata = filterMetadata;
			
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
	
}
