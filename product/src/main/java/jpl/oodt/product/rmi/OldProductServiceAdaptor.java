// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: OldProductServiceAdaptor.java,v 1.1 2004-11-30 21:18:20 kelly Exp $

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
