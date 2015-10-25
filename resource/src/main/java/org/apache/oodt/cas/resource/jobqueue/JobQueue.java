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


package org.apache.oodt.cas.resource.jobqueue;

//JDK imports
import java.util.List;

//OODT imports
import org.apache.oodt.cas.resource.jobrepo.JobRepository;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.exceptions.JobQueueException;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * An interface for persisting {@link JobSpec}s used by a
 * {@link JobQueueController}.
 * </p>.
 */
public interface JobQueue {

  /**
   * Persists a {@link JobSpec} to the queue.
   * 
   * @param spec
   *          The {@link JobSpec} to persist.
   * @return The ID of the JobSpec in the queue.
   * @throws JobQueueException
   *           If there is any error queueing the {@link JobSpec}.
   */
  String addJob(JobSpec spec) throws JobQueueException;
  
  /**
   * Re-adds a {@link JobSpec} to the back of the queue.
   * 
   * @param spec
   *          The {@link JobSpec} to re-add.
   * @throws JobQueueException
   *           If there is any error requeueing the {@link JobSpec}.
   */
  String requeueJob(JobSpec spec) throws JobQueueException;
  
  /**
   * Gets an ordered {@link List} of queued {@link JobSpec}s.
   * 
   * @return An ordered {@link List} of queued {@link JobSpec}s.
   * @throws JobQueueException
   *           If there is any error obtaining the queued jobs.
   */
  List getQueuedJobs();

  /**
   * Purges all {@link JobSpec}s from the queue.
   * 
   * @throws JobQueueException
   *           If there is any error purging all the {@link JobSpec}s.
   */
  void purge();

  /**
   * Returns a boolean value representing whether or not the queue is empty.
   * 
   * @return true, if the queue is empty, false otherwise.
   */
  boolean isEmpty();

  /**
   * Gets the next {@link JobSpec} from the queue, and correspondingly removes
   * it from persitance.
   * 
   * @return The next {@link JobSpec} from the queue.
   * @throws JobQueueException
   *           If there is any error getting the next {@link JobSpec}.
   */
  JobSpec getNextJob();
  
  
  /**
   * Gets the underlying {@link JobSpec} persistance layer
   * used by this {@link JobQueue}.
   * @return The underlying {@link JobRepository}.
   */
  JobRepository getJobRepository();

  /**
   * Gets the number of jobs in queue
   * @return Number of jobs in queue
   */
  int getSize();
  
  /**
   * Gets the max number of jobs allowed in
   * queue at any given time
   * @return Max number of jobs
   */
  int getCapacity();
  
}
