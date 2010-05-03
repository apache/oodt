// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: StatsException.java,v 1.1 2004-04-26 17:58:44 kelly Exp $

package jpl.eda.product;

/**
 * Statistics exception.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class StatsException extends Exception {
	/**
	 * Creates a new <code>StatsException</code> instance.
	 *
	 * @param msg Detail message
	 */
	public StatsException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new <code>StatsException</code> instance.
	 *
	 * @param cause Chained exception.
	 */
	public StatsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new <code>StatsException</code> instance.
	 *
	 * @param msg Detail message
	 * @param cause Chained exception
	 */
	public StatsException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
