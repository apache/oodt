//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.scheduler;

//OODT imports
import gov.nasa.jpl.oodt.cas.resource.batchmgr.Batchmgr;
import gov.nasa.jpl.oodt.cas.resource.jobqueue.JobQueue;
import gov.nasa.jpl.oodt.cas.resource.monitor.Monitor;
import gov.nasa.jpl.oodt.cas.resource.structs.JobSpec;
import gov.nasa.jpl.oodt.cas.resource.structs.ResourceNode;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.SchedulerException;

/**
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * A scheduler interface.
 * </p>
 * 
 */
public interface Scheduler extends Runnable{

	/**
	 * Schedules a job to be executed by a particular batch manager.
	 * 
	 * @param spec
	 *            The {@link JobSpec} to schedule for execution.
	 * @return Whether the job was successfully scheduled or not.
  * @throws SchedulerException If there was any error scheduling
  * the given {@link JobSpec}.
	 */
	public boolean schedule(JobSpec spec) throws SchedulerException;


 /**
  * Returns the ResourceNode that is considered to be <quote>most available</quote>
  * within our underlying set of resources for the given JobSpec.
  * @param spec The JobSpec to find an available node for.
  * @return The {@link ResourceNode} best suited to handle this {@link JobSpec}
  * @throws SchedulerException If any error occurs.
  */
 public ResourceNode nodeAvailable(JobSpec spec) throws SchedulerException;

 /**
  * 
  * @return The underlying {@link Monitor} used by this
  * Scheduler.
  */
 public Monitor getMonitor();
 
 /**
  * 
  * @return The underlying {@link Batchmgr} used by this
  * Scheduler.
  */
 public Batchmgr getBatchmgr();
 
 
 /**
  * 
  * @return The underlying {@link JobQueue} used by this
  * Scheduler.
  */
 public JobQueue getJobQueue();

}