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


package jpl.eda.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import jpl.eda.product.ProductClient;
import jpl.eda.product.ProductException;
import jpl.eda.profile.Profile;
import jpl.eda.profile.ProfileClient;
import jpl.eda.profile.ProfileException;
import jpl.eda.xmlquery.XMLQuery;
import jpl.eda.xmlquery.Statistic;

import jpl.eda.profile.*;

/**
 * A query engine runs queries.
 *
 * The query service uses the query engine to actually run profile and product queries.
 * You can use the query engine directly, too, if you don't need a query server and can
 * access profile and product servers directly.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public class QueryEngine implements QueryService.Server {
	public List queryProfileServers(XMLQuery query, List servers) throws QueryException {
		// check
		if (query == null) throw new IllegalArgumentException("Query required");
		if (servers == null) throw new IllegalArgumentException("Servers required");
		if (servers.isEmpty()) return Collections.EMPTY_LIST;
		Set queriedServers = new HashSet(servers.size());
		LinkedList queriers = new LinkedList();

		// sow
		for (Iterator i = servers.iterator(); i.hasNext();) {
			String serverID = (String) i.next();
			launchQuerier(queriers, queriedServers, query, serverID);
		}

		List results = new ArrayList();
		QueryException queryException = null;

		// reap
		for (;;) {
			Querier q = null;
			synchronized (queriedServers) {
				if (queriers.isEmpty()) break;
				q = (Querier) queriers.removeFirst();
			}

			for (;;) try {
				q.join();
				QueryException qe = q.getException();
				if (qe != null)
					queryException = qe;
				else
					results.addAll(q.getResults());
				break;
			} catch (InterruptedException ignore) {}
		}

		this.query = query;

		// report
		if (queryException != null) throw queryException;

		return results;
	}

	public List queryDefaultProfileServers(XMLQuery query) throws QueryException {
		return queryProfileServers(query, getDefaultProfileServerIDs());
	}

	public XMLQuery queryProductServer(XMLQuery query, String serverID) throws QueryException {
		if (query == null) throw new IllegalArgumentException("Query required");
		if (serverID == null) throw new IllegalArgumentException("Server ID required");
		try {
			System.err.println("=== Constructing ProductClient for server " + serverID);
			ProductClient pc = new ProductClient(serverID);
			System.err.println("Constructed, calling query with expr = " + query.getKwdQueryString());
			XMLQuery r = pc.query(query);
			System.err.println("Called; returning " + r.getResults().size() + " result(s)");
			return r;
		} catch (ProductException ex) {
			System.err.println("--- Exception!");
			ex.printStackTrace();
			throw new QueryException(ex);
		}
	}

	public XMLQuery getQuery() {
		return query;
	}

	public byte[] retrieveChunk(String productID, long offset, int length, String serverID) throws QueryException {
		if (productID == null) throw new IllegalArgumentException("Product ID required");
		if (serverID == null) throw new IllegalArgumentException("Server ID required");
		if (offset < 0) throw new IllegalArgumentException("Nonnegative offset required");
		if (length <= 0) throw new IllegalArgumentException("Positive size required");
		try {
			ProductClient pc = new ProductClient(serverID);
			return pc.retrieveChunk(productID, offset, length);
		} catch (ProductException ex) {
			throw new QueryException(ex);
		}
	}

	public void close(String productID, String serverID) throws QueryException {
		if (productID == null) throw new IllegalArgumentException("Product ID required");
		if (serverID == null) throw new IllegalArgumentException("Server ID required");
		try {
			ProductClient pc = new ProductClient(serverID);
			pc.close(productID);
		} catch (ProductException ex) {
			throw new QueryException(ex);
		}
	}

	/**
	 * Return the list of default profile servers.
	 *
	 * The list of default profile servers comes from the comma-separated list of
	 * names in the <code>jpl.eda.query.profileServers</code> system property.  If
	 * undefined, it defaults to <code>urn:eda:rmi:JP.Profile</code>.
	 *
	 * @return a {@link List} of {@link String} profile server IDs.
	 */
	private static List getDefaultProfileServerIDs() {
		while (defaultServerIDs == null) synchronized (QueryEngine.class) {
			if (defaultServerIDs == null) {
				defaultServerIDs = new ArrayList();
				String values = System.getProperty("jpl.eda.query.profileServers","urn:eda:rmi:JPL.Profile");
				for (Iterator i = jpl.eda.util.Utility.parseCommaList(values); i.hasNext();)
					defaultServerIDs.add(i.next());
			}
		}
		return defaultServerIDs;
	}

	/**
	 * Launch a new profile querier.
	 *
	 * This method avoids launching a querier at servers that have been or are currently being queried.
	 *
	 * @param queriers List of current queriers.
	 * @param queriedServers Set of {@link String} server IDs queried.
	 * @param query Query.
	 * @param serverID What server to query.
	 */
	private static void launchQuerier(LinkedList queriers, Set queriedServers, XMLQuery query, String serverID) {
		Querier q;
		synchronized (queriedServers) {
			if (queriedServers.contains(serverID)) return;
			queriedServers.add(serverID);
			q = new Querier(queriers, queriedServers, query, serverID);
			queriers.addLast(q);
		}
		q.start();
	}

	/** List of {@link String} profile server IDs to query by default. */
	private static List defaultServerIDs;
	
	/** Query to use. */
        private XMLQuery query;
	
	/**
	 * A querier is a thread that runs a query at a profile server.
	 */
	private static class Querier extends Thread {
		/**
		 * Creates a new <code>Querier</code> instance.
		 *
		 * @param queriers List of other queriers.
		 * @param queriedServers Set of {@link String} server IDs being queried.
		 * @param query Query.
		 * @param serverID Server to query.
		 */
		Querier(LinkedList queriers, Set queriedServers, XMLQuery query, String serverID) {
			super("Querying " + serverID + " for " + query.getKwdQueryString());
			this.queriers = queriers;
			this.queriedServers = queriedServers;
			this.query = query;
			this.serverID = serverID;
		}

		/**
		 * Get any exception from running this query.
		 *
		 * This method may be called only after this thread terminates.
		 *
		 * @return a {@link QueryException} value.
		 */
		public QueryException getException() {
			if (isAlive()) throw new IllegalStateException("Join thread first");
			return queryException;
		}

		/**
		 * Get any results of this query.
		 *
		 * This method may be called only after this thread terminates.
		 *
		 * @return a {@link List} of {@link Profile}s.
		 */
		public List getResults() {
			if (isAlive()) throw new IllegalStateException("Join thread first");
			return results;
		}

		/**
		 * Start querying, adding new queriers for any new profile servers discovered.
		 */
		public void run() {
			try {
				ProfileClient pc = new ProfileClient(serverID);
			
				long time = System.currentTimeMillis();
				results = pc.query(query);
				long searchTime = System.currentTimeMillis() - time;  // for performance evaluation
				List searchTimeList;
				synchronized (searchTimeList =query.getStatistics()) {
        				Statistic sta = new Statistic(serverID, searchTime);
        				searchTimeList.add(sta);
				}

				for (Iterator i = results.iterator(); i.hasNext();) {
					Profile p = (Profile) i.next();
					if ("system.profileServer".equals(p.getResourceAttributes().getResClass())) {
						i.remove();
						for (Iterator j = p.getResourceAttributes().getResLocations().iterator();
						        j.hasNext();) {
							String otherServerID = (String) j.next();
							launchQuerier(queriers, queriedServers, query, otherServerID);
						}
					}
				}
			} catch (ProfileException ex) {
				queryException = new QueryException(ex);
			} catch (RuntimeException ex) {
				ex.printStackTrace();
			}
		}

		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof Querier)) return false;
			Querier rhs = (Querier) obj;
			return serverID.equals(rhs.serverID);
		}

		/** Queriers currently running. */
		private LinkedList queriers;

		/** List of {@link Profile}s found at this server. */
		private List results;

		/** If nonnull, any exception generated as a result of running the query. */
		private QueryException queryException;

		/** Servers queried so far. */
		private Set queriedServers;

		/** Server I'm querying. */
		private String serverID;

		/** Query to use. */
		private XMLQuery query;
	}
}
