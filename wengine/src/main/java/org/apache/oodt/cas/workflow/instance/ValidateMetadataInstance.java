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
package org.apache.oodt.cas.workflow.instance;

//OODT imports
import org.apache.oodt.cas.workflow.metadata.ControlMetadata;
import org.apache.oodt.cas.workflow.state.results.ResultsFailureState;
import org.apache.oodt.cas.workflow.state.results.ResultsState;
import org.apache.oodt.cas.workflow.state.results.ResultsSuccessState;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * Test TaskInstance which checks to insure proper metadata flow is happening
 * between workflow processors
 *
 */
public class ValidateMetadataInstance extends TaskInstance {

	private static final String VALIDATE_KEY = "validateKey";
	
	@Override
	protected ResultsState performExecution(ControlMetadata crtlMetadata) {
		crtlMetadata.replaceLocalMetadata(this.getModelId() + "_key", this.getInstanceId());
		crtlMetadata.setAsWorkflowMetadataKey(this.getModelId() + "_key");
		
		String keyNames = crtlMetadata.getMetadata(VALIDATE_KEY);
		if (keyNames == null)
			return new ResultsSuccessState("No key name requested to look for");
		
		String[] splitKeyNames = keyNames.split(",");
		for (String keyName : splitKeyNames) {
			if (crtlMetadata.getMetadata(keyName) == null) 
				return new ResultsFailureState(keyName + " not found in metadata");
		}
		return new ResultsSuccessState("All keys '" + keyNames + "' where found in metadata");
	}

}
