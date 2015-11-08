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

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.naming.CommunicationException;
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
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;

/**
 * Context for binding and looking up distributed objects in RMI.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */



public class RMIContext implements Context {
	/*
	* Construct the RMI object context.
	* 
	* @param host Host name.
	* @param port Port number.
	*/
	public RMIContext(String host, int port) {
        	Hashtable environment = new Hashtable();
		environment.put("host", host);
		environment.put("port", port);
		initEnv(environment);
	}
	
	/**
	 * Construct the RMI object context.
	 *
	 * @param environment Its environment, currently unused.
	 */
        public RMIContext(Hashtable environment) {
                initEnv(environment);
	}

	/* Initializes environment
	*
	* @param environment Its environment, currently unused.
	*/
	private void initEnv(Hashtable environment) {
		if (environment == null) {
		  throw new IllegalArgumentException("Nonnull environment required");
		}
                this.environment = (Hashtable) environment.clone();
        }

	public Object lookup(String name) throws NamingException {
		checkName(name);
		name = toRMIName(name);
		if (name.length() == 0) {
		  return new RMIContext(environment);
		}
		Registry registry = getRegistry();
		try {
			return registry.lookup(name);
		} catch (NotBoundException ex) {
			throw new NameNotFoundException(name + " not found in RMI registry " + registry);
		} catch (RemoteException ex) {
			throw new NamingException("Remote exception: " + ex.getMessage());
		}
	}

	public Object lookup(Name name) throws NamingException {
		return lookup(name.toString());
	}

	public void bind(String name, Object obj) throws NamingException {
		checkName(name);
		Registry registry = getRegistry();
		try {
			registry.bind(toRMIName(name), (Remote) obj);
		} catch (AlreadyBoundException ex) {
			throw new NameAlreadyBoundException(name + " already bound in RMI registry " + registry);
		} catch (RemoteException ex) {
			throw new NamingException("Remote exception: " + ex.getMessage());
		}
	}

	public void bind(Name name, Object obj) throws NamingException {
		bind(name.toString(), obj);
	}

	public void rebind(String name, Object obj) throws NamingException {
		checkName(name);
		try {
			Registry registry = getRegistry();
			registry.rebind(toRMIName(name), (Remote) obj);
		} catch (RemoteException ex) {
			ex.printStackTrace();
			throw new NamingException("Remote exception: " + ex.getMessage());
		}
	}

	public void rebind(Name name, Object obj) throws NamingException {
		rebind(name.toString(), obj);
	}

	public void unbind(String name) throws NamingException {
		checkName(name);
		Registry registry = getRegistry();
		try {
			registry.unbind(toRMIName(name));
		} catch (NotBoundException ex) {
			throw new NameNotFoundException(name + " not found in RMI registry " + registry);
		} catch (RemoteException ex) {
			throw new NamingException("Remote exception: " + ex.getMessage());
		}
	}

	public void unbind(Name name) throws NamingException {
		unbind(name.toString());
	}

	public void rename(String oldName, String newName) throws NamingException {
		checkName(newName);
		throw new OperationNotSupportedException("Not yet implemented");
	}

	public void rename(Name oldName, Name newName) throws NamingException {
		rename(oldName.toString(), newName.toString());
	}

	public NamingEnumeration list(String name) throws NamingException {
		if (name.length() > 0) {
		  throw new NotContextException("Subcontexts not supported");
		}
				
		final Iterator i = getCurrentBindings().iterator();
		return new NamingEnumeration() {
			public void close() {}
			public boolean hasMore() {
				return i.hasNext();
			}
			public Object next() throws NamingException {
				String n = "urn:eda:rmi:" + (String) i.next();
				org.apache.oodt.commons.Service server = (org.apache.oodt.commons.Service) lookup(n);
				try {
					return new NameClassPair(n, server.getServerInterfaceName());
				} catch (RemoteException ex) {
					throw new CommunicationException(ex.getMessage());
				}
			}
			public boolean hasMoreElements() {
				return hasMore();
			}
			public Object nextElement() {
				Object next = null;
				try {
					next = next();
				} catch (RuntimeException ex) {
					throw ex;
				} catch (Exception ignore) {}
				return next;
			}
		};
	}
		
