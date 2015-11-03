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

package org.apache.oodt.commons;

import org.apache.oodt.commons.util.LogInit;
import org.apache.oodt.commons.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.util.concurrent.ConcurrentHashMap;;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * The MultiServer runs multiple server objects in a single JVM.  Instead of running a
 * separate product server, profile server, query server, and so forth, in their own JVMs,
 * which are extremely heavy-weight operating system processes, we can put them into one
 * JVM which reduces the memory footprint on a single computer enormously.
 *
 * <h3>Specifying the Configuration</h3>
 *
 * The MultiServer configuration is an XML document.  Here's
 * a sample:
 * <pre>&lt;multiserver
 *   xmlns="http://oodt.jpl.nasa.gov/edm-commons/xml/multiserver"
 *   id="My Multi Server"&gt;
 *   &lt;server
 *     class="org.apache.oodt.commons.product.rmi.ProductServiceImpl"
 *     id="urn:eda:rmi:BioServer"
 *     bind="rebind" /&gt;
 *   &lt;server
 *     class="org.apache.oodt.commons.product.rmi.ProductServiceImpl"
 *     id="urn:eda:rmi:SpaceServer"
 *     bind="1800000" /&gt;
 *   &lt;server
 *     class="org.apache.oodt.commons.profile.rmi.ProfileServiceImpl"
 *     id="urn:eda:rmi:Resource"
 *     bind="bind" /&gt;
 *   &lt;properties&gt;
 *     &lt;property name="urn:eda:rmi:BioServer.handlers"&gt;
 *       edrn.MedHandler,edrn.SpecimenHandler
 *     &lt;/property&gt;
 *     &lt;property name="urn:eda:rmi:SpaceServer.handlers"&gt;
 *       pds.PlanetoidHandler
 *     &lt;/property&gt;
 *     &lt;property name="org.apache.oodt.commons.profile.Handlers"&gt;
 *       com.sun.ResourceHandler
 *     &lt;/property&gt;
 *   &lt;/properties&gt;
 * &lt;/multiserver&gt;</pre>
 *
 * <p>This would start three servers (two products, one profile) with the various property
 * settings indicated.  The MultiServer expects the property
 * <code>org.apache.oodt.commons.MultiServer.config</code> to identify the URL of the configuration.  You
 * can shorten that to <code>MultiServer.config</code> or <code>multiserver.config</code>
 * or even just <code>config</code>, in that order.  If none of those properties are
 * specified then the MultiServer will expect the URL to be the first (and only) command
 * line argument.
 *
 * <p>The <code>id</code> attribute on the <code>multiserver</code> element tells the name
 * of the whole application; it's used to prefix log messages.
 *
 * <h3>Server Specification</h3>
 *
 * <p>Each <code>&lt;server&gt;</code> entry names a server to start.  The
 * <code>class</code> attribute is the name of the RMI-compatible Java class that the
 * server will run.  (Note that currently only RMI servers are supported.)  The
 * <code>id</code> attribute tells the name the server should use to register with the
 * naming context.  And the <code>bind</code> attribute tells how the registration should
 * proceed.  It can take on the following values:
 *
 * <ul>
 *   <li><code>true</code> meaning the object will attempt a bind.  If the ID is already
 *   bound in the context, the MultiServer fails.</li>
 *
 *   <li><code>false</code> meaning the object won't be bound.</li>
 *
 *   <li><code>rebind</code> meaning the object will rebind its ID in the context,
 *   overwriting any previous binding.
 *
 *   <li><var>number</var> meaining the object will rebind its ID once at starup, and
 *   every <var>number</var> milliseconds thereafter.  This is, in OODT's experience, the
 *   most useful option as it helps an entire dpeloyed network of servers self-heal after
 *   a naming context restart.
 * </ul>
 *
 * <h3>Propery Specification</h3>
 *
 * <p>For convenience, System Properties may be specified in the configirutaion as well.
 * However, any properties already defined (such as using the <code>-D</code> command-line
 * argument) get priority and their values won't be overridden.  To specify properties,
 * list any number of <code>&lt;property&gt;</code> elements under the
 * <code>&lt;properties&gt;</code> element with a <code>name</code> attribute naming the
 * System Property key and the text of the element naming its value.  Note that the text
 * will be unwrapped.
 *
 * @author Kelly
 * @version $Revision: 1.3 $
 */
