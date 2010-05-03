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


package gov.nasa.jpl.oodt.cas.metadata;

//JDK imports
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Map;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;

//OODT imports
import gov.nasa.jpl.oodt.cas.commons.xml.XMLUtils;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Metadata is a {@link Map} of <code>String</code> keys mapped to
 * <code>Vector</code> values. So, each key can map to potentially many
 * values, but also can map to null, or to a single value.
 * </p>
 * 
 */
public class Metadata {

    /* the map of elementName=>Elements */
    protected Map elementMap = null;

    /* our log stream */
    protected static final Logger LOG = Logger.getLogger(Metadata.class
            .getName());

    /**
     * <p>
     * Constructs a new Metadata
     * </p>
     */
    public Metadata() {
        elementMap = new Hashtable();
    }

    /**
     * @deprecated
     * <p>
     * Constructs a new Metadata from a given InputStream.
     * 
     * @param is
     *            The InputStream to read.
     */
    public Metadata(InputStream is) throws Exception {
        if (is == null) {
            throw new Exception(
                    "Unable to parse metadata stream: stream not set!");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder parser = factory.newDocumentBuilder();
            elementMap = new Hashtable();
            parse(parser.parse(new InputSource(is)));
        } catch (Exception e) {
            throw new Exception("Unable to parse metadata stream.", e);
        }
    }

    /**
     * <p>
     * Merges the existing Hashtable of metadata with the specified new
     * Hashtable of metadata.
     * </p>
     * 
     * @param metadata
     *            The metadata to merge the internal metadata with.
     */
    public void addMetadata(Hashtable metadata) {
        addMetadata(metadata, false);
    }

    /**
     * <p>
     * Merges the existing Hashtable of metadata with the specified new
     * Hashtable of metadata, replacing an existing metadata value if a new
     * metadata value with same key is specified, if the replace parameter is
     * set to true.
     * 
     * Otherwise, this function behaves the same as
     * {@link #addMetadata(Hashtable)}.
     * </p>
     * 
     * @param metadata
     *            The metadata to merge the internal metadata with.
     * @param replace
     *            If set to true, then an existing metadata value is replaced if
     *            a new metadata value with the same key is specified.
     *            Otherwise, if set to false then this function behaves as
     *            {@link #addMetadata(Hashtable)}.
     * @since OODT-143
     */
    public void addMetadata(Hashtable metadata, boolean replace) {
        // iterate through the keys and add values whereever possible, otherwise
        // replace them
        for (Iterator i = metadata.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            List values = null;

            if (elementMap.get(key) != null) {
                values = (List) elementMap.get(key);
                if (replace) {
                    values.clear();
                }
            } else {
                values = new Vector();
                elementMap.put(key, values);
            }

            if (metadata.get(key) != null) {
                Object val = metadata.get(key);

                if (val instanceof List) {
                    values.addAll((List) val);
                } else if (val instanceof String) {
                    values.add((String) val);
                }
            }
        }
    }

    /**
     * <p>
     * Replaces the internal hashtable of metadata with the specified
     * {@link Hashtable}.
     * </p>
     * 
     * @param metadata
     *            The metadata to replace the existing internal {@Hashtable}
     *            with.
     */
    public void replaceMetadata(Hashtable metadata) {
        elementMap = new Hashtable();
        for (Iterator i = metadata.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            Object val = metadata.get(key);

            List values = new Vector();
            if (val instanceof String) {
                values.add((String) val);
            } else if (val instanceof List) {
                values.addAll((List) val);
            }

            elementMap.put(key, values);
        }
    }

    /**
     * <p>
     * Adds a single String value to this metadata key. If the key already
     * exists, this value is appended to that list of metadata values. If the
     * key doesn't exist yet, a new list of metadata values is created and then
     * this value is inserted first, in order.
     * </p>
     * 
     * @param key
     *            The key to add the value for.
     * @param value
     *            The value to add.
     */
    public void addMetadata(String key, String value) {
        if (elementMap.get(key) != null) {
            // append this value to the existing key
            List values = (List) elementMap.get(key);
            values.add(value);
        } else {
            List values = new Vector();
            values.add(value);
            elementMap.put(key, values);
        }
    }

    /**
     * <p>
     * Adds a list of values to this metadata key. If the key already exists
     * then the list of values is appended to the existing list of values for
     * the key, in order. If the key doesn't exist, the list of String values
     * are set as the value list for this key.
     * </p>
     * 
     * @param key
     *            The key to set the values for.
     * @param values
     *            The ordered list of values for this String key. Values must be
     *            basic Strings.
     */
    public void addMetadata(String key, List values) {
        if (elementMap.get(key) != null) {
            // get the existing values
            List existingValues = (List) elementMap.get(key);
            existingValues.addAll(values);
        } else {
            elementMap.put(key, values);
        }
    }

    /**
     * <p>
     * Removes the existing value list for the specified <code>key</code>,
     * and replaces it with the specified {@link List} of <code>values</code>.
     * </p>
     * 
     * @param key
     *            The key to replace the metadata for.
     * @param values
     *            The new metadata values for this key.
     */
    public void replaceMetadata(String key, List values) {
        Object value = removeMetadata(key);
        value = null;
        elementMap.put(key, values);
    }

