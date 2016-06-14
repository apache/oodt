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

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.SchedulerDriver;
import org.apache.oodt.cas.resource.jobrepo.JobRepository;
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.JobExecutionException;
import org.apache.oodt.cas.resource.structs.exceptions.MesosFrameworkException;

/**
 * @author starchmd
 * @version $Revision$
 *
 * A batch-manager used to execute and control jobs in a mesos-cluster.
 */
public class MesosBatchManager implements Batchmgr {

    Map<String,TaskID> map = new ConcurrentHashMap<String,TaskID>();
    SchedulerDriver driver;
    JobRepository repo;
    Monitor mon;

    public MesosBatchManager() {
    }
    /**
     * Required to set the driver used to run the job, so "kill"
     * requests are mapped properly.
     * @param driver
     */
    public void setDriver(SchedulerDriver driver)
    {
        this.driver = driver;
    }
    /**
     * Register a new job with a batch manager.
     * @param jobId - jobId in "resource" manager.
     * @param task - mesos task.
     */
    public void registerExecutedJob(String jobId,TaskID task) {
        map.put(jobId, task);
    };


    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.batchmgr.Batchmgr#executeRemotely(org.apache.oodt.cas.resource.structs.JobSpec, org.apache.oodt.cas.resource.structs.ResourceNode)
     */
    @Override
    public boolean executeRemotely(JobSpec job, ResourceNode resNode)
            throws JobExecutionException {
        throw new NotImplementedException("Execute remotely is not called when using mesos.");
    }


    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.batchmgr.Batchmgr#setMonitor(org.apache.oodt.cas.resource.monitor.Monitor)
     */
    @Override
    public void setMonitor(Monitor monitor) {
        this.mon = monitor;
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
        TaskID id = (TaskID)map.get(jobId);
        driver.killTask(id);
        Status status = driver.killTask(id);
        if (status != Status.DRIVER_RUNNING)
            throw new MesosFrameworkException("Mesos Schedule Driver is dead: "+status.toString());
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.resource.batchmgr.Batchmgr#getExecutionNode(java.lang.String)
     */
    @Override
    public String getExecutionNode(String jobId) {
        // TODO Make this more meaningful.
        return "All Your Jobs are belong to Mesos";
    }

}
