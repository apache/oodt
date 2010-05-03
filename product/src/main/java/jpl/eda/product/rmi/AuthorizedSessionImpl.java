// Copyright 1999-2004 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id: AuthorizedSessionImpl.java,v 1.3 2004-08-17 14:29:39 kelly Exp $

package jpl.eda.product.rmi;

import java.lang.SecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import java.util.List;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import jpl.eda.product.LargeProductQueryHandler;
import jpl.eda.product.ProductException;
import jpl.eda.product.ProductPermission;
import jpl.eda.product.QueryHandler;
import jpl.eda.security.rmi.SequenceGenerator;
import jpl.eda.xmlquery.XMLQuery;

/**
 * Product server session for an authorized subject.
 *
 * @author Kelly
 * @version $Revision: 1.3 $
 */
public class AuthorizedSessionImpl extends UnicastRemoteObject implements AuthorizedSession {
	/**
	 * Creates a new {@link AuthorizedSessionImpl} instance.
	 *
	 * @param portNum Port number object should bind to.
	 * @param handlers Query handlers.
	 * @throws RemoteException if an error occurs.
	 */
	public AuthorizedSessionImpl(int portNum, List handlers) throws RemoteException {
		super(portNum);
		this.handlers = handlers;
	}

	/** {@inheritDoc} */
	public synchronized void init(SequenceGenerator sequenceGenerator, LoginContext context) {
		if (initialized) throw new IllegalStateException("Already initialized");
		this.sequenceGenerator = sequenceGenerator;
		this.context = context;
		subject = context.getSubject();
		initialized = true;
	}

	/** {@inheritDoc} */
	public XMLQuery query(int seq, final XMLQuery query) throws ProductException {
		checkLogin(seq);
		try {
			return (XMLQuery) Subject.doAsPrivileged(subject, new PrivilegedExceptionAction() {
				public Object run() throws ProductException {
					return query(query);
				}
			}, /*acc*/null);
		} catch (PrivilegedActionException ex) {
			throw (ProductException) ex.getException();
		}
	}


	/**
	 * Query the query handlers.
	 *
	 * @param q a {@link XMLQuery} value.
	 * @return a {@link XMLQuery} value.
	 * @throws ProductException if an error occurs.
	 */
	private XMLQuery query(XMLQuery q) throws ProductException {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) sm.checkPermission(new ProductPermission("query"));
		for (Iterator i = handlers.iterator(); i.hasNext();) {
			QueryHandler handler = (QueryHandler) i.next();
			handler.query(q);
		}
		return q;
	}

	/** {@inheritDoc} */
	public byte[] retrieveChunk(int seq, final String productID, final long offset, final int length) throws ProductException {
		checkLogin(seq);
		try {
			return (byte[]) Subject.doAsPrivileged(subject, new PrivilegedExceptionAction() {
				public Object run() throws ProductException {
					return retrieveChunk(productID, offset, length);
				}
			}, /*acc*/null);
		} catch (PrivilegedActionException ex) {
			throw (ProductException) ex.getException();
		}
	}

	/**
	 * Retrieve a chunk.
	 *
	 * @param productID a {@link String} value.
	 * @param offset Where in the product
	 * @param length How much of the product
	 * @return a chunk
	 * @throws ProductException if an error occurs.
	 */
	private byte[] retrieveChunk(final String productID, final long offset, final int length) throws ProductException {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) sm.checkPermission(new ProductPermission("retrieveChunk"));
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

	/** {@inheritDoc} */
	public void close(int seq, final String productID) throws ProductException {
		checkLogin(seq);
		try {
			Subject.doAsPrivileged(subject, new PrivilegedExceptionAction() {
				public Object run() throws ProductException {
					close(productID);
					return null;
				}
			}, /*acc*/null);
		} catch (PrivilegedActionException ex) {
			throw (ProductException) ex.getException();
		}
	}

	/**
	 * Close a chunked product.
	 *
	 * @param productID a {@link String} value.
	 * @throws ProductException if an error occurs.
	 */
	private void close(String productID) throws ProductException {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) sm.checkPermission(new ProductPermission("close"));
		for (Iterator i = handlers.iterator(); i.hasNext();) {
			Object qh = i.next();
			if (qh instanceof LargeProductQueryHandler) {
				LargeProductQueryHandler lpqh = (LargeProductQueryHandler) qh;
				lpqh.close(productID);
			}
		}
	}

	/**
	 * Check if the user is logged in and the sequence number is valid.
	 *
	 * @param seq Sequence number.
	 * @throws ProductException if an error occurs.
	 */
	private void checkLogin(int seq) throws ProductException {
		if (context == null) throw new ProductException("Logged out");
		if (seq == sequenceGenerator.nextSeqNum()) return;
		try {
			context.logout();
		} catch (LoginException ignore) {}
		context = null;
		throw new ProductException("Invalid sequence");
	}

	/** Query handlers. */
	private List handlers;

	/** Login context. */
	private LoginContext context;

	/** Sequence generator. */
	private SequenceGenerator sequenceGenerator;

	/** Subject for whom this session exists. */
	private Subject subject;

	/** Initialized yet? */
	private boolean initialized;
}
