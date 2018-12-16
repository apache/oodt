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

import org.apache.oodt.commons.util.DOMParser;
import org.apache.oodt.commons.util.XML;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

/** EDA Configuration.
 *
 * An object of this class represents the configuration information for the EDA software.
 *
 * @author Kelly
 */
public class Configuration {
  public static final int INT = 7577;
  public static final int INT1 = 6000000;
  /** The singleton configuration. */
	static Configuration configuration = null;

	/** Name of property that specifies the direcotries that contains XML entities. */
	public static final String ENTITY_DIRS_PROP = "entity.dirs";

	/** Name of the default config file. */
	public static final String DEFAULT_CONFIG_FILE = ".edarc.xml";

	/** Alternate config file. */
	public static final String ALT_CONFIG_FILE = ".oodtrc.xml";

	/** Library-location config file. */
	public static final File LIB_CONFIG_FILE = new File(System.getProperty("java.home", "/") + File.separator + "lib"
		+ File.separator + "edarc.xml");

	/** Non-JRE library location of config file. */
	public static final File ALT_LIB_CONFIG_FILE = new File(System.getProperty("java.home", "/") + File.separator + ".."
		+ File.separator + "lib" + File.separator + "edarc.xml");

	 /** Get the singleton configuration.
	  *
	  * This method returns the singleton configuration object, or creates it if it
	  * doesn't yet exist.  To create it, it reads the configuration file specified by
	  * the system property <code>org.apache.oodt.commons.Configuration.url</code> or the file in the
	  * user's home directory called .edarc.xml if the system property isn't
	  * specified.  It parses the file and returns a <code>Configuration</code> object
	  * initialized with the data specified therein.
	  *
	  * @throws IOException If reading the configuration file fails.
	  * @throws SAXException If parsing the configuration file fails.
	  * @throws MalformedURLException If the URL specification is invalid.
	  * @return An initialized configuration object.
	  */
	 public static Configuration getConfiguration() throws IOException, SAXException {
		 // Got one?  Use it.
		 if (configuration != null) {
		   return configuration;
		 }

		 URL url;

		 // First preference: URL via the org.apache.oodt.commons.Configuration.url prop.
		 String urlString = System.getProperty("org.apache.oodt.commons.Configuration.url");
		 if (urlString != null) {
			 url = new URL(urlString);
		 } else {
			 File file = null;

			 // Second preference: file via the org.apache.oodt.commons.Configuration.file prop.
			 String filename = System.getProperty("org.apache.oodt.commons.Configuration.file");
			 if (filename != null) {
				 file = new File(filename);
				 if (!file.exists()) {
				   throw new IOException("File " + file + " not found");
				 }
			 } else {
				 List candidates = new ArrayList();

				 // Third preference: ~/.edarc.xml
				 File homedir = new File(System.getProperty("user.home", "/"));
				 File homedirfile = new File(homedir, DEFAULT_CONFIG_FILE);
				 candidates.add(homedirfile);

				 // Fourth preference: ~/.oodtrc.xml
				 File alt = new File(homedir, ALT_CONFIG_FILE);
				 candidates.add(alt);

				 // Fifth and sixth preferences: $EDA_HOME/conf/edarc.xml and $EDA_HOME/etc/edarc.xml
				 String edaHome = System.getProperty("eda.home");
				 if (edaHome != null) {
					 File edaHomeDir = new File(edaHome);
					 candidates.add(new File(new File(edaHomeDir, "conf"), "edarc.xml"));
					 candidates.add(new File(new File(edaHomeDir, "etc"), "edarc.xml"));
				 }

				 // Seventh preference: JAVA_HOME/lib/edarc.xml
				 candidates.add(LIB_CONFIG_FILE);

				 // Final preference: JAVA_HOME/../lib/edarc.xml (to get out of JRE)
				 candidates.add(ALT_LIB_CONFIG_FILE);

				 // Now find one.
				 boolean found = false;
			   for (Object candidate : candidates) {
				 file = (File) candidate;
				 if (file.exists()) {
				   found = true;
				   break;
				 }
			   }
				 if (found && file == alt) {
				   System.err.println("WARNING: Using older config file " + alt + "; rename to "
									  + homedirfile + " as soon as possible.");
				 }
				 if (!found) {
					 return getEmptyConfiguration();
				 }
			 }
			 url = file.toURI().toURL();
		 }

		 return getConfiguration(url);

	 }

