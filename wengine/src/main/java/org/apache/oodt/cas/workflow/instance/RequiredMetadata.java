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

//JDK imports
import java.util.List;

//OODT imports
import org.apache.oodt.cas.workflow.metadata.ControlMetadata;
import org.apache.oodt.cas.workflow.state.results.ResultsFailureState;
import org.apache.oodt.cas.workflow.state.results.ResultsState;
import org.apache.oodt.cas.workflow.state.results.ResultsSuccessState;

/**
 * 
 * @author bfoster
 *
 */
public class RequiredMetadata extends TaskInstance {
	
	public static final String REQUIRED_METADATA = "RequiredMetadata";
	
	@Override
	protected ResultsState performExecution(ControlMetadata crtlMetadata) {
		List<String> requiredMetadata = crtlMetadata.getAllMetadata(REQUIRED_METADATA);
		if (requiredMetadata != null && requiredMetadata.size() > 0) {
			for (String requiredKey : requiredMetadata)
				if (crtlMetadata.getMetadata(requiredKey) == null)
					return new ResultsFailureState("Missing required metadata key '" + requiredKey + "'");
			return new ResultsSuccessState("All Required Metadata has been accounted for");
		}else {
			return new ResultsSuccessState("No Required Metadata");
		}
	}

}
