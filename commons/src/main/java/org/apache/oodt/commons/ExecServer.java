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

import org.apache.oodt.commons.io.Base64EncodingOutputStream;
import org.apache.oodt.commons.util.LogInit;
import org.apache.oodt.commons.util.PropertyMgr;
import org.apache.oodt.commons.util.XML;
import org.apache.xmlrpc.XmlRpcClientLite;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcServer;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.NamingException;

/** Server execution program.
 *
 * This is an executable class that starts a JPL EDA server.
 *
 * @author Kelly
 * @deprecated soon be replaced by avro-rpc
 */
@Deprecated
public class ExecServer {

  public static final int MILLIS = 15000;

  /** Start a server.
	 *
	 * The command-line should have two arguments:
	 *
	 * <ol>
	 *   <li>The fully-qualified class name of the server to execute.</li>
	 *   <li>The object name of the server to register with the naming service.</li>
	 * </ol>
	 *
	 * @param argv The command-line arguments
	 */
	public static void main(String[] argv) {
		if (argv.length < 2) {
			System.err.println("Usage: class-name-of-server object-name");
			System.exit(1);
		}

		String className = argv[0];
		String name = argv[1];

		// Enable support of our special URLs, like stdin:
		System.setProperty("java.protocol.handler.pkgs", "org.apache.oodt.commons.net.protocol");

		try {
			// Get the configuration.
			configuration = Configuration.getConfiguration();
			configuration.mergeProperties(System.getProperties());

			// Set up the logger.
			LogInit.init(System.getProperties(), name);

			// Run initializers
			try {
				runInitializers();
			} catch (EDAException ex) {
				ex.printStackTrace();
				System.exit(1);
			}

			// Create it.
			final ExecServer server = new ExecServer(name, className);

			// Print it.
			if (Boolean.getBoolean(PRINT_IOR_PROPERTY)) {
				if (server.getServant() instanceof RemoteObject) {
					RemoteObject remoteObject = (RemoteObject) server.getServant();
					RemoteStub remoteStub = (RemoteStub) RemoteObject.toStub(remoteObject);
					RemoteRef ref = remoteStub.getRef();
					System.out.print("RMI:");
					System.out.flush();
					ObjectOutputStream objOut
						= new ObjectOutputStream(new Base64EncodingOutputStream(System.out));
					objOut.writeObject(ref);
					objOut.flush();
					System.out.println();
				} else {
					org.omg.PortableServer.Servant servant=(org.omg.PortableServer.Servant)server.getServant();
					org.omg.CORBA.ORB orb = servant._orb();
					System.out.println(orb.object_to_string(servant._this_object(orb)));
				}
				System.out.flush();
			}

			// Bind it.
			if (!Boolean.getBoolean(DISABLE_BINDING)) {
				binder = new Binder(name, server);
				binder.start();
			}

			// Prepare for the inevitable
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					server.shutdown0();
				}
			});

			// We're done here.
			for (;;) {
			  try {
				Thread.currentThread().join();
			  } catch (InterruptedException ignore) {
			  }
			}
		} catch (IOException ex) {
			System.err.println("I/O error during initialization: " + ex.getMessage());
			ex.printStackTrace();
		} catch (SAXParseException ex) {
			System.err.println("Error in the configuration file at line " + ex.getLineNumber() + ", column "
				+ ex.getColumnNumber() + ": " + ex.getMessage());
		} catch (SAXException ex) {
			System.err.println("Error " + ex.getClass().getName() + " while attempting to parse the configuration"
				+ " file: " + ex.getMessage());
		} catch (java.lang.reflect.InvocationTargetException ex) {
			Throwable target = ex.getTargetException();
			System.err.println("Constructor for \"" + className + "\" threw " + target.getClass().getName() + ": "
				+ ex.getMessage());
			target.printStackTrace();
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			System.err.println("Exception " + ex.getClass().getName() + " initializing server \"" + name
				+ "\" with class \"" + className + "\": " + ex.getMessage());
			ex.printStackTrace();
		}
		System.exit(1);
	}

	protected ExecServer(String name) {
		this.name = name;
	}

	/** Create a new executable server.
	 *
	 * @param name Name of the server
	 * @param className Name of class that implements the server.
	 * @throws ClassNotFoundException If the class for <var>className</var> can't be found.
	 * @throws NoSuchMethodException If the constructor for <var>className</var> taking a single <code>ExecServer</code>
	 *         can't be found.
	 * @throws InstantiationException If the class for <var>className</var> is abstract or is an interface.
	 * @throws IllegalAccessException If the class for <var>className</var> isn't public.
	 * @throws InvocationTargetException If an exception occurs in the constructor for <var>className</var>.
	 * @throws DOMException If the server's status document can't be created.
	 * @throws UnknownHostException If the local host name can't be determined.
	 */
	public ExecServer(String name, String className) throws ClassNotFoundException, NoSuchMethodException,
		InstantiationException, IllegalAccessException, InvocationTargetException, DOMException, UnknownHostException {
		this.name = name;

		// Find the class and the required constructor.
		Class clazz = Class.forName(className);
		Constructor ctor = clazz.getConstructor(new Class[]{ExecServer.class});

		// Invoke the constructor to create the servant.
		servant = ctor.newInstance(new Object[]{this});
		Date startDate = new Date();

		// Create the XML-RPC interface to this server.
		xmlrpcServer = new XmlRpcServer();
		xmlrpcServer.addHandler("server", this);

		// Create the server status document.
		DocumentType docType = XML.getDOMImplementation().createDocumentType("server", STATUS_FPI, STATUS_URL);
		statusDocument = XML.getDOMImplementation().createDocument(/*namespaceURI*/null, "server", docType);
		Element serverElement = statusDocument.getDocumentElement();
		XML.add(serverElement, "name", name);
		XML.add(serverElement, "class", className);
		XML.add(serverElement, "state", "up");
		Element startElement = statusDocument.createElement("start");
		serverElement.appendChild(startElement);
		Element userElement = statusDocument.createElement("user");
		startElement.appendChild(userElement);
		XML.add(userElement, "name", System.getProperty("user.name", "UNKNOWN"));
		XML.add(userElement, "cwd", System.getProperty("user.dir", "UNKNOWN"));
		XML.add(userElement, "home", System.getProperty("user.home", "UNKNOWN"));
		Element dateElement = statusDocument.createElement("date");
		startElement.appendChild(dateElement);
		dateElement.setAttribute("ms", String.valueOf(startDate.getTime()));
		dateElement.appendChild(statusDocument.createTextNode(startDate.toString()));
		XML.add(startElement, "config", System.getProperty("org.apache.oodt.commons.Configuration.url", "UNKNOWN"));
		Element hostElement = statusDocument.createElement("host");
		serverElement.appendChild(hostElement);
		XML.add(hostElement, "name", InetAddress.getLocalHost().getHostName());
		Element osElement = statusDocument.createElement("os");
		hostElement.appendChild(osElement);
		XML.add(osElement, "name", System.getProperty("os.name", "UNKNOWN"));
		XML.add(osElement, "version", System.getProperty("os.version", "UNKNOWN"));
		XML.add(osElement, "arch", System.getProperty("os.arch", "UNKNOWN"));
		Element vmElement = statusDocument.createElement("vm");
		serverElement.appendChild(vmElement);
		XML.add(vmElement, "name", System.getProperty("java.vm.name", "UNKNOWN"));
		XML.add(vmElement, "version", System.getProperty("java.version", "UNKNOWN"));
		XML.add(vmElement, "classpath", System.getProperty("java.class.path", "UNKNOWN"));
		XML.add(vmElement, "extdirs", System.getProperty("java.ext.dirs", "UNKNOWN"));
		logElement = statusDocument.createElement("log");
		serverElement.appendChild(logElement);
	}

	/** Get my name.
	 *
	 * @return The name under which I'm registered in teh naming context.
	 */
	public String getName() {
		return name;
	}

	/** Return the servant for this executable server.
	 *
	 * @return The servant.
	 */
	public Object getServant() {
		return servant;
	}

	/** Control this server.
	 *
	 * @param command Command to send to the server.
	 * @return Results of <var>command</var>.
	 */
	public byte[] control(byte[] command) {
		return xmlrpcServer.execute(new ByteArrayInputStream(command));
	}

	/** Return the server's class name.
	 *
	 * @return Its class name.
	 */
	public String getServerClassName() {
		return className;
	}

	/** Return status of this server.
	 *
	 * @return Its status.
	 */
	public String getServerStatus() {
		// Update the status document with the current log.
	  for (Object o : LogInit.MEMORY_LOGGER.getMessages()) {
		String message = (String) o;
		Element messageElement = statusDocument.createElement("message");
		messageElement.setAttribute("xml:space", "preserve");
		messageElement.appendChild(statusDocument.createTextNode(message));
		logElement.appendChild(messageElement);
	  }

		// Serialize the document.
		String rc = XML.serialize(statusDocument);

		// Remove all the log messages from the document.
		NodeList children = logElement.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
		  logElement.removeChild(children.item(i));
		}

		// Return the serialized form, which included the log messages.
		System.err.println(rc);
		return rc;
	}

	/** Set a system property.
	 *
	 * This uses the property manager to notify property change listeners.
	 *
	 * @param key Property's name.
	 * @param value New value.
	 * @return Zero (return required for XML-RPC access).
	 */
	public int setSystemProperty(String key, String value) {
		System.err.println("Setting system property \"" + key + "\" to \"" + value + "\"");
		PropertyMgr.setProperty(key, value);
		return 0;
	}

	/**
	 * Call the server manager on the local system.
	 *
	 * @param port What port on the local system the server manager is listening.
	 * @param user User name to use for authentication.
	 * @param password Authenticator for <var>user</var>.
	 * @param method What method in the server manager to call.
	 * @param params Parameters to pass to the method named by <var>method</var>.
	 * @return The return value from the method named by <var>method</var>.
	 * @throws Exception If any error occurs.
	 */
	public Object callLocalServerManager(int port, String user, String password, String method, List params)
		throws IOException, XmlRpcException {
		XmlRpcClientLite local = new XmlRpcClientLite("localhost", port);
		local.setBasicAuthentication(user, password);
		return local.execute(method, new Vector(params));
	}

	/** Shut down and exit.
	 *
	 * @return Zero.
	 */
	public int shutdown() {
		// Log it.
		System.err.println("Received shutdown command");

		// Make sure we actually exit sometime soon.
		new Thread() {
			public void run() {
				try {
					Thread.sleep(MILLIS);
				} catch (InterruptedException ignore) {}
				System.exit(1);
			}
		}.start();

		// Clean up.
		shutdown0();

		// And exit.
		System.err.println("Calling System.exit with status code 0");
		System.exit(0);
		return 0;
	}

	private void shutdown0() {
		// Unbind.
		if (!Boolean.getBoolean(DISABLE_BINDING)) {
		  try {
			binder.stopBinding();
			Context objectContext = configuration.getObjectContext();
			objectContext.unbind(getName());
			objectContext.close();
		  } catch (NamingException ignore) {
		  }
		}

		// Kill the ORB.  YEAH!  KILL IT, KILL IT, KIIIIIIIIIIIIIIL IIIIIIIIT!!!!!!!1
		// Replace org.omg dependent code with reflection (to make it compatible with Java 11 (LTS))
		try {
			Class<?> servantClass = Class.forName("org.omg.PortableServer.Servant");
			Class<?> orbClass = Class.forName("org.omg.CORBA.ORB");
			if (servantClass.isInstance(servant)) {
				Object s = servantClass.cast(servant);
				Object orb = orbClass.cast(servantClass.getDeclaredMethod("_orb").invoke(s));
				orbClass.getDeclaredMethod("shutdown", boolean.class)
						.invoke(orb, false/*=>terminate without waiting for reqs to complete*/);
			}
		} catch (Exception ignore) {
		}
	}

	/**
	 * Binding thread.
	 */
	private static class Binder extends Thread {
		public Binder(String name, ExecServer server) {
			super("Binder for " + name);
			setDaemon(true);
			this.name = name;
			this.server = server;
			keepBinding = true;
		}
		public void run() {
			while (shouldKeepBinding()) {
			  try {
				Context objectContext = configuration.getObjectContext();
				objectContext.rebind(name, server.getServant());
				objectContext.close();
			  } catch (Exception ex) {
				System.err.println("Exception binding at " + new Date() + "; will keep trying...");
				ex.printStackTrace();
			  } finally {
				try {
				  Thread.sleep(REBIND_PERIOD);
				} catch (InterruptedException ignore) {
				}
			  }
			}
		}
		public synchronized void stopBinding() {
			keepBinding = false;
		}
		private synchronized boolean shouldKeepBinding() {
			return keepBinding;
		}
		private boolean keepBinding;
		private String name;
		private ExecServer server;
	}

	/**
	 * Run all initializers.
	 *
	 * This instantiates and calls the {@link Initializer#initialize} method of each
	 * initializer specified by class name in a comma separated list of classes in the
	 * system properties.  The property name is <code>org.apache.oodt.commons.initializers</code>, or
	 * if not defined, <code>org.apache.oodt.commons.ExecServer.initializers</code>, or if not
	 * defined, <code>initializers</code>.  And if that one's not defined, then none
	 * are run.
	 *
	 * @throws EDAException if an error occurs.
	 */
	public static void runInitializers() throws EDAException {
		String initList = System.getProperty("org.apache.oodt.commons.initializers",
			System.getProperty("org.apache.oodt.commons.ExecServer.initializers", System.getProperty("initializers", "")));
		for (Iterator i = org.apache.oodt.commons.util.Utility.parseCommaList(initList); i.hasNext();) {
			String iname = (String) i.next();
			try {
				Class initClass = Class.forName(iname);
				Initializer init = (Initializer) initClass.newInstance();
				init.initialize();
			} catch (ClassNotFoundException ex) {
				System.err.println("Initializer \"" + iname + "\" not found; aborting");
				throw new EDAException(ex);
			} catch (InstantiationException ex) {
				System.err.println("Initializer \"" + iname + "\" is abstract; aborting");
				throw new EDAException(ex);
			} catch (IllegalAccessException ex) {
				System.err.println("Initializer \"" + iname + "\" isn't public; aborting");
				throw new EDAException(ex);
			}
		}
	}

	/** The configuration. */
	private static Configuration configuration;

	/** Object key name. */
	protected String name;

	/** The servant. */
	private Object servant;

	/** The server class name. */
	private String className;

	/** Current binder, if any. */
	private static Binder binder;

	/** Server's status document. */
	private Document statusDocument;

	/** The &lt;log&gt; element within the status document. */
	private Element logElement;

	/** The XML-RPC interface to this server. */
	private XmlRpcServer xmlrpcServer;

	/** Status DTD formal public identifier. */
	public static final String STATUS_FPI = "-//JPL//DTD EDA Server Status 1.0";

	/** Status DTD system identifier. */
	public static final String STATUS_URL = "http://oodt.jpl.nasa.gov/edm-commons/xml/serverStatus.dtd";

	/** Name of the property that prints the server's IOR or RMI handle. */
	public static final String PRINT_IOR_PROPERTY = "org.apache.oodt.commons.ExecServer.printIOR";

	/** Name of the property that prevents binding of this object with the naming service. */
	public static final String DISABLE_BINDING = "org.apache.oodt.commons.ExecServer.disableBinding";

	/** How long to wait before bind attempts, in ms. */
	private static final long REBIND_PERIOD =
		Long.getLong("org.apache.oodt.commons.ExecServer.rebindPeriod", 30 * 60 * 1000);
}
