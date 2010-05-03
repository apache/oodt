//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.structs.exceptions;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>An Exception thrown by the {@link Monitor}ing
 * framework</p>.
 */
public class MonitorException extends Exception {

  /* the serial version UID */
  private static final long serialVersionUID = 626993879645497320L;

  /**
   * 
   */
  public MonitorException() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   */
  public MonitorException(String arg0) {
    super(arg0);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   */
  public MonitorException(Throwable arg0) {
    super(arg0);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   * @param arg1
   */
  public MonitorException(String arg0, Throwable arg1) {
    super(arg0, arg1);
    // TODO Auto-generated constructor stub
  }

}
