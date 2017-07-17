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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.resource.jobrepo.JobRepository;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.JobStatus;
import org.apache.oodt.cas.resource.structs.exceptions.JobQueueException;
import org.apache.oodt.cas.resource.structs.exceptions.JobRepositoryException;

/**
 * This implementation provides a "queue-aware" {@link JobQueue} that ensures
 * the FIFO execution of jobs.
 * 
 * @author resneck
 *
 */
public class FifoMappedJobQueue implements MappedJobQueue {

  private Map<String, Vector<String>> queues;
  private int maxQueueSize;
  private JobRepository repo;
  private static final Logger LOG = Logger
      .getLogger(FifoMappedJobQueue.class.getName());

  public FifoMappedJobQueue(int maxSize, JobRepository repo) {
    this.maxQueueSize = maxSize;
    this.repo = repo;
    this.queues = new HashMap<String, Vector<String>>();
  }

  /**
   * @see org.apache.oodt.cas.resource.jobqueue.JobQueue.addJob(JobSpec)
   */
  public synchronized String addJob(JobSpec spec) throws JobQueueException {

    // Check if the job is null and if its queue exists
    if (spec == null) {
      throw new JobQueueException("A null job was given.");
    }
    String queueName = spec.getJob().getQueueName();
    validateQueueName(queueName);

    // Check if the jobs queue is full
    List<String> queue = queues.get(queueName);
    if (queue.size() == maxQueueSize) {
      throw new JobQueueException(
          "The queue " + spec.getJob().getQueueName() + " is full.  The job "
              + spec.getJob().getId() + " could not be requeued.");
    }

    // Add the job to the repository
    try {
      this.repo.addJob(spec);
    } catch (JobRepositoryException e) {
      throw new JobQueueException(
          "An error occurred while adding job " + spec.getJob().getId()
              + " to the job repository: " + e.getMessage());
    }

    // Add the job to the queue
    queue.add(spec.getJob().getId());

    // Update the jobs status
    spec.getJob().setStatus(JobStatus.QUEUED);
    try {
      this.repo.updateJob(spec);
    } catch (JobRepositoryException e) {
      throw new JobQueueException("An error occurred while updating "
          + "the status of job " + spec.getJob().getId()
          + " in the job repository: " + e.getMessage());
    }

    LOG.log(Level.INFO,
        "Job [id=" + spec.getJob().getId() + ",name=" + spec.getJob().getName()
            + "] was added to the job queue in queue " + queueName);
    return spec.getJob().getId();

  }

  /**
   * @see org.apache.oodt.cas.resource.jobqueue.JobQueue.requeueJob(JobSpec)
   */
  public synchronized String requeueJob(JobSpec spec) throws JobQueueException {

    // Check if the job is null and if its queue exists
    if (spec == null) {
      throw new JobQueueException("A null job was given.");
    }
    String queueName = spec.getJob().getQueueName();
    validateQueueName(queueName);

    List<String> queue = queues.get(queueName);

    // Place the job at the front of the queue
    queue.add(0, spec.getJob().getId());

    // Set the jobs status
    spec.getJob().setStatus(JobStatus.QUEUED);
    try {
      this.repo.updateJob(spec);
    } catch (JobRepositoryException e) {
      throw new JobQueueException("An error occurred while updating "
          + "the status of job " + spec.getJob().getId()
          + " in the job repository: " + e.getMessage());
    }

    return spec.getJob().getId();

  }

  /**
   * @see org.apache.oodt.cas.resource.jobqueue.JobQueue.getQueuedJobs()
   */
  public synchronized List getQueuedJobs() {

    List<JobSpec> allJobs = new Vector<JobSpec>();
    for (Iterator<Vector<String>> i = queues.values().iterator(); i
        .hasNext();) {
      List<String> queue = i.next();
      for (String jobId : queue) {
        try {
          allJobs.add(this.repo.getJobById(jobId));
        } catch (JobRepositoryException e) {
          LOG.log(Level.WARNING, "Failed to fetch JobSpec from repo: " + jobId);
        }
      }
    }

    return allJobs;

  }

  public synchronized List<JobSpec> getQueuedJobs(String queueName)
      throws JobQueueException, JobRepositoryException {

    // Check if the queue name is null or if it does not exist
    validateQueueName(queueName);

    List<JobSpec> queueJobs = new Vector<JobSpec>();
    for (String jobId : this.queues.get(queueName)) {
      queueJobs.add(this.repo.getJobById(jobId));
    }

    return queueJobs;

  }

