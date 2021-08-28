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


package org.apache.oodt.cas.resource.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//OODT imports
import org.apache.oodt.cas.resource.jobqueue.JobQueue;
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.batchmgr.Batchmgr;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;
import org.apache.oodt.cas.resource.structs.exceptions.SchedulerException;

/**
 * 
 * @author woollard
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * An implementation of a {@link Scheduler} that uses a <a
 * href="http://en.wikipedia.org/wiki/Cache_algorithms">least-recently-used</a>
 * algorithm for scheduling {@link Job}s.
 * </p>
 */
public class LRUScheduler implements Scheduler {

    /* our log stream */
    private static final Logger LOG = LoggerFactory.getLogger(LRUScheduler.class);
    public static final double DOUBLE = 1000.0;

    private LRUQueueManager queueManager;
    
    /* the monitor we'll use to check the status of the resources */
    private Monitor myMonitor;

    /* the batch mgr we'll use to execute jobs */
    private Batchmgr myBatchmgr;

    /* our job queue */
    private JobQueue myJobQueue;

    /* our wait time between checking the queue */
    private double waitTime = -1;

    public LRUScheduler(Monitor m, Batchmgr b, JobQueue q, LRUQueueManager qm) {

    	queueManager = qm;
        myMonitor = m;
        myBatchmgr = b;
        myJobQueue = q;

        String waitStr = System.getProperty(
                "org.apache.oodt.cas.resource.scheduler.wait.seconds", "20");
        waitTime = Double.parseDouble(waitStr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        for (;;) {

            try {
            	long sleepTime = (long)(waitTime * DOUBLE);
                Thread.currentThread().sleep(sleepTime);
            } catch (Exception ignore) {}

            if (!myJobQueue.isEmpty()) {
                JobSpec exec;

                try {
                    exec = myJobQueue.getNextJob();
                    LOG.info("Obtained Job: [{}] from Queue: Scheduling for execution", exec.getJob().getId());
                } catch (Exception e) {
                    LOG.warn("Error getting next job from JobQueue: {}", e.getMessage(), e);
                    continue;
                }

                try {
                    schedule(exec);
                } catch (Exception e) {
                    LOG.warn("Error scheduling job [{}]: {}", exec.getJob().getId(), e.getMessage(), e);
                    // place the job spec back on the queue
                    try {
                        myJobQueue.requeueJob(exec);
                    } catch (Exception ignore) {
                    }
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#schedule(gov.nasa.jpl.oodt.cas.resource.structs.JobSpec)
     */
    public synchronized boolean schedule(JobSpec spec)
            throws SchedulerException {
        String queueName = spec.getJob().getQueueName();
        int load = spec.getJob().getLoadValue();

        ResourceNode node = nodeAvailable(spec);

        if (node != null) {
            try {
                myMonitor.assignLoad(node, load);
                queueManager.usedNode(queueName, node.getNodeId());
                
                // assign via batch system
                LOG.info("Assigning job [{}] to node [{}]", spec.getJob().getName(), node.getNodeId());
                try {
                    myBatchmgr.executeRemotely(spec, node);
                } catch (JobExecutionException e) {
                    LOG.warn("Exception executing job [{}] to node [{}]: {}", spec.getJob().getId(), node.getIpAddr(), e.getMessage(), e);
                    try {
                        // queue the job back up
                        LOG.info("Re-queueing job [{}]", spec.getJob().getId());
                        myJobQueue.requeueJob(spec);

                        // make sure to decrement the load
                        myMonitor.reduceLoad(node, load);
                    } catch (Exception ignore) {
                    }
                }
            } catch (MonitorException e) {
                String msg = String.format("Exception assigning load to resource node [%s]: load [%d]: %s", node.getNodeId(), load, e.getMessage());
                LOG.warn(msg, e);
                throw new SchedulerException(msg, e);
            }
        } else {
            // could not find resource, push onto JobQueue
            try {
                myJobQueue.requeueJob(spec);
            } catch (Exception ignore) {
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#getBatchmgr()
     */
    public Batchmgr getBatchmgr() {
        return myBatchmgr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#getMonitor()
     */
    public Monitor getMonitor() {
        return myMonitor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#getJobQueue()
     */
    public JobQueue getJobQueue() {
        return myJobQueue;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#getQueueManager()
     */
    public QueueManager getQueueManager() {
    	return this.queueManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#nodeAvailable(gov.nasa.jpl.oodt.cas.resource.structs.JobSpec)
     */
    public synchronized ResourceNode nodeAvailable(JobSpec spec)
            throws SchedulerException {
        try {
	    	String queueName = spec.getJob().getQueueName();
	        int load = spec.getJob().getLoadValue();
	
	        for (String nodeId : queueManager.getNodes(queueName)) {
	            int nodeLoad;
	            ResourceNode resNode = null;
	
	            try {
	                resNode = myMonitor.getNodeById(nodeId);
	                nodeLoad = myMonitor.getLoad(resNode);
	            } catch (MonitorException e) {
	                String msg = String.format("Exception getting load on node [%s]: %s", resNode != null ? resNode.getNodeId() : null, e.getMessage());
	                LOG.warn(msg, e);
	                throw new SchedulerException(msg, e);
	            }
	
	            if (load <= nodeLoad) {
	                return resNode;
	            }
	        }
	
	        return null;
        }catch (Exception e) {
        	throw new SchedulerException("Failed to find available node for job spec : " + e.getMessage(), e);
        }
    }

}
