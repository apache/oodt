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

//OODT imports

import org.apache.oodt.cas.resource.batchmgr.Batchmgr;
import org.apache.oodt.cas.resource.jobqueue.JobQueue;
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.SchedulerException;

/**
 * @author woollard
 * @author bfoster
 * @version $Revision$
 * <p>
 * <p>
 * A scheduler interface.
 * </p>
 */
public interface Scheduler extends Runnable {

    /**
     * Schedules a job to be executed by a particular batch manager.
     *
     * @param spec The {@link JobSpec} to schedule for execution.
     * @return Whether the job was successfully scheduled or not.
     * @throws SchedulerException If there was any error scheduling
     *                            the given {@link JobSpec}.
     */
    boolean schedule(JobSpec spec) throws SchedulerException;


    /**
     * Returns the ResourceNode that is considered to be <quote>most available</quote>
     * within our underlying set of resources for the given JobSpec.
     *
     * @param spec The JobSpec to find an available node for.
     * @return The {@link ResourceNode} best suited to handle this {@link JobSpec}
     * @throws SchedulerException If any error occurs.
     */
    ResourceNode nodeAvailable(JobSpec spec) throws SchedulerException;

    /**
     * @return The underlying {@link Monitor} used by this
     * Scheduler.
     */
    Monitor getMonitor();

    /**
     * @return The underlying {@link Batchmgr} used by this
     * Scheduler.
     */
    Batchmgr getBatchmgr();


    /**
     * @return The underlying {@link JobQueue} used by this
     * Scheduler.
     */
    JobQueue getJobQueue();

    /**
     * @return The underlying {@link QueueManager} used by this
     * Scheduler.
     */
    QueueManager getQueueManager();

}
