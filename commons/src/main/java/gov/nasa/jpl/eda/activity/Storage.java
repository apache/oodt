// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Storage.java,v 1.2 2005-08-03 17:19:44 kelly Exp $

package jpl.eda.activity;

import java.util.List;

/**
 * A Storage is a place to store for the long term record an activity and the incidents
 * that define it.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public interface Storage {
	/**
	 * Store the activity.
	 *
	 * @param id Activity ID.
	 * @param incidents a {@link List} of {@link Incident}s that defined it.
	 */
	void store(String id, List incidents);
}
