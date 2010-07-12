// Copyright 2001 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ExecServerConfig.java,v 1.2 2004-03-01 16:39:21 kelly Exp $

package jpl.eda;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import jpl.eda.util.Documentable;
import jpl.eda.util.XML;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Configuration for an EDA exec-server.
 *
 * @author Kelly
 */
public class ExecServerConfig extends Executable implements Documentable {
	/** Create an exec-server configuration.
	 *
	 * @param className Name of the class to execute.
	 * @param objectKey Object key under which class will register.
	 * @param properties Properties for the server.
	 */
	public ExecServerConfig(String className, String objectKey, InetAddress preferredHost, Properties properties) {
		this.className = className;
		this.objectKey = objectKey;
		this.preferredHost = preferredHost;
		this.properties = properties;
	}

	/** Create an exec-server configuration.
	 *
	 * @param xml XML DOM description, must be an &lt;execServer&gt; element.
	 * @throws SAXException If <var>xml</var> is invalid.
	 * @throws UnknownHostException If the <var>xml</var> refers to an unknown host name.
	 */
	public ExecServerConfig(Node xml) throws SAXException, UnknownHostException {
		properties = new Properties();
		preferredHost = jpl.eda.net.Net.getLoopbackAddress();
		NodeList children = xml.getChildNodes();
		className = XML.unwrappedText(children.item(0));
		objectKey = XML.unwrappedText(children.item(1));
		for (int i = 2; i < children.getLength(); ++i) {
			Node child = children.item(i);
			String name = child.getNodeName();
			if ("host".equals(name))
				preferredHost = InetAddress.getByName(XML.unwrappedText(children.item(2)));
			else if ("properties".equals(name))
				Configuration.loadProperties(child, properties);
			else throw new SAXException("Unknown node " + name + " in exec server XML");
		}
	}		

	/** Create an exec-server configuration.
	 *
	 * @param xml Serialized XML description.
	 * @throws SAXException If we can't parse <var>xml</var>.
	 * @throws UnknownHostException If the <var>xml</var> refers to an unknown host name.
	 */
	public ExecServerConfig(String xml) throws SAXException, UnknownHostException {
		this(XML.parse(xml).getDocumentElement());
	}

	protected String[] getCommandLine() {
		String[] commandLine = new String[6 + properties.size()];
		commandLine[0] = "java";
		commandLine[1] = "-Xms" + initialHeap;
		commandLine[2] = "-Xmx" + maxHeap;
		int index = 3;
		for (Iterator i = properties.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			commandLine[index++] = "-D" + entry.getKey() + "=" + entry.getValue();
		}
		commandLine[index++] = "jpl.eda.ExecServer";
		commandLine[index++] = className;
		commandLine[index++] = objectKey;
		return commandLine;
	}

	/** Get the class name I'm going to execute.
	 *
	 * @return The class name.
	 */
	public String getClassName() {
		return className;
	}

	/** Get the object key I'm going to use.
	 *
	 * @return The object key.
	 */
	public String getObjectKey() {
		return objectKey;
	}

	/** Get the properties for my process.
	 *
	 * @return The properties.
	 */
	public Properties getProperties() {
		return properties;
	}

	/** Get the preferred host.
	 *
	 * @return The host on which this server prefers to run.
	 */
	public InetAddress getPreferredHost() {
		return preferredHost;
	}

	public int hashCode() {
		return className.hashCode() ^ objectKey.hashCode() ^ properties.hashCode();
	}

	public boolean equals(Object rhs) {
		if (rhs == this) return true;
		if (rhs == null || !(rhs instanceof ExecServerConfig)) return false;
		ExecServerConfig obj = (ExecServerConfig) rhs;
		return className.equals(obj.className) && objectKey.equals(obj.objectKey) && properties.equals(obj.properties);
	}

	public String toString() {
		return className + " " + objectKey;
	}

	public Node toXML(Document doc) throws DOMException {
		Element execServerElement = doc.createElement("execServer");
		XML.add(execServerElement, "class", getClassName());
		XML.add(execServerElement, "objectKey", getObjectKey());
		XML.add(execServerElement, "preferredHost", getPreferredHost().toString());
		Configuration.dumpProperties(getProperties(), execServerElement);
		return execServerElement;
	}

	/** Yield this exce-server configuration as serialized XML.
	 *
	 * @return This object as a string serialized XML document.
	 * @throws DOMException If we can't create the XML document.
	 */
	public String toXML() throws DOMException {
		Document doc = Configuration.createDocument("execServer");
		doc.replaceChild(toXML(doc), doc.getDocumentElement());
		return XML.serialize(doc);
	}

	/** Class name for the ExecServer to execute. */
	private String className;

	/** Instance name. */
	private String objectKey;

	/** Properties for the server. */
	private Properties properties;

	/** Preferred host. */
	private InetAddress preferredHost;

	/** Initial heap size. */
	private static String initialHeap;

	/** Max heap size. */
	private static String maxHeap;

	/** Initialize the heap sizes. */
	static {
		Properties props = new Properties();
		jpl.eda.util.Utility.loadProperties(props, ExecServerConfig.class, "ExecServerConfig.properties");
		initialHeap = props.getProperty("initialHeap", "32m");
		maxHeap = props.getProperty("maxHeap", "128m");
	}
}
