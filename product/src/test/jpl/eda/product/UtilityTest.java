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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jpl.eda.xmlquery.XMLQuery;
import junit.framework.TestCase;

/**
 * Unit test for the Utility class.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class UtilityTest extends TestCase {
	/**
	 * Creates a new {@link UtilityTest} instance.
	 *
	 * @param name Case name.
	 */
	public UtilityTest(String name) {
		super(name);
	}

	/**
	 * Set up by saving old values of properties this test alters and clearing them for the case.
	 *
	 * @throws Exception if an error occurs.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		for (int i = 0; i < PROPS.length; ++i) {
			props.put(PROPS[i], System.getProperty(PROPS[i]));
			System.getProperties().remove(PROPS[i]);
		}
	}

	/**
	 * Tear down by restoring old values of properties this test altered.
	 *
	 * @throws Exception if an error occurs.
	 */
	protected void tearDown() throws Exception {
		for (Iterator i = props.entrySet().iterator(); i.hasNext();) {
			Map.Entry e = (Map.Entry) i.next();
			if (e.getValue() == null)
				System.getProperties().remove(e.getKey());
			else
				System.getProperties().put(e.getKey(), e.getValue());
		}
		super.tearDown();
	}

	/**
	 * Test the loading of query handlers.
	 */
	public void testHandlerLoading() {
		System.setProperty("jpl.eda.product.handlers", "");
		List handlers = Utility.loadHandlers("urn:eda:rmi:TestServer");
		assertTrue(handlers.isEmpty());
		System.getProperties().remove("jpl.eda.product.handlers");
		System.setProperty("handlers", "");
		handlers = Utility.loadHandlers("urn:eda:rmi:TestServer");
		assertTrue(handlers.isEmpty());
		System.getProperties().remove("handlers");
		handlers = Utility.loadHandlers("urn:eda:rmi:TestServer");
		assertTrue(handlers.isEmpty());

		System.setProperty("handlers", "non.existent.class");
		handlers = Utility.loadHandlers("urn:eda:rmi:TestServer");
		assertTrue(handlers.isEmpty());

		System.setProperty("handlers", "jpl.eda.product.UtilityTest$Handler1,jpl.eda.product.UtilityTest$Handler2");
		handlers = Utility.loadHandlers("urn:eda:rmi:TestServer");
		assertEquals(2, handlers.size());
		boolean saw1 = false;
		boolean saw2 = false;
		for (Iterator i = handlers.iterator(); i.hasNext();) {
			Object obj = i.next();
			if (obj instanceof Handler1) saw1 = true;
			else if (obj instanceof Handler2) saw2 = true;
			else fail("Unexpected object in handler list");
		}
		assertTrue(saw1 && saw2);

		System.setProperty("urn:eda:rmi:TestServer.handlers", "jpl.eda.product.UtilityTest$Handler2");
		handlers = Utility.loadHandlers("urn:eda:rmi:TestServer");
		assertEquals(1, handlers.size());
		assertTrue(handlers.get(0) instanceof Handler2);
	}

	/** Saved system property values.  Keys are String (prop name) and values are String (prop value). */
	private Map props = new HashMap();

	/**
	 * Test query handler.
	 */
	public static class Handler1 implements QueryHandler {
		public XMLQuery query(XMLQuery q) { return null; }
	}

	/**
	 * Another test query handler.
	 */
	public static class Handler2 implements QueryHandler {
		public XMLQuery query(XMLQuery q) { return null; }		
	}

	/** Properties this test alters and needs to restore when it's done. */
	private static final String[] PROPS = { "urn:eda:rmi:TestServer.handlers", "jpl.eda.product.handlers", "handlers" };
}
