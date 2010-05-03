// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProfileService.java,v 1.1.1.1 2004/03/02 20:53:16 kelly Exp $

package jpl.eda.profile;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Profile service.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public interface ProfileService extends Remote {
	/**
	 * Create a server to service profile queries and management.
	 *
	 * @return A server.
	 * @throws RemoteException if an error occurs.
	 */
	Server createServer() throws RemoteException;
}
