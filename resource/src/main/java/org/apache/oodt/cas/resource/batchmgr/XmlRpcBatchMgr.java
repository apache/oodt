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


package org.apache.oodt.cas.resource.batchmgr;

//OODT imports
import org.apache.oodt.cas.resource.jobrepo.JobRepository;
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobStatus;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.resource.structs.exceptions.JobRepositoryException;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;

//JDK imports
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * An XML-RPC interface to the batch manager.
 * </p>
 */
public class XmlRpcBatchMgr implements Batchmgr {
    /* our log stream */
    private static final Logger LOG = Logger.getLogger(XmlRpcBatchMgr.class
            .getName());

    private Monitor mon;

    private JobRepository repo;

    private Map nodeToJobMap;

    private Map specToProxyMap;

    public XmlRpcBatchMgr() {
        nodeToJobMap = new ConcurrentHashMap();
        specToProxyMap = new ConcurrentHashMap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.resource.batchmgr.Batchmgr#executeRemotely(org.apache.oodt.cas.resource.structs.JobSpec,
     *      java.net.URL)
     */
    public boolean executeRemotely(JobSpec jobSpec, ResourceNode resNode)
            throws JobExecutionException {

        XmlRpcBatchMgrProxy proxy = new XmlRpcBatchMgrProxy(jobSpec, resNode,
                this);
        if (!proxy.nodeAlive()) {
            throw new JobExecutionException("Node: [" + resNode.getNodeId()
                    + "] is down: Unable to execute job!");
        }

        synchronized (this.specToProxyMap) {
            specToProxyMap.put(jobSpec.getJob().getId(), proxy);
        }

        synchronized (this.nodeToJobMap) {
            this.nodeToJobMap
                    .put(jobSpec.getJob().getId(), resNode.getNodeId());
        }

        proxy.start();

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.resource.batchmgr.Batchmgr#setMonitor(org.apache.oodt.cas.resource.monitor.Monitor)
     */
    public void setMonitor(Monitor monitor) {
        this.mon = monitor;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.resource.batchmgr.Batchmgr#setJobRepository(org.apache.oodt.cas.resource.jobrepo.JobRepository)
     */
    public void setJobRepository(JobRepository repository) {
        this.repo = repository;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.resource.batchmgr.Batchmgr#getExecutionNode(java.lang.String)
     */
    public String getExecutionNode(String jobId) {
        return (String) nodeToJobMap.get(jobId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.resource.batchmgr.Batchmgr#killJob(java.lang.String,
     *      org.apache.oodt.cas.resource.structs.ResourceNode)
     */
    public boolean killJob(String jobId, ResourceNode node) {
        JobSpec spec;
        try {
            spec = repo.getJobById(jobId);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Unable to get job by id: [" + jobId
                    + "] to kill it: Message: " + e.getMessage());
            return false;
        }

        XmlRpcBatchMgrProxy proxy = new XmlRpcBatchMgrProxy(spec, node, this);
        return proxy.killJob();
    }
    
    public List getJobsOnNode(String nodeId){
    	Vector<String> jobIds = new Vector();
    	
    	if(this.nodeToJobMap.size() > 0){
            for (Object o : this.nodeToJobMap.keySet()) {
                String jobId = (String) o;
                if (nodeId.equals(this.nodeToJobMap.get(jobId))) {
                    jobIds.add(jobId);
                }
            }
    	}
    	
    	Collections.sort(jobIds); // sort the list to return as a courtesy to the user
    	
    	return jobIds;
    }

    protected void notifyMonitor(ResourceNode node, JobSpec jobSpec) {
        Job job = jobSpec.getJob();
        int reducedLoad = job.getLoadValue();
        try {
            mon.reduceLoad(node, reducedLoad);
        } catch (MonitorException ignored) {
        }
    }

    protected void jobSuccess(JobSpec spec) {
        spec.getJob().setStatus(JobStatus.SUCCESS);
        synchronized (this.nodeToJobMap) {
            this.nodeToJobMap.remove(spec.getJob().getId());
        }
        synchronized (this.specToProxyMap) {
            this.specToProxyMap
                    .remove(spec.getJob().getId());
        }

        try {
            repo.updateJob(spec);
        } catch (JobRepositoryException e) {
            LOG.log(Level.WARNING, "Error set job completion status for job: ["
                    + spec.getJob().getId() + "]: Message: " + e.getMessage());
        }
    }
    
    protected void jobFailure(JobSpec spec) {
        spec.getJob().setStatus(JobStatus.FAILURE);
        synchronized (this.nodeToJobMap) {
            this.nodeToJobMap.remove(spec.getJob().getId());
        }
        synchronized (this.specToProxyMap) {
            this.specToProxyMap
                    .remove(spec.getJob().getId());

        }

        try {
            repo.updateJob(spec);
        } catch (JobRepositoryException e) {
            LOG.log(Level.WARNING, "Error set job completion status for job: ["
                    + spec.getJob().getId() + "]: Message: " + e.getMessage());
        }
    }

    protected void jobKilled(JobSpec spec) {
        spec.getJob().setStatus(JobStatus.KILLED);
        nodeToJobMap.remove(spec.getJob().getId());
        try {
            repo.updateJob(spec);
        } catch (JobRepositoryException e) {
            LOG.log(Level.WARNING, "Error setting job killed status for job: ["
                    + spec.getJob().getId() + "]: Message: " + e.getMessage());
        }
    }

    protected void jobExecuting(JobSpec spec) {
        spec.getJob().setStatus(JobStatus.EXECUTED);
        try {
            repo.updateJob(spec);
        } catch (JobRepositoryException e) {
            LOG.log(Level.WARNING,
                    "Error setting job execution status for job: ["
                            + spec.getJob().getId() + "]: Message: "
                            + e.getMessage());
        }
    }

}
