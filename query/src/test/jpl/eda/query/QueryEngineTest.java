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
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import jpl.eda.product.ProductService;
import jpl.eda.profile.Profile;
import jpl.eda.profile.ProfileService;
import jpl.eda.xmlquery.XMLQuery;
import junit.framework.TestCase;

/**
 * Unit test for {@link QueryEngine}.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public class QueryEngineTest extends TestCase {
	/**
	 * Creates a new {@link QueryEngineTest} instance.
	 *
	 * @param caseName Case name.
	 */
	public QueryEngineTest(String caseName) {
		super(caseName);
	}

	/**
	 * Set up the unit test.  To do so, we set up a special JNDI context that yields
	 * test profile and product servers.
	 *
	 * @throws Exception if an error occurs.
	 */
	public void setUp() throws Exception {
		super.setUp();
		oldContexts = System.getProperty("jpl.eda.object.contexts");
		System.setProperty("jpl.eda.object.contexts", "jpl.eda.query.QueryEngineTest$TestContext");
		oldDefaultServers = System.getProperty("jpl.eda.query.profileServers");
		System.setProperty("jpl.eda.query.profileServers", "urn:profile:a");
		oldRMIRegistries = System.getProperty("jpl.eda.rmiregistries");
		System.setProperty("jpl.eda.rmiregistries", "");
		qe = new QueryEngine();
	}

	/**
	 * Test if various illegal arguments are properly rejected. 
	 *
	 * @throws QueryException if an error occurs.
	 */
	public void testIllegalArguments() throws QueryException {
		try {
			qe.queryProfileServers(/*query*/null, Collections.EMPTY_LIST);
			fail("null queries work");
		} catch (IllegalArgumentException ex) {}

		try {
			qe.queryProfileServers(createQuery("a = b"), null);
			fail("null profile servers work");
		} catch (IllegalArgumentException ex) {}

		List results = qe.queryProfileServers(createQuery("a = b"), Collections.EMPTY_LIST);
		assertNotNull(results);
		assertTrue(results.isEmpty());

		try {
			qe.queryDefaultProfileServers(/*query*/null);
			fail("null queries work");
		} catch (IllegalArgumentException ex) {}

		try {
			qe.queryProductServer(/*query*/null, "urn:product:a");
			fail("null queries work");
		} catch (IllegalArgumentException ex) {}

		try {
			qe.queryProductServer(createQuery("a = b"), null);
			fail("null product server works");
		} catch (IllegalArgumentException ex) {}

		try {
			qe.retrieveChunk(/*productID*/null, 0, 10, "urn:product:a");
			fail("null product ID works");
		} catch (IllegalArgumentException ex) {}

		try {
			qe.retrieveChunk("id", -5, 10, "urn:product:a");
			fail("negative offset works");
		} catch (IllegalArgumentException ex) {}

		try {
			qe.retrieveChunk("id", 10, -5, "urn:product:a");
			fail("negative size works");
		} catch (IllegalArgumentException ex) {}

		try {
			qe.retrieveChunk("id", 10, 0, "urn:product:a");
			fail("zero size works");
		} catch (IllegalArgumentException ex) {}

		try {
			qe.retrieveChunk("id", 10, 10, /*serverID*/null);
			fail("null server ID works");
		} catch (IllegalArgumentException ex) {}

		try {
			qe.close(/*productID*/null, "urn:product:a");
			fail("null product ID works");
		} catch (IllegalArgumentException ex) {}

		try {
			qe.close("id", /*serverID*/null);
			fail("null product ID works");
		} catch (IllegalArgumentException ex) {}
	}

	/**
	 * Test profile server queries.
	 *
	 * @throws QueryException if an error occurs.
	 */
	public void testProfileServerQueries() throws QueryException {
		XMLQuery q;
		List results;

		q = createQuery("follow = false");
		results = qe.queryProfileServers(q, Arrays.asList(new String[]{ "urn:profile:a", "urn:profile:b" }));
		assertNotNull(results);
		assertEquals(2, results.size());

		q = createQuery("follow = true");
		results = qe.queryProfileServers(q, Arrays.asList(new String[]{ "urn:profile:a", "urn:profile:b" }));
		assertNotNull(results);
		assertEquals(2, results.size());

		q = createQuery("follow = true");
		results = qe.queryProfileServers(q, Collections.singletonList("urn:profile:a"));
		assertNotNull(results);
		assertEquals(2, results.size());

		q = createQuery("follow = false");
		results = qe.queryProfileServers(q, Collections.singletonList("urn:profile:a"));
		assertNotNull(results);
		assertEquals(1, results.size());
	}

	/**
	 * Test profile server queries to the default profile servers.
	 *
	 * @throws QueryException if an error occurs.
	 */
	public void testDefaultProfileServerQueries() throws QueryException {
		XMLQuery q;
		List results;

		q = createQuery("follow = false");
		results = qe.queryDefaultProfileServers(q);
		assertNotNull(results);
		assertEquals(1, results.size());

		q = createQuery("follow = true");
		results = qe.queryDefaultProfileServers(q);
		assertNotNull(results);
		assertEquals(2, results.size());
	}

	/**
	 * Tet product server queries.
	 *
	 * @throws QueryException if an error occurs.
	 */
	public void testProductServerQueries() throws QueryException {
		currentTest = this;
		XMLQuery q = createQuery("a = b");
		qe.queryProductServer(q, "urn:product");
		qe.retrieveChunk("a", 5, 10, "urn:product");
		qe.close("a", "urn:product");
		assertTrue(productQueryCalled);
		assertTrue(retrieveChunkCalled);
		assertTrue(closeCalled);
	}

	/**
	 * Clean up the system properties overwritten for this test.
	 *
	 * @throws Exception if an error occurs.
	 */
	public void tearDown() throws Exception {
		if (oldContexts == null)
			System.getProperties().remove("jpl.eda.object.contexts");
		else
			System.setProperty("jpl.eda.object.contexts", oldContexts);
		if (oldDefaultServers == null)
			System.getProperties().remove("jpl.eda.query.profileServers");
		else
			System.setProperty("jpl.eda.query.profileServers", oldDefaultServers);
		if (oldRMIRegistries == null)
			System.getProperties().remove("jpl.eda.rmiregistries");
		else
			System.setProperty("jpl.eda.rmiregistries", oldRMIRegistries);
		super.tearDown();
	}

	/** Saved <code>jpl.eda.object.context</code> property. */
	private String oldContexts;

	/** Saved <code>jpl.eda.query.profileServers</code> property. */
	private String oldDefaultServers;

	/** Saved <code>jpl.eda.rmiregistries</code> property. */
	private String oldRMIRegistries;

	/** True if the product query method got called. */
	private boolean productQueryCalled;

	/** True if the retrieveChunk method got called. */
	private boolean retrieveChunkCalled;

	/** True if the close method got called. */
	private boolean closeCalled;

	/** Query engine being tested. */
	private QueryEngine qe;

	/** Current test instance being run. */
	private static QueryEngineTest currentTest;

	/**
	 * JNDI context that yields our test profile and product "servers".
	 */
	public static class TestContext implements Context {
		public Object lookup(Name name) { return null; }
		public Object lookup(String name) throws NameNotFoundException {
			if ("urn:profile:a".equals(name))
				return new ProfileAdaptor("a");
			else if ("urn:profile:b".equals(name))
				return new ProfileAdaptor("b");
			else if ("urn:product".equals(name))
				return new ProductAdaptor();
			else
				throw new NameNotFoundException(name);
		}
		public void bind(Name name, Object obj) {}
		public void bind(String name, Object obj) {}
		public void rebind(Name name, Object obj) {}
		public void rebind(String name, Object obj) {}
		public void unbind(Name name) {}
		public void unbind(String name) {}
		public void rename(Name o, Name n) {}
		public void rename(String o, String n) {}
		public NamingEnumeration list(Name n) { return null; }
		public NamingEnumeration list(String n) { return null; }
		public NamingEnumeration listBindings(Name n) { return null; }
		public NamingEnumeration listBindings(String n) { return null; }
		public void destroySubcontext(Name name) {}
		public void destroySubcontext(String name) {}
		public Context createSubcontext(Name name) { return null; }
		public Context createSubcontext(String name) { return null; }
		public Object lookupLink(Name name) { return null; }
		public Object lookupLink(String name) { return null; }
		public NameParser getNameParser(Name name) { return null; }
		public NameParser getNameParser(String name) { return null; }
		public Name composeName(Name n, Name p) { return null; }
		public String composeName(String n, String p) { return null; }
		public Object addToEnvironment(String k, Object v) { return null; }
		public Object removeFromEnvironment(String k) { return null; }
		public Hashtable getEnvironment() { return null; }
		public void close() {}
		public String getNameInNamespace() { return null; }
		private QueryEngineTest qet;
	}

	/**
	 * Profile server for testing.
	 *
	 * This profile server yields a single matching profile for every query.  And for
	 * a query that looks like <code>follow = true</code> and is coming to the profile
	 * server <code>urn:profile:a</code>, it adds a second profile that describes
	 * another profile server (<code>urn:profile:b</code>).
	 */
	private static class TestProfileServer implements jpl.eda.profile.Server {
		/**
		 * Creates a new {@link TestProfileServer} instance.
		 *
		 * @param id <code>a</code> or <code>b</code>.
		 */
		TestProfileServer(String id) {
			this.id = id;
		}

		public List query(XMLQuery q) {
			List results = new ArrayList();
			if ("follow = true".equals(q.getKwdQueryString()) && "a".equals(id)) {
				Profile p = new Profile();
				p.getResourceAttributes().setResClass("system.profileServer");
				p.getResourceAttributes().getResLocations().add("urn:profile:b");
				results.add(p);
			}

			results.add(new Profile());
			return results;
		}

		public Profile getProfile(String id) {
			return null;
		}
		
		public void add(String profileStr){}

		public  boolean remove(String profId, String version){
                        return false;
                }
                
                public  void replace(String profileStr){}

		/** <code>a</code> or <code>b</code>. */
		private String id;
	}

	/**
	 * Adaptor that turns the test profile service into the generic profile service.
	 */
	public static class ProfileAdaptor implements ProfileService {
		public ProfileAdaptor(String id) {
			this.id = id;
		}
		public jpl.eda.profile.Server createServer() {
			return new TestProfileServer(id);
		}
		private String id;
	}

	/**
	 * Test product server that merely notes when its methods get called.
	 */
	private static class TestProductServer implements jpl.eda.product.Server {
		public XMLQuery query(XMLQuery q) {
			currentTest.productQueryCalled = true;
			return q;
		}
		public byte[] retrieveChunk(String productID, long offset, int length) {
			currentTest.retrieveChunkCalled = true;
			return null;
		}
		public void close(String productID) {
			currentTest.closeCalled = true;
		}
	}

	/**
	 * Adaptor that turns the test product service into the generic product service.
	 */
	public static class ProductAdaptor implements ProductService {
		public jpl.eda.product.Server createServer() {
			return new TestProductServer();
		}
	}

	/**
	 * Create an XML query from the given expression, leaving all other characteristics at their defaults.
	 *
	 * @param expr Query expression.
	 * @return a {@link XMLQuery} value.
	 */
	private static XMLQuery createQuery(String expr) {
		return new XMLQuery(expr, /*id*/null, /*title*/null, /*desc*/null, /*ddId*/null, /*resultModeId*/null,
			/*propType*/null, /*propLevels*/null, /*maxResults*/999);
	}
}	
