//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.jobrepo;

//OODT imports
import gov.nasa.jpl.oodt.cas.resource.structs.JobSpec;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.JobRepositoryException;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * An interface for persisting {@link JobSpec}s
 * </p>.
 */
public interface JobRepository {

  public String addJob(JobSpec spec) throws JobRepositoryException;

  public void updateJob(JobSpec spec) throws JobRepositoryException;

  public void removeJob(JobSpec spec) throws JobRepositoryException;
  
  public JobSpec getJobById(String jobId) throws JobRepositoryException;

  public String getStatus(JobSpec spec) throws JobRepositoryException;

  public boolean jobFinished(JobSpec spec) throws JobRepositoryException;

}
