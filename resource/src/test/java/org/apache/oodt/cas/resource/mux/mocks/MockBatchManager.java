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
package org.apache.oodt.cas.resource.mux.mocks;

import java.util.List;
import org.apache.oodt.cas.resource.batchmgr.Batchmgr;
import org.apache.oodt.cas.resource.jobrepo.JobRepository;
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
/**
 * This is a mock version of the batch manager. It SHOULD NOT, and 
 * CAN NOT be used as a normal class.
 *
 * @author starchmd
 */
public class MockBatchManager implements Batchmgr {

    private JobSpec execJobSpec;
    private ResourceNode execResNode;

    @Override
    public boolean executeRemotely(JobSpec job, ResourceNode resNode)
            throws JobExecutionException {
        this.execJobSpec = job;
        this.execResNode = resNode;
        return true;
    }

    @Override
    public void setMonitor(Monitor monitor) {}

    @Override
    public void setJobRepository(JobRepository repository) {}

    @Override
    public List<Job> getJobsOnNode(String nodeId){
	throw new UnsupportedOperationException("method not implemented. getJobsOnNode");
    }

    @Override
    public boolean killJob(String jobId, ResourceNode node) {
        if (this.execJobSpec.getJob().getId().equals(jobId))
        {
            this.execJobSpec = null;
            this.execResNode = null;
            return true;
        }
        return false;
    }

    @Override
    public String getExecutionNode(String jobId) {
        return execResNode.getNodeId();
    }

    /*****
     * The following are test methods to report what jobs are here.
     *****/
    /**
     * Return the current jobspec, for testing purposes
     * @return
     */
    public JobSpec getCurrentJobSpec() {
        return this.execJobSpec;
    }
    /**
     * Return the current resource node, for testing purposes
     * @return
     */
    public ResourceNode getCurrentResourceNode() {
        return execResNode;
    }
}
