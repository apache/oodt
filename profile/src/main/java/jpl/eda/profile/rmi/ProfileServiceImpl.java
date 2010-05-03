// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProfileServiceImpl.java,v 1.1.1.1 2004/03/02 20:53:32 kelly Exp $

package jpl.eda.profile.rmi;

import java.util.List;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import jpl.eda.ExecServer;
import jpl.eda.Service;
import jpl.eda.profile.ProfileException;
import jpl.eda.profile.handlers.ProfileHandler;

/**
 * RMI implementation of profile service.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public class ProfileServiceImpl extends UnicastRemoteObject implements 
		jpl.eda.profile.ProfileService, Service {
	/**
	 * Creates a new <code>ProfileServiceImpl</code> instance.
	 *
	 * @param server Server executive.
	 * @throws RemoteException if an error occurs.
	 * @throws ProfileException If the profile handler can't be loaded.
	 */
	public ProfileServiceImpl(ExecServer server) throws RemoteException, ProfileException {
		super(Utility.getRMIPort());
		this.server = server;
		handlers = jpl.eda.profile.handlers.Utility.loadHandlers();
	}

	public String getServerInterfaceName() {
		return "jpl.eda.profile.ProfileService";
	}

	public jpl.eda.profile.Server createServer() throws RemoteException {
		return new ServerImpl(handlers);
	}

	public byte[] control(byte[] command) {
		return server.control(command);
	}

	public void controlAsync(byte[] command) {
		server.control(command);
	}

	/** Profile handlers. */
	private List handlers;

	/** Server executive. */
	private ExecServer server;

}
