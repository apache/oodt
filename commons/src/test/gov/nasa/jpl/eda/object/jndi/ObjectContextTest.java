// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ObjectContextTest.java,v 1.2 2005-07-19 18:54:31 kelly Exp $

package jpl.eda.object.jndi;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.naming.Binding;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import jpl.eda.object.jndi.ObjectContext;
import junit.framework.TestCase;

/**
 * Unit test for {@link ObjectContext}.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public class ObjectContextTest extends TestCase {
	/**
	 * Creates a new <code>ObjectContextTest</code> instance.
	 *
	 * @param caseName a <code>String</code> value.
	 */
	public ObjectContextTest(String caseName) {
		super(caseName);
	}

	public void setUp() throws Exception {
		super.setUp();

		aliasFile = File.createTempFile("test", ".properties");
		aliasFile.deleteOnExit();
		Properties aliases = new Properties();
		aliases.setProperty("urn:alias:x", "urn:a:x");
		FileOutputStream out = new FileOutputStream(aliasFile);
		aliases.save(out, "Temporary properties");
		out.close();

		a1 = new TestContext("urn:a");
		a2 = new TestContext("urn:a");
		b = new TestContext("urn:b");

		oldValue = System.getProperty("jpl.eda.object.jndi.aliases");
		System.setProperty("jpl.eda.object.jndi.aliases", aliasFile.toString());

		List contexts = new ArrayList();
		contexts.add(a1);
		contexts.add(a2);
		contexts.add(b);
		context = new ObjectContext(contexts);
	}

	public void tearDown() throws Exception {
		aliasFile.delete();
		if (oldValue == null)
			System.getProperties().remove("jpl.eda.object.jndi.aliases");
		else
			System.setProperty("jpl.eda.object.jndi.aliases", oldValue);
		super.tearDown();
	}

	/**
	 * Test various aspects of the object context.
	 *
	 * @throws NamingException if an error occurs.
	 */
	public void testObjectContext() throws NamingException {
		// Test retrieval of nonexistent bindings
		try {
			context.lookup("urn:a:x");
			fail("Found nonexistent object");
		} catch (NamingException ex) {}

		// Test binding names that don't match any delegate's namespace prefix.
		try {
			context.bind("urn:c:x", this);
			fail("Bound nonconforming name");
		} catch (NamingException ex) {}

		// Test binding and retrieval
		context.bind("urn:a:x", this);					       // Bind something
		assertSame(this, context.lookup("urn:a:x"));			       // Look it up
		assertSame(this, context.lookup("urn:alias:x"));		       // Try the alias
		assertTrue(a1.bindings.values().contains(this));		       // It should be in both a1...
		assertTrue(a2.bindings.values().contains(this));		       // ...and a2
		assertTrue(!b.bindings.values().contains(this));		       // But not b.

		context.bind("urn:b:x", getClass());				       // Now bind something for b
		assertSame(getClass(), context.lookup("urn:b:x"));		       // Look it up
		assertTrue(!a1.bindings.values().contains(getClass()));		       // It should not be in a1...
		assertTrue(!a2.bindings.values().contains(getClass()));		       // ...nor a2
		assertTrue(b.bindings.values().contains(getClass()));		       // But should be in b.

		// Test binding a bound name
		try {
			context.bind("urn:a:x", "");
			fail("Able to bind an already-bound name");
		} catch (NamingException ex) {}

		// Test rebinding a bound name
		context.rebind("urn:a:x", context);				       // Bind to a different object
		assertSame(context, context.lookup("urn:a:x"));			       // Look it up
		assertTrue(!a1.bindings.values().contains(this));		       // The old object should be gone from a1...
		assertTrue(!a2.bindings.values().contains(this));		       // ...and from a2
		assertTrue(!b.bindings.values().contains(context));		       // And the new object isn't in b

		// Test renaming
		context.rename("urn:a:x", "urn:a:y");				       // Change x to y
		try {
			context.lookup("urn:a:x");				       // Look it up
			fail("Found object under old name");
		} catch (NamingException ex) {}
		assertSame(context, context.lookup("urn:a:y"));			       // Just the name has changed
		assertTrue(a1.bindings.keySet().contains("urn:a:y"));		       // The new name is in a1
		assertTrue(!a1.bindings.keySet().contains("urn:a:x"));		       // But not the old
		assertTrue(a2.bindings.keySet().contains("urn:a:y"));		       // The new name is in a2
		assertTrue(!a2.bindings.keySet().contains("urn:a:x"));		       // But not the old
		assertTrue(!b.bindings.values().contains(context));		       // It was never in b

		// Test listing
		int count = 0;
		boolean sawA = false;
		boolean sawB = false;
		NamingEnumeration e = context.list("");
		while (e.hasMore()) {
			NameClassPair p = (NameClassPair) e.next();
			if ("urn:a:y".equals(p.getName()) && context.getClass().getName().equals(p.getClassName()))
				sawA = true;
			else if ("urn:b:x".equals(p.getName()) && "java.lang.Class".equals(p.getClassName()))
				sawB = true;
			else
				fail("Unexpected binding \"" + p.getName() + "\" to " + p.getClassName());
			++count;
		}
		assertEquals(3, count);
		assertTrue(sawA);
		assertTrue(sawB);

		// Test listing of bindings
		count = 0;
		sawA = false;
		sawB = false;
		e = context.listBindings("");
		while (e.hasMore()) {
			Binding b = (Binding) e.next();
			if ("urn:a:y".equals(b.getName()) && context == b.getObject())
				sawA = true;
			else if ("urn:b:x".equals(b.getName()) && getClass() == b.getObject())
				sawB = true;
			else
				fail("Unexpected binding \"" + b.getName() + "\" to " + b.getObject());
			++count;
		}
		assertEquals(3, count);
		assertTrue(sawA);
		assertTrue(sawB);
			
		// Test unbinding
		context.unbind("urn:a:y");					       // Unbind it
		try {
			context.lookup("urn:a:y");				       // Look it up
			fail("Found unbound object");
		} catch (NamingException ex) {}
		assertTrue(a1.bindings.isEmpty());				       // It's not in a1...
		assertTrue(a2.bindings.isEmpty());				       // ...nor in a2
	}

	/** First delegate context for "urn:a" namespace. */
	private TestContext a1;

	/** Second delegate context for "urn:a" namespace. */
	private TestContext a2;

	/** Delegate context for "urn:b" namespace. */
	private TestContext b;

	/** Test subject: the object context. */
	private ObjectContext context;

	/** Test alias file. */
	private File aliasFile;

	/** Old value of sys prop jpl.eda.object.jndi.aliases. */
	private String oldValue;
}
