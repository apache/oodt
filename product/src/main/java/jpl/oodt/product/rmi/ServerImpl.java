// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ServerImpl.java,v 1.1 2004-11-30 21:19:39 kelly Exp $

package jpl.oodt.product.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * RMI implementation of product server instance.
 *
 * @deprecated With no replacement; new installations should use the new product service in <code>jpl.eda.product</code>.
 */
abstract class ServerImpl extends UnicastRemoteObject implements jpl.oodt.product.Server {
	public ServerImpl() throws RemoteException {}
}
