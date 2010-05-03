//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.jobqueue;

import gov.nasa.jpl.oodt.cas.resource.jobrepo.JobRepository;
import gov.nasa.jpl.oodt.cas.resource.util.GenericResourceManagerObjectFactory;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A factory for creating {@link JobStack} {@link JobQueue}s.
 * </p>.
 */
public class JobStackJobQueueFactory implements JobQueueFactory {

  /* the maximum size of the jobqueue */
  private int stackSize = -1;
  
  /* our job repository for persisting jobs */
  private JobRepository repo;

  public JobStackJobQueueFactory() {
    String stackSizeStr = System
        .getProperty("gov.nasa.jpl.oodt.cas.resource.jobqueue.jobstack.maxstacksize");

    if (stackSizeStr != null) {
      stackSize = Integer.parseInt(stackSizeStr);
    }
    
    String jobRepoFactoryClassStr = System.getProperty(
        "resource.jobrepo.factory",
        "gov.nasa.jpl.oodt.cas.resource.jobrepo.MemoryJobRepositoryFactory");
    this.repo = GenericResourceManagerObjectFactory
        .getJobRepositoryFromServiceFactory(jobRepoFactoryClassStr);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.resource.jobqueue.JobQueueFactory#createQueue()
   */
  public JobQueue createQueue() {
    return new JobStack(stackSize, repo);
  }

}