public class MultiServer {
	/**
	 * Start the multi server.
	 *
	 * @param argv Command-line arguments.
	 * @throws Throwable if an error occurs.
	 */
	public static void main(String[] argv) throws Throwable {
		String config = System.getProperty("org.apache.oodt.commons.MultiServer.config", System.getProperty("MultiServer.config",
			System.getProperty("multiserver.config", System.getProperty("config"))));
		if (config == null) {
			if (argv.length != 1) {
			  throw new IllegalStateException(
				  "No org.apache.oodt.commons.MultiServer.config property or config URL argument");
			} else {
			  config = argv[0];
			}
		}

		StringReader reader = new StringReader(CONFIG);
		Configuration.configuration = new Configuration(new InputSource(reader));
		reader.close();
		parseConfig(new InputSource(config));

		Hashtable t = new Hashtable();
		t.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.oodt.commons.object.jndi.ObjectCtxFactory");
		String registryList = System.getProperty("org.apache.oodt.commons.rmiregistries", System.getProperty("rmiregistries"));
		if (registryList == null) {
			String host = System.getProperty("rmiregistry.host", "localhost");
			int port = Integer.getInteger("rmiregistry.port", Registry.REGISTRY_PORT);
			registryList = "rmi://" + host + ":" + port;
		}
		t.put("rmiregistries", registryList);
		context = NamingManager.getInitialContext(t);
		ExecServer.runInitializers();
		try {
			LogInit.init(System.getProperties(), getAppName());
			if (servers.isEmpty()) {
			  throw new IllegalStateException("No servers defined in config");
			}

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					shutdown();
				}
			});
			startup();
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				shutdown();
			} catch (Exception ignore) {}
			System.exit(1);
		}
		for (;;) {
		  try {
			Thread.currentThread().join();
		  } catch (InterruptedException ignore) {
		  }
		}
	}

	/**
	 * Parse the MultiServer configuration.
	 *
	 * @param is Source of the configuration.
	 * @throws ParserConfigurationException If we can't create a parser.
	 * @throws SAXException If there's a parse error.
	 * @throws IOException If there's a problem reading the configuration.
	 * @throws ClassNotFoundException If we can't find a server class.
	 * @throws NoSuchMethodException If the server class doesn't have the right constructor.
	 * @throws InstantiationException If we can create a server object.
	 * @throws IllegalAccessException If the server constructor isn't accessible.
	 * @throws InvocationTargetException If the server constructor throws an exception.
	 */
	static void parseConfig(InputSource is) throws ParserConfigurationException, SAXException, IOException,
		ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
		InvocationTargetException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(is);
		Element root = doc.getDocumentElement();
		appName = root.getAttribute("id");
		if (appName == null) {
		  throw new SAXException("id attribute missing from multiserver element");
		}

		// Set properties
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node node = (Node) children.item(i);
			if ("properties".equals(node.getNodeName())) {
				NodeList props = ((Element) node).getElementsByTagName("property");
				for (int j = 0; j < props.getLength(); ++j) {
					Element property = (Element) props.item(j);
					String name = property.getAttribute("name");
					if (!System.getProperties().containsKey(name)) {
						String value = XML.unwrappedText(property);
						System.setProperty(name, value);
					}
				}
			}
		}		

		// Create servers
		NodeList serverNodes = root.getElementsByTagName("server");
		servers = new ConcurrentHashMap();
		for (int i = 0; i < serverNodes.getLength(); ++i) {
			Element serverElem = (Element) serverNodes.item(i);
			String name = serverElem.getAttribute("id");
			if (name == null) {
			  throw new SAXException("id attribute missing from server element");
			}
			String className = serverElem.getAttribute("class");
			if (className == null) {
			  throw new SAXException("class attribute missing from server element");
			}
			String bindKind = serverElem.getAttribute("bind");
			if (bindKind == null) {
			  throw new SAXException("bind attribute missing from server element");
			}
			Server server;
			if ("true".equals(bindKind)) {
			  server = new BindingServer(name, className);
			} else if ("false".equals(bindKind)) {
			  server = new NonbindingServer(name, className);
			} else if ("rebind".equals(bindKind)) {
			  server = new RebindingServer(name, className);
			} else {
			  try {
				long period = Long.parseLong(bindKind);
				server = new AutobindingServer(name, className, period);
			  } catch (NumberFormatException ex) {
				throw new SAXException("Expected true, false, rebind, or auto for bind attribute but got `"
									   + bindKind + "'");
			  }
			}
			servers.put(name, server);
		}

	}

	/**
	 * Start each server.
	 *
	 * @throws NamingException if an error occurs.
	 */
	static void startup() throws NamingException {
	  for (Object o : servers.values()) {
		Server s = (Server) o;
		s.start();
	  }
	}

	/**
	 * Stop each server.
	 */
	static void shutdown() {
	  for (Object o : servers.values()) {
		try {
		  Server s = (Server) o;
		  s.stop();
		} catch (NamingException ignore) {
		}
	  }
		TIMER.cancel();
	}

	/**
	 * Get the name of the application.
	 *
	 * @return a {@link String} value.
	 */
	static String getAppName() {
		return appName;
	}

	/**
	 * Get the servers.  Keys are {@String} names and values are s.
	 *
	 * @return a {@link Map} of the defined servers.
	 */
	static Map getServers() {
		return servers;
	}

	/**
	 * A server.
	 */
	static abstract class Server extends ExecServer {
		/**
		 * Creates a new {@link Server} instance.
		 *
		 * @param name ID under which to register.
		 * @param className Class of server to instantiate.
		 * @throws ClassNotFoundException If we can't find the server class.
		 * @throws NoSuchMethodException If the server class doesn't have the right constructor.
		 * @throws InstantiationException If we can create the server object.
		 * @throws IllegalAccessException If the server constructor isn't accessible.
		 * @throws InvocationTargetException If the server constructor throws an exception.
		 */
		protected Server(String name, String className) throws ClassNotFoundException, NoSuchMethodException,
			InstantiationException, IllegalAccessException, InvocationTargetException {
			super(name);
			this.className = className;
			Class clazz = Class.forName(className);
			Constructor ctor = clazz.getConstructor(new Class[]{ ExecServer.class });
			servant = (RemoteObject) ctor.newInstance(new Object[]{ this });
		}

		/**
		 * Get the name of the server class to instantiate.
		 *
		 * @return a {@link String} value.
		 */
		public String getClassName() {
			return className;
		}

		/**
		 * Get the type of binding this server will perform.  Possible values are
		 * {@link #BINDING}, {@link #NONBINDING}, {@link #REBINDING}, or {@link
		 * #AUTO}.
		 *
		 * @return An integer identifiying the binding behavior.
		 */
		public abstract int getBindingBehavior();

		/**
		 * Start this server.
		 *
		 * @throws NamingException if an error occurs during the binding.
		 */
		public abstract void start() throws NamingException;

		/**
		 * Stop this server.
		 *
		 * @throws NamingException if an error occurs during unbinding.
		 */
		public abstract void stop() throws NamingException;

		/** Name of server class. */
		protected String className;

		/** Server object. */
		protected RemoteObject servant;
	}

	/** Inidcates server will try a bind. */
	public static final int BINDING = 1;

	/** Indicates server won't be bound. */
	public static final int NONBINDING = 2;

	/** Indicates server will try a rebind. */
	public static final int REBINDING = 3;

	/** Indicates server will try periodic rebinding. */
	public static final int AUTO = 4;

	/** Timer to schedule periodic rebinind. */
	private static final Timer TIMER = new Timer(/*isDaemon*/true);

	/** Name of this application. */
	private static String appName;

	/** Known servers.  Keys are {@String} names and values are s. */
	private static Map servers;

	/** The naming context. */
	private static Context context;

	/** The edarc.xml file to satisfy the MultiServer's servers. */
	private static final String CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE configuration PUBLIC"
		+ " \"-//JPL//DTD EDA Configuration 1.0//EN\" \"http://enterprise.jpl.nasa.gov/dtd/configuration.dtd\">\n"
		+ "<configuration><webServer><host>localhost</host><port>80</port></webServer>"
		+ "<nameServer><iiop><host>localhost</host><port>10000</port></iiop></nameServer></configuration>";

	/**
	 * A server that tries a single bind.
	 */
	static class BindingServer extends Server {
		/**
		 * Creates a new {@link BindingServer} instance.
		 *
		 * @param name ID under which to register.
		 * @param className Class of server to instantiate.
		 * @throws ClassNotFoundException If we can't find the server class.
		 * @throws NoSuchMethodException If the server class doesn't have the right constructor.
		 * @throws InstantiationException If we can create the server object.
		 * @throws IllegalAccessException If the server constructor isn't accessible.
		 * @throws InvocationTargetException If the server constructor throws an exception.
		 */
		BindingServer(String name, String className) throws ClassNotFoundException, NoSuchMethodException,
			InstantiationException, IllegalAccessException, InvocationTargetException {
			super(name, className);
		}

		public int getBindingBehavior() {
			return BINDING;
		}

		/**
		 * Start by binding.
		 *
		 * @throws NamingException if an error occurs.
		 */
		public void start() throws NamingException {
			context.bind(name, servant);
		}

		/**
		 * Stop by unbinding.
		 *
		 * @throws NamingException if an error occurs.
		 */
		public void stop() throws NamingException {
			context.unbind(name);
		}
	}

	/**
	 * A (named, but anonymous) server that isn't bound to a naming context.
	 */
	static class NonbindingServer extends Server {
		/**
		 * Creates a new {@link NonbindingServer} instance.
		 *
		 * @param name ID under which to register.
		 * @param className Class of server to instantiate.
		 * @throws ClassNotFoundException If we can't find the server class.
		 * @throws NoSuchMethodException If the server class doesn't have the right constructor.
		 * @throws InstantiationException If we can create the server object.
		 * @throws IllegalAccessException If the server constructor isn't accessible.
		 * @throws InvocationTargetException If the server constructor throws an exception.
		 */
		NonbindingServer(String name, String className) throws ClassNotFoundException, NoSuchMethodException,
			InstantiationException, IllegalAccessException, InvocationTargetException {
			super(name, className);
		}

		public int getBindingBehavior() {
			return NONBINDING;
		}

		/**
		 * Start by taking no action.
		 */
		public void start() {}

		/**
		 * Stop by taking no action.
		 */
		public void stop() {}
	}

	/**
	 * A server that rebinds its ID in the naming context.
	 */
	static class RebindingServer extends BindingServer {
		/**
		 * Creates a new {@link RebindingServer} instance.
		 *
		 * @param name ID under which to register.
		 * @param className Class of server to instantiate.
		 * @throws ClassNotFoundException If we can't find the server class.
		 * @throws NoSuchMethodException If the server class doesn't have the right constructor.
		 * @throws InstantiationException If we can create the server object.
		 * @throws IllegalAccessException If the server constructor isn't accessible.
		 * @throws InvocationTargetException If the server constructor throws an exception.
		 */
		RebindingServer(String name, String className) throws ClassNotFoundException, NoSuchMethodException,
			InstantiationException, IllegalAccessException, InvocationTargetException {
			super(name, className);
		}

		public int getBindingBehavior() {
			return REBINDING;
		}

		/**
		 * Start by rebinding in naming context.
		 *
		 * @throws NamingException if an error occurs.
		 */
		public void start() throws NamingException {
			context.rebind(name, servant);
		}
	}

	/**
	 * A server that periodically rebinds its ID in the naming context.
	 */
	static class AutobindingServer extends Server {
		/**
		 * Creates a new {@link AutobindingServer} instance.
		 *
		 * @param name ID under which to register.
		 * @param className Class of server to instantiate.
		 * @throws ClassNotFoundException If we can't find the server class.
		 * @throws NoSuchMethodException If the server class doesn't have the right constructor.
		 * @throws InstantiationException If we can create the server object.
		 * @throws IllegalAccessException If the server constructor isn't accessible.
		 * @throws InvocationTargetException If the server constructor throws an exception.
		 */
		AutobindingServer(String name, String className, long period) throws ClassNotFoundException, NoSuchMethodException,
			InstantiationException, IllegalAccessException, InvocationTargetException {
			super(name, className);
			this.period = period;
		}

		public int getBindingBehavior() {
			return AUTO;
		}

		/**
		 * Start by scheduling the timer task that rebinds this server.
		 */
		public void start() {
			TIMER.schedule(binder = new Binder(), /*delay*/0L, /*period*/period);
		}

		/**
		 * Stop by canceling the timer task and unbinding from the server.
		 *
		 * @throws NamingException if an error occurs.
		 */
		public void stop() throws NamingException {
			if (binder != null) {
			  binder.cancel();
			}
			context.unbind(name);
		}

		/**
		 * Get how often this server process will be rebound.
		 *
		 * @return Period in milliseconds.
		 */
		public long getPeriod() {
			return period;
		}

		/** How often to rebind in milliseconds. */
		private long period;

		/** Timer task that rebinds. */
		private Binder binder;

		/**
		 * Timer task that rebinds this server to the naming context.
		 */
		private class Binder extends TimerTask {
			public void run() {
				try {
					context.rebind(name, servant);
				} catch (NamingException ignored) {}
			}
		}
	}
}
