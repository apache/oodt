// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ActivityFactory.java,v 1.1 2004-03-02 19:28:56 kelly Exp $

package jpl.eda.activity;

/**
 * Factory for activities.  Classes that implement this interface are responsible for
 * generating application-specific Activities on demand.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public interface ActivityFactory {
	/**
	 * Create an activity.
	 *
	 * @return an new {@link Activity} instance.
	 */
	Activity createActivity();
}
