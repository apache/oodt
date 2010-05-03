//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.batchmgr;

//OODT imports
import gov.nasa.jpl.oodt.cas.resource.jobrepo.JobRepository;
import gov.nasa.jpl.oodt.cas.resource.monitor.Monitor;
import gov.nasa.jpl.oodt.cas.resource.structs.JobSpec;
import gov.nasa.jpl.oodt.cas.resource.structs.Job;
import gov.nasa.jpl.oodt.cas.resource.structs.JobStatus;
import gov.nasa.jpl.oodt.cas.resource.structs.ResourceNode;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.JobExecutionException;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.JobRepositoryException;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.MonitorException;

//JDK imports
import java.util.HashMap;
import java.util.Map;
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
        nodeToJobMap = new HashMap();
        specToProxyMap = new HashMap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.batchmgr.Batchmgr#executeRemotely(gov.nasa.jpl.oodt.cas.resource.structs.JobSpec,
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
     * @see gov.nasa.jpl.oodt.cas.resource.batchmgr.Batchmgr#setMonitor(gov.nasa.jpl.oodt.cas.resource.monitor.Monitor)
     */
    public void setMonitor(Monitor monitor) {
        this.mon = monitor;

    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.batchmgr.Batchmgr#setJobRepository(gov.nasa.jpl.oodt.cas.resource.jobrepo.JobRepository)
     */
    public void setJobRepository(JobRepository repository) {
        this.repo = repository;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.batchmgr.Batchmgr#getExecutionNode(java.lang.String)
     */
    public String getExecutionNode(String jobId) {
        return (String) nodeToJobMap.get(jobId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.batchmgr.Batchmgr#killJob(java.lang.String,
     *      gov.nasa.jpl.oodt.cas.resource.structs.ResourceNode)
     */
    public boolean killJob(String jobId, ResourceNode node) {
        JobSpec spec = null;
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

    protected void notifyMonitor(ResourceNode node, JobSpec jobSpec) {
        Job job = jobSpec.getJob();
        int reducedLoad = job.getLoadValue().intValue();
        try {
            mon.reduceLoad(node, reducedLoad);
        } catch (MonitorException e) {
        }
    }

    protected void jobComplete(JobSpec spec) {
        spec.getJob().setStatus(JobStatus.COMPLETE);
        synchronized (this.nodeToJobMap) {
            this.nodeToJobMap.remove(spec.getJob().getId());
        }
        synchronized (this.specToProxyMap) {
            XmlRpcBatchMgrProxy proxy = (XmlRpcBatchMgrProxy) this.specToProxyMap
                    .remove(spec.getJob().getId());
            if (proxy != null) {
                proxy = null;
            }
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
