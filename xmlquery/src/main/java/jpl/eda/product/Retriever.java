// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Retriever.java,v 1.1.1.1 2004-03-02 19:37:14 kelly Exp $

package jpl.eda.product;

/**
 * Retrievers retrieve products.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public interface Retriever {
	/**
	 * Retrieve a chunk from a large product.
	 *
	 * @param productID Product ID.
	 * @param offset Where in the product to retrieve the data.
	 * @param length How much data to get.
	 * @return The data.
	 * @throws ProductException if an error occurs.
	 */
	byte[] retrieveChunk(String productID, long offset, int length) throws ProductException;

	/**
	 * Close off a large product.
	 *
	 * @param productID Product ID.
	 * @throws ProductException if an error occurs.
	 */
	void close(String productID) throws ProductException;
}
