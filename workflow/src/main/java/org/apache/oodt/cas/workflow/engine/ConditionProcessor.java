/**
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

//OODT import
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.structs.Priority;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * WorkflowProcessor which handles Workflow Pre/Post Conditions
 * </p>.
 */
public class ConditionProcessor extends TaskProcessor {

	public ConditionProcessor(WorkflowLifecycleManager lifecycleManager) {
		super(lifecycleManager);
		this.setConditionProcessor(true);
	}
	
	public void setPriority(Priority priority) {
		super.setPriority(Priority.getPriority(priority.getValue() - 0.1));
	}
	
	@Override
	public void setPreConditions(WorkflowProcessor preConditions) {
		//not allowed
	}
	
	@Override
	public void setPostConditions(WorkflowProcessor postConditions) {
		//not allowed
	}
	
}
