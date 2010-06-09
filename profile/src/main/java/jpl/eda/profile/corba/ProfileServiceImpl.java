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
