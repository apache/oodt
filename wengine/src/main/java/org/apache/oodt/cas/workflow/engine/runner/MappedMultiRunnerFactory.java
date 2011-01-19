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

//JDK imports
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author bfoster
 *
 */
public class MappedMultiRunnerFactory implements EngineRunnerFactory {

	private static final Logger LOG = Logger.getLogger(MappedMultiRunnerFactory.class.getName());
	
	private Map<String, EngineRunnerFactory> runnerFactoryMap;
	private Map<String, String> executionTypeToRunnerMap;
	
	public EngineRunner createRunner() {
		try {
			HashMap<String, EngineRunner> runnerMap = new HashMap<String, EngineRunner>();
			for (Entry<String, EngineRunnerFactory> entry : this.runnerFactoryMap.entrySet())
				runnerMap.put(entry.getKey(), entry.getValue().createRunner());
			return new MappedMultiRunner(runnerMap, this.executionTypeToRunnerMap);
		}catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to create instance of '" + MappedMultiRunner.class.getCanonicalName() + "' : " + e.getMessage(), e);
			return null;
		}
	}

	public void setRunnerFactoryMap(Map<String, EngineRunnerFactory> runnerFactoryMap) throws Exception {
		this.runnerFactoryMap = runnerFactoryMap;
	}
	
	public void setExecutionTypeToRunnerMap(Map<String, String> executionTypeToRunnerMap) {
		this.executionTypeToRunnerMap = executionTypeToRunnerMap;
	}

}
