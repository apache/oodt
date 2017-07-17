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

import java.util.List;

import org.apache.oodt.cas.resource.jobqueue.JobQueue;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.exceptions.JobQueueException;
import org.apache.oodt.cas.resource.structs.exceptions.JobRepositoryException;

/**
 * The interface for a {@link JobQueue} that is aware of the different queues in
 * the ResourceManager and allows for the manipulation of jobs on a
 * queue-by-queue basis.
 * 
 * @author resneck
 *
 */
public interface MappedJobQueue extends JobQueue {

  /**
   * Returns a boolean value representing whether or not the job queue contains
   * jobs in the given queue.
   * 
   * @param queueName
   *          The name of the queue which you are checking.
   * @return true, if the queue is empty, false otherwise.
   * @throws JobQueueException
   *           If the given queue name is null or if no queue with that name
   *           exists.
   */
  public boolean isEmpty(String queueName) throws JobQueueException;

  /**
   * Gets the next {@link JobSpec} in the queue with the given name.
   * 
   * @param queueName
   *          The name of the queue from which the next {@link JobSpec} will be
   *          returned.
   * @return The next {@link JobSpec} from the jobqueue belonging to the queue
   *         with the given name.
   * @throws JobQueueException
   *           If the given queue name is null or if no queue with that name
   *           exists.
   */
  public JobSpec getNextJob(String queueName)
      throws JobQueueException, JobRepositoryException;

  /**
   * Gets the number of jobs in the queue with the given name.
   * 
   * @param queueName
   *          The name of the queue whos size will be given.
   * @return The number of {@link JobSpec}s in the queue with the given name.
   * @throws JobQueueException
   *           If the given queue name is null or if no queue with that name
   *           exists.
   */
  public int getSize(String queueName) throws JobQueueException;

  /**
   * Removes the {@link JobSpec} with the given ID from the queue.
   * 
   * @param id
   *          The ID of the {@link JobSpec} that will be removed.
   * @throws JobQueueException
   *           If no {@link JobSpec} has the given ID.
   */
  public void removeJob(JobSpec spec) throws JobQueueException;

  /**
   * Add a queue with the given name.
   * 
   * @param queueName
   *          The name of the queue to be created.
   * @throws JobQueueException
   *           If the given queue name is null or if a queue with the given name
   *           already exists.
   */
  public void addQueue(String queueName) throws JobQueueException;

  /**
   * Remove the queue with the given name.
   * 
   * @param queueName
   *          The name of the queue to be removed.
   * @throws JobQueueException
   *           If the given queue name is null or if no queue with the given
   *           name exists.
   */
  public void removeQueue(String queueName) throws JobQueueException;

  /**
   * Gets a list of all queued jobs that belong to the queue with the given
   * name.
   * 
   * @param queueName
   *          The name of the queue whose members will be given.
   * @return A {@link List} of queued {@JobSpec}s from the given queue.
   * @throws JobQueueException
   *           If the given queue name is null or if no queue with the given
   *           name exists.
   */
  public List<JobSpec> getQueuedJobs(String queueName)
      throws JobQueueException, JobRepositoryException;

  public String addJob(JobSpec spec) throws JobQueueException;

  public String requeueJob(JobSpec spec) throws JobQueueException;

  public void promoteJob(JobSpec spec) throws JobQueueException;

  public void promoteKeyValPair(String key, String val)
      throws JobQueueException, JobRepositoryException;

}