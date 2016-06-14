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


package org.apache.oodt.cas.resource.jobrepo;

//JDK imports
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

//OODT imports
import org.apache.oodt.commons.util.DateConvert;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.JobStatus;
import org.apache.oodt.cas.resource.structs.exceptions.JobRepositoryException;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * An implementation of a {@link JobRepository} that uses an internal
 * {@link ConcurrentHashMap} for persisting its {@link JobSpec}s.
 */
public class MemoryJobRepository implements JobRepository {

  /*
   * our storage for {@link JobSpec}s. A map of job id to {@link JobSpec}.
   */
  private ConcurrentHashMap jobMap = null;

  public MemoryJobRepository() {
    jobMap = new ConcurrentHashMap();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.resource.jobrepo.JobRepository#addJob(org.apache.oodt.cas.resource.structs.JobSpec)
   */
  public String addJob(JobSpec spec) throws JobRepositoryException {
    // need to generate a JobId for this job
    String jobId = DateConvert.isoFormat(new Date());

    if (spec.getJob() != null) {
      spec.getJob().setId(jobId);
      jobMap.put(jobId, spec);
      return jobId;
    } else {
      throw new JobRepositoryException("Exception persisting job: job is null!");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.resource.jobrepo.JobRepository#getStatus(org.apache.oodt.cas.resource.structs.JobSpec)
   */
  public String getStatus(JobSpec spec) throws JobRepositoryException {
    JobSpec persistedSpec = (JobSpec) jobMap.get(spec.getJob().getId());
    return persistedSpec.getJob().getStatus();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.resource.jobrepo.JobRepository#jobFinished(org.apache.oodt.cas.resource.structs.JobSpec)
   */
  public boolean jobFinished(JobSpec spec) throws JobRepositoryException {
    JobSpec persistedSpec = (JobSpec) jobMap.get(spec.getJob().getId());
    return persistedSpec.getJob().getStatus().equals(JobStatus.SUCCESS) 
    	||  persistedSpec.getJob().getStatus().equals(JobStatus.FAILURE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.resource.jobrepo.JobRepository#removeJob(org.apache.oodt.cas.resource.structs.JobSpec)
   */
  public void removeJob(JobSpec spec) throws JobRepositoryException {
    if (jobMap.remove(spec.getJob().getId()) == null) {
      throw new JobRepositoryException("Attempt to remove a job: ["
          + spec.getJob().getId() + "] that is not currently persisted");
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.resource.jobrepo.JobRepository#updateJob(org.apache.oodt.cas.resource.structs.JobSpec)
   */
  public void updateJob(JobSpec spec) throws JobRepositoryException {
    jobMap.put(spec.getJob().getId(), spec);

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.resource.jobrepo.JobRepository#getJobById(java.lang.String)
   */
  public JobSpec getJobById(String jobId) throws JobRepositoryException {
    return (JobSpec) jobMap.get(jobId);
  }

}
