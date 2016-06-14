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

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import org.apache.oodt.commons.util.Utility;

/**
 * Context for binding and looking up distributed objects.
 *
 * @author Kelly
 * @version $Revision: 1.5 $
 */
class ObjectContext implements Context {
	/**
	 * Construct the object context.
	 *
	 * @param environment Its environment, currently unused.
	 */
	ObjectContext(Hashtable environment) {
		this.environment = environment != null? (Hashtable) environment.clone() : new Hashtable();

		// Add the CORBA context, but by name so we don't get a compile-time
		// coupling with the edm-corba component, and only if CORBA's available.
		try {
			Class clazz = Class.forName("org.apache.oodt.commons.object.jndi.CORBAContext");
			Constructor ctor = clazz.getConstructor(new Class[]{Hashtable.class});
			Object corbaContext = ctor.newInstance(new Object[]{this.environment});
			contexts.add(corbaContext);
		} catch (Throwable ignored) {}

		String registryList = (String) (environment != null ? environment.get("rmiregistries") : null);
		if (registryList != null) {
		  for (Iterator i = Utility.parseCommaList(registryList); i.hasNext(); ) {
			Hashtable rmiEnv = (Hashtable) this.environment.clone();
			URI uri = URI.create((String) i.next());
			rmiEnv.put("host", uri.getHost());
			rmiEnv.put("port", uri.getPort());
			contexts.add(new RMIContext(rmiEnv));
		  }
		}

		Hashtable httpEnv = (Hashtable) this.environment.clone();
		contexts.add(new HTTPContext(httpEnv));

		String className = null;
		for (Iterator i = org.apache.oodt.commons.util.Utility.parseCommaList(System.getProperty("org.apache.oodt.commons.object.contexts", ""));
		        i.hasNext();) {
		  try {
			className = (String) i.next();
			Class clazz = Class.forName(className);
			contexts.add(clazz.newInstance());
		  } catch (ClassNotFoundException ex) {
			System.err.println("Ignoring not-found context class `" + className + "': " + ex.getMessage());
		  } catch (InstantiationException ex) {
			System.err.println("Ignoring non-instantiable context class `" + className + "': " + ex.getMessage());
		  } catch (IllegalAccessException ex) {
			System.err.println("Ignoring context class `" + className + "' with non-accessible no-args c'tor: "
							   + ex.getMessage());
		  }
		}

		installAliases();
		System.err.println("Object context ready; delegating to: " + contexts);
	}

	/**
	 * Creates a new <code>ObjectContext</code> instance.  This constructor takes a
	 * list of delegate contexts instead of building them from a passed-in
	 * environment.  Currently, it's used solely for this class's {@link
	 * ObjectContextTest unit test}.
	 *
	 * @param contexts a <code>List</code> of {@link Context}s.
	 */
	ObjectContext(List contexts) {
		this.contexts = contexts;
		installAliases();
	}

	/**
	 * Returns the object to which the given name is bound.  Because this context
	 * delegates to multiple other contexts, the lookup returns the first successful
	 * match.
	 *
	 * @param name a <code>String</code> value.
	 * @return an <code>Object</code> value.
	 * @throws NamingException if an error occurs.
	 */
	public Object lookup(String name) throws NamingException {
		if (name == null) {
		  throw new IllegalArgumentException("Name required");
		}
		if (name.length() == 0) {
		  return this;
		}

		// Let alias redirection do its magic
		String alias = aliases.getProperty(name);
		if (alias != null) {
		  name = alias;
		}

	  for (Object context : contexts) {
		Context c = (Context) context;
		try {
		  return c.lookup(name);
		} catch (InvalidNameException ignore) {
		} catch (NameNotFoundException ignore) {
		} catch (NamingException ignore) {
		}
	  }
		throw new NameNotFoundException(name + " not found in any managed subcontext");
	}

	public Object lookup(Name name) throws NamingException {
		return lookup(name.toString());
	}

	public synchronized void bind(String name, Object obj) throws NamingException {
		if (name == null) {
		  throw new IllegalArgumentException("Name required");
		}
		if (name.length() == 0) {
		  throw new InvalidNameException("Cannot bind object named after context");
		}

		// If it's an alias name, stop here.
		if (aliases.containsKey(name)) {
		  throw new NameAlreadyBoundException("Name \"" + name + "\" already bound as an aliased name");
		}

		// Make sure it isn't bound anywhere
		for (NamingEnumeration e = list(""); e.hasMore();) {
			NameClassPair nameClassPair = (NameClassPair) e.next();
			if (name.equals(nameClassPair.getName())) {
			  throw new NameAlreadyBoundException("Name \"" + name + "\" already bound by a managed subcontext");
			}
		}
		doRebind(name, obj);
	}

