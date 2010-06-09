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


package jpl.eda.product;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Product utilities.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public class Utility {
	/**
	 * Load query handlers.
	 *
	 * @param name Name under which server is registered
	 * @return A {@link List} of {@link QueryHandler}s.
	 */
	public static List loadHandlers(String name) {
		String qh = System.getProperty(name + ".handlers", System.getProperty("jpl.eda.product.handlers",
			System.getProperty("handlers","")));
		List handlers = new ArrayList();
		for (Iterator i = jpl.eda.util.Utility.parseCommaList(qh); i.hasNext();) {
			String className = (String) i.next();
			try {
				Class clazz = Class.forName(className);
				QueryHandler handler = (QueryHandler) clazz.newInstance();
				handlers.add(handler);
			} catch (ClassNotFoundException ex) {
				System.err.println("Query handler \"" + className + "\" not found; skipping it");
			} catch (InstantiationException ex) {
				System.err.println("Query handler \"" + className + "\" is abstract or has no null c'tor");
			} catch (IllegalAccessException ex) {
				System.err.println("Query handler \"" + className + "\" is not public");
			}
		}
		return handlers;
	}

	/**
	 * Get the correct RMI port to use.
	 *
	 * @param className Name of remote object; used solely for logging.
	 * @return Port number.
	 */
	public static int getRMIPort(String className) {
		int port = Integer.getInteger("jpl.eda.product.port",
			Integer.getInteger("jpl.eda.product.ProductServiceImpl.port", 0)).intValue();
		return port;
	}

	/**
	 * Utility class; do not construct.
	 */
	private Utility() {
		throw new IllegalStateException("Class has \u00abutility\u00bb stereotype");
	}
}
