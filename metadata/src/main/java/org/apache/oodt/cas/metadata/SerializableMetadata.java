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


package org.apache.oodt.cas.metadata;

//JDK imports
import org.apache.oodt.commons.xml.XMLUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * {@link Metadata} that's {@link Serializable}.
 * </p>.
 */
public class SerializableMetadata extends Metadata implements Serializable {

    private static final long serialVersionUID = 6863087581652632499L;

    private String xmlEncoding;

    private boolean useCDATA;

    /**
     * Accepts any encoding which is supported by java.net.URLEncoder If
     * useCDATA is set true then element text will be wrapped in a CDATA tag.
     * 
     * @param xmlEncoding
     *            The encoding to use when generating XML version of a
     *            SerializableMetadata
     * @param useCDATA
     *            whether or not to use CDATA tags around an element's text.
     * @throws InstantiationException
     *             if xmlEncoding equals null
     */
    public SerializableMetadata(String xmlEncoding, boolean useCDATA)
            throws InstantiationException {
        super();
        if (xmlEncoding == null)
            throw new InstantiationException("xmlEncoding cannot be null");
        this.xmlEncoding = xmlEncoding;
        this.useCDATA = useCDATA;
    }

    /**
     * @deprecated
     * 
     * @see org.apache.oodt.cas.metadata.Metadata#init(java.io.InputStream)
     */
    public SerializableMetadata(InputStream is, String xmlEncoding,
            boolean useCDATA) throws Exception {
        super(is);
        this.xmlEncoding = xmlEncoding;
        this.useCDATA = useCDATA;
    }

    /**
     * Builds a SerializableMetadata object from a Metadata object
     * 
     * @param metadata
     * @param xmlEncoding
     */
    public SerializableMetadata(Metadata metadata, String xmlEncoding,
            boolean useCDATA) {
        this.replaceMetadata(metadata.getHashtable());
        this.xmlEncoding = xmlEncoding;
        this.useCDATA = useCDATA;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(this.xmlEncoding);
        out.writeBoolean(useCDATA);
        this.writeMetadataToXmlStream(out);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        this.xmlEncoding = (String) in.readObject();
        this.useCDATA = in.readBoolean();
        this.loadMetadataFromXmlStream(in);
    }

    public String getEncoding() {
        return this.xmlEncoding;
    }

    public boolean isUsingCDATA() {
        return this.useCDATA;
    }

    /**
     * Writes out this SerializableMetadata object in XML format to the
     * OutputStream provided
     * 
     * @param os
     *            The OutputStream this method writes to
     * @throws IOException
     *             for any Exception
     */
    public void writeMetadataToXmlStream(OutputStream os) throws IOException {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(this.toXML());
            Result result = new StreamResult(os);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance()
                    .newTransformer();
            xformer.setOutputProperty(OutputKeys.ENCODING, this.xmlEncoding);
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error generating metadata xml file!: "
                    + e.getMessage());
        }
    }

    public Document toXML() throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            factory.setNamespaceAware(true);
            Document document = factory.newDocumentBuilder().newDocument();

            Element root = (Element) document.createElement("cas:metadata");
            // FIXME: change namespace URI?
            root.setAttribute("xmlns:cas", "http://oodt.jpl.nasa.gov/1.0/cas");
            document.appendChild(root);

            // now add the set of metadata elements in the properties object
            for (Iterator i = this.elementMap.keySet().iterator(); i.hasNext();) {
                String elemName = null;

                elemName = (String) i.next();
                List elemValues = (List) this.elementMap.get(elemName);

                Element metadataElem = document.createElement("keyval");
                Element keyElem = document.createElement("key");
                if (this.useCDATA)
                    keyElem.appendChild(document.createCDATASection(elemName));
                else
                    keyElem.appendChild(document.createTextNode(URLEncoder
                            .encode(elemName, this.xmlEncoding)));
                metadataElem.appendChild(keyElem);

                String type = "scalar";

                if (elemValues.size() > 1) {
                    type = "vector";
                }

                metadataElem.setAttribute("type", type);

                for (Iterator j = elemValues.iterator(); j.hasNext();) {
                    String elemValue = (String) j.next();
                    Element valElem = document.createElement("val");
                    if (elemValue == null) {
                        throw new Exception("Attempt to write null value "
                                + "for property: [" + elemName + "]: val: ["
                                + elemValue + "]");
                    }
                    if (this.useCDATA)
                        valElem.appendChild(document
                                .createCDATASection(elemValue));
                    else
                        valElem.appendChild(document.createTextNode(URLEncoder
                                .encode(elemValue, this.xmlEncoding)));
                    metadataElem.appendChild(valElem);
                }
                root.appendChild(metadataElem);
            }
            return document;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(
                    "Failed to create XML DOM Document for SerializableMetadata : "
                            + e.getMessage());
        }
    }

    /**
     * Reloads this SerializableMetadata from an InputStream in the format
     * created by writeMetadataToXmlStream(OutputStream).
     * 
     * @param in
     *            The InputStream which this object is loaded from
     * @throws IOException
     *             for any exception
     */
    public void loadMetadataFromXmlStream(InputStream in) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder parser = factory.newDocumentBuilder();
            elementMap = new Hashtable();
            Element root = parser.parse(new InputSource(in))
                    .getDocumentElement();

            NodeList keyValElems = root.getElementsByTagName("keyval");

            for (int i = 0; i < keyValElems.getLength(); i++) {
                Element keyValElem = (Element) keyValElems.item(i);

                String elemName = XMLUtils.read(keyValElem, "key",
                        this.xmlEncoding);
                List elemValues = XMLUtils.readMany(keyValElem, "val",
                        this.xmlEncoding);
                elementMap.put(elemName, elemValues);
            }
        } catch (Exception e) {
            throw new IOException(
                    "Failed to load SerializableMetadata from ObjectInputStream : "
                            + e.getMessage());
        }
    }

    /**
     * Converts SerializableMetadata into a plain metadata object
     * 
     * @return Metadata object with the same metadata that this
     *         SerializableMetadata contains
     */
    public Metadata getMetadata() {
        Metadata metadata = new Metadata();
        metadata.addMetadata(this.getHashtable());
        return metadata;
    }

}
