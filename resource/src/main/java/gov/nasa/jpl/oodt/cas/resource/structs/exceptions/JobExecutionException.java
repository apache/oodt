//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.structs.exceptions;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>Exception thrown when executing a {@link Job}.</p>
 */
public class JobExecutionException extends JobException {

	/* serial version UID */
	private static final long serialVersionUID = 411415656412173490L;

	/**
	 * 
	 */
	public JobExecutionException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public JobExecutionException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public JobExecutionException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public JobExecutionException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
