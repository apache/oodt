// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProductServiceAdaptor.java,v 1.1.1.1 2004-03-02 19:45:40 kelly Exp $

package jpl.eda.product.corba;

import java.rmi.RemoteException;
import jpl.eda.product.ProductException;
import jpl.eda.product.corba.ServerPackage.ProductServiceException;
import jpl.eda.xmlquery.XMLQuery;
import org.xml.sax.SAXException;

/**
 * Adapt a CORBA product service into a Java-compatible interface.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public class ProductServiceAdaptor implements jpl.eda.product.ProductService {
	/**
	 * Creates a new <code>ProductServiceAdaptor</code> instance.
	 *
	 * @param corbaProductService The CORBA product service to adapt.
	 */
	public ProductServiceAdaptor(ProductService corbaProductService) {
		svc = corbaProductService;
	}

	public jpl.eda.product.Server createServer() {
		return new ServerAdaptor(svc.createServer());
	}

	/**
	 * Adapter for the product server instance.
	 */
	private static class ServerAdaptor implements jpl.eda.product.Server {
		/**
		 * Creates a new <code>ServerAdaptor</code> instance.
		 *
		 * @param corbaServer CORBA server to adapt.
		 */
		public ServerAdaptor(Server corbaServer) {
			svr = corbaServer;
		}
		public XMLQuery query(XMLQuery q) throws ProductException, RemoteException {
			try {
				return new XMLQuery(new String(svr.query(q.getXMLDocString().getBytes())));
			} catch (ProductServiceException ex) {
				throw new ProductException(ex.getMessage());
			} catch (org.omg.CORBA.SystemException ex) {
				throw new RemoteException(ex.getMessage());
			} catch (SAXException ex) {
				throw new ProductException(ex);
			}
		}
		public byte[] retrieveChunk(String id, long offset, int length) throws ProductException, RemoteException {
			try {
				return svr.retrieveChunk(id, offset, length);
			} catch (ProductServiceException ex) {
				throw new ProductException(ex.getMessage());
			} catch (org.omg.CORBA.SystemException ex) {
				throw new RemoteException(ex.getMessage());
			}
		}
		public void close(String id) throws ProductException, RemoteException {
			try {
				svr.close(id);
			} catch (ProductServiceException ex) {
				throw new ProductException(ex.getMessage());
			} catch (org.omg.CORBA.SystemException ex) {
				throw new RemoteException(ex.getMessage());
			}
		}
		public void finalize() throws Throwable {
			try {
				svr.destroy();
			} catch (Throwable ignore) {}
			super.finalize();
		}

		/** CORBA instance server we're adapting. */
		private Server svr;
	}

	/** CORBA product service we're adapting. */
	private ProductService svc;
}
