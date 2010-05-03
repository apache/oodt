//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.structs.exceptions;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>An exception thrown by the {@link WorkflowInstanceRepository}</p>.
 */
public class InstanceRepositoryException extends Exception {

	/* serial version UID */
	private static final long serialVersionUID = -4956612049874952669L;

	/**
	 * 
	 */
	public InstanceRepositoryException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public InstanceRepositoryException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public InstanceRepositoryException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public InstanceRepositoryException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}