    /**
     * <p>
     * Removes the existing value list for the specified <code>key</code>,
     * and replaces it with the specified String value.
     * </p>
     * 
     * @param key
     *            The key to replace the metadata for.
     * @param value
     *            The new metadata value for this key.
     */
    public void replaceMetadata(String key, String value) {
        Object val = removeMetadata(key);
        val = null;
        List values = new Vector();
        values.add(value);
        elementMap.put(key, values);
    }

    /**
     * <p>
     * Removes the value of the specified metadata key.
     * </p>
     * 
     * @param key
     *            The key to remove the value from.
     * @return The removed value.
     */
    public Object removeMetadata(String key) {
        return elementMap.remove(key);
    }

    /**
     * <p>
     * Gets all metadata String values mapped to the specified <code>key</code>.
     * </p>
     * 
     * @param key
     *            The key to obtain multi-valued metadata for.
     * @return all metadata String values mapped to the specified
     *         <code>key</code>.
     */
    public List getAllMetadata(String key) {
        if (elementMap.get(key) != null) {
            return (List) elementMap.get(key);
        } else
            return null;
    }

    /**
     * <p>
     * Gets a single <code>String</code> value mapped to the specified
     * <code>key</code>.
     * </p>
     * 
     * @param key
     *            The key to obtain the single valued metadata for.
     * @return A single <code>String</code> value mapped to the specified
     *         <code>key</code>.
     */
    public String getMetadata(String key) {
        if (elementMap.get(key) != null) {
            List values = (List) elementMap.get(key);
            if (values.size() == 0) {
                return null;
            } else {
                return (String) values.get(0);
            }
        } else
            return null;
    }

    /**
     * 
     * @return The internal {@link Hashtable} representation of the metadata.
     */
    public Hashtable getHashtable() {
        return (Hashtable) elementMap;
    }

    /**
     * <p>
     * Test for existence of the specified key in the metadata element map.
     * </p>
     * 
     * @param key
     *            The key to check for existance of.
     * @return True if the key exists in the Metadata, false otherwise.
     */
    public boolean containsKey(String key) {
        return elementMap.containsKey(key);
    }

    /**
     * <p>
     * Checks to see whether or not a particular key is a multi-valued key
     * field.
     * </p>
     * 
     * @param key
     *            The key to perform the multi-valued check on.
     * @return True if the key is multi-valued, False, otherwise.
     */
    public boolean isMultiValued(String key) {
        if (elementMap.get(key) != null) {
            List values = (List) elementMap.get(key);

            if (values != null) {
                return values.size() > 1;
            } else
                return false;
        } else
            return false;
    }
    
    public boolean equals(Object obj) {
    	if (obj instanceof Metadata) {
    		return ((Metadata) obj).elementMap.size() == this.elementMap.size() 
    			&& ((Metadata) obj).elementMap.entrySet().containsAll(this.elementMap.entrySet());
    	}else
    		return false;
    }

    /**
     * @deprecated
     * <p>
     * @see gov.nasa.jpl.oodt.cas.metadata.SerializableMetadata for new toXML usage.
     * Returns an XML representation of this Metadata as an
     * <code>org.w3c.Document</code>.
     * </p>
     * 
     * @return an XML representation of this Metadata as an
     *         <code>org.w3c.Document</code>.
     * @throws Exception
     *             If any error occurs.
     */
    public Document toXML() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document = null;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();

            Element root = (Element) document.createElement("cas:metadata");
            root.setAttribute("xmlns:cas", "http://oodt.jpl.nasa.gov/1.0/cas");
            document.appendChild(root);

            // now add the set of metadata elements in the properties object
            for (Iterator i = elementMap.keySet().iterator(); i.hasNext();) {
                String elemName = null;

                try {
                    elemName = (String) i.next();
                    List elemValues = (List) elementMap.get(elemName);

                    Element metadataElem = document.createElement("keyval");
                    Element keyElem = document.createElement("key");
                    keyElem.appendChild(document.createTextNode(URLEncoder
                            .encode(elemName, "UTF-8")));

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
                                    + "for property: [" + elemName
                                    + "]: val: [" + elemValue + "]");
                        }
                        valElem.appendChild(document.createTextNode(URLEncoder
                                .encode(elemValue, "UTF-8")));
                        metadataElem.appendChild(valElem);
                    }
                    root.appendChild(metadataElem);
                } catch (UnsupportedEncodingException e) {
                    LOG.log(Level.WARNING, "Error encoding metadata "
                            + elementMap + " to xml file!");
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }

            }

        } catch (ParserConfigurationException pce) {
            LOG.log(Level.WARNING, "Error generating metadata xml file!: "
                    + pce.getMessage());
            throw new Exception("Error generating metadata xml file!: "
                    + pce.getMessage());
        }

        return document;
    }

    /**
     * @deprecated
     * @param document
     * @throws Exception
     */
    private void parse(Document document) throws Exception {

        Element root = document.getDocumentElement();

        NodeList keyValElems = root.getElementsByTagName("keyval");

        for (int i = 0; i < keyValElems.getLength(); i++) {
            Element keyValElem = (Element) keyValElems.item(i);

            String elemName = XMLUtils.read(keyValElem, "key");
            List elemValues = XMLUtils.readMany(keyValElem, "val");
            elementMap.put(elemName, elemValues);
        }

    }

}
