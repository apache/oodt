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
 * An exception thrown when persisting a {@link Job} to a {@link JobRepository}.
 * </p>.
 */
public class JobRepositoryException extends Exception {

  /* serial version UID */
  private static final long serialVersionUID = 7431338125659704680L;

  /**
   * 
   */
  public JobRepositoryException() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   */
  public JobRepositoryException(String arg0) {
    super(arg0);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   */
  public JobRepositoryException(Throwable arg0) {
    super(arg0);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   * @param arg1
   */
  public JobRepositoryException(String arg0, Throwable arg1) {
    super(arg0, arg1);
    // TODO Auto-generated constructor stub
  }

}
