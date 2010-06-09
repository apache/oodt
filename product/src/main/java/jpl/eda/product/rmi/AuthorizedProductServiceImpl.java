/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
