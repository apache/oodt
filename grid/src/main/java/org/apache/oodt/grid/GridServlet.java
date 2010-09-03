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

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.xml.sax.SAXException;

/**
 * Grid servlet is an abstract servlet that provides basic behavior (configuration access)
 * for grid servlets.
 */
public abstract class GridServlet extends HttpServlet {
	/**
	 * By default, grid servlets are POST only, so GETs get you the welcome page.
	 *
	 * @param req a <code>HttpServletRequest</code> value.
	 * @param res a <code>HttpServletResponse</code> value.
	 * @throws IOException if an error occurs.
	 * @throws ServletException if an error occurs.
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		req.getRequestDispatcher("index.html").forward(req, res);
	}

	/**
	 * Get the configuration.
	 *
	 * @return a <code>Configuration</code> value.
	 * @throws ServletException if an error occurs.
	 * @throws IOException if an error occurs.
	 */
	protected Configuration getConfiguration() throws ServletException, IOException {
		if (configuration != null) return configuration;
		String path = getServletContext().getRealPath("/WEB-INF/config.xml");
		if (path == null)
			throw new ServletException("The config.xml file can't be accessed. Are we running from a war file!??!");
		File file = new File(path);
		Configuration c = null;
		try {
			c = new Configuration(file);
		} catch (SAXException ex) {
			throw new ServletException("Cannot parse config.xml file", ex);
		}
		synchronized (GridServlet.class) {
			while (configuration == null)
				configuration = c;
		}
		return configuration;
	}

	/**
	 * Get the config bean.
	 *
	 * @param req a <code>HttpServletRequest</code> value.
	 * @return a <code>ConfigBean</code> value.
	 * @throws ServletException if an error occurs.
	 * @throws IOException if an error occurs.
	 */
	protected ConfigBean getConfigBean(HttpServletRequest req) throws ServletException, IOException {
		HttpSession session = req.getSession(/*create*/true);
		ConfigBean cb = (ConfigBean) session.getAttribute("cb");
		if (cb == null) {
			cb = new ConfigBean();
			session.setAttribute("cb", cb);
		}
		Configuration config = getConfiguration();
		cb.setConfiguration(config);
		return cb;
	}

	/**
	 * Check if administrative access is allowed.  This examines the request scheme
	 * (http, ftp, https, etc.) and sees if https is required by the configuration.
	 * It also checks the remote host and sees if localhost access is required.
	 *
	 * @param config a <code>Configuration</code> value.
	 * @param req a <code>HttpServletRequest</code> value.
	 * @param res a <code>HttpServletResponse</code> value.
	 * @return True if access is approved, false otherwise.
	 * @throws IOException if an error occurs.
	 */
	protected boolean approveAccess(Configuration config, HttpServletRequest req, HttpServletResponse res) throws IOException {
		if (config.isHTTPSrequired() && !"https".equals(req.getScheme())) {
			res.sendError(HttpServletResponse.SC_FORBIDDEN, "https required");
			return false;
		}

		if (config.isLocalhostRequired() && !Utility.isLocalhost(req.getRemoteHost())) {
			res.sendError(HttpServletResponse.SC_FORBIDDEN, "localhost access only");
			return false;
		}

		return true;
	}

	/** Singleton configuration. */
	private static volatile Configuration configuration;
}
