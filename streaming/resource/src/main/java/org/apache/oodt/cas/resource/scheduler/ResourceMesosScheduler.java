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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.NotImplementedException;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.ExecutorInfo;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.mesos.Protos.MasterInfo;
import org.apache.mesos.Protos.Offer;
import org.apache.mesos.Protos.OfferID;
import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.SlaveID;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskStatus;
import org.apache.mesos.Protos.Value;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;
import org.apache.oodt.cas.resource.batchmgr.Batchmgr;
import org.apache.oodt.cas.resource.batchmgr.MesosBatchManager;
import org.apache.oodt.cas.resource.jobqueue.JobQueue;
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.JobQueueException;
import org.apache.oodt.cas.resource.structs.exceptions.MesosFrameworkException;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;
import org.apache.oodt.cas.resource.structs.exceptions.SchedulerException;
import org.apache.oodt.cas.resource.util.MesosUtilities;

/**
 * @author starchmd
 * @version $Revision$
 *
 * A scheduler for part of the mesos frame work.
 */
public class ResourceMesosScheduler implements Scheduler, org.apache.oodt.cas.resource.scheduler.Scheduler {
    SchedulerDriver driver;
    MesosBatchManager batch;
    ExecutorInfo executor;
    JobQueue queue;
    Monitor mon;

    //Logger
    private static final Logger LOG = Logger.getLogger(ResourceMesosScheduler.class.getName());
    /**
     * Construct the scheduler
     * @param batch - batch manager (must be MesosBatchManager)
     * @param executor - Mesos ExecutorInfo
     * @param queue Job Queue used
     * @param mon - monitor used.
     */
    public ResourceMesosScheduler(MesosBatchManager batch,ExecutorInfo executor, JobQueue queue, Monitor mon) {
        this.batch = batch;
        this.executor = executor;
        this.queue = queue;
        this.mon = mon;
        LOG.log(Level.INFO,"Creating the resource-mesos scheduler.");
    }


