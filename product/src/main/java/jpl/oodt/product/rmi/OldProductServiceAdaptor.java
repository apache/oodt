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


package jpl.oodt.product.rmi;

import java.rmi.RemoteException;
import jpl.eda.xmlquery.XMLQuery;
import jpl.eda.product.ProductException;

/**
 * Adapt an old product service into one compatible with the new interface.
 *
 * @deprecated With no replacement; new installations should use the new product service in <code>jpl.eda.product</code>.
 */
public final class OldProductServiceAdaptor implements jpl.eda.product.ProductService {
	/**
	 * Creates a new <code>OldProductServiceAdaptor</code> instance.
	 *
	 * @param old The old OODT-based product service to adapt.
	 */
	public OldProductServiceAdaptor(jpl.oodt.product.ProductService old) {
		this.old = old;
	}

	public jpl.eda.product.Server createServer() throws RemoteException {
		return new ServerAdaptor(old.createServer());
	}

	/**
	 * Adapter for the product server instance.
	 */
	private static class ServerAdaptor implements jpl.eda.product.Server {
		/**
		 * Creates a new <code>ServerAdaptor</code> instance.
		 *
		 * @param oldServer Old server server to adapt.
		 */
		public ServerAdaptor(jpl.oodt.product.Server oldServer) {
			this.oldServer = oldServer;
		}
		public XMLQuery query(XMLQuery q) throws ProductException, RemoteException {
			jpl.oodt.xmlquery.XMLQuery oldQuery = new jpl.oodt.xmlquery.XMLQuery(q);
			oldQuery = oldServer.query(oldQuery);
			q.getResults().addAll(oldQuery.getResults());
			return q;
		}
		public byte[] retrieveChunk(String id, long offset, int length) throws ProductException, RemoteException {
			return oldServer.retrieveChunk(id, offset, length);
		}
		public void close(String id) throws ProductException, RemoteException {
			oldServer.close(id);
		}

		/** Old instance server we're adapting. */
		private jpl.oodt.product.Server oldServer;
	}

	/** Old product service we're adapting. */
	private jpl.oodt.product.ProductService old;
}
