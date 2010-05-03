// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: CORBAQueryServiceFactory.java,v 1.1.1.1 2004-03-04 18:35:16 kelly Exp $

package jpl.eda.query.corba;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import jpl.eda.query.QueryException;
import jpl.eda.query.corba.ServerPackage.QueryServiceException;
import jpl.eda.xmlquery.XMLQuery;
import org.xml.sax.SAXException;

/**
 * Factory for CORBA-based query service.
 *
 * This class adapts a CORBA-specific query service into the protocol-neutral query
 * service interface.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public class CORBAQueryServiceFactory implements jpl.eda.query.QueryService.Factory {
	/**
	 * Creates a new {@link CORBAQueryServiceFactory} instance.
	 *
	 * @param service CORBA query service.
	 */
	public CORBAQueryServiceFactory(QueryService service) {
		this.service = service;
	}

	public jpl.eda.query.QueryService createQueryServce() {
		return new QueryServiceAdaptor();
	}

	/** CORBA Query service to use. */
	private QueryService service;

	/**
	 * Adaptor for the CORBA-based query server.
	 */
	private class QueryServiceAdaptor implements jpl.eda.query.QueryService {
		public jpl.eda.query.QueryService.Server createServer() {
			return new ServerAdaptor(service.createServer());
		}
	}

	/**
	 * Adaptor for a CORBA-based server instance.
	 */
	private static class ServerAdaptor implements jpl.eda.query.QueryService.Server {
		/**
		 * Creates a new {@link ServerAdaptor} instance.
		 *
		 * @param svr CORBA sever instance.
		 */
		public ServerAdaptor(Server svr) {
			this.svr = svr;
		}

		public List queryProfileServers(XMLQuery query, List servers) throws QueryException {
			try {
				byte[] rc = svr.queryProfileServers(query.getXMLDocString(), (String[]) servers
					.toArray(EMPTY_STRING_ARRAY));
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rc));
				List result = (List) in.readObject();
				in.close();
				return result;
			} catch (QueryServiceException ex) {
				throw new QueryException(ex.errorInfo);
			} catch (org.omg.CORBA.SystemException ex) {
				throw new QueryException(ex.getMessage());
			} catch (IOException ex) {
				throw new IllegalStateException("Unexpected IOException: " + ex.getMessage());
			} catch (ClassNotFoundException ex) {
				throw new IllegalStateException("Unexpected ClassNotFoundException: " + ex.getMessage());
                        }
		}

		public List queryDefaultProfileServers(XMLQuery query) throws QueryException {
			try {
				byte[] rc = svr.queryDefaultProfileServers(query.getXMLDocString());
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rc));
				List result = (List) in.readObject();
				in.close();
				return result;
			} catch (QueryServiceException ex) {
				throw new QueryException(ex.errorInfo);
			} catch (org.omg.CORBA.SystemException ex) {
				throw new QueryException(ex.getMessage());
			} catch (IOException ex) {
				throw new IllegalStateException("Unexpected IOException: " + ex.getMessage());
			} catch (ClassNotFoundException ex) {
				throw new IllegalStateException("Unexpected ClassNotFoundException: " + ex.getMessage());
                        }
		}

		public XMLQuery queryProductServer(XMLQuery query, String serverID) throws QueryException {
			try {
				return new XMLQuery(svr.queryProductServer(query.getXMLDocString(), serverID));
			} catch (SAXException ex) {
				throw new QueryException(ex);
			} catch (QueryServiceException ex) {
				throw new QueryException(ex.errorInfo);
			} catch (org.omg.CORBA.SystemException ex) {
				throw new QueryException(ex.getMessage());
			}
		}

		public XMLQuery getQuery() throws QueryException {
			try {
				return new XMLQuery(svr.getQuery());
			} catch (SAXException ex) {
                              throw new QueryException(ex);
			}
		}

		public byte[] retrieveChunk(String id, long offset, int length, String serverID) throws QueryException {
			try {
				return svr.retrieveChunk(id, offset, length, serverID);
			} catch (QueryServiceException ex) {
				throw new QueryException(ex.errorInfo);
			} catch (org.omg.CORBA.SystemException ex) {
				throw new QueryException(ex.getMessage());
			}
		}

		public void close(String id, String serverID) throws QueryException {
			try {
				svr.close(id, serverID);
			} catch (QueryServiceException ex) {
				throw new QueryException(ex.errorInfo);
			} catch (org.omg.CORBA.SystemException ex) {
				throw new QueryException(ex.getMessage());
			}
		}

		/**
		 * Destroy the server-side instance.
		 *
		 * @throws Throwable if an error occurs.
		 */
		public void finalize() throws Throwable {
			try {
				svr.destroy();
			} catch (Throwable ignore) {}
			super.finalize();
		}

		/** CORBA-based server-side instance. */
		private Server svr;
	}

	/** Handy for list-to-array conversion. */
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
}
