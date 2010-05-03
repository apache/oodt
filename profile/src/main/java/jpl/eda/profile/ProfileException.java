// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProfileException.java,v 1.1.1.1 2004/03/02 20:53:16 kelly Exp $

package jpl.eda.profile;

/**
 * A profile-related exception.
 *
 * @author Kelly
 */
public class ProfileException extends Exception {
	/**
	 * Create a profile exception with no detail message.
	 */
	public ProfileException() {}

	/**
	 * Create a profile exception with the given detail message.
	 */
	public ProfileException(String message) {
		super(message);
	}

	/**
	 * Create a chained profile exception.
	 *
	 * @param cause Causing exception.
	 */
	public ProfileException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a chained profile exception with detail message.
	 *
	 * @param msg Detail message.
	 * @param cause Causing exception.
	 */
	public ProfileException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
