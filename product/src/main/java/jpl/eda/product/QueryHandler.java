// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: QueryHandler.java,v 1.1.1.1 2004-03-02 19:45:40 kelly Exp $

package jpl.eda.product;

import jpl.eda.xmlquery.XMLQuery;

/**
 * Handler for queries in a product service.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public interface QueryHandler {
	/**
	 * Run a query.
	 *
	 * @param q The query.
	 * @return The response.
	 * @throws ProductException if an error occurs.
	 */
	XMLQuery query(XMLQuery q) throws ProductException;
}
