// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Server.java,v 1.1.1.1 2004-03-04 18:35:16 kelly Exp $

package jpl.eda.query.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import jpl.eda.query.QueryException;
import jpl.eda.xmlquery.XMLQuery;

/**
 * Interface definition from RMI-based server instance.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public interface Server extends Remote {
	List queryProfileServers(XMLQuery query, List servers) throws QueryException, RemoteException;
	List queryDefaultProfileServers(XMLQuery query) throws QueryException, RemoteException;
	XMLQuery queryProductServer(XMLQuery query, String serverID) throws QueryException, RemoteException;
	XMLQuery getQuery() throws QueryException, RemoteException;
	byte[] retrieveChunk(String productID, long offset, int length, String serverID) throws QueryException, RemoteException;
	void close(String productID, String serverID) throws QueryException, RemoteException;
}
