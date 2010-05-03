// Copyright 1999-2001 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProfileServiceImpl.java,v 1.2 2004/07/30 22:31:50 kelly Exp $

package jpl.eda.profile.corba;

import java.util.List;
import jpl.eda.ExecServer;
import jpl.eda.profile.ProfileException;
import jpl.eda.profile.handlers.ProfileHandler;

/**
 * Profile service, CORBA style.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */

public class ProfileServiceImpl extends ProfileServicePOA {
	/**
         * Creates a new <code>ProfileServiceImpl</code> instance.
         *
         * @param server Server executive.
         * @throws ProfileException if an error occurs.
         */
	public ProfileServiceImpl(ExecServer server) throws ProfileException {
		_this(jpl.eda.util.CORBAMgr.getCORBAMgr().getORB());
                this.server = server;
                handlers = jpl.eda.profile.handlers.Utility.loadHandlers();
        }


	public Server createServer() {
		ServerImpl i = new ServerImpl(handlers);
		return i._this(jpl.eda.util.CORBAMgr.getCORBAMgr().getORB());
	}

        public String getServerInterfaceName() {
                return "jpl.eda.profile.corba.ProfileService";
        }

	public byte[] control(byte[] command) {
		return server.control(command);
	}

	public void controlAsync(byte[] command) {
		server.control(command);
	}
	
	/** Profile handlers. */
        private List handlers;
                
        /** Server executive. */
        private ExecServer server;
}
