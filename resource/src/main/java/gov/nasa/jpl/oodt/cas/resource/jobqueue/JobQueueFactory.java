//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.jobqueue;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A factory for creating {@link JobQueue} services
 * </p>.
 */
public interface JobQueueFactory {

  /**
   * Creates new {@link JobQueue} implementations.
   * 
   * @return New {@link JobQueue} implementations.
   */
  public JobQueue createQueue();
}
