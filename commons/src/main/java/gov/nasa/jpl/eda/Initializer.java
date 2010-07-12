// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Initializer.java,v 1.1.1.1 2004-02-28 13:09:13 kelly Exp $

package jpl.eda;

/**
 * Initializer for an enterprise application.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public interface Initializer {
	/**
	 * Initialize the appliction.
	 *
	 * @throws EDAException if an error occurs.
	 */
	void initialize() throws EDAException;
}
