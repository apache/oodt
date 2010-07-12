// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: CompositeActivity.java,v 1.1 2004-03-02 19:28:57 kelly Exp $

package jpl.eda.activity;

import java.util.Collection;
import java.util.Iterator;

/**
 * A composite activity multiplexes incidents to multiple other activities.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class CompositeActivity extends Activity {
	/**
	 * Creates a new {@link CompositeActivity} instance.
	 *
	 * @param activities a {@link Collection} of {@link Activity} instances.
	 */
	public CompositeActivity(Collection activities) {
		if (activities == null)
			throw new IllegalArgumentException("Activities collection required");
		for (Iterator i = activities.iterator(); i.hasNext();)
			if (!(i.next() instanceof Activity))
				throw new IllegalArgumentException("Non-Activity in activities collection");
		this.activities = activities;
	}

	/**
	 * Record the given incident in each of our delegate activities.
	 *
	 * @param incident The {@link Incident} to record.
	 */
	public void recordIncident(Incident incident) {
		for (Iterator i = activities.iterator(); i.hasNext();)
			((Activity) i.next()).recordIncident(incident);
	}

	/**
	 * A collection of {@link Activity} instances.
	 */
	private Collection activities;
}
