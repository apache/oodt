//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.structs.exceptions;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>An exception thrown by a {@link JobInstance}
 * when the given {@link JobInput} is not what it
 * expected</p>.
 */
public class JobInputException extends JobException {

  /* the serial version UID */
  private static final long serialVersionUID = 1673211096324899148L;

  /**
   * 
   */
  public JobInputException() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   */
  public JobInputException(String arg0) {
    super(arg0);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   */
  public JobInputException(Throwable arg0) {
    super(arg0);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param cause
   */
  public JobInputException(String message, Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

}
