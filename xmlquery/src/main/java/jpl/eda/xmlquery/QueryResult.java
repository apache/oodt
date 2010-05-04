// Copyright 2002-2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: QueryResult.java,v 1.1.1.1 2004-03-02 19:37:15 kelly Exp $

package jpl.eda.xmlquery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jpl.eda.product.Retriever;
import jpl.eda.util.Documentable;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
		if (!"queryResultSet".equals(node.getNodeName()))
			throw new IllegalArgumentException("QueryResult must be constructed from <queryResultSet> node, not <"
				+ node.getNodeName() + ">");
		list = new ArrayList();
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && "resultElement".equals(child.getNodeName()))
				list.add(new Result(child));
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
		for (Iterator i = list.iterator(); i.hasNext();) {
			Result r = (Result) i.next();
			root.appendChild(r.toXML(doc));
		}
		return root;
	}

	public int hashCode() {
		return list.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj == this) return true;
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
		for (Iterator i = list.iterator(); i.hasNext();) {
			Result r = (Result) i.next();
			r.setRetriever(retriever);
		}
	}

	public long getSize() {
		long size = 0;
		for (Iterator i = list.iterator(); i.hasNext();) {
			Result r = (Result) i.next();
			size += r.getSize();
		}
		return size;
	}

	/** List of {@link Result}s. */
	private List list;

	/** Serial version unique ID. */
	static final long serialVersionUID = 9156030927051226848L;
}
