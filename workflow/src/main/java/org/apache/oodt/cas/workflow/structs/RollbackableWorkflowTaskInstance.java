package org.apache.oodt.cas.workflow.structs;

//JDK imports
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.metadata.CoreMetKeys;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;
import org.apache.oodt.cas.workflow.system.rpc.RpcCommunicationFactory;

public abstract class RollbackableWorkflowTaskInstance implements WorkflowTaskInstance {

  private static final Logger LOG = LoggerFactory.getLogger(RollbackableWorkflowTaskInstance.class);

  protected String workflowInstId;

  public void run(Metadata metadata, WorkflowTaskConfiguration config) throws WorkflowTaskInstanceException {
    workflowInstId = getWorkflowInstanceId(metadata);
    try (WorkflowManagerClient wmc = createWorkflowMangerClient(metadata)){
      //clean generated metadata
      clearAllMetadata(wmc, metadata);

      //Determine next workflow state
      updateState(metadata);

      //clean up task instance from instance rep?
      clearInstRep();
    } catch (IOException e) {
      LOG.error("WorkflowManagerClient IOException: {}", e.getMessage(), e);
    }
  }

  private String getWorkflowInstanceId(Metadata metadata) {
    return metadata.getMetadata(CoreMetKeys.WORKFLOW_INST_ID);

  }

  private WorkflowManagerClient createWorkflowMangerClient(Metadata metadata) {
    URL url;
    try {
      url = new URL(metadata.getMetadata(CoreMetKeys.WORKFLOW_MANAGER_URL));
      return RpcCommunicationFactory.createClient(url);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  private void clearAllMetadata(WorkflowManagerClient client, Metadata metadata) {
    List<String> keys =  metadata.getAllKeys();

    //remove metadata for each key
    for(String key: keys) {
      metadata.removeMetadata(key);
    }
    try {
      client.updateMetadataForWorkflow(workflowInstId, metadata);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public abstract void updateState(Metadata metadata);

  public abstract void clearInstRep();
}