	/** Get the singleton configuration.
	 *
	 * This method returns the singleton configuration object from a 
	 * specified file url.  It parses the file and returns a <code>Configuration</code> object
	 * initialized with the data specified therein.  Added by Chris Mattmann 12/05/03.
	 *
	 * @throws IOException If an I/O error occurs.
	 * @return An initialized configuration object.
	 */
	public static Configuration getConfiguration(URL configFileUrl) throws SAXException, IOException {
		synchronized (Configuration.class) {
			if (configuration == null) {
			  configuration = new Configuration(configFileUrl);
			}
		}
		return configuration;
	}

	private static Configuration getEmptyConfiguration() {
		synchronized (Configuration.class) {
			if (configuration == null) {
			  configuration = new Configuration();
			}
		}
		return configuration;
	}


	/** Get the singleton configuration without exception.
	 *
	 * This method is identical to {@link #getConfiguration} but traps all checked
	 * exceptions.  If the configuration can't be read, it returns null.
	 *
	 * @return An initialized configuration object, or null if an error occurred.
	 */
	public static Configuration getConfigurationWithoutException() {
		// Got one?  Use it.  Do this out of a try block for performance.
		if (configuration != null) {
		  return configuration;
		}

		// Try to get it.
		try {
			return getConfiguration();
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			System.err.println("Exception " + ex.getClass().getName() + " while getting configuration: "
				+ ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}

	Configuration() {
		serverMgrPort = INT;
		nameServerStateFrequency = INT1;
		nameServerObjectKey = "StandardNS%20POA";
		nameServerPort = "10000";
		nameServerHost = "localhost";
		nameServerVersion = "1.0";
		nameServerUsingRIRProtocol = false;
		webServerDocumentDirectory = new File(System.getProperty("user.home", "/") + "tomcat/webapps/ROOT");
		webPort = "8080";
		webHost = "localhost";
		System.setProperty(WEB_PROTOCOL_PROPERTY, "http");
		initializeContext();
	}

	/** Construct a configuration.
	 *
	 * @param url The location of the configuration.
	 * @throws IOException If reading the configuration file fails.
	 * @throws SAXParseException If parsing the configuration file fails.
	 */
	Configuration(URL url) throws IOException, SAXException {
		this(new InputSource(url.toString()));
	}

	Configuration(InputSource inputSource) throws IOException, SAXException {
		String systemID = inputSource.getSystemId();
		if (systemID == null) {
		  inputSource.setSystemId("file:/unknown");
		}

		// Get the document
		DOMParser parser = XML.createDOMParser();
		parser.setEntityResolver(new ConfigurationEntityResolver());
		parser.setErrorHandler(new ErrorHandler() {
			public void error(SAXParseException ex) throws SAXException {
				throw ex;
			}
			public void warning(SAXParseException ex) {
				System.err.println("Warning: " + ex.getMessage());
			}
			public void fatalError(SAXParseException ex) throws SAXException {
				System.err.println("Fatal parse error: " + ex.getMessage());
				throw ex;
			}
		});
		parser.parse(inputSource);
		Document document = parser.getDocument();
		XML.removeComments(document);
		document.normalize();
		
		// See if this really is a <configuration> document.
		if (!document.getDocumentElement().getNodeName().equals("configuration")) {
		  throw new SAXException("Configuration " + inputSource.getSystemId() + " is not a <configuration> document");
		}

		NodeList list = document.getDocumentElement().getChildNodes();
		for (int eachChild = 0; eachChild < list.getLength(); ++eachChild) {
			Node childNode = list.item(eachChild);
			if (childNode.getNodeName().equals("webServer")) {
				NodeList children = childNode.getChildNodes();
				for (int i = 0; i < children.getLength(); ++i) {
					Node node = children.item(i);
					if ("host".equals(node.getNodeName())) {
					  webHost = XML.unwrappedText(node);
					} else if ("port".equals(node.getNodeName())) {
					  webPort = XML.unwrappedText(node);
					} else if ("dir".equals(node.getNodeName())) {
					  webServerDocumentDirectory = new File(XML.unwrappedText(node));
					}
				}					
				properties.setProperty("org.apache.oodt.commons.Configuration.webServer.baseURL", getWebServerBaseURL());
				if (webServerDocumentDirectory == null) {
				  webServerDocumentDirectory = new File(System.getProperty("user.home", "/")
														+ "/dev/htdocs");
				}
			} else if (childNode.getNodeName().equals("nameServer")) {
				Element nameServerNode = (Element) childNode;
				String nameServerStateFrequencyString = nameServerNode.getAttribute("stateFrequency");
				if (nameServerStateFrequencyString == null || nameServerStateFrequencyString.length() == 0) {
				  nameServerStateFrequency = 0;
				} else {
				  try {
					nameServerStateFrequency = Integer.parseInt(nameServerStateFrequencyString);
				  } catch (NumberFormatException ex) {
					throw new SAXException("Illegal nun-numeric value \"" + nameServerStateFrequencyString
										   + "\" for stateFrequency attribute");
				  }
				}
				if (childNode.getFirstChild().getNodeName().equals("rir")) {
					nameServerUsingRIRProtocol = true;
					NodeList children = childNode.getFirstChild().getChildNodes();
					nameServerObjectKey = children.getLength() == 1? XML.unwrappedText(children.item(0)):null;
				} else {
					nameServerUsingRIRProtocol = false;
					nameServerVersion = null;
					nameServerPort = null;
					// Must be same as CORBAMgr.NS_OBJECT_KEY:
					nameServerObjectKey = "StandardNS/NameServer%2DPOA/_root";
					NodeList children = childNode.getFirstChild().getChildNodes();
					for (int i = 0; i < children.getLength(); ++i) {
						Node node = children.item(i);
						if (node.getNodeName().equals("version")) {
						  nameServerVersion = XML.unwrappedText(node);
						} else if (node.getNodeName().equals("host")) {
						  nameServerHost = XML.unwrappedText(node);
						} else if (node.getNodeName().equals("port")) {
						  nameServerPort = XML.unwrappedText(node);
						} else if (node.getNodeName().equals("objectKey")) {
						  nameServerObjectKey = XML.unwrappedText(node);
						}
					}
				}
			} else if (childNode.getNodeName().equals("xml")) {
				NodeList children = childNode.getChildNodes();
				for (int i = 0; i < children.getLength(); ++i) {
					Node xmlNode = children.item(i);
					if ("entityRef".equals(xmlNode.getNodeName())) {
						NodeList dirNodes = xmlNode.getChildNodes();
						StringBuilder refDirs = new StringBuilder(System.getProperty(ENTITY_DIRS_PROP, ""));
						for (int j = 0; j < dirNodes.getLength(); ++j) {
						  refDirs.append(',').append(XML.unwrappedText(dirNodes.item(j)));
						}
						if (refDirs.length() > 0) {
						  System.setProperty(ENTITY_DIRS_PROP, refDirs.charAt(0) == ',' ?
															   refDirs.substring(1) : refDirs.toString());
						}
					}
				}
			} else if ("serverMgr".equals(childNode.getNodeName())) {
				serverMgrPort = Integer.parseInt(XML.unwrappedText(childNode.getFirstChild()));
			} else if (childNode.getNodeName().equals("properties")) {
				loadProperties(childNode, properties);
			} else if (childNode.getNodeName().equals("programs")) {
				NodeList children = childNode.getChildNodes();
				for (int i = 0; i < children.getLength(); ++i) {
					// They're all of type execServer---for now.
					ExecServerConfig esc = new ExecServerConfig(children.item(i));
					esc.getProperties().setProperty("org.apache.oodt.commons.Configuration.url", inputSource.getSystemId());
					execServers.add(esc);
				}
			}
		}

		initializeContext();
	}

	private void initializeContext() {
		contextEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.oodt.commons.object.jndi.ObjectCtxFactory");
		String registryList = System.getProperty("org.apache.oodt.commons.rmiregistries", System.getProperty("rmiregistries"));
		if (registryList == null) {
			String host = System.getProperty("rmiregistry.host", "localhost");
			int port = Integer.getInteger("rmiregistry.port", Registry.REGISTRY_PORT);
			registryList = "rmi://" + host + ":" + port;
		}
		contextEnvironment.put("rmiregistries", registryList);
	}

	/** Serialize this configuration into a serialized XML document.
	 *
	 * @return Serialized XML version of this configuration.
	 * @throws DOMException If an error occurs constructing the XML structure.
	 */
	public String toXML() throws DOMException {
		Document doc = createDocument("configuration");
		doc.replaceChild(toXML(doc), doc.getDocumentElement());
		return XML.serialize(doc);
	}

	/** 
	 *
	 * @param document The document to which the XML structure will belong.
	 * @return The root node representing this configuration.
	 * @throws DOMException If an error occurs constructing the XML structure.
	 */
	public Node toXML(Document document) throws DOMException {
		// <configuration>
		Element configurationNode = document.createElement("configuration");

		// <webServer>
		Element webServerNode = document.createElement("webServer");
		configurationNode.appendChild(webServerNode);

		// <webServer>
		//   <host>...</host><port>...</port><dir>...</dir>
		XML.add(webServerNode, "host", webHost);
		XML.add(webServerNode, "port", webPort);
		XML.add(webServerNode, "dir", webServerDocumentDirectory.toString());

		// <nameServer>
		Element nameServerNode = document.createElement("nameServer");
		nameServerNode.setAttribute("stateFrequency", String.valueOf(nameServerStateFrequency));
		configurationNode.appendChild(nameServerNode);

		// <nameServer>
		//   <rir> or <iiop>
		if (nameServerUsingRIRProtocol) {
			Element rirNode = document.createElement("rir");
			nameServerNode.appendChild(rirNode);
			if (nameServerObjectKey != null) {
			  XML.add(rirNode, "objectKey", nameServerObjectKey);
			}
		} else {
			Element iiopNode = document.createElement("iiop");
			nameServerNode.appendChild(iiopNode);
			if (nameServerVersion != null) {
			  XML.add(iiopNode, "version", nameServerVersion);
			}
			XML.add(iiopNode, "host", nameServerHost);
			if (nameServerPort != null) {
			  XML.add(iiopNode, "port", nameServerPort);
			}
			if (nameServerObjectKey != null) {
			  XML.add(iiopNode, "objectKey", nameServerObjectKey);
			}
		}

		// <xml><entityRef><dir>...
		if (!getEntityRefDirs().isEmpty()) {
			Element xmlNode = document.createElement("xml");
			configurationNode.appendChild(xmlNode);
			Element entityRefNode = document.createElement("entityRef");
			xmlNode.appendChild(entityRefNode);
			XML.add(entityRefNode, "dir", getEntityRefDirs());
		}

		// <serverMgr><port>...
		if (getServerMgrPort() != 0) {
			Element serverMgrNode = document.createElement("serverMgr");
			configurationNode.appendChild(serverMgrNode);
			XML.add(serverMgrNode, "port", String.valueOf(getServerMgrPort()));
		}

		// Global <properties>...</properties>
		if (properties.size() > 0) {
		  dumpProperties(properties, configurationNode);
		}

		// <programs>...
		if (execServers.size() > 0) {
			Element programsNode = document.createElement("programs");
			configurationNode.appendChild(programsNode);

		  for (Object execServer : execServers) {
			ExecServerConfig esc = (ExecServerConfig) execServer;
			Element execServerNode = document.createElement("execServer");
			programsNode.appendChild(execServerNode);
			XML.add(execServerNode, "class", esc.getClassName());
			XML.add(execServerNode, "objectKey", esc.getObjectKey());
			XML.add(execServerNode, "host", esc.getPreferredHost().toString());
			if (esc.getProperties().size() > 0) {
			  dumpProperties(esc.getProperties(), execServerNode);
			}
		  }
		}

		return configurationNode;
	}

	/** Merge the properties in the configuration into the given properties.
	 *
	 * Properties that already exist in the <var>targetProps</var> won't be
	 * overwritten.
	 *
	 * @param targetProps The target properties.
	 */
	public void mergeProperties(Properties targetProps) {
	  for (Map.Entry<Object, Object> objectObjectEntry : properties.entrySet()) {
		Map.Entry entry = (Map.Entry) objectObjectEntry;
		if (!targetProps.containsKey(entry.getKey())) {
		  targetProps.put(entry.getKey(), entry.getValue());
		}
	  }
	}

	/** Get the exec-server configurations.
	 *
	 * @return A collection of exec server configurations, each of class {@link ExecServerConfig}.
	 */
	public Collection getExecServerConfigs() {
		return execServers;
	}

        /** Get the exec-server configurations.
         *
         * @param clazz The class of exec servers that will be returned.
         * @return A collection of exec server configurations, each of class {@link ExecServerConfig}.
         */
        public Collection getExecServerConfigs(Class clazz) {
                String className = clazz.getName();
                Collection execServerConfigs = new ArrayList();
		  for (Object execServer : execServers) {
			ExecServerConfig exec = (ExecServerConfig) execServer;
			if (className.equals(exec.getClassName())) {
			  execServerConfigs.add(exec);
			}
		  }
                return execServerConfigs;
        }

        /** Get an exec-server configuration.
         *
         * @param objectKey The object key of the Exec Server to retrieve.
         * @return An {@link ExecServerConfig} or null if object key not found.
         */
        public ExecServerConfig getExecServerConfig(String objectKey) {
                ExecServerConfig execServerConfig = null;
                for (Iterator i = execServers.iterator(); i.hasNext() && execServerConfig == null;) {
                        ExecServerConfig exec = (ExecServerConfig) i.next();
                        if (objectKey.equals(exec.getObjectKey())) {
						  execServerConfig = exec;
						}
                }
                return execServerConfig;
        }

	/** Get the web server base URL.
	 *
	 * @return The base web server URL.
	 */
	public String getWebServerBaseURL() {
		String proto = System.getProperty(WEB_PROTOCOL_PROPERTY);
		if (proto == null) {
			if ("443".equals(webPort)) {
			  proto = "https";
			} else {
			  proto = "http";
			}
		}
		return proto + "://" + webHost + ":" + webPort;
	}

	/** Get the web server document directory.
	 *
	 * @return The document directory.
	 */
	public File getWebServerDocumentDirectory() {
		return webServerDocumentDirectory;
	}

	/** Get the name server URL.
	 *
	 * @return The name server URL.
	 */
	public String getNameServerURL() {
		return getWebServerBaseURL() + "/ns.ior";
	}

	/** Get the name server port, if any.
	 *
	 * @return The port.
	 */
	public String getNameServerPort() {
		return nameServerPort;
	}

	/** Get the frequency with which the name server saves its state.
	 *
	 * @return The state-save frequency in milliseconds; <= 0 means never save state.
	 */
	public int getNameServerStateFrequency() {
		return nameServerStateFrequency;
	}

	/** Get the object context.
	 *
	 * @return The object context based on this configuration.
	 * @throws NamingException If the context can't be created.
	 */
	public Context getObjectContext() throws NamingException {
		Context c;
		final String className = (String) contextEnvironment.get(javax.naming.Context.INITIAL_CONTEXT_FACTORY);
		if (className == null) {
		  c = new InitialContext(contextEnvironment);
		} else {
		  try {
			// Work around iPlanet bug.  JNDI uses the thread's context class
			// loader to load the initial context factory class.  For some
			// reason, that loader fails to find things in iPlanet's
			// classpath, such as the EDA initial context factory.  Here, we
			// cut a new thread and explicitly set its context class loader to
			// the application class loader.  When JNDI looks up the initial
			// context factory, the thread's context class loader is the app
			// class loader, which succeeds.
			Class clazz = Class.forName(className);
			final ClassLoader loader = clazz.getClassLoader();
			InitialContextThread thread = new InitialContextThread(loader);
			thread.start();
			try {
			  thread.join();
			} catch (InterruptedException ex) {
			  throw new NoInitialContextException("Initial context thread interrupted: " + ex.getMessage());
			}
			c = thread.getContext();
			if (c == null) {
			  throw thread.getException();
			}
		  } catch (ClassNotFoundException ex) {
			throw new NoInitialContextException("Class " + className + " not found");
		  }
		}
		return c;
	}

	/** Get the entity reference directories.
	 *
	 * @return A list of {@link java.lang.String}s naming directories for entity references.
	 */
	public List getEntityRefDirs() {
		List dirs = new ArrayList();
		for (StringTokenizer t = new StringTokenizer(System.getProperty(ENTITY_DIRS_PROP, ""), ",;|"); t.hasMoreTokens();) {
		  dirs.add(t.nextToken());
		}
		return dirs;
	}

	/** Get the port number on which the server manager is listening.
	 *
	 * @return The port number, or 0 if there is no server manager.
	 */
	public int getServerMgrPort() {
		return serverMgrPort;
	}

	/** Load the properties described in an XML properties element into the
	 * given properties object.
	 *
	 * @param propertiesNode The XML node which is a <code>&lt;properties&gt;</code> element.
	 * @param props The properties object to load with properties from <var>propertiesNode</var>.
	 */
	static void loadProperties(Node propertiesNode, Properties props) {
		NodeList children = propertiesNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i += 2) {
			String key = XML.unwrappedText(children.item(i));
			String value = XML.unwrappedText(children.item(i+1));
			props.setProperty(key, value);
		}
	}

	/** Dump the properties from the given properties object in XML form, appending
	 * them to the given node under a &lt;properties&gt; element.
	 *
	 * @param props The properties to dump in XML form.
	 * @param node The node to which to append the &lt;properties&gt; element.
	 * @throws DOMException If a DOM error occurs.
	 */
	static void dumpProperties(Properties props, Node node) {
		Element propertiesElement = node.getOwnerDocument().createElement("properties");
		node.appendChild(propertiesElement);
	  for (Map.Entry<Object, Object> objectObjectEntry : props.entrySet()) {
		Map.Entry entry = (Map.Entry) objectObjectEntry;
		XML.add(propertiesElement, "key", (String) entry.getKey());
		XML.add(propertiesElement, "value", (String) entry.getValue());
	  }
	}

	/** Create a new XML document with the configuration DTD.
	 *
	 * @returns An XML DOM document with the doctype and the root document empty element in place.
	 * @throws DOMException If we can't create the document.
	 */
	static Document createDocument(String documentElementName) throws DOMException {
		DocumentType docType = XML.getDOMImplementation().createDocumentType(documentElementName, DTD_FPI, DTD_URL);
	  return XML.getDOMImplementation().createDocument(/*namespaceURI*/null, documentElementName, docType);
	}

	/** The formal public identifier (FPI) of the configuration document type definition (DTD). */
	public static final String DTD_FPI = "-//JPL//DTD EDA Configuration 1.0//EN";
	
	/** The old formal public identifier (FPI) of the configuration document type definition (DTD). */
	public static final String DTD_OLD_FPI = "-//JPL//DTD OODT Configuration 1.0//EN";

	/** The system identifier of the configuration document type definition (DTD). */
	public static final String DTD_URL = "http://oodt.jpl.nasa.gov/edm-commons/Configuration.dtd";

	/** Name of the system property that names the web protocol to use. */
	public static final String WEB_PROTOCOL_PROPERTY = "org.apache.oodt.commons.Configuration.webProtocol";

	/** Global properties. */
	private Properties properties = new Properties();

	/** Object context environment. */
	Hashtable contextEnvironment = new Hashtable();

	/** Exec-servers. */
	private List execServers = new ArrayList();

	/** Web server host. */
	private String webHost;

	/** Web server port. */
	private String webPort;

	/** Web server doc dir. */
	private File webServerDocumentDirectory;

	/** Name server using rir protocol.
	 *
	 * If false, then it's using iiop.
	 */
	private boolean nameServerUsingRIRProtocol;

	/** Name server version. */
	private String nameServerVersion;

	/** Name server host. */
	private String nameServerHost;

	/** Name server port. */
	private String nameServerPort;

	/** Name server object key. */
	private String nameServerObjectKey;

	/** How often the name server saves its state. */
	private int nameServerStateFrequency;

	/** On what port the server manager will listen. */
	private int serverMgrPort;

	/** Thread to set a context class loader and get a JNDI initial context. */
	private class InitialContextThread extends Thread {
		/** Ctor
		 *
		 * @param loader What class loader to use as thread's context class loader.
		 */
		public InitialContextThread(ClassLoader loader) {
			setContextClassLoader(loader);
		}

		public void run() {
			try {
				context = new InitialContext(contextEnvironment);
			} catch (NamingException ex) {
				exception = ex;
			} catch (Exception t) {
				System.err.println("Unexpected throwable " + t.getClass().getName() + " getting initial context: "
					+ t.getMessage());
				t.printStackTrace();
			}
		}

		/** Get the context.
		 *
		 * <strong>Warning!</strong> Do not call this method until the thread terminates.
		 *
		 * @return The context, or null if the context could not be retrieved.
		 */
		public Context getContext() {
			return context;
		}

		/** Get any exception.
		 *
		 * @return Any exception that occurred while retrieving the context.
		 */
		public NamingException getException() {
			return exception;
		}

		/** JNDI context. */
		private Context context;

		/** Any exception. */
		private NamingException exception;
	}
}
