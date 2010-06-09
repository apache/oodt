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
