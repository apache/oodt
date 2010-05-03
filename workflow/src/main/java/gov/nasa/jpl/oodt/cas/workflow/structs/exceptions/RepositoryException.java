//Copyright (c) 2005, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.structs.exceptions;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>An exception thrown by the workflow repository.</p>
 *
 */
public class RepositoryException extends Exception {

    /* serial version UID */
	private static final long serialVersionUID = 7867039774935834472L;

	/**
	 * 
	 */
	public RepositoryException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public RepositoryException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public RepositoryException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public RepositoryException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
