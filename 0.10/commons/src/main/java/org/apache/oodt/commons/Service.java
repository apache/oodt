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

package org.apache.oodt.commons;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An enterprise service.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public interface Service extends Remote {
	/**
	 * Get the interface name of the service.
	 *
	 * Nominally, this should return the fully qualified class name of the
	 * implementation class, not the interface name.  No idea how that "standard" got
	 * started.  So, a good return value might be
	 * <code>jpl.oodt.product.rmi.ProductServiceImpl</code> and <strong>not</strong>
	 * <code>jpl.oodt.product.ProductService</code>.
	 *
	 * @return a <code>String</code> value.
	 * @throws RemoteException if an error occurs.
	 */
	String getServerInterfaceName() throws RemoteException;

	/**
	 * Control the server.
	 *
	 * @param command a <code>byte[]</code> value.
	 * @return Response.
	 * @throws RemoteException if an error occurs.
	 */
	byte[] control(byte[] command) throws RemoteException;

	
	/**
	 * Control the server asynchronously.
	 *
	 * @param command a <code>byte[]</code> value.
	 * @throws RemoteException if an error occurs.
	 */
	void controlAsync(byte[] command) throws RemoteException;
}
