// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Utility.java,v 1.1.1.1 2004/03/02 20:53:33 kelly Exp $

package jpl.eda.profile.rmi;

/**
 * Utility methods for the RMI profile service.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
class Utility {
	/**
	 * Do not call.
	 *
	 * This class has the &lt;&lt;utility&gt;&gt; stereotype.
	 */
	private Utility() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Get the RMI port to which to bind.
	 *
	 * @return Port number.
	 */
	public static int getRMIPort() {
		int port = Integer.getInteger("jpl.eda.profile.port", Integer.getInteger("jpl.eda.profile.rmi.port",
			Integer.getInteger("jpl.eda.profile.Utility.port", 0))).intValue();
		return port;
	}
}
