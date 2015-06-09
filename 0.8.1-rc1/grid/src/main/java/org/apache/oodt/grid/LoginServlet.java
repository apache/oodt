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
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller servlet that authenticates the administrator password.
 * 
 */
public class LoginServlet extends GridServlet {
	/**
	 * Handle authentication from an administrator.
	 *
	 * @param req a <code>HttpServletRequest</code> value.
	 * @param res a <code>HttpServletResponse</code> value.
	 * @throws ServletException if an error occurs.
	 * @throws IOException if an error occurs.
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		Configuration config = getConfiguration();			       // Get configuration
		if (!approveAccess(config, req, res)) return;			       // Do https, localhost checking first

		ConfigBean cb = getConfigBean(req);				       // Get bean
		if (cb.isAuthentic()) {						       // Already authentic?
			req.getRequestDispatcher("config.jsp").forward(req, res);      // Back to the config page with you!
			return;
		}

		String password = req.getParameter("password");			       // Get submitted password
		if (password == null) password = "";				       // If none, use an empty string
		byte[] bytes = password.getBytes();				       // Get the bytes
		if (!Arrays.equals(config.getPassword(), bytes)) {		       // Compare to stored password bytes 
			cb.setMessage("Password incorrect");			       // Not equal!  Set message.
			throw new ServletException(new AuthenticationRequiredException());
		} else {
			cb.setMessage("");					       // Equal, clear message
			cb.setAuthentic(true);					       // You are now authenticated
			req.getRequestDispatcher("config.jsp").forward(req, res);      // To the config page with you!
		}
	}
}
