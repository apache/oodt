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


package org.apache.oodt.xmlquery;

import org.apache.oodt.commons.util.Documentable;
import org.apache.oodt.commons.util.XML;
import org.apache.oodt.product.Retriever;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/** A single result.
 *
 * An object of this class is a result of a query.
 *
 * @author Kelly
 */
public class Result implements Serializable, Cloneable, Documentable {
	/** Create a new, blank result.
	 *
	 * This initializes the result with default values for various properties.
	 */
	public Result() {
		this(/*id*/"UNKNOWN", /*mimeType*/"UNKNOWN", /*profileId*/"UNKNOWN", /*resourceId*/"UNKNOWN",
			/*resultHeader*/ new ArrayList(), /*value*/"");
	}

	/** Create a result.
	 *
	 * Here, you specify the result's ID and value only.
	 *
	 * @param id Identification of this result.
	 * @param value The result.
	 */
	public Result(String id, Object value) {
		this(id, /*mimeType*/"UNKNOWN", /*profileId*/"UNKNOWN", /*resourceId*/"UNKNOWN",
			/*resultHeader*/ new ArrayList(), value);
	}

	/**
	 * Create a more fully specified result that's not classified and lasts forever.
	 *
	 * @param id Identification of this result.
	 * @param mimeType MIME Type.
	 * @param profileID ID of the resource profile where this result originated.
	 * @param resourceID ID of the resource where this result originated.
	 * @param headers A header elements, describing the result.
	 * @param value The result.
	 */
	public Result(String id, String mimeType, String profileID, String resourceID, List headers, Object value) {
		this(id, mimeType, profileID, resourceID, headers, value, /*classified*/false, /*validity*/INFINITE);
	}

	/**
	 * Create a fully specified result.
	 *
	 * @param id Identification of this result.
	 * @param mimeType MIME Type.
	 * @param profileID ID of the resource profile where this result originated.
	 * @param resourceID ID of the resource where this result originated.
	 * @param headers A header elements, describing the result.
	 * @param value The result.
	 * @param classified True if this result is secret, false otherwise.
	 * @param validity Time for how long this product is valid in milliseconds or {@link #INFINITE}.
	 */
	public Result(String id, String mimeType, String profileID, String resourceID, List headers, Object value,
		boolean classified, long validity) {
		if (validity < 0 && validity != INFINITE) {
		  throw new IllegalArgumentException("Validity must be a nonnegative time in milliseconds or "
											 + " Result.INFINITE to indicate no expiration");
		}
		if (!codecs.containsKey(mimeType)) {
		  throw new IllegalArgumentException("MIME type \"" + mimeType + "\" unknown");
		}
	  for (Object header : headers) {
		if (!(header instanceof Header)) {
		  throw new IllegalArgumentException("List of headers doesn't contain Header object");
		}
	  }

		this.id         = id;
		this.mimeType   = mimeType;
		this.profileID  = profileID;
		this.resourceID = resourceID;
		this.headers    = headers;
		this.value      = value;
		this.classified = classified;
		this.validity   = validity;
	}

