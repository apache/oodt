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

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.net.URI;

/** Header of a query
 *
 * @author Kelly
 */
public class QueryHeader implements Serializable, Cloneable, Documentable {
	/** Create a blank query header.
	 */
	public QueryHeader() {
		this(/*id*/"UNKNOWN", /*title*/"UNKNOWN", /*desc*/"UNKNOWN", /*type*/"QUERY", /*status*/"ACTIVE",
			/*security*/"UNKNOWN", /*rev*/"1999-12-12 JSH V1.0 Under Development", /*datadict*/"UNKNOWN");
	}

	/** Create a query header with the specified values.
	 *
	 * @param id The identification of this query.
	 * @param title The title of this query.
	 * @param description A string describing this query.
	 * @param type The type of the query, usually QUERY.
	 * @param statusID The status of the query.
	 * @param securityType The type of security to apply to this query.
	 * @param revisionNote A note about the revision history of this query.
	 * @param dataDictID The ID of the data dictionary used by this query.
	 */
	public QueryHeader(String id, String title, String description, String type, String statusID, String securityType,
		String revisionNote, String dataDictID) {
		this.id           = id;
		this.title        = title;
		this.description  = description;
		this.type         = type;
		this.statusID     = statusID;
		this.securityType = securityType;
		this.revisionNote = revisionNote;
		this.dataDictID   = dataDictID;
	}

	/** Create a query header from an XML node.
	 *
	 * @param node The &lt;queryAttributes&gt; node.
	 */
	public QueryHeader(Node node) {
		if (!"queryAttributes".equals(node.getNodeName())) {
		  throw new IllegalArgumentException("QueryHeader must be constructed from <queryAttributes> node, not <"
											 + node.getNodeName() + ">");
		}
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if ("queryId".equals(child.getNodeName())) {
			  id = XML.unwrappedText(child);
			} else if ("queryTitle".equals(child.getNodeName())) {
			  title = XML.unwrappedText(child);
			} else if ("queryDesc".equals(child.getNodeName())) {
			  description = XML.unwrappedText(child);
			} else if ("queryType".equals(child.getNodeName())) {
			  type = XML.unwrappedText(child);
			} else if ("queryStatusId".equals(child.getNodeName())) {
			  statusID = XML.unwrappedText(child);
			} else if ("querySecurityType".equals(child.getNodeName())) {
			  securityType = XML.unwrappedText(child);
			} else if ("queryRevisionNote".equals(child.getNodeName())) {
			  revisionNote = XML.unwrappedText(child);
			} else if ("queryDataDictId".equals(child.getNodeName())) {
			  dataDictID = XML.unwrappedText(child);
			}
		}
	}

	/** Get the identification of this query.
	 *
	 * @return The identification of this query.
	 */
	public String getID() {
		return id;
	}

 	/**
         * Get the identification of this query as a URI.
         *
         * @return an <code>URI</code> value.
         */
        public URI getURIID() {
                return URI.create(id.startsWith("urn")? id : "urn:oodt:query:" + id);
        }


	/** Get the title of this query.
	 *
	 * @return The title of this query.
	 */
	public String getTitle() {
		return title;
	}

	/** Get a string describing this query.
	 *
	 * @return A string describing this query.
	 */
	public String getDescription() {
		return description;
	}

	/** Get the type of the query, usually QUERY.
	 *
	 * @return The type of the query, usually QUERY.
	 */
	public String getType() {
		return type;
	}

	/** Get the status of the query.
	 *
	 * @return The status of the query.
	 */
	public String getStatusID() {
		return statusID;
	}

	/** Get the type of security to apply to this query.
	 *
	 * @return The type of security to apply to this query.
	 */
	public String getSecurityType() {
		return securityType;
	}

	/** Get a note about the revision history of this query.
	 *
	 * @return A note about the revision history of this query.
	 */
	public String getRevisionNote() {
		return revisionNote;
	}

	/** Get the ID of the data dictionary used by this query.
	 *
	 * @return The ID of the data dictionary used by this query.
	 */
	public String getDataDictID() {
		return dataDictID;
	}

	/** Set the identification of this query.
	 *
	 * @param id The identification of this query.
	 */
	public void setID(String id) {
		this.id = id;
	}

	/** Set the title of this query.
	 *
	 * @param title The title of this query.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/** Set a string describing this query.
	 *
	 * @param description A string describing this query.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/** Set the type of the query, usually QUERY.
	 *
	 * @param type The type of the query, usually QUERY.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/** Set the status of the query.
	 *
	 * @param statusID The status of the query.
	 */
	public void setStatusID(String statusID) {
		this.statusID = statusID;
	}

	/** Set the type of security to apply to this query.
	 *
	 * @param securityType The type of security to apply to this query.
	 */
	public void setSecurityType(String securityType) {
		this.securityType = securityType;
	}

	/** Set a note about the revision history of this query.
	 *
	 * @param revisionNote A note about the revision history of this query.
	 */
	public void setRevisionNote(String revisionNote) {
		this.revisionNote = revisionNote;
	}

	/** Set the ID of the data dictionary used by this query.
	 *
	 * @param dataDictID The ID of the data dictionary used by this query.
	 */
	public void setDataDictID(String dataDictID) {
		this.dataDictID = dataDictID;
	}

	public Node toXML(Document doc) throws DOMException {
		Element root = doc.createElement("queryAttributes");
		XML.add(root, "queryId", getID());
		XML.add(root, "queryTitle", getTitle());
		XML.add(root, "queryDesc", getDescription());
		XML.add(root, "queryType", getType());
		XML.add(root, "queryStatusId", getStatusID());
		XML.add(root, "querySecurityType", getSecurityType());
		XML.add(root, "queryRevisionNote", getRevisionNote());
		XML.add(root, "queryDataDictId", getDataDictID());
		return root;
	}

	public int hashCode() {
		return getID().hashCode();
	}

	public boolean equals(Object rhs) {
		if (rhs == this) {
		  return true;
		}
		if (rhs == null || !(rhs instanceof QueryHeader)) {
		  return false;
		}
		QueryHeader obj = (QueryHeader) rhs;
		return getID().equals(obj.getID());
	}

	public Object clone() {
		Object rc = null;
		try {
			rc = super.clone();
		} catch (CloneNotSupportedException ignored) {}
		return rc;
	}

	public String toString() {
		return getClass().getName() + "[id=" + id + "]";
	}

	/** The identification of this query. */
	private String id;

	/** The title of this query. */
	private String title;

	/** A string describing this query. */
	private String description;

	/** The type of the query, usually QUERY. */
	private String type;

	/** The status of the query. */
	private String statusID;

	/** The type of security to apply to this query. */
	private String securityType;

	/** A note about the revision history of this query. */
	private String revisionNote;

	/** The ID of the data dictionary used by this query. */
	private String dataDictID;

	/** Serial version unique ID. */
        static final long serialVersionUID = -8601229234696670816L;
}
