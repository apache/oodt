// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: QueryServiceImpl.java,v 1.2 2004-07-30 22:30:27 kelly Exp $

package jpl.eda.query.corba;

import jpl.eda.ExecServer;

/**
 * CORBA implementation of the query service.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public class QueryServiceImpl extends QueryServicePOA {
	public QueryServiceImpl(ExecServer server) {
		_this(jpl.eda.util.CORBAMgr.getCORBAMgr().getORB());
		this.server = server;
	}

	public Server createServer() {
		ServerImpl i = new ServerImpl();
		return i._this(jpl.eda.util.CORBAMgr.getCORBAMgr().getORB());
	}

        public String getServerInterfaceName() {
                return "jpl.eda.query.corba.QueryService";
        }

	public byte[] control(byte[] command) {
		return server.control(command);
	}

	public void controlAsync(byte[] command) {
		server.control(command);
	}

	/** Executive. */
	private ExecServer server;
}
