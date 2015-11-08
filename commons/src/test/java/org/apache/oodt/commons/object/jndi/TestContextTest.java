// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.commons.object.jndi;

import javax.naming.Binding;
import javax.naming.InvalidNameException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import junit.framework.TestCase;

/**
 * Unit test for the test context.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class TestContextTest extends TestCase {
	/**
	 * Creates a new <code>TestContextTest</code> instance.
	 *
	 * @param caseName a <code>String</code> value.
	 */
	public TestContextTest(String caseName) {
		super(caseName);
	}

	/**
	 * Test various aspects of the test context.
	 *
	 * @throws NamingException if an error occurs.
	 */
	public void testTestContext() throws NamingException {
		TContext ctx = new TContext("urn:x:");			       // Make it
		try {
			ctx.lookup("urn:x:y");					       // Lookup a nonexistent binding
			fail("Got a binding that doesn't exist");		       // Got something?  Yikes.
		} catch (NameNotFoundException ignored) {}

		try {
			ctx.bind("urn:y:z", this);				       // Bind an invalid prefix
			fail("Bound an invalid prefix");			       // Worked?  Dang.
		} catch (InvalidNameException ignored) {}

		ctx.bind("urn:x:a", this);					       // Bind something.
		assertSame(this, ctx.lookup("urn:x:a"));			       // Look it up

		try {
			ctx.bind("urn:x:a", getClass());			       // Bind it again
			fail("Able to re-bind");				       // Worked?  Crap.
		} catch (NameAlreadyBoundException ignored) {}

		ctx.rebind("urn:x:a", getClass());				       // Rebind it again
		assertSame(getClass(), ctx.lookup("urn:x:a"));			       // Look it up

		ctx.rename("urn:x:a", "urn:x:b");				       // Rename the binding
		assertSame(getClass(), ctx.lookup("urn:x:b"));			       // Look it up

		NamingEnumeration e = ctx.list("");				       // List the context
		assertTrue(e.hasMore());					       // Got something?  Good.
		NameClassPair p = (NameClassPair) e.next();			       // Get it
		assertEquals("urn:x:b", p.getName());				       // Right name?  Good.
		assertEquals("java.lang.Class", p.getClassName());		       // Right class?  Good.
		assertTrue(!e.hasMore());					       // Got no more?  Good.

		e = ctx.listBindings("");					       // List the bindings
		assertTrue(e.hasMore());					       // Got something?  Good.
		Binding b = (Binding) e.next();					       // Get it
		assertEquals("urn:x:b", p.getName());				       // Right name?  Good.
		assertSame(getClass(), b.getObject());				       // Right object?  Good.
		assertTrue(!e.hasMore());					       // Got no more?  Good.

		assertSame(getClass(), ctx.lookupLink("urn:x:b"));		       // Look up via the link

		ctx.unbind("urn:x:b");						       // Unbind it
		e = ctx.list("");						       // List the context
		assertTrue(!e.hasMore());					       // Got no more?  Good.
		e = ctx.listBindings("");					       // List the bindings
		assertTrue(!e.hasMore());					       // Got no more?  Good.
	}
}
