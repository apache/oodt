/**
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

package org.apache.oodt.grid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.xml.sax.SAXException;
import org.apache.oodt.xmlquery.XMLQuery;

/**
 * Query servlet provides common query functionality for profile queries and product
 * queries.  It treats GETs as POSTs, retrieves the complete XMLQuery (if the
 * <code>xmlq</code> request parameter appears) or constructs a new XMLQuery (using the
 * <code>q</code> request parameter, which contains just a query expression), updates
 * system properties for the handlers, instantiates any new handlers, and then runs the
 * query.
 *
 */
public abstract class QueryServlet extends GridServlet {
	/**
	 * Get a list of {@link Server}s that will provide handlers to handle the query.
	 * Subclasses implement this by returning a list of either {@link ProductServer}s
	 * or {@link ProfileServer}s.
	 *
	 * @param config a <code>Configuration</code> value.
	 * @return a <code>List</code> value of {@link Server}s of some kind.
	 */
	protected abstract List getServers(Configuration config);

	/**
	 * Handle the query.  Subclasses implement this by parsing the query and returning
	 * some results.
	 *
	 * @param query The query to handle.
	 * @param handlers A list of either product <code>QueryHandler</code>s or profile <code>ProfileHandler</code>s.
	 * @param req a <code>HttpServletRequest</code> value.
	 * @param res a <code>HttpServletResponse</code> value.
	 * @throws IOException if an I/O error occurs.
	 * @throws ServletException if a servlet error occurs.
	 */
	protected abstract void handleQuery(XMLQuery query, List handlers, HttpServletRequest req, HttpServletResponse res)
		throws IOException;

	/**
	 * Treat GETs as POSTs.
	 *
	 * @param req a <code>HttpServletRequest</code> value.
	 * @param res a <code>HttpServletResponse</code> value.
	 * @throws IOException if an error occurs.
	 * @throws ServletException if an error occurs.
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		doPost(req, res);
	}

	/**
	 * Handle the query.
	 *
	 * @param req a <code>HttpServletRequest</code> value.
	 * @param res a <code>HttpServletResponse</code> value.
	 * @throws IOException if an error occurs.
	 * @throws ServletException if an error occurs.
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		try {
			XMLQuery query = getQuery(req, res);			       // Get the query
			if (query == null) {
			  return;                       // No query? My favorite case, right here!
			}

			Configuration config = getConfiguration();		       // Get the current configuration.
			updateProperties(config);				       // Using it, update the system properties
			updateHandlers(getServers(config));			       // And any servers.

			List handlerObjects = new ArrayList(handlers.size());	       // Start with no handlers.
		  for (Object handler : handlers) {           // For each server
			InstantiedHandler ih = (InstantiedHandler) handler;   // Get its handler
			handlerObjects.add(ih.getHandler());               // Add its handler
		  }
			handleQuery(query, handlerObjects, req, res);		       // Handlers: handle query, please
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
	}

	/**
	 * Get the query from an HTTP request.  The request must have either a
	 * <code>xmlq</code> parameter (which is given priority) or a <code>q</code>
	 * parameter.  We expect <code>xmlq</code> to contain the XML text format of an
	 * XMLQuery object.  <code>q</code> can contain just the query expression, from
	 * which we'll construct a fresh <code>XMLQuery</code> with reasonable defaults.
	 * If the user specifies the <code>q</code> paramater, we'll treat it as a parsed
	 * query, letting the XMLQuery class parse it and build its various expression
	 * stacks according to the DIS syntax.  The user can specify the <code>unp</code>
	 * parameter to control this behavior, though.  Set to the string
	 * <code>true</code> and we'll treat the query as <em>unparsed</em>, and the
	 * XMLQuery class should leave it alone.  Otherwise, an other value (or
	 * unspecified) will be interpreted as <code>false</code>, meaning the query will
	 * be parsed.
	 *
	 * @param req a <code>HttpServletRequest</code> value.
	 * @param res a <code>HttpServletResponse</code> value.
	 * @return a <code>XMLQuery</code> value.
	 * @throws IOException if an error occurs.
	 */
	protected XMLQuery getQuery(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String xmlq = req.getParameter("xmlq");				       // Grab any xmlq
		String q = req.getParameter("q");				       // Grab any q
		String unp = req.getParameter("unp");				       // And grab any unp (pronounced "unp")
		if (xmlq == null) {
		  xmlq = "";                           // No xmlq?  Use epsilon
		}
		if (q == null) {
		  q = "";                               // No q?  Use lambda
		}
		if (unp == null) {
		  unp = "";                           // Use some other greek letter for empty str
		}
		String[] mimes = req.getParameterValues("mime");		       // Grab any mimes
		if (mimes == null) {
		  mimes = EMPTY_STRING_ARRAY;                   // None?  Use empty array
		}

		if (xmlq.length() > 0) {
		  try {                           // Was there an xmlq?
			return new XMLQuery(xmlq);                       // Use it in its entirety, ignoring the rest
		  } catch (SAXException ex) {                           // Can't parse it?
			res.sendError(HttpServletResponse.SC_BAD_REQUEST,           // Then that's a bad ...
				"cannot parse xmlq: " + ex.getMessage());           // ... request, which I hate
			return null;                               // so flag it with a null
		  }
		} else if (q.length() > 0) {					       // Was there a q?
			boolean unparsed = "true".equals(unp);			       // If so, was there also an unp?
			return new XMLQuery(q, "wgq", "Web Grid Query",		       // Use it to make an XMLQuery
				"Query from Web-Grid", /*ddID*/null,                   // And all of these extra
				/*resultModeId*/null, /*propType*/null,                // parameters really annoy
				/*propLevels*/null, /*maxResults*/Integer.MAX_VALUE,   // the poop out of me
				Arrays.asList(mimes), !unparsed);                      // It's just a query for /sbin/fsck sake!
		}

		res.sendError(HttpServletResponse.SC_BAD_REQUEST, "xmlq or q parameters required");
		return null;
	}

