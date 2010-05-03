// Copyright 1999-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ServerImpl.java,v 1.1.1.1 2004-03-02 19:45:41 kelly Exp $

package jpl.eda.product.corba;

import java.util.*;
import java.util.Iterator;
import java.util.Timer;
import jpl.eda.util.*;
import jpl.eda.product.LargeProductQueryHandler;
import jpl.eda.product.ProductException;
import jpl.eda.product.QueryHandler;
import jpl.eda.product.corba.ServerPackage.*;
import jpl.eda.util.*;
import jpl.eda.xmlquery.*;
import org.omg.CORBA.*;
import org.xml.sax.*;

/**
 * CORBA implementation of product instance server.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
class ServerImpl extends ServerPOA {
	public ServerImpl(List handlers) {
		this.handlers = handlers;
		timeoutTask = new TimeoutTask();
		TIMER.schedule(timeoutTask, INSTANCE_EXPIRY);
	}

	public byte[] query(byte[] queryStringBytes) throws ProductServiceException {
		long time0 = System.currentTimeMillis();
		System.err.println(toString() + ": received query at " + (new Date(time0)));
		XMLQuery productQuery = null;
		try {
			productQuery = new XMLQuery(new String(queryStringBytes));
			System.err.println("Acceptable mime types: " + productQuery.getMimeAccept());

			// This should be multi-threaded, but ...
			// Call each query handler and return the results
			for (Iterator i = handlers.iterator(); i.hasNext();) {
				QueryHandler qhandler = (QueryHandler) i.next();
				qhandler.query(productQuery);
				// Sean says: let's rethink here ... our existing product servers add their
				// results to the XMLQuery object, but maybe they should just return the
				// results and leave it to the code *here* to decide whether to add it.
				// This would let us not add results beyond the maximum, for example, or
				// filter duplicates, that sort of thing.
			}
			return productQuery.getXMLDocString().getBytes();
		} catch (SAXException ex) {
			ProductServiceException exception = new ProductServiceException();
			exception.errorInfo = "Can't parse XML product query: " + ex.getMessage();
			throw exception;
		} catch (ProductException ex) {
			ProductServiceException exception = new ProductServiceException();
			exception.errorInfo = ex.getMessage();
			throw exception;
		} finally {
			long time = System.currentTimeMillis();
			System.err.println(toString() + ": query complete after " + (time - time0) + " ms");
		}
	}

	public byte[] retrieveChunk(String id, long offset, int length) throws ProductServiceException {
		try {
			for (Iterator i = handlers.iterator(); i.hasNext();) {
				java.lang.Object qh = i.next();
				if (qh instanceof LargeProductQueryHandler) {
					LargeProductQueryHandler lpqh = (LargeProductQueryHandler) qh;
					byte[] chunk = lpqh.retrieveChunk(id, offset, length);
					if (chunk != null) return chunk;
				}
			}
		} catch (ProductException ex) {
			ProductServiceException pse = new ProductServiceException();
			System.err.println(ex.getMessage());

			pse.errorInfo = ex.getMessage();
			throw pse;
		}
		return null;
	}

	public void close(String id) throws ProductServiceException {
		try {
			for (Iterator i = handlers.iterator(); i.hasNext();) {
				java.lang.Object qh = i.next();
				if (qh instanceof LargeProductQueryHandler) {
					LargeProductQueryHandler lpqh = (LargeProductQueryHandler) qh;
					lpqh.close(id);
				}
			}
		} catch (ProductException ex) {
			ProductServiceException pse = new ProductServiceException();
			pse.errorInfo = ex.getMessage();
			throw pse;
		}
	}

	public synchronized void destroy() {
		if (timeoutTask != null) {
			timeoutTask.cancel();
			timeoutTask = null;
		}
		try {
			org.omg.PortableServer.POA poa = _default_POA();
			byte[] id = poa.servant_to_id(this);
			poa.deactivate_object(id);
		} catch (org.omg.PortableServer.POAPackage.ServantNotActive ex) {
			throw new IllegalStateException("Servant not active");
		} catch (org.omg.PortableServer.POAPackage.WrongPolicy ex) {
			throw new IllegalStateException("Wrong policy");
		} catch (org.omg.PortableServer.POAPackage.ObjectNotActive ex) {
			// ignore
		}
	}

	/** Query handlers. */
	private List handlers;

	/** Current task to expire this instance. */
	private TimeoutTask timeoutTask;

	/** Master timer shared by all instances. */
	private static final Timer TIMER = new Timer();

	/** When to expire an instance server. */
	public static final long INSTANCE_EXPIRY = Long.getLong("jpl.eda.product.corba.instanceExpiry",5*60*1000).longValue();

	/** Task to expire an instance. */
	private class TimeoutTask extends TimerTask {
		public void run() {
			try {
				destroy();
			} catch (IllegalStateException ignore) {}
		}
	}
}

