// Copyright 1999-2004 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id: AuthorizedProductServiceImpl.java,v 1.1 2004-08-13 21:33:55 kelly Exp $

package jpl.eda.product.rmi;

import java.rmi.RemoteException;
import java.util.List;
import jpl.eda.ExecServer;
import jpl.eda.security.rmi.SecureServiceImpl;
import jpl.eda.security.rmi.Session;

/**
 * Product service that requires authorization.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class AuthorizedProductServiceImpl extends SecureServiceImpl {
	/**
	 * Creates a new {@link AuthorizedProductServiceImpl} instance.
	 *
	 * @throws RemoteException if an error occurs.
	 */
	public AuthorizedProductServiceImpl() throws RemoteException {
		this(/*execServer*/null);
	}

	/**
	 * Creates a new {@link AuthorizedProductServiceImpl} instance.
	 *
	 * @param server Server executive.
	 * @throws RemoteException if an error occurs.
	 */
	public AuthorizedProductServiceImpl(ExecServer server) throws RemoteException {
		super(System.getProperty("login.context.name", "oodt"),
			jpl.eda.product.Utility.getRMIPort("AuthorizedProductServiceImpl"));
		this.server = server;
		name = server != null? server.getName() : getClass().getName();
		handlers = jpl.eda.product.Utility.loadHandlers(name);
	}

	/**
	 * Create a session for an authorized subject.
	 *
	 * @return a {@link Session} value.
	 * @throws RemoteException if an error occurs.
	 */
	protected Session createSessionServer() throws RemoteException {
		Integer portNum = Integer.getInteger(name + ".port", Integer.getInteger("port", 7576));
		AuthorizedSessionImpl session = new AuthorizedSessionImpl(portNum.intValue(), handlers);
		return session;
	}

	/** Server executive. */
	private ExecServer server;

	/** List of query handlers. */
	private List handlers;

	/** Object name. */
	private String name;
}
