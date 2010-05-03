//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.structs.exceptions;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Exception thrown when there is an error with the 
 * {@link JobQueue}ing subsystem.
 * </p>.
 */
public class JobQueueException extends Exception {

  /* serial version UID */
  private static final long serialVersionUID = -297153382304522924L;

  /**
   * 
   */
  public JobQueueException() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   */
  public JobQueueException(String arg0) {
    super(arg0);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   */
  public JobQueueException(Throwable arg0) {
    super(arg0);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   * @param arg1
   */
  public JobQueueException(String arg0, Throwable arg1) {
    super(arg0, arg1);
    // TODO Auto-generated constructor stub
  }

}
