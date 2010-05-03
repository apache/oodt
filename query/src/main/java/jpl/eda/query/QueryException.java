// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: QueryException.java,v 1.1.1.1 2004-03-04 18:35:15 kelly Exp $

package jpl.eda.query;

/**
 * Query exception.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public class QueryException extends Exception {
	/**
	 * Creates a new {@link QueryException} instance.
	 *
	 * @param msg Detail message.
	 */
	public QueryException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new {@link QueryException} instance.
	 *
	 * @param cause Chained exception.
	 */
	public QueryException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new {@link QueryException} instance.
	 *
	 * @param msg Detail message.
	 * @param cause Chained exception.
	 */
	public QueryException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
