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
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oodt.xmlquery.XMLQuery;

/**
 * The {@link RestfulProductQueryServlet} is an alternative to the standard {@link ProductQueryServlet} 
 * that allows clients to use a more intuitive syntax for encoding query constraints, than the traditional
 * DIS-style syntax. 
 * For example, instead of encoding a request as: 
 * "?q=identifier+EQ+urn:nasa:pds:phx_lidar:reduced:LS075RLS_00902835894_1885M1+AND+package+EQ+TGZ"
 * a client could encode it as:
 * "?identifier=urn:nasa:pds:phx_lidar:reduced:LS075RLS_00902835894_1885M1&package=TGZ".
 * Note that this servlet is meant to be back-ward compatible, i.e. it will first process a request by
 * parsing the "xmlq=" and "q=" parameters. If those are not found, it will build a request by combining 
 * all the available HTTP parameters in logical AND.
 * Note also that this servlet is NOT enabled by default 
 * (i.e. it must be explicitly configured by changing the web-grid deployment descriptor web.xml).
 * 
 * @author Luca Cinquini
 *
 */
public class RestfulProductQueryServlet extends ProductQueryServlet {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Overridden implementation that defaults to the standard behavior if the parameters "q" or "xmlq" are found,
	 * otherwise it uses the available request parameters to build a constraint query with logical AND.
	 */
	@Override
	protected XMLQuery getQuery(HttpServletRequest req, HttpServletResponse res) throws IOException {
		
		// if DIS-style parameters are found, default to standard processing
		if (req.getParameter("xmlq") !=null || req.getParameter("q")!=null) {
		  return super.getQuery(req, res);
			
		// combine all HTTP (name, value) pairs into XML query string with logical AND
		} else {
			
			StringBuilder q = new StringBuilder("");
			Enumeration<String> parameterNames = req.getParameterNames();
			while (parameterNames.hasMoreElements()) {
				String paramName = parameterNames.nextElement();
				String[] paramValues = req.getParameterValues(paramName);
			  for (String paramValue : paramValues) {
				if (q.length() > 0) {
				  q.append(" AND ");
				}
				q.append(paramName).append(" EQ ").append(paramValue);
			  }
			}
			
			// build XMLQuery object from HTTP parameters
			// no need to URL-encode since this request doesn't go over the network
			System.out.println("Executing query="+q.toString());
			return new XMLQuery(q.toString(), "wgq", "Web Grid Query",	
					"Query from Web-Grid", /*ddID*/null,                   
					/*resultModeId*/null, /*propType*/null,                
					/*propLevels*/null, /*maxResults*/Integer.MAX_VALUE,   
					new ArrayList<String>(), true);			
		}
		
	}

}
