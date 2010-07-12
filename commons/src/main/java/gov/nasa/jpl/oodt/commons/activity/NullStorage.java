// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: NullStorage.java,v 1.1 2004-03-02 19:28:58 kelly Exp $

package jpl.eda.activity;

import java.util.List;

/**
 * Null storage doesn't store any {@link Incident}s.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
class NullStorage implements Storage {
	/**
	 * Ignore the activity ID and the list of {@link Incident}s.
	 *
	 * @param id Ignored activity ID.
	 * @param incidents Ignored incidents.
	 */
	public void store(String id, List incidents) {}
}
