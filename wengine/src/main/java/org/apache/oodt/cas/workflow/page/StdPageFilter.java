package org.apache.oodt.cas.workflow.page;

import java.util.Collections;
import java.util.List;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.processor.ProcessorStub;
import org.apache.oodt.cas.workflow.state.WorkflowState;

public class StdPageFilter implements PageFilter {
	
	protected WorkflowState.Category category;
	protected WorkflowState state;
	protected String modelId;
	protected Metadata metadata;
	
	public boolean accept(ProcessorStub stub, Metadata cachedMetadata) {
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

	public void setCategory(WorkflowState.Category category) {
		this.category = category;
	}

	public void setState(WorkflowState state) {
		this.state = state;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

}
