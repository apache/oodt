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


import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.struct.avrotypes.AvroWorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;
import org.apache.oodt.cas.workflow.system.rpc.RpcCommunicationFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author mattmann
 * @version $Revision$
 * 
 * This class illustrates OODT-86, demonstrating how the method
 * {@link org.apache.oodt.cas.workflow.system.AvroRpcWorkflowManager#updateWorkflowInstance(AvroWorkflowInstance)}
 * allows a user to change the status of a given {@link org.apache.oodt.cas.workflow.structs.WorkflowInstance}
 * programmatically.
 */
public class RandomStatusUpdateTask implements WorkflowTaskInstance {
    public static final long MILLIS = 5000L;
    private static Logger LOG = Logger.getLogger(RandomStatusUpdateTask.class.getName());
    private static final String[] statuses = new String[] { "THINKING",
            "RUNNING", "WAITING", "INFINITELY WAITING", "WATCHING TV",
            "SLEEPING", "DREAMING", "WORKING", "WATCHING MOVIES" };

    private final int numStatusesToDisplay = 10;

    private WorkflowManagerClient client = null;

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
                Thread.currentThread().sleep(MILLIS);
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
            LOG.log(Level.SEVERE, e.getMessage());
        }

    }

    private void setWorkflowMgrUrl(String wmUrlStr) {
        System.out.println("Connecting to workflow mgr: ["+wmUrlStr+"]");
        this.client = RpcCommunicationFactory.createClient(safeGetUrl(wmUrlStr));
    }

    private URL safeGetUrl(String urlStr) {
        try {
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return null;
        }
    }

    @Override
    public void finalize() throws Exception {
        if (client != null) {
            client.close();
        }
    }
}
