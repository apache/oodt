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


package jpl.eda.query.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import jpl.eda.query.QueryException;
import jpl.eda.xmlquery.XMLQuery;

/**
 * Interface definition from RMI-based server instance.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public interface Server extends Remote {
	List queryProfileServers(XMLQuery query, List servers) throws QueryException, RemoteException;
	List queryDefaultProfileServers(XMLQuery query) throws QueryException, RemoteException;
	XMLQuery queryProductServer(XMLQuery query, String serverID) throws QueryException, RemoteException;
	XMLQuery getQuery() throws QueryException, RemoteException;
	byte[] retrieveChunk(String productID, long offset, int length, String serverID) throws QueryException, RemoteException;
	void close(String productID, String serverID) throws QueryException, RemoteException;
}
