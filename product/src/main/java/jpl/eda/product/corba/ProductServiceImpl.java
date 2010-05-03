// Copyright 1999-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProductServiceImpl.java,v 1.3 2004-07-30 22:31:05 kelly Exp $

package jpl.eda.product.corba;

import java.util.List;
import jpl.eda.ExecServer;

/**
 * CORBA implementation of the product service.
 *
 * @author Kelly
 * @version $Revision: 1.3 $
 */
public class ProductServiceImpl extends ProductServicePOA {
	/**
	 * Creates a new <code>ProductServiceImpl</code> instance.
	 *
	 * @param server Executive managing this service.
	 */
	public ProductServiceImpl(ExecServer server) {
		_this(jpl.eda.util.CORBAMgr.getCORBAMgr().getORB());
		this.server = server;
		handlers = jpl.eda.product.Utility.loadHandlers(server.getName());
	}

	public Server createServer() {
		ServerImpl i = new ServerImpl(handlers);
		return i._this(jpl.eda.util.CORBAMgr.getCORBAMgr().getORB());
	}

        public String getServerInterfaceName() {
                return "jpl.eda.product.corba.ProductService";
        }

	public byte[] control(byte[] command) {
		return server.control(command);
	}

	public void controlAsync(byte[] command) {
		server.control(command);
	}

	/** Executive. */
	private ExecServer server;

	/** Query handlers. */
	private List handlers;
}
