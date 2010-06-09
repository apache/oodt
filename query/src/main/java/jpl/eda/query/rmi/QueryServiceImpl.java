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