	public void bind(Name name, Object obj) throws NamingException {
		bind(name.toString(), obj);
	}

	/** {@inheritDoc} */
	public synchronized void rebind(String name, Object obj) throws NamingException {
		if (name == null) {
		  throw new IllegalArgumentException("Name required");
		}
		if (name.length() == 0) {
		  throw new InvalidNameException("Cannot rebind object named after context");
		}

		// If it's an alias name, remove the alias
		if (aliases.containsKey(name)) {
		  aliases.remove(name);
		}

		doRebind(name, obj);
	}

	/**
	 * Rebind the given name to the given object.
	 *
	 * @param name Name to rebind
	 * @param obj Object to which it's bound
	 * @throws NamingException if an error occurs.
	 */
	private void doRebind(String name, Object obj) throws NamingException {
		boolean bound = false;
	  for (Object context : contexts) {
		Context c = (Context) context;
		try {
		  c.rebind(name, obj);
		  bound = true;
		} catch (NamingException ignored) {
		}
	  }
		if (!bound) {
		  throw new InvalidNameException("Name \"" + name + "\" not compatible with any managed subcontext");
		}
	}

	public void rebind(Name name, Object obj) throws NamingException {
		rebind(name.toString(), obj);
	}

	public void unbind(String name) throws NamingException {
		if (name == null) {
		  throw new IllegalArgumentException("Name required");
		}
		if (name.length() == 0) {
		  throw new InvalidNameException("Cannot unbind object named after context");
		}

		// See if it's an aliased name
		if (aliases.containsKey(name)) {
			aliases.remove(name);
			return;
		}

		boolean unbound = false;
	  for (Object context : contexts) {
		Context c = (Context) context;
		try {
		  c.unbind(name);
		  unbound = true;
		} catch (NamingException ignore) {
		}
	  }
		if (!unbound) {
		  throw new InvalidNameException("Name \"" + name + "\" not compatible with any managed subcontext");
		}
	}

	public void unbind(Name name) throws NamingException {
		unbind(name.toString());
	}

	public void rename(String oldName, String newName) throws NamingException {
		if (oldName == null || newName == null) {
		  throw new IllegalArgumentException("Name required");
		}
		if (oldName.length() == 0 || newName.length() == 0) {
		  throw new InvalidNameException("Cannot rename object named after context");
		}

		// See if it's an aliased name
		String oldValue = (String) aliases.remove(oldName);
		if (oldValue != null) {
			aliases.setProperty(newName, oldName);
			return;
		}

		boolean renamed = false;
	  for (Object context : contexts) {
		Context c = (Context) context;
		try {
		  c.rename(oldName, newName);
		  renamed = true;
		} catch (NamingException ignore) {
		}
	  }
		if (!renamed) {
		  throw new InvalidNameException("Names not compatible with any managed subcontext");
		}
	}

	public void rename(Name oldName, Name newName) throws NamingException {
		rename(oldName.toString(), newName.toString());
	}

	public NamingEnumeration list(final String name) throws NamingException {
		final Iterator eachContext = contexts.iterator();
		return new NamingEnumeration() {
			private NamingEnumeration enumeration
				= eachContext.hasNext()? ((Context) eachContext.next()).list(name) : null;
			private boolean open = true;
			public Object next() throws NamingException {
				if (!open) {
				  throw new NamingException("closed");
				}
				if (enumeration != null && enumeration.hasMore()) {
				  return enumeration.next();
				} else if (eachContext.hasNext()) {
					enumeration = ((Context) eachContext.next()).list(name);
					if (enumeration.hasMore()) {
					  return enumeration.next();
					}
				}
				throw new NoSuchElementException("No more objects in context");
			}
			public Object nextElement() {
				Object rc = null;
				try {
					rc = next();
				} catch (NamingException ignore) {}
				return rc;
			}
			public boolean hasMore() throws NamingException {
				if (!open) {
				  return false;
				}
				if (enumeration == null) {
				  return false;
				} else if (enumeration.hasMore()) {
				  return true;
				} else if (eachContext.hasNext()) {
					enumeration = ((Context) eachContext.next()).list(name);
					return hasMore();
				}
				return false;
			}
			public boolean hasMoreElements() {
				boolean h = false;
				try {
					h = hasMore();
				} catch (NamingException ignore) {}
				return h;
			}
			public void close() throws NamingException {
				open = false;
				if (enumeration != null) {
				  enumeration.close();
				}
			}
		};
	}
		
	public NamingEnumeration list(Name name) throws NamingException {
		return list(name.toString());
	}

