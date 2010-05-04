// Copyright 1999-2001 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: QueryException.java,v 1.1.1.1 2004-03-02 19:37:15 kelly Exp $

package jpl.eda.xmlquery;

/** Checked exception to indicate a query fault.
 *
 * @author Kelly
 */
public class QueryException extends Exception {
	/** Construct a query exception with no detail message.
	 */
	public QueryException() {}

	/** Construct a query exception with the given detail message.
	 *
	 * @param msg Detail message.
	 */
	public QueryException(String msg) {
		super(msg);
	}
}
