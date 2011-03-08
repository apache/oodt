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
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;
import org.apache.oodt.cas.workflow.metadata.ControlMetadata;
import org.apache.oodt.cas.workflow.processor.WorkflowProcessor;
import org.apache.oodt.cas.workflow.state.WorkflowState;
import org.apache.oodt.cas.workflow.state.done.FailureState;
import org.apache.oodt.cas.workflow.state.done.StoppedState;
import org.apache.oodt.cas.workflow.state.done.SuccessState;
import org.apache.oodt.cas.workflow.state.results.ResultsBailState;
import org.apache.oodt.cas.workflow.state.results.ResultsFailureState;
import org.apache.oodt.cas.workflow.state.results.ResultsState;
import org.apache.oodt.cas.workflow.state.results.ResultsSuccessState;
import org.apache.oodt.cas.workflow.util.WorkflowUtils;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Connect Workflows Spawn/Wait Task Instance
 * </p>.
 */
public class WorkflowConnectTaskInstance extends TaskInstance {
	
	private static final Logger LOG = Logger.getLogger(WorkflowConnectTaskInstance.class.getName());
	
	public static final String N_CALCULATOR_CLASS = "WorkflowConnect/NCalculator/Class"; 
	public static final String N_MET_MOD_CLASS = "WorkflowConnect/NMetadataModifier/Class"; 
	public static final String SPAWN_MODEL_ID = "WorkflowConnect/ModelId"; 
	
	public static final String SPAWNED_WORKFLOWS = "WorkflowConnect/SpawnedWorkflows/InstanceIds";
	public static final String SPAWNED_BY_WORKFLOW = "WorkflowConnect/SpawnedByWorkflow/InstanceId";

	public static final String JOIN_METADATA = "WorkflowConnect/JoinMetadata";
	public static final String JOIN_ONLY_METADATA_KEYS = "WorkflowConnect/JoinMetadata/RestrictToKeys";
	
	private WorkflowEngineClient weClient;
	
	@Override
	public void setNotifyEngine(WorkflowEngineClient weClient) {
		super.setNotifyEngine(weClient);
		this.weClient = weClient;
	}
	
