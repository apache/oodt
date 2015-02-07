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

package org.apache.oodt.commons.activity;

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
