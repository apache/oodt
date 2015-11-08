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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;

/**
 * This is a pseudo context that yields any name passed in as a URL, if it can be parsed as an URL.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public class HTTPContext implements Context {
	/**
	 * Make the HTTPContext.
	 *
	 * @param environment Its environment, currently unused.
	 */
        public HTTPContext(Hashtable environment) {
		if (environment == null) {
		  throw new IllegalArgumentException("Nonnull environment required; don't know why, but it is");
		}
                this.environment = (Hashtable) environment.clone();
	}

	public Object lookup(String name) throws NamingException {
		checkName(name);
		try {
			return new URL(name);
		} catch (MalformedURLException ex) {
			throw new NamingException(ex.getMessage());
		}
	}

	public Object lookup(Name name) throws NamingException {
		return lookup(name.toString());
	}

	public void bind(String name, Object obj) throws NamingException {
		checkName(name);
		throw new OperationNotSupportedException("Not possible");
	}

	public void bind(Name name, Object obj) throws NamingException {
		bind(name.toString(), obj);
	}

	public void rebind(String name, Object obj) throws NamingException {
		checkName(name);
		throw new OperationNotSupportedException("Not possible");
	}

	public void rebind(Name name, Object obj) throws NamingException {
		rebind(name.toString(), obj);
	}

	public void unbind(String name) throws NamingException {
		checkName(name);
		throw new OperationNotSupportedException("Not possible");
	}

	public void unbind(Name name) throws NamingException {
		unbind(name.toString());
	}

	public void rename(String oldName, String newName) throws NamingException {
		checkName(newName);
		throw new OperationNotSupportedException("Not possible");
	}

	public void rename(Name oldName, Name newName) throws NamingException {
		rename(oldName.toString(), newName.toString());
	}

	public NamingEnumeration list(String name) throws NamingException {
		if (name.length() > 0) {
		  throw new NotContextException("Subcontexts not supported");
		}
				
		return new NamingEnumeration() {
			public void close() {}
			public boolean hasMore() {
				return false;
			}
			public Object next() {
				throw new NoSuchElementException();
			}
			public boolean hasMoreElements() {
				return hasMore();
			}
			public Object nextElement() {
				return next();
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
		return new NamingEnumeration() {
			public void close() {}
			public boolean hasMore() {
				return false;
			}
			public Object next() {
				throw new NoSuchElementException();
			}
			public boolean hasMoreElements() {
				return hasMore();
			}
			public Object nextElement() {
				return next();
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
	 * Ensure the name is an HTTP product context name.
	 *
	 * @param name Name to check.
	 * @throws InvalidNameException If <var>name</var>'s not an RMI object context name.
	 */
	protected void checkName(String name) throws InvalidNameException {
		if (name == null) {
		  throw new IllegalArgumentException("Can't check a null name");
		}
		if (name.length() == 0) {
		  throw new InvalidNameException("Name's length is zero");
		}
		if (name.startsWith("http:") || name.startsWith("https:")) {
		  return;
		}
		throw new InvalidNameException("Not an HTTP name; try http://some.host/some-context/...");
	}

	/** Context's environment; currently unused. */
	private Hashtable environment;

	/** Parser for object names. */
	private static final NameParser nameParser = new ObjectNameParser();
}

