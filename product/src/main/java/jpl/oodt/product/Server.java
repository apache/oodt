// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Server.java,v 1.1 2004-11-30 21:14:20 kelly Exp $

package jpl.oodt.product;

import java.rmi.Remote;
import java.rmi.RemoteException;
import jpl.oodt.xmlquery.XMLQuery;

/**
 * Server for a single client transaction for products.
 *
 * @deprecated Replaced by {@link jpl.eda.product.Server}.
 */
public interface Server extends Remote {
	/**
	 * Run a query.
	 *
	 * @param q The query.
	 * @return The response.
	 * @throws ProductException if an error occurs.
	 * @throws RemoteException if an error occurs.
	 */
	XMLQuery query(XMLQuery q) throws ProductException, RemoteException;

	/**
	 * Retrieve a chunk of a large product. 
	 *
	 * @param productID Product ID.
	 * @param offset Where in the product to get the data.
	 * @param length How much data to get.
	 * @return The data.
	 * @throws ProductException if an error occurs.
	 * @throws RemoteException if an error occurs.
	 */
	byte[] retrieveChunk(String productID, long offset, int length) throws ProductException, RemoteException;

	/**
	 * Close off a large product.
	 *
	 * @param Product ID.
	 * @throws ProductException if an error occurs.
	 * @throws RemoteException if an error occurs.
	 */
	void close(String productID) throws ProductException, RemoteException;
}











