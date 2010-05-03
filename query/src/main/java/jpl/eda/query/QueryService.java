// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: QueryService.java,v 1.1.1.1 2004-03-04 18:35:15 kelly Exp $

package jpl.eda.query;

import java.util.List;
import jpl.eda.profile.Profile;
import jpl.eda.xmlquery.XMLQuery;

/**
 * A query service.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public interface QueryService {
	/**
	 * A server instance.
	 */
	public interface Server {
		/**
		 * Query profile servers.
		 *
		 * This method gathers up matching profiles from each of the given profile
		 * servers.  For any matching profile that describes another profile
		 * server, this method follows the link and runs the query there, too,
		 * following additional links as they occur.
		 *
		 * @param query a {@link XMLQuery} value.
		 * @param servers a {@link List} value of {@link String} server IDs.
		 * @return a {@link List} of matching {@link Profile}s.
		 * @throws QueryException if an error occurs.
		 */
		List queryProfileServers(XMLQuery query, List servers) throws QueryException;

		/**
		 * Query the default profile servers.
		 *
		 * This method gathers up matching profiles from each of (server-defined)
		 * default profile servers.  For any matching profile that describes
		 * another profile server, this method follows the link and runs the query
		 * there, too, following additional links as they occur.
		 *
		 * @param query a {@link XMLQuery} value.
		 * @return a {@link List} of matching {@link Profile}s.
		 * @throws QueryException if an error occurs.
		 */
		List queryDefaultProfileServers(XMLQuery query) throws QueryException;

		/**
		 * Query a product server.
		 *
		 * @param query a {@link XMLQuery} value.
		 * @param serverID What server to query.
		 * @return a {@link XMLQuery} value.
		 * @throws QueryException if an error occurs.
		 */
		XMLQuery queryProductServer(XMLQuery query, String serverID) throws QueryException;

		/**
                 * Get the query.
                 *
                 * @return a {@link XMLQuery} value.
		 * @throws QueryException if an error occurs.
                 */
		XMLQuery getQuery()throws QueryException;

		/**
		 * Retrieve a chunk of an open, large product.
		 *
		 * @param productID What product to get.
		 * @param offset Where in the product to get the chunk.
		 * @param length How big a chunk to get.
		 * @param serverID From what server to get it.
		 * @return the chunk.
		 * @throws QueryException if an error occurs.
		 */
		byte[] retrieveChunk(String productID, long offset, int length, String serverID) throws QueryException;

		/**
		 * Close an open, large product.
		 *
		 * @param productID What product to close.
		 * @param serverID Where to close it.
		 * @throws QueryException if an error occurs.
		 */
		void close(String productID, String serverID) throws QueryException;
	}

	/**
	 * Factory for query services.
	 */
	public interface Factory {
		/**
		 * Create a new query service.
		 *
		 * @return a {@link QueryService} value.
		 * @throws QueryException if an error occurs.
		 */
		QueryService createQueryServce() throws QueryException;
	}

	/**
	 * Create a new server instance.
	 *
	 * @return a {@link Server} value.
	 * @throws QueryException if an error occurs.
	 */
	Server createServer() throws QueryException;
}