  /**
   * @see org.apache.oodt.cas.resource.jobqueue.JobQueue.purge()
   */
  public synchronized void purge() {

    for (Iterator<Vector<String>> i = this.queues.values().iterator(); i
        .hasNext();) {
      i.next().removeAllElements();
    }

  }

  /**
   * @see org.apache.oodt.cas.resource.jobqueue.JobQueue.isEmpty()
   */
  public synchronized boolean isEmpty() {

    return this.getSize() == 0;

  }

  public synchronized boolean isEmpty(String queueName)
      throws JobQueueException {

    // Check if the queue name is null or if it does not exist
    validateQueueName(queueName);

    return this.queues.get(queueName).size() == 0;

  }

  /**
   * @see org.apache.oodt.cas.resource.jobqueue.JobQueue.getNextJob()
   */
  public synchronized JobSpec getNextJob() {

    // Check if any queues exist
    if (this.queues.keySet().size() == 0) {
      throw new RuntimeException("No queues are defined.");
    }

    // Check if all queues are empty
    if (this.isEmpty()) {
      throw new RuntimeException("The queue contains no jobs.");
    }

    // Look in each queue for a job
    for (Iterator<String> i = this.queues.keySet().iterator(); i.hasNext();) {

      // Check if the queue is empty
      List<String> queue = this.queues.get(i.next());
      if (!queue.isEmpty()) {

        // Check jobs from the front of the queue until we find one that
        // is flagged as ready to schedule
        for (int index = 0; index < queue.size(); index++) {

          // Check how the job is flagged
          String jobId = queue.get(index);
          JobSpec spec = null;
          try {
            spec = this.repo.getJobById(jobId);
          } catch (JobRepositoryException e) {
            LOG.log(Level.WARNING,
                "Failed to fetch JobSpec from repo: " + jobId);
          }
          if (spec.getJob().getReady()) {

            // Remove the job from the queue
            queue.remove(index);

            // Set the status of the fetched job
            spec.getJob().setStatus(JobStatus.SCHEDULED);
            try {
              this.repo.updateJob(spec);
            } catch (JobRepositoryException e) {
              LOG.log(Level.WARNING,
                  "The status of job " + spec.getJob().getId()
                      + "was not properly set "
                      + "after being dequeued. Message: " + e.getMessage());
            }

            return spec;

          }

        }

      }

    }

    return null;

  }


  public synchronized JobSpec getNextJob(String queueName)
      throws JobQueueException, JobRepositoryException {

    // Check if the given queue name is null and if it exists
    validateQueueName(queueName);

    // If the queue contains no jobs, return null
    List<String> queue = queues.get(queueName);
    if (queue.isEmpty()) {
      return null;
    }

    // Check jobs from the front of the queue until we find one that
    // is flagged as ready to schedule
    for (int index = 0; index < queue.size(); index++) {

      // Check how the job is flagged
      String jobId = queue.get(index);
      JobSpec spec = this.repo.getJobById(jobId);
      if (spec.getJob().getReady()) {

        // Remove the job from the queue
        queue.remove(index);

        // Set the status of the fetched job
        spec.getJob().setStatus(JobStatus.SCHEDULED);
        try {
          this.repo.updateJob(spec);
        } catch (JobRepositoryException e) {
          LOG.log(Level.WARNING,
              "The status of job " + spec.getJob().getId()
                  + "was not properly set after being" + " dequeued. Message: "
                  + e.getMessage());
        }

        return spec;

      }

    }

    return null;

  }

  /**
   * @see org.apache.oodt.cas.resource.jobqueue.JobQueue.getJobRepository()
   */
  public synchronized JobRepository getJobRepository() {

    return this.repo;

  }

  /**
   * @see org.apache.oodt.cas.resource.jobqueue.JobQueue.getSize()
   */
  public synchronized int getSize() {

    int totalJobs = 0;
    for (Iterator<Vector<String>> i = queues.values().iterator(); i
        .hasNext();) {
      totalJobs += i.next().size();
    }
    return totalJobs;

  }

  public synchronized int getSize(String queueName) throws JobQueueException {

    // Check if the given queue name is null and if it exists
    validateQueueName(queueName);

    return this.queues.get(queueName).size();

  }

