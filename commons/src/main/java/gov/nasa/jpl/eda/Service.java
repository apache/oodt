// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Service.java,v 1.1 2004-03-01 16:52:06 kelly Exp $

package jpl.eda;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An enterprise service.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public interface Service extends Remote {
	/**
	 * Get the interface name of the service.
	 *
	 * Nominally, this should return the fully qualified class name of the
	 * implementation class, not the interface name.  No idea how that "standard" got
	 * started.  So, a good return value might be
	 * <code>jpl.oodt.product.rmi.ProductServiceImpl</code> and <strong>not</strong>
	 * <code>jpl.oodt.product.ProductService</code>.
	 *
	 * @return a <code>String</code> value.
	 * @throws RemoteException if an error occurs.
	 */
	String getServerInterfaceName() throws RemoteException;

	/**
	 * Control the server.
	 *
	 * @param command a <code>byte[]</code> value.
	 * @return Response.
	 * @throws RemoteException if an error occurs.
	 */
	byte[] control(byte[] command) throws RemoteException;

	
	/**
	 * Control the server asynchronously.
	 *
	 * @param command a <code>byte[]</code> value.
	 * @throws RemoteException if an error occurs.
	 */
	void controlAsync(byte[] command) throws RemoteException;
}
