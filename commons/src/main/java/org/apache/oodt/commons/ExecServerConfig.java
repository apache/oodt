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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import org.apache.oodt.commons.util.Documentable;
import org.apache.oodt.commons.util.XML;
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
		preferredHost = org.apache.oodt.commons.net.Net.getLoopbackAddress();
		NodeList children = xml.getChildNodes();
		className = XML.unwrappedText(children.item(0));
		objectKey = XML.unwrappedText(children.item(1));
		for (int i = 2; i < children.getLength(); ++i) {
			Node child = children.item(i);
			String name = child.getNodeName();
			if ("host".equals(name)) {
			  preferredHost = InetAddress.getByName(XML.unwrappedText(children.item(2)));
			} else if ("properties".equals(name)) {
			  Configuration.loadProperties(child, properties);
			} else {
			  throw new SAXException("Unknown node " + name + " in exec server XML");
			}
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
	  for (Map.Entry<Object, Object> objectObjectEntry : properties.entrySet()) {
		Map.Entry entry = (Map.Entry) objectObjectEntry;
		commandLine[index++] = "-D" + entry.getKey() + "=" + entry.getValue();
	  }
		commandLine[index++] = "org.apache.oodt.commons.ExecServer";
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
		if (rhs == this) {
		  return true;
		}
		if (rhs == null || !(rhs instanceof ExecServerConfig)) {
		  return false;
		}
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
		org.apache.oodt.commons.util.Utility.loadProperties(props, ExecServerConfig.class, "ExecServerConfig.properties");
		initialHeap = props.getProperty("initialHeap", "32m");
		maxHeap = props.getProperty("maxHeap", "128m");
	}
}