	/**
	 * Update the query handlers instantiated.
	 *
	 * @param servers a <code>List</code> of {@link Server}s.
	 * @throws ClassNotFoundException if a class can't be found.
	 * @throws InstantiationException if a class can't be instantiated.
	 * @throws IllegalAccessException if a constructor isn't public.
	 */
	private synchronized void updateHandlers(List servers) throws ClassNotFoundException, InstantiationException,
		IllegalAccessException {
		eachServer:
	  for (Object server1 : servers) {               // For each server
		Server server = (Server) server1;                   // Grab the server
		for (Object handler1 : handlers) {           // For each handler
		  InstantiedHandler handler = (InstantiedHandler) handler1; // Grab the handler
		  if (handler.getServer().equals(server))               // Have we already instantiated?
		  {
			continue eachServer;                   // Yes, try the next server
		  }
		}
		InstantiedHandler handler                       // No.  Create ...
			= new InstantiedHandler(server, server.createHandler()); // ... a fresh handler
		handlers.add(handler);                           // Save it
	  }

		for (Iterator i = handlers.iterator(); i.hasNext();) {		       // Now, for each handler
			InstantiedHandler handler = (InstantiedHandler) i.next();      // Grab the handler
			if (!servers.contains(handler.getServer()))		       // Does its server still exist?
			{
			  i.remove();                           // If not, remove the handler
			}
		}
	}

	/**
	 * Update system properties used by query handlers, if any have changed.
	 *
	 * @param config a <code>Configuration</code> value.
	 */
	private synchronized void updateProperties(Configuration config) {
		if (properties != null) {					       // Any old properties?
			if (properties.equals(config.getProperties())) {
			  return;           // Yes, any changes?  No?  Then done.
			}
		  for (Object o : properties.keySet()) {
			System.getProperties().remove(o);           // and remove it.
		  }
		}
		properties = (Properties) config.getProperties().clone();	       // Now copy the new settings
		System.getProperties().putAll(properties);			       // And set them!
	}

	/**
	 * Instantiated query handlers.
	 *
	 * These are either <code>ProfileHandler</code>s or product <code>QueryHandler</code>s.
	 */
	protected List handlers = new ArrayList();

	/** Current settings of system properties. */
	private Properties properties;

	/**
	 * An instantiated handler.  This is a <code>ProfileHandler</code> or a product
	 * <code>QueryHandler</code> along with the {@link Server} that defined it.
	 */
	private static class InstantiedHandler {
		/**
		 * Creates a new <code>InstantiedHandler</code> instance.
		 *
		 * @param server a <code>Server</code>.
		 * @param handler a <code>ProfileHandler</code> or a product <code>QueryHandler</code>.
		 */
		InstantiedHandler(Server server, Object handler) {
			this.server = server;
			this.handler = handler;
		}

		/**
		 * Get the server that defined this handler.
		 *
		 * @return a <code>Server</code> value.
		 */
		public Server getServer() {
			return server;
		}

		/**
		 * Get the handler.
		 *
		 * @return a <code>ProfileHandler</code> or a product <code>QueryHandler</code>.
		 */
		public Object getHandler() {
			return handler;
		}

		/** Server that defines the handler. */
		private Server server;

		/** A <code>ProfileHandler</code> or a product <code>QueryHandler</code> */
		private Object handler;
	}

	/** So we don't create a bunch of empty string arrays, here's the only one we'll ever need. */
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
}