    /* (non-Javadoc)
     * @see org.apache.mesos.Scheduler#disconnected(org.apache.mesos.SchedulerDriver)
     */
    @Override
    public void disconnected(SchedulerDriver schedDriver) {
        //TODO: Pause scheduler until master comes back online.

    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Scheduler#error(org.apache.mesos.SchedulerDriver, java.lang.String)
     */
    @Override
    public void error(SchedulerDriver schedDriver, String error) {
        LOG.log(Level.SEVERE,"Mesos issued an error: "+error);
        //TODO: kill something here.
    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Scheduler#executorLost(org.apache.mesos.SchedulerDriver, org.apache.mesos.Protos.ExecutorID, org.apache.mesos.Protos.SlaveID, int)
     */
    @Override
    public void executorLost(SchedulerDriver schedDriver, ExecutorID executor,SlaveID slave, int status) {
        //Tasks will have a "task lost" message automatically q.e.d no action necessary.
        //TODO: do we need to restart?
        LOG.log(Level.SEVERE,"Mesos executor "+executor+" on slave "+slave+" died with status "+status);
    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Scheduler#frameworkMessage(org.apache.mesos.SchedulerDriver, org.apache.mesos.Protos.ExecutorID, org.apache.mesos.Protos.SlaveID, byte[])
     */
    @Override
    public void frameworkMessage(SchedulerDriver schedDriver, ExecutorID executor,
            SlaveID slave, byte[] bytes) {
        try {
            LOG.log(Level.INFO,"Mesos framework executor"+executor+" on slave "+slave+" issued message: "+
                new String(bytes,"ascii"));
        } catch (UnsupportedEncodingException e) {
            LOG.log(Level.WARNING,"Mesos framework message missed due to bad encoding: ascii. "+e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Scheduler#offerRescinded(org.apache.mesos.SchedulerDriver, org.apache.mesos.Protos.OfferID)
     */
    @Override
    public void offerRescinded(SchedulerDriver schedDriver, OfferID offer) {
        //TODO: take away resources from batch manager...or stand in.
        //Unneeded?
    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Scheduler#registered(org.apache.mesos.SchedulerDriver, org.apache.mesos.Protos.FrameworkID, org.apache.mesos.Protos.MasterInfo)
     */
    @Override
    public void registered(SchedulerDriver schedDriver, FrameworkID framework,
            MasterInfo masterInfo) {
        LOG.log(Level.INFO,"Mesos framework registered: "+framework.getValue()+" with master: "+masterInfo.getId());
    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Scheduler#reregistered(org.apache.mesos.SchedulerDriver, org.apache.mesos.Protos.MasterInfo)
     */
    @Override
    public void reregistered(SchedulerDriver schedDriver, MasterInfo masterInfo) {
        LOG.log(Level.INFO,"Mesos framework re-registered with: "+masterInfo.getId());
        //TODO: call start, we are registered.

    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Scheduler#resourceOffers(org.apache.mesos.SchedulerDriver, java.util.List)
     */
    @Override
    public void resourceOffers(SchedulerDriver driver, List<Offer> offers) {
        LOG.log(Level.INFO,"Offered mesos resources: "+offers.size()+" offers.");
        //Log, if possible the offers
        if (LOG.isLoggable(Level.FINER)) {
            for (Offer offer : offers) {
                try {
                    this.mon.addNode(new ResourceNode(offer.getSlaveId().getValue(),new URL("http://"+offer.getHostname()),-1));
                } catch (MalformedURLException e) {
                    LOG.log(Level.WARNING,"Cannot add node to monitor (bad url).  Giving up: "+e.getMessage());
                } catch ( MonitorException e) {
                    LOG.log(Level.WARNING,"Cannot add node to monitor (unkn).  Giving up: "+e.getMessage());
                }
                LOG.log(Level.FINER,"Offer ("+offer.getId().getValue()+"): "+offer.getHostname()+ "(Slave: "+
                        offer.getSlaveId().getValue()+") "+MesosUtilities.getResourceMessage(offer.getResourcesList()));
            }
        }
        List<JobSet> assignments = this.getJobAssignmentsJobs(offers);
        List<OfferID> used = new LinkedList<OfferID>();
        for (JobSet assignment : assignments) {
            //Launch tasks requires lists
            List<OfferID> ids = new LinkedList<OfferID>();
            List<TaskInfo> tasks = new LinkedList<TaskInfo>();
            tasks.add(assignment.task);
            used.add(assignment.offer.getId());
            ids.add(assignment.offer.getId());
            //Register locally and launch on mesos
            batch.registerExecutedJob(assignment.job.getJob().getId(), assignment.task.getTaskId());
            Status status = driver.launchTasks(ids,tasks); //Assumed one to one mapping
            if (status != Status.DRIVER_RUNNING)
                throw new MesosFrameworkException("Driver stopped: "+status.toString());
        }
        for (Offer offer : offers) {
            if (!used.contains(offer.getId())) {
                LOG.log(Level.INFO,"Rejecting Offer: "+offer.getId().getValue());
                driver.declineOffer(offer.getId());
            }
        }
    }
    /**
     * Builds a TaskInfo from the given jobspec
     * @param job - JobSpec to TaskInfo-ify
     * @param offer - offer add extra data (SlaveId)
     * @return TaskInfo fully formed
     */
    private TaskInfo getTaskInfo(JobSpec job,Offer offer) {
        TaskID taskId = TaskID.newBuilder().setValue(job.getJob().getId()).build();
        TaskInfo info = TaskInfo.newBuilder().setName("task " + taskId.getValue())
                 .setTaskId(taskId)
                 .setSlaveId(offer.getSlaveId())
                 .addResources(Resource.newBuilder()
                               .setName("cpus")
                               .setType(Value.Type.SCALAR)
                               .setScalar(Value.Scalar.newBuilder().setValue(job.getJob().getLoadValue()*1.0)))
                 .addResources(Resource.newBuilder()
                               .setName("mem")
                               .setType(Value.Type.SCALAR)
                               .setScalar(Value.Scalar.newBuilder().setValue(job.getJob().getLoadValue()*1024.0)))
                 .setExecutor(ExecutorInfo.newBuilder(executor)).setData(MesosUtilities.jobSpecToByteString(job)).build();
        return info;
    }
    /**
     * Checks all offers against jobs in order, assigning jobs to offers until each offer is full,
     * or all jobs are gone.
     * @param offers - offers to assign jobs to.
     * @return List of <JobSpec,TaskInfo,Offer> tuples (assigned to each other).
     */
    private List<JobSet> getJobAssignmentsJobs(List<Offer> offers) {
        List<JobSet> list = new LinkedList<JobSet>();
        for (Offer offer : offers)
        {
            double cpus = 0.0, mem = 0.0;
            //Get the resources offered from this offer
            for (Resource resc : offer.getResourcesList()) {
                if (resc.getName().equals("cpus"))
                    cpus += resc.getScalar().getValue();
                if (resc.getName().equals("mem"))
                    mem += resc.getScalar().getValue();
            }
            //Search for enough jobs to fill the offer
            for (int i = 0;i < queue.getSize();i++)
            {
                try {
                    JobSpec job = queue.getNextJob();
                    double load = job.getJob().getLoadValue();
                    //Check if enough resources
                    if (cpus < load || mem < load*1024)
                    {
                        queue.requeueJob(job);
                        continue;
                    }
                    cpus -= load;
                    mem -= 1024*load;
                    JobSet tmp = new JobSet(job,getTaskInfo(job,offer),offer);
                    list.add(tmp);
                    //Not enough left, optimise and stop looking for jobs
                    if (cpus < 0.5 || mem <= 512.0)
                        break;
                } catch (JobQueueException e) {throw new RuntimeException(e);}
            }
            //Optimization: break when no jobs
            if (queue.getSize() == 0)
                break;
        }
        return list;
    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Scheduler#slaveLost(org.apache.mesos.SchedulerDriver, org.apache.mesos.Protos.SlaveID)
     */
    @Override
    public void slaveLost(SchedulerDriver schedDriver, SlaveID slave) {
        LOG.log(Level.WARNING,"Mesos slave "+slave+" lost, reissuing jobs.");
        //TODO: reregister jobs
    }

    /* (non-Javadoc)
     * @see org.apache.mesos.Scheduler#statusUpdate(org.apache.mesos.SchedulerDriver, org.apache.mesos.Protos.TaskStatus)
     */
    @Override
    public void statusUpdate(SchedulerDriver schedDriver, TaskStatus taskStatus) {
        //TODO: deliver messages, some rerun, some finish.
        LOG.log(Level.INFO,"Status update: "+taskStatus.getMessage());
    }



    @Override
    public void run() {
        LOG.log(Level.INFO,"Attempting to run framework. Nothing to do.");
        LOG.log(Level.FINEST, "Paradigm shift enabled.");
        LOG.log(Level.FINEST, "Spin and poll surplanted by event based execution.");
        LOG.log(Level.FINEST, "Mesos-OODT Fusion complete.");
        //Don't run anything
        return;
    }



    @Override
    public boolean schedule(JobSpec spec) throws SchedulerException {
        throw new NotImplementedException("Schedule is not called when using mesos.");
    }



    @Override
    public ResourceNode nodeAvailable(JobSpec spec) throws SchedulerException {
        return null;
    }



    @Override
    public Monitor getMonitor() {
        return mon;
    }



    @Override
    public Batchmgr getBatchmgr() {
        return batch;
    }



    @Override
    public JobQueue getJobQueue() {
        // TODO Auto-generated method stub
        return queue;
    }



    @Override
    public QueueManager getQueueManager() {
        // TODO Auto-generated method stub
        return null;
    }
    //Job set used internally to simplify data transmission
    private class JobSet {
        public JobSpec job;
        public TaskInfo task;
        public Offer offer;
        //Build a job set
        public JobSet(JobSpec job, TaskInfo task, Offer offer) {
            this.job = job;
            this.task = task;
            this.offer = offer;
        }
    }
}
