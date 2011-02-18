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
package org.apache.oodt.cas.workflow.engine.runner;

//OODT imports
import org.apache.oodt.cas.workflow.instance.TaskInstance;

//JDK imports
import java.util.Map;

/**
 * 
 * @author bfoster
 *
 */
public class MappedMultiRunner extends EngineRunner {

	protected static final String DEFAULT_RUNNER = "default";
	
	private Map<String, EngineRunner> runnerMap;
	private Map<String, String> executionTypeToRunnerMap;
	
	public MappedMultiRunner(Map<String, EngineRunner> runnerMap, Map<String, String> executionTypeToRunnerMap) throws InstantiationException {
		if (!runnerMap.containsKey(DEFAULT_RUNNER))
			throw new InstantiationException("Must set default runner key '" + DEFAULT_RUNNER + "' in runner map");
		this.runnerMap = runnerMap;
		this.executionTypeToRunnerMap = executionTypeToRunnerMap;
	}
	
	@Override
	public void execute(TaskInstance workflowInstance) throws Exception {
		this.getRunner(workflowInstance).execute(workflowInstance);
	}

	@Override
	public int getOpenSlots(TaskInstance workflowInstance) throws Exception {
		return this.getRunner(workflowInstance).getOpenSlots(workflowInstance);
	}

	@Override
	public boolean hasOpenSlots(TaskInstance workflowInstance) throws Exception {
		return this.getRunner(workflowInstance).hasOpenSlots(workflowInstance);
	}
	
	@Override
	public void shutdown() throws Exception {
		for (EngineRunner runner : this.runnerMap.values())
			try { runner.shutdown(); }catch (Exception e) {}
	}
	
	private EngineRunner getRunner(TaskInstance workflowInstance) {
		String runnerId = this.executionTypeToRunnerMap.get(workflowInstance.getExecutionType());
		if (runnerId == null)
			runnerId = DEFAULT_RUNNER;
		return runnerMap.get(runnerId);
	}
	
}
