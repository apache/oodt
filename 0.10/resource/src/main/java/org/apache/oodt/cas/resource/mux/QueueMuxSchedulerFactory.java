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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.resource.jobqueue.JobQueue;
import org.apache.oodt.cas.resource.jobqueue.JobStackJobQueueFactory;
import org.apache.oodt.cas.resource.queuerepo.XmlQueueRepositoryFactory;
import org.apache.oodt.cas.resource.scheduler.QueueManager;
import org.apache.oodt.cas.resource.scheduler.Scheduler;
import org.apache.oodt.cas.resource.scheduler.SchedulerFactory;
import org.apache.oodt.cas.resource.structs.exceptions.RepositoryException;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;

/**
 * This class acts as a factory for the whole queue-mux
 * set of classes.
 *
 * @author starchmd
 */
public class QueueMuxSchedulerFactory implements SchedulerFactory {

    private static final Logger LOG = Logger.getLogger(QueueMuxSchedulerFactory.class.getName());

    BackendManager backend;
    QueueManager qManager;
    JobQueue jobQueue;
    /**
     * ctor
     */
    public QueueMuxSchedulerFactory() {
        //Load backend manager
        String  backRepo = System.getProperty("resource.backend.mux.repository",
                XmlBackendRepository.class.getCanonicalName());
        try {
            backend = GenericResourceManagerObjectFactory.getBackendRepositoryFromFactory(backRepo).load();
        } catch (RepositoryException e) {
            LOG.log(Level.SEVERE,"Error loading backend repository: "+e.getMessage(),e);
            backend = null;
        }
        //Load user-specified queue factory
        String qFact = System.getProperty("org.apache.oodt.cas.resource.queues.repo.factory",
                XmlQueueRepositoryFactory.class.getCanonicalName());
        qManager = GenericResourceManagerObjectFactory.getQueueRepositoryFromFactory(
                qFact).loadQueues();
        //Load job queue
        String jobFact = System.getProperty("resource.jobqueue.factory",
                        JobStackJobQueueFactory.class.getCanonicalName());
        jobQueue = GenericResourceManagerObjectFactory
                .getJobQueueServiceFromFactory(jobFact);
    }

    @Override
    public Scheduler createScheduler() {
        return new QueueMuxScheduler(this.backend, this.qManager, this.jobQueue);
    }
}
