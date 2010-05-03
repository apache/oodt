// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: QueryServiceImpl.java,v 1.1.1.1 2004-03-04 18:35:16 kelly Exp $

package jpl.eda.query.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import jpl.eda.ExecServer;
import jpl.eda.Service;

/**
 * RMI implementation of the query service.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public class QueryServiceImpl extends UnicastRemoteObject implements jpl.eda.query.rmi.QueryService, Service {
	/**
	 * Creates a new <code>QueryServiceImpl</code> instance.
	 *
	 * @param server Server executive.
	 * @throws RemoteException if an error occurs.
	 */
	public QueryServiceImpl(ExecServer server) throws RemoteException {
		super(getRMIPort());
		this.server = server;
	}

	public Server createServer() throws RemoteException {
		return new ServerImpl();
	}

	public String getServerInterfaceName() {
		return "jpl.eda.query.rmi.QueryService";
	}

	public byte[] control(byte[] command) {
		return server.control(command);
	}

	public void controlAsync(byte[] command) {
		server.control(command);
	}

	/**
	 * Get the port the query service should use.
	 *
	 * The port number is specified by the <code>jpl.eda.query.port</code> value, or
	 * the <code>jpl.eda.query.rmi.QueryServiceImpl.port</code> value if not
	 * specified, or is zero (meaning use a system-assigned port).
	 *
	 * @return Port number
	 */
	private static int getRMIPort() {
		int port = Integer.getInteger("jpl.eda.query.port",
			Integer.getInteger("jpl.eda.query.rmi.QueryServiceImpl.port", 0)).intValue();
		System.err.println("Using RMI port " + port + " for query service");
		return port;
	}

	/** Server executive. */
	private ExecServer server;
}