	@Override
	protected ResultsState performExecution(ControlMetadata ctrlMetadata) {
		if (ctrlMetadata.getMetadata(SPAWNED_WORKFLOWS) == null) {
			
			//Add spawning keys to localized workflow metadata keys
			Vector<String> localKeys = new Vector<String>();
			if (ctrlMetadata.getMetadata(WorkflowProcessor.LOCAL_KEYS) != null)
				localKeys.addAll(ctrlMetadata.getAllMetadata(WorkflowProcessor.LOCAL_KEYS));
			localKeys.addAll(Arrays.asList(SPAWNED_WORKFLOWS, SPAWNED_BY_WORKFLOW));
			ctrlMetadata.replaceLocalMetadata(WorkflowProcessor.LOCAL_KEYS, localKeys);
			ctrlMetadata.setAsWorkflowMetadataKey(WorkflowProcessor.LOCAL_KEYS);
			
			//Get Spawn ModelId
			String spawnModelId = ctrlMetadata.getMetadata(SPAWN_MODEL_ID);
			if (spawnModelId == null)
				return new ResultsFailureState("Must specify '" + SPAWN_MODEL_ID + "'");
			
			//Get NCalculator Class
			String nCalClass = ctrlMetadata.getMetadata(N_CALCULATOR_CLASS);
			if (nCalClass == null)
				return new ResultsFailureState("Must specify '" + N_CALCULATOR_CLASS + "'");
			
			//Load NCalculator Class
			NCalculator calculator = null;
			try {
				calculator = (NCalculator) Class.forName(nCalClass).newInstance();
			}catch (Exception e) {
				LOG.log(Level.SEVERE, "Failed to load NCalculator class '" + nCalClass + "' : " + e.getMessage(), e);
				return new ResultsFailureState("Failed to load NCalculator class '" + nCalClass + "' : " + e.getMessage());
			}

			//Load NMetModClass if specified
			String nMetModClass = ctrlMetadata.getMetadata(N_MET_MOD_CLASS);
			NMetadataModification nMetMod = null;
			if (nMetModClass != null) {
				try {
					nMetMod = (NMetadataModification) Class.forName(nMetModClass).newInstance();
				}catch (Exception e) {
					LOG.log(Level.SEVERE, "Failed to load NMetadataModification class '" + nMetModClass + "' : " + e.getMessage(), e);
					return new ResultsFailureState("Failed to load NMetadataModification class '" + nMetModClass + "' : " + e.getMessage());
				}
			}
			
			int n = -1;
			try {
				n = calculator.determineN(ctrlMetadata);
			}catch (Exception e) {
				LOG.log(Level.SEVERE, "Failed to determine N : " + e.getMessage(), e);
				return new ResultsFailureState("Failed to determine N : " + e.getMessage());
			}
			Metadata spawnWorkflowMet = ctrlMetadata.asMetadata();
			Vector<String> spawnedInstanceIds = new Vector<String>();
			for (int i = 0; i < n; i++) {
				Metadata curWorkflowMet = new Metadata(spawnWorkflowMet);
				try {
					if (nMetMod != null)
						nMetMod.prepare(i+1, n, curWorkflowMet);
					curWorkflowMet.replaceMetadata(SPAWNED_BY_WORKFLOW, this.getInstanceId());
					spawnedInstanceIds.add(this.weClient.startWorkflow(spawnModelId, curWorkflowMet));
				}catch (Exception e) {
					LOG.log(Level.SEVERE, "Failed to start workflow ModelId '" + spawnModelId + "' [i = '" + i + "'] : " + e.getMessage(), e);
					for (String spawenedInstanceId : spawnedInstanceIds) {
						try {
							this.weClient.setWorkflowState(spawenedInstanceId, new StoppedState("Spawing workflow failed to spawn sibling workflow [i = '" + i + "']"));
						}catch (Exception e2) {
							LOG.log(Level.SEVERE, "Failed to stop workflow InstanceId = '" + spawenedInstanceId + "' : " + e2.getMessage(), e2);
						}
					}
					return new ResultsFailureState("Failed to start workflow ModelId '" + spawnModelId + "' [i = '" + i + "'] : " + e.getMessage());
				}
			}
			ctrlMetadata.replaceLocalMetadata(SPAWNED_WORKFLOWS, spawnedInstanceIds);
			ctrlMetadata.setAsWorkflowMetadataKey(SPAWNED_WORKFLOWS);
			
			return new ResultsBailState("Waiting for " + n + " of " + n + " spawned workflows to complete");
		}else {
			
			int nDone = 0;
			List<String> spawnedInstanceIds = ctrlMetadata.getAllMetadata(SPAWNED_WORKFLOWS);
			for (String spawnedInstanceId : spawnedInstanceIds) {
				try {
					WorkflowState state = this.weClient.getWorkflowState(spawnedInstanceId);
					if (state instanceof FailureState)
						return new ResultsFailureState("Spawned workflow [InstanceId='" + spawnedInstanceId + "'] failed");
					else if (state instanceof SuccessState) 
						nDone++;
				}catch (Exception e) {
					return new ResultsFailureState("Failed to get state of spawned workflow [InstanceId='" + spawnedInstanceId + "']");
				}
			}
			if (nDone == spawnedInstanceIds.size()) {
				Metadata dynMet = new Metadata();
				for (String spawnedInstanceId : spawnedInstanceIds) {
					try {
						if (Boolean.parseBoolean(ctrlMetadata.getMetadata(JOIN_METADATA))) {
							if (ctrlMetadata.getMetadata(JOIN_ONLY_METADATA_KEYS) == null) {
								dynMet = WorkflowUtils.mergeMetadata(dynMet, this.weClient.getWorkflowMetadata(spawnedInstanceId));
							}else { 
								Metadata spawnedMetadata = this.weClient.getWorkflowMetadata(spawnedInstanceId);
								Metadata joinMetadata = new Metadata();
								for (String key : ctrlMetadata.getAllMetadata(JOIN_ONLY_METADATA_KEYS))
									joinMetadata.replaceMetadata(key, spawnedMetadata.getAllMetadata(key));
								dynMet = WorkflowUtils.mergeMetadata(dynMet, joinMetadata);									
							}
						}
					}catch (Exception e) {
						return new ResultsFailureState("Failed to get metadata of spawned workflow [InstanceId='" + spawnedInstanceId + "']");
					}
				}
				this.clearReserveKeys(dynMet);
				ctrlMetadata.replaceLocalMetadata(dynMet);
				List<String> keys = dynMet.getAllKeys();
				ctrlMetadata.setAsWorkflowMetadataKey(keys.toArray(new String[keys.size()]));
				return new ResultsSuccessState("All spawned workflow completed successfully");
			}else {
				return new ResultsBailState("Waiting on " + (spawnedInstanceIds.size() - nDone) + " of " + spawnedInstanceIds.size() + " spawned workflows to finish");
			}
		}
	}
	
	private void clearReserveKeys(Metadata metadata) {
		metadata.removeMetadata(N_CALCULATOR_CLASS);
		metadata.removeMetadata(N_MET_MOD_CLASS);
		metadata.removeMetadata(SPAWN_MODEL_ID);
		metadata.removeMetadata(SPAWNED_WORKFLOWS);
		metadata.removeMetadata(SPAWNED_BY_WORKFLOW);
		metadata.removeMetadata(JOIN_METADATA);
		metadata.removeMetadata(JOIN_ONLY_METADATA_KEYS);
	}
	
	public interface NCalculator {
		
		public int determineN(ControlMetadata ctrlMetadata) throws Exception;
		
	}
	
	public interface NMetadataModification {
		
		/**
		 * @param i Current index number of workflow being submitted -- 1 indexed (i.e. 1-n)
		 * @param n Total number of workflows submitted
		 * @param metadata Metadata for current workflow being submitted
		 */
		public void prepare(int i, int n, Metadata metadata) throws Exception;
		
	}
	
}