  /**
   * This method returns the number of jobs in any given queue that can be
   * retained in the queue. This number does not change with the number of
   * queues in the ResourceManager.
   * 
   * @return The number of jobs of each queue that can be queued.
   */
  public int getCapacity() {

    return this.maxQueueSize;

  }

  // TODO: Write a javadoc for this method when it can actually be used by
  // the operator.
  public synchronized void removeJob(JobSpec spec) throws JobQueueException {

    // Check if the job is null and if its queue exists
    if (spec == null) {
      throw new JobQueueException("A null job was given.");
    }
    String queueName = spec.getJob().getQueueName();
    validateQueueName(queueName);

    // Get the ID of the job
    String id = spec.getJob().getId();

    // Find the job in the queue and remove it
    List<String> queue = this.queues.get(queueName);
    int index = getIndexInQueue(id, queue);
    if (index == -1) {
      LOG.log(Level.WARNING, "No job with ID " + id + "could be removed "
          + "since it was not found in the queue.");
    } else {
      queue.remove(index);
    }

  }

  public synchronized void addQueue(String queueName) throws JobQueueException {

    // Check if queue name is null or already exists
    if (queueName == null) {
      throw new JobQueueException("A null queue name was given.");
    }
    if (queues.containsKey(queueName)) {
      throw new JobQueueException("A queue with name " + queueName
          + " could not be created as one " + "with that name already exists.");
    }

    // Add the new queue to our map
    this.queues.put(queueName, new Vector());

  }


  public synchronized void removeQueue(String queueName)
      throws JobQueueException {

    // Check if the given queue name is null and if it exists
    validateQueueName(queueName);

    // Warn the user if they are losing jobs
    int queueSize = this.queues.get(queueName).size();
    if (queueSize > 0) {
      LOG.log(Level.WARNING, "The queue being removed (" + queueName
          + ") contains " + queueSize + " jobs.");
    }

    // Delete the queue
    this.queues.remove(queueName);

  }

  public synchronized void promoteJob(JobSpec spec) throws JobQueueException {

    // Check if the job is null and if its queue exists
    if (spec == null) {
      throw new JobQueueException("A null job was given.");
    }
    String queueName = spec.getJob().getQueueName();
    validateQueueName(queueName);

    // Get the ID of the job
    String id = spec.getJob().getId();

    // Find the job in the queue and move it to the front
    List<String> queue = this.queues.get(queueName);
    int index = getIndexInQueue(id, queue);
    if (index == -1) {
      LOG.log(Level.WARNING, "No job with ID " + id + "could be promoted "
          + "since it was not found in the queue.");
    } else {
      queue.add(0, queue.remove(index));
    }

  }
  
  public synchronized List<String> getQueueNames(){
    if (this.queues != null && this.queues.keySet() != null && 
        this.queues.keySet().size() > 0){
      return Arrays.asList(queues.keySet().toArray(new String[]{""}));
    }
    return Collections.EMPTY_LIST;
  }

  public synchronized void promoteKeyValPair(String key, String val)
      throws JobQueueException, JobRepositoryException {

    List<JobSpec> specsToPromote = new Vector<JobSpec>();

    for (Iterator<Vector<String>> i = queues.values().iterator(); i
        .hasNext();) {
      List<String> queue = i.next();
      for (String jobId : queue) {
        JobSpec spec = null;
        try {
          spec = this.repo.getJobById(jobId);
        } catch (JobRepositoryException e) {
          LOG.log(Level.WARNING, "Failed to fetch JobSpec from repo: " + jobId);
        }
        if (spec.getIn().getMetadata().get(key).equals(val)) {
          specsToPromote.add(spec);
        }
      }
    }

    for (JobSpec spec : specsToPromote) {
      promoteJob(spec);
    }

  }

  /**
   * This method checks if a given queue name is valid
   * 
   * @param queueName
   *          The name of the queue to validate
   * @throws JobQueueException
   *           If the name is null or no queue with the given name exists
   */
  private void validateQueueName(String queueName) throws JobQueueException {

    if (queueName == null) {
      throw new JobQueueException("A null queue name was given.");
    }
    if (!queues.containsKey(queueName)) {
      throw new JobQueueException(
          "An invalid queue name was given: " + queueName);
    }

  }

  private int getIndexInQueue(String id, List<String> queue) {
    for (int i = 0; i < queue.size(); i++) {
      if (queue.get(i).equals(id)) {
        return i;
      }
    }
    return -1;
  }

}