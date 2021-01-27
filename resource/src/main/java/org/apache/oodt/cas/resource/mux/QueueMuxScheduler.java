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

//JDKimports
import org.apache.oodt.cas.resource.batchmgr.Batchmgr;
import org.apache.oodt.cas.resource.jobqueue.JobQueue;
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.scheduler.QueueManager;
import org.apache.oodt.cas.resource.scheduler.Scheduler;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.JobQueueException;
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;
import org.apache.oodt.cas.resource.structs.exceptions.SchedulerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//OODT imports

/**
 * This scheduler multiplexes between multiple schedulers based on the "queue" .
 *
 * @author starchmd
 * @version $Revision$
 */
public class QueueMuxScheduler implements Scheduler {

    private static final Logger LOG = LoggerFactory.getLogger(QueueMuxScheduler.class);

    private BackendManager backend;
    private JobQueue queue;
    private float waitTime = -1;

    //Manages other queue-muxing components
    private QueueMuxBatchManager batch;
    private QueueMuxMonitor mon;
    private QueueManager qManager;

    /**
     * ctor
     * @param backend - Backend manager to handle the many different backends.
     */
    public QueueMuxScheduler(BackendManager backend, QueueManager qm, JobQueue jq) {
        String waitStr = System.getProperty("org.apache.oodt.cas.resource.scheduler.wait.seconds", "20");
        waitTime = Float.parseFloat(waitStr);
        this.queue = jq;
        this.qManager = qm;
        this.backend = backend;
        //Required, so make them here
        batch = new QueueMuxBatchManager(backend);
        mon = new QueueMuxMonitor(backend,qm);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        //Loop forever
        while (true) {
            try {
                Thread.sleep((long) (waitTime * 1000.0));
            } catch (InterruptedException e) {
                //If the thread will continue, reinterrupt thread
                Thread.currentThread().interrupt();
            }
            //You have jobs
            if (!queue.isEmpty()) {
                JobSpec job = null;
                try {
                    job = queue.getNextJob();
                    LOG.info("Scheduling job [{}] for execution", job.getJob().getId());
                    schedule(job);
                } catch (SchedulerException se) {
                    LOG.warn("Error occurred while scheduling job: {}. Attempt re-queueing", se.getMessage(), se);
                    try {
                        queue.requeueJob(job);
                    } catch (JobQueueException je) {
                        LOG.warn("Error while re-queueing job: {}", je.getMessage(), je);
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
        System.out.println("Spec: "+spec+" Job: "+spec.getJob()+" Backend:"+backend);
        String queue = spec.getJob().getQueueName();
        try {
            return backend.getScheduler(queue).schedule(spec);
        } catch (QueueManagerException e) {
            String msg = String.format("QueueManagerException occurred: %s", e.getMessage());
            LOG.warn(msg, e);
            throw new SchedulerException(msg, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#getBatchmgr()
     */
    public Batchmgr getBatchmgr() {
        return batch;
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#getMonitor()
     */
    public Monitor getMonitor() {
        return mon;
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#getJobQueue()
     */
    public JobQueue getJobQueue() {
        return this.queue;
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#getQueueManager()
     */
    public QueueManager getQueueManager() {
        return qManager;
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.nasa.jpl.oodt.cas.resource.scheduler.Scheduler#nodeAvailable(gov.nasa.jpl.oodt.cas.resource.structs.JobSpec)
     */
    public synchronized ResourceNode nodeAvailable(JobSpec spec)
            throws SchedulerException {
        String queue = spec.getJob().getQueueName();
        try {
            return backend.getScheduler(queue).nodeAvailable(spec);
        } catch (QueueManagerException e) {
            String msg = String.format("QueueManagerException occurred: %s", e.getMessage());
            LOG.warn(msg, e);
            throw new SchedulerException(msg, e);
        }
    }
}
