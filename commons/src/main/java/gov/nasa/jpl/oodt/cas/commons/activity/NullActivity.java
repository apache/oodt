// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: NullActivity.java,v 1.1 2004-03-02 19:28:58 kelly Exp $

package jpl.eda.activity;

/**
 * A null activity doesn't record any incidents.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class NullActivity extends Activity {
	/**
	 * Ignore the incident.
	 *
	 * @param incident Ignored.
	 */
	public void recordIncident(Incident incident) {}
}
