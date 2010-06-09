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
