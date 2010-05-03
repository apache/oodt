// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProductService.java,v 1.1.1.1 2004-03-02 19:45:40 kelly Exp $

package jpl.eda.product;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Product service.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public interface ProductService extends Remote {
	/**
	 * Create a server to service product queries.
	 *
	 * @return A server.
	 * @throws RemoteException if an error occurs.
	 */
	Server createServer() throws RemoteException;
}
