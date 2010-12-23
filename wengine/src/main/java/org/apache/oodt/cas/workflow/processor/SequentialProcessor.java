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
package org.apache.oodt.cas.workflow.processor;

//JDK imports
import java.util.Collections;
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.workflow.state.WorkflowState;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * WorkflowProcessor which handles running sub-processors in sequence
 * </p>.
 */
public class SequentialProcessor extends WorkflowProcessor {

	@Override
	public List<WorkflowProcessor> getRunnableSubProcessors() {
		WorkflowProcessor nextWP = this.getNext();
		if (nextWP != null)
			return Collections.singletonList(nextWP);
		else
			return new Vector<WorkflowProcessor>();
	}

	@Override
	public void handleSubProcessorMetadata(WorkflowProcessor workflowProcessor) {
		this.setDynamicMetadata(workflowProcessor.getDynamicMetadata());
		WorkflowProcessor nextWP = this.getNext();
		if (nextWP != null)
			nextWP.setDynamicMetadataRecur(workflowProcessor.getDynamicMetadata());
	}

	private WorkflowProcessor getNext() {
		for (WorkflowProcessor wp : this.getSubProcessors())
			if (!wp.getState().getCategory().equals(WorkflowState.Category.DONE))
				return wp;
		return null;
	}
	
}