	/** Create a result from a DOM node.
	 *
	 * @param node The DOM node, which must be a &lt;resultElement&gt; element.
	 */
	public Result(Node node) {
		if (!"resultElement".equals(node.getNodeName())) {
		  throw new IllegalArgumentException("Result must be constructed from <resultElement> node, not <"
											 + node.getNodeName() + ">");
		}
		Element rootElement = (Element) node;
		classified = "true".equals(rootElement.getAttribute("classified"));
		validity = Long.parseLong(rootElement.getAttribute("validity"));
		NodeList children = node.getChildNodes();
		String encodedValue = null;
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if ("resultId".equals(child.getNodeName())) {
			  id = XML.unwrappedText(child);
			} else if ("resultMimeType".equals(child.getNodeName())) {
			  mimeType = XML.unwrappedText(child);
			} else if ("profId".equals(child.getNodeName())) {
			  profileID = XML.unwrappedText(child);
			} else if ("identifier".equals(child.getNodeName())) {
			  resourceID = XML.unwrappedText(child);
			} else if ("resultHeader".equals(child.getNodeName())) {
			  headers = Header.createHeaders(child);
			} else if ("resultValue".equals(child.getNodeName())) {
				Codec codec = (Codec) codecs.get(mimeType);
				if (codec == null) {
				  throw new IllegalArgumentException("Unkown MIME type \"" + mimeType
													 + "\" in <resultElement>'s <resultMimeType>");
				}
				try {
					value = codec.decode(child);
				} catch (RuntimeException ex) {
					throw ex;
				} catch (Exception ex) {
					throw new IllegalArgumentException("Bad encoding of " + mimeType + " object");
				}
			}
		}
	}

	/** Get the result ID.
	 *
	 * @return The identification of this result.
	 */
	public String getID() {
		return id;
	}

	public URI getURIID() {
		return URI.create(id.startsWith("urn")? id : "urn:eda:result:unspec:" + id);
	}

	/** Get the MIME type of this result.
	 *
	 * @return The MIME type.
	 */
	public String getMimeType() {
		return mimeType;
	}

	/** Get the profile ID.
	 *
	 * @return The ID of the resource profile where this result originated.
	 */
	public String getProfileID() {
		return profileID;
	}

	/** Get the resource ID.
	 *
	 * @return The ID of the resource where this result originated.
	 */
	public String getResourceID() {
		return resourceID;
	}

	/** Get the headers.
	 *
	 * @return A list of {@link Header}s describing the result.
	 */
	public List getHeaders() {
		return headers;
	}

	/** Get the result's value.
	 *
	 * @return The result instance.
	 * @deprecated This method requires the caller to know the return type and to
	 * downcast to it; further, the result may be too large to contain in memory.  Use
	 * {@link #getInputStream} instead to perform stream processing on product data.
	 */
	public Object getValue() {
		return value;
	}

	/** Set the result ID.
	 *
	 * @param id The identification of this result.
	 */
	public void setID(String id) {
		this.id = id;
	}

	/** Set the MIME type of this result.
	 *
	 * @param mimeType The MIME type.
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/** Set the profile ID.
	 *
	 * @param profileID The ID of the resource profile where this result originated.
	 */
	public void setProfileID(String profileID) {
		this.profileID = profileID;
	}

	/** Set the resource ID.
	 *
	 * @param resourceID The ID of the resource where this result originated.
	 */
	public void setResourceID(String resourceID) {
		this.resourceID = resourceID;
	}

	/** Set the result's value.
	 *
	 * @param value The result instance.
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Get the size of this product.
	 *
	 * @return Size in bytes.
	 */
	public long getSize() {
		Codec codec = (Codec) codecs.get(mimeType);
		if (codec == null) {
		  throw new IllegalStateException("No codec available for supposedly valid MIME type \""
										  + mimeType + "\"");
		}
		return codec.sizeOf(value);
	}

	public Node toXML(Document doc) throws DOMException {
		Element root = doc.createElement("resultElement");
		root.setAttribute("classified", String.valueOf(classified));
		root.setAttribute("validity", String.valueOf(validity));
		XML.add(root, "resultId", id);
		XML.add(root, "resultMimeType", mimeType);
		XML.add(root, "profId", profileID);
		XML.add(root, "identifier", resourceID);
		Element resultHeader = doc.createElement("resultHeader");
		root.appendChild(resultHeader);
	  for (Object header1 : headers) {
		Header header = (Header) header1;
		resultHeader.appendChild(header.toXML(doc));
	  }
		Codec codec = (Codec) codecs.get(mimeType);
		if (codec == null) {
		  throw new IllegalStateException("No codec available for supposedly valid MIME type \""
										  + mimeType + "\"");
		}
		root.appendChild(codec.encode(value, doc));
		return root;
	}

	/**
	 * Get an input stream version of the result's value.
	 *
	 * @return an <code>InputStream</code> value.
	 * @throws IOException if an error occurs.
	 */
	public InputStream getInputStream() throws IOException {
		Codec codec = (Codec) codecs.get(mimeType);
		if (codec == null) {
		  throw new IllegalStateException("No codec available for allegedly valid MIME type \""
										  + mimeType + "\"");
		}
		return codec.getInputStream(value);
	}

	/**
	 * Is this result classified?
	 *
	 * @return a boolean value.
	 */
	public boolean isClassified() {
		return classified;
	}

	/**
	 * Set whether this result is classified.
	 *
	 * @param classified a boolean value.
	 */
	public void setClassified(boolean classified) {
		this.classified = classified;
	}

	/**
	 * Get how long this product is valid.
	 *
	 * @return Time in milliseconds or {@link #INFINITE}.
	 */
	public long getValidity() {
		return validity;
	}

	/**
	 * Set the time this product is valid.
	 *
	 * @param validity Time in milliseconds or {@link #INFINITE}.
	 */
	public void setValidity(long validity) {
		this.validity = validity;
	}

	public int hashCode() {
		return id.hashCode() ^ mimeType.hashCode() ^ profileID.hashCode() ^ resourceID.hashCode() ^ headers.hashCode()
			^ value.hashCode();
	}

	public boolean equals(Object rhs) {
		if (rhs == this) {
		  return true;
		}
		if (rhs == null || !(rhs instanceof Result)) {
		  return false;
		}
		Result obj = (Result) rhs;
		return id.equals(obj.id) && mimeType.equals(obj.mimeType) && profileID.equals(obj.profileID)
			&& resourceID.equals(obj.resourceID) && headers.equals(obj.headers) && value.equals(obj.value);
	}

	public Object clone() {
		Object rc;
		try {
			rc = super.clone();
		} catch (CloneNotSupportedException cantHappen) {
			throw new RuntimeException("CloneNotSupportedException thrown for class that implements Cloneable: "
				+ cantHappen.getMessage());
		}
		return rc;
	}

	public void setRetriever(Retriever retriever) {
		if (retriever == null) {
		  throw new IllegalArgumentException("retriever must be non-null");
		}
		if (this.retriever == null) {
		  this.retriever = retriever;
		}
	}

	public String toString() {
		return getClass().getName() + "[id=" + getID() + ",mimeType=" + getMimeType() + ",profileID=" + getProfileID()
			+ ",resourceID=" + getResourceID() + ",value=" + getValue() + "]";
	}

	/** The identification of this result. */
	protected String id;

	/** The MIME type. */
	protected String mimeType;

	/** The ID of the resource profile where this result originated. */
	private String profileID;

	/** The ID of the resource where this result originated. */
	private String resourceID;

	/** The headers describing the result. */
	private List headers;

	/** The result instance. */
	protected Object value;

	/** True if this product is classified. */
	private boolean classified;

	/** For how long the product's good. */
	private long validity;

	/** Mapping of MIME type to codec. */
	protected static Map codecs;

	/** Object to retrieve this product's data. */
	protected transient Retriever retriever;

	/** Initialize the set of valid MIME types. */
	static {
		codecs = new ConcurrentHashMap();
		try {
			java.io.InputStream inp = Result.class.getResourceAsStream("mime.properties");
			BufferedInputStream in = new BufferedInputStream(inp);
			Properties props = new Properties();
			props.load(in);
			in.close();
		  for (Map.Entry<Object, Object> objectObjectEntry : props.entrySet()) {
			Map.Entry entry = (Map.Entry) objectObjectEntry;
			codecs.put(entry.getKey(), CodecFactory.createCodec((String) entry.getValue()));
		  }
		} catch (IOException ex) {
			System.err.println("I/O exception WHILE reading mime.properties: " + ex.getMessage());
			ex.printStackTrace();
			System.err.println("No valid MIME types will be recognized by class Result");
		}

		// Add our own special MIME type:
		codecs.put("UNKNOWN",CodecFactory.createCodec("org.apache.oodt.xmlquery.UnsupportedMimeTypeCodec"));
	}

	/** Serial version unique ID. */
	static final long serialVersionUID = 9169143944191239575L;

	/** Sentinel value for validity that indicates a product never expires. */
	public static final long INFINITE = -1;
}
