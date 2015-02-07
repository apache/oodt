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


package org.apache.oodt.cas.workflow.examples;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.system.XmlRpcWorkflowManager;
import org.apache.oodt.cas.workflow.system.XmlRpcWorkflowManagerClient;

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
     * @see org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance#run(org.apache.oodt.cas.metadata.Metadata,
     *      org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration)
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
