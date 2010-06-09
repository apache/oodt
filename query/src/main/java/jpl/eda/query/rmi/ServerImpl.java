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
import java.util.List;
import jpl.eda.query.QueryEngine;
import jpl.eda.query.QueryException;
import jpl.eda.xmlquery.XMLQuery;

/**
 * RMI-based implementation of a query server instance.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
class ServerImpl extends UnicastRemoteObject implements jpl.eda.query.rmi.Server {
	/**
	 * Creates a new {@link ServerImpl} instance.
	 *
	 * @throws RemoteException if an error occurs.
	 */
	public ServerImpl() throws RemoteException {
		super();
		qe = new QueryEngine();
	}

	public List queryProfileServers(XMLQuery query, List servers) throws QueryException {
		xmlQuery = query;
		return qe.queryProfileServers(query, servers);
	}

	public List queryDefaultProfileServers(XMLQuery query) throws QueryException {
		return qe.queryDefaultProfileServers(query);
	}

	public XMLQuery queryProductServer(XMLQuery query, String serverID) throws QueryException {
		return qe.queryProductServer(query, serverID);
	}

	/** Get query */   
        public XMLQuery getQuery() {
                return xmlQuery;
        }

	public byte[] retrieveChunk(String productID, long offset, int length, String serverID) throws QueryException {
		return qe.retrieveChunk(productID, offset, length, serverID);
	}

	public void close(String productID, String serverID) throws QueryException {
		qe.close(productID, serverID);
	}

	/** Heavy lifter. */
	private QueryEngine qe;

	/** query */
        XMLQuery xmlQuery;
}

