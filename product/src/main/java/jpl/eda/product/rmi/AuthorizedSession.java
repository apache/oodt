// Copyright 1999-2004 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id: AuthorizedSession.java,v 1.2 2005-02-24 21:34:56 kelly Exp $

package jpl.eda.product.rmi;

import java.rmi.RemoteException;
import jpl.eda.product.ProductException;
import jpl.eda.security.rmi.Session;
import jpl.eda.xmlquery.XMLQuery;

/**
 * An authorized session with a product server.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public interface AuthorizedSession extends Session {
	/**
	 * Run a query.
	 *
	 * @param seq Secure sequence number.
	 * @param q The query.
	 * @return The response.
	 * @throws ProductException if an error occurs.
	 * @throws RemoteException if an error occurs.
	 */
	XMLQuery query(int seq, XMLQuery q) throws ProductException, RemoteException;

	/**
	 * Retrieve a chunk of a large product. 
	 *
	 * @param seq Secure sequence number.
	 * @param productID Product ID.
	 * @param offset Where in the product to get the data.
	 * @param length How much data to get.
	 * @return The data.
	 * @throws ProductException if an error occurs.
	 * @throws RemoteException if an error occurs.
	 */
	byte[] retrieveChunk(int seq, String productID, long offset, int length) throws ProductException, RemoteException;

	/**
	 * Close off a large product.
	 *
	 * @param seq Secure sequence number.
	 * @param productID Product ID.
	 * @throws ProductException if an error occurs.
	 * @throws RemoteException if an error occurs.
	 */
	void close(int seq, String productID) throws ProductException, RemoteException;
}
