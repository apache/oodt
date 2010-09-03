/**
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

package org.apache.oodt.grid;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import org.apache.oodt.commons.util.Base64;

/**
 * web-grid configuration. This holds all the runtime configuration of profile servers,
 * product servers, properties, and other settings for the web-grid container.
 *
 */
public class Configuration implements Serializable {
    /**
     * Creates a new <code>Configuration</code> instance.
     *
     * @param file File containing the serialized configuration.
     * @throws IOException if an I/O error occurs.
     * @throws SAXException if the file can't be parsed.
     */
    public Configuration(File file) throws IOException, SAXException {
        this.file = file;
        if (file.isFile() && file.length() > 0)
            parse(file);
    }

    /**
     * Convert the configuration to XML.
     *
     * @param owner Owning document.
     * @return XML representation of this configuration.
     */
    public Node toXML(Document owner) {
        Element elem = owner.createElement("configuration");                    // <configuration>
        elem.setAttribute("xmlns", NS);                                         // Set default namespace
        elem.setAttribute("https", httpsRequired? "true" : "false");            // Add https attribute
        elem.setAttribute("localhost", localhostRequired? "true" : "false");    // Add localhost attribute

        if (password != null && password.length > 0)                            // If we have a password
            elem.setAttribute("password", encode(password));                    // Add passowrd attribute

        for (Iterator i = productServers.iterator(); i.hasNext();) {            // For each product server
            ProductServer ps = (ProductServer) i.next();                        // Get the product server
            elem.appendChild(ps.toXML(owner));                                  // And add it under <configuration>
        }

        for (Iterator i = profileServers.iterator(); i.hasNext();) {            // For each profile server
            ProfileServer ps = (ProfileServer) i.next();                        // Get the profile server
            elem.appendChild(ps.toXML(owner));                                  // And add it under the <configuration>
        }

        if (!codeBases.isEmpty()) {                                             // Got any code bases?
            Element cbs = owner.createElement("codeBases");                     // Boo yah.  Make a parent for 'em
            elem.appendChild(cbs);                                              // Add parent
            for (Iterator i = codeBases.iterator(); i.hasNext();) {             // Then, for each code base
                URL url = (URL) i.next();                                       // Get the URL to it
                Element cb = owner.createElement("codeBase");                   // And make a <codeBase> for it
                cb.setAttribute("url", url.toString());                         // And an "url" attribute
                cbs.appendChild(cb);                                            // Add it
            }
        }

        if (!properties.isEmpty()) {                                            // If we have any properties>
            Element props = owner.createElement("properties");                  // Add <properties> under <configuration>
            props.setAttribute("xml:space", "preserve");                        // And make sure space is properly preserved
            elem.appendChild(props);                                            // Add the space attribute
            for (Iterator i = properties.entrySet().iterator(); i.hasNext();) { // For each property
                Map.Entry entry = (Map.Entry) i.next();                         // Get the property key/value pair
                String key = (String) entry.getKey();                           // Key is always a String
                String value = (String) entry.getValue();                       // So is the value
                Element prop = owner.createElement("property");                 // Create a <property> element
                props.appendChild(prop);                                        // Add it under the <properties>
                prop.setAttribute("key", key);                                  // Set the key as an attribute
                Text text = owner.createTextNode(value);                        // Make text to hold the value
                prop.appendChild(text);                                         // Add it under the <property>
            }
        }
        return elem;
    }

    /**
     * Get the properties.
     *
     * @return a <code>Properties</code> value.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Is localhost access required for the configuration?
     *
     * @return True if the configuration must be accessed from the localhost, false otherwise.
     */
    public boolean isLocalhostRequired() {
        return localhostRequired;
    }

    /**
     * Is https access required for the configuration?
     *
     * @return True if the configuration must be accessed via https, false if http is OK.
     */
    public boolean isHTTPSrequired() {
        return httpsRequired;
    }

    /**
     * Set if https is required to access the configuration.
     *
     * @param required True if the configuration must be accessed via https, false if http is OK.
     */
    public void setHTTPSrequired(boolean required) {
        httpsRequired = required;
    }

    /**
     * Set if localhost is required to access the configuration.
     *
     * @param required True if the configuration must be accessed from the localhost, false otherwise.
     */
    public void setLocalhostRequired(boolean required) {
        localhostRequired = required;
    }

    /**
     * Return the code bases.
     *
     * @return a <code>List</code> of {@link URL}s.
     */
    public List getCodeBases() {
        return codeBases;
    }

