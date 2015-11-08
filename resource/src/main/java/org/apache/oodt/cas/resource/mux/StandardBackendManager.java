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

import org.apache.oodt.cas.resource.batchmgr.Batchmgr;
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.scheduler.Scheduler;
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This manager keeps track of the mux-able backends for the resource manager.
 * It effectively maps a queue to the backend that this queue feeds.
 *
 * It uses a private BackendSet to keep track of everything.
 *
 * For reference, a backend is a set of the following:
 *    1. Batch manger, responsible for running jobs
 *    2. Scheduler, responsible for scheduling a job to run
 *    3. Monitor, responsible for managing nodes
 *
 * @author starchmd
 */
public class StandardBackendManager implements BackendManager {
    Map<String,BackendSet> queueToBackend = new HashMap<String,BackendSet>();

    /**
     * Add in a backend set to this manager.
     * @param queue - queue that maps to the given monitor, batchmgr, and scheduler
     * @param monitor - monitor used for this set
     * @param batchmgr - batch manager for this set
     * @param scheduler - scheduler for this set
     */
    public void addSet(String queue,Monitor monitor, Batchmgr batchmgr, Scheduler scheduler) {
        queueToBackend.put(queue, new BackendSet(monitor,batchmgr,scheduler));
    }
    /**
     * Return monitor for the given queue.
     * @param queue - queue to check
     * @return montior
     * @throws QueueManagerException when queue does not exist
     */
    public Monitor getMonitor(String queue) throws QueueManagerException {
        BackendSet set = queueToBackend.get(queue);
        if (set == null) {
            throw new QueueManagerException("Queue '" + queue + "' does not exist");
        }
        return set.monitor;
    }
    /**
     * Return batch manager for the given queue.
     * @param queue - queue to check
     * @return batchmgr
     * @throws QueueManagerException when queue does not exist
     */
    public Batchmgr getBatchmgr(String queue) throws QueueManagerException {
        BackendSet set = queueToBackend.get(queue);
        if (set == null) {
            throw new QueueManagerException("Queue '" + queue + "' does not exist");
        }
        return set.batchmgr;
    }
    /**
     * Return scheduler for the given queue.
     * @param queue - queue to check
     * @return scheduler
     * @throws QueueManagerException when queue does not exist
     */
    public Scheduler getScheduler(String queue) throws QueueManagerException {
        BackendSet set = queueToBackend.get(queue);
        if (set == null) {
            throw new QueueManagerException("Queue '" + queue + "' does not exist");
        }
        return set.scheduler;
    }
    /**
     * Return a list of all monitors.
     * @return list of all monitors
     */
    public List<Monitor> getMonitors() {
        List<Monitor> monitors = new LinkedList<Monitor>();
        for (BackendSet set : queueToBackend.values()) {
            monitors.add(set.monitor);
        }
        return monitors;
    }
    /**
     * Class that holds a set of the three backend pieces.
     * Private class, because no accessor/modifiers have been
     * created(public members). Acts like a struct.
     *
     * @author starchmd
     */
    private class BackendSet {
        public Monitor monitor = null;
        public Batchmgr batchmgr = null;
        public Scheduler scheduler = null;

        public BackendSet(Monitor monitor, Batchmgr batchmgr, Scheduler scheduler) {
            this.monitor = monitor;
            this.batchmgr = batchmgr;
            this.scheduler = scheduler;
        }
    }
}
