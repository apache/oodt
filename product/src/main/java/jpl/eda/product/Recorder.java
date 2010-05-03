// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Recorder.java,v 1.1 2004-04-26 17:58:44 kelly Exp $

package jpl.eda.product;

/**
 * Recorder of statistics.
 *
 * A recorder is a class implementing this interface and, in addition, providing a one
 * argument constructor that takes a {@link javax.servlet.ServletConfig} object and may
 * throw a {@link javax.servlet.ServletException}.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public interface Recorder {
	/**
	 * Record the given transaction.
	 *
	 * @param transaction a <code>Transaction</code> value.
	 * @throws StatsException if an error occurs.
	 */
	void record(Transaction transaction) throws StatsException;
}
