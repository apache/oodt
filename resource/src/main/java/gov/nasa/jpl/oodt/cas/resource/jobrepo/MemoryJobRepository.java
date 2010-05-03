//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.jobrepo;

//JDK imports
import java.util.Date;
import java.util.HashMap;

//OODT imports
import jpl.eda.util.DateConvert;
import gov.nasa.jpl.oodt.cas.resource.structs.JobSpec;
import gov.nasa.jpl.oodt.cas.resource.structs.JobStatus;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.JobRepositoryException;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * An implementation of a {@link JobRepository} that uses an internal
 * {@link HashMap} for persisting its {@link JobSpec}s.
 */
public class MemoryJobRepository implements JobRepository {

  /*
   * our storage for {@link JobSpec}s. A map of job id to {@link JobSpec}.
   */
  private HashMap jobMap = null;

  public MemoryJobRepository() {
    jobMap = new HashMap();
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.resource.jobrepo.JobRepository#addJob(gov.nasa.jpl.oodt.cas.resource.structs.JobSpec)
   */
  public String addJob(JobSpec spec) throws JobRepositoryException {
    // need to generate a JobId for this job
    String jobId = DateConvert.isoFormat(new Date());

    if (spec.getJob() != null) {
      spec.getJob().setId(jobId);
      jobMap.put(jobId, spec);
      return jobId;
    } else
      throw new JobRepositoryException("Exception persisting job: job is null!");
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.resource.jobrepo.JobRepository#getStatus(gov.nasa.jpl.oodt.cas.resource.structs.JobSpec)
   */
  public String getStatus(JobSpec spec) throws JobRepositoryException {
    JobSpec persistedSpec = (JobSpec) jobMap.get(spec.getJob().getId());
    return persistedSpec.getJob().getStatus();
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.resource.jobrepo.JobRepository#jobFinished(gov.nasa.jpl.oodt.cas.resource.structs.JobSpec)
   */
  public boolean jobFinished(JobSpec spec) throws JobRepositoryException {
    JobSpec persistedSpec = (JobSpec) jobMap.get(spec.getJob().getId());
    return persistedSpec.getJob().getStatus().equals(JobStatus.COMPLETE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.resource.jobrepo.JobRepository#removeJob(gov.nasa.jpl.oodt.cas.resource.structs.JobSpec)
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
   * @see gov.nasa.jpl.oodt.cas.resource.jobrepo.JobRepository#updateJob(gov.nasa.jpl.oodt.cas.resource.structs.JobSpec)
   */
  public void updateJob(JobSpec spec) throws JobRepositoryException {
    jobMap.put(spec.getJob().getId(), spec);

  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.resource.jobrepo.JobRepository#getJobById(java.lang.String)
   */
  public JobSpec getJobById(String jobId) throws JobRepositoryException {
    return (JobSpec) jobMap.get(jobId);
  }

}
