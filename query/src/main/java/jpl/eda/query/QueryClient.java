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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.naming.Context;
import jpl.eda.Configuration;
import jpl.eda.profile.Profile;
import jpl.eda.query.corba.CORBAQueryServiceFactory;
import jpl.eda.query.rmi.RMIQueryServiceFactory;
import jpl.eda.xmlquery.Result;
import jpl.eda.xmlquery.XMLQuery;

/**
 * Client to access a query server.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public class QueryClient {
	/**
	 * Initialize this class by running any enterprise initializers.
	 */
	static {
		try {
			jpl.eda.ExecServer.runInitializers();
		} catch (jpl.eda.EDAException ex) {
			System.err.println("\bFatal error:"); 
			ex.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Creates a new <code>QueryClient</code> instance.
	 *
	 * @param serviceID URI of the query server.
	 * @throws QueryException if an error occurs.
	 */
	public QueryClient(String serviceID) throws QueryException {
		try {
			Configuration configuration = Configuration.getConfiguration();
			Context ctx = configuration.getObjectContext();
			Object result = ctx.lookup(serviceID);
			QueryService.Factory factory = null;

			// disgusting.  this should disappear into the jndi layer.
			if (result instanceof jpl.eda.query.rmi.QueryService)
				factory = new RMIQueryServiceFactory((jpl.eda.query.rmi.QueryService) result);
			else if (result instanceof jpl.eda.query.corba.QueryService)
				factory = new CORBAQueryServiceFactory((jpl.eda.query.corba.QueryService) result);
			else
				throw new IllegalStateException("Unknown kind of query service `" + result + "'");

			service = factory.createQueryServce();
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new QueryException(ex);
		}
	}

	/**
	 * Query the given profile servers.
	 *
	 * This query will crawl a network of profile servers whenever a matching profile
	 * refers to another profile server.
	 *
	 * @param query a {@link XMLQuery} value.
	 * @param servers a {@link List} of {@link String} server IDs.
	 * @return a {@link List} of {@link Profile}s that match.
	 * @throws QueryException if an error occurs.
	 */
	public List queryProfileServers(XMLQuery query, List servers) throws QueryException {
		QueryService.Server server = service.createServer();
		List results = server.queryProfileServers(query, servers);
		xmlQuery = server.getQuery();
		return results;
	}

	public XMLQuery getQuery() throws QueryException {
		return xmlQuery;
	}

	/**
	 * Query the default profile servers.
	 *
	 * This query will crawl a network of profile servers whenever a matching profile
	 * refers to another profile server.
	 *
	 * @param query a {@link XMLQuery} value.
	 * @throws QueryException if an error occurs.
	 */
	public List queryDefaultProfileServers(XMLQuery query) throws QueryException {
		QueryService.Server server = service.createServer();
		return server.queryDefaultProfileServers(query);
	}

	/**
	 * Query a product server.
	 *
	 * @param query a {@link XMLQuery} value.
	 * @param serverID What server to query.
	 * @return a {@link XMLQuery} value.
	 * @throws QueryException if an error occurs.
	 */
	public XMLQuery queryProductServer(XMLQuery query, String serverID) throws QueryException {
		QueryService.Server server = service.createServer();
		return server.queryProductServer(query, serverID);
	}

	/**
	 * Retrieve a chunk from a product server.
	 *
	 * @param productID What product to get.
	 * @param offset Where in the product to get a chunk.
	 * @param length How big a chunk to get.
	 * @param serverID What server has the product.
	 * @return a chunk.
	 * @throws QueryException if an error occurs.
	 */
	public byte[] retrieveChunk(String productID, long offset, int length, String serverID) throws QueryException {
		QueryService.Server server = service.createServer();
		return server.retrieveChunk(productID, offset, length, serverID);
	}

	/**
	 * Close a chunked product.
	 *
	 * @param productID Product to close.
	 * @param serverID Where to close it.
	 * @throws QueryException if an error occurs.
	 */
	public void close(String productID, String serverID) throws QueryException {
		QueryService.Server server = service.createServer();
		server.close(productID, serverID);
	}

	/**
	 * Command-line access.
	 *
	 * @param argv Command-line arguments.
	 * @throws Throwable if an error occurs.
	 */
	public static void main(String[] argv) throws Throwable {
		if (argv.length < 2) {
			System.err.println("Usage: -profile [-server <serverID>...] <queryServer> <expr>");
			System.err.println("   or: -product {-xml|-out} -server <serverID> <queryServer> <expr>");
			System.exit(1);
		}

		byte type = 0; // 0=unspecified, 1=profile, 2=product
		byte xml = 0; // 0=unspecified, 1=xml output, 2=raw data output
		String serverID = null;
		String expr = null;
		List servers = new ArrayList();
		for (int i = 0; i < argv.length; ++i) {
			String arg = argv[i];
			if ("-profile".equals(arg))
				type = 1;
			else if ("-product".equals(arg))
				type = 2;
			else if ("-server".equals(arg)) {
				if (i == argv.length) throw new IllegalArgumentException("Server ID required");
				servers.add(argv[++i]);
			} else if ("-xml".equals(arg))
				xml = 1;
			else if ("-out".equals(arg))
				xml = 2;
			else {
				if (i != argv.length - 2 && i != argv.length - 1)
					throw new IllegalArgumentException("Usage: <options>... <queryServer> <expr>");
				if (serverID == null)
					serverID = arg;
				else
					expr = arg;
			}
		}

		if (expr == null) throw new IllegalArgumentException("Query expression missing"); 
		if (serverID == null) throw new IllegalArgumentException("Query server ID missing"); 
		if (type == 0) throw new IllegalArgumentException("Specify either -product or -profile");
		if (type == 2 && xml == 0) throw new IllegalArgumentException("Specify either -xml or -out");
		if (type == 2 && servers.size() != 1) throw new IllegalArgumentException("Specify exactly one product server");
		if (type == 1 && xml != 0) throw new IllegalArgumentException("-xml/-out is for product queries only");

		QueryClient qc = new QueryClient(serverID);
		XMLQuery xq = new XMLQuery(expr, /*id*/null, /*title*/null, /*desc*/null, /*ddId*/null, /*resultModeId*/null,
			/*propType*/null, /*propLevels*/null, 999);
		if (type == 1) {
			List results = servers.isEmpty()? qc.queryDefaultProfileServers(xq) : qc.queryProfileServers(xq, servers);
			System.out.println(results);
		} else {
			xq = qc.queryProductServer(xq, (String) servers.get(0));
			if (xml == 1)
				System.out.println(xq.getXMLDocString());
			else {
				List results = xq.getResults();
				if (results.isEmpty())
					System.err.println("No matching products");
				else {
					Result result = (Result) results.get(0);
					InputStream in = result.getInputStream();
					byte[] buf = new byte[512];
					int numRead;
					while ((numRead = in.read(buf)) != -1)
						System.out.write(buf, 0, numRead);
					in.close();
					System.out.close();
				}					
			}
		}
	}

	/** Query service we're using. */
	private QueryService service;

	/** query */
	private XMLQuery xmlQuery;
}
