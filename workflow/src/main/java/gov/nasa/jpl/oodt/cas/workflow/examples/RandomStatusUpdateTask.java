//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.examples;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowInstance;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskInstance;
import gov.nasa.jpl.oodt.cas.workflow.system.XmlRpcWorkflowManager;
import gov.nasa.jpl.oodt.cas.workflow.system.XmlRpcWorkflowManagerClient;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * This class illustrates OODT-86, demonstrating how the method
 * {@link XmlRpcWorkflowManager#updateWorkflowInstance(java.util.Hashtable)}
 * allows a user to change the status of a given {@link WorkflowInstance}
 * programmatically.
 */
public class RandomStatusUpdateTask implements WorkflowTaskInstance {

    private static final String[] statuses = new String[] { "THINKING",
            "RUNNING", "WAITING", "INFINITELY WAITING", "WATCHING TV",
            "SLEEPING", "DREAMING", "WORKING", "WATCHING MOVIES" };

    private final int numStatusesToDisplay = 10;

    private XmlRpcWorkflowManagerClient client = null;

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskInstance#run(gov.nasa.jpl.oodt.cas.metadata.Metadata,
     *      gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskConfiguration)
     */
    public void run(Metadata metadata, WorkflowTaskConfiguration config) {
        // the goal in this task to is randomly display statuses, and then sleep
        // for 5 seconds
        // after each status is picked
        setWorkflowMgrUrl(metadata.getMetadata("WorkflowManagerUrl"));
        String workflowInstId = metadata.getMetadata("WorkflowInstId");

        int numPicked = 0;
        Random r = new Random();

        while (numPicked < 10) {
            int idx = r.nextInt(statuses.length);
            String statusPicked = statuses[idx];
            updateWorkflowInstanceStatus(workflowInstId, statusPicked);
            try {
                Thread.currentThread().sleep(5000L);
            } catch (InterruptedException ignore) {
            }
            numPicked++;
        }

    }

    private void updateWorkflowInstanceStatus(String wInstId, String status) {
        System.out.println("Sending status update for "+wInstId+","+status);
        try {
            this.client.updateWorkflowInstanceStatus(wInstId, status);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setWorkflowMgrUrl(String wmUrlStr) {
        System.out.println("Connecting to workflow mgr: ["+wmUrlStr+"]");
        this.client = new XmlRpcWorkflowManagerClient(safeGetUrl(wmUrlStr));
    }

    private URL safeGetUrl(String urlStr) {
        try {
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
