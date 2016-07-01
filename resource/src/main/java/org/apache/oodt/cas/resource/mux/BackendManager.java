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

import org.apache.oodt.cas.resource.batchmgr.Batchmgr;
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.scheduler.Scheduler;
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;

/**
 * Interface for the backend manager
 *
 * @author starchmd
 */
public interface BackendManager {

    /**
     * Add in a backend set to this manager.
     * @param queue - queue that maps to the given monitor, batchmgr, and scheduler
     * @param monitor - monitor used for this set
     * @param batchmgr - batch manager for this set
     * @param scheduler - scheduler for this set
     */
    void addSet(String queue, Monitor monitor, Batchmgr batchmgr, Scheduler scheduler);
    /**
     * Return monitor for the given queue.
     * @param queue - queue to check
     * @return montior
     * @throws QueueManagerException when queue does not exist
     */
    Monitor getMonitor(String queue) throws QueueManagerException;
    /**
     * Return batch manager for the given queue.
     * @param queue - queue to check
     * @return batchmgr
     * @throws QueueManagerException when queue does not exist
     */
    Batchmgr getBatchmgr(String queue) throws QueueManagerException;
    /**
     * Return scheduler for the given queue.
     * @param queue - queue to check
     * @return scheduler
     * @throws QueueManagerException when queue does not exist
     */
    Scheduler getScheduler(String queue) throws QueueManagerException;
    /**
     * Return a list of all monitors.
     * @return list of all monitors
     */
    List<Monitor> getMonitors();
}
