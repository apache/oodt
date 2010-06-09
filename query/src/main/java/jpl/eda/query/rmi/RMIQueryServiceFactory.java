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
import java.util.List;
import jpl.eda.query.QueryException;
import jpl.eda.xmlquery.XMLQuery;

/**
 * Factory that creates query service adapted from an RMI-specific query service.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public class RMIQueryServiceFactory implements jpl.eda.query.QueryService.Factory {
	/**
	 * Creates a new {@link RMIQueryServiceFactory} instance.
	 *
	 * @param service RMI-based query service.
	 */
	public RMIQueryServiceFactory(QueryService service) {
		this.service = service;
	}

	public jpl.eda.query.QueryService createQueryServce() {
		return new QueryServiceAdaptor();
	}

	/** RMI-based query service. */
	private QueryService service;

	/**
	 * Adaptor from an RMI-based query service to the generic query service.
	 */
	private class QueryServiceAdaptor implements jpl.eda.query.QueryService {
		public jpl.eda.query.QueryService.Server createServer() throws QueryException {
			try {
				return new ServerAdaptor(service.createServer());
			} catch (RemoteException ex) {
				throw new QueryException(ex);
			}
		}
	}

	/**
	 * Adaptor from an RMI-based server instance to the generic server instance.
	 */
	private static class ServerAdaptor implements jpl.eda.query.QueryService.Server {
		/**
		 * Creates a new {@link ServerAdaptor} instance.
		 *
		 * @param server RMI-based server instance.
		 */
		public ServerAdaptor(Server server) {
			this.server = server;
		}

		public List queryProfileServers(XMLQuery query, List servers) throws QueryException {
			try {
				return server.queryProfileServers(query, servers);
			} catch (RemoteException ex) {
				throw new QueryException(ex);
			}
		}

		public XMLQuery getQuery() throws QueryException {
                        try {
                                return server.getQuery();
                        } catch (RemoteException ex) {
                                throw new QueryException(ex);
                        }
                }

		public List queryDefaultProfileServers(XMLQuery query) throws QueryException {
			try {
				return server.queryDefaultProfileServers(query);
			} catch (RemoteException ex) {
				throw new QueryException(ex);
			}
		}

		public XMLQuery queryProductServer(XMLQuery query, String serverID) throws QueryException {
			try {
				return server.queryProductServer(query, serverID);
			} catch (RemoteException ex) {
				throw new QueryException(ex);
			}
		}

		public byte[] retrieveChunk(String productID, long offset, int length, String serverID) throws QueryException {
			try {
				return server.retrieveChunk(productID, offset, length, serverID);
			} catch (RemoteException ex) {
				throw new QueryException(ex);
			}
		}

		public void close(String productID, String serverID) throws QueryException {
			try {
				server.close(productID, serverID);
			} catch (RemoteException ex) {
				throw new QueryException(ex);
			}
		}

		/** RMI-based server instance. */
		private Server server;
	}
}
