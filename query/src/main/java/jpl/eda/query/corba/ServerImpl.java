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


package jpl.eda.query.corba;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import jpl.eda.query.QueryEngine;
import jpl.eda.query.QueryException;
import jpl.eda.query.corba.ServerPackage.QueryServiceException;
import jpl.eda.xmlquery.XMLQuery;
import org.xml.sax.SAXException;

/**
 * CORBA implementation of query instance server.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
class ServerImpl extends ServerPOA {
	/**
	 * Creates a new <code>ServerImpl</code> instance.
	 */
	public ServerImpl() {
		qe = new QueryEngine();
		timeoutTask = new TimeoutTask();
		TIMER.schedule(timeoutTask, INSTANCE_EXPIRY);
	}

	public String queryProductServer(String queryString, String serverID) throws QueryServiceException {
		try {
			return qe.queryProductServer(new XMLQuery(queryString), serverID).getXMLDocString();
		} catch (SAXException ex) {
			QueryServiceException exception = new QueryServiceException();
			exception.errorInfo = "Can't parse XML product query: " + ex.getMessage();
			throw exception;
		} catch (QueryException ex) {
			QueryServiceException exception = new QueryServiceException();
			exception.errorInfo = ex.getMessage();
			throw exception;
		}
	}

	public byte[] retrieveChunk(String productID, long offset, int length, String serverID) throws QueryServiceException {
		try {
			return qe.retrieveChunk(productID, offset, length, serverID);
		} catch (QueryException ex) {
			QueryServiceException exception = new QueryServiceException();
			exception.errorInfo = ex.getMessage();
			throw exception;
		}
	}

	public void close(String productID, String serverID) throws QueryServiceException {
		try {
			qe.close(productID, serverID);
		} catch (QueryException ex) {
			QueryServiceException exception = new QueryServiceException();
			exception.errorInfo = ex.getMessage();
			throw exception;
		}
	}

	public byte[] queryProfileServers(String queryString, String[] servers) throws QueryServiceException {
		try {
			xmlQuery = new XMLQuery(queryString);
			List results = qe.queryProfileServers(xmlQuery, Arrays.asList(servers));
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
			QueryServiceException exception = new QueryServiceException();
			exception.errorInfo = "Can't parse XML profile query: " + ex.getMessage();
			throw exception;
		} catch (QueryException ex) {
			QueryServiceException exception = new QueryServiceException();
			exception.errorInfo = ex.getMessage();
			throw exception;
		}
	}

	public byte[] queryDefaultProfileServers(String queryString) throws QueryServiceException {
		try {
			List results = qe.queryDefaultProfileServers(new XMLQuery(queryString));
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
			QueryServiceException exception = new QueryServiceException();
			exception.errorInfo = "Can't parse XML profile query: " + ex.getMessage();
			throw exception;
		} catch (QueryException ex) {
			QueryServiceException exception = new QueryServiceException();
			exception.errorInfo = ex.getMessage();
			throw exception;
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

	 /** Get query */
        public String getQuery() {
                return xmlQuery.getXMLDocString();
        } 

	/** Heavy lifter. */
	private QueryEngine qe;

	/** Current task to expire this instance. */
	private TimeoutTask timeoutTask;

	/** Master timer shared by all instances. */
	private static final Timer TIMER = new Timer();

	/** When to expire an instance server. */
	public static final long INSTANCE_EXPIRY = Long.getLong("jpl.eda.query.corba.instanceExpiry",5*60*1000).longValue();

	/** query */
	XMLQuery xmlQuery;

	/** Task to expire an instance. */
	private class TimeoutTask extends TimerTask {
		public void run() {
			try {
				destroy();
			} catch (IllegalStateException ignore) {}
		}
	}
}

