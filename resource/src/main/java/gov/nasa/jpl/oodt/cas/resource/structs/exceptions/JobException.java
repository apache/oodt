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
 * The base class for Job Exceptions.
 * </p>
 */
public class JobException extends Exception {

	/* serial version UID */
	private static final long serialVersionUID = 4802353574545230182L;

	/**
	 * 
	 */
	public JobException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public JobException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public JobException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public JobException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