    /**
     * Get the product servers.
     *
     * @return a <code>List</code> of {@link ProductServer}s.
     */
    public List getProductServers() {
        return productServers;
    }

    /**
     * Get the profile servers.
     *
     * @return a <code>List</code> of {@link ProfileServer}s.
     */
    public List getProfileServers() {
        return profileServers;
    }

    /**
     * Get the administrator password.
     *
     * @return Administrator password.
     */
    public byte[] getPassword() {
        return password;
    }

    /**
     * Set the administrator password.
     *
     * @param password Administrator password.
     */
    public void setPassword(byte[] password) {
        if (password == null)
            throw new IllegalArgumentException("Non-null passwords not allowed");
        this.password = password;
    }

    /**
     * Save the configuration.
     *
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void save() throws IOException {
        BufferedWriter writer = null;                           // Start w/no writer
        try {                                                   // Then try ...
            writer = new BufferedWriter(new FileWriter(file));  // Create a writer
            Document doc;                                       // As for the doc...
            synchronized (DOCUMENT_BUILDER) {                   // Using the document builder...
                doc = DOCUMENT_BUILDER.newDocument();           // Create an empty document
            }
            Node root = toXML(doc);                             // Convert this config to XML
            doc.appendChild(root);                              // Add it to the doc
            DOMSource source = new DOMSource(doc);              // Use the source Luke
            StreamResult result = new StreamResult(writer);     // And serialize it to the writer
            TRANSFORMER.transform(source, result);              // Serialize
        } catch (TransformerException ex) {
            throw new IllegalStateException("Unexpected TransformerException: " + ex.getMessage());
        } finally {
            if (writer != null) try {                           // And if we got a writer, try ...
                writer.close();                                 // to close it
            } catch (IOException ignore) {}                     // Ignoring any error
        }
    }

    /**
     * Parse a serialized configuration document.
     *
     * @param file File to parse
     * @throws IOException if an I/O error occurs.
     * @throws SAXException if a parse error occurs.
     */
    private void parse(File file) throws IOException, SAXException {
        Document doc;                                                               // Start with a doc ...
        synchronized (DOCUMENT_BUILDER) {                                           // And using the DOCUMENT_BUILDER
            doc = DOCUMENT_BUILDER.parse(file);                                     // Try to parse the file
        }
        Element root = doc.getDocumentElement();                                    // Assume the root element is <configuration>

        String httpsAttr = root.getAttribute("https");                              // Get the https attribute
        httpsRequired = httpsAttr != null && "true".equals(httpsAttr);              // See if it's "true"

        String localhostAttr = root.getAttribute("localhost");                      // Get the localhost attribute
        localhostRequired = localhostAttr != null && "true".equals(localhostAttr);  // See if it's "true"

        String passwordAttr = root.getAttribute("password");                        // Get the password attribute
        if (passwordAttr != null && passwordAttr.length() > 0)                      // If it's there, and non-empty
            password = decode(passwordAttr);                                        // Then decode it

        NodeList children = root.getChildNodes();                                   // Get the child nodes
        for (int i = 0; i < children.getLength(); ++i) {                            // For each child node
            Node child = children.item(i);                                          // Get the child
            if (child.getNodeType() == Node.ELEMENT_NODE) {                         // An element?
                if ("server".equals(child.getNodeName())) {                         // A <server>?
                    Server server = Server.create(this, (Element) child);           // Create the correct server

                    // Keep these in separate sets?
                    if (server instanceof ProductServer)                            // Is a product server?
                        productServers.add(server);                                 // Add to product servers
                    else if (server instanceof ProfileServer)                       // Is a profile server?
                        profileServers.add(server);                                 // Add to profile servers
                    else throw new IllegalArgumentException("Unexpected server type " + server + " in " + file);
                } else if ("properties".equals(child.getNodeName())) {              // Is it a <properties>?
                    NodeList props = child.getChildNodes();                         // Get its children
                    for (int j = 0; j < props.getLength(); ++j) {                   // For each child
                        Node node = props.item(j);                                  // Get the chld
                        if (node.getNodeType() == Node.ELEMENT_NODE                 // And element?
                            && "property".equals(node.getNodeName())) {             // And it's <property>?
                            Element propNode = (Element) node;                      // Great, use it as an element
                            String key = propNode.getAttribute("key");              // Get its key attribute
                            if (key == null || key.length() == 0)                   // Make sure it's there
                                throw new SAXException("Required 'key' attribute missing from "
                                    + "<property> element");
                            properties.setProperty(key, text(propNode));            // And set it
                        }
                    }
                } else if ("codeBases".equals(child.getNodeName())) {               // And yadda
                    NodeList cbs = child.getChildNodes();                           // yadda
                    for (int j = 0; j < cbs.getLength(); ++j) {                     // yadda.
                        Node node = cbs.item(j);
                        if (node.getNodeType() == Node.ELEMENT_NODE
                            && "codeBase".equals(node.getNodeName())) {
                            Element cbNode = (Element) node;
                            String u = cbNode.getAttribute("url");
                            if (u == null || u.length() == 0)
                                throw new SAXException("Required 'url' attribute missing from "
                                    + "<codeBase> element");
                            try {
                                codeBases.add(new URL(u));
                            } catch (MalformedURLException ex) {
                                throw new SAXException("url attribute " + u + " isn't a valid URL");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Encode a password.  This just hides it so it's not plain text, but it's just as
     * easy to decode it if you have a base-64 decoder handy.
     *
     * @param password a <code>byte[]</code> value.
     * @return a <code>String</code> value.
     */
    static String encode(byte[] password) {
        return new String(Base64.encode(password));
    }

    /**
     * Decode a password.
     *
     * @param password a <code>String</code> value.
     * @return a <code>byte[]</code> value.
     */
    static byte[] decode(String password) {
        return Base64.decode(password.getBytes());
    }

    /**
     * Get the text under an XML node.
     *
     * @param node a <code>Node</code> value.
     * @return a <code>String</code> value.
     */
    private static String text(Node node) {
        StringBuffer b = new StringBuffer();
        text0(node, b);
        return b.toString();
    }

    /**
     * Get the text from an XML node into a StringBuffer. 
     *
     * @param node a <code>Node</code> value.
     * @param b a <code>StringBuffer</code> value.
     */
    private static void text0(Node node, StringBuffer b) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i)
            text0(children.item(i), b);
        short type = node.getNodeType();
        if (type == Node.CDATA_SECTION_NODE || type == Node.TEXT_NODE)
            b.append(node.getNodeValue());
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Configuration) {
            Configuration rhs = (Configuration) obj;
            return codeBases.equals(rhs.codeBases) && productServers.equals(rhs.productServers)
                && profileServers.equals(rhs.profileServers) && Arrays.equals(password, rhs.password)
                && httpsRequired == rhs.httpsRequired && localhostRequired == rhs.localhostRequired
                && properties.equals(rhs.properties);
        }
        return false;
    }

    public int hashCode() {
        return codeBases.hashCode() ^ productServers.hashCode() ^ profileServers.hashCode();
    }

    /** List of {@link URL}s to code bases. */
    private List codeBases = new ArrayList();

    /** List of {@link ProductServer}s. */
    private List productServers = new ArrayList();

    /** List of {@link ProfileServer}s. */
    private List profileServers = new ArrayList();

    /** Admin password. */
    private byte[] password = DEFAULT_PASSWORD;

    /** True if https is requried. */
    private boolean httpsRequired;

    /** True if localhost access is required. */
    private boolean localhostRequired;

    /** Properties to set. */
    private Properties properties = new Properties();

    /** Where to save the file. */
    private transient File file;

    /** Default password. */
    static final byte[] DEFAULT_PASSWORD = { (byte)'h', (byte)'a', (byte)'n', (byte)'a', (byte)'l', (byte)'e', (byte)'i' };

    /** XML namespace */
    public static final String NS = "http://oodt.jpl.nasa.gov/web-grid/ns/";

    /** Sole document builder we'll need. */
    private static final DocumentBuilder DOCUMENT_BUILDER;

    /** Sole transfomer we'll need. */
    private static final Transformer TRANSFORMER;

    static {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setValidating(false);
            documentBuilderFactory.setIgnoringElementContentWhitespace(false);
            documentBuilderFactory.setExpandEntityReferences(true);
            documentBuilderFactory.setIgnoringComments(true);
            documentBuilderFactory.setCoalescing(true);
            DOCUMENT_BUILDER = documentBuilderFactory.newDocumentBuilder();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            TRANSFORMER = transformerFactory.newTransformer();
            TRANSFORMER.setOutputProperty(OutputKeys.METHOD, "xml");
            TRANSFORMER.setOutputProperty(OutputKeys.VERSION, "1.0");
            TRANSFORMER.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            TRANSFORMER.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            TRANSFORMER.setOutputProperty(OutputKeys.INDENT, "yes");
            TRANSFORMER.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException("Cannot create document builder");
        } catch (TransformerConfigurationException ex) {
            throw new IllegalStateException("Cannot create transformer");
        }
    }
}
