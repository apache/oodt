// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: QueryService.java,v 1.1.1.1 2004-03-04 18:35:16 kelly Exp $

package jpl.eda.query.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface definition for the RMI-based query service.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public interface QueryService extends Remote {
	/**
	 * Create a server instance.
	 *
	 * @return a {@link Server} value.
	 * @throws RemoteException if an error occurs.
	 */
	Server createServer() throws RemoteException;
}
