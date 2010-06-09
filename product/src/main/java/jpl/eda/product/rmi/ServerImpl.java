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
import java.util.Iterator;
import java.util.List;
import jpl.eda.product.LargeProductQueryHandler;
import jpl.eda.product.ProductException;
import jpl.eda.product.QueryHandler;
import jpl.eda.xmlquery.XMLQuery;

/**
 * RMI implementation of product server instance.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
class ServerImpl extends UnicastRemoteObject implements jpl.eda.product.Server {
	/**
	 * Creates a new <code>ServerImpl</code> instance.
	 *
	 * @param handlers List of {@link QueryHandler}s.
	 * @throws RemoteException if an error occurs.
	 */
	ServerImpl(List handlers) throws RemoteException {
		super(jpl.eda.product.Utility.getRMIPort("ServerImpl"));
		this.handlers = handlers;
	}

	public XMLQuery query(XMLQuery q) throws ProductException {
		System.err.println("RECEIVED QUERY " + q.getKwdQueryString());
		for (Iterator i = handlers.iterator(); i.hasNext();) {
			QueryHandler handler = (QueryHandler) i.next();
			handler.query(q);
		}
		return q;
	}

	public byte[] retrieveChunk(String productID, long offset, int length) throws ProductException {
		for (Iterator i = handlers.iterator(); i.hasNext();) {
			Object qh = i.next();
			if (qh instanceof LargeProductQueryHandler) {
				LargeProductQueryHandler lpqh = (LargeProductQueryHandler) qh;
				byte[] chunk = lpqh.retrieveChunk(productID, offset, length);
				if (chunk != null) return chunk;
			}
		}
		return null;
	}

	public void close(String productID) throws ProductException {
		for (Iterator i = handlers.iterator(); i.hasNext();) {
			Object qh = i.next();
			if (qh instanceof LargeProductQueryHandler) {
				LargeProductQueryHandler lpqh = (LargeProductQueryHandler) qh;
				lpqh.close(productID);
			}
		}
	}

	/** Query handlers. */
	private List handlers;
}
