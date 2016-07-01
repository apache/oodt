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

import java.util.concurrent.ConcurrentHashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.naming.Binding;
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

/**
 * A context for testing.  This context uses a simple {@link Map} to track bindings.
 * Names must start with a given prefix string or they cause {@link
 * InvalidNameException}s.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class TContext implements Context {

  /**
	 * Creates a new <code>TestContext</code> instance.
	 *
	 * @param prefix What every name must start with.
	 */
	public TContext(String prefix) {
		this.prefix = prefix;
	}

	public Object lookup(Name name) throws NamingException {
		return lookup(name.toString());
	}
	public Object lookup(String name) throws NamingException {
		Object rc = bindings.get(name);
		if (rc == null)
			throw new NameNotFoundException(name);
		return rc;
	}
	public void bind(Name name, Object obj) throws NamingException {
		bind(name.toString(), obj);
	}
	public void bind(String name, Object obj) throws NamingException {
		if (!name.startsWith(prefix)) throw new InvalidNameException("Name doesn't start with " + prefix);
		if (bindings.containsKey(name))
			throw new NameAlreadyBoundException(name);
		bindings.put(name, obj);
	}
	public void rebind(Name name, Object obj) throws NamingException {
		rebind(name.toString(), obj);
	}
	public void rebind(String name, Object obj) throws NamingException {
		if (!name.startsWith(prefix)) throw new InvalidNameException("Name doesn't start with " + prefix);
		bindings.put(name, obj);
	}
	public void unbind(Name name) throws NamingException {
		unbind(name.toString());
	}
	public void unbind(String name) throws NamingException {
		if (bindings.remove(name) == null)
			throw new NameNotFoundException(name);
	}
	public void rename(Name oldName, Name newName) throws NamingException {
		rename(oldName.toString(), newName.toString());
	}
	public void rename(String oldName, String newName) throws NamingException {
		if (!bindings.containsKey(oldName))
			throw new NameNotFoundException(oldName);
		if (bindings.containsKey(newName))
			throw new NameAlreadyBoundException(newName);
		if (!newName.startsWith(prefix))
			throw new InvalidNameException("Name doesn't start with " + prefix);
		bindings.put(newName, bindings.remove(oldName));
	}
	public NamingEnumeration list(Name name) throws NamingException {
		return list(name.toString());
	}
	public NamingEnumeration list(String name) throws NamingException {
		if (name.length() > 0)
			throw new OperationNotSupportedException("subcontexts not supported");
		final Iterator i = bindings.entrySet().iterator();
		return new NamingEnumeration() {
			public Object next() {
				Map.Entry e = (Map.Entry) i.next();
				return new NameClassPair((String) e.getKey(), e.getValue().getClass().getName());
			}
			public boolean hasMore() {
				return i.hasNext();
			}
			public void close() {}
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
	public NamingEnumeration listBindings(String name) throws NamingException {
		if (name.length() > 0)
			throw new OperationNotSupportedException("subcontexts not supported");
		final Iterator i = bindings.entrySet().iterator();
		return new NamingEnumeration() {
			public Object next() {
				Map.Entry e = (Map.Entry) i.next();
				return new Binding((String) e.getKey(), e.getValue());
			}
			public boolean hasMore() {
				return i.hasNext();
			}
			public void close() {}
			public boolean hasMoreElements() {
				return hasMore();
			}
			public Object nextElement() {
				return next();
			}

		};
	}
	public void destroySubcontext(Name name) throws NamingException {
		destroySubcontext(name.toString());
	}
	public void destroySubcontext(String name) throws NamingException {
		throw new OperationNotSupportedException("subcontexts not supported");
	}
	public Context createSubcontext(Name name) throws NamingException {
		return createSubcontext(name.toString());
	}
	public Context createSubcontext(String name) throws NamingException {
		throw new OperationNotSupportedException("subcontexts not supported");
	}
	public Object lookupLink(Name name) throws NamingException {
		return lookupLink(name.toString());
	}
	public Object lookupLink(String name) throws NamingException {
		return lookup(name);
	}
	public NameParser getNameParser(Name name) {
		return getNameParser(name.toString());
	}
	public NameParser getNameParser(String name) {
		return new ObjectNameParser();
	}
	public String composeName(String name, String prefix) throws NamingException {
		return composeName(new CompositeName(name), new CompositeName(prefix)).toString();
	}
	public Name composeName(Name name, Name prefix) throws NamingException {
		Name result = (Name) prefix.clone();
		result.addAll(name);
		return result;
	}
	public Object addToEnvironment(String key, Object val) {
		if (environment == null) environment = new Hashtable();
		return environment.put(key, val);
	}
	public Object removeFromEnvironment(String key) {
		if (environment == null) environment = new Hashtable();
		return environment.remove(key);
	}
	public Hashtable getEnvironment() {
		if (environment == null) environment = new Hashtable();
		return environment;
	}
	public void close() {}
	public String getNameInNamespace() {
		return "";
	}

	/** What holds the bindings.  Keys are {@link String}s, values are {@link Object}s. */
	Map bindings = new ConcurrentHashMap();

	/** What every key must start with. */
	private String prefix;

	/** Context's environment. */
	private Hashtable environment;
}