	public NamingEnumeration list(Name name) throws NamingException {
		return list(name.toString());
	}

	public NamingEnumeration listBindings(String name) throws NamingException {
		if (name.length() > 0) {
		  throw new NotContextException("Subcontexts not supported");
		}
		final Iterator i = getCurrentBindings().iterator();
		return new NamingEnumeration() {
			public void close() {}
			public boolean hasMore() {
				return i.hasNext();
			}
			public Object next() throws NamingException {
				String n = "urn:eda:rmi:" + (String) i.next();
				return new javax.naming.Binding(n, lookup(n));
			}
			public boolean hasMoreElements() {
				return hasMore();
			}
			public Object nextElement() {
				Object next = null;
				try {
					next = next();
				} catch (RuntimeException ex) {
					throw ex;
				} catch (Exception ignore) {}
				return next;
			}
		};
	}

	public NamingEnumeration listBindings(Name name) throws NamingException {
		return listBindings(name.toString());
	}

	public void destroySubcontext(String name) throws NamingException {
		throw new OperationNotSupportedException("Not yet implemented");
	}

	public void destroySubcontext(Name name) throws NamingException {
		destroySubcontext(name.toString());
	}

	public Context createSubcontext(String name) throws NamingException {
		throw new OperationNotSupportedException("Subcontexts not supported");
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
	 * Get the current bindings in the RMI registry.
	 *
	 * @return A list of the current bindings, as simple string names.
	 */
	private List getCurrentBindings() throws NamingException {
		List names;
		try {
			Registry registry = getRegistry();
			names = Arrays.asList(registry.list());
		} catch (RemoteException ex) {
			names = Collections.EMPTY_LIST;
		}
		return names;
	}

	/**
	 * Convert the context object name into an rmiregistry name.
	 *
	 * @param name Context name.
	 * @return rmiregistry name.
	 */
	private String toRMIName(String name) {
		if (name == null) {
		  return "";
		}
		if (name.startsWith("urn:eda:rmi:")) {
		  return name.substring(12);
		}
		if (name.startsWith("rmi:")) {
		  return name.substring(4);
		}
		return name;
	}

	/**
	 * Get the RMI registry.
	 *
	 * @return a <code>Registry</code> value.
	 * @throws NamingException if an error occurs.
	 */
	private Registry getRegistry() throws NamingException {
		if (registry != null) {
		  return registry;
		}
		try {
			String host = environment.containsKey("host")? (String) environment.get("host") : "localhost";
			int port = environment.containsKey("port")? (Integer) environment.get("port")
				: Registry.REGISTRY_PORT;

			
			registry = LocateRegistry.getRegistry(host, port);
		} catch (RemoteException ex) {
			throw new NamingException("Remote exception locating registry: " + ex.getMessage());
		}
		return registry;
	}

	/**
	 * Ensure the name is an RMI object context name.
	 *
	 * RMI object context names are URNs in the <code>eda</code> namespace, in the
	 * <code>rmi</code> subnamespace.
	 *
	 * @param name Name to check.
	 * @throws InvalidNameException If <var>name</var>'s not an RMI object context name.
	 */
	private void checkName(String name) throws InvalidNameException {
		if (name == null) {
		  throw new IllegalArgumentException("Can't check a null name");
		}
		if (name.length() == 0) {
		  throw new InvalidNameException("Name's length is zero");
		}
		if (!name.startsWith("urn:eda:rmi:")) {
		  throw new InvalidNameException("Not an RMI name; try urn:eda:rmi:yadda-yadda");
		}
	}

	/** Context's environment; currently unused. */
	private Hashtable environment;

	/** RMI Registry. */
	private Registry registry;

	/** Parser for object names. */
	private static final NameParser nameParser = new ObjectNameParser();
}

