// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: XMLStorage.java,v 1.1 2004-03-02 19:28:59 kelly Exp $

package jpl.eda.activity;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Storage that saves activities as XML documents.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public abstract class XMLStorage implements Storage {
	/**
	 * Creates a new {@link XMLStorage} instance.
	 */
	protected XMLStorage() {
		factory = DocumentBuilderFactory.newInstance();
	}

	public void store(String id, List incidents) {
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element root = doc.createElement("activity");
			root.setAttribute("id", id);
			doc.appendChild(root);

			for (Iterator i = incidents.iterator(); i.hasNext();) {
				Incident incident = (Incident) i.next();
				Element e = doc.createElement(incident.getClass().getName());
				e.setAttribute("time", String.valueOf(incident.getTime().getTime()));
				e.appendChild(doc.createTextNode(incident.toString()));
				root.appendChild(e);
			}
			saveDocument(doc);
		} catch (ParserConfigurationException ex) {
			throw new IllegalStateException("Unexpected ParserConfigurationException: " + ex.getMessage());
		} catch (IOException ex) {
			System.err.println("Unable to save activity " + id + " due to " + ex.getClass().getName() + ": "
				+ ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Save a completed activity record.
	 *
	 * @param doc XML document containing the activity's incidents.
	 * @throws IOException if an error occurs.
	 */
	protected abstract void saveDocument(Document doc) throws IOException;

	/** Factory for document builders which we use to create XML documents. */
	protected DocumentBuilderFactory factory;
}