	public NamingEnumeration listBindings(final String name) throws NamingException {
		final Iterator eachContext = contexts.iterator();
		return new NamingEnumeration() {
			private NamingEnumeration enumeration
				= eachContext.hasNext()? ((Context) eachContext.next()).listBindings(name) : null;
			private boolean open = true;
			public Object next() throws NamingException {
				if (!open) {
				  throw new NamingException("closed");
				}
				if (enumeration != null && enumeration.hasMore()) {
				  return enumeration.next();
				} else if (eachContext.hasNext()) {
					enumeration = ((Context) eachContext.next()).listBindings(name);
					if (enumeration.hasMore()) {
					  return enumeration.next();
					}
				}
				throw new NoSuchElementException("No more objects in context");
			}
			public Object nextElement() {
				Object rc = null;
				try {
					rc = next();
				} catch (NamingException ignore) {}
				return rc;
			}
			public boolean hasMore() throws NamingException {
				if (!open) {
				  return false;
				}
				if (enumeration == null) {
				  return false;
				} else if (enumeration.hasMore()) {
				  return true;
				} else if (eachContext.hasNext()) {
					enumeration = ((Context) eachContext.next()).listBindings(name);
					return hasMore();
				}
				return false;
			}
			public boolean hasMoreElements() {
				boolean h = false;
				try {
					h = hasMore();
				} catch (NamingException ignore) {}
				return h;
			}
			public void close() throws NamingException {
				open = false;
				if (enumeration != null) {
				  enumeration.close();
				}
			}
		};
	}

	public NamingEnumeration listBindings(Name name) throws NamingException {
		return listBindings(name.toString());
	}

	public void destroySubcontext(String name) throws NamingException {
		throw new OperationNotSupportedException("Subcontexts not supported by ObjectContext");
	}

	public void destroySubcontext(Name name) throws NamingException {
		destroySubcontext(name.toString());
	}

	public Context createSubcontext(String name) throws NamingException {
		throw new OperationNotSupportedException("Subcontexts not supported by ObjectContext");
	}

	public Context createSubcontext(Name name) throws NamingException {
		return createSubcontext(name.toString());
	}

	public Object lookupLink(String name) throws NamingException {
		return lookup(name);
	}

	public Object lookupLink(Name name) throws NamingException {
		return lookupLink(name.toString());
	}

	public NameParser getNameParser(String name) throws NamingException {
		return nameParser;
	}

	public NameParser getNameParser(Name name) throws NamingException {
		return getNameParser(name.toString());
	}

	public String composeName(String name, String prefix) throws NamingException {
		Name result = composeName(new CompositeName(name), new CompositeName(prefix));
		return result.toString();
	}

	public Name composeName(Name name, Name prefix) throws NamingException {
		Name result = (Name) prefix.clone();
		result.addAll(name);
		return result;
	}

	public Object addToEnvironment(String propName, Object propVal) throws NamingException {
		if (environment == null) {
		  environment = new Hashtable();
		}
		return environment.put(propName, propVal);
	}

	public Object removeFromEnvironment(String propName) throws NamingException {
		if (environment == null) {
		  return null;
		}
		return environment.remove(propName);
	}

	public Hashtable getEnvironment() throws NamingException {
		if (environment == null) {
		  return new Hashtable();
		}
		return (Hashtable) environment.clone();
	}

	public String getNameInNamespace() throws NamingException {
		return "";
	}

	public void close() throws NamingException {
		environment = null;
	}

	/**
	 * Install aliases specified in the properties file.  The properties file simply
	 * maps a string object name to a new object name.  Use the system property
	 * <code>org.apache.oodt.commons.object.jndi.aliases</code> (preferred) or simply
	 * <code>aliases</code> to tell the location of the properties file.
	 */
	private void installAliases() {
		String aliasFileName = System.getProperty("org.apache.oodt.commons.object.jndi.aliases", System.getProperty("aliases"));
		if (aliasFileName != null && aliasFileName.length() > 0) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(aliasFileName);
				aliases.load(in);
			} catch (IOException ex) {
				throw new IllegalStateException("Cannot handle I/O exception reading alias file " + aliasFileName
					+ ": " + ex.getMessage());
			} finally {
				if (in != null) {
				  try {
					in.close();
				  } catch (IOException ignore) {
				  }
				}
			}
		}
	}

	/** Context's environment; currently unused. */
	private Hashtable environment;

	/** Parser for object names. */
	private static final NameParser nameParser = new ObjectNameParser();

	/** List of {@link Context}s to which we "multiplex". */
	private List contexts = new ArrayList();

	/** Aliased names. */
	private Properties aliases = new Properties();
}
