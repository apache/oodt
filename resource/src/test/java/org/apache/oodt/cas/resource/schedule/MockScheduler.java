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
package org.apache.oodt.cas.resource.schedule;

//OODT imports
import org.apache.oodt.cas.resource.batchmgr.Batchmgr;
import org.apache.oodt.cas.resource.jobqueue.JobQueue;
import org.apache.oodt.cas.resource.monitor.Monitor;
import org.apache.oodt.cas.resource.scheduler.QueueManager;
import org.apache.oodt.cas.resource.scheduler.Scheduler;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.SchedulerException;

/**
 * A Mock {@link Scheduler}.
 *
 * @author bfoster (Brian Foster)
 */
public class MockScheduler implements Scheduler {

   public void run() {
      // Do nothing.
   }

   public boolean schedule(JobSpec spec) throws SchedulerException {
      return false;
   }

   public ResourceNode nodeAvailable(JobSpec spec) throws SchedulerException {
      return null;
   }

   public Monitor getMonitor() {
      return null;
   }

   public Batchmgr getBatchmgr() {
      return null;
   }

   public JobQueue getJobQueue() {
      return null;
   }

   public QueueManager getQueueManager() {
      return null;
   }
}
