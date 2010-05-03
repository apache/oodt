// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ServerImpl.java,v 1.1.1.1 2004-03-02 19:45:41 kelly Exp $

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
