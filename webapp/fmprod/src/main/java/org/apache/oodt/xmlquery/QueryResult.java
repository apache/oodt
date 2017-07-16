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
import org.apache.oodt.product.Retriever;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Results of a query.
 *
 * @author Kelly
 */
public class QueryResult implements Serializable, Cloneable, Documentable {
	/**
	 * Create empty results.
	 */
	public QueryResult() {
		this.list = new ArrayList();
	}

	/**
	 * Create results from the given list of results.
	 *
	 * @param list List of {@link Result}s.
	 */
	public QueryResult(List list) {
		this.list = list;
	}

	/**
	 * Create results from an XML node.
	 *
	 * @param node The &lt;queryResultSet&gt; node.
	 */
	public QueryResult(Node node) {
		if (!"queryResultSet".equals(node.getNodeName())) {
		  throw new IllegalArgumentException("QueryResult must be constructed from <queryResultSet> node, not <"
											 + node.getNodeName() + ">");
		}
		list = new ArrayList();
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && "resultElement".equals(child.getNodeName())) {
			  list.add(new Result(child));
			}
		}
	}

	/**
	 * Get the list of results.
	 *
	 * @return A list of {@link Result}s.
	 */
	public List getList() {
		return list;
	}

	/**
	 * Clear out any results.
	 */
	public void clear() {
		list.clear();
	}

	public Node toXML(Document doc) throws DOMException {
		Element root = doc.createElement("queryResultSet");
	  for (Object aList : list) {
		Result r = (Result) aList;
		root.appendChild(r.toXML(doc));
	  }
		return root;
	}

	public int hashCode() {
		return list.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj == this) {
		  return true;
		}
		if (obj instanceof QueryResult) {
			QueryResult rhs = (QueryResult) obj;
			return list.equals(rhs.list);
		}
		return false;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException cantHappen) {
			throw new IllegalStateException("CloneNotSupportedException happened: " + cantHappen.getMessage());
		}
	}

	public String toString() {
		return getClass().getName() + "[list=" + list + "]";
	}

	public void setRetriever(Retriever retriever) {
	  for (Object aList : list) {
		Result r = (Result) aList;
		r.setRetriever(retriever);
	  }
	}

	public long getSize() {
		long size = 0;
	  for (Object aList : list) {
		Result r = (Result) aList;
		size += r.getSize();
	  }
		return size;
	}

	/** List of {@link Result}s. */
	private List list;

	/** Serial version unique ID. */
	static final long serialVersionUID = 9156030927051226848L;


}
