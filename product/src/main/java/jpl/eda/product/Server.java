// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Server.java,v 1.3 2005-06-22 21:51:40 kelly Exp $

package jpl.eda.product;

import java.rmi.Remote;
import java.rmi.RemoteException;
import jpl.eda.xmlquery.XMLQuery;

/**
 * Server for a single client transaction for products.
 *
 * @author Kelly
 * @version $Revision: 1.3 $
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
	 * @param productID Product ID.
	 * @throws ProductException if an error occurs.
	 * @throws RemoteException if an error occurs.
	 */
	void close(String productID) throws ProductException, RemoteException;
}
