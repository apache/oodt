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
 * An exception thrown by the {@link Scheduler} when an error occurs.
 * </p>
 */
public class SchedulerException extends Exception {

  /* serial version UID */
  private static final long serialVersionUID = 4568261126290589269L;

  /**
   * 
   */
  public SchedulerException() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   */
  public SchedulerException(String message) {
    super(message);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param cause
   */
  public SchedulerException(Throwable cause) {
    super(cause);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param cause
   */
  public SchedulerException(String message, Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

}
