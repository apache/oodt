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

package org.apache.oodt.cas.resource.mux;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.resource.batchmgr.Batchmgr;
import org.apache.oodt.cas.resource.jobrepo.JobRepository;
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;

/**
 * @author starchmd
 * @version $Revision$
 *
 * A batch-manager used to execute and control jobs in a mesos-cluster.
 */
public class QueueMuxBatchManager implements Batchmgr {

    private Logger LOG = Logger.getLogger(QueueMuxBatchManager.class.getName());

    BackendManager backend;
    Map<String,String> jobIdToQueue = new ConcurrentHashMap<String,String>();
    JobRepository repo;

    /**
     * ctor
     * @param bm - backend manager
     */
    public QueueMuxBatchManager(BackendManager bm) {
        setBackendManager(bm);
    }
    /**
     * Set the backend manager.
     * @param backend - backend manager effectively mapping queue's to sets of backends.
     */
    public void setBackendManager(BackendManager backend) {
        this.backend = backend;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.batchmgr.Batchmgr#executeRemotely(org.apache.oodt.cas.resource.structs.JobSpec, org.apache.oodt.cas.resource.structs.ResourceNode)
     */
    @Override
    public boolean executeRemotely(JobSpec job, ResourceNode resNode)
            throws JobExecutionException {
        try {
            jobIdToQueue.put(job.getJob().getId(),job.getJob().getQueueName());
            return getManagerByQueue(job.getJob().getQueueName()).executeRemotely(job, resNode);
        } catch (QueueManagerException e) {
            jobIdToQueue.remove(job.getJob().getQueueName());
            LOG.log(Level.WARNING, "Exception recieved while executing job: "+e.getLocalizedMessage()+". Job will not execute.");
            throw new JobExecutionException(e);
        }
    }


    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.batchmgr.Batchmgr#setMonitor(org.apache.oodt.cas.resource.monitor.Monitor)
     */
    @Override
    public void setMonitor(Monitor monitor) {
        throw new UnsupportedOperationException("Cannot set the monitor when using the queue-mux batch manager.");
    }

    @Override
    public List<Job> getJobsOnNode(String nodeId) {
        throw new UnsupportedOperationException("Method not supported: get Jobs on Node.");
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.batchmgr.Batchmgr#setJobRepository(org.apache.oodt.cas.resource.jobrepo.JobRepository)
     */
    @Override
    public void setJobRepository(JobRepository repository) {
        this.repo = repository;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.batchmgr.Batchmgr#killJob(java.lang.String, org.apache.oodt.cas.resource.structs.ResourceNode)
     */
    @Override
    public boolean killJob(String jobId, ResourceNode node) {
        try {
            return getManagerByJob(jobId).killJob(jobId,node);
        } catch (QueueManagerException e) {
            LOG.log(Level.SEVERE, "Cannot kill job: "+e.getLocalizedMessage());
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.batchmgr.Batchmgr#getExecutionNode(java.lang.String)
     */
    @Override
    public String getExecutionNode(String jobId) {
        try {
            return getManagerByJob(jobId).getExecutionNode(jobId);
        } catch (QueueManagerException e) {
            LOG.log(Level.SEVERE, "Cannot get exectuion node for job: "+e.getLocalizedMessage());
        }
        return null;
    }

    private Batchmgr getManagerByJob(String jobId) throws QueueManagerException {
        return getManagerByQueue(jobIdToQueue.get(jobId));
    }

    private Batchmgr getManagerByQueue(String queue) throws QueueManagerException {
        return this.backend.getBatchmgr(queue);
    }

}
