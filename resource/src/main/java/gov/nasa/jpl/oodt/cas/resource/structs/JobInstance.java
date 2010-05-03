//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$
package gov.nasa.jpl.oodt.cas.resource.structs;

import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.JobInputException;

/**
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * A job interface: the thing that actually performs the work.
 * </p>
 */
public interface JobInstance {

  /**
   * Executes the underlying code for this job.
   * 
   * @param in
   *          The Job's input.
   * 
   * @return true if the job execution was successful, false otherwise.
   * @throws JobInputException
   *           If there was an error handling the {@link JobInput}.
   */
  boolean execute(JobInput in) throws JobInputException;
}
