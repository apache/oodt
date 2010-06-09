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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import jpl.eda.profile.*;
import jpl.eda.profile.corba.ServerPackage.ProfileServiceException;
import jpl.eda.profile.handlers.*;
import jpl.eda.xmlquery.XMLQuery;
import org.xml.sax.SAXException;

/**
 * Profile server instance, CORBA style.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
class ServerImpl extends ServerPOA {
	/**
	 * Creates a new <code>ServerImpl</code> instance.
	 *
	 * @param handler a <code>ProfileHandler</code> value.
	 */
	public ServerImpl(List handlers) {
		this.handlers = handlers;
		timeoutTask = new TimeoutTask();
		TIMER.schedule(timeoutTask, INSTANCE_EXPIRY);
	}

	public byte[] query(String xmlQueryString) throws ProfileServiceException {
		XMLQuery profileQuery = null;
		try {
			profileQuery = new XMLQuery(xmlQueryString);
			List results = jpl.eda.profile.handlers.Utility.findProfiles(profileQuery, handlers);
			System.err.println("# matching profiles is " + results.size());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(baos);
                        oos.writeObject(results);
                        oos.close();
                        baos.close();
                        return baos.toByteArray();
		 } catch (IOException ex) {
                        ex.printStackTrace();
                        throw new IllegalStateException("Unexpected IOException: " + ex.getMessage());
                } catch (SAXException ex) {
                        ProfileServiceException corbaEx = new ProfileServiceException();
                        corbaEx.errorInfo = "Can't parse xmlquery string: " + ex.getMessage();
                        throw corbaEx;
                } catch (ProfileException ex) {
                        ProfileServiceException corbaEx = new ProfileServiceException();
                        corbaEx.errorInfo = ex.getMessage();
                        throw corbaEx;
                }
	}

	public byte[] getProfile(String profileID) throws ProfileServiceException {
		try {
			Profile profile = jpl.eda.profile.handlers.Utility.getProfile(profileID, handlers);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(profile);
			oos.close();
			baos.close();
			return baos.toByteArray();
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new IllegalStateException("Unexpected IOException: " + ex.getMessage());
		} catch (ProfileException ex) {
			ProfileServiceException corbaEx = new ProfileServiceException();
			corbaEx.errorInfo = ex.getMessage();
			throw corbaEx;
		}
	}

	public void add(String profileStr) throws ProfileServiceException
        {       
                try {
			jpl.eda.profile.handlers.Utility.addAll(profileStr, handlers);
                } catch (Exception ex) {
			ProfileServiceException corbaEx = new ProfileServiceException();
                        corbaEx.errorInfo = ex.getMessage();
                        throw corbaEx;
                }
        }
                        
        public void replace(String profileStr) throws ProfileServiceException
        {
                try {
			jpl.eda.profile.handlers.Utility.replace(new Profile(profileStr), handlers);
                } catch (Exception ex) {
			ProfileServiceException corbaEx = new ProfileServiceException();
                        corbaEx.errorInfo = ex.getMessage();
                        throw corbaEx;
                }
        }

	public boolean remove(String profId, String version)
                throws ProfileServiceException
        {
                try {
			return jpl.eda.profile.handlers.Utility.remove(profId, version, handlers);
                } catch (Exception ex) {
			ProfileServiceException corbaEx = new ProfileServiceException();
                        corbaEx.errorInfo = ex.getMessage();
                        throw corbaEx;
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

	/** Handlers that do the actual work. */	
	private List handlers;

	/** Current task to expire this instance. */
	private TimeoutTask timeoutTask;

	/** Master timer shared by all instances. */
	private static final Timer TIMER = new Timer();

	/** When to expire an instance server. */
	public static final long INSTANCE_EXPIRY =Long.getLong("jpl.eda.product.corba.instanceExpiry", 5*60*1000).longValue();

	/** Task to expire an instance. */
	private class TimeoutTask extends TimerTask {
		public void run() {
			try {
				destroy();
			} catch (IllegalStateException ignore) {}
		}
	}
}

