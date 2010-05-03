// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProductService.java,v 1.1 2004-11-30 21:13:43 kelly Exp $

package jpl.oodt.product;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Old product service interface.
 *
 * @deprecated Replaced by {@link jpl.eda.product.ProductService}.
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
