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

//OODT imports
import org.apache.oodt.cas.resource.jobrepo.JobRepository;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.JobStatus;
import org.apache.oodt.cas.resource.structs.exceptions.JobQueueException;
import org.apache.oodt.cas.resource.structs.exceptions.JobRepositoryException;

//JAVA imports
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * The Job Stack for unallocatable Jobs.
 * </p>
 */
public class JobStack implements JobQueue {
  
  /* our queue */
  private Vector queue;

  /* max queue size */
  private int maxQueueSize;
  
  /* our job persistance layer */
  private JobRepository repo;

  /* our log stream */
  private static final Logger LOG = Logger.getLogger(JobStack.class.getName());

  public JobStack(int maxSize, JobRepository repo) {
    queue = new Vector();
    maxQueueSize = maxSize;
    this.repo = repo;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.resource.jobqueue.JobQueue#addJob(org.apache.oodt.cas.resource.structs.JobSpec)
   */
  public String addJob(JobSpec spec) throws JobQueueException {
    String jobId = safeAddJob(spec);
    if (queue.size() != maxQueueSize) {
      LOG
          .log(Level.INFO, "Added Job: [" + spec.getJob().getId()
              + "] to queue");
      queue.add(spec);
      spec.getJob().setStatus(JobStatus.QUEUED);
      safeUpdateJob(spec);
      return jobId;
    } else {
      throw new JobQueueException("Reached max queue size: [" + maxQueueSize
                                  + "]: Unable to add job: [" + spec.getJob().getId() + "]");
    }
  }

  /*
   * (non-Javadoc)
   * @see gov.nasa.jpl.oodt.cas.resource.jobqueue.JobQueue#requeueJob(gov.nasa.jpl.oodt.cas.resource.structs.JobSpec)
   */
  public String requeueJob(JobSpec spec) throws JobQueueException {
	  try {
	      queue.add(spec);
	      spec.getJob().setStatus(JobStatus.QUEUED);
	      safeUpdateJob(spec);
	      return spec.getJob().getId();
	  }catch (Exception e) {
		  throw new JobQueueException("Failed to re-queue job '"
                    + (spec != null ? spec.getJob().getId() : "null") + "' : "
                    + e.getMessage(), e);
	  }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.resource.jobqueue.JobQueue#getQueuedJobs()
   */
  public List getQueuedJobs() {
    return queue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.resource.jobqueue.JobQueue#purge()
   */
  public void purge() {
    queue.removeAllElements();
    //TODO: think about whether or not it makes
    //sense to do something with the JobRepository
    //here too
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.resource.jobqueue.JobQueue#isEmpty()
   */
  public boolean isEmpty() {
    return queue.isEmpty();
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.resource.jobqueue.JobQueue#getNextJob()
   */
  public JobSpec getNextJob() {
    JobSpec spec = (JobSpec)queue.remove(0);
    // update its status since getNextJob is
    // called by the scheduler when it is going
    // to execute a job
    spec.getJob().setStatus(JobStatus.SCHEDULED);
    safeUpdateJob(spec);
    return spec;
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.resource.jobqueue.JobQueue#getJobRepository()
   */
  public JobRepository getJobRepository() {
    return repo;
  }
  
  private void safeUpdateJob(JobSpec spec) {
    try {
      this.repo.updateJob(spec);
    } catch (JobRepositoryException e) {
      LOG.log(Level.WARNING, "Exception updating job: ["
          + spec.getJob().getId() + "]: Message: " + e.getMessage());
    }
  }
  
  private String safeAddJob(JobSpec spec) {
    try {
      return this.repo.addJob(spec);
    } catch (JobRepositoryException e) {
      LOG
          .log(Level.WARNING, "Exception adding job: Message: "
              + e.getMessage());
      return null;
    }
  }

	public int getCapacity() {
		return this.maxQueueSize;
	}

	public int getSize() {
		return this.queue.size();
	}

}
