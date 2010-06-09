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


package jpl.eda.profile.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import jpl.eda.activity.Activity;
import jpl.eda.activity.ActivityTracker;
import jpl.eda.profile.Profile;
import jpl.eda.profile.ProfileException;
import jpl.eda.profile.activity.QueriedHandlers;
import jpl.eda.profile.activity.ReceivedQuery;
import jpl.eda.profile.handlers.ProfileHandler;
import jpl.eda.profile.handlers.ProfileManager;
import jpl.eda.xmlquery.*;
import org.xml.sax.SAXException;

/**
 * RMI implementation of profile server instance.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
class ServerImpl extends UnicastRemoteObject implements jpl.eda.profile.Server {
	/**
	 * Creates a new <code>ServerImpl</code> instance.
	 *
	 * @param handlers List of {@link QueryHandler}s.
	 * @throws RemoteException if an error occurs.
	 */
	ServerImpl(List handlers) throws RemoteException {
		super(Utility.getRMIPort());
		this.handlers = handlers;
	}

	public List query(XMLQuery q) throws ProfileException {
		Activity activity = ActivityTracker.createActivity();
                activity.setID(q.getQueryHeader().getID());
                activity.log(new ReceivedQuery());
                List results = jpl.eda.profile.handlers.Utility.findProfiles(q, handlers);
                activity.log(new QueriedHandlers());
                return results;
	}

	public Profile getProfile(String id) throws ProfileException {
		return jpl.eda.profile.handlers.Utility.getProfile(id, handlers);
	}

	public void add(String profileStr)throws ProfileException {
		jpl.eda.profile.handlers.Utility.addAll(profileStr, handlers);
        }

	public void replace(String profileStr)throws ProfileException {
		try {
			jpl.eda.profile.handlers.Utility.replace(new Profile(profileStr),handlers);
		} catch (SAXException se) {
			throw new ProfileException(se.getMessage());
		}		
        }

	public boolean remove(String profId, String version)throws ProfileException {
		return jpl.eda.profile.handlers.Utility.remove(profId, version, handlers);
	}

	/** Query handlers. */
	private List handlers;
}
