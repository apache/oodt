// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProductServiceImpl.java,v 1.3 2004-08-13 21:28:48 kelly Exp $

package jpl.eda.product.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jpl.eda.ExecServer;
import jpl.eda.Service;

/**
 * RMI implementation of product service.
 *
 * @author Kelly
 * @version $Revision: 1.3 $
 */
public class ProductServiceImpl extends UnicastRemoteObject implements jpl.eda.product.ProductService, Service {
	/**
	 * Creates a new <code>ProductServiceImpl</code> instance.
	 *
	 * @param server Server executive.
	 * @throws RemoteException if an error occurs.
	 */
	public ProductServiceImpl(ExecServer server) throws RemoteException {
		super(jpl.eda.product.Utility.getRMIPort("ProductServiceImpl"));
		this.server = server;
		handlers = jpl.eda.product.Utility.loadHandlers(server.getName());
	}

	public String getServerInterfaceName() {
		return "jpl.eda.product.rmi.ProductService";
	}

	public jpl.eda.product.Server createServer() throws RemoteException {
		return new ServerImpl(handlers);
	}

	public byte[] control(byte[] command) {
		return server.control(command);
	}

	public void controlAsync(byte[] command) {
		server.control(command);
	}

	/** Query handlers. */
	private List handlers;

	/** Server executive. */
	private ExecServer server;
}
