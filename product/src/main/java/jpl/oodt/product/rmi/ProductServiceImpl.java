// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProductServiceImpl.java,v 1.1 2004-11-30 21:19:13 kelly Exp $

package jpl.oodt.product.rmi;

import java.rmi.server.UnicastRemoteObject;
import jpl.eda.Service;
import java.rmi.RemoteException;

/**
 * RMI implementation of product service.
 *
 * @deprecated With no replacement; new installations should use the new product service in <code>jpl.eda.product</code>.
 */
public abstract class ProductServiceImpl extends UnicastRemoteObject implements jpl.oodt.product.ProductService, Service {
	public ProductServiceImpl() throws RemoteException {}
}

