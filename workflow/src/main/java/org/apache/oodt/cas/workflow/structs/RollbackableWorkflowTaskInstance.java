package org.apache.oodt.cas.workflow.structs;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.metadata.CoreMetKeys;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;
import org.apache.oodt.cas.workflow.system.XmlRpcWorkflowManagerClient;

public abstract class RollbackableWorkflowTaskInstance implements
    WorkflowTaskInstance {
  
  protected XmlRpcWorkflowManagerClient wmc; 
  protected String workflowInstId;
  
  public void run(Metadata metadata, WorkflowTaskConfiguration config)
      throws WorkflowTaskInstanceException {
    
    workflowInstId = getWorkflowInstanceId(metadata);
    wmc = createWorkflowMangerClient(metadata);
    
    
    //clean generated metadata 
    clearAllMetadata(metadata);
    
    //Determine next workflow state
    updateState(metadata);
    
    //clean up task instance from instance rep?
    clearInstRep();

  }
  
  protected String getWorkflowInstanceId(Metadata metadata) {
    
	String instId = metadata.getMetadata(CoreMetKeys.WORKFLOW_INST_ID);  
    return instId;
    
  }
  
  protected XmlRpcWorkflowManagerClient createWorkflowMangerClient(Metadata metadata) {
    URL url;
    try {
      url = new URL(metadata.getMetadata(CoreMetKeys.WORKFLOW_MANAGER_URL));
      wmc = new XmlRpcWorkflowManagerClient(url);
     
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return wmc;
  }

  protected void clearAllMetadata(Metadata metadata) {
  
    List<String> keys =  metadata.getAllKeys();
    
    //remove metadata for each key
    for(String key: keys) {
      metadata.removeMetadata(key);
    }  
    try {
      wmc.updateMetadataForWorkflow(workflowInstId, metadata);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public abstract void updateState(Metadata metadata);
  
  public abstract void clearInstRep();
}
