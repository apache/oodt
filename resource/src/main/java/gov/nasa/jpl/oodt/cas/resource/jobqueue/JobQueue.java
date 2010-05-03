//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.jobqueue;

//JDK imports
import java.util.List;

//OODT imports
import gov.nasa.jpl.oodt.cas.resource.jobrepo.JobRepository;
import gov.nasa.jpl.oodt.cas.resource.structs.JobSpec;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.JobQueueException;

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
  public String addJob(JobSpec spec) throws JobQueueException;

  /**
   * Gets an ordered {@link List} of queued {@link JobSpec}s.
   * 
   * @return An ordered {@link List} of queued {@link JobSpec}s.
   * @throws JobQueueException
   *           If there is any error obtaining the queued jobs.
   */
  public List getQueuedJobs() throws JobQueueException;

  /**
   * Purges all {@link JobSpec}s from the queue.
   * 
   * @throws JobQueueException
   *           If there is any error purging all the {@link JobSpec}s.
   */
  public void purge() throws JobQueueException;

  /**
   * Returns a boolean value representing whether or not the queue is empty.
   * 
   * @return true, if the queue is empty, false otherwise.
   */
  public boolean isEmpty();

  /**
   * Gets the next {@link JobSpec} from the queue, and correspondingly removes
   * it from persitance.
   * 
   * @return The next {@link JobSpec} from the queue.
   * @throws JobQueueException
   *           If there is any error getting the next {@link JobSpec}.
   */
  public JobSpec getNextJob() throws JobQueueException;
  
  
  /**
   * Gets the underlying {@link JobSpec} persistance layer
   * used by this {@link JobQueue}.
   * @return The underlying {@link JobRepository}.
   */
  public JobRepository getJobRepository();

  /**
   * Gets the number of jobs in queue
   * @return Number of jobs in queue
   */
  public int getSize();
  
  /**
   * Gets the max number of jobs allowed in
   * queue at any given time
   * @return Max number of jobs
   */
  public int getCapacity();
  
}
