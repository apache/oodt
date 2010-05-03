// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ServerImpl.java,v 1.1.1.1 2004-03-04 18:35:16 kelly Exp $

package jpl.eda.query.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import jpl.eda.query.QueryEngine;
import jpl.eda.query.QueryException;
import jpl.eda.xmlquery.XMLQuery;

/**
 * RMI-based implementation of a query server instance.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
class ServerImpl extends UnicastRemoteObject implements jpl.eda.query.rmi.Server {
	/**
	 * Creates a new {@link ServerImpl} instance.
	 *
	 * @throws RemoteException if an error occurs.
	 */
	public ServerImpl() throws RemoteException {
		super();
		qe = new QueryEngine();
	}

	public List queryProfileServers(XMLQuery query, List servers) throws QueryException {
		xmlQuery = query;
		return qe.queryProfileServers(query, servers);
	}

	public List queryDefaultProfileServers(XMLQuery query) throws QueryException {
		return qe.queryDefaultProfileServers(query);
	}

	public XMLQuery queryProductServer(XMLQuery query, String serverID) throws QueryException {
		return qe.queryProductServer(query, serverID);
	}

	/** Get query */   
        public XMLQuery getQuery() {
                return xmlQuery;
        }

	public byte[] retrieveChunk(String productID, long offset, int length, String serverID) throws QueryException {
		return qe.retrieveChunk(productID, offset, length, serverID);
	}

	public void close(String productID, String serverID) throws QueryException {
		qe.close(productID, serverID);
	}

	/** Heavy lifter. */
	private QueryEngine qe;

	/** query */
        XMLQuery xmlQuery;
}

