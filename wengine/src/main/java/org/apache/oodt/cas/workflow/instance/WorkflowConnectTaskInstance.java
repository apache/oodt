package org.apache.oodt.cas.workflow.instance;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;
import org.apache.oodt.cas.workflow.metadata.ControlMetadata;
import org.apache.oodt.cas.workflow.state.WorkflowState;
import org.apache.oodt.cas.workflow.state.done.FailureState;
import org.apache.oodt.cas.workflow.state.done.SuccessState;
import org.apache.oodt.cas.workflow.state.results.ResultsBailState;
import org.apache.oodt.cas.workflow.state.results.ResultsFailureState;
import org.apache.oodt.cas.workflow.state.results.ResultsState;
import org.apache.oodt.cas.workflow.state.results.ResultsSuccessState;

public class WorkflowConnectTaskInstance extends TaskInstance {
	
	private static final Logger LOG = Logger.getLogger(WorkflowConnectTaskInstance.class.getName());
	
	public static final String N_CALCULATOR_CLASS = "WorkflowConnect/NCalculator/Class"; 
	public static final String N_MET_MOD_CLASS = "WorkflowConnect/NMetadataModifier/Class"; 
	public static final String SPAWN_MODEL_ID = "WorkflowConnect/ModelId"; 
	public static final String SPAWNED_WORKFLOWS = "WorkflowConnect/SpawnedWorkflows/InstanceIds";
	public static final String SPAWNED_BY_WORKFLOW = "WorkflowConnect/SpawnedByWorkflow/InstanceId";

	private WorkflowEngineClient weClient;
	
	@Override
	public void setNotifyEngine(WorkflowEngineClient weClient) {
		super.setNotifyEngine(weClient);
		this.weClient = weClient;
	}
	
	@Override
	protected ResultsState performExecution(ControlMetadata ctrlMetadata) {
		if (ctrlMetadata.getMetadata(SPAWNED_WORKFLOWS) == null) {
			
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
			
			int n = calculator.determineN(ctrlMetadata);
			Metadata spawnWorkflowMet = ctrlMetadata.asMetadata();
			Vector<String> spawnedInstanceId = new Vector<String>();
			for (int i = 0; i < n; i++) {
				Metadata curWorkflowMet = new Metadata(spawnWorkflowMet);
				if (nMetMod != null)
					nMetMod.prepare(n, curWorkflowMet);
				try {
					curWorkflowMet.replaceMetadata(SPAWNED_BY_WORKFLOW, this.getInstanceId());
					spawnedInstanceId.add(this.weClient.startWorkflow(spawnModelId, curWorkflowMet));
				}catch (Exception e) {
					LOG.log(Level.SEVERE, "Failed to start workflow ModelId '" + spawnModelId + "' : " + e.getMessage(), e);
					return new ResultsFailureState("Failed to start workflow ModelId '" + spawnModelId + "' : " + e.getMessage());
				}
			}
			ctrlMetadata.replaceLocalMetadata(SPAWNED_WORKFLOWS, spawnedInstanceId);
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
			if (nDone == spawnedInstanceIds.size())
				return new ResultsSuccessState("All spawned workflow completed successfully");
			else
				return new ResultsBailState("Waiting on " + (spawnedInstanceIds.size() - nDone) + " of " + spawnedInstanceIds.size() + " spawned workflows to finish");
		}
	}
	
	public interface NCalculator {
		
		public int determineN(ControlMetadata ctrlMetadata);
		
	}
	
	public interface NMetadataModification {
		
		public void prepare(int n, Metadata metadata);
		
	}
	
}
